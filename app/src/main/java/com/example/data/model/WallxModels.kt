package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

const val OFFICIAL_ADMIN_EMAIL = "d99312907@gmail.com"
val RUNTIME_ADMIN_EMAILS = setOf(
    "d99312907@gmail.com"
)
const val MAX_UPLOAD_BYTES = 2L * 1024L * 1024L * 1024L * 1024L // 2 TB (2,048 GB) Admin Cloud & Vault Capacity
const val MAX_FIRESTORE_RULE_BYTES = 2L * 1024L * 1024L * 1024L * 1024L // 2 TB Firestore rule integer cap

val ALLOWED_IMAGE_MIME_TYPES = setOf(
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/webp"
)

@IgnoreExtraProperties
data class UserProfileModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val role: String = "user",
    val status: String = "active",
    val downloadCount: Long = 0L,
    val favoriteCount: Long = 0L,
    val themePreference: String = "light",
    val isPremium: Boolean = false,
    val premiumUtr: String = "",
    val premiumPlan: String = "",
    @ServerTimestamp val premiumPurchasedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isAdminRole: Boolean
        get() = email.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true) ||
            RUNTIME_ADMIN_EMAILS.any { it.equals(email, ignoreCase = true) }

    val isDisabled: Boolean
        get() = status == "disabled"

    fun toCreateMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "role" to role,
            "status" to status,
            "downloadCount" to downloadCount,
            "favoriteCount" to favoriteCount,
            "themePreference" to themePreference,
            "isPremium" to isPremium,
            "premiumUtr" to premiumUtr,
            "premiumPlan" to premiumPlan,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>
    }
}

@IgnoreExtraProperties
data class PremiumPurchaseModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userDisplayName: String = "",
    val utrReference: String = "",
    val planName: String = "₹299 VIP Creator Pass",
    val amountInr: Long = 299L,
    val status: String = "active",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId.trim().take(128),
            "userEmail" to userEmail.trim().ifBlank { "user@pixora.app" }.take(200),
            "userDisplayName" to userDisplayName.trim().ifBlank { "Pixora Member" }.take(100),
            "utrReference" to utrReference.trim().take(64),
            "planName" to planName.trim().ifBlank { "₹299 VIP Creator Pass" }.take(80),
            "amountInr" to amountInr.coerceAtLeast(0L),
            "status" to if (status in setOf("active", "verified", "revoked")) status else "active",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

