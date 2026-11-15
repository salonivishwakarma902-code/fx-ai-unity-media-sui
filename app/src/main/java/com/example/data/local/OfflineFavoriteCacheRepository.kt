package com.example.data.local

import android.content.Context
import com.example.data.model.FavoriteRecord
import com.example.data.model.WallpaperItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Room-backed Repository for offline caching of user's favorite wallpapers.
 * Stores metadata in SQLite via [FavoriteWallpaperDao] and caches the wallpaper image file on disk
 * so favorite wallpapers remain 100% viewable and applicable even when the device is offline.
 */
class OfflineFavoriteCacheRepository(
    private val favoriteDao: FavoriteWallpaperDao,
    private val appContext: Context? = null
) {
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun observeCachedFavorites(userId: String): Flow<List<CachedFavoriteWallpaperEntity>> {
        val safeUserId = userId.ifBlank { "pixora_local_user" }
        return favoriteDao.observeFavoritesForUser(safeUserId)
    }

    fun observeAllCachedFavorites(): Flow<List<CachedFavoriteWallpaperEntity>> {
        return favoriteDao.observeAllCachedFavorites()
    }

    suspend fun isFavoriteCached(userId: String, wallpaperId: String): Boolean {
        val safeUserId = userId.ifBlank { "pixora_local_user" }
        return favoriteDao.isFavoriteCached(safeUserId, wallpaperId)
    }

    suspend fun getCachedFavorite(userId: String, wallpaperId: String): CachedFavoriteWallpaperEntity? {
        val safeUserId = userId.ifBlank { "pixora_local_user" }
        return favoriteDao.getCachedFavorite(safeUserId, wallpaperId)
    }

    /**
     * Immediately inserts the favorite wallpaper into Room so the UI updates instantaneously
     * (even when offline), and then caches a local copy of the image file on disk if reachable.
     */
    suspend fun cacheFavoriteWallpaper(
        userId: String,
        wallpaper: WallpaperItem
    ): CachedFavoriteWallpaperEntity = withContext(Dispatchers.IO) {
        val safeUserId = userId.ifBlank { "pixora_local_user" }
        val existing = favoriteDao.getCachedFavorite(safeUserId, wallpaper.id)
        val initialLocalUri = existing?.localCachedImageUri.orEmpty()

        val initialEntity = CachedFavoriteWallpaperEntity.fromWallpaperItem(
            userId = safeUserId,
            wallpaper = wallpaper,
            localCachedUri = initialLocalUri,
            createdAtMillis = existing?.createdAtMillis ?: System.currentTimeMillis()
        )
        favoriteDao.upsertFavorite(initialEntity)

        // Attempt to persist the image file locally in `offline_favorites_cache/` so it loads offline
        val downloadedLocalUri = downloadAndCacheImageForOffline(wallpaper)
        if (!downloadedLocalUri.isNullOrBlank() && downloadedLocalUri != initialLocalUri) {
            val enrichedEntity = initialEntity.copy(localCachedImageUri = downloadedLocalUri)
            favoriteDao.upsertFavorite(enrichedEntity)
            enrichedEntity
        } else {
            initialEntity
        }
    }

    suspend fun removeCachedFavorite(userId: String, wallpaperId: String) = withContext(Dispatchers.IO) {
        val safeUserId = userId.ifBlank { "pixora_local_user" }
        val existing = favoriteDao.getCachedFavorite(safeUserId, wallpaperId)
        favoriteDao.deleteFavorite(safeUserId, wallpaperId)
        // Clean up local cached file if present
        existing?.localCachedImageUri?.let { uriStr ->
            if (uriStr.startsWith("file:")) {
                runCatching {
                    val file = File(java.net.URI(uriStr))
                    if (file.exists() && file.parentFile?.name == "offline_favorites_cache") {
                        file.delete()
                    }
                }
            }
        }
    }

    suspend fun removeCachedFavoriteAcrossAllUsers(wallpaperId: String) = withContext(Dispatchers.IO) {
        favoriteDao.deleteByWallpaperId(wallpaperId)
    }

    suspend fun clearUserCachedFavorites(userId: String) = withContext(Dispatchers.IO) {
        val safeUserId = userId.ifBlank { "pixora_local_user" }
        favoriteDao.clearFavoritesForUser(safeUserId)
    }

    /**
     * Synchronizes remote Firestore [FavoriteRecord] items into the local Room cache so that
     * any favorites added online are automatically available when the user later goes offline.
     */
    suspend fun syncCloudFavoritesToRoom(
        userId: String,
        cloudFavorites: List<FavoriteRecord>,
        catalogWallpapers: List<WallpaperItem>
    ) = withContext(Dispatchers.IO) {
        if (cloudFavorites.isEmpty()) return@withContext
        val safeUserId = userId.ifBlank { "pixora_local_user" }
        val catalogById = catalogWallpapers.associateBy { it.id }

        val entitiesToUpsert = cloudFavorites.mapNotNull { fav ->
            val wallId = fav.wallpaperId.trim()
            if (wallId.isBlank()) return@mapNotNull null

            val existing = favoriteDao.getCachedFavorite(safeUserId, wallId)
            val matchedCatalogItem = catalogById[wallId]
            val createdMillis = fav.createdAt?.toDate()?.time
                ?: existing?.createdAtMillis
                ?: System.currentTimeMillis()

            if (matchedCatalogItem != null) {
                CachedFavoriteWallpaperEntity.fromWallpaperItem(
                    userId = safeUserId,
                    wallpaper = matchedCatalogItem,
                    localCachedUri = existing?.localCachedImageUri.orEmpty(),
                    createdAtMillis = createdMillis
                )
            } else {
                val fallbackUrl = fav.wallpaperThumbnailUrl.ifBlank {
                    existing?.imageUrl.orEmpty()
                }
                CachedFavoriteWallpaperEntity(
                    id = "${safeUserId}_$wallId",
                    userId = safeUserId,
                    wallpaperId = wallId,
                    title = fav.wallpaperTitle.ifBlank { existing?.title ?: "4K Wallpaper" },
                    description = existing?.description ?: "Cached in Room Database for offline 4K viewing.",
                    category = fav.wallpaperCategory.ifBlank { existing?.category ?: "4K UHD" },
                    tagsCsv = existing?.tagsCsv ?: "${fav.wallpaperCategory.lowercase()},4k,offline_cached",
                    resolution = fav.wallpaperResolution.ifBlank { existing?.resolution ?: "3840x2160 (4K)" },
                    fileSize = existing?.fileSize ?: "2.4 MB",
                    fileSizeBytes = existing?.fileSizeBytes ?: 2516582L,
                    mimeType = existing?.mimeType ?: "image/jpeg",
                    imageUrl = fallbackUrl,
                    thumbnailUrl = fallbackUrl,
                    localCachedImageUri = existing?.localCachedImageUri.orEmpty(),
                    createdAtMillis = createdMillis
                )
            }
        }

        if (entitiesToUpsert.isNotEmpty()) {
            favoriteDao.upsertAllFavorites(entitiesToUpsert)
        }
    }

    private fun downloadAndCacheImageForOffline(wallpaper: WallpaperItem): String? {
        val ctx = appContext ?: return null
        val sourceUrl = wallpaper.imageUrl.ifBlank { wallpaper.thumbnailUrl }.trim()
        if (sourceUrl.isBlank()) return null

        // Already a local drawable, file URI, or inline data URI — works 100% offline already
        if (sourceUrl.startsWith("drawable://", ignoreCase = true) ||
            sourceUrl.startsWith("file:", ignoreCase = true) ||
            sourceUrl.startsWith("data:image", ignoreCase = true)
        ) {
            return sourceUrl
        }

        // Download HTTP/HTTPS wallpaper image into local Room offline cache directory
        if (sourceUrl.startsWith("http://", ignoreCase = true) ||
            sourceUrl.startsWith("https://", ignoreCase = true)
        ) {
            return runCatching {
                val cacheDir = File(ctx.filesDir, "offline_favorites_cache").apply { mkdirs() }
                val safeId = wallpaper.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "fav_wall" }
                val targetFile = File(cacheDir, "${safeId}.jpg")
                if (targetFile.exists() && targetFile.length() > 1024L) {
                    return@runCatching targetFile.toURI().toString()
                }
                val request = Request.Builder().url(sourceUrl).build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    val bytes = response.body?.bytes() ?: return@use null
                    if (bytes.isEmpty()) return@use null
                    targetFile.writeBytes(bytes)
                    targetFile.toURI().toString()
                }
            }.getOrNull()
        }

        return null
    }
}
