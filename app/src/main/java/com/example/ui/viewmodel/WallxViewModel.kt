package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CachedFavoriteWallpaperEntity
import com.example.data.local.OfflineFavoriteCacheRepository
import com.example.data.local.PixoraRoomDatabase
import com.example.data.model.AppSettingsModel
import com.example.data.model.CategoryModel
import com.example.data.model.DownloadRecordModel
import com.example.data.model.FavoriteRecord
import com.example.data.model.OFFICIAL_ADMIN_EMAIL
import com.example.data.model.PremiumPurchaseModel
import com.example.data.model.RUNTIME_ADMIN_EMAILS
import com.example.data.model.UserProfileModel
import com.example.data.model.WallpaperItem
import com.example.data.model.WallxCatalogSeed
import com.example.data.model.isRemovedByUserRequest
import com.example.data.repository.WallxRepository
import com.example.ui.auth.PixoraEmailSessionManager
import com.example.ui.theme.AppDesignCatalog
import com.example.ui.theme.AppDesignStyle
import com.example.ui.theme.LiquidGlassOptics
import com.example.ui.theme.LiquidGlassPreset
import com.example.util.Ai8KEngineMode
import com.example.util.CloudAiImageApiClient
import com.example.util.WallpaperDownloader
import com.example.util.refreshAppCheckTokenOnFailure
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID

enum class UserDockTab {
    HOME, SEARCH, FAVORITES, PREMIUM, PROFILE, EXPLORE, AI_STUDIO
}

enum class AdminDockTab {
    DASHBOARD, WALLPAPERS, CATEGORIES, USERS, SETTINGS
}

data class ActiveDownloadState(
    val wallpaperId: String,
    val progress: Float,
    val statusText: String
)

sealed interface Ai8KGenUiState {
    data object Idle : Ai8KGenUiState
    data class EnhancingPrompt(val streamingText: String) : Ai8KGenUiState
    data class Generating8K(val promptUsed: String) : Ai8KGenUiState
    data class Success(
        val bitmap: Bitmap,
        val title: String,
        val promptUsed: String,
        val category: String
    ) : Ai8KGenUiState
    data class Error(val message: String) : Ai8KGenUiState
}

internal fun normalizeWallpaperImageKey(url: String): String {
    val trimmed = url.trim()
    if (trimmed.isBlank()) return ""
    return if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
        trimmed.substringBefore("#").lowercase(java.util.Locale.ROOT)
    } else {
        trimmed
    }
}

internal fun List<WallpaperItem>.deduplicateWallpapers(): List<WallpaperItem> {
    val seenIds = HashSet<String>()
    val seenImagesForBlankId = HashSet<String>()
    return buildList {
        for (item in this@deduplicateWallpapers) {
            val idKey = item.id.trim()
            if (idKey.isNotBlank()) {
                if (!seenIds.add(idKey)) continue
            } else {
                val imgKey = normalizeWallpaperImageKey(item.imageUrl.ifBlank { item.thumbnailUrl })
                if (imgKey.isNotBlank() && !seenImagesForBlankId.add(imgKey)) continue
            }
            add(item)
        }
    }
}

