package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.AutoWallpaperChangerManager
import com.example.ui.components.ios27LiquidStretch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.AppSettingsModel
import com.example.data.model.CategoryModel
import com.example.data.model.DownloadRecordModel
import com.example.data.model.FavoriteRecord
import com.example.data.model.UserProfileModel
import com.example.data.model.WallpaperItem
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassCircularButton
import com.example.ui.components.LiquidGlassScreenWithUpperBlur
import com.example.ui.components.LiquidGlassSlider
import com.example.ui.components.PlayStoreDeleteAccountDialog
import com.example.ui.components.PlayStoreEmailCenterDialog
import com.example.ui.components.PlayStorePrivacyPolicyDialog
import com.example.ui.components.PlayStoreTermsAndAiPolicyDialog
import com.example.ui.components.WallxAsyncImage
import com.example.ui.components.premiumSmoothClick
import com.example.ui.components.shimmerSweepHighlight
import com.example.ui.components.staggeredBouncyEntrance
import com.example.ui.components.subtleFloatingBounce
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AppDesignCatalog
import com.example.ui.viewmodel.deduplicateWallpapers
import com.example.ui.viewmodel.normalizeWallpaperImageKey
import com.example.ui.theme.AppDesignStyle
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LiquidGlassOptics
import com.example.ui.theme.LiquidGlassPreset
import com.example.ui.theme.LocalAppDesignStyle
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RoseGlow
import java.util.Locale

// =============================================================================
// CURATED 4K WALLPAPERS CATALOG MATCHING THE 12-SCREEN REFERENCE IMAGE
// =============================================================================

private val PrimaryPurple = Color(0xFF5B2EFF)
private val SecondaryBlue = Color(0xFF1D74FF)
private val LavenderPageBg = Color.Transparent
private val DarkPageBg = Color.Transparent

data class FourKCategoryCardSpec(
    val title: String,
    val countLabel: String,
    val imageUrl: String,
    val icon: ImageVector,
    val pastelBg: Color,
    val iconTint: Color
)

private val FOUR_K_CATEGORIES_CATALOG = listOf(
    FourKCategoryCardSpec(
        title = "Anime",
        countLabel = "12K+",
        imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=3840&q=100",
        icon = Icons.Default.Face,
        pastelBg = Color(0xFFEAE3FF),
        iconTint = Color(0xFF7C3AED)
    ),
    FourKCategoryCardSpec(
        title = "Nature",
        countLabel = "20K+",
        imageUrl = "drawable://home_hero_mountain_4k",
        icon = Icons.Default.Eco,
        pastelBg = Color(0xFFDCF6E6),
        iconTint = Color(0xFF16A34A)
    ),
    FourKCategoryCardSpec(
        title = "Cars",
        countLabel = "15K+",
        imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=3840&q=100",
        icon = Icons.Default.DirectionsCar,
        pastelBg = Color(0xFFFFE4E6),
        iconTint = Color(0xFFE11D48)
    ),
    FourKCategoryCardSpec(
        title = "Gaming",
        countLabel = "18K+",
        imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=3840&q=100",
        icon = Icons.Default.SportsEsports,
        pastelBg = Color(0xFFE2ECFF),
        iconTint = Color(0xFF2563EB)
    ),
    FourKCategoryCardSpec(
        title = "Abstract",
        countLabel = "10K+",
        imageUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=3840&q=100",
        icon = Icons.Default.AutoAwesome,
        pastelBg = Color(0xFFE0F2FE),
        iconTint = Color(0xFF0284C7)
    ),
    FourKCategoryCardSpec(
        title = "Minimal",
        countLabel = "7K+",
        imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=3840&q=100",
        icon = Icons.Default.Palette,
        pastelBg = Color(0xFFF3E8FF),
        iconTint = Color(0xFF9333EA)
    ),
    FourKCategoryCardSpec(
        title = "4K Ultra HD",
        countLabel = "25K+",
        imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=3840&q=100",
        icon = Icons.Default.HighQuality,
        pastelBg = Color(0xFFDBEAFE),
        iconTint = Color(0xFF2563EB)
    ),
    FourKCategoryCardSpec(
        title = "Aesthetic",
        countLabel = "9K+",
        imageUrl = "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=3840&q=100",
        icon = Icons.Default.Favorite,
        pastelBg = Color(0xFFFFEDD5),
        iconTint = Color(0xFFEA580C)
    )
)

private fun buildDynamicCategorySpecs(
    categories: List<CategoryModel>,
    wallpapers: List<WallpaperItem>
): List<FourKCategoryCardSpec> {
    val palette = listOf(
        Triple(Icons.Default.AutoAwesome, Color(0xFFEAE3FF), Color(0xFF5B2EFF)),
        Triple(Icons.Default.Eco, Color(0xFFDCF6E6), Color(0xFF16A34A)),
        Triple(Icons.Default.DirectionsCar, Color(0xFFFFE4E6), Color(0xFFE11D48)),
        Triple(Icons.Default.SportsEsports, Color(0xFFE2ECFF), Color(0xFF2563EB)),
        Triple(Icons.Default.Palette, Color(0xFFE0F2FE), Color(0xFF0284C7)),
        Triple(Icons.Default.HighQuality, Color(0xFFF3E8FF), Color(0xFF9333EA)),
        Triple(Icons.Default.Favorite, Color(0xFFFFEDD5), Color(0xFFEA580C)),
        Triple(Icons.Default.Category, Color(0xFFDBEAFE), Color(0xFF2563EB))
    )

    val distinctFallbackCovers = listOf(
        "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=3840&q=100",
        "drawable://home_hero_mountain_4k",
        "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=3840&q=100",
        "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=3840&q=100",
        "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=3840&q=100",
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=3840&q=100",
        "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=3840&q=100",
        "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=3840&q=100",
        "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=3840&q=100",
        "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?w=3840&q=100",
        "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=3840&q=100",
        "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=3840&q=100"
    )

    val usedCoverKeys = mutableSetOf<String>()

    fun pickUniqueCover(candidates: List<String>, index: Int): String {
        for (url in candidates) {
            if (url.isNotBlank()) {
                val key = normalizeWallpaperImageKey(url)
                if (key.isNotBlank() && usedCoverKeys.add(key)) {
                    return url
                }
            }
        }
        for (fallback in distinctFallbackCovers) {
            val key = normalizeWallpaperImageKey(fallback)
            if (usedCoverKeys.add(key)) {
                return fallback
            }
        }
        return distinctFallbackCovers[index % distinctFallbackCovers.size]
    }

    val enabledAdminCategories = categories
        .filter { it.enabled && it.name.isNotBlank() }
        .distinctBy { it.name.trim().lowercase(Locale.ROOT) }

    if (categories.isNotEmpty()) {
        return enabledAdminCategories.mapIndexed { idx, cat ->
            val preset = FOUR_K_CATEGORIES_CATALOG.find {
                it.title.equals(cat.name, ignoreCase = true)
            }
            val matchingWallpapers = wallpapers.filter {
                it.category.equals(cat.name, ignoreCase = true) ||
                    it.tags.any { tag -> tag.equals(cat.name, ignoreCase = true) }
            }
            val candidateUrls = buildList {
                if (cat.thumbnailUrl.isNotBlank()) add(cat.thumbnailUrl)
                matchingWallpapers.forEach {
                    add(it.thumbnailUrl.ifBlank { it.imageUrl })
                }
                if (preset != null) add(preset.imageUrl)
            }
            val resolvedImage = pickUniqueCover(candidateUrls, idx)
            val countText = when {
                matchingWallpapers.isNotEmpty() -> "${matchingWallpapers.size} Wallpapers"
                cat.wallpaperCount > 0L -> "${cat.wallpaperCount}+"
                preset != null -> preset.countLabel
                else -> "New Category"
            }
            val triple = palette[idx % palette.size]
            FourKCategoryCardSpec(
                title = cat.name,
                countLabel = countText,
                imageUrl = resolvedImage,
                icon = preset?.icon ?: triple.first,
                pastelBg = preset?.pastelBg ?: triple.second,
                iconTint = preset?.iconTint ?: triple.third
            )
        }
    }

    return FOUR_K_CATEGORIES_CATALOG.distinctBy { it.title.lowercase(Locale.ROOT) }
}


val CURATED_4K_WALLPAPERS: List<WallpaperItem> = emptyList()

// =============================================================================
// SCREEN 1: PIXORA WALLPAPERS SPLASH SCREEN (Top-Left Screen in Reference Image)
// =============================================================================
// SCREEN 1: IOS 27 LIQUID GLASS 4K SPLASH SCREEN WITH MOTION GRAPHIC DESIGN
// =============================================================================

@Composable
fun FourKWallpapersSplashScreen(
    modifier: Modifier = Modifier,
    onDismissOrComplete: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splashMotionGraphics")

    // Motion Graphic 1: Dual Concentric Orbital Halo Waves
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 2.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Scale"
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Alpha"
    )
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 2.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Scale"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Alpha"
    )

    // Motion Graphic 2: Floating Sinusoidal Liquid Glass Caustic Orbs
    val orbAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbAngle"
    )
    val orbRad = Math.toRadians(orbAngle.toDouble())
    val orb1OffsetX = (kotlin.math.sin(orbRad) * 48f).toFloat()
    val orb1OffsetY = (kotlin.math.cos(orbRad) * 36f).toFloat()
    val orb2OffsetX = (-kotlin.math.sin(orbRad * 1.2) * 52f).toFloat()
    val orb2OffsetY = (kotlin.math.cos(orbRad * 1.2) * 44f).toFloat()

    // Motion Graphic 3: Specular Shimmer Sheen on Liquid Glass Logo
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerPhase"
    )

    // Motion Graphic 4: Smooth Apple-grade Rotating Progress Indicator
    val arcAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arcAngle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onDismissOrComplete() }
            .testTag("splash_screen_4k_wallpapers"),
        contentAlignment = Alignment.Center
    ) {
        // LAYER 1: Deep Luxurious Cosmic iOS Dark Glass Gradient Backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF080417),
                            Color(0xFF0F0B2E),
                            Color(0xFF130938),
                            Color(0xFF081224)
                        )
                    )
                )
        )

        // LAYER 2: Motion Graphic Animated Liquid Glass Background Ambient Caustic Orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Cyan Caustic Orb (Floating Top-Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00D9FF).copy(alpha = 0.38f),
                        Color(0xFF00D9FF).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.28f + orb1OffsetX, h * 0.32f + orb1OffsetY),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.28f + orb1OffsetX, h * 0.32f + orb1OffsetY)
            )

            // Violet Caustic Orb (Floating Bottom-Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFA855F7).copy(alpha = 0.35f),
                        Color(0xFF6366F1).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.72f + orb2OffsetX, h * 0.62f + orb2OffsetY),
                    radius = w * 0.58f
                ),
                radius = w * 0.58f,
                center = Offset(w * 0.72f + orb2OffsetX, h * 0.62f + orb2OffsetY)
            )

            // Center Ambient Gold Caustic Highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD426).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.44f),
                    radius = w * 0.38f
                ),
                radius = w * 0.38f,
                center = Offset(w * 0.5f, h * 0.44f)
            )
        }

        // LAYER 3: Main Central Liquid Glass Emblem + Motion Graphic Concentric Orbital Waves
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .staggeredBouncyEntrance(index = 0)
        ) {
            Box(
                modifier = Modifier.size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding Motion Graphic Ring 1
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .graphicsLayer {
                            scaleX = ring1Scale
                            scaleY = ring1Scale
                            alpha = ring1Alpha
                        }
                        .border(
                            width = 1.6.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFF38BDF8),
                                    Color(0xFFA855F7),
                                    Color(0xFFFFDF38),
                                    Color(0xFF38BDF8)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Expanding Motion Graphic Ring 2
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .graphicsLayer {
                            scaleX = ring2Scale
                            scaleY = ring2Scale
                            alpha = ring2Alpha
                        }
                        .border(
                            width = 1.2.dp,
                            color = Color.White.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                )

                // Central iOS 27 Liquid Glass 3D Squircle
                Box(
                    modifier = Modifier
                        .size(106.dp)
                        .subtleFloatingBounce(amplitudePx = 8f, durationMs = 2100)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.28f),
                                    Color(0xFF6366F1).copy(alpha = 0.35f),
                                    Color(0xFF0EA5E9).copy(alpha = 0.38f),
                                    Color(0xFF1E103E).copy(alpha = 0.75f)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.95f),
                                    ElectricCyan.copy(alpha = 0.85f),
                                    NeonViolet.copy(alpha = 0.85f),
                                    Color.White.copy(alpha = 0.45f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .drawBehind {
                            // Specular Light Sweep Sheen across Glass Squircle
                            val w = size.width
                            val h = size.height
                            val startX = w * (shimmerPhase - 0.3f)
                            val endX = w * (shimmerPhase + 0.3f)
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.32f),
                                        Color.Transparent
                                    ),
                                    start = Offset(startX, 0f),
                                    end = Offset(endX, h)
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(54.dp)) {
                        val w = size.width
                        val h = size.height

                        // Golden Sun Dot (Top-Left)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFDF38),
                                    Color(0xFFFF9900)
                                )
                            ),
                            radius = w * 0.10f,
                            center = Offset(w * 0.24f, h * 0.26f)
                        )

                        // Left Peak Fill
                        val leftPath = Path().apply {
                            moveTo(w * 0.14f, h * 0.78f)
                            lineTo(w * 0.32f, h * 0.46f)
                            lineTo(w * 0.46f, h * 0.78f)
                            close()
                        }
                        drawPath(
                            leftPath,
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.85f), Color(0xFF38BDF8).copy(alpha = 0.65f))
                            )
                        )

                        // Main Mountain Peak (Crystal White with Cyan Edge)
                        val mainPeak = Path().apply {
                            moveTo(w * 0.32f, h * 0.78f)
                            lineTo(w * 0.65f, h * 0.20f)
                            lineTo(w * 0.94f, h * 0.78f)
                            close()
                        }
                        drawPath(
                            path = mainPeak,
                            brush = Brush.verticalGradient(
                                listOf(Color.White, Color(0xFFC4B5FD))
                            ),
                            style = Stroke(width = 4.8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App Name with Apple SF-style Headline Typography
            Text(
                text = "Pixora Wallpapers",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    letterSpacing = (-0.8).sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // iOS Frosted Glass Pill Badge: "4K Ultra HD • Lossless Liquid Glass"
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.75f),
                                ElectricCyan.copy(alpha = 0.65f),
                                Color.White.copy(alpha = 0.40f)
                            )
                        ),
                        CircleShape
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "4K ULTRA HD • 2 TB CLOUD VAULT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(52.dp))

            // Apple iOS Style Smooth Liquid Glass Loading Arc
            Canvas(modifier = Modifier.size(32.dp)) {
                drawArc(
                    color = Color.White.copy(alpha = 0.18f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            ElectricCyan,
                            Color(0xFFA855F7),
                            Color(0xFFFFDF38),
                            ElectricCyan
                        )
                    ),
                    startAngle = arcAngle,
                    sweepAngle = 220f,
                    useCenter = false,
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

// =============================================================================
// SCREEN 11: "GO PREMIUM" MODAL / SCREEN (Bottom Row, #5 in Reference Image)
// =============================================================================

/**
 * Validates that a UPI Transaction ID / UTR is a genuine 12-digit numeric banking reference
 * (e.g., 431289450128) and rejects empty strings, placeholders, or repeating dummy digits.
 */
fun isValidRealUpiTransactionId(raw: String): Boolean {
    val cleaned = raw.trim().replace(" ", "")
    if (!cleaned.matches(Regex("^[1-9]\\d{11}$"))) return false
    if (cleaned.toSet().size < 3) return false
    if (cleaned == "123456789012" || cleaned == "987654321098") return false
    return true
}

@Composable
fun GoPremiumModalDialog(
    isPremiumUser: Boolean = false,
    customQrCodeUri: String? = null,
    startWithQrStep: Boolean = true,
    onUpdateCustomQrCode: ((Uri) -> Unit)? = null,
    onOpenPremiumUpload: (() -> Unit)? = null,
    onUpgradeClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showQrPaymentPopup by remember(startWithQrStep) { mutableStateOf(startWithQrStep) }
    var utrNote by remember { mutableStateOf("") }
    var utrErrorText by remember { mutableStateOf<String?>(null) }
    val isUtrValid = remember(utrNote) { isValidRealUpiTransactionId(utrNote) }

    val qrPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && onUpdateCustomQrCode != null) {
            onUpdateCustomQrCode(uri)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.56f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = showQrPaymentPopup,
                transitionSpec = {
                    (fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                        scaleIn(
                            initialScale = 0.90f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ))
                        .togetherWith(
                            fadeOut(tween(150)) + scaleOut(targetScale = 0.94f, animationSpec = tween(150))
                        )
                },
                label = "premiumQrConnectedTransition"
            ) { isQrStep ->
                if (isQrStep) {
                    // =========================================================================
                    // POPUP WINDOW WITH ₹299 ON TOP & USER'S REAL NAVI UPI QR CODE + REAL 12-DIGIT UTR GATE
                    // =========================================================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .ios27LiquidStretch()
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E1348).copy(alpha = 0.92f),
                                        Color(0xFF140B34).copy(alpha = 0.94f),
                                        Color(0xFF0C0622).copy(alpha = 0.96f)
                                    )
                                )
                            )
                            .border(
                                1.6.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.92f),
                                        Color(0xFFFFD426).copy(alpha = 0.72f),
                                        Color(0xFF00E5FF).copy(alpha = 0.65f)
                                    )
                                ),
                                RoundedCornerShape(32.dp)
                            )
                            .clickable(enabled = false) {}
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                            .testTag("qr_payment_299_modal")
                    ) {
                        // Top-Left Back Button & Top-Right Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .ios27LiquidStretch()
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f))
                                    .border(1.dp, Color.White.copy(alpha = 0.65f), CircleShape)
                                    .premiumSmoothClick { showQrPaymentPopup = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Top ₹299 Pill Badge
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFFFE047), Color(0xFFFF9900))
                                        )
                                    )
                                    .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.35f))
                                    .padding(horizontal = 18.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "₹299 • LIFETIME PASS",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    ),
                                    color = Color(0xFF1A1033)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .ios27LiquidStretch()
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f))
                                    .border(1.dp, Color.White.copy(alpha = 0.65f), CircleShape)
                                    .premiumSmoothClick(onClick = onDismiss),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 42.dp)
                        ) {
                            Text(
                                text = "₹299",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 38.sp,
                                    letterSpacing = (-1).sp
                                ),
                                color = Color(0xFFFFDF38)
                            )
                            Text(
                                text = "Scan Navi / UPI QR Code & Enter 12-Digit UPI Transaction ID to Unlock Premium",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.4.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White.copy(alpha = 0.90f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // QR Code Display Container (Renders User's Real Navi UPI QR Code or Custom Picked QR)
                            Box(
                                modifier = Modifier
                                    .size(226.dp)
                                    .ios27LiquidStretch()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White)
                                    .border(3.dp, Color(0xFFFFD426), RoundedCornerShape(24.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!customQrCodeUri.isNullOrBlank()) {
                                    WallxAsyncImage(
                                        imageUrl = customQrCodeUri,
                                        contentDescription = "₹299 Payment QR Code",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                } else {
                                    UpiQrCodeCanvasArtwork(
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Button allowing user to attach/replace QR image anytime
                            if (onUpdateCustomQrCode != null) {
                                Box(
                                    modifier = Modifier
                                        .ios27LiquidStretch()
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.16f))
                                        .border(1.dp, Color.White.copy(alpha = 0.42f), CircleShape)
                                        .premiumSmoothClick {
                                            qrPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode2,
                                            contentDescription = null,
                                            tint = Color(0xFFFFDF38),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (customQrCodeUri.isNullOrBlank()) {
                                                "Divyansh Official Navi UPI QR • Tap to Replace"
                                            } else {
                                                "Change Custom QR Code Image"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color(0xFFFFDF38)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // MANDATORY 12-Digit UPI Transaction ID / UTR Box (Required to unlock Premium)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "UPI Transaction ID / UTR (Required *)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.5.sp
                                        ),
                                        color = if (isUtrValid) Color(0xFF4ADE80) else Color(0xFFFFDF38)
                                    )
                                    Text(
                                        text = "${utrNote.length}/12 digits",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isUtrValid) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.72f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .ios27LiquidStretch()
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.16f))
                                        .border(
                                            width = 1.5.dp,
                                            color = when {
                                                utrErrorText != null -> Color(0xFFFB7185)
                                                isUtrValid -> Color(0xFF22C55E)
                                                else -> Color.White.copy(alpha = 0.48f)
                                            },
                                            shape = CircleShape
                                        )
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (utrNote.isEmpty()) {
                                                Text(
                                                    text = "Enter 12-digit UPI Transaction ID (e.g. 431892045712)",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.2.sp),
                                                    color = Color.White.copy(alpha = 0.62f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            BasicTextField(
                                                value = utrNote,
                                                onValueChange = { input ->
                                                    val digitsOnly = input.filter { it.isDigit() }.take(12)
                                                    utrNote = digitsOnly
                                                    if (utrErrorText != null) utrErrorText = null
                                                },
                                                singleLine = true,
                                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 1.1.sp
                                                ),
                                                cursorBrush = SolidColor(Color(0xFFFFDF38)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("upi_transaction_id_input")
                                            )
                                        }
                                        if (isUtrValid) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Valid 12-digit UTR",
                                                tint = Color(0xFF4ADE80),
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                    }
                                }

                                if (utrErrorText != null) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = utrErrorText!!,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.2.sp
                                        ),
                                        color = Color(0xFFFF8FA3),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp)
                                            .testTag("upi_transaction_id_error")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Primary CTA: Requires valid 12-digit numeric UPI Transaction ID before unlocking
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .ios27LiquidStretch()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = if (isUtrValid) {
                                                listOf(Color(0xFF22C55E), Color(0xFF10B981))
                                            } else {
                                                listOf(
                                                    Color.White.copy(alpha = 0.22f),
                                                    Color.White.copy(alpha = 0.14f)
                                                )
                                            }
                                        )
                                    )
                                    .border(
                                        1.4.dp,
                                        if (isUtrValid) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.38f),
                                        CircleShape
                                    )
                                    .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.30f))
                                    .premiumSmoothClick(pressedScale = 0.93f) {
                                        val cleanedUtr = utrNote.trim()
                                        if (isValidRealUpiTransactionId(cleanedUtr)) {
                                            utrErrorText = null
                                            onUpgradeClick(cleanedUtr)
                                        } else {
                                            utrErrorText = "Please enter a valid 12-digit UPI Transaction ID / UTR from your payment app to unlock Premium."
                                        }
                                    }
                                    .testTag("confirm_qr_payment_unlock_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isUtrValid) Icons.Default.Verified else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isUtrValid) {
                                            "Verify UTR & Unlock Premium"
                                        } else {
                                            "Enter 12-Digit Transaction ID to Unlock"
                                        },
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.5.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // =========================================================================
                    // SCREEN 11: "UNLOCK PREMIUM" PURE TRANSPARENT CLEAR LIQUID GLASS OVERVIEW MODAL
                    // =========================================================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredBouncyEntrance(index = 0)
                            .ios27LiquidStretch()
                            .clip(RoundedCornerShape(34.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.72f),
                                        Color(0xFFEFF6FF).copy(alpha = 0.62f),
                                        Color(0xFFDDD6FE).copy(alpha = 0.68f)
                                    )
                                )
                            )
                            .border(
                                1.8.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.96f),
                                        Color.White.copy(alpha = 0.55f),
                                        Color(0xFFA5F3FC).copy(alpha = 0.85f)
                                    )
                                ),
                                RoundedCornerShape(34.dp)
                            )
                            .clickable(enabled = false) {}
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                            .testTag("go_premium_modal")
                    ) {
                        // Top-Left Back/Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.65f))
                                .border(1.dp, Color.White, CircleShape)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "👑 Premium • ₹299",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.5.sp
                                ),
                                color = Color(0xFF1E1B4B)
                            )
                        }

                        // Top-Right Close Button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(34.dp)
                                .ios27LiquidStretch()
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.55f))
                                .border(1.dp, Color.White, CircleShape)
                                .premiumSmoothClick(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF1E1B4B),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                        ) {
                            // Gold 3D Crown Badge with Floating Bounce & Glowing Aura
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .subtleFloatingBounce(amplitudePx = 8f, durationMs = 1850)
                                    .drawBehind {
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFFFD426).copy(alpha = 0.50f),
                                                    Color(0xFFA855F7).copy(alpha = 0.22f),
                                                    Color.Transparent
                                                )
                                            ),
                                            radius = size.minDimension * 0.72f
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFFFFE047), Color(0xFFFF9900))
                                            )
                                        )
                                        .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.40f))
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Unlock Premium Crown",
                                        tint = Color.White,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isPremiumUser) "Premium Unlocked 👑" else "Unlock Premium",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 26.sp
                                ),
                                color = Color(0xFF0F172A)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Get unlimited access to all wallpapers and premium features.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 19.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF334155),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Feature Checkmark Rows inside Clear Liquid Glass Card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(Color.White.copy(alpha = 0.44f))
                                    .border(1.2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PremiumFeatureCheckRow("Unlimited Downloads")
                                PremiumFeatureCheckRow("4K & 8K Wallpapers")
                                PremiumFeatureCheckRow("Creator Earnings & Payouts")
                                PremiumFeatureCheckRow("Exclusive Collections & Uploads")
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Purple-to-Blue Liquid Glass Pill Button: "Upgrade to Premium →"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .ios27LiquidStretch()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF7C3AED),
                                                Color(0xFF4F46E5),
                                                Color(0xFF06B6D4)
                                            )
                                        )
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                                    .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.36f))
                                    .premiumSmoothClick(pressedScale = 0.93f) {
                                        if (isPremiumUser && onOpenPremiumUpload != null) {
                                            onDismiss()
                                            onOpenPremiumUpload()
                                        } else {
                                            showQrPaymentPopup = true
                                        }
                                    }
                                    .testTag("upgrade_to_premium_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isPremiumUser) {
                                            "Upload 4K Wallpaper Now"
                                        } else {
                                            "Upgrade to Premium"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Exact 37x37 QR Version 5 Matrix decoded from the user's provided Navi UPI QR code image
 * (`IMG_20261015_173614_077.jpg`), including the center white rounded square and
 * signature green Navi `'n'` + top-right arrow chevron logo (`#00B865`).
 */
private val USER_NAVI_QR_MATRIX_37 = listOf(
    "1111111010110000110010100100001111111",
    "1000001001011010111110011000101000001",
    "1011101001101010001101000101001011101",
    "1011101010010111111011100110001011101",
    "1011101010101011111001011111101011101",
    "1000001000001101100111011000101000001",
    "1111111010101010101010101010101111111",
    "0000000010001100101011111011000000000",
    "1110111001001000110100110110110100010",
    "1010001110111001100001011001100110101",
    "0110001010101110111100111000110111100",
    "0000111111010011010001010011011010101",
    "0011111110110011100110101101101110001",
    "0000100100011010000000001111011101100",
    "0000111001101100000000001011111001000",
    "0001110111011100000000001001010100101",
    "0111011010101100000000001111000010100",
    "0001100110000000000000001111010011110",
    "0011101001001100000000001001110100101",
    "1110001111110000000000001100101110100",
    "0101111011000100000000001010111011110",
    "1000111011000100000000001100011010011",
    "0110011111010011101000100111001111011",
    "1011101110111010110010111000101110110",
    "1100101001001000111111011001101000100",
    "1110111101011101010001111010100101001",
    "0101011011100110110010111000110001100",
    "0111101100110101011100111011011000000",
    "0011111010110111101011010001111110101",
    "0000000010010011110101001010100010110",
    "1111111010010111110111010001101110000",
    "1000001011011001101101100110100010010",
    "1011101010101000101111011101111110100",
    "1011101001111110110101010110001101111",
    "1011101000100011011000100100110101111",
    "1000001011101000111001100010101111111",
    "1111111000000100000111111010001100100"
)

@Composable
private fun UpiQrCodeCanvasArtwork(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(2.dp)
            .testTag("user_navi_upi_qr_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.payment_qr_code),
            contentDescription = "Official Navi UPI Payment QR Code",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
        )
    }
}

