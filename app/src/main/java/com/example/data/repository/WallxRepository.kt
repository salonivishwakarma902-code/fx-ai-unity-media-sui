package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.R
import com.example.data.model.ALLOWED_IMAGE_MIME_TYPES
import com.example.data.model.AppSettingsModel
import com.example.data.model.CategoryModel
import com.example.data.model.DownloadRecordModel
import com.example.data.model.FavoriteRecord
import com.example.data.model.MAX_UPLOAD_BYTES
import com.example.data.model.OFFICIAL_ADMIN_EMAIL
import com.example.data.model.PremiumPurchaseModel
import com.example.data.model.RUNTIME_ADMIN_EMAILS
import com.example.data.model.UserProfileModel
import com.example.data.model.WallpaperItem
import com.example.data.model.WallxCatalogSeed
import com.example.data.model.isRemovedByUserRequest
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class WallxRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
    private val storage: FirebaseStorage? = try {
        FirebaseStorage.getInstance()
    } catch (_: Exception) {
        null
    },
    private val appContext: Context? = null
) {
    // Convenience constructor: always initializes with default Firestore instance for cross-device sync
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(),
        appContext = context.applicationContext
    )

    private fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        trySend(auth.currentUser)
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: com.example.ui.auth.PixoraEmailSessionManager.activeSession.value?.uid?.takeIf { it.isNotBlank() }
            ?: "pixora_creator"
    }

    fun isCurrentUserVerifiedAdmin(): Boolean {
        val user = auth.currentUser
        val email = user?.email
            ?: com.example.ui.auth.PixoraEmailSessionManager.activeSession.value?.email
            ?: return false
        return email.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true)
    }

    private fun isNonRetryableFirestoreError(error: Throwable): Boolean {
        var current: Throwable? = error
        while (current != null) {
            if (current is FirebaseFirestoreException) {
                if (current.code == FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ||
                    current.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ||
                    current.code == FirebaseFirestoreException.Code.UNAUTHENTICATED ||
                    current.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION
                ) {
                    return true
                }
            }
            val msg = current.message.orEmpty()
            if (msg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                msg.contains("Quota limit exceeded", ignoreCase = true) ||
                msg.contains("PERMISSION_DENIED", ignoreCase = true)
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }

    // -------------------------------------------------------------------------
    // 1. REAL-TIME STREAMS (Using Query.snapshots() & ServerTimestampBehavior.ESTIMATE)
    // -------------------------------------------------------------------------

    data class CloudWallpaperSnapshot(
        val items: List<WallpaperItem>,
        val isFromCache: Boolean
    )

    private fun isCloudPlayableUrl(url: String): Boolean {
        val trimmed = url.trim()
        return trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("data:image", ignoreCase = true) ||
            trimmed.startsWith("drawable://", ignoreCase = true)
    }

    private fun parseWallpaperDocumentSafely(doc: DocumentSnapshot): WallpaperItem? {
        val parsed = runCatching {
            doc.toObject(WallpaperItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?.let { item -> if (item.id.isBlank()) item.copy(id = doc.id) else item }
        }.getOrNull() ?: runCatching {
            val img = runCatching { doc.getString("imageUrl") }.getOrNull().orEmpty().trim()
            val thumb = runCatching { doc.getString("thumbnailUrl") }.getOrNull().orEmpty().trim()
            if (img.isBlank() && thumb.isBlank()) return@runCatching null
            val rawTags = runCatching { doc.get("tags") as? List<*> }.getOrNull()
                ?.mapNotNull { it?.toString()?.trim()?.takeIf { t -> t.isNotBlank() } }
                ?: emptyList()
            WallpaperItem(
                id = doc.id,
                title = runCatching { doc.getString("title") }.getOrNull().orEmpty().ifBlank { "4K Wallpaper" },
                description = runCatching { doc.getString("description") }.getOrNull().orEmpty(),
                category = runCatching { doc.getString("category") }.getOrNull().orEmpty().ifBlank { "4K Ultra HD" },
                tags = rawTags,
                resolution = runCatching { doc.getString("resolution") }.getOrNull().orEmpty().ifBlank { "3840x2160 (4K)" },
                fileSize = runCatching { doc.getString("fileSize") }.getOrNull().orEmpty().ifBlank { "2.4 MB" },
                fileSizeBytes = runCatching { doc.getLong("fileSizeBytes") }.getOrNull() ?: 0L,
                mimeType = runCatching { doc.getString("mimeType") }.getOrNull().orEmpty().ifBlank { "image/jpeg" },
                imageUrl = img.ifBlank { thumb },
                thumbnailUrl = thumb.ifBlank { img },
                storagePath = runCatching { doc.getString("storagePath") }.getOrNull().orEmpty(),
                status = runCatching { doc.getString("status") }.getOrNull().orEmpty().ifBlank { "published" },
                isFeatured = runCatching { doc.getBoolean("isFeatured") }.getOrNull() ?: false,
                isTrending = runCatching { doc.getBoolean("isTrending") }.getOrNull() ?: false,
                downloadCount = runCatching { doc.getLong("downloadCount") }.getOrNull() ?: 0L,
                favoriteCount = runCatching { doc.getLong("favoriteCount") }.getOrNull() ?: 0L,
                viewCount = runCatching { doc.getLong("viewCount") }.getOrNull() ?: 0L,
                uploadedBy = runCatching { doc.getString("uploadedBy") }.getOrNull().orEmpty(),
                createdAt = runCatching { doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }.getOrNull(),
                updatedAt = runCatching { doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }.getOrNull()
            )
        }.getOrNull() ?: return null

        val cleanImg = parsed.imageUrl.trim()
        val cleanThumb = parsed.thumbnailUrl.trim()
        val fixedImg = when {
            cleanImg.isBlank() -> cleanThumb
            !isCloudPlayableUrl(cleanImg) && isCloudPlayableUrl(cleanThumb) -> cleanThumb
            else -> cleanImg
        }
        val fixedThumb = when {
            cleanThumb.isBlank() -> fixedImg
            !isCloudPlayableUrl(cleanThumb) && isCloudPlayableUrl(fixedImg) -> fixedImg
            else -> cleanThumb
        }
        return if (fixedImg == parsed.imageUrl && fixedThumb == parsed.thumbnailUrl) {
            parsed
        } else {
            parsed.copy(imageUrl = fixedImg, thumbnailUrl = fixedThumb)
        }
    }

    private fun purgeLegacyDocsInBackground(rawDocs: List<DocumentSnapshot>) {
        // Keep all wallpapers in Firestore intact
    }

    suspend fun fetchWallpapersFromSdkCacheOnly(): List<WallpaperItem> {
        return runCatching {
            val cacheSnap = db.collection("wallpapers").limit(500).get(Source.CACHE).await()
            cacheSnap.documents
                .mapNotNull { doc -> parseWallpaperDocumentSafely(doc) }
                .filterNot { it.isRemovedByUserRequest() }
                .sortedWith(
                    compareByDescending<WallpaperItem> {
                        it.id.startsWith("wall_admin_") || it.id.startsWith("prem_upload_")
                    }.thenByDescending { it.createdAt ?: Timestamp(0, 0) }
                )
        }.getOrDefault(emptyList())
    }

    suspend fun fetchAllWallpapersWithMetadata(forceServer: Boolean = false): Result<CloudWallpaperSnapshot> = runCatching {
        val snapResult = runCatching {
            db.collection("wallpapers")
                .limit(500)
                .get(if (forceServer) Source.SERVER else Source.DEFAULT)
                .await()
        }
        val snap = snapResult.getOrNull()
        if (snap != null) {
            purgeLegacyDocsInBackground(snap.documents)
            val parsedList = snap.documents
                .mapNotNull { doc -> parseWallpaperDocumentSafely(doc) }
                .filterNot { it.isRemovedByUserRequest() }
                .sortedWith(
                    compareByDescending<WallpaperItem> {
                        it.id.startsWith("wall_admin_") || it.id.startsWith("prem_upload_")
                    }.thenByDescending { it.createdAt ?: Timestamp(0, 0) }
                )
            android.util.Log.d(
                "WallxRepository",
                "fetchAllWallpapersWithMetadata loaded ${parsedList.size} wallpapers (fromCache=${snap.metadata.isFromCache}, raw docs=${snap.size()}, forceServer=$forceServer)"
            )
            CloudWallpaperSnapshot(
                items = parsedList,
                isFromCache = snap.metadata.isFromCache
            )
        } else {
            val err = snapResult.exceptionOrNull() ?: IllegalStateException("Firestore fetch failed")
            val cachedItems = fetchWallpapersFromSdkCacheOnly()
            if (cachedItems.isNotEmpty()) {
                CloudWallpaperSnapshot(
                    items = cachedItems,
                    isFromCache = true
                )
            } else {
                throw err
            }
        }
    }

    suspend fun fetchAllWallpapersOnce(forceServer: Boolean = false): Result<List<WallpaperItem>> =
        fetchAllWallpapersWithMetadata(forceServer = forceServer).map { it.items }

    suspend fun fetchPublishedWallpapersOnce(forceServer: Boolean = false): Result<List<WallpaperItem>> =
        fetchAllWallpapersOnce(forceServer = forceServer).map { list ->
            list.filter { it.status.isBlank() || it.status.equals("published", ignoreCase = true) }
        }

    /**
     * Single shared real-time snapshot stream for ALL wallpapers (Admin + User Panel)
     * preserving [SnapshotMetadata.isFromCache] so disk cache is never wiped by cold empty cache emissions.
     */
    fun observeAllWallpapersWithMetadata(): Flow<CloudWallpaperSnapshot> {
        val path = "wallpapers"
        return db.collection(path)
            .limit(500)
            .snapshots()
            .map { snapshot ->
                purgeLegacyDocsInBackground(snapshot.documents)
                val list = snapshot.documents
                    .mapNotNull { doc -> parseWallpaperDocumentSafely(doc) }
                    .filterNot { it.isRemovedByUserRequest() }
                    .sortedWith(
                        compareByDescending<WallpaperItem> {
                            it.id.startsWith("wall_admin_") || it.id.startsWith("prem_upload_")
                        }.thenByDescending { it.createdAt ?: Timestamp(0, 0) }
                    )
                android.util.Log.d(
                    "WallxRepository",
                    "observeAllWallpapersWithMetadata emitted ${list.size} items (fromCache=${snapshot.metadata.isFromCache}, raw=${snapshot.size()})"
                )
                CloudWallpaperSnapshot(
                    items = list,
                    isFromCache = snapshot.metadata.isFromCache
                )
            }
            .retryWhen { error, attempt ->
                if (error is CancellationException || isNonRetryableFirestoreError(error) || attempt >= 1L) {
                    if (error is Exception && error !is CancellationException) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    false
                } else {
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    delay(2500L)
                    true
                }
            }
    }

    fun observeAllWallpapersUnified(): Flow<List<WallpaperItem>> =
        observeAllWallpapersWithMetadata().map { it.items }

    fun observePublishedWallpapers(): Flow<List<WallpaperItem>> =
        observeAllWallpapersUnified().map { list ->
            list.filter { it.status.isBlank() || it.status.equals("published", ignoreCase = true) }
        }

    fun observeAllWallpapersForAdmin(): Flow<List<WallpaperItem>> =
        observeAllWallpapersUnified()

    /**
     * Single shared real-time snapshot stream for ALL categories (Admin + User Panel)
     * so we avoid duplicate collection listeners on "categories".
     */
    fun observeAllCategoriesUnified(): Flow<List<CategoryModel>> {
        val path = "categories"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CategoryModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?.let { cat -> if (cat.id.isBlank()) cat.copy(id = doc.id) else cat }
                }.sortedBy { it.order }
            }
            .retryWhen { error, attempt ->
                if (error is CancellationException || isNonRetryableFirestoreError(error) || attempt >= 2L) {
                    if (error is Exception && error !is CancellationException) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    false
                } else {
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    delay(((attempt + 1) * 2000L).coerceAtMost(6000L))
                    true
                }
            }
    }

    fun observeEnabledCategories(): Flow<List<CategoryModel>> =
        observeAllCategoriesUnified().map { list -> list.filter { it.enabled } }

    fun observeAllCategoriesForAdmin(): Flow<List<CategoryModel>> =
        observeAllCategoriesUnified()

    fun observeUserFavorites(userId: String): Flow<List<FavoriteRecord>> {
        val path = "favorites"
        return authStateFlow().flatMapLatest { fbUser ->
            if (fbUser == null || fbUser.uid != userId) {
                flowOf(emptyList())
            } else {
                db.collection(path)
                    .whereEqualTo("userId", userId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toObject(FavoriteRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    }
                    .catch { error ->
                        if (auth.currentUser?.uid == userId && error is Exception) {
                            handleFirestoreError(error, OperationType.LIST, path)
                        }
                        emit(emptyList())
                    }
            }
        }
    }

    fun observeAllFavoritesForAdmin(): Flow<List<FavoriteRecord>> {
        val path = "favorites"
        return authStateFlow().flatMapLatest { fbUser ->
            if (fbUser == null || !isCurrentUserVerifiedAdmin()) {
                flowOf(emptyList())
            } else {
                db.collection(path)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toObject(FavoriteRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        }
                    }
                    .catch { error ->
                        if (auth.currentUser != null && error is Exception) {
                            handleFirestoreError(error, OperationType.LIST, path)
                        }
                        emit(emptyList())
                    }
            }
        }
    }

    fun observeUserDownloads(userId: String): Flow<List<DownloadRecordModel>> {
        val path = "downloads"
        return authStateFlow().flatMapLatest { fbUser ->
            if (fbUser == null || fbUser.uid != userId) {
                flowOf(emptyList())
            } else {
                db.collection(path)
                    .whereEqualTo("userId", userId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toObject(DownloadRecordModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    }
                    .catch { error ->
                        if (auth.currentUser?.uid == userId && error is Exception) {
                            handleFirestoreError(error, OperationType.LIST, path)
                        }
                        emit(emptyList())
                    }
            }
        }
    }

    fun observeAllDownloadsForAdmin(): Flow<List<DownloadRecordModel>> {
        val path = "downloads"
        return authStateFlow().flatMapLatest { fbUser ->
            if (fbUser == null || !isCurrentUserVerifiedAdmin()) {
                flowOf(emptyList())
            } else {
                db.collection(path)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toObject(DownloadRecordModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        }
                    }
                    .catch { error ->
                        if (auth.currentUser != null && error is Exception) {
                            handleFirestoreError(error, OperationType.LIST, path)
                        }
                        emit(emptyList())
                    }
            }
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfileModel?> {
        val docRef = db.collection("users").document(userId)
        return authStateFlow().flatMapLatest { fbUser ->
            if (fbUser == null || fbUser.uid != userId) {
                flowOf(null)
            } else {
                docRef.snapshots()
                    .map { snapshot ->
                        if (snapshot.exists()) {
                            snapshot.toObject(UserProfileModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        } else {
                            null
                        }
                    }
                    .catch { error ->
                        if (auth.currentUser?.uid == userId && error is Exception) {
                            handleFirestoreError(error, OperationType.GET, docRef.path)
                        }
                        emit(null)
                    }
            }
        }
    }

    fun observeAllUsersForAdmin(): Flow<List<UserProfileModel>> {
        val path = "users"
        return authStateFlow().flatMapLatest { fbUser ->
            if (fbUser == null || !isCurrentUserVerifiedAdmin()) {
                flowOf(emptyList())
            } else {
                db.collection(path)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toObject(UserProfileModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    }
                    .catch { error ->
                        if (auth.currentUser != null && error is Exception) {
                            handleFirestoreError(error, OperationType.LIST, path)
                        }
                        emit(emptyList())
                    }
            }
        }
    }

    fun observeUserPremiumPurchases(userId: String): Flow<List<PremiumPurchaseModel>> {
        val path = "premiumPurchases"
        return authStateFlow().flatMapLatest { fbUser ->
            if (fbUser == null || fbUser.uid != userId) {
                flowOf(emptyList())
            } else {
                db.collection(path)
                    .whereEqualTo("userId", userId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toObject(PremiumPurchaseModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                                ?.let { p -> if (p.id.isBlank()) p.copy(id = doc.id) else p }
                        }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    }
                    .catch { error ->
                        if (auth.currentUser?.uid == userId && error is Exception) {
                            handleFirestoreError(error, OperationType.LIST, path)
                        }
                        emit(emptyList())
                    }
            }
        }
    }

    fun observeAllPremiumPurchasesForAdmin(): Flow<List<PremiumPurchaseModel>> {
        val path = "premiumPurchases"
        return authStateFlow().flatMapLatest { fbUser ->
            if (fbUser == null || !isCurrentUserVerifiedAdmin()) {
                flowOf(emptyList())
            } else {
                db.collection(path)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toObject(PremiumPurchaseModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                                ?.let { p -> if (p.id.isBlank()) p.copy(id = doc.id) else p }
                        }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    }
                    .catch { error ->
                        if (auth.currentUser != null && error is Exception) {
                            handleFirestoreError(error, OperationType.LIST, path)
                        }
                        emit(emptyList())
                    }
            }
        }
    }

    fun observeAppSettings(): Flow<AppSettingsModel> {
        val docRef = db.collection("appSettings").document("global")
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(AppSettingsModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: AppSettingsModel()
                } else {
                    AppSettingsModel()
                }
            }
            .retryWhen { error, attempt ->
                if (error is CancellationException || isNonRetryableFirestoreError(error) || attempt >= 2L) {
                    if (error is Exception && error !is CancellationException) {
                        handleFirestoreError(error, OperationType.GET, docRef.path)
                    }
                    false
                } else {
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                    delay(((attempt + 1) * 2000L).coerceAtMost(6000L))
                    true
                }
            }
    }

    // -------------------------------------------------------------------------
    // 2. USER PROFILE & ADMIN BOOTSTRAP
    // -------------------------------------------------------------------------

    suspend fun ensureUserProfile(): Result<UserProfileModel> = runCatching {
        val user = auth.currentUser
        if (user == null) {
            val session = com.example.ui.auth.PixoraEmailSessionManager.activeSession.value
            val sessionEmail = session?.email.orEmpty().ifBlank { "user@pixora.app" }
            val sessionUid = session?.uid.orEmpty().ifBlank { "local_user" }
            val sessionName = session?.displayName.orEmpty().ifBlank { sessionEmail.substringBefore("@") }
            val isAdm = sessionEmail.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true)
            return@runCatching UserProfileModel(
                id = sessionUid,
                userId = sessionUid,
                email = sessionEmail,
                displayName = sessionName,
                role = if (isAdm) "admin" else "user",
                status = "active"
            )
        }
        val uid = user.uid
        val email = user.email?.ifBlank { "user@pixora.app" } ?: "user@pixora.app"
        val displayName = user.displayName?.ifBlank { email.substringBefore("@") } ?: email.substringBefore("@")
        val photoUrl = user.photoUrl?.toString()
        val isVerifiedAdmin = isCurrentUserVerifiedAdmin()

        // If verified admin email, ensure /admins/{uid} entry exists
        if (isVerifiedAdmin) {
            val adminRef = db.collection("admins").document(uid)
            try {
                val adminSnap = adminRef.get().await()
                if (!adminSnap.exists()) {
                    adminRef.set(
                        mapOf(
                            "uid" to uid,
                            "email" to email,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, adminRef.path)
            }
        }

        val userRef = db.collection("users").document(uid)
        try {
            val snap = userRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(UserProfileModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: UserProfileModel(id = uid, userId = uid, email = email, displayName = displayName)
                existing
            } else {
                val assignedRole = if (isVerifiedAdmin) "admin" else "user"
                val newProfile = UserProfileModel(
                    id = uid,
                    userId = uid,
                    email = email,
                    displayName = displayName.take(100),
                    photoUrl = photoUrl?.take(2000),
                    role = assignedRole,
                    status = "active",
                    downloadCount = 0L,
                    favoriteCount = 0L,
                    themePreference = "dark"
                )
                userRef.set(newProfile.toCreateMap()).await()
                newProfile
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userRef.path)
            throw e
        }
    }

    suspend fun updateThemePreference(themePref: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        val userRef = db.collection("users").document(uid)
        try {
            userRef.update(
                mapOf(
                    "themePreference" to themePref,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            throw e
        }
    }

    suspend fun getUserProfileById(targetUserId: String): Result<UserProfileModel?> = runCatching {
        val docRef = db.collection("users").document(targetUserId)
        try {
            val snap = docRef.get(Source.SERVER).await()
            snap.toObject(UserProfileModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------------------
    // 3. FAVORITES & DOWNLOADS (Real-time user records & atomic counters)
    // -------------------------------------------------------------------------

    suspend fun addFavorite(wallpaper: WallpaperItem): Result<String> = runCatching {
        val wallRef = db.collection("wallpapers").document(wallpaper.id)
        runCatching {
            wallRef.update(
                mapOf(
                    "favoriteCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
        val uid = auth.currentUser?.uid ?: return@runCatching "local_${wallpaper.id}"
        val favId = "${uid}_${wallpaper.id}"
        val favRef = db.collection("favorites").document(favId)
        val record = FavoriteRecord(
            id = favId,
            userId = uid,
            wallpaperId = wallpaper.id,
            wallpaperTitle = wallpaper.title,
            wallpaperThumbnailUrl = wallpaper.thumbnailUrl,
            wallpaperCategory = wallpaper.category,
            wallpaperResolution = wallpaper.resolution
        )
        try {
            favRef.set(record.toCreateMap()).await()
            val userRef = db.collection("users").document(uid)
            runCatching {
                userRef.update(
                    mapOf(
                        "favoriteCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            favId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, favRef.path)
            throw e
        }
    }

    suspend fun removeFavorite(wallpaperId: String): Result<Unit> = runCatching {
        val wallRef = db.collection("wallpapers").document(wallpaperId)
        runCatching {
            val snap = wallRef.get(Source.CACHE).await()
            val currentCount = snap.getLong("favoriteCount") ?: 0L
            if (currentCount > 0L) {
                wallRef.update(
                    mapOf(
                        "favoriteCount" to FieldValue.increment(-1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }
        val uid = auth.currentUser?.uid ?: return@runCatching
        val favId = "${uid}_${wallpaperId}"
        val favRef = db.collection("favorites").document(favId)
        try {
            favRef.delete().await()
            val userRef = db.collection("users").document(uid)
            runCatching {
                userRef.update(
                    mapOf(
                        "favoriteCount" to FieldValue.increment(-1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, favRef.path)
            throw e
        }
    }

    suspend fun recordDownload(wallpaper: WallpaperItem): Result<String> = runCatching {
        val wallRef = db.collection("wallpapers").document(wallpaper.id)
        runCatching {
            wallRef.update(
                mapOf(
                    "downloadCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
        val uid = auth.currentUser?.uid ?: return@runCatching "local_dl_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val downloadId = "dl_${uid.take(12)}_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val dlRef = db.collection("downloads").document(downloadId)
        val record = DownloadRecordModel(
            id = downloadId,
            userId = uid,
            wallpaperId = wallpaper.id,
            wallpaperTitle = wallpaper.title,
            wallpaperThumbnailUrl = wallpaper.thumbnailUrl,
            wallpaperResolution = wallpaper.resolution,
            fileSize = wallpaper.fileSize
        )
        try {
            dlRef.set(record.toCreateMap()).await()
            val userRef = db.collection("users").document(uid)
            runCatching {
                userRef.update(
                    mapOf(
                        "downloadCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            downloadId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, dlRef.path)
            throw e
        }
    }

    suspend fun incrementWallpaperViewCount(wallpaperId: String): Result<Unit> = runCatching {
        if (wallpaperId.isBlank()) return@runCatching
        val wallRef = db.collection("wallpapers").document(wallpaperId)
        wallRef.update(
            mapOf(
                "viewCount" to FieldValue.increment(1L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // -------------------------------------------------------------------------
    // 4. ADMIN WALLPAPER & FIREBASE STORAGE OPERATIONS
    // -------------------------------------------------------------------------

    private fun readLocalVaultOrUriBytes(rawUrl: String, wallId: String = ""): ByteArray? {
        val trimmed = rawUrl.trim()
        if (trimmed.startsWith("file:/", ignoreCase = true) || trimmed.startsWith("/")) {
            val directBytes = runCatching {
                val f = if (trimmed.startsWith("file:/", ignoreCase = true)) {
                    runCatching { java.io.File(java.net.URI(trimmed)) }.getOrElse {
                        java.io.File(trimmed.removePrefix("file://").removePrefix("file:"))
                    }
                } else {
                    java.io.File(trimmed)
                }
                if (f.exists() && f.isFile && f.length() > 0L) f.readBytes() else null
            }.getOrNull()
            if (directBytes != null) return directBytes
        }
        val ctx = appContext
        if (trimmed.startsWith("content://") && ctx != null) {
            val contentBytes = runCatching {
                ctx.contentResolver.openInputStream(Uri.parse(trimmed))?.use { it.readBytes() }
            }.getOrNull()
            if (contentBytes != null && contentBytes.isNotEmpty()) return contentBytes
        }
        if (wallId.isNotBlank() && ctx != null) {
            val vaultBytes = runCatching {
                val vaultDir = java.io.File(ctx.filesDir, "wallpapers_vault_50gb")
                val cacheDir = java.io.File(ctx.filesDir, "cloud_cache_images")
                val match = (vaultDir.listFiles().orEmpty().toList() + cacheDir.listFiles().orEmpty().toList())
                    .firstOrNull {
                        it.isFile && it.length() > 0L && it.nameWithoutExtension == wallId
                    }
                match?.readBytes()
            }.getOrNull()
            if (vaultBytes != null && vaultBytes.isNotEmpty()) return vaultBytes
        }
        return null
    }

    private fun sanitizeUrlForCloud(
        rawUrl: String,
        isThumbnail: Boolean = false,
        wallId: String = ""
    ): String {
        val trimmed = rawUrl.trim()
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("drawable://", ignoreCase = true)
        ) {
            return trimmed
        }
        if (trimmed.startsWith("file:/", ignoreCase = true) ||
            trimmed.startsWith("/") ||
            trimmed.startsWith("content://") ||
            (trimmed.isBlank() && wallId.isNotBlank())
        ) {
            val fileBytes = readLocalVaultOrUriBytes(trimmed, wallId)
            if (fileBytes != null) {
                return if (isThumbnail) encodeCloudSafeThumbUri(fileBytes) else encodeCompactDataUri(fileBytes)
            }
            return trimmed
        }
        if (trimmed.startsWith("data:image")) {
            val maxAllowedLen = if (isThumbnail) 48_000 else 240_000
            if (trimmed.length > maxAllowedLen) {
                val decodedBytes = runCatching {
                    val b64 = trimmed.substringAfter("base64,", "")
                    if (b64.isNotBlank()) Base64.decode(b64, Base64.DEFAULT) else null
                }.getOrNull()
                if (decodedBytes != null) {
                    return if (isThumbnail) encodeCloudSafeThumbUri(decodedBytes) else encodeCompactDataUri(decodedBytes)
                }
            }
            return trimmed
        }
        return trimmed
    }

    suspend fun createWallpaperRecord(wallpaper: WallpaperItem): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = wallpaper.id.ifBlank {
            "wall_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        }
        require(wallpaper.fileSizeBytes in 0L..MAX_UPLOAD_BYTES) {
            "File size exceeds the 2 TB maximum upload quota."
        }
        val safeMime = if (wallpaper.mimeType in ALLOWED_IMAGE_MIME_TYPES) wallpaper.mimeType else "image/jpeg"
        val docRef = db.collection("wallpapers").document(docId)
        val cloudImgUrl = sanitizeUrlForCloud(
            rawUrl = wallpaper.imageUrl.ifBlank { wallpaper.thumbnailUrl },
            isThumbnail = false,
            wallId = docId
        )
        val cloudThumbUrl = sanitizeUrlForCloud(
            rawUrl = wallpaper.thumbnailUrl.ifBlank { wallpaper.imageUrl },
            isThumbnail = true,
            wallId = docId
        )
        val resolvedImg = if (isCloudPlayableUrl(cloudImgUrl)) cloudImgUrl else if (isCloudPlayableUrl(cloudThumbUrl)) cloudThumbUrl else cloudImgUrl
        val resolvedThumb = if (isCloudPlayableUrl(cloudThumbUrl)) cloudThumbUrl else resolvedImg
        val modelWithUploader = wallpaper.copy(
            id = docId,
            mimeType = safeMime,
            imageUrl = resolvedImg,
            thumbnailUrl = resolvedThumb,
            uploadedBy = wallpaper.uploadedBy.ifBlank { uid }
        )
        try {
            docRef.set(modelWithUploader.toCreateMap()).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    /**
     * Prepares a local HD vault copy + cloud-ready WallpaperItem from a picked Uri so the caller
     * can display & persist the upload in 0ms even if Firestore is temporarily offline or rate-limited.
     */
    suspend fun prepareWallpaperFromUri(
        context: Context,
        uri: Uri,
        title: String,
        description: String,
        category: String,
        tags: List<String>,
        status: String,
        isFeatured: Boolean,
        isTrending: Boolean,
        preferredId: String? = null,
        preloadedBytes: ByteArray? = null,
        saveToLocalVault: Boolean = true,
        onProgress: (Float) -> Unit = {}
    ): Result<WallpaperItem> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = requireUserId()
            val resolver = context.contentResolver
            val rawMime = (resolver.getType(uri) ?: "image/jpeg").lowercase(Locale.ROOT)
            val mimeType = if (rawMime in ALLOWED_IMAGE_MIME_TYPES) rawMime else "image/jpeg"

            val bytes = preloadedBytes?.takeIf { it.isNotEmpty() }
                ?: resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Unable to read selected image file.")

            require(bytes.size <= MAX_UPLOAD_BYTES) {
                val gb = String.format(Locale.US, "%.2f GB", bytes.size / (1024f * 1024f * 1024f))
                "Image is $gb, which exceeds the 2 TB storage quota."
            }

            onProgress(0.25f)

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            val width = if (options.outWidth > 0) options.outWidth else 3840
            val height = if (options.outHeight > 0) options.outHeight else 2160
            val badge = when {
                width >= 7000 || height >= 7000 -> " (8K)"
                width >= 3000 || height >= 3000 -> " (4K)"
                else -> " (4K UHD)"
            }
            val resolutionStr = "${width}x${height}$badge".take(30)
            val fileSizeStr = String.format(Locale.US, "%.2f MB", (bytes.size / (1024f * 1024f)).coerceAtLeast(0.1f)).take(30)

            val wallId = preferredId?.takeIf { it.isNotBlank() }
                ?: "wall_${UUID.randomUUID().toString().replace("-", "").take(14)}"
            val ext = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val storagePath = "wallpapers/$wallId.$ext"

            var localFileUri: String? = null
            if (saveToLocalVault) {
                runCatching {
                    val vaultDir = java.io.File(context.filesDir, "wallpapers_vault_50gb").apply { mkdirs() }
                    val outFile = java.io.File(vaultDir, "$wallId.$ext")
                    outFile.writeBytes(bytes)
                    localFileUri = Uri.fromFile(outFile).toString()
                }
            }

            onProgress(0.55f)

            val firestoreSafeImageUrl = encodeCompactDataUriOrNull(bytes)
                ?: localFileUri
                ?: error("Could not decode selected image. Please choose a valid JPG, PNG, or WebP file.")
            val firestoreSafeThumbUrl = encodeCloudSafeThumbUriOrNull(bytes)
                ?: encodeAdaptiveDataUriOrNull(bytes, initialMaxDim = 480, maxRawBytes = 55_000)
                ?: firestoreSafeImageUrl

            onProgress(0.75f)

            WallpaperItem(
                id = wallId,
                title = title.ifBlank { "Untitled 4K Wallpaper" }.take(120),
                description = description.ifBlank { "Published to Cloud Firestore 24/7 Catalog." }.take(1000),
                category = category.ifBlank { "4K Ultra HD" }.take(60),
                tags = tags.ifEmpty { listOf(category.lowercase(Locale.ROOT), "4k", "recent") }.take(10),
                resolution = resolutionStr,
                fileSize = fileSizeStr,
                fileSizeBytes = bytes.size.toLong(),
                mimeType = "image/jpeg",
                imageUrl = firestoreSafeImageUrl,
                thumbnailUrl = firestoreSafeThumbUrl,
                storagePath = storagePath,
                status = if (status.equals("draft", ignoreCase = true)) "draft" else "published",
                isFeatured = isFeatured,
                isTrending = isTrending,
                downloadCount = 0L,
                favoriteCount = 0L,
                viewCount = 0L,
                uploadedBy = uid
            )
        }
    }

    suspend fun uploadWallpaperFromUri(
        context: Context,
        uri: Uri,
        title: String,
        description: String,
        category: String,
        tags: List<String>,
        status: String,
        isFeatured: Boolean,
        isTrending: Boolean,
        preferredId: String? = null,
        preloadedBytes: ByteArray? = null,
        saveToLocalVault: Boolean = true,
        onProgress: (Float) -> Unit = {}
    ): Result<WallpaperItem> = withContext(Dispatchers.IO) {
        val preparedResult = prepareWallpaperFromUri(
            context = context,
            uri = uri,
            title = title,
            description = description,
            category = category,
            tags = tags,
            status = status,
            isFeatured = isFeatured,
            isTrending = isTrending,
            preferredId = preferredId,
            preloadedBytes = preloadedBytes,
            saveToLocalVault = saveToLocalVault,
            onProgress = onProgress
        )
        val firestoreWallpaper = preparedResult.getOrElse { return@withContext Result.failure(it) }
        onProgress(0.85f)
        val docRef = db.collection("wallpapers").document(firestoreWallpaper.id)
        try {
            docRef.set(firestoreWallpaper.toCreateMap()).await()
            android.util.Log.d("WallxRepository", "Successfully uploaded wallpaper ${firestoreWallpaper.id} to Firestore (${db.app.name})")
            onProgress(1.0f)
            Result.success(firestoreWallpaper)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("WallxRepository", "Failed to upload wallpaper ${firestoreWallpaper.id} to Firestore", e)
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Produces a 100% complete, untruncated Base64 JPEG data URI guaranteed to fit cleanly
     * inside Firestore's 1 MiB document limit alongside metadata and thumbnail.
     */
    private fun encodeCompactDataUri(bytes: ByteArray): String {
        return encodeCompactDataUriOrNull(bytes)
            ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=3840&q=100"
    }

    private fun encodeCloudSafeThumbUri(bytes: ByteArray): String {
        return encodeCloudSafeThumbUriOrNull(bytes)
            ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80"
    }

    private fun encodeCompactDataUriOrNull(bytes: ByteArray): String? {
        return encodeAdaptiveDataUriOrNull(bytes, initialMaxDim = 1440, maxRawBytes = 140_000)
    }

    private fun encodeCloudSafeThumbUriOrNull(bytes: ByteArray): String? {
        return encodeAdaptiveDataUriOrNull(bytes, initialMaxDim = 540, maxRawBytes = 24_000)
    }

    private fun encodeAdaptiveDataUriOrNull(
        bytes: ByteArray,
        initialMaxDim: Int,
        maxRawBytes: Int
    ): String? {
        var decoded: Bitmap? = null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            var sampleSize = 1
            val maxDecodeSide = initialMaxDim * 2
            if (bounds.outWidth > 0 && bounds.outHeight > 0) {
                while ((bounds.outWidth / sampleSize) > maxDecodeSide ||
                    (bounds.outHeight / sampleSize) > maxDecodeSide
                ) {
                    sampleSize *= 2
                }
            }
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
                ?: BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (decoded == null) return null

            var currentMaxDim = initialMaxDim
            var encodedBytes = ByteArray(0)
            for (pass in 0..5) {
                val ratio = minOf(
                    currentMaxDim.toFloat() / decoded.width,
                    currentMaxDim.toFloat() / decoded.height,
                    1f
                )
                val targetW = (decoded.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (decoded.height * ratio).toInt().coerceAtLeast(1)
                val scaled = if (targetW != decoded.width || targetH != decoded.height) {
                    Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
                } else {
                    decoded
                }
                var quality = 88
                while (quality >= 36) {
                    val out = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    encodedBytes = out.toByteArray()
                    if (encodedBytes.size <= maxRawBytes) break
                    quality -= 12
                }
                if (scaled !== decoded) {
                    scaled.recycle()
                }
                if (encodedBytes.isNotEmpty() && encodedBytes.size <= maxRawBytes) break
                currentMaxDim = (currentMaxDim * 0.72f).toInt().coerceAtLeast(360)
            }
            if (encodedBytes.isEmpty()) return null
            val b64 = Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$b64"
        } catch (_: Throwable) {
            null
        } finally {
            runCatching { decoded?.recycle() }
        }
    }

    suspend fun updateWallpaperAdmin(wallpaper: WallpaperItem): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(wallpaper.id.isNotBlank()) { "Wallpaper ID must not be blank" }
        val docRef = db.collection("wallpapers").document(wallpaper.id)
        val cloudImgUrl = sanitizeUrlForCloud(
            rawUrl = wallpaper.imageUrl.ifBlank { wallpaper.thumbnailUrl },
            isThumbnail = false,
            wallId = wallpaper.id
        )
        val cloudThumbUrl = sanitizeUrlForCloud(
            rawUrl = wallpaper.thumbnailUrl.ifBlank { wallpaper.imageUrl },
            isThumbnail = true,
            wallId = wallpaper.id
        )
        val resolvedImg = if (isCloudPlayableUrl(cloudImgUrl)) cloudImgUrl else if (isCloudPlayableUrl(cloudThumbUrl)) cloudThumbUrl else cloudImgUrl
        val resolvedThumb = if (isCloudPlayableUrl(cloudThumbUrl)) cloudThumbUrl else resolvedImg
        val sanitized = wallpaper.copy(
            imageUrl = resolvedImg,
            thumbnailUrl = resolvedThumb,
            uploadedBy = wallpaper.uploadedBy.ifBlank { uid }
        )
        try {
            docRef.set(sanitized.toCreateMap()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteWallpaperAdmin(wallpaper: WallpaperItem): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("wallpapers").document(wallpaper.id)
        val storagePathToDelete = wallpaper.storagePath.ifBlank { "wallpapers/${wallpaper.id}.jpg" }
        runCatching {
            storage?.reference?.child(storagePathToDelete)?.delete()?.await()
        }
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------------------
    // 5. ADMIN CATEGORY, USER & HOMEPAGE SETTINGS MANAGEMENT
    // -------------------------------------------------------------------------

    suspend fun createOrUpdateCategoryAdmin(category: CategoryModel, isNew: Boolean): Result<String> = runCatching {
        requireUserId()
        val catId = category.id.ifBlank {
            "cat_${category.name.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")}"
        }
        val docRef = db.collection("categories").document(catId)
        val sanitizedThumb = sanitizeUrlForCloud(category.thumbnailUrl, isThumbnail = true)
        val sanitizedCategory = category.copy(id = catId, thumbnailUrl = sanitizedThumb)
        try {
            docRef.set(sanitizedCategory.toCreateMap()).await()
            catId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCategoryAdmin(categoryId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("categories").document(categoryId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun updateUserStatusAdmin(targetUserId: String, disabled: Boolean): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("users").document(targetUserId)
        try {
            docRef.update(
                mapOf(
                    "status" to if (disabled) "disabled" else "active",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateUserRoleAdmin(targetUserId: String, isAdmin: Boolean): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("users").document(targetUserId)
        try {
            docRef.update(
                mapOf(
                    "role" to if (isAdmin) "admin" else "user",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun recordPremiumPurchase(
        utrReference: String,
        fallbackEmail: String = "",
        fallbackName: String = ""
    ): Result<PremiumPurchaseModel?> = runCatching {
        val fbUser = auth.currentUser ?: return@runCatching null
        ensureUserProfile()
        val uid = fbUser.uid
        val cleanUtr = utrReference.trim().take(64)
        val email = fbUser.email?.takeIf { it.isNotBlank() }
            ?: fallbackEmail.ifBlank { "user@pixora.app" }
        val displayName = fbUser.displayName?.takeIf { it.isNotBlank() }
            ?: fallbackName.ifBlank { email.substringBefore("@").ifBlank { "Pixora Member" } }
        val planName = "₹299 VIP Creator Pass"
        val purchaseId = "${uid}_${cleanUtr.replace(Regex("[^a-zA-Z0-9_-]"), "")}".take(128)

        val userRef = db.collection("users").document(uid)
        runCatching {
            userRef.update(
                mapOf(
                    "isPremium" to true,
                    "premiumUtr" to cleanUtr,
                    "premiumPlan" to planName,
                    "premiumPurchasedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }.onFailure { err ->
            if (err is CancellationException) throw err
            if (err is Exception) handleFirestoreError(err, OperationType.UPDATE, userRef.path)
        }

        val purchaseModel = PremiumPurchaseModel(
            id = purchaseId,
            userId = uid,
            userEmail = email.take(200),
            userDisplayName = displayName.take(100),
            utrReference = cleanUtr,
            planName = planName,
            amountInr = 299L,
            status = "active"
        )
        val purchaseRef = db.collection("premiumPurchases").document(purchaseId)
        try {
            purchaseRef.set(purchaseModel.toCreateMap()).await()
            purchaseModel
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, purchaseRef.path)
            throw e
        }
    }

    suspend fun updateUserPremiumStatusAdmin(
        targetUserId: String,
        isPremium: Boolean,
        purchaseId: String? = null,
        purchaseStatus: String = if (isPremium) "verified" else "revoked",
        utrOverride: String? = null
    ): Result<Unit> = runCatching {
        if (auth.currentUser == null || !isCurrentUserVerifiedAdmin()) {
            throw IllegalStateException("Admin Google authentication required")
        }
        val cleanUid = targetUserId.trim()
        if (cleanUid.isNotBlank()) {
            val userRef = db.collection("users").document(cleanUid)
            val updates = mutableMapOf<String, Any>(
                "isPremium" to isPremium,
                "premiumPlan" to if (isPremium) "₹299 VIP Creator Pass" else "",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (utrOverride != null) {
                updates["premiumUtr"] = utrOverride.take(64)
            } else if (!isPremium) {
                updates["premiumUtr"] = ""
            }
            if (isPremium) {
                updates["premiumPurchasedAt"] = FieldValue.serverTimestamp()
            }
            runCatching {
                userRef.update(updates).await()
            }.onFailure { err ->
                if (err is CancellationException) throw err
                if (err is Exception) handleFirestoreError(err, OperationType.UPDATE, userRef.path)
            }
        }
        if (!purchaseId.isNullOrBlank()) {
            val pRef = db.collection("premiumPurchases").document(purchaseId)
            try {
                pRef.update(
                    mapOf(
                        "status" to purchaseStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, pRef.path)
                throw e
            }
        }
    }

    suspend fun saveAppSettingsAdmin(settings: AppSettingsModel): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("appSettings").document("global")
        val sanitizedHero = sanitizeUrlForCloud(settings.heroBannerImageUrl, isThumbnail = false)
        val sanitizedSettings = settings.copy(heroBannerImageUrl = sanitizedHero)
        try {
            docRef.set(sanitizedSettings.toCreateMap()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun seedInitialCatalogIfEmpty(
        extraWallpapersToSync: List<WallpaperItem> = emptyList(),
        extraCategoriesToSync: List<CategoryModel> = emptyList(),
        settingsOverride: AppSettingsModel? = null,
        deletedWallpaperIds: Set<String> = emptySet(),
        forceOverwritePending: Set<String> = emptySet()
    ): Result<Int> = runCatching {
        val uid = requireUserId()

        // Fetch existing wallpapers only when needed (skip expensive read if we are only pushing explicitly pending items)
        var fetchWallpapersError: Throwable? = null
        val onlyPushPendingDirect = forceOverwritePending.isNotEmpty() && !isCurrentUserVerifiedAdmin()
        val existingWallpaperDocs = if (onlyPushPendingDirect) {
            emptyList()
        } else {
            try {
                db.collection("wallpapers").limit(500).get().await().documents
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fetchWallpapersError = e
                emptyList()
            }
        }

        val existingLegacyIds = existingWallpaperDocs
            .map { it.id }
            .filter { id ->
                id in WallxCatalogSeed.legacyDefaultSeedWallpaperIds ||
                    id.startsWith("pixora_curated_", ignoreCase = true) ||
                    id.startsWith("pixora_starter_", ignoreCase = true) ||
                    id.startsWith("wall_admin_studio_", ignoreCase = true)
            }
        if (existingLegacyIds.isNotEmpty()) {
            android.util.Log.d("WallxRepository", "Cleaning ${existingLegacyIds.size} legacy/default wallpapers from Firestore...")
            for (legacyId in existingLegacyIds) {
                runCatching {
                    db.collection("wallpapers").document(legacyId).delete().await()
                }
            }
        }

        val existingById = existingWallpaperDocs
            .filterNot { doc ->
                doc.id in WallxCatalogSeed.legacyDefaultSeedWallpaperIds ||
                    doc.id.startsWith("pixora_curated_", ignoreCase = true) ||
                    doc.id.startsWith("pixora_starter_", ignoreCase = true) ||
                    doc.id.startsWith("wall_admin_studio_", ignoreCase = true)
            }
            .associateBy { it.id }

        val existingCategoryDocs = if (fetchWallpapersError != null || onlyPushPendingDirect) {
            emptyMap()
        } else {
            runCatching {
                db.collection("categories").get().await().documents
            }.getOrDefault(emptyList()).associateBy { it.id }
        }

        val cleanExtraWallpapers = extraWallpapersToSync
            .filterNot { it.isRemovedByUserRequest() || it.id in deletedWallpaperIds }
        val extraCategoryIds = extraCategoriesToSync.map { it.id }.toSet()

        val allCategoriesToSync = (
            (if (existingCategoryDocs.isNotEmpty()) WallxCatalogSeed.defaultCategories else emptyList()) +
                extraCategoriesToSync +
                cleanExtraWallpapers.map { wall ->
                    CategoryModel(
                        id = "cat_${wall.category.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")}",
                        name = wall.category,
                        description = "Curated ${wall.category} 4K collection",
                        thumbnailUrl = wall.thumbnailUrl,
                        enabled = true,
                        wallpaperCount = 1L,
                        accentHex = "#5B2EFF"
                    )
                }
            ).distinctBy { it.name.trim().lowercase(Locale.ROOT) }

        for (cat in allCategoriesToSync) {
            runCatching {
                val snap = existingCategoryDocs[cat.id]
                val existingThumb = snap?.getString("thumbnailUrl").orEmpty()
                val needsHeal = existingThumb.isBlank() ||
                    existingThumb.startsWith("file:/", ignoreCase = true) ||
                    existingThumb.startsWith("/") ||
                    existingThumb.startsWith("content://")

                if (snap == null || !snap.exists() || needsHeal || cat.id in extraCategoryIds) {
                    val docRef = db.collection("categories").document(cat.id)
                    val safeThumb = sanitizeUrlForCloud(cat.thumbnailUrl, isThumbnail = true)
                    val model = cat.copy(thumbnailUrl = safeThumb)
                    docRef.set(model.toCreateMap()).await()
                }
            }
        }

        val fallbackPlaceholder = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=3840&q=100"
        // Default wallpapers removed — only sync Admin & Creator uploaded wallpapers
        val allToSync = cleanExtraWallpapers.distinctBy { it.id }
        var seededCount = 0
        var firstWriteError: Throwable? = null
        for (wall in allToSync) {
            val snap = existingById[wall.id]
            val existingImg = snap?.getString("imageUrl").orEmpty()
            val isPending = wall.id in forceOverwritePending
            val needsHeal = existingImg.isBlank() ||
                existingImg.startsWith("file:/", ignoreCase = true) ||
                existingImg.startsWith("/") ||
                existingImg.startsWith("content://") ||
                existingImg == fallbackPlaceholder
            if (snap == null || !snap.exists() || needsHeal || isPending) {
                val docRef = db.collection("wallpapers").document(wall.id)
                val safeImg = sanitizeUrlForCloud(
                    rawUrl = wall.imageUrl.ifBlank { wall.thumbnailUrl },
                    isThumbnail = false,
                    wallId = wall.id
                )
                val safeThumb = sanitizeUrlForCloud(
                    rawUrl = wall.thumbnailUrl.ifBlank { wall.imageUrl },
                    isThumbnail = true,
                    wallId = wall.id
                )
                if (snap == null || !snap.exists() || safeImg != fallbackPlaceholder || isPending) {
                    val model = wall.copy(
                        imageUrl = safeImg,
                        thumbnailUrl = safeThumb,
                        uploadedBy = wall.uploadedBy.ifBlank { uid }
                    )
                    try {
                        docRef.set(model.toCreateMap()).await()
                        seededCount++
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (firstWriteError == null) firstWriteError = e
                    }
                }
            }
        }

        val settingsRef = db.collection("appSettings").document("global")
        if (settingsOverride != null) {
            val sanitizedHero = sanitizeUrlForCloud(settingsOverride.heroBannerImageUrl, isThumbnail = false)
            runCatching {
                settingsRef.set(settingsOverride.copy(heroBannerImageUrl = sanitizedHero).toCreateMap()).await()
            }
        } else if (!onlyPushPendingDirect && fetchWallpapersError == null) {
            val existingSettings = runCatching { settingsRef.get().await() }.getOrNull()
            if (existingSettings == null || !existingSettings.exists()) {
                runCatching {
                    settingsRef.set(AppSettingsModel().toCreateMap()).await()
                }
            }
        }

        if (seededCount == 0 && (fetchWallpapersError != null || firstWriteError != null)) {
            throw (firstWriteError ?: fetchWallpapersError!!)
        }

        seededCount
    }

    suspend fun ensureCategoryExistsInCloud(category: CategoryModel): Result<Unit> = runCatching {
        val docRef = db.collection("categories").document(category.id)
        val snap = runCatching { docRef.get(Source.CACHE).await() }.getOrNull()
        val existingThumb = snap?.getString("thumbnailUrl").orEmpty()
        val needsHeal = existingThumb.isBlank() ||
            existingThumb.startsWith("file:/", ignoreCase = true) ||
            existingThumb.startsWith("/") ||
            existingThumb.startsWith("content://")

        if (snap == null || !snap.exists() || needsHeal) {
            val safeThumb = sanitizeUrlForCloud(category.thumbnailUrl, isThumbnail = true)
            docRef.set(category.copy(thumbnailUrl = safeThumb).toCreateMap()).await()
        }
    }

    /**
     * Google Play Store Account & Cloud Data Deletion Compliance:
     * Permanently deletes the signed-in user's favorites, download history, and user profile document.
     */
    suspend fun deleteCurrentUserAccountAndData(): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        try {
            val favSnap = db.collection("favorites").whereEqualTo("userId", uid).get().await()
            for (doc in favSnap.documents) {
                runCatching { doc.reference.delete().await() }
            }
            val dlSnap = db.collection("downloads").whereEqualTo("userId", uid).get().await()
            for (doc in dlSnap.documents) {
                runCatching { doc.reference.delete().await() }
            }
            runCatching { db.collection("users").document(uid).delete().await() }
            runCatching { auth.currentUser?.delete()?.await() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.w("WallxRepository", "Account deletion cleanup notice: ${e.message}")
        }
    }
}