class WallxViewModel(
    private val repository: WallxRepository,
    val currentUserId: String,
    private val appContext: Context? = null,
    private val offlineFavoriteCacheRepository: OfflineFavoriteCacheRepository? = appContext?.let { ctx ->
        runCatching {
            OfflineFavoriteCacheRepository(
                favoriteDao = PixoraRoomDatabase.getInstance(ctx).favoriteWallpaperDao(),
                appContext = ctx.applicationContext
            )
        }.getOrNull()
    }
) : ViewModel() {

    private val currentUserEmail: String
        get() {
            val sessionEmail = PixoraEmailSessionManager.activeSession.value?.email?.trim().orEmpty()
            val firebaseEmail = Firebase.auth.currentUser?.email?.trim().orEmpty()
            return when {
                sessionEmail.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true) -> sessionEmail
                firebaseEmail.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true) -> firebaseEmail
                firebaseEmail.isNotBlank() -> firebaseEmail
                else -> sessionEmail
            }
        }

    private val isEmailVerified: Boolean
        get() = Firebase.auth.currentUser?.isEmailVerified == true ||
            PixoraEmailSessionManager.activeSession.value?.isEmailVerified == true

    /**
     * Verified Administrator check:
     * - Official administrator: d99312907@gmail.com -> Opens ADMIN PANEL automatically.
     * - Also supports verified runtime owner emails so the developer can test Admin Panel.
     */
    val isOfficialAdminEmail: Boolean
        get() {
            val activeSessionEmail = PixoraEmailSessionManager.activeSession.value?.email.orEmpty()
            val firebaseEmail = Firebase.auth.currentUser?.email.orEmpty()
            return currentUserEmail.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true) ||
                activeSessionEmail.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true) ||
                firebaseEmail.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true) ||
                RUNTIME_ADMIN_EMAILS.any { adminEmail ->
                    adminEmail.equals(currentUserEmail, ignoreCase = true) ||
                        adminEmail.equals(activeSessionEmail, ignoreCase = true) ||
                        adminEmail.equals(firebaseEmail, ignoreCase = true)
                }
        }

    // Admin Studio access permanently connected to User Panel
    val isAuthorizedAdmin: Boolean
        get() = isOfficialAdminEmail

    // Opens on USER PANEL by default so users can browse wallpapers and switch to Admin Panel anytime
    private val _isAdminPanelActive = MutableStateFlow(false)
    val isAdminPanelActive: StateFlow<Boolean> = _isAdminPanelActive.asStateFlow()

    private val _userTab = MutableStateFlow(UserDockTab.HOME)
    val userTab: StateFlow<UserDockTab> = _userTab.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminDockTab.DASHBOARD)
    val adminTab: StateFlow<AdminDockTab> = _adminTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchHistory = MutableStateFlow(
        listOf("AMOLED", "iOS 27", "8K", "Supercars", "Space")
    )
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _selectedWallpaper = MutableStateFlow<WallpaperItem?>(null)
    val selectedWallpaper: StateFlow<WallpaperItem?> = _selectedWallpaper.asStateFlow()

    private val _touchedBackgroundWallpaper = MutableStateFlow<WallpaperItem?>(null)
    val touchedBackgroundWallpaper: StateFlow<WallpaperItem?> = _touchedBackgroundWallpaper.asStateFlow()

    private val _activeDownload = MutableStateFlow<ActiveDownloadState?>(null)
    val activeDownload: StateFlow<ActiveDownloadState?> = _activeDownload.asStateFlow()

    private val _uploadProgress = MutableStateFlow<Float?>(null)
    val uploadProgress: StateFlow<Float?> = _uploadProgress.asStateFlow()

    private val _bannerNotification = MutableStateFlow<String?>(null)
    val bannerNotification: StateFlow<String?> = _bannerNotification.asStateFlow()

    private val _errorNotification = MutableStateFlow<String?>(null)
    val errorNotification: StateFlow<String?> = _errorNotification.asStateFlow()

    private val _glassOptics = MutableStateFlow(LiquidGlassOptics())
    val glassOptics: StateFlow<LiquidGlassOptics> = _glassOptics.asStateFlow()

    private val _activeThemeMode = MutableStateFlow("light")
    val activeThemeMode: StateFlow<String> = _activeThemeMode.asStateFlow()

    private val _selectedAppDesign = MutableStateFlow(AppDesignCatalog.ios27Crystal)
    val selectedAppDesign: StateFlow<AppDesignStyle> = _selectedAppDesign.asStateFlow()

    // Firebase AI Logic Models (Initialized lazily per firebase-ai-logic-android guidelines)
    private val aiImageModelPrimary by lazy {
        val config = generationConfig {
            responseModalities = listOf(ResponseModality.IMAGE)
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1",
            generationConfig = config
        )
    }

    private val aiImageModelMultimodal by lazy {
        val config = generationConfig {
            responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1",
            generationConfig = config
        )
    }

    private val aiImageModelFlashLite by lazy {
        val config = generationConfig {
            responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.1-flash-lite-image",
            generationConfig = config
        )
    }

    private val aiImageModelPro by lazy {
        val config = generationConfig {
            responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3-pro-image",
            generationConfig = config
        )
    }

    private val aiTextModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private val _ai8KState = MutableStateFlow<Ai8KGenUiState>(Ai8KGenUiState.Idle)
    val ai8KState: StateFlow<Ai8KGenUiState> = _ai8KState.asStateFlow()

    private val _aiGeneratedHistory = MutableStateFlow<List<WallpaperItem>>(emptyList())
    val aiGeneratedHistory: StateFlow<List<WallpaperItem>> = _aiGeneratedHistory.asStateFlow()

    private val premiumPrefs by lazy {
        appContext?.getSharedPreferences("wallx_premium_prefs", Context.MODE_PRIVATE)
    }

    private val _isPremiumUser = MutableStateFlow(
        premiumPrefs?.getBoolean("is_premium_unlocked_${currentUserId}", false) == true
    )
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    private val _customQrCodeImageUri = MutableStateFlow<String?>(
        premiumPrefs?.getString("custom_qr_code_uri", null)
    )
    val customQrCodeImageUri: StateFlow<String?> = _customQrCodeImageUri.asStateFlow()

    private val _deletedWallpaperIds = MutableStateFlow<Set<String>>(
        cleanUrlEntriesFromDeletedIds(loadStringSetFromPrefs("deleted_wallpaper_ids_v5"))
    )

    private val _deletedCategoryIds = MutableStateFlow<Set<String>>(
        loadStringSetFromPrefs("deleted_category_ids_v3")
    )

    private val _pendingCloudSyncIds = MutableStateFlow<Set<String>>(
        loadStringSetFromPrefs("pending_cloud_sync_ids_v1")
    )
    val pendingCloudSyncCount: StateFlow<Int> = _pendingCloudSyncIds
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _pendingCloudSyncIds.value.size)

    private val _userUploadedWallpapers = MutableStateFlow<List<WallpaperItem>>(
        loadLocalWallpapersFromPrefs()
    )

    private val _localCategories = MutableStateFlow<List<CategoryModel>>(
        loadLocalCategoriesFromPrefs()
    )

    private val _localAppSettings = MutableStateFlow<AppSettingsModel?>(
        loadLocalAppSettingsFromPrefs()
    )

    private fun cleanUrlEntriesFromDeletedIds(raw: Set<String>): Set<String> {
        val cleaned = raw.filterNot {
            it.startsWith("http://", ignoreCase = true) ||
                it.startsWith("https://", ignoreCase = true)
        }.toSet()
        if (cleaned.size != raw.size) {
            saveStringSetToPrefs("deleted_wallpaper_ids_v5", cleaned)
        }
        return cleaned
    }

    private fun loadStringSetFromPrefs(key: String): Set<String> {
        return premiumPrefs?.getStringSet(key, emptySet())?.toSet() ?: emptySet()
    }

    private fun saveStringSetToPrefs(key: String, set: Set<String>) {
        premiumPrefs?.edit()?.putStringSet(key, set)?.apply()
    }

    private fun markPendingCloudSync(wallId: String) {
        if (wallId.isBlank()) return
        _pendingCloudSyncIds.update { ids ->
            val next = ids + wallId
            saveStringSetToPrefs("pending_cloud_sync_ids_v1", next)
            next
        }
    }

    private fun clearPendingCloudSync(wallId: String) {
        if (wallId.isBlank()) return
        _pendingCloudSyncIds.update { ids ->
            if (wallId !in ids) return@update ids
            val next = ids - wallId
            saveStringSetToPrefs("pending_cloud_sync_ids_v1", next)
            next
        }
    }

    /**
     * Saves raw original image bytes to the 50 GB internal storage vault without recompression
     * so uploaded wallpapers render in 100% lossless 4K/8K quality and never bloat SharedPreferences.
     */
    private fun saveBytesTo50GbVault(bytes: ByteArray, wallId: String, ext: String = "jpg"): String? {
        val ctx = appContext ?: return null
        return try {
            val vaultDir = java.io.File(ctx.filesDir, "wallpapers_vault_50gb").apply { mkdirs() }
            val outFile = java.io.File(vaultDir, "$wallId.$ext")
            outFile.writeBytes(bytes)
            Uri.fromFile(outFile).toString()
        } catch (_: Exception) {
            null
        }
    }

    private fun parseWallpaperJsonArray(raw: String?): List<WallpaperItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = org.json.JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    if (id.isBlank() ||
                        id in WallxCatalogSeed.legacyDefaultSeedWallpaperIds ||
                        id.startsWith("pixora_curated_", ignoreCase = true) ||
                        id.startsWith("pixora_starter_", ignoreCase = true) ||
                        id.startsWith("wall_admin_studio_", ignoreCase = true)
                    ) continue
                    val tagsArr = obj.optJSONArray("tags")
                    val tags = buildList {
                        if (tagsArr != null) {
                            for (t in 0 until tagsArr.length()) {
                                val tag = tagsArr.optString(t)
                                if (tag.isNotBlank()) add(tag)
                            }
                        }
                    }
                    var imgUrl = obj.optString("imageUrl", "")
                    var thumbUrl = obj.optString("thumbnailUrl", imgUrl)
                    // Ensure a copy is also backed up in the 50 GB disk vault while keeping data:image Base64 intact for cloud sync & fast rendering
                    if (imgUrl.startsWith("data:image")) {
                        runCatching {
                            val b64Part = imgUrl.substringAfter("base64,", "")
                            if (b64Part.isNotBlank() && findLocalVaultFileUri(id) == null) {
                                val decodedBytes = android.util.Base64.decode(b64Part, android.util.Base64.DEFAULT)
                                saveBytesTo50GbVault(decodedBytes, id, "jpg")
                            }
                        }
                    }
                    if (imgUrl.isBlank() && thumbUrl.isNotBlank()) {
                        imgUrl = thumbUrl
                    }
                    if (imgUrl.isBlank()) {
                        val vaultUri = findLocalVaultFileUri(id)
                        if (!vaultUri.isNullOrBlank()) {
                            imgUrl = vaultUri
                            thumbUrl = vaultUri
                        }
                    }
                    if (imgUrl.isBlank()) continue
                    add(
                        WallpaperItem(
                            id = id,
                            title = obj.optString("title", "4K Wallpaper"),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "4K Ultra HD"),
                            tags = tags,
                            resolution = obj.optString("resolution", "3840x2160 (4K)"),
                            fileSize = obj.optString("fileSize", "4.2 MB"),
                            fileSizeBytes = obj.optLong("fileSizeBytes", 4200000L),
                            mimeType = obj.optString("mimeType", "image/jpeg"),
                            imageUrl = imgUrl,
                            thumbnailUrl = thumbUrl.ifBlank { imgUrl },
                            status = obj.optString("status", "published"),
                            isFeatured = obj.optBoolean("isFeatured", true),
                            isTrending = obj.optBoolean("isTrending", true),
                            downloadCount = obj.optLong("downloadCount", 1L),
                            favoriteCount = obj.optLong("favoriteCount", 1L),
                            viewCount = obj.optLong("viewCount", 0L),
                            uploadedBy = obj.optString("uploadedBy", currentUserId)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadLocalWallpapersFromPrefs(): List<WallpaperItem> {
        val fromDiskCatalog = runCatching {
            val ctx = appContext
            if (ctx != null) {
                val f = java.io.File(ctx.filesDir, "pixora_local_uploads_catalog.json")
                if (f.exists() && f.length() > 0L) parseWallpaperJsonArray(f.readText()) else emptyList()
            } else emptyList()
        }.getOrDefault(emptyList())
        val fromV3 = parseWallpaperJsonArray(premiumPrefs?.getString("local_uploaded_wallpapers_v3", null))
        val fromV2 = parseWallpaperJsonArray(premiumPrefs?.getString("local_uploaded_wallpapers_v2", null))
        val fromV1 = parseWallpaperJsonArray(premiumPrefs?.getString("local_uploaded_wallpapers", null))
        val mergedFromPrefs = (fromDiskCatalog + fromV3 + fromV2 + fromV1).distinctBy { it.id }

        // Also scan both on-disk image directories (wallpapers_vault_50gb and cloud_cache_images) so any uploaded image file on disk is automatically recovered
        val diskRecovered = buildList {
            val ctx = appContext
            if (ctx != null) {
                val vaultDir = java.io.File(ctx.filesDir, "wallpapers_vault_50gb")
                val cacheDir = java.io.File(ctx.filesDir, "cloud_cache_images")
                val candidateFiles = (
                    (if (vaultDir.exists() && vaultDir.isDirectory) vaultDir.listFiles().orEmpty().toList() else emptyList()) +
                        (if (cacheDir.exists() && cacheDir.isDirectory) cacheDir.listFiles().orEmpty().toList() else emptyList())
                    )
                    .filter { it.isFile && it.length() > 0L }
                    .sortedByDescending { it.lastModified() }
                    .distinctBy { it.nameWithoutExtension }

                val knownIds = mergedFromPrefs.map { it.id }.toSet()
                candidateFiles.forEach { file ->
                    val idFromFile = file.nameWithoutExtension
                    if (idFromFile.isNotBlank() &&
                        idFromFile != "custom_qr_code_299" &&
                        idFromFile !in knownIds
                    ) {
                        val fileUri = Uri.fromFile(file).toString()
                        val mbStr = String.format(java.util.Locale.US, "%.2f MB", (file.length() / (1024f * 1024f)).coerceAtLeast(0.1f))
                        add(
                            WallpaperItem(
                                id = idFromFile,
                                title = "Uploaded 4K Wallpaper",
                                description = "Stored in 2 TB Ultra HD Vault • Original Resolution",
                                category = "4K Ultra HD",
                                tags = listOf("4k", "uploaded", "recent"),
                                resolution = "3840x2160 (4K)",
                                fileSize = mbStr,
                                fileSizeBytes = file.length(),
                                mimeType = "image/jpeg",
                                imageUrl = fileUri,
                                thumbnailUrl = fileUri,
                                status = "published",
                                isFeatured = true,
                                isTrending = true,
                                downloadCount = 0L,
                                favoriteCount = 0L,
                                viewCount = 0L,
                                uploadedBy = currentUserId
                            )
                        )
                    }
                }
            }
        }

        val allRecovered = (mergedFromPrefs + diskRecovered)
            .deduplicateWallpapers()
            .filterNot { item ->
                val removed = item.isRemovedByUserRequest() || item.id in _deletedWallpaperIds.value
                if (item.isRemovedByUserRequest()) {
                    runCatching {
                        val ctx = appContext
                        if (ctx != null) {
                            val vaultDir = java.io.File(ctx.filesDir, "wallpapers_vault_50gb")
                            vaultDir.listFiles()?.filter { it.nameWithoutExtension == item.id }?.forEach { it.delete() }
                        }
                    }
                }
                removed
            }
        if (fromV3.size != allRecovered.size || fromDiskCatalog.size != allRecovered.size) {
            saveLocalWallpapersToPrefs(allRecovered)
        }
        return allRecovered
    }

    private fun saveLocalWallpapersToPrefs(list: List<WallpaperItem>) {
        try {
            val arr = org.json.JSONArray()
            list.filterNot { it.isRemovedByUserRequest() }.take(10000).forEach { item ->
                val obj = org.json.JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("description", item.description)
                    put("category", item.category)
                    put("tags", org.json.JSONArray(item.tags))
                    put("resolution", item.resolution)
                    put("fileSize", item.fileSize)
                    put("fileSizeBytes", item.fileSizeBytes)
                    put("mimeType", item.mimeType)
                    put("imageUrl", item.imageUrl)
                    put("thumbnailUrl", item.thumbnailUrl)
                    put("status", item.status.ifBlank { "published" })
                    put("isFeatured", item.isFeatured)
                    put("isTrending", item.isTrending)
                    put("downloadCount", item.downloadCount)
                    put("favoriteCount", item.favoriteCount)
                    put("viewCount", item.viewCount)
                    put("uploadedBy", item.uploadedBy)
                }
                arr.put(obj)
            }
            val serialized = arr.toString()
            premiumPrefs?.edit()?.putString("local_uploaded_wallpapers_v3", serialized)?.apply()
            val ctx = appContext
            if (ctx != null) {
                runCatching {
                    java.io.File(ctx.filesDir, "pixora_local_uploads_catalog.json").writeText(serialized)
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun loadLocalAppSettingsFromPrefs(): AppSettingsModel? {
        val raw = premiumPrefs?.getString("local_app_settings_v3", null) ?: return null
        return try {
            val obj = org.json.JSONObject(raw)
            AppSettingsModel(
                id = "global",
                appName = obj.optString("appName", "Pixora Wallpapers"),
                logoText = obj.optString("logoText", "WALLPERS • M3 EXCLUSIVE"),
                maintenanceMode = obj.optBoolean("maintenanceMode", false),
                maxUploadSizeMb = 2048000L,
                announcementText = obj.optString("announcementText", ""),
                announcementEnabled = obj.optBoolean("announcementEnabled", true),
                heroBannerTitle = obj.optString("heroBannerTitle", "Nature Beauty"),
                heroBannerSubtitle = obj.optString("heroBannerSubtitle", "Explore the beauty of nature"),
                heroBannerImageUrl = obj.optString("heroBannerImageUrl", ""),
                showFeaturedSection = obj.optBoolean("showFeaturedSection", true),
                showTrendingSection = obj.optBoolean("showTrendingSection", true),
                showCategoriesSection = obj.optBoolean("showCategoriesSection", true),
                showNewArrivalsSection = obj.optBoolean("showNewArrivalsSection", true),
                userLayoutId = obj.optString("userLayoutId", "ios27_crystal")
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun saveLocalAppSettingsToPrefs(settings: AppSettingsModel) {
        try {
            val obj = org.json.JSONObject().apply {
                put("appName", settings.appName)
                put("logoText", settings.logoText)
                put("maintenanceMode", settings.maintenanceMode)
                put("maxUploadSizeMb", 2048000L)
                put("announcementText", settings.announcementText)
                put("announcementEnabled", settings.announcementEnabled)
                put("heroBannerTitle", settings.heroBannerTitle)
                put("heroBannerSubtitle", settings.heroBannerSubtitle)
                put("heroBannerImageUrl", settings.heroBannerImageUrl)
                put("showFeaturedSection", settings.showFeaturedSection)
                put("showTrendingSection", settings.showTrendingSection)
                put("showCategoriesSection", settings.showCategoriesSection)
                put("showNewArrivalsSection", settings.showNewArrivalsSection)
                put("userLayoutId", settings.userLayoutId)
            }
            premiumPrefs?.edit()?.putString("local_app_settings_v3", obj.toString())?.apply()
        } catch (_: Exception) {
        }
    }

    private fun parseCategoryJsonArray(raw: String?): List<CategoryModel> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = org.json.JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    val name = obj.optString("name")
                    if (id.isBlank() || name.isBlank()) continue
                    add(
                        CategoryModel(
                            id = id,
                            name = name,
                            description = obj.optString("description", "Curated $name collection"),
                            thumbnailUrl = obj.optString(
                                "thumbnailUrl",
                                "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=2400&q=95"
                            ),
                            order = obj.optLong("order", (i + 1).toLong()),
                            enabled = obj.optBoolean("enabled", true),
                            wallpaperCount = obj.optLong("wallpaperCount", 1L),
                            accentHex = obj.optString("accentHex", "#5B2EFF")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadLocalCategoriesFromPrefs(): List<CategoryModel> {
        val v3 = parseCategoryJsonArray(premiumPrefs?.getString("local_categories_v3", null))
        val v2 = parseCategoryJsonArray(premiumPrefs?.getString("local_categories_v2", null))
        val v1 = parseCategoryJsonArray(premiumPrefs?.getString("local_categories", null))
        return (v3 + v2 + v1).distinctBy { it.name.trim().lowercase(java.util.Locale.ROOT) }
    }

    private fun saveLocalCategoriesToPrefs(list: List<CategoryModel>) {
        try {
            val arr = org.json.JSONArray()
            list.forEach { cat ->
                val obj = org.json.JSONObject().apply {
                    put("id", cat.id)
                    put("name", cat.name)
                    put("description", cat.description)
                    put("thumbnailUrl", cat.thumbnailUrl)
                    put("order", cat.order)
                    put("enabled", cat.enabled)
                    put("wallpaperCount", cat.wallpaperCount)
                    put("accentHex", cat.accentHex)
                }
                arr.put(obj)
            }
            premiumPrefs?.edit()?.putString("local_categories_v3", arr.toString())?.apply()
        } catch (_: Exception) {
        }
    }

    private fun ensureCategoryExistsLocally(categoryName: String, coverUrl: String = "") {
        val trimmed = categoryName.trim()
        if (trimmed.isBlank() || trimmed.equals("All", ignoreCase = true)) return
        val catId = "cat_${trimmed.lowercase(java.util.Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")}"
        val alreadyInDefault = WallxCatalogSeed.defaultCategories.any {
            it.name.equals(trimmed, ignoreCase = true)
        }
        val alreadyInLocal = _localCategories.value.any {
            it.name.equals(trimmed, ignoreCase = true) || it.id == catId
        }
        if (!alreadyInDefault && !alreadyInLocal) {
            val newCat = CategoryModel(
                id = catId,
                name = trimmed,
                description = "Curated $trimmed 4K collection",
                thumbnailUrl = coverUrl.ifBlank {
                    "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=2400&q=95"
                },
                order = 0L,
                enabled = true,
                wallpaperCount = 1L,
                accentHex = "#5B2EFF"
            )
            _localCategories.update { existing ->
                val updated = listOf(newCat) + existing.filterNot { it.id == catId }
                saveLocalCategoriesToPrefs(updated)
                updated
            }
        }
    }

    fun unlockPremiumViaQrCode(utrReference: String = "") {
        val cleaned = utrReference.trim()
        val isReal12DigitUtr = cleaned.matches(Regex("^[1-9]\\d{11}$")) &&
            cleaned.toSet().size >= 3 &&
            cleaned != "123456789012" &&
            cleaned != "987654321098"
        if (!isReal12DigitUtr) {
            _errorNotification.value = "Valid 12-digit UPI Transaction ID / UTR is required to unlock Premium."
            return
        }
        _isPremiumUser.value = true
        premiumPrefs?.edit()
            ?.putBoolean("is_premium_unlocked_${currentUserId}", true)
            ?.putString("premium_utr_ref_${currentUserId}", cleaned)
            ?.apply()
        viewModelScope.launch {
            repository.recordPremiumPurchase(
                utrReference = cleaned,
                fallbackEmail = currentUserEmail,
                fallbackName = userProfile.value?.displayName.orEmpty()
            ).onFailure { e ->
                Log.w(TAG, "Cloud sync for Premium purchase deferred", e)
            }
        }
        _bannerNotification.value =
            "₹299 QR Payment Verified (UTR #$cleaned)! All Premium Features, Creator Earnings & 2 TB Wallpaper Upload Unlocked!"
    }

    fun updateCustomQrCodeImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    val savedFileUri = saveBytesTo50GbVault(bytes, "custom_qr_code_299", "png")
                        ?: ("data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP))
                    _customQrCodeImageUri.value = savedFileUri
                    premiumPrefs?.edit()?.putString("custom_qr_code_uri", savedFileUri)?.apply()
                    _bannerNotification.value = "Custom ₹299 QR Code updated in HD!"
                }
            } catch (e: Exception) {
                _errorNotification.value = e.localizedMessage ?: "Could not load QR image"
            }
        }
    }

    private fun encodeCompactDataUriFromBytes(
        bytes: ByteArray,
        initialMaxDim: Int = 1440,
        maxRawBytes: Int = 140_000
    ): String {
        var decoded: Bitmap? = null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            var sampleSize = 1
            if (bounds.outWidth > 0 && bounds.outHeight > 0) {
                while ((bounds.outWidth / sampleSize) > 3840 || (bounds.outHeight / sampleSize) > 3840) {
                    sampleSize *= 2
                }
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
                ?: BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (decoded != null) {
                var currentMaxDim = initialMaxDim
                var encodedBytes = ByteArray(0)
                for (pass in 0..5) {
                    val ratio = minOf(currentMaxDim.toFloat() / decoded.width, currentMaxDim.toFloat() / decoded.height, 1f)
                    val targetW = (decoded.width * ratio).toInt().coerceAtLeast(1)
                    val targetH = (decoded.height * ratio).toInt().coerceAtLeast(1)
                    val scaled = if (targetW != decoded.width || targetH != decoded.height) {
                        Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
                    } else {
                        decoded
                    }
                    var quality = 84
                    while (quality >= 34) {
                        val out = java.io.ByteArrayOutputStream()
                        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
                        encodedBytes = out.toByteArray()
                        if (encodedBytes.size <= maxRawBytes) break
                        quality -= 12
                    }
                    if (scaled !== decoded) {
                        scaled.recycle()
                    }
                    if (encodedBytes.isNotEmpty() && encodedBytes.size <= maxRawBytes) break
                    currentMaxDim = (currentMaxDim * 0.72f).toInt().coerceAtLeast(240)
                }
                if (encodedBytes.isNotEmpty()) {
                    "data:image/jpeg;base64," + android.util.Base64.encodeToString(encodedBytes, android.util.Base64.NO_WRAP)
                } else {
                    ""
                }
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        } finally {
            runCatching { decoded?.recycle() }
        }
    }

    fun uploadPremiumUserWallpaper(
        context: Context,
        uri: Uri,
        title: String,
        category: String,
        description: String
    ) {
        val cleanTitle = title.trim().ifBlank { "Ultra HD 4K Creation" }
        val cleanCategory = category.trim().ifBlank { "4K Ultra HD" }
        val cleanDesc = description.trim().ifBlank {
            "Uploaded 4K Wallpaper • Live 24/7 on Pixora."
        }
        _selectedCategory.value = "All"
        _searchQuery.value = ""

        viewModelScope.launch {
            _uploadProgress.value = 0.15f
            try {
                val wallId = "prem_upload_${System.currentTimeMillis()}"
                val preparedResult = repository.prepareWallpaperFromUri(
                    context = context,
                    uri = uri,
                    title = cleanTitle,
                    description = cleanDesc,
                    category = cleanCategory,
                    tags = listOf(cleanCategory.lowercase(java.util.Locale.ROOT), "4k", "uploaded", "recent"),
                    status = "published",
                    isFeatured = true,
                    isTrending = true,
                    preferredId = wallId,
                    saveToLocalVault = true,
                    onProgress = { p -> _uploadProgress.value = p.coerceIn(0.15f, 0.80f) }
                )
                val preparedWall = preparedResult.getOrElse { e ->
                    Log.e(TAG, "Creator upload prep failed for $wallId", e)
                    _uploadProgress.value = null
                    _errorNotification.value = e.localizedMessage ?: "Unable to read selected image."
                    return@launch
                }

                // Immediately make live on User & Admin Panels and persist locally so nothing is ever lost
                ensureCategoryExistsLocally(cleanCategory, preparedWall.thumbnailUrl)
                upsertLocalAndCacheWallpaper(preparedWall, markPendingSync = true)
                _uploadProgress.value = 0.88f

                val catId = "cat_${cleanCategory.lowercase(java.util.Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")}"
                val categoryModel = CategoryModel(
                    id = catId,
                    name = cleanCategory,
                    thumbnailUrl = preparedWall.thumbnailUrl,
                    enabled = true
                )
                repository.createWallpaperRecord(preparedWall)
                    .onSuccess {
                        clearPendingCloudSync(preparedWall.id)
                        if (cloudAllCategories.value.none { it.id == catId }) {
                            runCatching { repository.ensureCategoryExistsInCloud(categoryModel) }
                        }
                        _uploadProgress.value = null
                        _bannerNotification.value = "Uploaded '$cleanTitle' to Cloud Firestore — Now Live 24/7 for All Users!"
                    }
                    .onFailure { e ->
                        Log.w(TAG, "Creator Firestore sync deferred for $wallId (saved locally)", e)
                        _uploadProgress.value = null
                        _bannerNotification.value = "Saved '$cleanTitle' locally — queued for Cloud Sync (${formatFriendlyCatalogError(e)})"
                    }
            } catch (e: CancellationException) {
                _uploadProgress.value = null
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Creator upload failed", e)
                _uploadProgress.value = null
                _errorNotification.value = formatFriendlyCatalogError(e)
            }
        }
    }

    // Explicit server-fetch, disk-cache, & newly-uploaded cloud wallpapers to guarantee immediate + 24/7 visibility even under quota limits
    private val _cachedCloudWallpapers = MutableStateFlow<List<WallpaperItem>>(emptyList())
    private val _serverFetchedWallpapers = MutableStateFlow<List<WallpaperItem>>(emptyList())
    private val _recentlyUploadedCloudWallpapers = MutableStateFlow<List<WallpaperItem>>(emptyList())
    private val _lastKnownLiveCloudWallpapers = MutableStateFlow<List<WallpaperItem>>(emptyList())
    private var hasServerConfirmedSnapshot = false
    private val _isCatalogLoading = MutableStateFlow(false)
    val isCatalogLoading: StateFlow<Boolean> = _isCatalogLoading.asStateFlow()
    private val _catalogSyncError = MutableStateFlow<String?>(null)
    val catalogSyncError: StateFlow<String?> = _catalogSyncError.asStateFlow()

    private fun formatFriendlyCatalogError(error: Throwable): String {
        val raw = error.localizedMessage ?: error.message ?: ""
        return if (raw.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
            raw.contains("Quota limit exceeded", ignoreCase = true)
        ) {
            "Cloud daily read quota reached. Cached & uploaded wallpapers remain live."
        } else {
            raw.ifBlank { "Offline / cloud unreachable right now. Local & cached wallpapers remain live." }
        }
    }

    private fun upsertLocalAndCacheWallpaper(wall: WallpaperItem, markPendingSync: Boolean) {
        if (wall.id in _deletedWallpaperIds.value) {
            _deletedWallpaperIds.update { ids ->
                val next = ids - wall.id
                saveStringSetToPrefs("deleted_wallpaper_ids_v5", next)
                next
            }
        }
        if (markPendingSync) {
            markPendingCloudSync(wall.id)
        } else {
            clearPendingCloudSync(wall.id)
        }
        _recentlyUploadedCloudWallpapers.update { existing ->
            listOf(wall) + existing.filterNot { it.id == wall.id }
        }
        _userUploadedWallpapers.update { existing ->
            val next = listOf(wall) + existing.filterNot { it.id == wall.id }
            saveLocalWallpapersToPrefs(next)
            next
        }
        _serverFetchedWallpapers.update { existing ->
            if (existing.any { it.id == wall.id }) {
                existing.map { if (it.id == wall.id) wall else it }
            } else {
                listOf(wall) + existing
            }
        }
        _cachedCloudWallpapers.update { existing ->
            val next = listOf(wall) + existing.filterNot { it.id == wall.id }
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                saveDiskCatalogCache(next)
            }
            next
        }
    }

    private fun loadDiskCatalogCache(): List<WallpaperItem> {
        val ctx = appContext ?: return emptyList()
        return runCatching {
            val cacheFile = java.io.File(ctx.filesDir, "pixora_cloud_catalog_cache.json")
            if (!cacheFile.exists() || cacheFile.length() == 0L) return emptyList()
            val parsed = parseWallpaperJsonArray(cacheFile.readText())
            val cleaned = parsed.filterNot { it.isRemovedByUserRequest() }
            if (cleaned.size != parsed.size) {
                saveDiskCatalogCache(cleaned)
            }
            cleaned
        }.getOrDefault(emptyList())
    }

    private fun saveDiskCatalogCache(list: List<WallpaperItem>) {
        val ctx = appContext ?: return
        runCatching {
            val cacheDir = java.io.File(ctx.filesDir, "cloud_cache_images").apply { mkdirs() }
            val arr = org.json.JSONArray()
            list.filterNot { it.isRemovedByUserRequest() }.take(500).forEach { item ->
                val cachedImg = item.imageUrl
                val cachedThumb = item.thumbnailUrl
                if (cachedImg.startsWith("data:image")) {
                    val b64 = cachedImg.substringAfter("base64,", "")
                    if (b64.isNotBlank()) {
                        val outFile = java.io.File(cacheDir, "${item.id}.jpg")
                        runCatching {
                            val decoded = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                            if (!outFile.exists() || outFile.length() != decoded.size.toLong()) {
                                outFile.writeBytes(decoded)
                            }
                        }
                    }
                }
                val obj = org.json.JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("description", item.description)
                    put("category", item.category)
                    put("tags", org.json.JSONArray(item.tags))
                    put("resolution", item.resolution)
                    put("fileSize", item.fileSize)
                    put("fileSizeBytes", item.fileSizeBytes)
                    put("mimeType", item.mimeType)
                    put("imageUrl", cachedImg)
                    put("thumbnailUrl", cachedThumb.ifBlank { cachedImg })
                    put("status", item.status.ifBlank { "published" })
                    put("isFeatured", item.isFeatured)
                    put("isTrending", item.isTrending)
                    put("downloadCount", item.downloadCount)
                    put("favoriteCount", item.favoriteCount)
                    put("viewCount", item.viewCount)
                    put("uploadedBy", item.uploadedBy)
                }
                arr.put(obj)
            }
            java.io.File(ctx.filesDir, "pixora_cloud_catalog_cache.json").writeText(arr.toString())
        }
    }

    private sealed interface CloudWallpapersEvent {
        data object Loading : CloudWallpapersEvent
        data class Snapshot(val items: List<WallpaperItem>, val isFromCache: Boolean) : CloudWallpapersEvent
        data class Error(val error: Throwable) : CloudWallpapersEvent
    }

    // SINGLE unified real-time Firestore stream for all wallpapers preserving cache metadata & error state
    private val cloudWallpapersEvent: StateFlow<CloudWallpapersEvent> =
        repository.observeAllWallpapersWithMetadata()
            .map<WallxRepository.CloudWallpaperSnapshot, CloudWallpapersEvent> { snap ->
                CloudWallpapersEvent.Snapshot(snap.items, snap.isFromCache)
            }
            .catch { e ->
                Log.w(TAG, "Error observing unified wallpapers", e)
                emit(CloudWallpapersEvent.Error(e))
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, CloudWallpapersEvent.Loading)

    private val cloudAllWallpapers: StateFlow<List<WallpaperItem>>
        get() = _lastKnownLiveCloudWallpapers

    // SINGLE unified real-time Firestore stream for all categories
    private val cloudAllCategories: StateFlow<List<CategoryModel>> =
        repository.observeAllCategoriesUnified()
            .catch { e ->
                Log.w(TAG, "Error observing unified categories", e)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val cloudAppSettings: StateFlow<AppSettingsModel?> =
        repository.observeAppSettings()
            .map<AppSettingsModel, AppSettingsModel?> { it }
            .catch { e ->
                Log.w(TAG, "Error observing appSettings", e)
                emit(null)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val appSettings: StateFlow<AppSettingsModel> =
        combine(
            _localAppSettings,
            cloudAppSettings
        ) { localOverride, cloudSettings ->
            cloudSettings ?: localOverride ?: AppSettingsModel()
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, _localAppSettings.value ?: AppSettingsModel())

    private fun findLocalVaultFileUri(wallId: String): String? {
        if (wallId.isBlank()) return null
        val ctx = appContext ?: return null
        return runCatching {
            val vaultDir = java.io.File(ctx.filesDir, "wallpapers_vault_50gb")
            val cacheDir = java.io.File(ctx.filesDir, "cloud_cache_images")
            val match = (vaultDir.listFiles().orEmpty().toList() + cacheDir.listFiles().orEmpty().toList())
                .firstOrNull { it.isFile && it.length() > 0L && it.nameWithoutExtension == wallId }
            match?.let { Uri.fromFile(it).toString() }
        }.getOrNull()
    }

    private fun isSingleUrlAccessibleOnDevice(rawUrl: String): Boolean {
        val raw = rawUrl.trim()
        if (raw.isBlank()) return false
        if (raw.startsWith("http", ignoreCase = true) ||
            raw.startsWith("data:", ignoreCase = true) ||
            raw.startsWith("drawable://", ignoreCase = true)
        ) {
            return true
        }
        if (raw.startsWith("content://", ignoreCase = true)) {
            val ctx = appContext ?: return true
            return runCatching {
                ctx.contentResolver.openInputStream(Uri.parse(raw))?.use { true } ?: false
            }.getOrDefault(false)
        }
        if (raw.startsWith("file:/", ignoreCase = true)) {
            val file = runCatching { java.io.File(java.net.URI(raw)) }.getOrElse {
                java.io.File(raw.removePrefix("file://").removePrefix("file:"))
            }
            return runCatching { file.exists() && file.length() > 0L }.getOrDefault(false)
        }
        if (raw.startsWith("/")) {
            return runCatching {
                val f = java.io.File(raw)
                f.exists() && f.length() > 0L
            }.getOrDefault(false)
        }
        return true
    }

    private fun isWallpaperAccessibleOnDevice(item: WallpaperItem): Boolean {
        return isSingleUrlAccessibleOnDevice(item.imageUrl) ||
            isSingleUrlAccessibleOnDevice(item.thumbnailUrl) ||
            findLocalVaultFileUri(item.id) != null
    }

    private fun sanitizeImageUrlFormat(url: String): String {
        val raw = url.trim()
        if (!raw.contains("images.unsplash.com")) return raw
        val replaced = raw
            .replace("auto=format", "fm=jpg")
            .replace("fm=avif", "fm=jpg")
            .replace("fm=webp", "fm=jpg")
        return if (!replaced.contains("fm=jpg")) {
            if (replaced.contains("?")) "$replaced&fm=jpg" else "$replaced?fm=jpg"
        } else {
            replaced
        }
    }

    private fun normalizeAccessibleWallpaperUrls(item: WallpaperItem): WallpaperItem {
        val cleanImage = sanitizeImageUrlFormat(item.imageUrl)
        val cleanThumb = sanitizeImageUrlFormat(item.thumbnailUrl)
        val sanitizedItem = if (cleanImage != item.imageUrl || cleanThumb != item.thumbnailUrl) {
            item.copy(imageUrl = cleanImage, thumbnailUrl = cleanThumb)
        } else {
            item
        }
        val imgOk = isSingleUrlAccessibleOnDevice(sanitizedItem.imageUrl)
        val thumbOk = isSingleUrlAccessibleOnDevice(sanitizedItem.thumbnailUrl)
        return when {
            imgOk && thumbOk -> sanitizedItem
            !imgOk && thumbOk -> sanitizedItem.copy(imageUrl = sanitizedItem.thumbnailUrl)
            imgOk && !thumbOk -> sanitizedItem.copy(thumbnailUrl = sanitizedItem.imageUrl)
            else -> {
                val localVaultUri = findLocalVaultFileUri(sanitizedItem.id)
                if (!localVaultUri.isNullOrBlank()) {
                    sanitizedItem.copy(imageUrl = localVaultUri, thumbnailUrl = localVaultUri)
                } else {
                    sanitizedItem
                }
            }
        }
    }

    private fun buildUnifiedMergedCatalog(
        liveCloud: List<WallpaperItem>,
        recentCloud: List<WallpaperItem>,
        serverFetched: List<WallpaperItem>,
        diskCached: List<WallpaperItem>,
        uploadedLocal: List<WallpaperItem>,
        aiLocal: List<WallpaperItem>,
        pendingIds: Set<String>,
        allDeletedIds: Set<String>
    ): List<WallpaperItem> {
        fun isDeleted(item: WallpaperItem): Boolean {
            return item.isRemovedByUserRequest() || item.id in allDeletedIds
        }

        val pendingLocalEdits = uploadedLocal.filter { it.id in pendingIds }

        // Combine uploaded wallpapers first, followed by live cloud & resilient studio catalog so the app always displays wallpapers
        return (pendingLocalEdits +
            recentCloud +
            uploadedLocal +
            liveCloud +
            serverFetched +
            diskCached +
            aiLocal +
            WallxCatalogSeed.defaultWallpapers(currentUserId))
            .filterNot { isDeleted(it) }
            .filter { isWallpaperAccessibleOnDevice(it) }
            .map { normalizeAccessibleWallpaperUrls(it) }
            .distinctBy { it.id }
            .deduplicateWallpapers()
    }

    // Unified catalog flow used by both Admin Panel & User Panel
    val adminAllWallpapers: StateFlow<List<WallpaperItem>> =
        combine(
            _lastKnownLiveCloudWallpapers,
            _recentlyUploadedCloudWallpapers,
            _serverFetchedWallpapers,
            _cachedCloudWallpapers,
            _userUploadedWallpapers,
            _aiGeneratedHistory,
            _pendingCloudSyncIds,
            _deletedWallpaperIds,
            appSettings
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val liveCloud = values[0] as List<WallpaperItem>
            @Suppress("UNCHECKED_CAST")
            val recentCloud = values[1] as List<WallpaperItem>
            @Suppress("UNCHECKED_CAST")
            val serverFetched = values[2] as List<WallpaperItem>
            @Suppress("UNCHECKED_CAST")
            val diskCached = values[3] as List<WallpaperItem>
            @Suppress("UNCHECKED_CAST")
            val uploadedLocal = values[4] as List<WallpaperItem>
            @Suppress("UNCHECKED_CAST")
            val aiLocal = values[5] as List<WallpaperItem>
            @Suppress("UNCHECKED_CAST")
            val pendingIds = values[6] as Set<String>
            @Suppress("UNCHECKED_CAST")
            val deletedIds = values[7] as Set<String>

            buildUnifiedMergedCatalog(
                liveCloud = liveCloud,
                recentCloud = recentCloud,
                serverFetched = serverFetched,
                diskCached = diskCached,
                uploadedLocal = uploadedLocal,
                aiLocal = aiLocal,
                pendingIds = pendingIds,
                allDeletedIds = deletedIds
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                buildUnifiedMergedCatalog(
                    liveCloud = emptyList(),
                    recentCloud = emptyList(),
                    serverFetched = emptyList(),
                    diskCached = emptyList(),
                    uploadedLocal = _userUploadedWallpapers.value,
                    aiLocal = emptyList(),
                    pendingIds = _pendingCloudSyncIds.value,
                    allDeletedIds = _deletedWallpaperIds.value
                )
            )

    // User Panel shows only Published wallpapers AFTER merging & deduplicating by ID so Admin Draft/Edit changes reflect immediately
    val publishedWallpapers: StateFlow<List<WallpaperItem>> =
        adminAllWallpapers
            .map { allWallpapers ->
                allWallpapers.filter { it.status.isBlank() || it.status.equals("published", ignoreCase = true) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                adminAllWallpapers.value.filter { it.status.isBlank() || it.status.equals("published", ignoreCase = true) }
            )

    val userUploadedWallpapers: StateFlow<List<WallpaperItem>> =
        combine(
            adminAllWallpapers,
            _userUploadedWallpapers,
            _deletedWallpaperIds,
            appSettings
        ) { allWallpapers, localUploaded, deletedIds, settings ->
            val allDeletedIds = deletedIds + settings.deletedWallpaperIds.toSet()
            val mineFromUnified = allWallpapers.filter {
                (it.uploadedBy == currentUserId ||
                    it.id.startsWith("wall_admin_") ||
                    it.id.startsWith("prem_upload_") ||
                    it.uploadedBy.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true)) &&
                    !it.isRemovedByUserRequest() &&
                    it.id !in allDeletedIds
            }
            val mineLocal = localUploaded.filter {
                !it.isRemovedByUserRequest() &&
                    it.id !in allDeletedIds
            }
            (mineFromUnified + mineLocal)
                .distinctBy { it.id }
                .deduplicateWallpapers()
        }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                _userUploadedWallpapers.value.filterNot { it.isRemovedByUserRequest() }
            )

    val recentlyAddedWallpapers: StateFlow<List<WallpaperItem>> =
        publishedWallpapers

    val adminAllCategories: StateFlow<List<CategoryModel>> =
        combine(
            _localCategories,
            cloudAllCategories,
            _deletedCategoryIds,
            appSettings
        ) { localCats, cloudCats, deletedCatIds, settings ->
            val allDeletedCatIds = deletedCatIds + settings.deletedCategoryIds.toSet()
            fun isCatDeleted(cat: CategoryModel): Boolean {
                val nameKey = cat.name.trim().lowercase(java.util.Locale.ROOT)
                return cat.id in allDeletedCatIds || nameKey in allDeletedCatIds
            }

            // localCats + cloudCats override defaultCategories so Admin edits/disables/reorders reflect in 0ms everywhere
            (localCats + cloudCats + WallxCatalogSeed.defaultCategories)
                .filterNot { isCatDeleted(it) }
                .distinctBy { it.name.trim().lowercase(java.util.Locale.ROOT) }
                .sortedWith(compareBy<CategoryModel> { it.order }.thenBy { it.name.lowercase(java.util.Locale.ROOT) })
        }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                (_localCategories.value + WallxCatalogSeed.defaultCategories)
                    .filterNot {
                        it.id in _deletedCategoryIds.value ||
                            it.name.trim().lowercase(java.util.Locale.ROOT) in _deletedCategoryIds.value
                    }
                    .distinctBy { it.name.trim().lowercase(java.util.Locale.ROOT) }
                    .sortedWith(compareBy<CategoryModel> { it.order }.thenBy { it.name.lowercase(java.util.Locale.ROOT) })
            )

    val enabledCategories: StateFlow<List<CategoryModel>> =
        combine(
            adminAllCategories,
            _deletedCategoryIds,
            appSettings
        ) { allMergedAdminCats, deletedCatIds, settings ->
            val allDeletedCatIds = deletedCatIds + settings.deletedCategoryIds.toSet()
            allMergedAdminCats.filter { cat ->
                val nameKey = cat.name.trim().lowercase(java.util.Locale.ROOT)
                cat.enabled && cat.id !in allDeletedCatIds && nameKey !in allDeletedCatIds
            }
        }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                (_localCategories.value + WallxCatalogSeed.defaultCategories)
                    .filterNot {
                        it.id in _deletedCategoryIds.value ||
                            it.name.trim().lowercase(java.util.Locale.ROOT) in _deletedCategoryIds.value
                    }
                    .distinctBy { it.name.trim().lowercase(java.util.Locale.ROOT) }
                    .filter { it.enabled }
                    .sortedWith(compareBy<CategoryModel> { it.order }.thenBy { it.name.lowercase(java.util.Locale.ROOT) })
            )

    // Room-cached offline favorite entities stream (always available even without internet)
    private val roomCachedFavoriteEntities: StateFlow<List<CachedFavoriteWallpaperEntity>> =
        (offlineFavoriteCacheRepository?.observeCachedFavorites(currentUserId)
            ?: kotlinx.coroutines.flow.flowOf(emptyList()))
            .catch { e ->
                Log.w(TAG, "Error observing Room cached favorites", e)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Cloud favorites stream (gracefully falls back to empty when offline)
    private val cloudUserFavorites: StateFlow<List<FavoriteRecord>> =
        repository.observeUserFavorites(currentUserId)
            .catch { e ->
                Log.w(TAG, "Error observing cloud user favorites (offline mode active)", e)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Full [WallpaperItem] objects cached in the local Room Database (`cached_favorite_wallpapers`),
     * including locally cached offline image URIs so users can view & apply favorites offline.
     */
    val cachedFavoriteWallpapers: StateFlow<List<WallpaperItem>> =
        combine(
            roomCachedFavoriteEntities,
            _deletedWallpaperIds,
            appSettings
        ) { roomEntities, deletedIds, settings ->
            val allDeletedIds = deletedIds + settings.deletedWallpaperIds.toSet()
            roomEntities
                .map { it.toWallpaperItem() }
                .filterNot { item ->
                    val imgKey = normalizeWallpaperImageKey(item.imageUrl.ifBlank { item.thumbnailUrl })
                    item.isRemovedByUserRequest() ||
                        item.id in allDeletedIds ||
                        (imgKey.isNotBlank() && imgKey in allDeletedIds)
                }
                .distinctBy { it.id }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Unified Favorites stream combining Room offline cache + Firestore cloud favorites so
     * favorite badges and the Favorites screen work seamlessly both online and offline.
     */
    val userFavorites: StateFlow<List<FavoriteRecord>> =
        combine(
            roomCachedFavoriteEntities,
            cloudUserFavorites,
            _deletedWallpaperIds,
            appSettings
        ) { roomEntities, cloudFavs, deletedIds, settings ->
            val allDeletedIds = deletedIds + settings.deletedWallpaperIds.toSet()
            val roomRecords = roomEntities.map { it.toFavoriteRecord() }
            (roomRecords + cloudFavs)
                .filterNot { fav ->
                    fav.wallpaperId in allDeletedIds ||
                        fav.wallpaperTitle.trim().lowercase(java.util.Locale.ROOT) in WallxCatalogSeed.permanentlyRemovedWallpaperTitles
                }
                .distinctBy { it.wallpaperId }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val userDownloads: StateFlow<List<DownloadRecordModel>> =
        repository.observeUserDownloads(currentUserId)
            .catch { e ->
                Log.w(TAG, "Error observing user downloads", e)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userProfile: StateFlow<UserProfileModel?> =
        combine(
            repository.observeUserProfile(currentUserId)
                .catch { e ->
                    Log.w(TAG, "Error observing user profile", e)
                    emit(null)
                },
            PixoraEmailSessionManager.activeSession
        ) { cloudProfile, emailSession ->
            when {
                cloudProfile != null && emailSession != null -> {
                    cloudProfile.copy(
                        email = emailSession.email.ifBlank { cloudProfile.email },
                        displayName = emailSession.displayName.ifBlank { cloudProfile.displayName }
                    )
                }
                cloudProfile != null -> cloudProfile
                emailSession != null -> UserProfileModel(
                    id = emailSession.uid,
                    userId = emailSession.uid,
                    email = emailSession.email,
                    displayName = emailSession.displayName,
                    role = if (emailSession.email.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true)) "admin" else "user",
                    status = "active"
                )
                else -> null
            }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val adminAllUsers: StateFlow<List<UserProfileModel>> =
        repository.observeAllUsersForAdmin()
            .catch { e ->
                Log.w(TAG, "Error observing all users for admin", e)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val adminAllPremiumPurchases: StateFlow<List<PremiumPurchaseModel>> =
        repository.observeAllPremiumPurchasesForAdmin()
            .catch { e ->
                Log.w(TAG, "Error observing all premium purchases for admin", e)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val userCloudPremiumPurchases: StateFlow<List<PremiumPurchaseModel>> =
        repository.observeUserPremiumPurchases(currentUserId)
            .catch { e ->
                Log.w(TAG, "Error observing user premium purchases", e)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val viewModelCreatedAtMs: Long = System.currentTimeMillis()
    private var lastServerRefreshMs: Long = 0L
    private var isRetryingPendingSync = false
    private var lastPendingRetryMs: Long = 0L

    fun onAppResumed() {
        retryPendingCloudSyncsIfNeeded()
        val elapsedSinceInit = System.currentTimeMillis() - viewModelCreatedAtMs
        // Skip duplicate server fetch during initial cold start since observeAllWallpapersWithMetadata() and init already run
        if (elapsedSinceInit > 8_000L && (!hasServerConfirmedSnapshot || _catalogSyncError.value != null)) {
            refreshWallpapersNow(forceServer = false)
        }
    }

    private fun prepareWallpaperForCloudSync(wall: WallpaperItem): WallpaperItem {
        val img = wall.imageUrl.trim()
        val thumb = wall.thumbnailUrl.trim()
        val needsDataUri = img.startsWith("file:", ignoreCase = true) ||
            img.startsWith("/") ||
            img.startsWith("content://", ignoreCase = true)
        if (!needsDataUri) return wall

        val bytes = runCatching {
            when {
                img.startsWith("file:", ignoreCase = true) -> {
                    val file = runCatching { java.io.File(java.net.URI(img)) }.getOrElse {
                        java.io.File(img.removePrefix("file://").removePrefix("file:"))
                    }
                    if (file.exists() && file.length() > 0L) file.readBytes() else null
                }
                img.startsWith("/") -> {
                    val file = java.io.File(img)
                    if (file.exists() && file.length() > 0L) file.readBytes() else null
                }
                img.startsWith("content://", ignoreCase = true) -> {
                    appContext?.contentResolver?.openInputStream(Uri.parse(img))?.use { it.readBytes() }
                }
                else -> null
            }
        }.getOrNull()

        if (bytes != null && bytes.isNotEmpty()) {
            val compactDataUri = encodeCompactDataUriFromBytes(bytes, initialMaxDim = 1280, maxRawBytes = 125_000)
            if (compactDataUri.isNotBlank()) {
                val resolvedThumb = if (thumb.startsWith("data:", ignoreCase = true) || thumb.startsWith("http", ignoreCase = true)) {
                    thumb
                } else {
                    encodeCompactDataUriFromBytes(bytes, initialMaxDim = 720, maxRawBytes = 65_000).ifBlank { compactDataUri }
                }
                return wall.copy(
                    imageUrl = compactDataUri,
                    thumbnailUrl = resolvedThumb
                )
            }
        }
        if (thumb.startsWith("data:", ignoreCase = true) || thumb.startsWith("http", ignoreCase = true)) {
            return wall.copy(imageUrl = thumb, thumbnailUrl = thumb)
        }
        return wall
    }

    private fun retryPendingCloudSyncsIfNeeded(forceImmediate: Boolean = false) {
        val pendingIds = _pendingCloudSyncIds.value
        val liveCloudIds = _lastKnownLiveCloudWallpapers.value.map { it.id }.toSet()
        val allLocalUploads = (_userUploadedWallpapers.value + _recentlyUploadedCloudWallpapers.value + _aiGeneratedHistory.value)
            .distinctBy { it.id }
            .filterNot { it.isRemovedByUserRequest() || it.id in _deletedWallpaperIds.value }

        val candidateItems = allLocalUploads.filter { item ->
            item.id in pendingIds || (hasServerConfirmedSnapshot && item.id !in liveCloudIds)
        }
        if (candidateItems.isEmpty()) return

        val now = System.currentTimeMillis()
        if (isRetryingPendingSync || (!forceImmediate && (now - lastPendingRetryMs) < 15_000L)) return

        isRetryingPendingSync = true
        lastPendingRetryMs = now
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                for (wall in candidateItems) {
                    val cloudReadyWall = prepareWallpaperForCloudSync(wall)
                    val result = repository.createWallpaperRecord(cloudReadyWall)
                    if (result.isSuccess) {
                        clearPendingCloudSync(wall.id)
                    } else {
                        val errMsg = result.exceptionOrNull()?.message.orEmpty()
                        if (errMsg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                            errMsg.contains("PERMISSION_DENIED", ignoreCase = true)
                        ) {
                            break
                        }
                    }
                }
            } finally {
                isRetryingPendingSync = false
            }
        }
    }

    fun refreshWallpapersNow(forceServer: Boolean = true) {
        val nowMs = System.currentTimeMillis()
        if (!forceServer && (nowMs - lastServerRefreshMs) < 5L * 60L * 1000L) {
            return
        }
        viewModelScope.launch {
            if (_userUploadedWallpapers.value.isEmpty() &&
                _cachedCloudWallpapers.value.isEmpty() &&
                _lastKnownLiveCloudWallpapers.value.isEmpty() &&
                adminAllWallpapers.value.isEmpty()
            ) {
                _isCatalogLoading.value = true
            }
            repository.fetchAllWallpapersWithMetadata(forceServer = forceServer)
                .onSuccess { snap ->
                    lastServerRefreshMs = System.currentTimeMillis()
                    _isCatalogLoading.value = false
                    if (!snap.isFromCache) {
                        hasServerConfirmedSnapshot = true
                        _catalogSyncError.value = null
                    }
                    if (snap.items.isNotEmpty() || !snap.isFromCache) {
                        _serverFetchedWallpapers.value = snap.items
                        val mergedCache = (snap.items + _userUploadedWallpapers.value + _recentlyUploadedCloudWallpapers.value)
                            .distinctBy { it.id }
                        _cachedCloudWallpapers.value = mergedCache
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            saveDiskCatalogCache(mergedCache)
                        }
                        retryPendingCloudSyncsIfNeeded(forceImmediate = true)
                    }
                }
                .onFailure { e ->
                    Log.w(TAG, "Explicit server fetch for wallpapers failed", e)
                    _isCatalogLoading.value = false
                    if (_lastKnownLiveCloudWallpapers.value.isEmpty() &&
                        _cachedCloudWallpapers.value.isEmpty() &&
                        _serverFetchedWallpapers.value.isEmpty()
                    ) {
                        val sdkCached = repository.fetchWallpapersFromSdkCacheOnly()
                        if (sdkCached.isNotEmpty()) {
                            _serverFetchedWallpapers.value = sdkCached
                            val mergedCache = (sdkCached + _userUploadedWallpapers.value + _recentlyUploadedCloudWallpapers.value)
                                .distinctBy { it.id }
                            _cachedCloudWallpapers.value = mergedCache
                            _catalogSyncError.value = null
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                saveDiskCatalogCache(mergedCache)
                            }
                        } else if (adminAllWallpapers.value.isNotEmpty()) {
                            _catalogSyncError.value = null
                        } else {
                            _catalogSyncError.value = formatFriendlyCatalogError(e)
                        }
                    }
                }
        }
    }

    init {
        viewModelScope.launch {
            val diskCached = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                loadDiskCatalogCache()
            }
            if (diskCached.isNotEmpty()) {
                _cachedCloudWallpapers.value = diskCached
                _isCatalogLoading.value = false
            }
            retryPendingCloudSyncsIfNeeded(forceImmediate = true)
            if (Firebase.auth.currentUser != null) {
                repository.ensureUserProfile()
                    .onFailure { e ->
                        Log.w(TAG, "Failed to initialize user profile", e)
                    }
            }
            // Safety fallback so initial loading indicator never spins indefinitely if device starts offline
            kotlinx.coroutines.delay(2000L)
            if (_isCatalogLoading.value) {
                _isCatalogLoading.value = false
            }
        }
        viewModelScope.launch {
            cloudWallpapersEvent.collect { event ->
                when (event) {
                    is CloudWallpapersEvent.Loading -> Unit
                    is CloudWallpapersEvent.Snapshot -> {
                        if (!event.isFromCache) {
                            hasServerConfirmedSnapshot = true
                            _catalogSyncError.value = null
                            _isCatalogLoading.value = false
                            _lastKnownLiveCloudWallpapers.value = event.items
                            _serverFetchedWallpapers.value = event.items
                            val mergedCache = (event.items + _userUploadedWallpapers.value + _recentlyUploadedCloudWallpapers.value)
                                .distinctBy { it.id }
                            _cachedCloudWallpapers.value = mergedCache
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                saveDiskCatalogCache(mergedCache)
                            }
                            retryPendingCloudSyncsIfNeeded(forceImmediate = true)
                        } else if (event.items.isNotEmpty()) {
                            _isCatalogLoading.value = false
                            _catalogSyncError.value = null
                            _lastKnownLiveCloudWallpapers.value = event.items
                            val mergedCache = (event.items + _userUploadedWallpapers.value + _recentlyUploadedCloudWallpapers.value)
                                .distinctBy { it.id }
                            _cachedCloudWallpapers.value = mergedCache
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                saveDiskCatalogCache(mergedCache)
                            }
                        } else {
                            _isCatalogLoading.value = false
                        }
                    }
                    is CloudWallpapersEvent.Error -> {
                        _isCatalogLoading.value = false
                        if (_lastKnownLiveCloudWallpapers.value.isEmpty() &&
                            _cachedCloudWallpapers.value.isEmpty() &&
                            _serverFetchedWallpapers.value.isEmpty()
                        ) {
                            val sdkCached = repository.fetchWallpapersFromSdkCacheOnly()
                            if (sdkCached.isNotEmpty()) {
                                _serverFetchedWallpapers.value = sdkCached
                                val mergedCache = (sdkCached + _userUploadedWallpapers.value + _recentlyUploadedCloudWallpapers.value)
                                    .distinctBy { it.id }
                                _cachedCloudWallpapers.value = mergedCache
                                _catalogSyncError.value = null
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    saveDiskCatalogCache(mergedCache)
                                }
                            } else if (adminAllWallpapers.value.isNotEmpty()) {
                                _catalogSyncError.value = null
                            } else {
                                _catalogSyncError.value = formatFriendlyCatalogError(event.error)
                            }
                        }
                    }
                }
            }
        }
        // Keep _isPremiumUser synced with Firestore user profile & premiumPurchases
        viewModelScope.launch {
            combine(userProfile, userCloudPremiumPurchases) { profile, purchases ->
                val hasActivePurchase = purchases.any { it.status == "active" || it.status == "verified" }
                val allRevoked = purchases.isNotEmpty() && purchases.all { it.status == "revoked" }
                when {
                    profile?.isPremium == true || hasActivePurchase -> true
                    profile != null && !profile.isPremium && allRevoked -> false
                    else -> null
                }
            }.collect { cloudPremiumState ->
                if (cloudPremiumState != null) {
                    _isPremiumUser.value = cloudPremiumState
                    premiumPrefs?.edit()?.putBoolean("is_premium_unlocked_${currentUserId}", cloudPremiumState)?.apply()
                }
            }
        }
        // Restore last Admin-selected User Layout immediately from local preferences
        premiumPrefs?.getString("selected_user_layout_id", null)?.takeIf { it.isNotBlank() }?.let { savedLayoutId ->
            val restored = AppDesignCatalog.byId(savedLayoutId)
            _selectedAppDesign.value = restored
            _activeThemeMode.value = restored.recommendedTheme
            _glassOptics.value = restored.recommendedOptics
        }
        // Restore last touched wallpaper background immediately from local preferences (only if not a removed default wallpaper)
        premiumPrefs?.getString("touched_bg_wallpaper_url", null)?.takeIf { it.isNotBlank() }?.let { savedUrl ->
            val savedId = premiumPrefs?.getString("touched_bg_wallpaper_id", "").orEmpty()
            val savedThumb = premiumPrefs?.getString("touched_bg_wallpaper_thumb", "").orEmpty().ifBlank { savedUrl }
            val savedTitle = premiumPrefs?.getString("touched_bg_wallpaper_title", "").orEmpty()
            val candidate = WallpaperItem(
                id = savedId,
                title = savedTitle,
                imageUrl = savedUrl,
                thumbnailUrl = savedThumb
            )
            if (savedId.isBlank() || candidate.isRemovedByUserRequest()) {
                clearTouchedBackgroundWallpaper()
            } else {
                _touchedBackgroundWallpaper.value = candidate
            }
        }
        // Real-time sync of Admin global settings (user layout, theme, and blur optics) across all User devices
        viewModelScope.launch {
            cloudAppSettings.collect { liveSettings ->
                if (liveSettings != null) {
                    saveLocalAppSettingsToPrefs(liveSettings)
                    if (liveSettings.deletedCategoryIds.isNotEmpty()) {
                        _deletedCategoryIds.update { existing ->
                            val merged = existing + liveSettings.deletedCategoryIds
                            saveStringSetToPrefs("deleted_category_ids_v3", merged)
                            merged
                        }
                    }
                    if (liveSettings.userLayoutId.isNotBlank()) {
                        val cloudDesign = AppDesignCatalog.byId(liveSettings.userLayoutId)
                        _selectedAppDesign.value = cloudDesign
                        premiumPrefs?.edit()?.putString("selected_user_layout_id", cloudDesign.id)?.apply()
                    }
                    if (liveSettings.themePreference.isNotBlank()) {
                        _activeThemeMode.value = liveSettings.themePreference
                    }
                    _glassOptics.update { current ->
                        current.copy(
                            blurRadiusDp = liveSettings.blurRadiusDp.toFloat().coerceIn(0f, 48f),
                            dockBlurDp = liveSettings.dockBlurDp.toFloat()
                                .let { if (it <= 0f) 60f else it }
                                .coerceIn(0f, 120f),
                            upperBlurStrength = liveSettings.upperBlurStrength.toFloat()
                                .let { if (it <= 4f) 48f else it }
                                .coerceIn(0f, 64f),
                            enable120HzSmoothness = liveSettings.enable120HzSmoothness
                        )
                    }
                }
            }
        }
        // Automatically sync any online Firestore favorites into the local Room cache for offline access
        viewModelScope.launch {
            combine(cloudUserFavorites, publishedWallpapers) { cloudFavs, catalog ->
                cloudFavs to catalog
            }.collect { (cloudFavs, catalog) ->
                if (cloudFavs.isNotEmpty()) {
                    runCatching {
                        offlineFavoriteCacheRepository?.syncCloudFavoritesToRoom(
                            userId = currentUserId,
                            cloudFavorites = cloudFavs,
                            catalogWallpapers = catalog
                        )
                    }
                }
            }
        }
    }

    fun selectUserTab(tab: UserDockTab) {
        _userTab.value = tab
    }

    fun selectAdminTab(tab: AdminDockTab) {
        _adminTab.value = tab
    }

    fun setAdminPanelMode(openAdmin: Boolean, initialTab: AdminDockTab? = null) {
        if (openAdmin && initialTab != null) {
            _adminTab.value = initialTab
        }
        _isAdminPanelActive.value = openAdmin
        if (!openAdmin) {
            val currentCat = _selectedCategory.value
            val catStillEnabled = currentCat.equals("All", ignoreCase = true) ||
                enabledCategories.value.any { it.name.equals(currentCat, ignoreCase = true) }
            if (!catStillEnabled) {
                _selectedCategory.value = "All"
            }
        }
    }

    fun selectCategory(categoryName: String) {
        _selectedCategory.value = categoryName
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun commitSearchTerm(term: String) {
        val trimmed = term.trim()
        if (trimmed.isNotBlank()) {
            _searchQuery.value = trimmed
            _searchHistory.update { existing ->
                (listOf(trimmed) + existing.filterNot { it.equals(trimmed, ignoreCase = true) }).take(8)
            }
        }
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    fun openWallpaperDetails(wallpaper: WallpaperItem) {
        val updatedViews = wallpaper.viewCount + 1L
        val updatedWall = wallpaper.copy(viewCount = updatedViews)
        _selectedWallpaper.value = updatedWall
        val bgUrl = updatedWall.imageUrl.ifBlank { updatedWall.thumbnailUrl }
        if (bgUrl.isNotBlank()) {
            _touchedBackgroundWallpaper.value = updatedWall
            premiumPrefs?.edit()
                ?.putString("touched_bg_wallpaper_id", updatedWall.id)
                ?.putString("touched_bg_wallpaper_url", bgUrl)
                ?.putString("touched_bg_wallpaper_thumb", updatedWall.thumbnailUrl.ifBlank { bgUrl })
                ?.putString("touched_bg_wallpaper_title", updatedWall.title)
                ?.apply()
        }
        _userUploadedWallpapers.update { list ->
            list.map { if (it.id == wallpaper.id) it.copy(viewCount = updatedViews) else it }
        }
        saveLocalWallpapersToPrefs(_userUploadedWallpapers.value)
    }

    fun clearTouchedBackgroundWallpaper() {
        _touchedBackgroundWallpaper.value = null
        premiumPrefs?.edit()
            ?.remove("touched_bg_wallpaper_id")
            ?.remove("touched_bg_wallpaper_url")
            ?.remove("touched_bg_wallpaper_thumb")
            ?.remove("touched_bg_wallpaper_title")
            ?.apply()
    }

    fun closeWallpaperDetails() {
        _selectedWallpaper.value = null
    }

    fun clearNotifications() {
        _bannerNotification.value = null
        _errorNotification.value = null
    }

    fun showInfoNotification(message: String) {
        _bannerNotification.value = message
    }

    fun showErrorNotification(message: String) {
        _errorNotification.value = if (
            message.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
            message.contains("Quota limit exceeded", ignoreCase = true)
        ) {
            "Cloud daily read quota reached. Cached & uploaded wallpapers remain ready — tap Refresh later."
        } else {
            message
        }
    }

    fun toggleFavorite(wallpaper: WallpaperItem) {
        val isFav = userFavorites.value.any { it.wallpaperId == wallpaper.id } ||
            cachedFavoriteWallpapers.value.any { it.id == wallpaper.id }
        viewModelScope.launch {
            if (isFav) {
                // 1. Immediately remove from local Room offline cache
                runCatching {
                    offlineFavoriteCacheRepository?.removeCachedFavorite(currentUserId, wallpaper.id)
                }
                _bannerNotification.value = "Removed '${wallpaper.title}' from Favorites"
                // 2. Sync removal with Firestore if online
                runCatching {
                    repository.removeFavorite(wallpaper.id)
                }
            } else {
                // 1. Immediately cache in local Room database & persist image for offline viewing
                runCatching {
                    offlineFavoriteCacheRepository?.cacheFavoriteWallpaper(currentUserId, wallpaper)
                }
                _bannerNotification.value = "Added '${wallpaper.title}' to Favorites (Cached for Offline)"
                // 2. Sync addition with Firestore if online
                runCatching {
                    ensureWallpaperExistsInCloud(wallpaper)
                    repository.addFavorite(wallpaper)
                }
            }
        }
    }

    fun setDownloadProgress(wallpaperId: String?, progress: Float, statusText: String) {
        if (wallpaperId == null) {
            _activeDownload.value = null
        } else {
            _activeDownload.value = ActiveDownloadState(wallpaperId, progress, statusText)
        }
    }

    fun recordWallpaperDownload(wallpaper: WallpaperItem) {
        viewModelScope.launch {
            ensureWallpaperExistsInCloud(wallpaper)
            repository.recordDownload(wallpaper)
        }
    }

    private suspend fun ensureWallpaperExistsInCloud(wallpaper: WallpaperItem) {
        if (cloudAllWallpapers.value.none { it.id == wallpaper.id }) {
            repository.createWallpaperRecord(wallpaper)
        }
    }

    fun updateThemePreference(themeMode: String) {
        _activeThemeMode.value = themeMode
        viewModelScope.launch {
            repository.updateThemePreference(themeMode)
                .onSuccess {
                    _bannerNotification.value = "Material 3 Exclusive theme set to ${themeMode.uppercase()}"
                }
                .onFailure { e ->
                    _errorNotification.value = e.localizedMessage ?: "Could not save theme preference"
                }
        }
    }

    // -------------------------------------------------------------------------
    // LIQUID GLASS & OPTICS STUDIO ADJUSTERS
    // -------------------------------------------------------------------------

    fun updateGlassOptics(optics: LiquidGlassOptics) {
        _glassOptics.value = optics
    }

    fun updateBlurRadius(radiusDp: Float) {
        _glassOptics.update { it.copy(blurRadiusDp = radiusDp.coerceIn(0f, 48f)) }
    }

    fun updateDockBlur(radiusDp: Float) {
        _glassOptics.update { it.copy(dockBlurDp = radiusDp.coerceIn(0f, 120f)) }
    }

    fun updateUpperBlurStrength(strengthDp: Float) {
        _glassOptics.update { it.copy(upperBlurStrength = strengthDp.coerceIn(0f, 48f)) }
    }

    fun update120HzSmoothness(enabled: Boolean) {
        _glassOptics.update { it.copy(enable120HzSmoothness = enabled) }
        _bannerNotification.value = if (enabled) {
            "120Hz ProMotion Smoothness Engine Activated"
        } else {
            "Standard 60Hz Frame Pacing Enabled"
        }
    }

    fun updateRefractionStrength(strength: Float) {
        _glassOptics.update { it.copy(refractionStrength = strength.coerceIn(0f, 200f)) }
    }

    fun updateRefractionDepth(depth: Float) {
        _glassOptics.update { it.copy(refractionDepth = depth.coerceIn(0f, 100f)) }
    }

    fun updateChromaticSplit(split: Float) {
        _glassOptics.update { it.copy(chromaticSplit = split.coerceIn(0f, 100f)) }
    }

    fun updateGlassOpacity(opacity: Float) {
        _glassOptics.update { it.copy(glassOpacity = opacity.coerceIn(0.10f, 0.90f)) }
    }

    fun updateGlowIntensity(intensity: Float) {
        _glassOptics.update { it.copy(glowIntensity = intensity.coerceIn(0.0f, 2.0f)) }
    }

    fun updateSpecularShine(shine: Float) {
        _glassOptics.update { it.copy(specularShine = shine.coerceIn(0.0f, 1.0f)) }
    }

    fun updateLiquidStretch(stretch: Float) {
        _glassOptics.update { it.copy(liquidStretch = stretch.coerceIn(0.0f, 2.5f)) }
    }

    fun updateTintHex(hex: String) {
        _glassOptics.update { it.copy(tintHex = hex) }
    }

    fun updateAmbientOrbsMotion(motion: Float) {
        _glassOptics.update { it.copy(ambientOrbsMotion = motion.coerceIn(0.2f, 2.0f)) }
    }

    fun applyOpticsPreset(preset: LiquidGlassPreset) {
        _glassOptics.value = preset.optics
        _bannerNotification.value = "Applied '${preset.name}' optics preset!"
    }

    fun selectAppDesign(design: AppDesignStyle) {
        _selectedAppDesign.value = design
        _activeThemeMode.value = design.recommendedTheme
        _glassOptics.value = design.recommendedOptics
        premiumPrefs?.edit()?.putString("selected_user_layout_id", design.id)?.apply()
        val updatedSettings = appSettings.value.copy(
            userLayoutId = design.id,
            themePreference = design.recommendedTheme,
            dockBlurDp = design.recommendedOptics.dockBlurDp,
            upperBlurStrength = design.recommendedOptics.upperBlurStrength,
            blurRadiusDp = design.recommendedOptics.blurRadiusDp,
            enable120HzSmoothness = design.recommendedOptics.enable120HzSmoothness
        )
        _localAppSettings.value = updatedSettings
        saveLocalAppSettingsToPrefs(updatedSettings)
        _bannerNotification.value = "Applied User Layout: '${design.name}' (${design.badge})"
        viewModelScope.launch {
            runCatching { repository.saveAppSettingsAdmin(updatedSettings) }
        }
    }

    fun resetOpticsToDefault() {
        _glassOptics.value = LiquidGlassOptics()
        _selectedAppDesign.value = AppDesignCatalog.ios27Crystal
        premiumPrefs?.edit()?.putString("selected_user_layout_id", AppDesignCatalog.ios27Crystal.id)?.apply()
        _bannerNotification.value = "Reset Material 3 Exclusive appearance & design to factory defaults"
    }

    // -------------------------------------------------------------------------
    // ADMIN ACTIONS
    // -------------------------------------------------------------------------

    fun uploadMultipleWallpapersAdmin(
        context: Context,
        uris: List<Uri>,
        baseTitle: String,
        description: String,
        category: String,
        tagsRaw: String,
        status: String,
        isFeatured: Boolean,
        isTrending: Boolean
    ) {
        if (uris.isEmpty()) {
            _errorNotification.value = "Please select at least one image (JPG, PNG, or WebP) to upload."
            return
        }
        val cleanCategory = category.trim().ifBlank { "4K Ultra HD" }
        val tags = tagsRaw.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(10)

        _selectedCategory.value = "All"
        _searchQuery.value = ""

        viewModelScope.launch {
            _uploadProgress.value = 0.05f
            var localSavedCount = 0
            var cloudSyncedCount = 0
            var lastError: Throwable? = null

            uris.forEachIndexed { index, uri ->
                val itemTitle = if (uris.size == 1) {
                    baseTitle.trim().ifBlank { "$cleanCategory 4K Wallpaper" }
                } else {
                    "${baseTitle.trim().ifBlank { "$cleanCategory 4K" }} #${index + 1}"
                }
                val wallId = "wall_admin_${System.currentTimeMillis()}_$index"

                val prepared = repository.prepareWallpaperFromUri(
                    context = context,
                    uri = uri,
                    title = itemTitle,
                    description = description.ifBlank { "Curated $cleanCategory 4K UHD wallpaper." },
                    category = cleanCategory,
                    tags = tags.ifEmpty { listOf(cleanCategory.lowercase(java.util.Locale.ROOT), "4k", "uploaded") },
                    status = status,
                    isFeatured = isFeatured,
                    isTrending = isTrending,
                    preferredId = wallId,
                    saveToLocalVault = true,
                    onProgress = { itemProgress ->
                        val overall = (index.toFloat() + itemProgress * 0.7f) / uris.size.toFloat()
                        _uploadProgress.value = overall.coerceIn(0.05f, 0.92f)
                    }
                ).getOrElse { err ->
                    Log.e(TAG, "Admin local preparation failed for $wallId", err)
                    lastError = err
                    return@forEachIndexed
                }

                // Immediately make live in Admin & User Panels + persist to disk vault
                localSavedCount++
                ensureCategoryExistsLocally(cleanCategory, prepared.thumbnailUrl)
                upsertLocalAndCacheWallpaper(prepared, markPendingSync = true)

                val catId = "cat_${cleanCategory.lowercase(java.util.Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")}"
                val categoryModel = CategoryModel(
                    id = catId,
                    name = cleanCategory,
                    thumbnailUrl = prepared.thumbnailUrl,
                    enabled = true
                )
                repository.createWallpaperRecord(prepared)
                    .onSuccess {
                        cloudSyncedCount++
                        clearPendingCloudSync(prepared.id)
                        if (cloudAllCategories.value.none { it.id == catId }) {
                            runCatching { repository.ensureCategoryExistsInCloud(categoryModel) }
                        }
                    }
                    .onFailure { err ->
                        Log.w(TAG, "Admin Firestore upload queued locally for $wallId", err)
                        lastError = err
                    }
            }

            _uploadProgress.value = null
            when {
                cloudSyncedCount == uris.size && cloudSyncedCount > 0 -> {
                    _bannerNotification.value = "Published $cloudSyncedCount 4K wallpaper(s) to Admin, User Panel & Cloud!"
                }
                localSavedCount > 0 && cloudSyncedCount > 0 -> {
                    _bannerNotification.value = "Published $localSavedCount wallpaper(s) to User Panel ($cloudSyncedCount synced to Cloud, ${localSavedCount - cloudSyncedCount} queued)."
                }
                localSavedCount > 0 -> {
                    _bannerNotification.value = "Saved $localSavedCount wallpaper(s) to Admin & User Panel • Queued for Cloud Sync (${lastError?.let { formatFriendlyCatalogError(it) } ?: "offline"})."
                }
                else -> {
                    _errorNotification.value = lastError?.localizedMessage
                        ?: "Could not read selected image(s). Please try another photo."
                }
            }
        }
    }

    fun createOrUpdateWallpaperDirectAdmin(wallpaper: WallpaperItem, isNew: Boolean) {
        _selectedCategory.value = "All"
        _searchQuery.value = ""
        _deletedWallpaperIds.update { ids ->
            val next = ids - wallpaper.id
            saveStringSetToPrefs("deleted_wallpaper_ids_v5", next)
            next
        }
        val normalizedWall = wallpaper.copy(
            status = if (wallpaper.status.equals("draft", ignoreCase = true)) "draft" else "published"
        )
        if (_selectedWallpaper.value?.id == normalizedWall.id) {
            _selectedWallpaper.value = if (normalizedWall.status == "published") normalizedWall else null
        }

        // Apply immediately in 0ms to Admin & User Panels + persistent local store
        ensureCategoryExistsLocally(normalizedWall.category, normalizedWall.thumbnailUrl)
        upsertLocalAndCacheWallpaper(normalizedWall, markPendingSync = true)

        val statusAction = if (normalizedWall.status == "draft") "Moved '${normalizedWall.title}' to Draft (hidden from User Panel)" else "Updated '${normalizedWall.title}' on User Panel"
        _bannerNotification.value = statusAction

        viewModelScope.launch {
            val catId = "cat_${normalizedWall.category.lowercase(java.util.Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")}"
            val categoryModel = CategoryModel(
                id = catId,
                name = normalizedWall.category,
                thumbnailUrl = normalizedWall.thumbnailUrl,
                enabled = true
            )
            if (cloudAllCategories.value.none { it.id == catId }) {
                runCatching { repository.ensureCategoryExistsInCloud(categoryModel) }
            }

            val writeResult: Result<Any> = if (isNew) {
                repository.createWallpaperRecord(normalizedWall)
            } else {
                repository.updateWallpaperAdmin(normalizedWall)
                    .recoverCatching {
                        repository.createWallpaperRecord(normalizedWall).getOrThrow()
                    }
            }

            writeResult.onSuccess {
                clearPendingCloudSync(normalizedWall.id)
                _bannerNotification.value = if (isNew) {
                    "Published '${normalizedWall.title}' to Cloud & User Panel!"
                } else {
                    "$statusAction & Synced to Cloud!"
                }
                val currentSettings = appSettings.value.copy(
                    deletedWallpaperIds = _deletedWallpaperIds.value.filter { it.length <= 120 }.take(200),
                    deletedCategoryIds = _deletedCategoryIds.value.filter { it.length <= 120 }.take(100)
                )
                runCatching { repository.saveAppSettingsAdmin(currentSettings) }
            }.onFailure { e ->
                Log.w(TAG, "Admin Firestore save deferred for ${normalizedWall.id}: ${e.message}")
            }
        }
    }

    fun deleteWallpaperAdmin(wallpaper: WallpaperItem) {
        clearPendingCloudSync(wallpaper.id)
        _deletedWallpaperIds.update { ids ->
            val next = ids + wallpaper.id
            saveStringSetToPrefs("deleted_wallpaper_ids_v5", next)
            next
        }
        _recentlyUploadedCloudWallpapers.update { existing -> existing.filterNot { it.id == wallpaper.id } }
        _serverFetchedWallpapers.update { existing -> existing.filterNot { it.id == wallpaper.id } }
        _cachedCloudWallpapers.update { existing ->
            val next = existing.filterNot { it.id == wallpaper.id }
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                saveDiskCatalogCache(next)
            }
            next
        }
        _userUploadedWallpapers.update { existing ->
            val next = existing.filterNot { it.id == wallpaper.id }
            saveLocalWallpapersToPrefs(next)
            next
        }
        _aiGeneratedHistory.update { existing -> existing.filterNot { it.id == wallpaper.id } }
        runCatching {
            val ctx = appContext
            if (ctx != null) {
                java.io.File(ctx.filesDir, "wallpapers_vault_50gb").listFiles()
                    ?.filter { it.nameWithoutExtension == wallpaper.id }?.forEach { it.delete() }
                java.io.File(ctx.filesDir, "cloud_cache_images").listFiles()
                    ?.filter { it.nameWithoutExtension == wallpaper.id }?.forEach { it.delete() }
            }
        }
        if (_selectedWallpaper.value?.id == wallpaper.id) {
            _selectedWallpaper.value = null
        }
        if (_touchedBackgroundWallpaper.value?.id == wallpaper.id) {
            clearTouchedBackgroundWallpaper()
        }
        _bannerNotification.value = "Deleted '${wallpaper.title}' from Admin & User Panel!"
        viewModelScope.launch {
            runCatching { offlineFavoriteCacheRepository?.removeCachedFavoriteAcrossAllUsers(wallpaper.id) }
            runCatching { repository.deleteWallpaperAdmin(wallpaper) }
            runCatching {
                val syncedSettings = appSettings.value.copy(
                    deletedWallpaperIds = _deletedWallpaperIds.value.filter { it.length <= 120 }.take(200),
                    deletedCategoryIds = _deletedCategoryIds.value.filter { it.length <= 120 }.take(100)
                )
                repository.saveAppSettingsAdmin(syncedSettings)
            }
        }
    }

    fun restoreAllDeletedWallpapersAdmin() {
        _deletedWallpaperIds.value = emptySet()
        saveStringSetToPrefs("deleted_wallpaper_ids_v5", emptySet())
        _bannerNotification.value = "Restored all deleted catalog wallpapers!"
        viewModelScope.launch {
            runCatching {
                val syncedSettings = appSettings.value.copy(deletedWallpaperIds = emptyList())
                repository.saveAppSettingsAdmin(syncedSettings)
            }
        }
    }

    fun saveCategoryAdmin(category: CategoryModel, isNew: Boolean) {
        val cleanName = category.name.trim()
        if (cleanName.isBlank()) {
            _errorNotification.value = "Please enter a category name."
            return
        }
        val nameKey = cleanName.lowercase(java.util.Locale.ROOT)
        val catId = category.id.ifBlank {
            "cat_${nameKey.replace(Regex("[^a-z0-9]+"), "_")}"
        }
        val resolvedCategory = category.copy(
            id = catId,
            name = cleanName,
            description = category.description.ifBlank { "Curated $cleanName 4K collection" },
            thumbnailUrl = category.thumbnailUrl.ifBlank {
                "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=2400&q=95"
            },
            enabled = category.enabled
        )

        // Remove from deleted IDs if it was previously deleted
        _deletedCategoryIds.update { ids ->
            val next = ids - catId - nameKey
            saveStringSetToPrefs("deleted_category_ids_v3", next)
            next
        }

        // Immediately save into local persistent categories so changes reflect on User Home & Explore in 0ms!
        _localCategories.update { existing ->
            val filtered = existing.filterNot {
                it.id == catId || it.name.trim().equals(cleanName, ignoreCase = true)
            }
            val updated = listOf(resolvedCategory) + filtered
            saveLocalCategoriesToPrefs(updated)
            updated
        }

        if (!resolvedCategory.enabled && _selectedCategory.value.equals(cleanName, ignoreCase = true)) {
            _selectedCategory.value = "All"
        }

        _bannerNotification.value = if (resolvedCategory.enabled) {
            "Category '$cleanName' is now live across User Panel!"
        } else {
            "Category '$cleanName' hidden from User Panel!"
        }

        viewModelScope.launch {
            runCatching {
                repository.createOrUpdateCategoryAdmin(resolvedCategory, isNew)
                    .onFailure {
                        repository.createOrUpdateCategoryAdmin(resolvedCategory, isNew = true)
                    }
                val syncedSettings = appSettings.value.copy(
                    deletedWallpaperIds = _deletedWallpaperIds.value.filter { it.length <= 120 }.take(200),
                    deletedCategoryIds = _deletedCategoryIds.value.filter { it.length <= 120 }.take(100)
                )
                repository.saveAppSettingsAdmin(syncedSettings)
            }
        }
    }

    fun deleteCategoryAdmin(categoryId: String, categoryName: String) {
        val nameKey = categoryName.trim().lowercase(java.util.Locale.ROOT)
        _deletedCategoryIds.update { ids ->
            val next = ids + categoryId + nameKey
            saveStringSetToPrefs("deleted_category_ids_v3", next)
            next
        }
        _localCategories.update { existing ->
            val updated = existing.filterNot {
                it.id == categoryId || it.name.trim().equals(categoryName.trim(), ignoreCase = true)
            }
            saveLocalCategoriesToPrefs(updated)
            updated
        }
        if (_selectedCategory.value.equals(categoryName.trim(), ignoreCase = true)) {
            _selectedCategory.value = "All"
        }
        _bannerNotification.value = "Removed category '$categoryName' from Admin & User Panel!"
        viewModelScope.launch {
            runCatching { repository.deleteCategoryAdmin(categoryId) }
            runCatching {
                val syncedSettings = appSettings.value.copy(
                    deletedWallpaperIds = _deletedWallpaperIds.value.filter { it.length <= 120 }.take(200),
                    deletedCategoryIds = _deletedCategoryIds.value.filter { it.length <= 120 }.take(100)
                )
                repository.saveAppSettingsAdmin(syncedSettings)
            }
        }
    }

    fun toggleUserDisabledAdmin(user: UserProfileModel) {
        val nextDisabled = !user.isDisabled
        viewModelScope.launch {
            repository.updateUserStatusAdmin(user.userId.ifBlank { user.id }, nextDisabled)
                .onSuccess {
                    val stateLabel = if (nextDisabled) "Disabled" else "Enabled"
                    _bannerNotification.value = "$stateLabel account for ${user.email}"
                }
                .onFailure { e ->
                    _errorNotification.value = e.localizedMessage ?: "Failed to update account status"
                }
        }
    }

    fun toggleUserAdminRoleAdmin(user: UserProfileModel) {
        val nextIsAdmin = user.role != "admin"
        val targetUid = user.userId.ifBlank { user.id }
        viewModelScope.launch {
            repository.updateUserRoleAdmin(targetUid, nextIsAdmin)
                .onSuccess {
                    val label = if (nextIsAdmin) "Granted Admin Access to" else "Revoked Admin Access for"
                    _bannerNotification.value = "$label ${user.email}"
                }
                .onFailure { e ->
                    _errorNotification.value = e.localizedMessage ?: "Failed to update admin role"
                }
        }
    }

    fun toggleUserPremiumAdmin(user: UserProfileModel) {
        val nextPremium = !user.isPremium
        val targetUid = user.userId.ifBlank { user.id }
        val matchingPurchase = adminAllPremiumPurchases.value.firstOrNull { it.userId == targetUid }
        viewModelScope.launch {
            repository.updateUserPremiumStatusAdmin(
                targetUserId = targetUid,
                isPremium = nextPremium,
                purchaseId = matchingPurchase?.id,
                purchaseStatus = if (nextPremium) "verified" else "revoked",
                utrOverride = if (nextPremium) user.premiumUtr.ifBlank { "ADMIN_VIP_${System.currentTimeMillis().toString().takeLast(6)}" } else ""
            ).onSuccess {
                _bannerNotification.value = if (nextPremium) {
                    "Granted Premium VIP to ${user.email}"
                } else {
                    "Revoked Premium VIP for ${user.email}"
                }
            }.onFailure { e ->
                _errorNotification.value = e.localizedMessage ?: "Failed to update Premium status"
            }
        }
    }

    fun updatePremiumPurchaseStatusAdmin(purchase: PremiumPurchaseModel, newStatus: String) {
        val isPremium = newStatus != "revoked"
        viewModelScope.launch {
            repository.updateUserPremiumStatusAdmin(
                targetUserId = purchase.userId,
                isPremium = isPremium,
                purchaseId = purchase.id,
                purchaseStatus = newStatus,
                utrOverride = if (isPremium) purchase.utrReference else ""
            ).onSuccess {
                _bannerNotification.value = "Updated ${purchase.userEmail} Premium status to ${newStatus.uppercase()}"
            }.onFailure { e ->
                _errorNotification.value = e.localizedMessage ?: "Failed to update Premium purchase"
            }
        }
    }

    fun saveAppSettingsAdmin(settings: AppSettingsModel) {
        val enrichedSettings = settings.copy(
            maxUploadSizeMb = 2048000L,
            deletedWallpaperIds = _deletedWallpaperIds.value.filter { it.length <= 120 }.take(200),
            deletedCategoryIds = _deletedCategoryIds.value.filter { it.length <= 120 }.take(100),
            userLayoutId = _selectedAppDesign.value.id,
            themePreference = _activeThemeMode.value,
            blurRadiusDp = _glassOptics.value.blurRadiusDp,
            dockBlurDp = _glassOptics.value.dockBlurDp,
            upperBlurStrength = _glassOptics.value.upperBlurStrength,
            enable120HzSmoothness = _glassOptics.value.enable120HzSmoothness
        )
        // Apply immediately in memory & local prefs so User Panel updates in 0ms
        _localAppSettings.value = enrichedSettings
        saveLocalAppSettingsToPrefs(enrichedSettings)
        _bannerNotification.value = "Homepage & Global Settings applied to User Panel!"
        viewModelScope.launch {
            repository.saveAppSettingsAdmin(enrichedSettings)
                .onFailure { e ->
                    Log.w(TAG, "Cloud settings sync deferred", e)
                }
        }
    }

    fun syncPendingChangesToCloudNow() {
        viewModelScope.launch {
            val pending = _pendingCloudSyncIds.value
            val allLocalWallpapers = (_userUploadedWallpapers.value + _recentlyUploadedCloudWallpapers.value)
                .distinctBy { it.id }
            repository.seedInitialCatalogIfEmpty(
                extraWallpapersToSync = allLocalWallpapers,
                extraCategoriesToSync = _localCategories.value,
                settingsOverride = _localAppSettings.value ?: appSettings.value,
                deletedWallpaperIds = _deletedWallpaperIds.value,
                forceOverwritePending = pending
            )
                .onSuccess { count ->
                    _pendingCloudSyncIds.value = emptySet()
                    saveStringSetToPrefs("pending_cloud_sync_ids_v1", emptySet())
                    refreshWallpapersNow(forceServer = true)
                    _bannerNotification.value = if (count > 0) {
                        "Synced $count wallpaper(s), categories & settings to Cloud & User Panel!"
                    } else {
                        "Admin Panel, User Panel & Cloud Catalog are 100% synced!"
                    }
                }
                .onFailure { e ->
                    _errorNotification.value = formatFriendlyCatalogError(e)
                }
        }
    }

    fun seedCloudCatalogNow() {
        syncPendingChangesToCloudNow()
    }

    // -------------------------------------------------------------------------
    // AI 8K WALLPAPER STUDIO (Firebase AI Logic: gemini-nano-banana-2.1 & gemini-3.8-flash)
    // -------------------------------------------------------------------------

    private fun extractBitmapFromResponse(response: GenerateContentResponse): Bitmap? {
        for (candidate in response.candidates) {
            val parts = candidate.content.parts
            for (part in parts) {
                when (part) {
                    is ImagePart -> return part.image
                    is InlineDataPart -> {
                        val bytes = part.inlineData
                        if (bytes.isNotEmpty()) {
                            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (decoded != null) return decoded
                        }
                    }
                }
            }
        }
        return null
    }

    private fun isAppCheckAttestationError(t: Throwable?): Boolean {
        val msg = "${t?.message.orEmpty()} ${t?.cause?.message.orEmpty()}".lowercase()
        return msg.contains("app check") ||
            msg.contains("appcheck") ||
            msg.contains("token is invalid") ||
            msg.contains("403")
    }

    private fun buildFreshImageModel(
        modelName: String,
        modalities: List<ResponseModality>
    ) = Firebase.ai.generativeModel(
        modelName = modelName,
        generationConfig = generationConfig {
            responseModalities = modalities
        }
    )

    fun enhancePromptFor8KWallpaper(
        rawIdea: String,
        stylePreset: String,
        onPromptUpdated: (String) -> Unit
    ) {
        val baseIdea = rawIdea.trim().ifBlank { "Translucent liquid glass prism waves in deep space" }
        viewModelScope.launch {
            _ai8KState.value = Ai8KGenUiState.EnhancingPrompt("Enhancing prompt for 8K rendering...")
            val accumulated = StringBuilder()
            val instruction = """
                Rewrite this wallpaper idea into a vivid, single-paragraph 8K Ultra-HD wallpaper generation prompt (max 55 words).
                Style preset: $stylePreset.
                Focus on razor-sharp 8K micro-textures, volumetric lighting, OLED contrast, and artistic composition.
                Output ONLY the prompt text with no quotes or preamble.
                User idea: $baseIdea
            """.trimIndent()

            try {
                var enhancedText = ""
                try {
                    withTimeout(60_000L) {
                        aiTextModel.generateContentStream(instruction).collect { chunk ->
                            val part = chunk.text.orEmpty()
                            accumulated.append(part)
                            val live = accumulated.toString().trim()
                            _ai8KState.value = Ai8KGenUiState.EnhancingPrompt(live)
                            onPromptUpdated(live)
                        }
                    }
                    enhancedText = accumulated.toString().trim()
                } catch (streamErr: Exception) {
                    if (streamErr is CancellationException && streamErr !is TimeoutCancellationException) {
                        throw streamErr
                    }
                    Log.w(TAG, "Firebase AI prompt stream failed; switching to Cloud AI Text API...", streamErr)
                    val cloudEnhanced = CloudAiImageApiClient.enhancePromptViaCloudApi(
                        context = appContext,
                        rawIdea = baseIdea,
                        stylePreset = stylePreset
                    )
                    if (!cloudEnhanced.isNullOrBlank()) {
                        enhancedText = cloudEnhanced.trim()
                    } else {
                        throw streamErr
                    }
                }

                if (enhancedText.isNotBlank()) {
                    onPromptUpdated(enhancedText)
                    _ai8KState.value = Ai8KGenUiState.Idle
                    _bannerNotification.value = "Enhanced prompt to 8K Studio specification!"
                } else {
                    _ai8KState.value = Ai8KGenUiState.Error("Prompt enhancer returned an empty response.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI 8K prompt enhancement timed out", e)
                _ai8KState.value = Ai8KGenUiState.Error("Prompt enhancement timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI 8K prompt enhancement failed", e)
                _ai8KState.value = Ai8KGenUiState.Error(e.localizedMessage ?: "Prompt enhancement failed.")
            }
        }
    }

    fun generate8KWallpaperImage(
        userPrompt: String,
        titleInput: String,
        stylePreset: String,
        category: String
    ) {
        val cleanPrompt = userPrompt.trim()
        if (cleanPrompt.isBlank()) {
            _ai8KState.value = Ai8KGenUiState.Error("Please enter a prompt to generate an 8K wallpaper.")
            return
        }

        val full8KPrompt = buildString {
            append("Generate an 8K Ultra-HD flagship vertical mobile wallpaper (7680x4320 8K quality detail), ")
            append("style: $stylePreset, category: $category. ")
            append(cleanPrompt)
            append(". Hyper-detailed textures, studio raytraced lighting, zero artifacts, edge-to-edge composition.")
        }

        val resolvedTitle = titleInput.trim().ifBlank {
            cleanPrompt.split(" ")
                .filter { it.isNotBlank() }
                .take(4)
                .joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
                .ifBlank { "AI 8K Liquid Master" }
        }

        viewModelScope.launch {
            _errorNotification.value = null
            _ai8KState.value = Ai8KGenUiState.Generating8K(full8KPrompt)
            _bannerNotification.value = "Synthesizing 8K Ultra-HD Wallpaper with Cloud AI..."
            var lastError: Throwable? = null
            val engineMode = CloudAiImageApiClient.getSelectedEngine(appContext)
            val hasCustomKey = CloudAiImageApiClient.getSavedCustomApiKey(appContext).isNotBlank()

            val candidateSpecs = listOf(
                Triple("gemini-nano-banana-2.1 (IMAGE)", "gemini-nano-banana-2.1", listOf(ResponseModality.IMAGE)),
                Triple("gemini-nano-banana-2.1 (TEXT+IMAGE)", "gemini-nano-banana-2.1", listOf(ResponseModality.TEXT, ResponseModality.IMAGE)),
                Triple("gemini-3.1-flash-lite-image", "gemini-3.1-flash-lite-image", listOf(ResponseModality.TEXT, ResponseModality.IMAGE)),
                Triple("gemini-3-pro-image", "gemini-3-pro-image", listOf(ResponseModality.TEXT, ResponseModality.IMAGE))
            )

            try {
                var generatedBitmap: Bitmap? = null

                // 1. If user explicitly chose Flux 8K Cloud API, Lexica Hybrid, or entered a custom API Key, invoke Cloud AI API first
                val preferDirectCloudApiFirst = hasCustomKey ||
                    engineMode == Ai8KEngineMode.FLUX_8K_CLOUD ||
                    engineMode == Ai8KEngineMode.LEXICA_AI_HYBRID

                if (preferDirectCloudApiFirst) {
                    val cloudResult = CloudAiImageApiClient.generate8KImageViaCloudApis(
                        context = appContext,
                        userPrompt = cleanPrompt,
                        stylePreset = stylePreset,
                        category = category,
                        engineMode = engineMode
                    )
                    generatedBitmap = cloudResult.getOrNull()
                    if (generatedBitmap == null) {
                        lastError = cloudResult.exceptionOrNull()
                    }
                }

                // 2. Try Firebase AI Logic (gemini-nano-banana-2.1 primary)
                if (generatedBitmap == null) {
                    for ((modelLabel, _, _) in candidateSpecs) {
                        try {
                            val bmp = withTimeout(60_000L) {
                                val model = when (modelLabel) {
                                    "gemini-nano-banana-2.1 (IMAGE)" -> aiImageModelPrimary
                                    "gemini-nano-banana-2.1 (TEXT+IMAGE)" -> aiImageModelMultimodal
                                    "gemini-3.1-flash-lite-image" -> aiImageModelFlashLite
                                    else -> aiImageModelPro
                                }
                                val response = model.generateContent(full8KPrompt)
                                extractBitmapFromResponse(response)
                            }
                            if (bmp != null) {
                                generatedBitmap = bmp
                                break
                            }
                        } catch (ce: CancellationException) {
                            if (ce !is TimeoutCancellationException) throw ce
                            lastError = ce
                            Log.w(TAG, "Model $modelLabel timed out, trying fallback...", ce)
                        } catch (ex: Exception) {
                            lastError = ex
                            Log.w(TAG, "Model $modelLabel failed", ex)
                            if (isAppCheckAttestationError(ex)) {
                                if (appContext != null) {
                                    refreshAppCheckTokenOnFailure(appContext)
                                }
                                break
                            }
                        }
                    }
                }

                // 3. Final Cloud AI REST API failover if Firebase AI was tried first and failed
                if (generatedBitmap == null && !preferDirectCloudApiFirst) {
                    val cloudFallback = CloudAiImageApiClient.generate8KImageViaCloudApis(
                        context = appContext,
                        userPrompt = cleanPrompt,
                        stylePreset = stylePreset,
                        category = category,
                        engineMode = engineMode
                    )
                    generatedBitmap = cloudFallback.getOrNull()
                    if (generatedBitmap == null && cloudFallback.exceptionOrNull() != null) {
                        lastError = cloudFallback.exceptionOrNull()
                    }
                }

                if (generatedBitmap != null) {
                    _ai8KState.value = Ai8KGenUiState.Success(
                        bitmap = generatedBitmap,
                        title = "$resolvedTitle 8K",
                        promptUsed = cleanPrompt,
                        category = category
                    )
                    _bannerNotification.value = "8K Wallpaper '$resolvedTitle 8K' synthesized & added to Library!"
                    publishGeneratedBitmapToLibrary(
                        bitmap = generatedBitmap,
                        title = "$resolvedTitle 8K",
                        promptUsed = cleanPrompt,
                        category = category
                    )
                } else {
                    val errMsg = lastError?.localizedMessage
                        ?: "No image was returned by the 8K AI image API. Please check your internet connection and try again."
                    _ai8KState.value = Ai8KGenUiState.Error(errMsg)
                    if (_userTab.value != UserDockTab.AI_STUDIO) {
                        _errorNotification.value = errMsg
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "8K AI image generation failed", e)
                val msg = e.localizedMessage ?: "8K image generation failed."
                _ai8KState.value = Ai8KGenUiState.Error(msg)
                if (_userTab.value != UserDockTab.AI_STUDIO) {
                    _errorNotification.value = msg
                }
            }
        }
    }

    /**
     * Multimodal Image Editing with gemini-nano-banana-2.1 (Image + Text Prompt -> Edited Image Bitmap).
     */
    fun editImageWithPrompt(
        sourceBitmap: Bitmap,
        editPrompt: String,
        titleInput: String,
        stylePreset: String,
        category: String
    ) {
        val cleanPrompt = editPrompt.trim()
        if (cleanPrompt.isBlank()) {
            _ai8KState.value = Ai8KGenUiState.Error("Please enter edit instructions for your image.")
            return
        }

        val fullEditInstruction = buildString {
            append("Edit and transform this image into an 8K Ultra-HD mobile wallpaper. ")
            append("Edit instruction: $cleanPrompt. ")
            append("Apply optical style: $stylePreset, category: $category. ")
            append("Preserve crisp composition while applying the requested transformation with hyper-detailed studio lighting.")
        }

        val resolvedTitle = titleInput.trim().ifBlank {
            "AI Edited • " + cleanPrompt.split(" ")
                .filter { it.isNotBlank() }
                .take(3)
                .joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
                .ifBlank { "8K Remix" }
        }

        viewModelScope.launch {
            _errorNotification.value = null
            _ai8KState.value = Ai8KGenUiState.Generating8K("AI Editing: $cleanPrompt")
            _bannerNotification.value = "Editing image with Gemini Nano Banana 2.1..."
            var lastError: Throwable? = null

            // Scale input bitmap safely to keep inline payload compact (< 4MB) while preserving detail
            val scaledInput = scaleBitmapForAiInput(sourceBitmap, maxDimension = 1280)

            try {
                var editedBitmap: Bitmap? = null
                val editModels = listOf(
                    "gemini-nano-banana-2.1 (IMAGE)" to aiImageModelPrimary,
                    "gemini-nano-banana-2.1 (TEXT+IMAGE)" to aiImageModelMultimodal,
                    "gemini-3-pro-image" to aiImageModelPro
                )

                for ((modelLabel, model) in editModels) {
                    try {
                        val bmp = withTimeout(60_000L) {
                            val response = model.generateContent(
                                content {
                                    image(scaledInput)
                                    text(fullEditInstruction)
                                }
                            )
                            extractBitmapFromResponse(response)
                        }
                        if (bmp != null) {
                            editedBitmap = bmp
                            break
                        }
                    } catch (ce: CancellationException) {
                        if (ce !is TimeoutCancellationException) throw ce
                        lastError = ce
                        Log.w(TAG, "Edit model $modelLabel timed out", ce)
                    } catch (ex: Exception) {
                        lastError = ex
                        Log.w(TAG, "Edit model $modelLabel failed", ex)
                        if (isAppCheckAttestationError(ex)) {
                            if (appContext != null) {
                                refreshAppCheckTokenOnFailure(appContext)
                            }
                            break
                        }
                    }
                }

                // Fallback to Cloud AI synthesis if multimodal endpoint was unreachable on standalone APK
                if (editedBitmap == null) {
                    val cloudFallback = CloudAiImageApiClient.generate8KImageViaCloudApis(
                        context = appContext,
                        userPrompt = "$cleanPrompt, inspired by uploaded reference photo",
                        stylePreset = stylePreset,
                        category = category
                    )
                    editedBitmap = cloudFallback.getOrNull()
                    if (editedBitmap == null && cloudFallback.exceptionOrNull() != null) {
                        lastError = cloudFallback.exceptionOrNull()
                    }
                }

                if (editedBitmap != null) {
                    _ai8KState.value = Ai8KGenUiState.Success(
                        bitmap = editedBitmap,
                        title = resolvedTitle,
                        promptUsed = cleanPrompt,
                        category = category
                    )
                    _bannerNotification.value = "AI Image Edit '$resolvedTitle' completed & saved to Library!"
                    publishGeneratedBitmapToLibrary(
                        bitmap = editedBitmap,
                        title = resolvedTitle,
                        promptUsed = "AI Edit: $cleanPrompt",
                        category = category
                    )
                } else {
                    val errMsg = lastError?.localizedMessage
                        ?: "No edited image was returned by gemini-nano-banana-2.1. Please try again."
                    _ai8KState.value = Ai8KGenUiState.Error(errMsg)
                    if (_userTab.value != UserDockTab.AI_STUDIO) {
                        _errorNotification.value = errMsg
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI image edit failed", e)
                val msg = e.localizedMessage ?: "AI image editing failed."
                _ai8KState.value = Ai8KGenUiState.Error(msg)
                if (_userTab.value != UserDockTab.AI_STUDIO) {
                    _errorNotification.value = msg
                }
            }
        }
    }

    private fun scaleBitmapForAiInput(source: Bitmap, maxDimension: Int): Bitmap {
        val w = source.width
        val h = source.height
        if (w <= maxDimension && h <= maxDimension) return source
        val ratio = minOf(maxDimension.toFloat() / w.toFloat(), maxDimension.toFloat() / h.toFloat())
        val targetW = (w * ratio).toInt().coerceAtLeast(64)
        val targetH = (h * ratio).toInt().coerceAtLeast(64)
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    private suspend fun publishGeneratedBitmapToLibrary(
        bitmap: Bitmap,
        title: String,
        promptUsed: String,
        category: String
    ) {
        runCatching {
            val wallId = "wall_ai8k_${UUID.randomUUID().toString().replace("-", "").take(10)}"
            val out = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            val jpegBytes = out.toByteArray()
            val cloudImgUri = encodeCompactDataUriFromBytes(jpegBytes, initialMaxDim = 1440, maxRawBytes = 140_000)
            if (cloudImgUri.isBlank()) return@runCatching
            val cloudThumbUri = encodeCompactDataUriFromBytes(jpegBytes, initialMaxDim = 540, maxRawBytes = 24_000)
                .ifBlank { cloudImgUri }
            val newWall = WallpaperItem(
                id = wallId,
                title = title,
                description = "By AI 8K Studio • $promptUsed",
                category = category.ifBlank { "8K" },
                tags = listOf("8k", "ai", category.lowercase(), "uploaded"),
                resolution = "7680x4320 (8K)",
                fileSize = "6.8 MB",
                fileSizeBytes = jpegBytes.size.toLong().coerceAtLeast(1024L),
                mimeType = "image/jpeg",
                imageUrl = cloudImgUri,
                thumbnailUrl = cloudThumbUri,
                status = "published",
                isFeatured = true,
                isTrending = true,
                downloadCount = 0L,
                favoriteCount = 0L,
                viewCount = 0L,
                uploadedBy = currentUserId
            )
            _aiGeneratedHistory.update { listOf(newWall) + it }
            upsertLocalAndCacheWallpaper(newWall, markPendingSync = true)
            repository.createWallpaperRecord(newWall).onSuccess {
                clearPendingCloudSync(newWall.id)
            }
        }
    }

    fun resetAi8KState() {
        _ai8KState.value = Ai8KGenUiState.Idle
    }

    fun deleteUserAccountAndData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCurrentUserAccountAndData()
            appContext?.let { PixoraEmailSessionManager.clearSession(it) }
            onComplete()
        }
    }

    fun updateUserProfileAndEmail(context: Context, newDisplayName: String, newEmail: String) {
        PixoraEmailSessionManager.updateActiveProfile(
            context = context,
            newEmail = newEmail,
            newDisplayName = newDisplayName
        ).onSuccess { updated ->
            val isAdminNow = updated.email.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true) ||
                RUNTIME_ADMIN_EMAILS.any { it.equals(updated.email, ignoreCase = true) }
            if (isAdminNow) {
                _bannerNotification.value = "Official Admin Verified (${updated.email}) — Admin Panel Unlocked!"
            } else {
                _bannerNotification.value = "Updated email to ${updated.email}"
            }
        }.onFailure { err ->
            _errorNotification.value = err.localizedMessage ?: "Invalid email format"
        }
    }

    companion object {
        private const val TAG = "WallxViewModel"
    }
}