/**
 * Pop-Up Window: "Earn Money by Uploading Wallpapers 💸"
 * Shows transparent liquid glass breakdown of earnings:
 * ₹25/upload, ₹2.50/download, ₹50 bonus, 2 TB cloud storage, and instant UPI payouts.
 */
@Composable
fun EarnMoneyAndShareStartupPopup(
    isPremiumUser: Boolean = false,
    onUploadOrUpgradeClick: () -> Unit = {},
    onShareAppClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.68f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 18.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredBouncyEntrance(index = 0)
                    .ios27LiquidStretch()
                    .clip(RoundedCornerShape(38.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E0E44).copy(alpha = 0.98f),
                                Color(0xFF11072C).copy(alpha = 0.98f),
                                Color(0xFF09041A).copy(alpha = 0.99f)
                            )
                        )
                    )
                    .border(
                        width = 1.6.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD426),
                                Color.White.copy(alpha = 0.90f),
                                ElectricCyan.copy(alpha = 0.75f),
                                Color(0xFF10B981)
                            )
                        ),
                        shape = RoundedCornerShape(38.dp)
                    )
                    .clickable(enabled = false) {}
                    .padding(horizontal = 20.dp, vertical = 22.dp)
                    .testTag("startup_earn_money_popup")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Pill Bar: Creator Earnings Badge + Close Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                                    )
                                )
                                .border(1.dp, Color(0xFFFFD426), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "💸 EARN MONEY • CREATOR PROGRAM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.5.sp,
                                    letterSpacing = 0.6.sp
                                ),
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .premiumSmoothClick(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Floating 3D Money & Sparkle Emblem
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .subtleFloatingBounce(amplitudePx = 6f, durationMs = 1800)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF10B981), Color(0xFF6366F1), Color(0xFFFFD426))
                                )
                            )
                            .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.40f))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💸",
                            fontSize = 34.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Earn Money by Uploading Wallpapers 💸",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp,
                            lineHeight = 26.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Publish your 4K & 8K wallpapers to the 2 TB Cloud Vault and get real cash rewards credited straight to your UPI balance!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color.White.copy(alpha = 0.88f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Creator Earning Highlights Matrix
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$1.00",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                ),
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "3K Views 👁️",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.90f)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFF34D399).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$1.00",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                ),
                                color = Color(0xFFFFDF38)
                            )
                            Text(
                                text = "5K Uploads 📤",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.90f)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFFF472B6).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$2.00",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                ),
                                color = Color(0xFF34D399)
                            )
                            Text(
                                text = "1K Downloads ⬇️",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.90f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // UPI Instant Payouts Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F261D))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instant UPI Withdrawals to GPay, PhonePe, Paytm",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = Color(0xFF6EE7B7)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Action Button: Upload & Start Earning
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFDF38), Color(0xFFFF9900))
                                )
                            )
                            .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.38f))
                            .premiumSmoothClick(pressedScale = 0.92f) {
                                onUploadOrUpgradeClick()
                            }
                            .testTag("startup_popup_upgrade_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPremiumUser) Icons.Default.CloudUpload else Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFF170B3B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPremiumUser) "Upload 4K Wallpaper Now (+₹25) 📤" else "Upload & Start Earning (₹299 VIP) 💸",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp
                                ),
                                color = Color(0xFF170B3B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Pill Button: Share App to Everyone 🚀
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(PrimaryPurple, SecondaryBlue)
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                            .premiumSmoothClick(pressedScale = 0.92f) {
                                onShareAppClick()
                            }
                            .testTag("startup_popup_share_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share App to Friends & Family 🚀",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dismiss Pill Button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                            .premiumSmoothClick(onClick = onDismiss)
                            .padding(horizontal = 20.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Explore Wallpapers First →",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Developer / Hacker About App Pill Dialog:
 * Displays Developer Avatar, Name (Divyansh), Role (Developer / Hacker),
 * YouTube Channel, Telegram Channel, and Technical Specs in full Pill Geometry.
 */
@Composable
fun DeveloperAboutAppPillDialog(
    onDismiss: () -> Unit,
    onShareApp: () -> Unit = {}
) {
    val context = LocalContext.current
    var customDevPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val devPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customDevPhotoUri = uri
        }
    }

    val youtubeUrl = "https://youtube.com/@nkphonk1?si=LdwDyXDIgKK6JWiW"
    val telegramUrl = "https://t.me/SUPPER_SAIYAN_PHONK_NK"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.68f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredBouncyEntrance(index = 0)
                    .ios27LiquidStretch()
                    .clip(RoundedCornerShape(42.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E0B4C).copy(alpha = 0.97f),
                                Color(0xFF10072B).copy(alpha = 0.98f),
                                Color(0xFF070417).copy(alpha = 0.99f)
                            )
                        )
                    )
                    .border(
                        width = 1.6.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.85f),
                                ElectricCyan.copy(alpha = 0.78f),
                                NeonViolet.copy(alpha = 0.78f),
                                Color(0xFFFFDF38).copy(alpha = 0.65f)
                            )
                        ),
                        shape = RoundedCornerShape(42.dp)
                    )
                    .clickable(enabled = false) {}
                    .padding(horizontal = 20.dp, vertical = 22.dp)
                    .testTag("developer_about_app_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Pill Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(PrimaryPurple, SecondaryBlue)
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ABOUT DEVELOPER & APP",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.5.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .premiumSmoothClick(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close About App",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Developer / Hacker Avatar Circle with Neon Cyber Halo & Tap-to-Change Photo
                    Box(
                        modifier = Modifier
                            .size(106.dp)
                            .subtleFloatingBounce(amplitudePx = 6f, durationMs = 1900)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(ElectricCyan, PrimaryPurple, Color(0xFFEC4899))
                                )
                            )
                            .border(2.5.dp, Color.White, CircleShape)
                            .premiumSmoothClick {
                                devPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        WallxAsyncImage(
                            imageUrl = customDevPhotoUri?.toString()
                                ?: "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?fm=jpg&fit=crop&w=900&q=85",
                            contentDescription = "Divyansh Developer Hacker Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF090418).copy(alpha = 0.55f)
                                        )
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF059669))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DEV / HACKER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 7.5.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Developer Name Pill Header
                    Text(
                        text = "Divyansh",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Who :- Developer / Hacker Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF059669), Color(0xFF0EA5E9), PrimaryPurple)
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Who :- Developer / Hacker",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Developer Details Pill Cards
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Detail Pill 1: Name
                        DeveloperInfoPillRow(
                            label = "Name :-",
                            value = "Divyansh",
                            accentColor = Color(0xFFFFDF38)
                        )

                        // Detail Pill 2: Who
                        DeveloperInfoPillRow(
                            label = "Who :-",
                            value = "Developer / Hacker",
                            accentColor = ElectricCyan
                        )

                        // Detail Pill 3: Creator / Music Alias
                        DeveloperInfoPillRow(
                            label = "Creator Alias :-",
                            value = "NK PHONK • Super Saiyan Phonk",
                            accentColor = Color(0xFFF472B6)
                        )

                        // Detail Pill 4: Specialization
                        DeveloperInfoPillRow(
                            label = "Skills :-",
                            value = "Android Architect • Cybersecurity • 4K/8K Engine",
                            accentColor = Color(0xFF34D399)
                        )

                        // Detail Pill 5: App Engine
                        DeveloperInfoPillRow(
                            label = "Engine :-",
                            value = "Pixora 4K v2.5 • 2 TB Vault • 120Hz Liquid Glass",
                            accentColor = Color(0xFFA78BFA)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Official YouTube Channel Pill Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFDC2626), Color(0xFF991B1B))
                                )
                            )
                            .border(1.2.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                            .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.28f))
                            .premiumSmoothClick(pressedScale = 0.93f) {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
                                    )
                                } catch (_: Exception) {
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("developer_youtube_pill_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.20f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "YouTube Channel",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "YouTube Channel • @nkphonk1",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.5.sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = youtubeUrl,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Official Telegram Channel Pill Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF0284C7), Color(0xFF2563EB))
                                )
                            )
                            .border(1.2.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                            .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.28f))
                            .premiumSmoothClick(pressedScale = 0.93f) {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
                                    )
                                } catch (_: Exception) {
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("developer_telegram_pill_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.20f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Telegram Channel",
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Telegram • SUPPER_SAIYAN_PHONK_NK",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = telegramUrl,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Share App & Close Pill Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .border(1.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                                .premiumSmoothClick { onShareApp() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share App 🚀",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(PrimaryPurple, SecondaryBlue)
                                    )
                                )
                                .premiumSmoothClick(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Done ✓",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeveloperInfoPillRow(
    label: String,
    value: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.09f))
            .border(1.dp, accentColor.copy(alpha = 0.42f), CircleShape)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            ),
            color = accentColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.2.sp
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


/**
 * Premium-Only Wallpaper Upload Dialog:
 * Allows users who have unlocked Premium (₹299 QR Code) to upload their own 4K wallpapers.
 */