@IgnoreExtraProperties
data class WallpaperItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "AMOLED",
    val tags: List<String> = emptyList(),
    val resolution: String = "3840x2160 (4K)",
    val fileSize: String = "2.4 MB",
    val fileSizeBytes: Long = 2516582L,
    val mimeType: String = "image/webp",
    val imageUrl: String = "",
    val thumbnailUrl: String = "",
    val storagePath: String = "",
    val status: String = "published",
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val downloadCount: Long = 0L,
    val favoriteCount: Long = 0L,
    val viewCount: Long = 0L,
    val uploadedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val rawImg = imageUrl.ifBlank { thumbnailUrl }
        val safeImg = if (rawImg.startsWith("data:image", ignoreCase = true)) rawImg else rawImg.take(780000)
        val rawThumb = thumbnailUrl.ifBlank { safeImg }
        val safeThumb = if (rawThumb.startsWith("data:image", ignoreCase = true)) rawThumb else rawThumb.take(780000)
        return mapOf(
            "title" to title.trim().ifBlank { "Untitled 4K Wallpaper" }.take(120),
            "description" to description.take(1000),
            "category" to category.trim().ifBlank { "4K Ultra HD" }.take(60),
            "tags" to tags.map { it.trim().take(40) }.filter { it.isNotBlank() }.take(10),
            "resolution" to resolution.trim().ifBlank { "3840x2160 (4K)" }.take(30),
            "fileSize" to fileSize.trim().ifBlank { "4.2 MB" }.take(30),
            "fileSizeBytes" to fileSizeBytes.coerceIn(0L, MAX_FIRESTORE_RULE_BYTES),
            "mimeType" to if (mimeType in ALLOWED_IMAGE_MIME_TYPES) mimeType else "image/jpeg",
            "imageUrl" to safeImg,
            "thumbnailUrl" to safeThumb,
            "storagePath" to storagePath.take(300),
            "status" to if (status.equals("draft", ignoreCase = true)) "draft" else "published",
            "isFeatured" to isFeatured,
            "isTrending" to isTrending,
            "downloadCount" to downloadCount.coerceAtLeast(0L),
            "favoriteCount" to favoriteCount.coerceAtLeast(0L),
            "viewCount" to viewCount.coerceAtLeast(0L),
            "uploadedBy" to uploadedBy.trim().ifBlank { "pixora_admin" }.take(128),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    fun toAdminUpdateMap(): Map<String, Any> {
        val rawImg = imageUrl.ifBlank { thumbnailUrl }
        val safeImg = if (rawImg.startsWith("data:image", ignoreCase = true)) rawImg else rawImg.take(780000)
        val rawThumb = thumbnailUrl.ifBlank { safeImg }
        val safeThumb = if (rawThumb.startsWith("data:image", ignoreCase = true)) rawThumb else rawThumb.take(780000)
        return mapOf(
            "title" to title.trim().ifBlank { "Untitled 4K Wallpaper" }.take(120),
            "description" to description.take(1000),
            "category" to category.trim().ifBlank { "4K Ultra HD" }.take(60),
            "tags" to tags.map { it.trim().take(40) }.filter { it.isNotBlank() }.take(10),
            "resolution" to resolution.trim().ifBlank { "3840x2160 (4K)" }.take(30),
            "fileSize" to fileSize.trim().ifBlank { "4.2 MB" }.take(30),
            "fileSizeBytes" to fileSizeBytes.coerceIn(0L, MAX_FIRESTORE_RULE_BYTES),
            "mimeType" to if (mimeType in ALLOWED_IMAGE_MIME_TYPES) mimeType else "image/jpeg",
            "imageUrl" to safeImg,
            "thumbnailUrl" to safeThumb,
            "storagePath" to storagePath.take(300),
            "status" to if (status.equals("draft", ignoreCase = true)) "draft" else "published",
            "isFeatured" to isFeatured,
            "isTrending" to isTrending,
            "downloadCount" to downloadCount.coerceAtLeast(0L),
            "favoriteCount" to favoriteCount.coerceAtLeast(0L),
            "viewCount" to viewCount.coerceAtLeast(0L),
            "uploadedBy" to uploadedBy.trim().ifBlank { "pixora_admin" }.take(128),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

@IgnoreExtraProperties
data class CategoryModel(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val order: Long = 0L,
    val enabled: Boolean = true,
    val wallpaperCount: Long = 0L,
    val accentHex: String = "#00F0FF",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf(
            "name" to name.trim().ifBlank { "4K Ultra HD" }.take(60),
            "description" to description.take(300),
            "thumbnailUrl" to thumbnailUrl.ifBlank {
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80"
            }.take(780000),
            "order" to order.coerceIn(0L, 1000L),
            "enabled" to enabled,
            "wallpaperCount" to wallpaperCount.coerceAtLeast(0L),
            "accentHex" to accentHex.ifBlank { "#5B2EFF" }.take(12),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    fun toUpdateMap(): Map<String, Any> {
        return mapOf(
            "name" to name.trim().ifBlank { "4K Ultra HD" }.take(60),
            "description" to description.take(300),
            "thumbnailUrl" to thumbnailUrl.ifBlank {
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80"
            }.take(780000),
            "order" to order.coerceIn(0L, 1000L),
            "enabled" to enabled,
            "wallpaperCount" to wallpaperCount.coerceAtLeast(0L),
            "accentHex" to accentHex.ifBlank { "#5B2EFF" }.take(12),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

@IgnoreExtraProperties
data class FavoriteRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val wallpaperId: String = "",
    val wallpaperTitle: String = "",
    val wallpaperThumbnailUrl: String = "",
    val wallpaperCategory: String = "",
    val wallpaperResolution: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "wallpaperId" to wallpaperId,
            "wallpaperTitle" to wallpaperTitle.ifBlank { "4K Wallpaper" }.take(120),
            "wallpaperThumbnailUrl" to wallpaperThumbnailUrl.ifBlank {
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80"
            }.take(780000),
            "wallpaperCategory" to wallpaperCategory.ifBlank { "4K Ultra HD" }.take(60),
            "wallpaperResolution" to wallpaperResolution.ifBlank { "3840x2160 (4K)" }.take(30),
            "createdAt" to FieldValue.serverTimestamp()
        )
    }
}

@IgnoreExtraProperties
data class DownloadRecordModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val wallpaperId: String = "",
    val wallpaperTitle: String = "",
    val wallpaperThumbnailUrl: String = "",
    val wallpaperResolution: String = "",
    val fileSize: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "wallpaperId" to wallpaperId,
            "wallpaperTitle" to wallpaperTitle.ifBlank { "4K Wallpaper" }.take(120),
            "wallpaperThumbnailUrl" to wallpaperThumbnailUrl.ifBlank {
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80"
            }.take(780000),
            "wallpaperResolution" to wallpaperResolution.ifBlank { "3840x2160 (4K)" }.take(30),
            "fileSize" to fileSize.ifBlank { "4.2 MB" }.take(30),
            "createdAt" to FieldValue.serverTimestamp()
        )
    }
}

@IgnoreExtraProperties
data class AppSettingsModel(
    @DocumentId val id: String = "global",
    val appName: String = "Pixora Wallpapers",
    val logoText: String = "IOS 27 • 2 TB CLOUD",
    val maintenanceMode: Boolean = false,
    val maxUploadSizeMb: Long = 2048000L,
    val announcementText: String = "iOS 27 Studio • 2 TB Admin Cloud Vault Active • 4K & 8K Curations Live",
    val announcementEnabled: Boolean = true,
    val heroBannerTitle: String = "",
    val heroBannerSubtitle: String = "",
    val heroBannerImageUrl: String = "",
    val showFeaturedSection: Boolean = true,
    val showTrendingSection: Boolean = true,
    val showCategoriesSection: Boolean = true,
    val showNewArrivalsSection: Boolean = true,
    val deletedWallpaperIds: List<String> = emptyList(),
    val deletedCategoryIds: List<String> = emptyList(),
    val themePreference: String = "light",
    val userLayoutId: String = "ios27_crystal",
    val dockBlurDp: Float = 60f,
    val upperBlurStrength: Float = 48f,
    val blurRadiusDp: Float = 2f,
    val enable120HzSmoothness: Boolean = true,
    val visibility: String = "public",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf(
            "appName" to appName.ifBlank { "Pixora Wallpapers" }.take(60),
            "logoText" to logoText.ifBlank { "IOS 27 • 2 TB CLOUD" }.take(30),
            "maintenanceMode" to maintenanceMode,
            "maxUploadSizeMb" to maxUploadSizeMb.coerceIn(1L, 2048000L),
            "announcementText" to announcementText.take(300),
            "announcementEnabled" to announcementEnabled,
            "heroBannerTitle" to heroBannerTitle.ifBlank { "WALLPAPER OF THE DAY" }.take(120),
            "heroBannerSubtitle" to heroBannerSubtitle.ifBlank { "Curated 4K & 8K Wallpapers" }.take(240),
            "heroBannerImageUrl" to heroBannerImageUrl.ifBlank {
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1600&q=85"
            }.take(780000),
            "showFeaturedSection" to showFeaturedSection,
            "showTrendingSection" to showTrendingSection,
            "showCategoriesSection" to showCategoriesSection,
            "showNewArrivalsSection" to showNewArrivalsSection,
            "deletedWallpaperIds" to deletedWallpaperIds.take(500),
            "deletedCategoryIds" to deletedCategoryIds.take(200),
            "themePreference" to themePreference.ifBlank { "light" },
            "userLayoutId" to userLayoutId.ifBlank { "ios27_crystal" }.take(64),
            "dockBlurDp" to dockBlurDp.toDouble(),
            "upperBlurStrength" to upperBlurStrength.toDouble(),
            "blurRadiusDp" to blurRadiusDp.toDouble(),
            "enable120HzSmoothness" to enable120HzSmoothness,
            "visibility" to "public",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    fun toUpdateMap(): Map<String, Any> {
        return mapOf(
            "appName" to appName.ifBlank { "Pixora Wallpapers" }.take(60),
            "logoText" to logoText.ifBlank { "IOS 27 • 2 TB CLOUD" }.take(30),
            "maintenanceMode" to maintenanceMode,
            "maxUploadSizeMb" to maxUploadSizeMb.coerceIn(1L, 2048000L),
            "announcementText" to announcementText.take(300),
            "announcementEnabled" to announcementEnabled,
            "heroBannerTitle" to heroBannerTitle.ifBlank { "WALLPAPER OF THE DAY" }.take(120),
            "heroBannerSubtitle" to heroBannerSubtitle.ifBlank { "Curated 4K & 8K Wallpapers" }.take(240),
            "heroBannerImageUrl" to heroBannerImageUrl.ifBlank {
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1600&q=85"
            }.take(780000),
            "showFeaturedSection" to showFeaturedSection,
            "showTrendingSection" to showTrendingSection,
            "showCategoriesSection" to showCategoriesSection,
            "showNewArrivalsSection" to showNewArrivalsSection,
            "deletedWallpaperIds" to deletedWallpaperIds.take(500),
            "deletedCategoryIds" to deletedCategoryIds.take(200),
            "themePreference" to themePreference.ifBlank { "light" },
            "userLayoutId" to userLayoutId.ifBlank { "ios27_crystal" }.take(64),
            "dockBlurDp" to dockBlurDp.toDouble(),
            "upperBlurStrength" to upperBlurStrength.toDouble(),
            "blurRadiusDp" to blurRadiusDp.toDouble(),
            "enable120HzSmoothness" to enable120HzSmoothness,
            "visibility" to "public",
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

fun WallpaperItem.isRemovedByUserRequest(): Boolean {
    val cleanTitle = title.trim().lowercase()
    val cleanId = id.trim().lowercase()
    return cleanTitle in WallxCatalogSeed.permanentlyRemovedWallpaperTitles ||
        cleanId.contains("daddy") ||
        cleanId == "default_vault_placeholder"
}

object WallxCatalogSeed {
    val permanentlyRemovedWallpaperTitles: Set<String> = setOf(
        "daddy"
    )

    val defaultCategories: List<CategoryModel> = listOf(
        CategoryModel(
            id = "cat_amoled",
            name = "AMOLED",
            description = "True deep-black pitch contrast with neon refraction",
            thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80&fm=jpg&fit=crop",
            order = 1L,
            enabled = true,
            wallpaperCount = 4L,
            accentHex = "#00F0FF"
        ),
        CategoryModel(
            id = "cat_abstract",
            name = "Abstract",
            description = "Fluid iridescent waves & glass sculptures",
            thumbnailUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=800&q=80&fm=jpg&fit=crop",
            order = 2L,
            enabled = true,
            wallpaperCount = 3L,
            accentHex = "#A855F7"
        ),
        CategoryModel(
            id = "cat_anime",
            name = "Anime",
            description = "Cyberpunk Tokyo & atmospheric cel-shaded vistas",
            thumbnailUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&q=80&fm=jpg&fit=crop",
            order = 3L,
            enabled = true,
            wallpaperCount = 2L,
            accentHex = "#EC4899"
        ),
        CategoryModel(
            id = "cat_nature",
            name = "Nature",
            description = "Bioluminescent forests, glaciers & alpine peaks",
            thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&q=80&fm=jpg&fit=crop",
            order = 4L,
            enabled = true,
            wallpaperCount = 2L,
            accentHex = "#10B981"
        ),
        CategoryModel(
            id = "cat_cars",
            name = "Cars",
            description = "Midnight GT silhouettes & wet asphalt reflections",
            thumbnailUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&q=80&fm=jpg&fit=crop",
            order = 5L,
            enabled = true,
            wallpaperCount = 2L,
            accentHex = "#3B82F6"
        ),
        CategoryModel(
            id = "cat_supercars",
            name = "Supercars",
            description = "Aerodynamic hypercars in wind-tunnel studio lighting",
            thumbnailUrl = "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800&q=80&fm=jpg&fit=crop",
            order = 6L,
            enabled = true,
            wallpaperCount = 2L,
            accentHex = "#F43F5E"
        ),
        CategoryModel(
            id = "cat_gaming",
            name = "Gaming",
            description = "Next-gen raytraced worlds & neon battlegrounds",
            thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&q=80&fm=jpg&fit=crop",
            order = 7L,
            enabled = true,
            wallpaperCount = 2L,
            accentHex = "#8B5CF6"
        ),
        CategoryModel(
            id = "cat_space",
            name = "Space",
            description = "Deep-field nebulae, orbital horizons & event horizons",
            thumbnailUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=800&q=80&fm=jpg&fit=crop",
            order = 8L,
            enabled = true,
            wallpaperCount = 2L,
            accentHex = "#06B6D4"
        ),
        CategoryModel(
            id = "cat_minimal",
            name = "Minimal",
            description = "Pure geometric serenity & negative space",
            thumbnailUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&q=80&fm=jpg&fit=crop",
            order = 9L,
            enabled = true,
            wallpaperCount = 2L,
            accentHex = "#E2E8F0"
        ),
        CategoryModel(
            id = "cat_aesthetic",
            name = "Aesthetic",
            description = "Dreamy vaporwave & pastel glass refractions",
            thumbnailUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=800&q=80&fm=jpg&fit=crop",
            order = 10L,
            enabled = true,
            wallpaperCount = 2L,
            accentHex = "#F472B6"
        ),
        CategoryModel(
            id = "cat_architecture",
            name = "Architecture",
            description = "Brutalist glass monoliths & futuristic skylines",
            thumbnailUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=800&q=80&fm=jpg&fit=crop",
            order = 11L,
            enabled = true,
            wallpaperCount = 2L,
            accentHex = "#38BDF8"
        ),
        CategoryModel(
            id = "cat_dark",
            name = "Dark",
            description = "Stealth obsidian textures & low-luminance titanium",
            thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80&fm=jpg&fit=crop",
            order = 12L,
            enabled = true,
            wallpaperCount = 2L,
            accentHex = "#64748B"
        ),
        CategoryModel(
            id = "cat_4k",
            name = "4K",
            description = "Pin-sharp 2160p masters calibrated for OLED",
            thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=80&fm=jpg&fit=crop",
            order = 13L,
            enabled = true,
            wallpaperCount = 3L,
            accentHex = "#22D3EE"
        ),
        CategoryModel(
            id = "cat_8k",
            name = "8K",
            description = "Ultra-high-density 4320p flagship exhibition series",
            thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80&fm=jpg&fit=crop",
            order = 14L,
            enabled = true,
            wallpaperCount = 3L,
            accentHex = "#C084FC"
        )
    )

    val legacyDefaultSeedWallpaperIds: Set<String> = emptySet()

    fun defaultWallpapers(uploadedByUid: String = "system_admin"): List<WallpaperItem> = listOf(
        WallpaperItem(
            id = "wall_admin_studio_01",
            title = "Liquid Prism 8K",
            description = "iOS 27 obsidian crystal wave with deep AMOLED contrast.",
            category = "AMOLED",
            tags = listOf("amoled", "dark", "liquid", "8k"),
            resolution = "3840x2160 (4K)",
            fileSize = "4.8 MB",
            fileSizeBytes = 5033164L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = true,
            isTrending = true,
            downloadCount = 1420L,
            favoriteCount = 640L,
            viewCount = 4120L,
            uploadedBy = uploadedByUid
        ),
        WallpaperItem(
            id = "wall_admin_studio_02",
            title = "Cyber Samurai Neon",
            description = "Neo-Tokyo rain reflections in ultra-crisp 4K HDR.",
            category = "Anime",
            tags = listOf("anime", "cyberpunk", "neon", "4k"),
            resolution = "3840x2160 (4K)",
            fileSize = "5.1 MB",
            fileSizeBytes = 5347737L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = true,
            isTrending = true,
            downloadCount = 1890L,
            favoriteCount = 920L,
            viewCount = 5300L,
            uploadedBy = uploadedByUid
        ),
        WallpaperItem(
            id = "wall_admin_studio_03",
            title = "Alpine Emerald Valley",
            description = "Crystal mountain lake reflection at golden sunrise.",
            category = "Nature",
            tags = listOf("nature", "mountains", "4k", "scenic"),
            resolution = "3840x2160 (4K)",
            fileSize = "6.2 MB",
            fileSizeBytes = 6501171L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = true,
            isTrending = true,
            downloadCount = 1650L,
            favoriteCount = 780L,
            viewCount = 4890L,
            uploadedBy = uploadedByUid
        ),
        WallpaperItem(
            id = "wall_admin_studio_04",
            title = "Midnight GT Hypercar",
            description = "Stealth aerodynamic supercar under studio rim lighting.",
            category = "Cars",
            tags = listOf("cars", "supercars", "amoled", "4k"),
            resolution = "3840x2160 (4K)",
            fileSize = "4.6 MB",
            fileSizeBytes = 4823449L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = true,
            isTrending = true,
            downloadCount = 2140L,
            favoriteCount = 1050L,
            viewCount = 6100L,
            uploadedBy = uploadedByUid
        ),
        WallpaperItem(
            id = "wall_admin_studio_05",
            title = "Iridescent Silk Flow",
            description = "Chromatic liquid glass ribbons calibrated for 120Hz displays.",
            category = "Abstract",
            tags = listOf("abstract", "liquid", "minimal", "4k"),
            resolution = "3840x2160 (4K)",
            fileSize = "4.3 MB",
            fileSizeBytes = 4508876L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = true,
            isTrending = true,
            downloadCount = 1320L,
            favoriteCount = 610L,
            viewCount = 3740L,
            uploadedBy = uploadedByUid
        ),
        WallpaperItem(
            id = "wall_admin_studio_06",
            title = "Deep Nebula Horizon",
            description = "Cosmic starfield and violet interstellar dust clouds in 8K.",
            category = "Space",
            tags = listOf("space", "amoled", "cosmos", "8k"),
            resolution = "7680x4320 (8K)",
            fileSize = "7.4 MB",
            fileSizeBytes = 7759462L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = true,
            isTrending = true,
            downloadCount = 1760L,
            favoriteCount = 840L,
            viewCount = 4950L,
            uploadedBy = uploadedByUid
        ),
        WallpaperItem(
            id = "wall_admin_studio_07",
            title = "Neon Raytraced Arena",
            description = "Next-gen cyberpunk gaming horizon with volumetric neon fog.",
            category = "Gaming",
            tags = listOf("gaming", "cyber", "neon", "4k"),
            resolution = "3840x2160 (4K)",
            fileSize = "5.5 MB",
            fileSizeBytes = 5767168L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = false,
            isTrending = true,
            downloadCount = 1540L,
            favoriteCount = 720L,
            viewCount = 4320L,
            uploadedBy = uploadedByUid
        ),
        WallpaperItem(
            id = "wall_admin_studio_08",
            title = "Valkyrie Track Edition",
            description = "Carbon-fiber hypercar silhouette in moody studio lighting.",
            category = "Supercars",
            tags = listOf("supercars", "cars", "dark", "4k"),
            resolution = "3840x2160 (4K)",
            fileSize = "4.9 MB",
            fileSizeBytes = 5138022L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = false,
            isTrending = true,
            downloadCount = 1680L,
            favoriteCount = 810L,
            viewCount = 4610L,
            uploadedBy = uploadedByUid
        ),
        WallpaperItem(
            id = "wall_admin_studio_09",
            title = "Pastel Vapor Refraction",
            description = "Dreamy frosted glass gradients and soft aurora light.",
            category = "Aesthetic",
            tags = listOf("aesthetic", "pastel", "minimal", "4k"),
            resolution = "3840x2160 (4K)",
            fileSize = "3.9 MB",
            fileSizeBytes = 4089446L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = false,
            isTrending = true,
            downloadCount = 1290L,
            favoriteCount = 590L,
            viewCount = 3510L,
            uploadedBy = uploadedByUid
        ),
        WallpaperItem(
            id = "wall_admin_studio_10",
            title = "Obsidian Titanium Dark",
            description = "Ultra-low luminance brushed obsidian texture for OLED battery saving.",
            category = "Dark",
            tags = listOf("dark", "amoled", "minimal", "4k"),
            resolution = "3840x2160 (4K)",
            fileSize = "3.7 MB",
            fileSizeBytes = 3879731L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = false,
            isTrending = true,
            downloadCount = 1490L,
            favoriteCount = 700L,
            viewCount = 4190L,
            uploadedBy = uploadedByUid
        ),
        WallpaperItem(
            id = "wall_admin_studio_11",
            title = "Nordic Minimal Coast",
            description = "Serene turquoise ocean horizon with soft morning mist.",
            category = "Minimal",
            tags = listOf("minimal", "nature", "calm", "4k"),
            resolution = "3840x2160 (4K)",
            fileSize = "4.1 MB",
            fileSizeBytes = 4299161L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = false,
            isTrending = true,
            downloadCount = 1180L,
            favoriteCount = 540L,
            viewCount = 3290L,
            uploadedBy = uploadedByUid
        ),
        WallpaperItem(
            id = "wall_admin_studio_12",
            title = "Glass Monolith Skyline",
            description = "Futuristic architectural glass tower reflecting twilight clouds.",
            category = "Architecture",
            tags = listOf("architecture", "glass", "city", "4k"),
            resolution = "3840x2160 (4K)",
            fileSize = "5.3 MB",
            fileSizeBytes = 5557452L,
            mimeType = "image/jpeg",
            imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=2160&q=92&fm=jpg&fit=crop",
            thumbnailUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=900&q=85&fm=jpg&fit=crop",
            status = "published",
            isFeatured = false,
            isTrending = true,
            downloadCount = 1360L,
            favoriteCount = 630L,
            viewCount = 3820L,
            uploadedBy = uploadedByUid
        )
    )
}
