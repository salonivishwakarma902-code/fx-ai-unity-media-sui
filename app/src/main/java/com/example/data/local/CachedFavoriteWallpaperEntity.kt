package com.example.data.local

import com.example.data.model.FavoriteRecord
import com.example.data.model.WallpaperItem
import com.google.firebase.Timestamp

data class CachedFavoriteWallpaperEntity(
    val id: String,
    val userId: String,
    val wallpaperId: String,
    val title: String,
    val description: String = "",
    val category: String = "4K UHD",
    val tagsCsv: String = "",
    val resolution: String = "3840x2160 (4K)",
    val fileSize: String = "2.4 MB",
    val fileSizeBytes: Long = 2516582L,
    val mimeType: String = "image/jpeg",
    val imageUrl: String = "",
    val thumbnailUrl: String = "",
    val localCachedImageUri: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun effectiveImageUrl(): String {
        return localCachedImageUri.ifBlank { imageUrl.ifBlank { thumbnailUrl } }
    }

    fun toFavoriteRecord(): FavoriteRecord {
        val sec = (createdAtMillis / 1000L).coerceAtLeast(0L)
        val nano = ((createdAtMillis % 1000L) * 1_000_000L).toInt().coerceIn(0, 999_999_999)
        return FavoriteRecord(
            id = id,
            userId = userId,
            wallpaperId = wallpaperId,
            wallpaperTitle = title,
            wallpaperThumbnailUrl = effectiveImageUrl(),
            wallpaperCategory = category,
            wallpaperResolution = resolution,
            createdAt = Timestamp(sec, nano)
        )
    }

    fun toWallpaperItem(): WallpaperItem {
        val sec = (createdAtMillis / 1000L).coerceAtLeast(0L)
        val nano = ((createdAtMillis % 1000L) * 1_000_000L).toInt().coerceIn(0, 999_999_999)
        val bestUrl = effectiveImageUrl()
        val parsedTags = tagsCsv.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf(category.lowercase(), "4k", "offline_cached") }
        return WallpaperItem(
            id = wallpaperId,
            title = title.ifBlank { "4K Wallpaper" },
            description = description.ifBlank { "Cached in Room Database for offline 4K viewing." },
            category = category.ifBlank { "4K UHD" },
            tags = parsedTags,
            resolution = resolution.ifBlank { "3840x2160 (4K)" },
            fileSize = fileSize.ifBlank { "2.4 MB" },
            fileSizeBytes = fileSizeBytes,
            mimeType = mimeType,
            imageUrl = bestUrl,
            thumbnailUrl = bestUrl,
            status = "published",
            isFeatured = true,
            isTrending = true,
            downloadCount = 1L,
            favoriteCount = 1L,
            uploadedBy = userId,
            createdAt = Timestamp(sec, nano),
            updatedAt = Timestamp(sec, nano)
        )
    }

    companion object {
        fun fromWallpaperItem(
            userId: String,
            wallpaper: WallpaperItem,
            localCachedUri: String = "",
            createdAtMillis: Long = System.currentTimeMillis()
        ): CachedFavoriteWallpaperEntity {
            val safeUserId = userId.ifBlank { "pixora_local_user" }
            val compositeId = "${safeUserId}_${wallpaper.id}"
            return CachedFavoriteWallpaperEntity(
                id = compositeId,
                userId = safeUserId,
                wallpaperId = wallpaper.id,
                title = wallpaper.title.ifBlank { "4K Wallpaper" },
                description = wallpaper.description,
                category = wallpaper.category.ifBlank { "4K UHD" },
                tagsCsv = wallpaper.tags.joinToString(","),
                resolution = wallpaper.resolution.ifBlank { "3840x2160 (4K)" },
                fileSize = wallpaper.fileSize.ifBlank { "2.4 MB" },
                fileSizeBytes = wallpaper.fileSizeBytes,
                mimeType = wallpaper.mimeType,
                imageUrl = wallpaper.imageUrl.ifBlank { wallpaper.thumbnailUrl },
                thumbnailUrl = wallpaper.thumbnailUrl.ifBlank { wallpaper.imageUrl },
                localCachedImageUri = localCachedUri,
                createdAtMillis = createdAtMillis
            )
        }
    }
}