@Composable
fun PremiumUploadWallpaperDialog(
    isUploading: Boolean = false,
    availableCategories: List<String> = emptyList(),
    onUploadWallpaper: (Uri, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    val categories = remember(availableCategories) {
        val defaults = listOf("Anime", "Nature", "Cars", "Gaming", "Abstract", "Minimal", "4K Ultra HD", "Aesthetic")
        (availableCategories.filter { it.isNotBlank() && !it.equals("All", ignoreCase = true) } + defaults)
            .distinctBy { it.lowercase(Locale.ROOT) }
    }
    var selectedCategory by remember(categories) { mutableStateOf(categories.firstOrNull() ?: "Anime") }
    var description by remember { mutableStateOf("") }
    var autoPublishOnPick by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            if (autoPublishOnPick) {
                autoPublishOnPick = false
                onUploadWallpaper(uri, title, selectedCategory, description)
                onDismiss()
            }
        } else {
            autoPublishOnPick = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.62f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredBouncyEntrance(index = 0)
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (glass.isDark) Color(0xFF151B2E) else Color.White)
                    .border(
                        1.5.dp,
                        PrimaryPurple.copy(alpha = 0.45f),
                        RoundedCornerShape(28.dp)
                    )
                    .clickable(enabled = false) {}
                    .padding(22.dp)
                    .testTag("premium_upload_wallpaper_dialog"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Upload 4K Wallpaper (2 TB Vault)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                ),
                                color = if (glass.isDark) Color.White else Color(0xFF141229)
                            )
                            Text(
                                text = "Premium Creator Exclusive 👑 • 2 TB (2,048 GB) Storage Ready",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (glass.isDark) Color(0xFF222A45) else Color(0xFFF1EFFC))
                            .premiumSmoothClick(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (glass.isDark) Color.White else Color(0xFF141229),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Image Picker Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (glass.isDark) Color(0xFF1E2640) else Color(0xFFF3F0FF))
                        .border(
                            1.5.dp,
                            PrimaryPurple.copy(alpha = 0.35f),
                            RoundedCornerShape(20.dp)
                        )
                        .premiumSmoothClick {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val picked = selectedImageUri
                    if (picked != null) {
                        WallxAsyncImage(
                            imageUrl = picked.toString(),
                            contentDescription = "Selected 4K Wallpaper",
                            contentScale = ContentScale.Crop,
                            isFullQualityInspection = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Select 4K Image",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap to Select 4K Wallpaper Image",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryPurple
                            )
                        }
                    }
                }

                // Title Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (glass.isDark) Color(0xFF1E2640) else Color(0xFFF3F0FF))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (title.isEmpty()) {
                        Text(
                            text = "Wallpaper Title (e.g., Cyber Neon Samurai)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF7C7999)
                        )
                    }
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = if (glass.isDark) Color.White else Color(0xFF141229),
                            fontWeight = FontWeight.SemiBold
                        ),
                        cursorBrush = SolidColor(PrimaryPurple),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Category Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories, key = { "upload_cat_$it" }) { cat ->
                        val isSel = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSel) PrimaryPurple
                                    else if (glass.isDark) Color(0xFF222A45)
                                    else Color(0xFFECE9FA)
                                )
                                .premiumSmoothClick { selectedCategory = cat }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSel) Color.White else if (glass.isDark) Color.White else Color(0xFF141229)
                            )
                        }
                    }
                }

                // Description Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (glass.isDark) Color(0xFF1E2640) else Color(0xFFF3F0FF))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (description.isEmpty()) {
                        Text(
                            text = "Short description (Optional)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF7C7999)
                        )
                    }
                    BasicTextField(
                        value = description,
                        onValueChange = { description = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = if (glass.isDark) Color.White else Color(0xFF141229)
                        ),
                        cursorBrush = SolidColor(PrimaryPurple),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Submit Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(PrimaryPurple, SecondaryBlue)
                            )
                        )
                        .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.28f))
                        .premiumSmoothClick(pressedScale = 0.93f) {
                            val uri = selectedImageUri
                            if (uri != null) {
                                onUploadWallpaper(uri, title, selectedCategory, description)
                                onDismiss()
                            } else {
                                autoPublishOnPick = true
                                imagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        }
                        .testTag("publish_premium_wallpaper_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedImageUri == null) {
                            "Select Image to Upload"
                        } else if (isUploading) {
                            "Uploading 4K Wallpaper..."
                        } else {
                            "Publish to Recently Added"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumFeatureCheckRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFF9333EA)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.8.sp
            ),
            color = Color(0xFF1E1B4B)
        )
    }
}

// =============================================================================
// SCREEN 2: HOME SCREEN ("4K Wallpapers", Recently Added Top Carousel,
//           "Nature Beauty", 4 Pastel Categories, "Trending Wallpapers" Grid)
// =============================================================================

@Composable
fun UserHomeScreen(
    wallpapers: List<WallpaperItem>,
    recentlyAddedWallpapers: List<WallpaperItem> = emptyList(),
    userUploadedWallpapers: List<WallpaperItem> = emptyList(),
    categories: List<CategoryModel>,
    selectedCategory: String,
    favoriteIds: Set<String>,
    appSettings: AppSettingsModel,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    isPremiumUser: Boolean = false,
    isAuthorizedAdmin: Boolean = false,
    onOpenAdminPanel: () -> Unit = {},
    customQrCodeUri: String? = null,
    onUnlockPremiumViaQr: (String) -> Unit = {},
    onUpdateCustomQrCode: (Uri) -> Unit = {},
    onUploadPremiumWallpaper: (Uri, String, String, String) -> Unit = { _, _, _, _ -> },
    onSelectCategory: (String) -> Unit,
    onSelectWallpaper: (WallpaperItem) -> Unit,
    onToggleFavorite: (WallpaperItem) -> Unit,
    onQuickSearchNavigate: () -> Unit,
    onOpenMenuDrawer: () -> Unit = {},
    onOpenProfile: () -> Unit = { onOpenMenuDrawer() },
    onOpenRingtonesStudio: () -> Unit = {},
    onOpenArtworksScreen: (String) -> Unit = { onQuickSearchNavigate() },
    onNotifyRingtone: (String) -> Unit = {},
    isCatalogLoading: Boolean = false,
    catalogSyncError: String? = null,
    onRefreshCatalog: () -> Unit = {},
    userName: String = "Divyansh",
    userEmail: String = "d99312907@gmail.com"
) {
    val glass = LocalLiquidGlassColors.current
    var showGoPremiumModal by remember { mutableStateOf(false) }
    var openPremiumModalAtQrStep by remember { mutableStateOf(false) }
    var showPremiumUploadDialog by remember { mutableStateOf(false) }
    var homeSearchQuery by remember(searchQuery) { mutableStateOf(searchQuery) }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }

    val pageBg = Color.Transparent
    val textPrimary = if (glass.isDark) Color.White else Color(0xFF0B0F19)
    val textSecondary = if (glass.isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    val mergedWallpapers = remember(userUploadedWallpapers, wallpapers) {
        (userUploadedWallpapers + wallpapers)
            .filter { it.status.equals("published", ignoreCase = true) || it.status.isBlank() }
            .deduplicateWallpapers()
    }

    // Build Featured Hero Carousel directly from uploaded & featured wallpapers without overwriting uploaded image URLs
    val heroCarouselItems = remember(mergedWallpapers, userUploadedWallpapers, recentlyAddedWallpapers) {
        val featuredPool = (userUploadedWallpapers + recentlyAddedWallpapers + mergedWallpapers.filter { it.isFeatured } + mergedWallpapers)
            .deduplicateWallpapers()
            .take(8)
        if (featuredPool.isEmpty()) {
            emptyList()
        } else {
            val primaryHero = featuredPool.first().let { first ->
                first.copy(
                    title = first.title.ifBlank { "Featured 4K" }
                )
            }
            val others = featuredPool.drop(1)
            if (others.isEmpty()) {
                listOf(primaryHero)
            } else {
                val leftCard = others.getOrNull(0) ?: primaryHero
                val rightCard = others.getOrNull(1) ?: primaryHero
                val extraCards = others.drop(2).take(5)
                listOf(leftCard, primaryHero, rightCard) + extraCards
            }
        }
    }

    val dynamicCategorySpecs = remember(categories, mergedWallpapers) {
        buildDynamicCategorySpecs(categories, mergedWallpapers)
    }

    val isSearchActive = homeSearchQuery.isNotBlank() || !selectedTagFilter.isNullOrBlank()
    val isDefaultUnfilteredHome = (selectedCategory == "All" || selectedCategory.isBlank()) && !isSearchActive

    val trendingWallpapers = remember(
        mergedWallpapers,
        selectedCategory,
        homeSearchQuery,
        selectedTagFilter,
        isDefaultUnfilteredHome
    ) {
        val categoryFiltered = if (selectedCategory == "All" || selectedCategory.isBlank()) {
            mergedWallpapers
        } else {
            mergedWallpapers.filter {
                it.category.equals(selectedCategory, ignoreCase = true) ||
                    (selectedCategory.equals("Amoled", ignoreCase = true) && (it.category.contains("Dark", ignoreCase = true) || it.category.contains("Amoled", ignoreCase = true) || it.tags.any { t -> t.contains("amoled", ignoreCase = true) || t.contains("dark", ignoreCase = true) })) ||
                    (selectedCategory.equals("Animals", ignoreCase = true) && (it.category.contains("Animal", ignoreCase = true) || it.category.contains("Nature", ignoreCase = true) || it.tags.any { t -> t.contains("animal", ignoreCase = true) || t.contains("nature", ignoreCase = true) })) ||
                    (selectedCategory.equals("Abstract", ignoreCase = true) && (it.category.contains("Abstract", ignoreCase = true) || it.category.contains("Minimal", ignoreCase = true) || it.tags.any { t -> t.contains("abstract", ignoreCase = true) })) ||
                    (selectedCategory.equals("Cars", ignoreCase = true) && it.category.equals("Supercars", ignoreCase = true)) ||
                    it.tags.any { tag -> tag.equals(selectedCategory, ignoreCase = true) }
            }.ifEmpty { mergedWallpapers }
        }

        val tagFiltered = if (selectedTagFilter.isNullOrBlank()) {
            categoryFiltered
        } else {
            val tagQuery = selectedTagFilter!!.removePrefix("#").trim()
            categoryFiltered.filter { wall ->
                wall.tags.any { it.equals(tagQuery, ignoreCase = true) || it.contains(tagQuery, ignoreCase = true) } ||
                    wall.category.equals(tagQuery, ignoreCase = true) ||
                    wall.title.contains(tagQuery, ignoreCase = true)
            }
        }

        val rawQuery = homeSearchQuery.trim().removePrefix("#")
        val searchFiltered = if (rawQuery.isBlank()) {
            tagFiltered
        } else {
            val queryTokens = rawQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
            tagFiltered.filter { wall ->
                queryTokens.all { token ->
                    wall.title.contains(token, ignoreCase = true) ||
                        wall.category.contains(token, ignoreCase = true) ||
                        wall.description.contains(token, ignoreCase = true) ||
                        wall.resolution.contains(token, ignoreCase = true) ||
                        wall.tags.any { tag -> tag.contains(token, ignoreCase = true) }
                }
            }
        }.deduplicateWallpapers()

        if (isDefaultUnfilteredHome) {
            searchFiltered.sortedWith(
                compareByDescending<WallpaperItem> {
                    it.id.startsWith("wall_admin_") || it.id.startsWith("prem_upload_")
                }
                    .thenByDescending { it.isTrending }
                    .thenByDescending { it.downloadCount }
            )
        } else {
            searchFiltered
        }
    }

    // Build Category Row directly from Admin's enabled categories (sorted by Admin order) so every Admin category edit/disable/delete reflects immediately
    val lagguyCategoryPills = remember(categories, dynamicCategorySpecs) {
        val enabledOrdered = categories
            .filter { it.enabled && it.name.isNotBlank() }
            .sortedBy { it.order }
        val dynamicPills = if (enabledOrdered.isNotEmpty()) {
            enabledOrdered.map { cat ->
                val specIcon = dynamicCategorySpecs.firstOrNull {
                    it.title.equals(cat.name, ignoreCase = true)
                }?.icon ?: Icons.Default.AutoAwesome
                cat.name to specIcon
            }
        } else {
            dynamicCategorySpecs.map { it.title to it.icon }
        }
        listOf("All" to null) + dynamicPills.distinctBy { it.first.lowercase(Locale.ROOT) }
    }

    val pagerState = rememberPagerState(
        initialPage = if (heroCarouselItems.size >= 2) 1 else 0,
        pageCount = { heroCarouselItems.size.coerceAtLeast(1) }
    )

    val appDesign = LocalAppDesignStyle.current
    val activeGridColumns = appDesign.gridColumns.coerceIn(1, 3)
    val designAccent = remember(appDesign.recommendedOptics.tintHex) {
        runCatching {
            Color(android.graphics.Color.parseColor(appDesign.recommendedOptics.tintHex))
        }.getOrDefault(Color(0xFF38BDF8))
    }

    val isGlassPillHeader = appDesign.headerStyle == "glass_pill"
    val isCenteredHeader = appDesign.headerStyle == "centered_editorial"
    val headerTitle = remember(appSettings.appName) {
        val raw = appSettings.appName.trim()
        if (raw.isNotBlank() && raw != "PIXORA 4K" && raw != "WallX 4K" && raw != "4K Wallpapers") {
            raw
        } else {
            "Pixora"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        LiquidGlassScreenWithUpperBlur(
            headerExtraHeight = 76.dp,
            headerClearanceDp = if (isGlassPillHeader) 72.dp else 62.dp,
            headerContent = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .then(
                            if (isGlassPillHeader) {
                                Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (glass.isDark) Color.White.copy(alpha = 0.12f)
                                        else Color.White.copy(alpha = 0.78f)
                                    )
                                    .border(
                                        1.dp,
                                        designAccent.copy(alpha = 0.35f),
                                        CircleShape
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            } else {
                                Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
                            }
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .ios27LiquidStretch()
                                .clickable(onClick = onOpenMenuDrawer)
                                .testTag("home_hamburger_menu_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Menu",
                                tint = textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            if (isCenteredHeader) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(designAccent)
                                )
                            }
                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = if (isCenteredHeader) 27.sp else 25.sp,
                                    letterSpacing = (-0.6).sp
                                ),
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Hi ${userName.ifBlank { "Divyansh" }}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                ),
                                color = designAccent
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Direct Admin Panel Switch Pill when signed in as d99312907@gmail.com
                        if (isAuthorizedAdmin) {
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0B0F19))
                                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                    .ios27LiquidStretch()
                                    .premiumSmoothClick(pressedScale = 0.90f, onClick = onOpenAdminPanel)
                                    .padding(horizontal = 12.dp)
                                    .testTag("home_open_admin_panel_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = "Open Admin Panel",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Admin",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.5.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // 1) Premium Icon Button (Hidden automatically once user unlocks Premium)
                        if (!isPremiumUser) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0B0F19))
                                    .border(1.dp, Color(0xFFFFD426).copy(alpha = 0.65f), CircleShape)
                                    .ios27LiquidStretch()
                                    .premiumSmoothClick(pressedScale = 0.88f) {
                                        openPremiumModalAtQrStep = false
                                        showGoPremiumModal = true
                                    }
                                    .testTag("home_upload_wallpaper_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Open Premium",
                                    tint = Color(0xFFFFD426),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 2) Circular Avatar / Profile Button -> Opens Profile screen directly
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFE2E8F0),
                                            Color(0xFFCBD5E1)
                                        )
                                    )
                                )
                                .border(1.dp, Color.White, CircleShape)
                                .ios27LiquidStretch()
                                .premiumSmoothClick(pressedScale = 0.88f, onClick = onOpenProfile)
                                .testTag("home_profile_avatar_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Open Profile",
                                tint = Color(0xFF0B0F19),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        ) { topHeaderClearance ->
        // PORTRAIT GRID DRIVEN BY ADMIN USER LAYOUT STRUCTURE BEHIND PURE LIQUID GLASS DOCK & UPPER BLUR
        LazyVerticalGrid(
            columns = GridCells.Fixed(activeGridColumns),
            modifier = Modifier
                .fillMaxSize()
                .testTag("user_home_grid"),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = topHeaderClearance,
                bottom = 176.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(if (activeGridColumns == 3) 9.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (activeGridColumns == 3) 10.dp else 12.dp)
        ) {
            // 1. DYNAMIC HERO + CATEGORY ROW
            item(span = { GridItemSpan(maxLineSpan) }, key = "lagguy_header_and_hero") {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // Admin live announcement banner if enabled
                    if (appSettings.announcementEnabled && appSettings.announcementText.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.75f))
                                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("home_admin_announcement_banner"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF0B0F19),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = appSettings.announcementText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = textPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Active Search Bar if user is filtering by search query
                    if (isSearchActive) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .height(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = homeSearchQuery,
                                onValueChange = { updated ->
                                    homeSearchQuery = updated
                                    onSearchQueryChange(updated)
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = textSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        homeSearchQuery = ""
                                        selectedTagFilter = null
                                        onSearchQueryChange("")
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // =====================================================================
                    // DYNAMIC HERO SECTION DRIVEN BY ADMIN USER LAYOUT STYLE
                    // =====================================================================
                    if (appSettings.showFeaturedSection && heroCarouselItems.isNotEmpty() && appDesign.heroStyle != "minimal_header") {
                        when (appDesign.heroStyle) {
                            "compact_banner" -> {
                                val heroItem = heroCarouselItems.first()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(132.dp)
                                        .clip(RoundedCornerShape(appDesign.cardCornerDp.coerceIn(18, 30).dp))
                                        .border(1.2.dp, designAccent.copy(alpha = 0.55f), RoundedCornerShape(appDesign.cardCornerDp.coerceIn(18, 30).dp))
                                        .premiumSmoothClick(pressedScale = 0.98f) { onSelectWallpaper(heroItem) }
                                        .testTag("home_nature_beauty_hero_card")
                                ) {
                                    WallxAsyncImage(
                                        imageUrl = heroItem.imageUrl.ifBlank { heroItem.thumbnailUrl },
                                        fallbackImageUrl = heroItem.thumbnailUrl,
                                        contentDescription = heroItem.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.68f),
                                                        Color.Black.copy(alpha = 0.18f)
                                                    )
                                                )
                                            )
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 18.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "FEATURED CURATION",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 1.2.sp
                                                ),
                                                color = designAccent
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = heroItem.title.ifBlank { "Featured 4K" },
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = heroItem.category.ifBlank { "4K Ultra HD" },
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White.copy(alpha = 0.78f)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(designAccent.copy(alpha = 0.90f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            "full_bleed", "spotlight" -> {
                                val heroItem = heroCarouselItems.first()
                                val isSpotlight = appDesign.heroStyle == "spotlight"
                                val heroCorner = appDesign.cardCornerDp.coerceIn(20, 32).dp
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isSpotlight) 236.dp else 278.dp)
                                        .clip(RoundedCornerShape(heroCorner))
                                        .border(
                                            width = if (isSpotlight) 1.6.dp else 1.dp,
                                            color = if (isSpotlight) designAccent.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.45f),
                                            shape = RoundedCornerShape(heroCorner)
                                        )
                                        .premiumSmoothClick(pressedScale = 0.97f) { onSelectWallpaper(heroItem) }
                                        .testTag("home_nature_beauty_hero_card")
                                ) {
                                    WallxAsyncImage(
                                        imageUrl = heroItem.imageUrl.ifBlank { heroItem.thumbnailUrl },
                                        fallbackImageUrl = heroItem.thumbnailUrl,
                                        contentDescription = heroItem.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.22f),
                                                        Color.Black.copy(alpha = 0.72f)
                                                    )
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(18.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(designAccent.copy(alpha = 0.85f))
                                                .padding(horizontal = 10.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (isSpotlight) "SPOTLIGHT EDITION" else "IMAX 8K FEATURED",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp
                                                ),
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = heroItem.title.ifBlank { "Featured 4K" },
                                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            else -> {
                                HorizontalPager(
                                    state = pagerState,
                                    contentPadding = PaddingValues(horizontal = 22.dp),
                                    pageSpacing = 12.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(252.dp)
                                        .testTag("home_nature_beauty_hero_card")
                                ) { page ->
                                    val heroItem = heroCarouselItems[page % heroCarouselItems.size]
                                    val centerTitle = heroItem.title.ifBlank { "Featured 4K" }
                                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                                        .let { kotlin.math.abs(it).coerceIn(0f, 1f) }
                                    val liquidHeroScale = 1f - (pageOffset * 0.065f)
                                    val liquidHeroAlpha = 1f - (pageOffset * 0.18f)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                scaleX = liquidHeroScale
                                                scaleY = liquidHeroScale
                                                alpha = liquidHeroAlpha
                                                translationY = pageOffset * 10f
                                            }
                                            .clip(RoundedCornerShape(30.dp))
                                            .border(
                                                width = 1.dp,
                                                color = Color.White.copy(alpha = 0.65f),
                                                shape = RoundedCornerShape(30.dp)
                                            )
                                            .premiumSmoothClick(pressedScale = 0.96f) {
                                                onSelectWallpaper(heroItem)
                                            }
                                    ) {
                                        WallxAsyncImage(
                                            imageUrl = heroItem.imageUrl.ifBlank { heroItem.thumbnailUrl },
                                            fallbackImageUrl = heroItem.thumbnailUrl,
                                            contentDescription = centerTitle,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Black.copy(alpha = 0.08f),
                                                            Color.Black.copy(alpha = 0.24f),
                                                            Color.Black.copy(alpha = 0.34f)
                                                        )
                                                    )
                                                )
                                        )

                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .padding(horizontal = 20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.38f))
                                                    .border(1.2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = centerTitle,
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 22.sp,
                                                    letterSpacing = (-0.3).sp
                                                ),
                                                color = Color.White,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (appSettings.showCategoriesSection) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_tag_filter_row"),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(
                                items = lagguyCategoryPills,
                                key = { "lagguy_cat_${it.first}" }
                            ) { (catName, catIcon) ->
                                val activeCat = selectedCategory.ifBlank { "All" }
                                val isSelected = activeCat.equals(catName, ignoreCase = true)
                                val pillBgColor by animateColorAsState(
                                    targetValue = if (isSelected) Color(0xFF0B0F19) else Color.Transparent,
                                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                                    label = "catPillBg_$catName"
                                )
                                val pillHorizPad by animateDpAsState(
                                    targetValue = if (isSelected) 18.dp else 11.dp,
                                    animationSpec = spring(
                                        dampingRatio = 0.66f,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    label = "catPillPad_$catName"
                                )
                                val pillScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.04f else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.62f,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    label = "catPillScale_$catName"
                                )

                                Box(
                                    modifier = Modifier
                                        .animateItem()
                                        .height(36.dp)
                                        .graphicsLayer {
                                            scaleX = pillScale
                                            scaleY = pillScale
                                        }
                                        .clip(CircleShape)
                                        .background(pillBgColor)
                                        .premiumSmoothClick(pressedScale = 0.90f) {
                                            onSelectCategory(catName)
                                        }
                                        .padding(
                                            horizontal = pillHorizPad,
                                            vertical = 6.dp
                                        )
                                        .testTag("home_category_tile_${catName.lowercase(Locale.ROOT)}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = catName,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                letterSpacing = (-0.2).sp
                                            ),
                                            color = if (isSelected) Color.White else textPrimary
                                        )
                                        if (catIcon != null) {
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Icon(
                                                imageVector = catIcon,
                                                contentDescription = null,
                                                tint = if (isSelected) {
                                                    Color.White
                                                } else if (catName.equals("Abstract", ignoreCase = true)) {
                                                    Color(0xFF6366F1)
                                                } else {
                                                    textPrimary.copy(alpha = 0.85f)
                                                },
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // 2. 3-COLUMN TALL PORTRAIT WALLPAPER CARDS
            if (trendingWallpapers.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "home_search_empty_state") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp, horizontal = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = if (glass.isDark) 0.10f else 0.92f))
                            .border(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.55f), RoundedCornerShape(24.dp))
                            .padding(24.dp)
                            .testTag("home_search_empty_state"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (mergedWallpapers.isEmpty() && isCatalogLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                strokeWidth = 2.8.dp,
                                color = if (glass.isDark) Color.White else Color(0xFF0B0F19)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading wallpapers from Cloud Firestore...",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = textPrimary,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = if (mergedWallpapers.isEmpty()) "No uploaded wallpapers yet" else "No matching wallpapers found",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = textPrimary
                            )
                            if (mergedWallpapers.isEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val friendlySyncMsg = catalogSyncError?.let { rawErr ->
                                    if (rawErr.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                                        rawErr.contains("Quota limit exceeded", ignoreCase = true)
                                    ) {
                                        "Cloud daily read limit reached. Cached & uploaded wallpapers remain available — tap Refresh later."
                                    } else {
                                        rawErr
                                    }
                                }?.takeIf { it.isNotBlank() }
                                    ?: "Only wallpapers uploaded from Admin Panel appear here live."
                                Text(
                                    text = friendlySyncMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0B0F19))
                                            .premiumSmoothClick(onClick = onRefreshCatalog)
                                            .padding(horizontal = 18.dp)
                                            .testTag("home_refresh_catalog_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Refresh Wallpapers",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Refresh Wallpapers",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }
                                    if (isAuthorizedAdmin) {
                                        Box(
                                            modifier = Modifier
                                                .height(38.dp)
                                                .clip(CircleShape)
                                                .background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryBlue)))
                                                .premiumSmoothClick(onClick = onOpenAdminPanel)
                                                .padding(horizontal = 16.dp)
                                                .testTag("home_empty_admin_upload_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CloudUpload,
                                                    contentDescription = "Upload in Admin",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Upload",
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0B0F19))
                                        .premiumSmoothClick {
                                            homeSearchQuery = ""
                                            selectedTagFilter = null
                                            onSearchQueryChange("")
                                            onSelectCategory("All")
                                        }
                                        .padding(horizontal = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Show All Wallpapers",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = trendingWallpapers.distinctBy { it.id },
                    key = { _, it -> "home_trending_${it.id}" }
                ) { idx, item ->
                    val dynamicHeight = remember(appDesign.id, idx) {
                        when {
                            appDesign.layoutMode == "bento" -> if (idx % 3 == 0) 288.dp else 218.dp
                            appDesign.gridColumns == 1 -> appDesign.cardHeightDp.coerceAtLeast(290).dp
                            appDesign.gridColumns == 2 -> appDesign.cardHeightDp.dp
                            else -> appDesign.cardHeightDp.coerceIn(176, 224).dp
                        }
                    }
                    FourKPortraitWallpaperCard(
                        wallpaper = item,
                        isFavorited = item.id in favoriteIds,
                        staggerIndex = idx,
                        height = dynamicHeight,
                        cornerDp = appDesign.cardCornerDp.dp,
                        overlayStyle = appDesign.cardOverlayStyle,
                        accentColor = designAccent,
                        onClick = { onSelectWallpaper(item) },
                        onToggleFavorite = { onToggleFavorite(item) },
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                            placementSpec = spring(
                                dampingRatio = 0.74f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            fadeOutSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                        )
                    )
                }
            }
        }
        }

        if (showGoPremiumModal && !isPremiumUser) {
            GoPremiumModalDialog(
                isPremiumUser = false,
                customQrCodeUri = customQrCodeUri,
                startWithQrStep = openPremiumModalAtQrStep,
                onUpdateCustomQrCode = onUpdateCustomQrCode,
                onOpenPremiumUpload = { showPremiumUploadDialog = true },
                onUpgradeClick = { verifiedUtr ->
                    showGoPremiumModal = false
                    onUnlockPremiumViaQr(verifiedUtr)
                },
                onDismiss = { showGoPremiumModal = false }
            )
        }

        if (showPremiumUploadDialog) {
            PremiumUploadWallpaperDialog(
                availableCategories = dynamicCategorySpecs.map { it.title },
                onUploadWallpaper = { uri, title, category, desc ->
                    onUploadPremiumWallpaper(uri, title, category, desc)
                },
                onDismiss = { showPremiumUploadDialog = false }
            )
        }
    }
}

@Composable
private fun SectionTitleWithSeeAll(
    title: String,
    textColor: Color,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            ),
            color = textColor
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .premiumSmoothClick(pressedScale = 0.92f, onClick = onSeeAll)
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "See All",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = PrimaryPurple
            )
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "See All $title",
                tint = PrimaryPurple,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun FourKPortraitWallpaperCard(
    wallpaper: WallpaperItem,
    isFavorited: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    staggerIndex: Int = 0,
    height: Dp = 204.dp,
    cornerDp: Dp = 18.dp,
    overlayStyle: String = "clean_badge",
    accentColor: Color = Color(0xFF38BDF8)
) {
    val isLuxuryRim = overlayStyle == "luxury_rim"
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerDp))
            .border(
                width = if (isLuxuryRim) 1.3.dp else 0.8.dp,
                color = if (isLuxuryRim) accentColor.copy(alpha = 0.72f) else Color.Black.copy(alpha = 0.08f),
                shape = RoundedCornerShape(cornerDp)
            )
            .premiumSmoothClick(pressedScale = 0.96f, onClick = onClick)
            .testTag("wallpaper_card_${wallpaper.id}")
    ) {
        val primaryCardUrl = remember(wallpaper.thumbnailUrl, wallpaper.imageUrl) {
            val thumb = wallpaper.thumbnailUrl.trim()
            val full = wallpaper.imageUrl.trim()
            val thumbReachable = thumb.isNotBlank() && (
                thumb.startsWith("http", ignoreCase = true) ||
                    thumb.startsWith("data:image", ignoreCase = true) ||
                    (thumb.startsWith("file:/", ignoreCase = true) && runCatching {
                        val f = runCatching { java.io.File(java.net.URI(thumb)) }.getOrElse {
                            java.io.File(thumb.removePrefix("file://").removePrefix("file:"))
                        }
                        f.exists() && f.length() > 0L
                    }.getOrDefault(false)) ||
                    (thumb.startsWith("/") && java.io.File(thumb).exists())
                )
            if (thumbReachable) thumb else full.ifBlank { thumb }
        }
        val fallbackCardUrl = remember(wallpaper.imageUrl, primaryCardUrl) {
            val full = wallpaper.imageUrl.trim()
            if (full.isNotBlank() && full != primaryCardUrl) full else ""
        }
        WallxAsyncImage(
            imageUrl = primaryCardUrl,
            fallbackImageUrl = fallbackCardUrl,
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (overlayStyle == "floating_caption") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = wallpaper.title.ifBlank { "4K Art" },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = wallpaper.category.ifBlank { "4K Ultra HD" },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else if (overlayStyle == "glass_pill") {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.44f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = wallpaper.title.ifBlank { wallpaper.category },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Top-right favorite / verified badge with liquid spring pulse
        val favBadgeScale by animateFloatAsState(
            targetValue = if (isFavorited) 1.12f else 1.0f,
            animationSpec = spring(
                dampingRatio = 0.52f,
                stiffness = Spring.StiffnessMedium
            ),
            label = "favBadgePulse_${wallpaper.id}"
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(7.dp)
                .size(if (overlayStyle == "zero_clutter") 18.dp else 22.dp)
                .graphicsLayer {
                    scaleX = favBadgeScale
                    scaleY = favBadgeScale
                }
                .clip(CircleShape)
                .background(
                    when {
                        isFavorited -> Color(0xFFFF2D55)
                        isLuxuryRim -> accentColor.copy(alpha = 0.88f)
                        else -> Color.White.copy(alpha = 0.86f)
                    }
                )
                .premiumSmoothClick(pressedScale = 0.82f, onClick = onToggleFavorite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.Verified,
                contentDescription = "Favorite ${wallpaper.title}",
                tint = if (isFavorited || isLuxuryRim) Color.White else Color(0xFF0B0F19),
                modifier = Modifier.size(if (overlayStyle == "zero_clutter") 10.dp else 12.dp)
            )
        }
    }
}

// =============================================================================
// SCREEN 3 ("← Categories" 2-Column Grid) &
// SCREEN 4 ("Search wallpapers..." + "Trending Searches" + "Filters")
// =============================================================================

enum class ExploreSubScreenMode {
    CATEGORIES_GRID,
    SEARCH_AND_FILTERS,
    CATEGORY_WALLPAPERS_RESULT
}

@Composable
fun UserExploreScreen(
    wallpapers: List<WallpaperItem>,
    categories: List<CategoryModel>,
    searchQuery: String,
    searchHistory: List<String>,
    favoriteIds: Set<String>,
    onSearchQueryChange: (String) -> Unit,
    onCommitSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    onSelectWallpaper: (WallpaperItem) -> Unit,
    onToggleFavorite: (WallpaperItem) -> Unit,
    onCreateCategory: (CategoryModel) -> Unit = {},
    onOpenMenuDrawer: () -> Unit = {},
    initialSubMode: ExploreSubScreenMode = ExploreSubScreenMode.CATEGORIES_GRID,
    modeRequestKey: Int = 0
) {
    val glass = LocalLiquidGlassColors.current
    val pageBg = if (glass.isDark) DarkPageBg else LavenderPageBg
    val textPrimary = if (glass.isDark) Color.White else Color(0xFF141229)
    val textSecondary = if (glass.isDark) Color(0xFF94A3B8) else Color(0xFF52506E)

    var subMode by remember { mutableStateOf(initialSubMode) }
    LaunchedEffect(initialSubMode, modeRequestKey) {
        subMode = initialSubMode
    }
    var selectedCategoryChip by remember { mutableStateOf("All") }
    var activeResultCategory by remember { mutableStateOf("All") }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    // Filter state for Screen 4 ("Filters")
    var filterResolution by remember { mutableStateOf("Any") }
    var filterCategory by remember { mutableStateOf("Any") }
    var filterSortBy by remember { mutableStateOf("Popular") }
    var filterOnly4K by remember { mutableStateOf(true) }
    var filterHdOnly by remember { mutableStateOf(false) }

    val mergedWallpapers = remember(wallpapers) {
        wallpapers
            .filter { it.status.equals("published", ignoreCase = true) || it.status.isBlank() }
            .deduplicateWallpapers()
    }

    val dynamicCategorySpecs = remember(categories, mergedWallpapers) {
        buildDynamicCategorySpecs(categories, mergedWallpapers)
    }

    BackHandler(enabled = subMode != ExploreSubScreenMode.CATEGORIES_GRID) {
        subMode = ExploreSubScreenMode.CATEGORIES_GRID
    }

    run {
        val mode = subMode
        when (mode) {
            ExploreSubScreenMode.CATEGORIES_GRID -> {
                // =============================================================
                // SCREEN 3: "← Categories" + Chips + 2-Column Category Cards
                // =============================================================
                val chipCategories = remember(dynamicCategorySpecs) {
                    (listOf("All") + dynamicCategorySpecs.map { it.title }).distinctBy { it.lowercase(Locale.ROOT) }
                }

                val filteredCategories = remember(selectedCategoryChip, dynamicCategorySpecs) {
                    if (selectedCategoryChip == "All") {
                        dynamicCategorySpecs
                    } else {
                        dynamicCategorySpecs.filter {
                            it.title.equals(selectedCategoryChip, ignoreCase = true)
                        }.ifEmpty { dynamicCategorySpecs }
                    }
                }

                LiquidGlassScreenWithUpperBlur(
                    modifier = Modifier
                        .background(pageBg)
                        .testTag("screen_categories_grid"),
                    headerExtraHeight = 76.dp,
                    headerClearanceDp = 62.dp,
                    headerContent = {
                        // Top Bar: ← Categories + [+ New Category] + 🔍 (Opens Screen 4: Search & Filters)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (glass.isDark) Color.White.copy(alpha = 0.14f)
                                            else Color.White.copy(alpha = 0.56f)
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                                        .premiumSmoothClick(onClick = onOpenMenuDrawer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Categories (${dynamicCategorySpecs.size})",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp
                                    ),
                                    color = textPrimary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryBlue))
                                        )
                                        .premiumSmoothClick(pressedScale = 0.90f) {
                                            showCreateCategoryDialog = true
                                        }
                                        .padding(horizontal = 12.dp)
                                        .testTag("explore_create_category_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Create Category",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "New Category",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.5.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (glass.isDark) Color.White.copy(alpha = 0.14f)
                                            else Color.White.copy(alpha = 0.56f)
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                                        .premiumSmoothClick {
                                            subMode = ExploreSubScreenMode.SEARCH_AND_FILTERS
                                        }
                                        .testTag("categories_open_search_filters_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Open Search & Filters",
                                        tint = textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                ) { topHeaderClearance ->
                    // 2-Column Category Cards Grid + Filter Chips scrolling cleanly beneath upper blur
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = topHeaderClearance,
                            bottom = 148.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "cat_filter_chips_row") {
                            LazyRow(
                                contentPadding = PaddingValues(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chipCategories, key = { it }) { chip ->
                                    val isSelected = chip == selectedCategoryChip
                                    val chipScale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.07f else 1.0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        ),
                                        label = "catChipScale_$chip"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .graphicsLayer {
                                                scaleX = chipScale
                                                scaleY = chipScale
                                            }
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) PrimaryPurple
                                                else if (glass.isDark) Color(0xFF1A2036)
                                                else Color(0xFFECE9FA)
                                            )
                                            .premiumSmoothClick(pressedScale = 0.90f) {
                                                selectedCategoryChip = chip
                                            }
                                            .padding(horizontal = 18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = chip,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                fontSize = 13.5.sp
                                            ),
                                            color = if (isSelected) Color.White else textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        itemsIndexed(
                            items = filteredCategories,
                            key = { idx, it -> "cat_card_${idx}_${it.title}" }
                        ) { idx, catSpec ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(126.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .premiumSmoothClick(pressedScale = 0.93f) {
                                        activeResultCategory = catSpec.title
                                        subMode = ExploreSubScreenMode.CATEGORY_WALLPAPERS_RESULT
                                    }
                                    .testTag("category_card_${catSpec.title.lowercase()}")
                            ) {
                                WallxAsyncImage(
                                    imageUrl = catSpec.imageUrl,
                                    contentDescription = catSpec.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.25f),
                                                    Color.Black.copy(alpha = 0.76f)
                                                )
                                            )
                                        )
                                )

                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = catSpec.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.5.sp
                                            ),
                                            color = Color.White
                                        )
                                        Text(
                                            text = catSpec.countLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            ),
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .subtleFloatingBounce(amplitudePx = 2.5f, durationMs = 1750 + idx * 90)
                                            .clip(CircleShape)
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Open ${catSpec.title}",
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ExploreSubScreenMode.SEARCH_AND_FILTERS -> {
                // =============================================================
                // SCREEN 4: "Search wallpapers..." + "Trending Searches" + "Filters"
                // =============================================================
                val trendingSearchTags = remember(dynamicCategorySpecs) {
                    (dynamicCategorySpecs.map { it.title } + listOf("4K", "Space", "Dark")).distinct().take(8)
                }
                val resolutionOptions = remember { listOf("Any", "4K UHD", "8K Ultra", "Full HD") }
                val categoryOptions = remember(dynamicCategorySpecs) {
                    (listOf("Any") + dynamicCategorySpecs.map { it.title }).distinct()
                }
                val sortByOptions = remember { listOf("Popular", "Trending", "Newest", "Downloads") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(pageBg)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .padding(bottom = 140.dp)
                        .testTag("screen_search_and_filters")
                ) {
                    // Top Pill Search Bar: [ 🔍 Search wallpapers...   🎤 ]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(CircleShape)
                            .background(if (glass.isDark) Color(0xFF192036) else Color(0xFFEDEAFD))
                            .border(
                                1.dp,
                                if (glass.isDark) Color.White.copy(alpha = 0.10f) else Color(0xFFDDD6FE),
                                CircleShape
                            )
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search wallpapers...",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                        color = textSecondary
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChange,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = textPrimary,
                                        fontSize = 15.sp
                                    ),
                                    cursorBrush = SolidColor(PrimaryPurple),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("explore_search_input")
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = textSecondary,
                                    modifier = Modifier
                                        .size(19.dp)
                                        .clickable { onSearchQueryChange("") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Search",
                                tint = if (glass.isDark) Color(0xFFC4B5FD) else Color(0xFF3B2A6E),
                                modifier = Modifier
                                    .size(21.dp)
                                    .clickable {
                                        onSearchQueryChange("Anime")
                                    }
                            )
                        }
                    }

                    // Live Instant Search Results when typing in Search Bar
                    val liveMatches = remember(searchQuery, mergedWallpapers) {
                        val q = searchQuery.trim()
                        if (q.isBlank()) {
                            emptyList()
                        } else {
                            mergedWallpapers.filter { wall ->
                                wall.title.contains(q, ignoreCase = true) ||
                                    wall.category.contains(q, ignoreCase = true) ||
                                    wall.description.contains(q, ignoreCase = true) ||
                                    wall.tags.any { t -> t.contains(q, ignoreCase = true) }
                            }.take(8)
                        }
                    }

                    if (searchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (liveMatches.isNotEmpty()) {
                                "Search Results for \"$searchQuery\" (${liveMatches.size})"
                            } else {
                                "No wallpapers found for \"$searchQuery\""
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = textPrimary
                        )
                        if (liveMatches.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                liveMatches.chunked(2).forEach { rowWalls ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowWalls.forEach { wall ->
                                            WallpaperGridCard(
                                                wallpaper = wall,
                                                isFavorited = wall.id in favoriteIds,
                                                onClick = { onSelectWallpaper(wall) },
                                                onToggleFavorite = { onToggleFavorite(wall) },
                                                modifier = Modifier.weight(1f),
                                                cardHeight = 200.dp
                                            )
                                        }
                                        if (rowWalls.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // "👑 Trending Searches" + "See All →"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Trending Searches",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.5.sp
                                ),
                                color = textPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    activeResultCategory = "All"
                                    subMode = ExploreSubScreenMode.CATEGORY_WALLPAPERS_RESULT
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "See All",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryPurple
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2 Rows of 4 Trending Search Chips: [Anime] [4K] [Cars] [Nature] / [Gaming] [Space] [Abstract] [Dark]
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        trendingSearchTags.chunked(4).forEach { rowTags ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowTags.forEach { tag ->
                                    val isSelected = searchQuery.equals(tag, ignoreCase = true) ||
                                        filterCategory.equals(tag, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) PrimaryPurple
                                                else if (glass.isDark) Color(0xFF1A2138)
                                                else Color(0xFFECE9FA)
                                            )
                                            .premiumSmoothClick(pressedScale = 0.95f) {
                                                onSearchQueryChange(tag)
                                                onCommitSearch(tag)
                                                activeResultCategory = tag
                                                subMode = ExploreSubScreenMode.CATEGORY_WALLPAPERS_RESULT
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.8.sp
                                            ),
                                            color = if (isSelected) Color.White else textPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // "⏳ Filters" Header + "Reset" Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Filters",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                ),
                                color = textPrimary
                            )
                        }

                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = PrimaryPurple,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    filterResolution = "Any"
                                    filterCategory = "Any"
                                    filterSortBy = "Popular"
                                    filterOnly4K = true
                                    filterHdOnly = false
                                    onSearchQueryChange("")
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filters Card Container (Pure Transparent Clear Liquid Glass matching Screen 4)
                    val filterDividerColor = if (glass.isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.55f)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .ios27LiquidStretch()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (glass.isDark) Color.White.copy(alpha = 0.18f)
                                else Color.White.copy(alpha = 0.44f)
                            )
                            .border(
                                1.4.dp,
                                Color.White.copy(alpha = 0.86f),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        FilterDropdownRow(
                            icon = Icons.Default.AspectRatio,
                            label = "Resolution",
                            value = filterResolution,
                            textColor = textPrimary,
                            isDark = glass.isDark,
                            onClick = {
                                val nextIdx = (resolutionOptions.indexOf(filterResolution) + 1) % resolutionOptions.size
                                filterResolution = resolutionOptions[nextIdx]
                            }
                        )
                        HorizontalDivider(color = filterDividerColor, thickness = 0.8.dp)

                        FilterDropdownRow(
                            icon = Icons.Default.Category,
                            label = "Category",
                            value = filterCategory,
                            textColor = textPrimary,
                            isDark = glass.isDark,
                            onClick = {
                                val nextIdx = (categoryOptions.indexOf(filterCategory) + 1) % categoryOptions.size
                                filterCategory = categoryOptions[nextIdx]
                            }
                        )
                        HorizontalDivider(color = filterDividerColor, thickness = 0.8.dp)

                        FilterDropdownRow(
                            icon = Icons.AutoMirrored.Filled.Sort,
                            label = "Sort By",
                            value = filterSortBy,
                            textColor = textPrimary,
                            isDark = glass.isDark,
                            onClick = {
                                val nextIdx = (sortByOptions.indexOf(filterSortBy) + 1) % sortByOptions.size
                                filterSortBy = sortByOptions[nextIdx]
                            }
                        )
                        HorizontalDivider(color = filterDividerColor, thickness = 0.8.dp)

                        FilterToggleRow(
                            icon = Icons.Default.HighQuality,
                            label = "Only 4K",
                            checked = filterOnly4K,
                            textColor = textPrimary,
                            isDark = glass.isDark,
                            onCheckedChange = { filterOnly4K = it }
                        )
                        HorizontalDivider(color = filterDividerColor, thickness = 0.8.dp)

                        FilterToggleRow(
                            icon = Icons.Default.Hd,
                            label = "HD Only",
                            checked = filterHdOnly,
                            textColor = textPrimary,
                            isDark = glass.isDark,
                            onCheckedChange = { filterHdOnly = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Full-Width Purple-to-Blue Gradient Pill Button: "Search" with Shimmer Sweep & Bouncy Press
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .staggeredBouncyEntrance(index = 3)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(PrimaryPurple, SecondaryBlue)
                                )
                            )
                            .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.30f))
                            .premiumSmoothClick(pressedScale = 0.93f) {
                                activeResultCategory = if (filterCategory != "Any") {
                                    filterCategory
                                } else if (searchQuery.isNotBlank()) {
                                    searchQuery
                                } else {
                                    "All"
                                }
                                if (searchQuery.isNotBlank()) {
                                    onCommitSearch(searchQuery)
                                }
                                subMode = ExploreSubScreenMode.CATEGORY_WALLPAPERS_RESULT
                            }
                            .testTag("search_filters_submit_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Search",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            ExploreSubScreenMode.CATEGORY_WALLPAPERS_RESULT -> {
                // =============================================================
                // DRILL-DOWN 3-COLUMN WALLPAPERS GALLERY FOR SELECTED CATEGORY/SEARCH
                // =============================================================
                WallpersArtworksCategoryDetailScreen(
                    categoryTitle = activeResultCategory,
                    wallpapers = mergedWallpapers,
                    favoriteIds = favoriteIds,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onBack = { subMode = ExploreSubScreenMode.CATEGORIES_GRID },
                    onSelectWallpaper = onSelectWallpaper,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }

    if (showCreateCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        var newCatImageUri by remember { mutableStateOf<Uri?>(null) }
        val catImagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) newCatImageUri = uri
        }

        Dialog(
            onDismissRequest = { showCreateCategoryDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
                    .clickable { showCreateCategoryDialog = false }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredBouncyEntrance(index = 0)
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (glass.isDark) Color(0xFF151B2E) else Color.White)
                        .border(1.5.dp, PrimaryPurple.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
                        .clickable(enabled = false) {}
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Create New Category",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = textPrimary
                    )
                    Text(
                        text = "Your category will appear immediately in Home, Categories & Upload.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (glass.isDark) Color(0xFF1E2640) else Color(0xFFF3F0FF))
                            .border(1.5.dp, PrimaryPurple.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                            .premiumSmoothClick {
                                catImagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val pickedUri = newCatImageUri
                        if (pickedUri != null) {
                            WallxAsyncImage(
                                imageUrl = pickedUri.toString(),
                                contentDescription = "Category Cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap to Pick Category Cover Image (Optional)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryPurple
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (glass.isDark) Color(0xFF1E2640) else Color(0xFFF3F0FF))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (newCatName.isEmpty()) {
                            Text(
                                text = "Category Name (e.g., Superheroes, Cyberpunk)...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF7C7999)
                            )
                        }
                        BasicTextField(
                            value = newCatName,
                            onValueChange = { newCatName = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = textPrimary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(PrimaryPurple),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .height(46.dp)
                                .clip(CircleShape)
                                .background(if (glass.isDark) Color(0xFF222A45) else Color(0xFFECE9FA))
                                .premiumSmoothClick { showCreateCategoryDialog = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = textPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp)
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryBlue)))
                                .premiumSmoothClick {
                                    val cleanName = newCatName.trim()
                                    if (cleanName.isNotBlank()) {
                                        val catId = "cat_${cleanName.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")}"
                                        onCreateCategory(
                                            CategoryModel(
                                                id = catId,
                                                name = cleanName,
                                                description = "Curated $cleanName 4K wallpapers",
                                                thumbnailUrl = newCatImageUri?.toString()
                                                    ?: "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=900&q=85",
                                                order = 0L,
                                                enabled = true,
                                                wallpaperCount = 1L,
                                                accentHex = "#5B2EFF"
                                            )
                                        )
                                        selectedCategoryChip = cleanName
                                        showCreateCategoryDialog = false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Create Category",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDropdownRow(
    icon: ImageVector,
    label: String,
    value: String,
    textColor: Color,
    isDark: Boolean = false,
    onClick: () -> Unit
) {
    val iconTint = if (isDark) Color(0xFFC4B5FD) else Color(0xFF312E59)
    val pillBg = if (isDark) Color(0xFF222A45) else Color(0xFFF1EFFC)
    val pillText = if (isDark) Color.White else Color(0xFF28244A)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = textColor
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(pillBg)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = pillText
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = pillText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    textColor: Color,
    isDark: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isDark) Color(0xFFC4B5FD) else Color(0xFF312E59),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = textColor
            )
        }

        FourKPurpleToggleSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun FourKPurpleToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlassColors.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "purpleToggleOffset"
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (checked) 1.06f else 0.94f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "purpleToggleThumbScale"
    )
    val uncheckedTrack = if (glass.isDark) Color(0xFF2A324D) else Color(0xFFE2E0F0)
    val trackColor by animateColorAsState(
        targetValue = if (checked) PrimaryPurple else uncheckedTrack,
        animationSpec = tween(180),
        label = "purpleToggleTrack"
    )

    Box(
        modifier = modifier
            .width(48.dp)
            .height(28.dp)
            .clip(CircleShape)
            .background(trackColor)
            .premiumSmoothClick(pressedScale = 0.88f) { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .graphicsLayer {
                    scaleX = thumbScale
                    scaleY = thumbScale
                }
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun WallpersArtworksCategoryDetailScreen(
    categoryTitle: String,
    wallpapers: List<WallpaperItem>,
    favoriteIds: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onSelectWallpaper: (WallpaperItem) -> Unit,
    onToggleFavorite: (WallpaperItem) -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    val pageBg = if (glass.isDark) DarkPageBg else LavenderPageBg
    val textPrimary = if (glass.isDark) Color.White else Color(0xFF141229)

    val allAvailableWallpapers = remember(wallpapers) {
        wallpapers
            .filter { it.status.equals("published", ignoreCase = true) || it.status.isBlank() }
            .deduplicateWallpapers()
    }

    val filteredItems = remember(allAvailableWallpapers, categoryTitle, searchQuery) {
        val base = if (categoryTitle == "All" || categoryTitle.isBlank()) {
            allAvailableWallpapers
        } else {
            allAvailableWallpapers.filter {
                it.category.equals(categoryTitle, ignoreCase = true) ||
                    it.title.contains(categoryTitle, ignoreCase = true) ||
                    it.tags.any { t -> t.equals(categoryTitle, ignoreCase = true) }
            }.ifEmpty { allAvailableWallpapers }
        }
        if (searchQuery.isBlank()) base else {
            base.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.tags.any { t -> t.contains(searchQuery, ignoreCase = true) }
            }.ifEmpty { base }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .premiumSmoothClick(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = categoryTitle.ifBlank { "4K Wallpapers" },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    ),
                    color = textPrimary
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 148.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                items = filteredItems,
                key = { idx, it -> "cat_detail_${idx}_${it.id}" }
            ) { idx, item ->
                FourKPortraitWallpaperCard(
                    wallpaper = item,
                    isFavorited = item.id in favoriteIds,
                    staggerIndex = idx,
                    onClick = { onSelectWallpaper(item) },
                    onToggleFavorite = { onToggleFavorite(item) }
                )
            }
        }
    }
}

// =============================================================================
// SCREEN 8: "♥ Favorites" 3-COLUMN SCREEN (Bottom Row, #2 in Reference Image)
// =============================================================================

@Composable
fun UserFavoritesScreen(
    favorites: List<FavoriteRecord>,
    downloads: List<DownloadRecordModel>,
    allWallpapers: List<WallpaperItem>,
    cachedFavoriteWallpapers: List<WallpaperItem> = emptyList(),
    onSelectWallpaper: (WallpaperItem) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onToggleFavoriteItem: ((WallpaperItem) -> Unit)? = null,
    onBackToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val glass = LocalLiquidGlassColors.current
    val pageBg = if (glass.isDark) DarkPageBg else LavenderPageBg
    val textPrimary = if (glass.isDark) Color.White else Color(0xFF141229)

    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Detect whether the device is currently offline so we can inform the user that Room cache is serving favorites
    val isDeviceOffline = remember(favorites, cachedFavoriteWallpapers) {
        runCatching {
            val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val activeNet = cm?.activeNetwork
            val caps = activeNet?.let { cm.getNetworkCapabilities(it) }
            caps == null || !caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }.getOrDefault(false)
    }

    val mergedWallpapers = remember(allWallpapers) {
        allWallpapers
    }

    val favoriteIdsSet = remember(favorites, cachedFavoriteWallpapers) {
        (favorites.map { it.wallpaperId } + cachedFavoriteWallpapers.map { it.id }).toSet()
    }

    val favoriteWallpapers = remember(favorites, cachedFavoriteWallpapers, mergedWallpapers, searchQuery) {
        val cachedById = cachedFavoriteWallpapers.associateBy { it.id }
        val fromRecords = favorites.mapIndexed { index, fav ->
            val safeId = fav.wallpaperId.ifBlank { "fav_item_$index" }
            // Prefer Room-cached WallpaperItem first because it contains localCachedImageUri for 100% offline display
            cachedById[safeId]
                ?: mergedWallpapers.find { it.id == safeId }
                ?: WallpaperItem(
                    id = safeId,
                    title = fav.wallpaperTitle.ifBlank { "4K Wallpaper" },
                    category = fav.wallpaperCategory.ifBlank { "4K UHD" },
                    resolution = fav.wallpaperResolution.ifBlank { "4K UHD" },
                    imageUrl = fav.wallpaperThumbnailUrl,
                    thumbnailUrl = fav.wallpaperThumbnailUrl
                )
        }
        val combinedFavs = (cachedFavoriteWallpapers + fromRecords).distinctBy { it.id }
        val base = combinedFavs.ifEmpty { mergedWallpapers }
        if (searchQuery.isBlank()) base else {
            base.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    LiquidGlassScreenWithUpperBlur(
        modifier = Modifier
            .background(pageBg)
            .testTag("screen_favorites_3col"),
        headerExtraHeight = 80.dp,
        headerClearanceDp = 66.dp,
        headerContent = {
            // Top Header: ♥ Favorites + Offline Room Cache Pill on Left | 🔍 on Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .ios27LiquidStretch()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = if (glass.isDark) 0.24f else 0.56f),
                                        Color.White.copy(alpha = if (glass.isDark) 0.10f else 0.28f)
                                    )
                                )
                            )
                            .border(1.3.dp, Color.White.copy(alpha = 0.88f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorites",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Favorites",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            ),
                            color = textPrimary
                        )
                        Text(
                            text = if (isDeviceOffline) {
                                "Offline Mode • Viewing ${favoriteIdsSet.size} Room Cached Favorites"
                            } else {
                                "Room Offline Cache Ready • ${favoriteIdsSet.size} Saved in 4K"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = if (isDeviceOffline) Color(0xFF059669) else textPrimary.copy(alpha = 0.72f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .ios27LiquidStretch()
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (glass.isDark) 0.22f else 0.52f),
                                    Color.White.copy(alpha = if (glass.isDark) 0.10f else 0.26f)
                                )
                            )
                        )
                        .border(1.2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                        .premiumSmoothClick { showSearchBar = !showSearchBar }
                        .testTag("favorites_search_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Favorites",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) { topHeaderClearance ->
        // 3-Column Portrait Grid scrolling smoothly beneath the upper blur
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = topHeaderClearance,
                bottom = 148.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (showSearchBar) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "fav_search_bar_item") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .height(46.dp)
                            .ios27LiquidStretch()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (glass.isDark) {
                                        listOf(
                                            Color.White.copy(alpha = 0.18f),
                                            Color.White.copy(alpha = 0.10f)
                                        )
                                    } else {
                                        listOf(
                                            Color.White.copy(alpha = 0.54f),
                                            Color.White.copy(alpha = 0.34f)
                                        )
                                    }
                                )
                            )
                            .border(1.3.dp, Color.White.copy(alpha = 0.88f), CircleShape)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textPrimary),
                            cursorBrush = SolidColor(PrimaryPurple),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("favorites_search_input")
                        )
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search offline-cached favorites by title or tag...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textPrimary.copy(alpha = 0.58f)
                            )
                        }
                    }
                }
            }

            itemsIndexed(
                items = favoriteWallpapers,
                key = { idx, it -> "fav_grid_${idx}_${it.id}" }
            ) { idx, item ->
                FourKPortraitWallpaperCard(
                    wallpaper = item,
                    isFavorited = favoriteIdsSet.isEmpty() || item.id in favoriteIdsSet,
                    staggerIndex = idx,
                    onClick = { onSelectWallpaper(item) },
                    onToggleFavorite = {
                        if (onToggleFavoriteItem != null) {
                            onToggleFavoriteItem(item)
                        } else {
                            onRemoveFavorite(item.id)
                        }
                    }
                )
            }
        }
    }
}

// =============================================================================
// SCREEN 9 ("My Library": Downloaded | Collection | History) &
// SCREEN 10 ("← Downloads" + "4K_Wallpaper.jpg" + "Download Manager" Card)
// =============================================================================

@Composable
fun UserLibraryAndDownloadsScreen(
    favorites: List<FavoriteRecord>,
    downloads: List<DownloadRecordModel>,
    allWallpapers: List<WallpaperItem>,
    favoriteIds: Set<String>,
    uploadedWallpapers: List<WallpaperItem> = emptyList(),
    availableCategories: List<String> = emptyList(),
    isPremiumUser: Boolean = false,
    onOpenPremiumModal: () -> Unit = {},
    onUploadWallpaper: (Uri, String, String, String) -> Unit = { _, _, _, _ -> },
    onSelectWallpaper: (WallpaperItem) -> Unit,
    onToggleFavorite: (WallpaperItem) -> Unit,
    onOpenAiStudioModal: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    val pageBg = if (glass.isDark) DarkPageBg else LavenderPageBg
    val textPrimary = if (glass.isDark) Color.White else Color(0xFF141229)

    var showDownloadsManagerScreen by remember { mutableStateOf(false) }
    var showLibraryUploadDialog by remember { mutableStateOf(false) }
    var selectedLibraryTab by remember { mutableStateOf("Uploaded") } // "Uploaded" | "Downloaded" | "Collection" | "History"

    val mergedWallpapers = remember(uploadedWallpapers, allWallpapers) {
        (uploadedWallpapers + allWallpapers).distinctBy { it.id }
    }

    val uploadedItems = remember(uploadedWallpapers, mergedWallpapers) {
        if (uploadedWallpapers.isNotEmpty()) {
            (uploadedWallpapers + mergedWallpapers).distinctBy { it.id }
        } else {
            mergedWallpapers
        }
    }

    val downloadedItems = remember(uploadedWallpapers, downloads, mergedWallpapers) {
        val dlItems = downloads.mapIndexed { index, dl ->
            val safeId = dl.wallpaperId.ifBlank { "dl_item_$index" }
            mergedWallpapers.find { it.id == safeId }
                ?: WallpaperItem(
                    id = safeId,
                    title = dl.wallpaperTitle.ifBlank { "4K Wallpaper" },
                    category = "4K UHD",
                    resolution = dl.wallpaperResolution.ifBlank { "4K UHD" },
                    imageUrl = dl.wallpaperThumbnailUrl.ifBlank { "drawable://home_hero_mountain_4k" },
                    thumbnailUrl = dl.wallpaperThumbnailUrl.ifBlank { "drawable://home_hero_mountain_4k" }
                )
        }.distinctBy { it.id }
        (uploadedWallpapers + dlItems).distinctBy { it.id }
    }

    val collectionItems = remember(uploadedWallpapers, favorites, mergedWallpapers) {
        val favItems = favorites.mapIndexed { index, fav ->
            val safeId = fav.wallpaperId.ifBlank { "col_item_$index" }
            mergedWallpapers.find { it.id == safeId }
                ?: WallpaperItem(
                    id = safeId,
                    title = fav.wallpaperTitle.ifBlank { "4K Wallpaper" },
                    category = fav.wallpaperCategory.ifBlank { "4K UHD" },
                    resolution = fav.wallpaperResolution.ifBlank { "4K UHD" },
                    imageUrl = fav.wallpaperThumbnailUrl.ifBlank { "drawable://home_hero_mountain_4k" },
                    thumbnailUrl = fav.wallpaperThumbnailUrl.ifBlank { "drawable://home_hero_mountain_4k" }
                )
        }.distinctBy { it.id }
        (uploadedWallpapers + favItems).distinctBy { it.id }
    }

    val historyItems = remember(uploadedWallpapers, mergedWallpapers) {
        (uploadedWallpapers + mergedWallpapers).distinctBy { it.id }
    }

    val usedVaultMb = remember(uploadedWallpapers, downloads) {
        val uploadedBytes = uploadedWallpapers.sumOf { it.fileSizeBytes.coerceAtLeast(3_800_000L) }
        val dlBytes = downloads.size * 4_800_000L
        ((uploadedBytes + dlBytes) / (1024f * 1024f)).coerceAtLeast(4.8f)
    }

    BackHandler(enabled = showDownloadsManagerScreen) {
        showDownloadsManagerScreen = false
    }

    run {
        val isDownloadsScreen = showDownloadsManagerScreen
        if (isDownloadsScreen) {
            // =================================================================
            // SCREEN 10: "← 2 TB Storage & Downloads" + Uploaded & Downloaded Files
            // =================================================================
            DownloadsManagerDetailScreen(
                downloads = downloads,
                uploadedWallpapers = uploadedWallpapers,
                allWallpapers = mergedWallpapers,
                onBack = { showDownloadsManagerScreen = false },
                onOpenSettings = onOpenSettings,
                onSelectWallpaper = onSelectWallpaper,
                onOpenUploadDialog = {
                    if (isPremiumUser) {
                        showLibraryUploadDialog = true
                    } else {
                        onOpenPremiumModal()
                    }
                }
            )
        } else {
            // =================================================================
            // SCREEN 9: "My Library" + [Uploaded | Downloaded | Collection | History] + 3-Col Grid
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pageBg)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .testTag("screen_my_library")
            ) {
                // Top Bar: 🧩 My Library on Left | [Upload] + (AI Studio) & 🔍 (2 TB Storage Manager) on Right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = "My Library",
                            tint = if (glass.isDark) Color(0xFFC4B5FD) else Color(0xFF3B1E54),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "My Library",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                ),
                                color = textPrimary
                            )
                            Text(
                                text = "${uploadedWallpapers.size} Uploaded • 2 TB Vault",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (glass.isDark) Color(0xFF94A3B8) else Color(0xFF6E6B8A)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Upload Wallpaper Button in My Library (Premium Only)
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryBlue))
                                )
                                .premiumSmoothClick(pressedScale = 0.88f) {
                                    if (isPremiumUser) {
                                        showLibraryUploadDialog = true
                                    } else {
                                        onOpenPremiumModal()
                                    }
                                }
                                .padding(horizontal = 12.dp)
                                .testTag("library_upload_wallpaper_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Upload to 2 TB Vault",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isPremiumUser) "Upload" else "Upload 👑",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        // "+" Button -> Opens 8K AI Studio
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .premiumSmoothClick(onClick = onOpenAiStudioModal)
                                .testTag("library_open_ai_studio_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create with 8K AI",
                                tint = textPrimary,
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        // Storage & Download Manager Button -> Opens Screen 10 ("← 2 TB Storage")
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple.copy(alpha = 0.12f))
                                .premiumSmoothClick { showDownloadsManagerScreen = true }
                                .testTag("library_open_downloads_manager_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "2 TB Storage Manager",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 4 Pill Tabs: [ Uploaded ] [ Downloaded ] [ Collection ] [ History ] with Bouncy Spring Scale
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Uploaded", "Downloaded", "Collection", "History").forEach { tab ->
                        val isSelected = selectedLibraryTab == tab
                        val tabScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "libTabScale_$tab"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .graphicsLayer {
                                    scaleX = tabScale
                                    scaleY = tabScale
                                }
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) PrimaryPurple
                                    else if (glass.isDark) Color(0xFF1A2138)
                                    else Color(0xFFECE9FA)
                                )
                                .premiumSmoothClick(pressedScale = 0.90f) {
                                    selectedLibraryTab = tab
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = if (isSelected) Color.White else textPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 2 TB Ultra HD Storage Vault Banner -> Opens Screen 10 ("2 TB Storage & Downloads Manager")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .staggeredBouncyEntrance(index = 0)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (glass.isDark) Color(0xFF182038) else Color.White)
                        .border(
                            1.dp,
                            if (glass.isDark) Color.White.copy(alpha = 0.10f) else Color(0xFFE4DFFA),
                            RoundedCornerShape(16.dp)
                        )
                        .premiumSmoothClick(pressedScale = 0.95f) { showDownloadsManagerScreen = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PrimaryPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "2 TB Ultra HD Storage Vault",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", usedVaultMb)} MB used of 2 TB (2,048 GB) • ${uploadedWallpapers.size} Uploaded in 4K/8K",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (glass.isDark) Color(0xFF94A3B8) else Color(0xFF6E6B8A)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open 2 TB Storage Manager",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(if (glass.isDark) Color(0xFF253052) else Color(0xFFE9E4FC))
                        ) {
                            val fillFraction = ((usedVaultMb / 2097152f) + 0.06f).coerceIn(0.06f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fillFraction)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(PrimaryPurple, SecondaryBlue, Color(0xFF10B981))
                                        )
                                    )
                            )
                        }
                    }
                }

                // 3-Column Portrait Grid rendered directly without tab animation
                run {
                    val activeTab = selectedLibraryTab
                    val activeTabItems = when (activeTab) {
                        "Uploaded" -> uploadedItems
                        "Downloaded" -> downloadedItems
                        "Collection" -> collectionItems
                        else -> historyItems
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 6.dp,
                            bottom = 148.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = activeTabItems,
                            key = { idx, it -> "lib_${activeTab}_${idx}_${it.id}" }
                        ) { idx, item ->
                            FourKPortraitWallpaperCard(
                                wallpaper = item,
                                isFavorited = item.id in favoriteIds || activeTab == "Downloaded",
                                staggerIndex = idx,
                                onClick = { onSelectWallpaper(item) },
                                onToggleFavorite = { onToggleFavorite(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLibraryUploadDialog) {
        PremiumUploadWallpaperDialog(
            availableCategories = availableCategories,
            onUploadWallpaper = { uri, title, category, desc ->
                onUploadWallpaper(uri, title, category, desc)
            },
            onDismiss = { showLibraryUploadDialog = false }
        )
    }
}

// =============================================================================
// SCREEN 10: "← 50 GB Storage & Downloads" Screen
// =============================================================================

@Composable
fun DownloadsManagerDetailScreen(
    downloads: List<DownloadRecordModel>,
    uploadedWallpapers: List<WallpaperItem> = emptyList(),
    allWallpapers: List<WallpaperItem>,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectWallpaper: (WallpaperItem) -> Unit,
    onOpenUploadDialog: () -> Unit = {}
) {
    val glass = LocalLiquidGlassColors.current
    val pageBg = if (glass.isDark) DarkPageBg else LavenderPageBg
    val textPrimary = if (glass.isDark) Color.White else Color(0xFF141229)
    val textSecondary = if (glass.isDark) Color(0xFF94A3B8) else Color(0xFF52506E)

    val primaryWallpaper = remember(uploadedWallpapers, allWallpapers) {
        uploadedWallpapers.firstOrNull()
            ?: allWallpapers.firstOrNull()
    }

    val usedVaultMb = remember(uploadedWallpapers, downloads) {
        val uploadedBytes = uploadedWallpapers.sumOf { it.fileSizeBytes.coerceAtLeast(3_800_000L) }
        val dlBytes = downloads.size * 4_800_000L
        ((uploadedBytes + dlBytes) / (1024f * 1024f)).coerceAtLeast(4.8f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .padding(bottom = 140.dp)
            .testTag("screen_downloads_manager")
    ) {
        // Top Header: ← 50 GB Storage & Downloads on Left | [Upload] & ⚙️ on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .premiumSmoothClick(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "2 TB Storage Vault",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = textPrimary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", usedVaultMb)} MB used of 2 TB (2,048 GB) Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = textSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(PrimaryPurple, SecondaryBlue)))
                        .premiumSmoothClick(onClick = onOpenUploadDialog)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Upload 4K",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .premiumSmoothClick(onClick = onOpenSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = if (glass.isDark) Color(0xFFC4B5FD) else Color(0xFF332C54),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2 TB Storage Quota Progress Card (Pure Transparent Clear Liquid Glass)
        val dlBorderColor = Color.White.copy(alpha = 0.85f)
        val dlGlassBg = if (glass.isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.44f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .ios27LiquidStretch()
                .clip(RoundedCornerShape(22.dp))
                .background(dlGlassBg)
                .border(1.4.dp, dlBorderColor, RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Permanent 2 TB Ultra HD 4K/8K Storage",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = textPrimary
                    )
                    Text(
                        text = "2 TB (2,048 GB) Active",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF10B981)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.45f))
                ) {
                    val fillFraction = ((usedVaultMb / 2097152f) + 0.08f).coerceIn(0.08f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fillFraction)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PrimaryPurple, SecondaryBlue, Color(0xFF10B981))
                                )
                            )
                    )
                }
                Text(
                    text = "Used: ${String.format(Locale.US, "%.2f", usedVaultMb)} MB • Free: ${String.format(Locale.US, "%.2f", 2048f - (usedVaultMb / 1024f))} GB (2 TB Permanent) • Lossless 4K/8K Enabled",
                    style = MaterialTheme.typography.labelSmall,
                    color = textSecondary
                )
            }
        }

        // Show all Uploaded Wallpapers in the 2 TB Vault
        if (uploadedWallpapers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Uploaded Wallpapers in 2 TB Vault (${uploadedWallpapers.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            uploadedWallpapers.forEach { upWall ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .ios27LiquidStretch()
                        .clip(RoundedCornerShape(20.dp))
                        .background(dlGlassBg)
                        .border(1.3.dp, dlBorderColor, RoundedCornerShape(20.dp))
                        .premiumSmoothClick { onSelectWallpaper(upWall) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            WallxAsyncImage(
                                imageUrl = upWall.imageUrl.ifBlank { upWall.thumbnailUrl },
                                contentDescription = upWall.title,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = upWall.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${upWall.category} • ${upWall.resolution} • ${upWall.fileSize}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Stored in 2 TB Vault",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Show real download or uploaded entry only (no fake/default placeholder card)
        if (downloads.isEmpty() && uploadedWallpapers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .ios27LiquidStretch()
                    .clip(RoundedCornerShape(20.dp))
                    .background(dlGlassBg)
                    .border(1.3.dp, dlBorderColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "No downloaded or uploaded wallpapers yet. Any wallpaper you download or upload appears here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )
            }
        } else if (downloads.isEmpty() && primaryWallpaper != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .ios27LiquidStretch()
                    .clip(RoundedCornerShape(20.dp))
                    .background(dlGlassBg)
                    .border(1.4.dp, dlBorderColor, RoundedCornerShape(20.dp))
                    .premiumSmoothClick { onSelectWallpaper(primaryWallpaper) }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        WallxAsyncImage(
                            imageUrl = primaryWallpaper.imageUrl.ifBlank { primaryWallpaper.thumbnailUrl },
                            contentDescription = primaryWallpaper.title,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = primaryWallpaper.title.ifBlank { "Uploaded 4K Wallpaper" },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                ),
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${primaryWallpaper.fileSize.ifBlank { "4K UHD" }} • Ready in Vault",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.8.sp
                                ),
                                color = textSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Ready",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Additional user downloads if any
        downloads.take(4).forEach { dl ->
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .ios27LiquidStretch()
                    .clip(RoundedCornerShape(20.dp))
                    .background(dlGlassBg)
                    .border(1.3.dp, dlBorderColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WallxAsyncImage(
                            imageUrl = dl.wallpaperThumbnailUrl,
                            contentDescription = dl.wallpaperTitle,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${dl.wallpaperTitle.replace(" ", "_")}.jpg",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "5.2 MB • Completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // "All Downloads Complete" Pure Transparent Clear Liquid Glass Card (Matches Screen 9 Reference!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .ios27LiquidStretch()
                .clip(RoundedCornerShape(28.dp))
                .background(dlGlassBg)
                .border(1.5.dp, Color.White.copy(alpha = 0.88f), RoundedCornerShape(28.dp))
                .padding(top = 24.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(PrimaryPurple, Color(0xFF7C3AED))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Download Manager",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            ),
                            color = textPrimary
                        )
                        Text(
                            text = "Manage your downloads",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "View All →" Pill Button
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFFD8CEFF), CircleShape)
                        .premiumSmoothClick(onClick = onBack)
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            color = PrimaryPurple
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 3D-Styled Purple Folder with Floating Bounce & Shimmer (Matches Screen 10 Bottom Art)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(118.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(114.dp)
                            .height(86.dp)
                            .subtleFloatingBounce(amplitudePx = 8f, durationMs = 1950)
                            .drawBehind {
                                drawRoundRect(
                                    color = PrimaryPurple.copy(alpha = 0.25f),
                                    topLeft = Offset(0f, 10.dp.toPx()),
                                    size = size,
                                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                                )
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFF5B2EFF))
                                )
                            )
                            .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.26f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// SCREEN 12: "⚙️ Settings" SCREEN (Bottom Row, #6 in Reference Image)
// - Profile Header: Avatar + "Divyansh" + "Premium User 👑"
// - Rows:
//   1. Notifications (Toggle)
//   2. Auto Download (Toggle)
//   3. Quality Preference (4K >)
//   4. Storage Manager (>)
//   5. Appearance (System >)
//   6. About App (v1.0.0)
// =============================================================================

@Composable
fun UserProfileScreen(
    userProfile: UserProfileModel?,
    currentUserId: String,
    favoritesCount: Int,
    downloadsCount: Int,
    themePreference: String,
    optics: LiquidGlassOptics,
    isAuthorizedAdmin: Boolean,
    onUpdateThemePreference: (String) -> Unit,
    onUpdateBlurRadius: (Float) -> Unit,
    onUpdateDockBlur: (Float) -> Unit = {},
    onUpdateUpperBlurStrength: (Float) -> Unit = {},
    onUpdate120HzSmoothness: (Boolean) -> Unit = {},
    onUpdateGlassOpacity: (Float) -> Unit,
    onUpdateGlowIntensity: (Float) -> Unit,
    onUpdateSpecularShine: (Float) -> Unit,
    onUpdateLiquidStretch: (Float) -> Unit,
    onUpdateAmbientOrbsMotion: (Float) -> Unit,
    onUpdateRefractionStrength: (Float) -> Unit,
    onUpdateRefractionDepth: (Float) -> Unit,
    onUpdateChromaticSplit: (Float) -> Unit = {},
    onUpdateTintHex: (String) -> Unit,
    onApplyPreset: (LiquidGlassPreset) -> Unit,
    onResetOptics: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onLogout: () -> Unit,
    selectedAppDesign: AppDesignStyle = AppDesignCatalog.ios27Crystal,
    onSelectAppDesign: (AppDesignStyle) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onOpenAiStudio: () -> Unit = {},
    onNotifyMessage: (String) -> Unit = {},
    onDeleteAccountAndData: () -> Unit = { onLogout() },
    onUpdateEmailProfile: (String, String) -> Unit = { _, _ -> },
    onOpenStorageDownloadsManager: () -> Unit = {},
    isPremiumUser: Boolean = false,
    customQrCodeUri: String? = null,
    onUnlockPremiumViaQr: (String) -> Unit = {},
    onUpdateCustomQrCode: (Uri) -> Unit = {},
    onUploadPremiumWallpaper: (Uri, String, String, String) -> Unit = { _, _, _, _ -> },
    userUploadedWallpapers: List<WallpaperItem> = emptyList(),
    onOpenEarnMoneyPopup: () -> Unit = {}
) {
    val context = LocalContext.current
    val glass = LocalLiquidGlassColors.current
    val pageBg = if (glass.isDark) DarkPageBg else LavenderPageBg
    val textPrimary = if (glass.isDark) Color.White else Color(0xFF141229)
    val textSecondary = if (glass.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val cardBg = if (glass.isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.44f)
    val dividerColor = if (glass.isDark) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.78f)

    val isGuestUser = currentUserId == com.example.ui.auth.PixoraEmailSessionManager.GUEST_USER_ID
    val displayName = remember(userProfile?.displayName, isGuestUser) {
        if (isGuestUser) {
            "Guest Explorer"
        } else {
            val raw = userProfile?.displayName?.trim().orEmpty()
            if (raw.isBlank() || raw.equals("Pixora Creator", ignoreCase = true) || raw.equals("Collector", ignoreCase = true)) {
                "Divyansh"
            } else {
                raw
            }
        }
    }
    val displayEmail = if (isGuestUser) {
        "Tap to sign in & sync favorites or VIP"
    } else {
        userProfile?.email?.ifBlank { "d99312907@gmail.com" } ?: "d99312907@gmail.com"
    }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoDownloadEnabled by remember { mutableStateOf(true) }
    var qualityPreference by remember { mutableStateOf("4K") }
    val autoConfig by AutoWallpaperChangerManager.config.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        AutoWallpaperChangerManager.initialize(context)
    }

    val qualityOptions = remember { listOf("4K", "8K Ultra", "Full HD") }

    val appearanceLabel = remember(themePreference) {
        when (themePreference.lowercase(Locale.ROOT)) {
            "dark" -> "Dark"
            "light" -> "Light"
            else -> "System"
        }
    }

    var showGoPremiumModal by remember { mutableStateOf(false) }
    var showUploadWallpaperModal by remember { mutableStateOf(false) }
    var showCreatorEarningsModal by remember { mutableStateOf(false) }
    var showSplashPreviewOverlay by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsAndAiDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showEmailCenterDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        LiquidGlassScreenWithUpperBlur(
            headerExtraHeight = 80.dp,
            headerClearanceDp = 66.dp,
            headerContent = {
                // Floating Telegram-style Settings Top Header above Progressive Upper Blur
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (glass.isDark) Color.White.copy(alpha = 0.14f)
                                else Color.White.copy(alpha = 0.58f)
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile & Settings",
                            tint = if (glass.isDark) Color.White else Color(0xFF28224A),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp
                        ),
                        color = textPrimary
                    )

                    if (!isPremiumUser) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0B0F19))
                                .border(1.dp, Color(0xFFFFD426).copy(alpha = 0.65f), CircleShape)
                                .premiumSmoothClick { showGoPremiumModal = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Open Premium",
                                tint = Color(0xFFFFD426),
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }
        ) { topHeaderClearance ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("user_profile_list"),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = topHeaderClearance,
                    bottom = 148.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2. PROFILE CARD: User Profile Details + Saved/Downloads Stats + Premium Badge
                item(key = "settings_profile_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredBouncyEntrance(index = 0)
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg)
                        .border(1.dp, dividerColor, RoundedCornerShape(24.dp))
                        .premiumSmoothClick(pressedScale = 0.96f) {
                            if (isGuestUser) onLogout() else showEmailCenterDialog = true
                        }
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(PrimaryPurple, SecondaryBlue)
                                            )
                                        )
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile Avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 19.sp
                                        ),
                                        color = textPrimary
                                    )
                                    Text(
                                        text = displayEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .then(
                                                if (!isPremiumUser) {
                                                    Modifier.clickable { showGoPremiumModal = true }
                                                } else {
                                                    Modifier
                                                }
                                            )
                                    ) {
                                        Text(
                                            text = if (isPremiumUser) "Creator Account • Active" else "Upgrade to Premium • ₹299",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            ),
                                            color = if (isPremiumUser) Color(0xFF10B981) else Color(0xFFF59E0B)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Icon(
                                            imageVector = if (isPremiumUser) Icons.Default.Verified else Icons.Default.WorkspacePremium,
                                            contentDescription = if (isPremiumUser) "Verified Creator" else "Premium Crown",
                                            tint = if (isPremiumUser) Color(0xFF10B981) else Color(0xFFF59E0B),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .subtleFloatingBounce(amplitudePx = 2.5f, durationMs = 1700)
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Edit Profile",
                                tint = textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (glass.isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.55f))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Saved: $favoritesCount",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (glass.isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.55f))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Downloads: $downloadsCount",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }
            }


            // 3. MAIN SETTINGS LIST CARD (Matches Screen 12 Rows 1–6) with Staggered Bouncy Entrance
            item(key = "settings_main_card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredBouncyEntrance(index = 2)
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg)
                        .border(1.dp, dividerColor, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    // Row 1: Notifications (Toggle)
                    Screen12SettingsToggleRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        checked = notificationsEnabled,
                        textColor = textPrimary,
                        onCheckedChange = {
                            notificationsEnabled = it
                            onNotifyMessage(if (it) "Notifications enabled" else "Notifications muted")
                        }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    // Row 2: Auto Download (Toggle)
                    Screen12SettingsToggleRow(
                        icon = Icons.Default.Download,
                        title = "Auto Download",
                        checked = autoDownloadEnabled,
                        textColor = textPrimary,
                        onCheckedChange = {
                            autoDownloadEnabled = it
                            onNotifyMessage(if (it) "Auto Download enabled for 4K Wi-Fi" else "Auto Download disabled")
                        }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    // Row 3: Quality Preference (4K >)
                    Screen12SettingsNavigationRow(
                        icon = Icons.Default.GridView,
                        title = "Quality Preference",
                        trailingValue = qualityPreference,
                        showChevron = true,
                        textColor = textPrimary,
                        secondaryColor = textSecondary,
                        onClick = {
                            val nextIdx = (qualityOptions.indexOf(qualityPreference) + 1) % qualityOptions.size
                            qualityPreference = qualityOptions[nextIdx]
                            onNotifyMessage("Quality Preference set to $qualityPreference")
                        }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    // Row 4: Storage Manager (2 TB Cloud Vault >)
                    Screen12SettingsNavigationRow(
                        icon = Icons.Default.Storage,
                        title = "Storage Manager",
                        trailingValue = "2 TB Cloud Vault",
                        showChevron = true,
                        textColor = textPrimary,
                        secondaryColor = Color(0xFF10B981),
                        onClick = onOpenStorageDownloadsManager
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    // Row 4.5: Auto Wallpaper Changer (Every 3h > / Active >)
                    Screen12SettingsNavigationRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Auto Wallpaper Changer",
                        trailingValue = if (autoConfig.enabled) "Active (${autoConfig.intervalHours}h)" else "Off",
                        showChevron = true,
                        textColor = textPrimary,
                        secondaryColor = if (autoConfig.enabled) Color(0xFF10B981) else textSecondary,
                        onClick = {
                            val nextState = !autoConfig.enabled
                            AutoWallpaperChangerManager.updateConfig(enabled = nextState)
                            onNotifyMessage(if (nextState) "Auto Wallpaper Changer Enabled (Every ${autoConfig.intervalHours}h)" else "Auto Wallpaper Changer Disabled")
                        }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    // Row 5: Appearance (System >)
                    Screen12SettingsNavigationRow(
                        icon = Icons.Default.Palette,
                        title = "Appearance",
                        trailingValue = appearanceLabel,
                        showChevron = true,
                        textColor = textPrimary,
                        secondaryColor = textSecondary,
                        onClick = {
                            val nextMode = when (themePreference.lowercase(Locale.ROOT)) {
                                "system" -> "light"
                                "light" -> "dark"
                                else -> "system"
                            }
                            onUpdateThemePreference(nextMode)
                        }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    // Row 6: Upload 4K Wallpaper (Requires Premium Purchase to Earn & Upload)
                    Screen12SettingsNavigationRow(
                        icon = if (isPremiumUser) Icons.Default.CloudUpload else Icons.Default.Lock,
                        title = "Upload 4K Wallpaper",
                        trailingValue = if (isPremiumUser) "2 TB Ready" else "Earn & Upload 👑",
                        showChevron = true,
                        textColor = textPrimary,
                        secondaryColor = if (isPremiumUser) Color(0xFF10B981) else Color(0xFFF59E0B),
                        onClick = {
                            if (isPremiumUser) {
                                showUploadWallpaperModal = true
                            } else {
                                onOpenEarnMoneyPopup()
                            }
                        }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    // Row 6.2: How to Earn Money by Uploading Wallpaper (shown only before Premium unlock)
                    if (!isPremiumUser) {
                        Screen12SettingsNavigationRow(
                            icon = Icons.Default.Paid,
                            title = "Earn Money by Uploading Wallpaper 💸",
                            trailingValue = "Earn ₹25+",
                            showChevron = true,
                            textColor = textPrimary,
                            secondaryColor = Color(0xFF10B981),
                            iconTint = Color(0xFFFFD426),
                            onClick = onOpenEarnMoneyPopup
                        )
                        HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                    }

                    // Row 6.5: EARN BUTTON (Appears in Settings once user unlocks Creator/Earn access)
                    if (isPremiumUser) {
                        Screen12SettingsNavigationRow(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Earn • Check Creator Earnings 💸",
                            trailingValue = "Check Balance",
                            showChevron = true,
                            textColor = textPrimary,
                            secondaryColor = Color(0xFF10B981),
                            iconTint = Color(0xFF10B981),
                            onClick = {
                                showCreatorEarningsModal = true
                            }
                        )
                        HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                    }

                    // Row 7: About App & Developer Details (Divyansh • Developer / Hacker)
                    Screen12SettingsNavigationRow(
                        icon = Icons.Default.Info,
                        title = "About App & Developer",
                        trailingValue = "Divyansh • Hacker",
                        showChevron = true,
                        textColor = textPrimary,
                        secondaryColor = PrimaryPurple,
                        onClick = { showSplashPreviewOverlay = true }
                    )
                }
            }

            // 3.5 CREATOR EARNINGS CARD (Visible in Settings as soon as User purchases Premium!)
            if (isPremiumUser) {
                item(key = "settings_creator_earnings_card") {
                    val myUploadsCount = userUploadedWallpapers.size
                    val myViewsCount = userUploadedWallpapers.sumOf { it.viewCount }
                    val totalDl = userUploadedWallpapers.sumOf { it.downloadCount }
                    val totalFav = userUploadedWallpapers.sumOf { it.favoriteCount }
                    // Real Data: 3k views = $1, 5k uploads = $1, 1k downloads = $2, starts at $0.00
                    val viewEarnings = myViewsCount / 3000.0
                    val uploadEarnings = myUploadsCount / 5000.0
                    val downloadEarnings = (totalDl / 1000.0) * 2.0
                    val estimatedDollars = viewEarnings + uploadEarnings + downloadEarnings
                    val formattedEarnings = String.format(Locale.US, "$%.2f", estimatedDollars)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredBouncyEntrance(index = 2)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF0F766E), Color(0xFF059669), Color(0xFF10B981))
                                )
                            )
                            .border(1.4.dp, Color(0xFFFFD426).copy(alpha = 0.85f), RoundedCornerShape(22.dp))
                            .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.28f))
                            .premiumSmoothClick(pressedScale = 0.95f) {
                                showCreatorEarningsModal = true
                            }
                            .padding(horizontal = 18.dp, vertical = 15.dp)
                            .testTag("settings_earn_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.20f))
                                        .border(1.dp, Color(0xFFFFD426), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Paid,
                                        contentDescription = "Earn Money",
                                        tint = Color(0xFFFFD426),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Earn • Creator Earnings ($formattedEarnings)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "3k views = $1 • 5k uploads = $1 • 1k downloads = $2",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.90f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFFFFD426),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 4. GO PREMIUM BANNER CARD (Completely hidden once user has purchased Premium)
            if (!isPremiumUser) {
                item(key = "settings_go_premium_card") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredBouncyEntrance(index = 2)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF4C1D95), PrimaryPurple)
                                )
                            )
                            .shimmerSweepHighlight(shimmerColor = Color.White.copy(alpha = 0.24f))
                            .premiumSmoothClick(pressedScale = 0.95f) {
                                showGoPremiumModal = true
                            }
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD426),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Go Premium • ₹299 QR Code Pass",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Pay ₹299 via QR Code to unlock wallpaper uploads & Earn tab",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFFFFD426),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 5. ADMIN CONTROL CENTER (If Authorized Admin)
            if (isAuthorizedAdmin) {
                item(key = "settings_admin_card") {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp,
                        glowColor = AmberGlow,
                        secondaryGlowColor = ElectricCyan,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Access",
                                    tint = AmberGlow
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Administrator Control Center",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LiquidGlassButton(
                                text = "Open Admin Panel",
                                icon = Icons.Default.AdminPanelSettings,
                                glowColor = AmberGlow,
                                filledAccent = true,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "open_admin_panel_button",
                                onClick = onOpenAdminPanel
                            )
                        }
                    }
                }
            }

            // 6. PRIVACY POLICY, TERMS & ACCOUNT ACTIONS
            item(key = "settings_privacy_and_logout") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg)
                        .border(1.dp, dividerColor, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Screen12SettingsNavigationRow(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy & Store Approval",
                        trailingValue = "Open & View",
                        showChevron = true,
                        textColor = textPrimary,
                        secondaryColor = textSecondary,
                        onClick = { showPrivacyPolicyDialog = true }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    Screen12SettingsNavigationRow(
                        icon = Icons.Default.Share,
                        title = "Copy Privacy Policy Link",
                        trailingValue = "Copy URL",
                        showChevron = true,
                        textColor = textPrimary,
                        secondaryColor = Color(0xFF2563EB),
                        onClick = {
                            com.example.ui.components.copyTextToClipboard(
                                context = context,
                                label = "Privacy Policy Link",
                                text = "https://ais-pre-klmpoxxrbkbpzblzu424wp-116029677818.asia-southeast1.run.app/privacy-policy",
                                toastMessage = "Privacy Policy Link copied!"
                            )
                        }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    Screen12SettingsNavigationRow(
                        icon = Icons.Default.Gavel,
                        title = "Terms & AI Content Safety",
                        trailingValue = null,
                        showChevron = true,
                        textColor = textPrimary,
                        secondaryColor = textSecondary,
                        onClick = { showTermsAndAiDialog = true }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

                    if (!isGuestUser) {
                        Screen12SettingsNavigationRow(
                            icon = Icons.Default.DeleteForever,
                            title = "Delete Account & Data",
                            trailingValue = null,
                            showChevron = true,
                            textColor = Color(0xFFE11D48),
                            secondaryColor = Color(0xFFE11D48),
                            iconTint = Color(0xFFE11D48),
                            onClick = { showDeleteAccountDialog = true }
                        )
                        HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onLogout)
                            .padding(vertical = 14.dp)
                            .testTag("logout_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isGuestUser) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout,
                            contentDescription = if (isGuestUser) "Sign In" else "Log out",
                            tint = if (isGuestUser) Color(0xFF0EA5E9) else Color(0xFFE11D48),
                            modifier = Modifier.size(21.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (isGuestUser) "Sign In / Create Account" else "Log out",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = if (isGuestUser) Color(0xFF0EA5E9) else Color(0xFFE11D48)
                        )
                    }
                }
            }
        }
        }

        if (showGoPremiumModal && !isPremiumUser) {
            GoPremiumModalDialog(
                isPremiumUser = false,
                customQrCodeUri = customQrCodeUri,
                onUpdateCustomQrCode = onUpdateCustomQrCode,
                onOpenPremiumUpload = {
                    showGoPremiumModal = false
                    showUploadWallpaperModal = true
                },
                onUpgradeClick = { verifiedUtr ->
                    onUnlockPremiumViaQr(verifiedUtr)
                    showGoPremiumModal = false
                },
                onDismiss = { showGoPremiumModal = false }
            )
        }

        if (showUploadWallpaperModal) {
            if (isPremiumUser) {
                PremiumUploadWallpaperDialog(
                    onUploadWallpaper = { uri, title, cat, desc ->
                        onUploadPremiumWallpaper(uri, title, cat, desc)
                        showUploadWallpaperModal = false
                    },
                    onDismiss = { showUploadWallpaperModal = false }
                )
            } else {
                showUploadWallpaperModal = false
                showGoPremiumModal = true
            }
        }

        if (showCreatorEarningsModal) {
            CreatorEarningsModalDialog(
                userName = displayName,
                uploadedWallpapers = userUploadedWallpapers,
                totalDownloadsCount = downloadsCount,
                totalFavoritesCount = favoritesCount,
                onUploadMoreClick = {
                    showCreatorEarningsModal = false
                    showUploadWallpaperModal = true
                },
                onRequestPayout = { upiId, amountStr ->
                    showCreatorEarningsModal = false
                    onNotifyMessage("Payout request of $amountStr submitted to UPI ID: $upiId!")
                },
                onDismiss = { showCreatorEarningsModal = false }
            )
        }

        if (showSplashPreviewOverlay) {
            DeveloperAboutAppPillDialog(
                onDismiss = { showSplashPreviewOverlay = false },
                onShareApp = {
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Pixora 4K & 8K Wallpapers by Divyansh")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "🔥 Download Pixora 4K & 8K Liquid Glass Wallpapers by Divyansh (Developer / Hacker)! Upgrade to Premium to upload 4K wallpapers & earn money 💸\n\nYouTube: https://youtube.com/@nkphonk1?si=LdwDyXDIgKK6JWiW\nTelegram: https://t.me/SUPPER_SAIYAN_PHONK_NK"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Pixora 4K App"))
                    } catch (_: Exception) {
                    }
                }
            )
        }

        if (showPrivacyPolicyDialog) {
            PlayStorePrivacyPolicyDialog(onDismiss = { showPrivacyPolicyDialog = false })
        }
        if (showTermsAndAiDialog) {
            PlayStoreTermsAndAiPolicyDialog(
                onDismiss = { showTermsAndAiDialog = false },
                onReportSubmitted = { msg -> onNotifyMessage(msg) }
            )
        }
        if (showDeleteAccountDialog) {
            PlayStoreDeleteAccountDialog(
                userEmail = displayEmail,
                onConfirmDelete = {
                    showDeleteAccountDialog = false
                    onDeleteAccountAndData()
                },
                onDismiss = { showDeleteAccountDialog = false }
            )
        }
        if (showEmailCenterDialog) {
            PlayStoreEmailCenterDialog(
                currentName = displayName,
                currentEmail = displayEmail,
                onSaveProfile = { newName, newEmail ->
                    onUpdateEmailProfile(newName, newEmail)
                },
                onSendSupportEmail = { subject, body ->
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:d99312907@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, subject.ifBlank { "4K Wallpapers Support" })
                            putExtra(
                                Intent.EXTRA_TEXT,
                                body.ifBlank { "Hello 4K Wallpapers Team,\n\nAccount Email: $displayEmail\n" }
                            )
                        }
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    } catch (_: Exception) {
                        onNotifyMessage("Support message queued for d99312907@gmail.com")
                    }
                },
                onDismiss = { showEmailCenterDialog = false }
            )
        }
    }
}

@Composable
private fun Screen12SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    textColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (glass.isDark) Color(0xFFC4B5FD) else Color(0xFF28224A),
                modifier = Modifier.size(21.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.2.sp
                ),
                color = textColor
            )
        }

        FourKPurpleToggleSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun Screen12SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    trailingValue: String?,
    showChevron: Boolean,
    textColor: Color,
    secondaryColor: Color,
    iconTint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    val resolvedIconTint = if (iconTint != Color.Unspecified) {
        iconTint
    } else if (glass.isDark) {
        Color(0xFFC4B5FD)
    } else {
        Color(0xFF28224A)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = resolvedIconTint,
                modifier = Modifier.size(21.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.2.sp
                ),
                color = textColor
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!trailingValue.isNullOrBlank()) {
                Text(
                    text = trailingValue,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.8.sp
                    ),
                    color = secondaryColor
                )
            }
            if (showChevron) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = secondaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// =============================================================================
// SHARED GRID CARD (Used by UserAiStudioScreen & Admin Screens)
// =============================================================================

@Composable
fun WallpaperGridCard(
    wallpaper: WallpaperItem,
    isFavorited: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    cardHeight: Dp = 228.dp
) {
    val appDesign = LocalAppDesignStyle.current
    val cornerShape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .height(cardHeight)
            .testTag("wallpaper_card_${wallpaper.id}")
            .clip(cornerShape)
            .border(1.dp, Color.White.copy(alpha = 0.65f), cornerShape)
            .premiumSmoothClick(pressedScale = 0.97f, onClick = onClick)
    ) {
        WallxAsyncImage(
            imageUrl = wallpaper.imageUrl.ifBlank { wallpaper.thumbnailUrl },
            fallbackImageUrl = wallpaper.thumbnailUrl,
            contentDescription = wallpaper.title,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        LiquidGlassCircularButton(
            icon = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite ${wallpaper.title}",
            onClick = onToggleFavorite,
            size = 36.dp,
            iconSize = 17.dp,
            isActive = isFavorited,
            activeTint = RoseGlow,
            tint = if (isFavorited) RoseGlow else Color(0xFF141824),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.72f)
                .padding(start = 12.dp, bottom = 10.dp)
        ) {
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CreatorEarningsModalDialog(
    userName: String,
    uploadedWallpapers: List<WallpaperItem>,
    totalDownloadsCount: Int,
    totalFavoritesCount: Int,
    onUploadMoreClick: () -> Unit,
    onRequestPayout: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val uploadsCount = uploadedWallpapers.size
    val wallViews = uploadedWallpapers.sumOf { it.viewCount }
    val wallDownloads = uploadedWallpapers.sumOf { it.downloadCount }
    val wallFavorites = uploadedWallpapers.sumOf { it.favoriteCount }
    val welcomeBonus = 0.0 // starts from $0.00
    // Real View & Activity Monetization: 3k views = $1.00, 5k uploads = $1.00, 1k downloads = $2.00
    val viewEarnings = wallViews / 3000.0
    val uploadEarnings = uploadsCount / 5000.0
    val downloadEarnings = (wallDownloads / 1000.0) * 2.0
    val totalBalance = welcomeBonus + viewEarnings + uploadEarnings + downloadEarnings
    val formattedBalance = String.format(Locale.US, "$%.2f", totalBalance)

    var upiIdInput by remember { mutableStateOf("") }
    var payoutError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.64f))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 18.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .ios27LiquidStretch()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F292A).copy(alpha = 0.96f),
                                Color(0xFF0A1D24).copy(alpha = 0.98f),
                                Color(0xFF071318).copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(
                        1.6.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF34D399),
                                Color(0xFFFFD426),
                                Color(0xFF38BDF8)
                            )
                        ),
                        RoundedCornerShape(32.dp)
                    )
                    .clickable(enabled = false) {}
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
                    .testTag("creator_earnings_modal"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.24f))
                                .border(1.dp, Color(0xFF34D399), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Paid,
                                contentDescription = null,
                                tint = Color(0xFFFFD426),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Creator Earnings 💸",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "$userName • Verified Creator Account",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6EE7B7)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                            .premiumSmoothClick(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Total Earnings Balance Hero Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF059669), Color(0xFF10B981))
                            )
                        )
                        .border(1.2.dp, Color(0xFFFFD426), RoundedCornerShape(24.dp))
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AVAILABLE CREATOR BALANCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White.copy(alpha = 0.90f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedBalance,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp
                        ),
                        color = Color(0xFFFFDF38)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3k Views = $1.00 • 5k Uploads = $1.00 • 1k Downloads = $2.00",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.92f),
                        textAlign = TextAlign.Center
                    )
                }

                // Live Breakdown Matrix (4 Metrics: Views, Uploads, Downloads, Likes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$wallViews",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "Views 👁️",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "+$${String.format(Locale.US, "%.2f", viewEarnings)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = Color(0xFF4ADE80)
                        )
                        Text(
                            text = "3k=$1",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFFFFDF38)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(1.dp, Color(0xFFFFDF38).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$uploadsCount",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFFFFDF38)
                        )
                        Text(
                            text = "Uploads 📤",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "+$${String.format(Locale.US, "%.2f", uploadEarnings)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = Color(0xFF4ADE80)
                        )
                        Text(
                            text = "5k=$1",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFFFFDF38)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$wallDownloads",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF34D399)
                        )
                        Text(
                            text = "Downloads ⬇️",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "+$${String.format(Locale.US, "%.2f", downloadEarnings)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = Color(0xFF4ADE80)
                        )
                        Text(
                            text = "1k=$2",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFFFFDF38)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(1.dp, Color(0xFFFB7185).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$wallFavorites",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFFFB7185)
                        )
                        Text(
                            text = "Likes ❤️",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "$wallFavorites",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = Color(0xFF4ADE80)
                        )
                        Text(
                            text = "Real",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFFFFDF38)
                        )
                    }
                }

                // Per-Wallpaper Real View Earnings Breakdown
                if (uploadedWallpapers.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(18.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "YOUR WALLPAPERS & LIVE VIEW EARNINGS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color(0xFFFFDF38)
                        )
                        uploadedWallpapers.take(6).forEach { wall ->
                            val wallViewReward = wall.viewCount / 3000.0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.4f))
                                    ) {
                                        WallxAsyncImage(
                                            imageUrl = wall.imageUrl.ifBlank { wall.thumbnailUrl },
                                            contentDescription = wall.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = wall.title.ifBlank { "4K Wallpaper" },
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "👁️ ${wall.viewCount} views • ⬇️ ${wall.downloadCount} dl",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.70f)
                                        )
                                    }
                                }
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", wallViewReward)}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }
                }

                // Button to Upload More Wallpapers & Earn More
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                            )
                        )
                        .border(1.2.dp, Color.White.copy(alpha = 0.75f), CircleShape)
                        .premiumSmoothClick(onClick = onUploadMoreClick),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upload New 4K Wallpaper (2 TB Vault)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    }
                }

                // Withdraw via UPI Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Withdraw Earnings to UPI / Bank",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(1.dp, Color.White.copy(alpha = 0.40f), CircleShape)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (upiIdInput.isEmpty()) {
                            Text(
                                text = "Enter your UPI ID (e.g. yourname@upi)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                        }
                        BasicTextField(
                            value = upiIdInput,
                            onValueChange = {
                                upiIdInput = it
                                payoutError = null
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            cursorBrush = SolidColor(Color(0xFFFFDF38)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (payoutError != null) {
                        Text(
                            text = payoutError!!,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF8FA3)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFE047), Color(0xFFFF9900))
                                )
                            )
                            .premiumSmoothClick {
                                val cleanedUpi = upiIdInput.trim()
                                if (!cleanedUpi.contains("@") || cleanedUpi.length < 5) {
                                    payoutError = "Please enter a valid UPI ID (e.g. name@upi) to withdraw $formattedBalance."
                                } else {
                                    onRequestPayout(cleanedUpi, formattedBalance)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Withdraw $formattedBalance",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF1A1033)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Screen opened when user clicks "Earn" button in the bottom navigation dock.
 * Features two dedicated sections:
 *  1) Earn Section: Full Creator Earnings dashboard, rates, per-wallpaper breakdown & UPI withdrawal.
 *  2) Upload Section: 2 TB Cloud Vault wallpaper uploader & complete grid of user-uploaded wallpapers.
 */
@Composable
fun UserUploadsManagementScreen(
    userName: String = "Creator",
    uploadedWallpapers: List<WallpaperItem>,
    totalDownloadsCount: Int,
    totalFavoritesCount: Int,
    isUploading: Boolean = false,
    availableCategories: List<String> = emptyList(),
    onUploadWallpaper: (Uri, String, String, String) -> Unit,
    onRequestPayout: (String, String) -> Unit = { _, _ -> },
    onSelectWallpaper: (WallpaperItem) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToHome() }
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Earn Section, 1 = Upload Section
    var showUploadModal by remember { mutableStateOf(false) }
    var upiIdInput by remember { mutableStateOf("") }
    var payoutError by remember { mutableStateOf<String?>(null) }
    var payoutSubmittedMsg by remember { mutableStateOf<String?>(null) }

    val uploadsCount = uploadedWallpapers.size
    val totalViews = uploadedWallpapers.sumOf { it.viewCount }
    val totalDl = uploadedWallpapers.sumOf { it.downloadCount }
    val totalFav = uploadedWallpapers.sumOf { it.favoriteCount }

    // Real Earnings Formula: 3k views = $1, 5k uploads = $1, 1k dl = $2, starting at $0.00
    val viewReward = totalViews / 3000.0
    val uploadReward = uploadsCount / 5000.0
    val downloadReward = (totalDl / 1000.0) * 2.0
    val totalEarnings = viewReward + uploadReward + downloadReward
    val formattedEarnings = String.format(Locale.US, "$%.2f", totalEarnings)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .testTag("user_uploads_management_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                            .premiumSmoothClick(onClick = onBackToHome)
                            .testTag("uploads_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (selectedSection == 0) "Earn & Creator Wallet" else "Upload • 2 TB Vault",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "$userName • $uploadsCount Uploads • $totalViews Views",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF34D399)
                        )
                    }
                }

                // Live Balance Chip (tapping switches to Earn Section)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF059669), Color(0xFF10B981))
                            )
                        )
                        .border(1.2.dp, Color(0xFFFFD426), CircleShape)
                        .premiumSmoothClick { selectedSection = 0 }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("uploads_check_earnings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = null,
                            tint = Color(0xFFFFD426),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedEarnings,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    }
                }
            }

            // Segmented Switcher: [ 💰 Earn Section | 📤 Upload Section ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 10.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (selectedSection == 0) {
                                Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
                            } else {
                                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .premiumSmoothClick { selectedSection = 0 }
                        .testTag("earn_hub_tab_earn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = "Earn Details",
                            tint = if (selectedSection == 0) Color(0xFFFFD426) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Earn Details",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (selectedSection == 0) Color.White else Color.White.copy(alpha = 0.72f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (selectedSection == 1) {
                                Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF3B82F6)))
                            } else {
                                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .premiumSmoothClick { selectedSection = 1 }
                        .testTag("earn_hub_tab_upload"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload Wallpapers",
                            tint = Color.White.copy(alpha = if (selectedSection == 1) 1f else 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Upload ($uploadsCount)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (selectedSection == 1) Color.White else Color.White.copy(alpha = 0.72f)
                        )
                    }
                }
            }

            if (selectedSection == 0) {
                // =====================================================================
                // SECTION 1: COMPLETE EARNING DETAILS + RATES + PER-WALLPAPER + PAYOUT
                // =====================================================================
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("earn_details_section_list")
                ) {
                    // 1. Total Available Balance Hero Card
                    item(span = { GridItemSpan(2) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(26.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF065F46), Color(0xFF064E3B), Color(0xFF042F2E))
                                    )
                                )
                                .border(1.5.dp, Color(0xFFFFD426), RoundedCornerShape(26.dp))
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "TOTAL CREATOR EARNINGS BALANCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFA7F3D0)
                            )
                            Text(
                                text = formattedEarnings,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 40.sp
                                ),
                                color = Color(0xFFFFDF38)
                            )
                            Text(
                                text = "3K Views = $1.00  •  5K Uploads = $1.00  •  1K Downloads = $2.00",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White.copy(alpha = 0.90f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                                    .premiumSmoothClick { selectedSection = 1 }
                                    .testTag("earn_to_upload_switch_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Open Upload Section & Earn More",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // 2. 4-Card Breakdown Matrix
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Views Revenue 👁️",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                            Text(
                                text = "$totalViews views",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "+$${String.format(Locale.US, "%.2f", viewReward)} (3k = $1)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF34D399)
                            )
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFF34D399).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Downloads Revenue ⬇️",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                            Text(
                                text = "$totalDl downloads",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFF34D399)
                            )
                            Text(
                                text = "+$${String.format(Locale.US, "%.2f", downloadReward)} (1k = $2)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF34D399)
                            )
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFFFFDF38).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Uploads Bonus 📤",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                            Text(
                                text = "$uploadsCount wallpapers",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFFFFDF38)
                            )
                            Text(
                                text = "+$${String.format(Locale.US, "%.2f", uploadReward)} (5k = $1)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF34D399)
                            )
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFFFB7185).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Community Likes ❤️",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                            Text(
                                text = "$totalFav likes",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFFFB7185)
                            )
                            Text(
                                text = "$totalDownloadsCount saved in vault",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White.copy(alpha = 0.70f)
                            )
                        }
                    }

                    // 3. Per-Wallpaper Live Earning Breakdown
                    item(span = { GridItemSpan(2) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "EARNING BREAKDOWN BY WALLPAPER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = Color(0xFFFFDF38)
                            )
                            if (uploadedWallpapers.isEmpty()) {
                                Text(
                                    text = "No uploaded wallpapers yet. Switch to the Upload tab to publish your first 4K wallpaper and track real-time views & earnings here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            } else {
                                uploadedWallpapers.forEach { wall ->
                                    val wallEarning = (wall.viewCount / 3000.0) + ((wall.downloadCount / 1000.0) * 2.0)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .premiumSmoothClick { onSelectWallpaper(wall) }
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color.Black.copy(alpha = 0.4f))
                                            ) {
                                                WallxAsyncImage(
                                                    imageUrl = wall.imageUrl.ifBlank { wall.thumbnailUrl },
                                                    contentDescription = wall.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = wall.title.ifBlank { "4K Wallpaper" },
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "👁️ ${wall.viewCount} • ⬇️ ${wall.downloadCount} • ❤️ ${wall.favoriteCount}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.70f)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "$${String.format(Locale.US, "%.2f", wallEarning)}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = Color(0xFF34D399)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Direct UPI Withdrawal & Payout Card
                    item(span = { GridItemSpan(2) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.White.copy(alpha = 0.07f))
                                .border(1.dp, Color(0xFF34D399).copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Withdraw Earnings to UPI / Bank",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            Text(
                                text = "Instant UPI payouts to GPay, PhonePe, Paytm, or BHIM.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (upiIdInput.isEmpty()) {
                                    Text(
                                        text = "Enter your UPI ID (e.g. name@upi)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.52f)
                                    )
                                }
                                BasicTextField(
                                    value = upiIdInput,
                                    onValueChange = {
                                        upiIdInput = it
                                        payoutError = null
                                        payoutSubmittedMsg = null
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    cursorBrush = SolidColor(Color(0xFFFFDF38)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("earn_upi_input")
                                )
                            }

                            if (payoutError != null) {
                                Text(
                                    text = payoutError!!,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFF8FA3)
                                )
                            }
                            if (payoutSubmittedMsg != null) {
                                Text(
                                    text = payoutSubmittedMsg!!,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF34D399)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFFFE047), Color(0xFFFF9900))
                                        )
                                    )
                                    .premiumSmoothClick {
                                        val cleanedUpi = upiIdInput.trim()
                                        if (!cleanedUpi.contains("@") || cleanedUpi.length < 5) {
                                            payoutError = "Please enter a valid UPI ID (e.g. name@upi) to withdraw $formattedEarnings."
                                        } else {
                                            onRequestPayout(cleanedUpi, formattedEarnings)
                                            payoutSubmittedMsg = "Payout request of $formattedEarnings sent for $cleanedUpi!"
                                        }
                                    }
                                    .testTag("earn_withdraw_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Withdraw $formattedEarnings via UPI",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color(0xFF1A1033)
                                )
                            }
                        }
                    }
                }
            } else {
                // =====================================================================
                // SECTION 2: UPLOAD WALLPAPER + ALL UPLOADED WALLPAPERS GALLERY
                // =====================================================================
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("upload_wallpapers_section_list")
                ) {
                    // Hero Banner (Spans across all 2 columns)
                    item(span = { GridItemSpan(2) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(26.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF1E1B4B).copy(alpha = 0.95f),
                                            Color(0xFF0F172A).copy(alpha = 0.98f)
                                        )
                                    )
                                )
                                .border(
                                    1.5.dp,
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFF34D399))
                                    ),
                                    RoundedCornerShape(26.dp)
                                )
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "2 TB STORAGE VAULT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = Color(0xFF818CF8)
                                    )
                                    Text(
                                        text = "$uploadsCount Wallpapers Uploaded",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.20f))
                                        .border(1.dp, Color(0xFF34D399), CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "2 TB Ready",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF34D399)
                                    )
                                }
                            }

                            // Big Button to Upload New Wallpaper to 2 TB Storage
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF6366F1), Color(0xFF3B82F6), Color(0xFF06B6D4))
                                        )
                                    )
                                    .border(1.2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                    .premiumSmoothClick { showUploadModal = true }
                                    .testTag("upload_new_wallpaper_hero_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isUploading) "Uploading to 2 TB Vault..." else "Upload New 4K Wallpaper (2 TB Vault)",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Section Title: My Uploaded Wallpapers
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Uploaded Wallpapers (${uploadedWallpapers.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Tap to preview",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.60f)
                            )
                        }
                    }

                    if (uploadedWallpapers.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                                    .padding(vertical = 36.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = "No Wallpapers Uploaded Yet",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Your 2 TB permanent storage is ready. Tap the upload button above to publish your first 4K masterpiece and earn from views & downloads!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.70f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(uploadedWallpapers, key = { it.id }) { wall ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                                    .premiumSmoothClick { onSelectWallpaper(wall) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    WallxAsyncImage(
                                        imageUrl = wall.imageUrl.ifBlank { wall.thumbnailUrl },
                                        contentDescription = "",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(16.dp)
                                    )
                                    WallxAsyncImage(
                                        imageUrl = wall.imageUrl.ifBlank { wall.thumbnailUrl },
                                        contentDescription = wall.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(Alignment.TopStart)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.55f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = wall.category.ifBlank { "4K" },
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                            color = Color.White
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = wall.title.ifBlank { "4K Wallpaper" },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "👁️ ${wall.viewCount} • ⬇️ ${wall.downloadCount}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                        Text(
                                            text = "❤️ ${wall.favoriteCount}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color(0xFFFB7185)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upload Wallpaper Dialog
        if (showUploadModal) {
            PremiumUploadWallpaperDialog(
                isUploading = isUploading,
                availableCategories = availableCategories,
                onUploadWallpaper = { uri, title, cat, desc ->
                    onUploadWallpaper(uri, title, cat, desc)
                    showUploadModal = false
                },
                onDismiss = { showUploadModal = false }
            )
        }
    }
}

