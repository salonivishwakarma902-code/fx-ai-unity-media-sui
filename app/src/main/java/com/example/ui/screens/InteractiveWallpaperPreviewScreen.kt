package com.example.ui.screens

import android.app.WallpaperManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WallpaperItem
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassSlider
import com.example.ui.components.WallxAsyncImage
import com.example.ui.components.elasticLiquidGlass
import com.example.ui.components.progressiveTopHighToLowBlur
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.LocalLiquidGlassOptics
import com.example.ui.theme.NeonViolet
import com.example.ui.viewmodel.ActiveDownloadState
import com.example.util.WallpaperDownloader
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class InteractivePreviewMode(val label: String, val testTag: String) {
    LOCK_SCREEN("Lock Screen", "preview_mode_lock"),
    HOME_SCREEN("Home & Dock", "preview_mode_home"),
    GLASS_LENS("Glass Lens", "preview_mode_lens")
}

/**
 * Full-screen interactive wallpaper preview & testing studio.
 * Allows users to test real-time Gaussian Blur, Progressive Top High-to-Low Blur,
 * Dimming, and Pure Liquid Glass optics across Lock Screen, Home Screen & Dock,
 * and a draggable Liquid Glass Inspection Lens before applying or saving the wallpaper.
 */
@Composable
fun InteractiveWallpaperPreviewScreen(
    wallpaper: WallpaperItem,
    activeDownload: ActiveDownloadState?,
    onDownloadProgress: (String?, Float, String) -> Unit,
    onRecordDownload: (WallpaperItem) -> Unit,
    onNotifySuccess: (String) -> Unit,
    onNotifyError: (String) -> Unit,
    onSyncOpticsToApp: ((blurRadiusDp: Float, upperBlurDp: Float, glassOpacity: Float, specularShine: Float) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOptics = LocalLiquidGlassOptics.current

    // Live interactive testing parameters initialized with rich defaults
    var previewMode by remember { mutableStateOf(InteractivePreviewMode.LOCK_SCREEN) }
    var previewBlurDp by remember { mutableFloatStateOf(currentOptics.blurRadiusDp.coerceIn(0f, 36f)) }
    var previewTopBlurDp by remember { mutableFloatStateOf(currentOptics.upperBlurStrength.coerceIn(0f, 36f)) }
    var previewGlassOpacity by remember { mutableFloatStateOf(currentOptics.glassOpacity.coerceIn(0.15f, 0.90f)) }
    var previewSpecularShine by remember { mutableFloatStateOf(currentOptics.specularShine.coerceIn(0.15f, 1.0f)) }
    var previewDimAlpha by remember { mutableFloatStateOf(0.10f) }

    var isCompareOriginal by remember { mutableStateOf(false) }
    var isHudVisible by remember { mutableStateOf(true) }
    var isControlsExpanded by remember { mutableStateOf(true) }

    // Effective values when comparing original vs. tested blur & glass
    val effectiveBlurDp = if (isCompareOriginal) 0f else previewBlurDp
    val effectiveTopBlurDp = if (isCompareOriginal) 0f else previewTopBlurDp
    val effectiveDimAlpha = if (isCompareOriginal) 0f else previewDimAlpha

    BackHandler {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("interactive_wallpaper_preview_screen")
    ) {
        // =========================================================================
        // 1. FULL-SCREEN LIVE WALLPAPER CANVAS WITH REAL-TIME GAUSSIAN BLUR
        // =========================================================================
        // 1. Ambient Blurred Backdrop: Fills entire screen with blurred wallpaper to create beautiful blurred sides
        WallxAsyncImage(
            imageUrl = wallpaper.imageUrl,
            contentDescription = "",
            contentScale = ContentScale.Crop,
            isFullQualityInspection = false,
            modifier = Modifier
                .fillMaxSize()
                .blur(32.dp)
        )

        // Dark atmospheric overlay for visual focus
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )

        // 2. Full Edge-to-Edge Foreground Wallpaper
        WallxAsyncImage(
            imageUrl = wallpaper.imageUrl,
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            isFullQualityInspection = true,
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    // Tapping the background toggles HUD when collapsed or exits compare mode
                    if (isCompareOriginal) {
                        isCompareOriginal = false
                    } else if (!isControlsExpanded) {
                        isHudVisible = !isHudVisible
                    }
                }
        )

        // =========================================================================
        // 2. LIVE WALLPAPER DIMMING SCRIM
        // =========================================================================
        if (effectiveDimAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = effectiveDimAlpha.coerceIn(0f, 0.75f)))
            )
        }

        // =========================================================================
        // 3. LIVE PROGRESSIVE TOP HIGH-TO-LOW BLUR OVERLAY
        // =========================================================================
        if (effectiveTopBlurDp > 0.5f) {
            LivePreviewTopHighToLowBlur(
                topBlurStrengthDp = effectiveTopBlurDp,
                glassOpacity = previewGlassOpacity,
                specularShine = previewSpecularShine,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // =========================================================================
        // 4. INTERACTIVE FULL-SCREEN MOCKUP OVERLAYS (LOCK / HOME & DOCK / LENS)
        // =========================================================================
        AnimatedVisibility(
            visible = isHudVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (previewMode) {
                    InteractivePreviewMode.LOCK_SCREEN -> {
                        LockScreenInteractiveOverlay(
                            wallpaperTitle = wallpaper.title,
                            blurRadiusDp = effectiveBlurDp,
                            glassOpacity = previewGlassOpacity,
                            specularShine = previewSpecularShine,
                            isControlsExpanded = isControlsExpanded
                        )
                    }

                    InteractivePreviewMode.HOME_SCREEN -> {
                        HomeScreenInteractiveOverlay(
                            blurRadiusDp = effectiveBlurDp,
                            glassOpacity = previewGlassOpacity,
                            specularShine = previewSpecularShine,
                            isControlsExpanded = isControlsExpanded
                        )
                    }

                    InteractivePreviewMode.GLASS_LENS -> {
                        DraggableLiquidGlassLensOverlay(
                            blurRadiusDp = effectiveBlurDp,
                            topBlurDp = effectiveTopBlurDp,
                            glassOpacity = previewGlassOpacity,
                            specularShine = previewSpecularShine
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 5. TOP FLOATING LIQUID GLASS NAVIGATION & MODE SWITCHER BAR
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back / Close Interactive Preview Button
                PreviewPureGlassIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Exit Interactive Preview",
                    glassOpacity = previewGlassOpacity,
                    specularShine = previewSpecularShine,
                    onClick = onDismiss,
                    testTag = "close_interactive_preview_button"
                )

                // Live Optical Status Pill Badge
                Box(
                    modifier = Modifier
                        .previewPureLiquidGlassSurface(
                            cornerRadius = 999.dp,
                            glassOpacity = previewGlassOpacity,
                            specularShine = previewSpecularShine
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isCompareOriginal) {
                                "Original (0 dp Blur)"
                            } else {
                                "Blur ${previewBlurDp.roundToInt()}dp • Top ${previewTopBlurDp.roundToInt()}dp • Glass ${(previewGlassOpacity * 100).roundToInt()}%"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right Controls: Compare Original & Hide/Show HUD
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PreviewPureGlassIconButton(
                        icon = Icons.Default.Compare,
                        contentDescription = "Compare Original vs Blurred",
                        glassOpacity = if (isCompareOriginal) 0.75f else previewGlassOpacity,
                        specularShine = previewSpecularShine,
                        isActive = isCompareOriginal,
                        onClick = { isCompareOriginal = !isCompareOriginal },
                        testTag = "preview_compare_original_button"
                    )

                    PreviewPureGlassIconButton(
                        icon = if (isHudVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Preview Overlays",
                        glassOpacity = previewGlassOpacity,
                        specularShine = previewSpecularShine,
                        onClick = { isHudVisible = !isHudVisible },
                        testTag = "preview_toggle_hud_button"
                    )
                }
            }

            // Mode Switcher Row (Lock Screen | Home & Dock | Glass Lens)
            AnimatedVisibility(visible = isHudVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .previewPureLiquidGlassSurface(
                            cornerRadius = 999.dp,
                            glassOpacity = previewGlassOpacity,
                            specularShine = previewSpecularShine
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InteractivePreviewMode.entries.forEach { mode ->
                        val selected = previewMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag(mode.testTag)
                                .clip(CircleShape)
                                .background(
                                    if (selected) {
                                        Color.White.copy(alpha = 0.28f)
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .then(
                                    if (selected) {
                                        Modifier.border(
                                            1.dp,
                                            Color.White.copy(alpha = 0.85f),
                                            CircleShape
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { previewMode = mode },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.White else Color.White.copy(alpha = 0.72f),
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 6. BOTTOM INTERACTIVE BLUR & LIQUID GLASS CONTROLS + APPLY SHEET
        // =========================================================================
        AnimatedVisibility(
            visible = isHudVisible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .previewPureLiquidGlassSurface(
                        cornerRadius = 30.dp,
                        glassOpacity = (previewGlassOpacity * 0.95f).coerceIn(0.22f, 0.82f),
                        specularShine = previewSpecularShine
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                // Sheet Header: Title + Expand/Collapse Toggle + Reset Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isControlsExpanded = !isControlsExpanded }
                            .testTag("preview_toggle_controls_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Material 3 Exclusive Preview Studio",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isControlsExpanded) {
                                    "Adjust live preview before setting wallpaper"
                                } else {
                                    "Tap to expand live controls • Ready to set wallpaper"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Reset Sliders Pill
                        Box(
                            modifier = Modifier
                                .testTag("preview_reset_optics_button")
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                                .clickable {
                                    previewBlurDp = 0f
                                    previewTopBlurDp = 14f
                                    previewGlassOpacity = 0.36f
                                    previewSpecularShine = 0.82f
                                    previewDimAlpha = 0.08f
                                    isCompareOriginal = false
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Reset",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Collapse / Expand Button
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                                .clickable { isControlsExpanded = !isControlsExpanded },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isControlsExpanded) {
                                    Icons.Default.KeyboardArrowDown
                                } else {
                                    Icons.Default.KeyboardArrowUp
                                },
                                contentDescription = "Expand or Collapse Controls",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Expanded Live Optical Presets + iOS-Style Pill Sliders
                AnimatedVisibility(visible = isControlsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // One-Tap Optical Blur & Glass Presets Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PreviewOpticalPresetChip(
                                label = "Crystal 0dp",
                                active = previewBlurDp < 0.5f && !isCompareOriginal,
                                modifier = Modifier.weight(1f),
                                testTag = "preview_preset_crystal",
                                onClick = {
                                    isCompareOriginal = false
                                    previewBlurDp = 0f
                                    previewTopBlurDp = 10f
                                    previewGlassOpacity = 0.28f
                                    previewSpecularShine = 0.90f
                                    previewDimAlpha = 0.04f
                                }
                            )
                            PreviewOpticalPresetChip(
                                label = "Soft 6dp",
                                active = previewBlurDp in 4f..8f && !isCompareOriginal,
                                modifier = Modifier.weight(1f),
                                testTag = "preview_preset_soft",
                                onClick = {
                                    isCompareOriginal = false
                                    previewBlurDp = 6f
                                    previewTopBlurDp = 16f
                                    previewGlassOpacity = 0.36f
                                    previewSpecularShine = 0.85f
                                    previewDimAlpha = 0.10f
                                }
                            )
                            PreviewOpticalPresetChip(
                                label = "Frosted 14dp",
                                active = previewBlurDp in 11f..17f && !isCompareOriginal,
                                modifier = Modifier.weight(1f),
                                testTag = "preview_preset_frosted",
                                onClick = {
                                    isCompareOriginal = false
                                    previewBlurDp = 14f
                                    previewTopBlurDp = 22f
                                    previewGlassOpacity = 0.48f
                                    previewSpecularShine = 0.92f
                                    previewDimAlpha = 0.16f
                                }
                            )
                            PreviewOpticalPresetChip(
                                label = "Deep 26dp",
                                active = previewBlurDp >= 22f && !isCompareOriginal,
                                modifier = Modifier.weight(1f),
                                testTag = "preview_preset_deep",
                                onClick = {
                                    isCompareOriginal = false
                                    previewBlurDp = 26f
                                    previewTopBlurDp = 30f
                                    previewGlassOpacity = 0.58f
                                    previewSpecularShine = 0.96f
                                    previewDimAlpha = 0.22f
                                }
                            )
                        }

                        // Slider 1: Specular Rim Shine
                        LiquidGlassSlider(
                            label = "Material 3 Specular Highlight",
                            value = previewSpecularShine,
                            onValueChange = { previewSpecularShine = it },
                            valueRange = 0.15f..1.0f,
                            steps = 0,
                            valueText = "${(previewSpecularShine * 100).roundToInt()}%",
                            modifier = Modifier.testTag("preview_specular_slider")
                        )

                        // Slider 5: Ambient Contrast Dimming
                        LiquidGlassSlider(
                            label = "Wallpaper Contrast Dimming",
                            value = previewDimAlpha,
                            onValueChange = {
                                isCompareOriginal = false
                                previewDimAlpha = it
                            },
                            valueRange = 0f..0.65f,
                            steps = 0,
                            valueText = "${(previewDimAlpha * 100).roundToInt()}%",
                            modifier = Modifier.testTag("preview_dim_slider")
                        )
                    }
                }

                // Active Apply / Download Progress Indicator
                if (activeDownload != null && activeDownload.wallpaperId == wallpaper.id) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = activeDownload.statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                            Text(
                                text = "${(activeDownload.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { activeDownload.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.25f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // PRIMARY ACTION ROW: Set Home | Set Lock | Set Both (with tested Blur & Dimming baked in)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiquidGlassButton(
                        text = "Set Home",
                        icon = Icons.Default.PhoneAndroid,
                        glowColor = ElectricCyan,
                        filledAccent = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_apply_home_button"),
                        onClick = {
                            scope.launch {
                                onDownloadProgress(
                                    wallpaper.id,
                                    0.1f,
                                    "Applying (${previewBlurDp.roundToInt()}dp blur) to Home..."
                                )
                                val res = WallpaperDownloader.applyWallpaperWithEffects(
                                    context = context,
                                    wallpaper = wallpaper,
                                    blurRadiusDp = previewBlurDp,
                                    topBlurStrengthDp = previewTopBlurDp,
                                    dimAlpha = previewDimAlpha,
                                    targetFlags = WallpaperManager.FLAG_SYSTEM,
                                    onProgress = { p ->
                                        onDownloadProgress(
                                            wallpaper.id,
                                            p,
                                            "Applying (${previewBlurDp.roundToInt()}dp blur) to Home..."
                                        )
                                    }
                                )
                                onDownloadProgress(null, 1f, "")
                                res.onSuccess {
                                    onRecordDownload(wallpaper)
                                    onNotifySuccess(
                                        "Home Screen set (${previewBlurDp.roundToInt()}dp blur, ${previewTopBlurDp.roundToInt()}dp top blur)!"
                                    )
                                }.onFailure { err ->
                                    onNotifyError(err.localizedMessage ?: "Failed to set Home Screen")
                                }
                            }
                        }
                    )

                    LiquidGlassButton(
                        text = "Set Lock",
                        icon = Icons.Default.Lock,
                        glowColor = NeonViolet,
                        filledAccent = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_apply_lock_button"),
                        onClick = {
                            scope.launch {
                                onDownloadProgress(
                                    wallpaper.id,
                                    0.1f,
                                    "Applying (${previewBlurDp.roundToInt()}dp blur) to Lock..."
                                )
                                val res = WallpaperDownloader.applyWallpaperWithEffects(
                                    context = context,
                                    wallpaper = wallpaper,
                                    blurRadiusDp = previewBlurDp,
                                    topBlurStrengthDp = previewTopBlurDp,
                                    dimAlpha = previewDimAlpha,
                                    targetFlags = WallpaperManager.FLAG_LOCK,
                                    onProgress = { p ->
                                        onDownloadProgress(
                                            wallpaper.id,
                                            p,
                                            "Applying (${previewBlurDp.roundToInt()}dp blur) to Lock..."
                                        )
                                    }
                                )
                                onDownloadProgress(null, 1f, "")
                                res.onSuccess {
                                    onRecordDownload(wallpaper)
                                    onNotifySuccess(
                                        "Lock Screen set (${previewBlurDp.roundToInt()}dp blur, ${previewTopBlurDp.roundToInt()}dp top blur)!"
                                    )
                                }.onFailure { err ->
                                    onNotifyError(err.localizedMessage ?: "Failed to set Lock Screen")
                                }
                            }
                        }
                    )

                    LiquidGlassButton(
                        text = "Set Both",
                        icon = Icons.Default.Check,
                        glowColor = EmeraldGlow,
                        filledAccent = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_apply_both_button"),
                        onClick = {
                            scope.launch {
                                onDownloadProgress(
                                    wallpaper.id,
                                    0.1f,
                                    "Applying to Home & Lock..."
                                )
                                val res = WallpaperDownloader.applyWallpaperWithEffects(
                                    context = context,
                                    wallpaper = wallpaper,
                                    blurRadiusDp = previewBlurDp,
                                    topBlurStrengthDp = previewTopBlurDp,
                                    dimAlpha = previewDimAlpha,
                                    targetFlags = WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK,
                                    onProgress = { p ->
                                        onDownloadProgress(
                                            wallpaper.id,
                                            p,
                                            "Applying to Home & Lock..."
                                        )
                                    }
                                )
                                onDownloadProgress(null, 1f, "")
                                res.onSuccess {
                                    onRecordDownload(wallpaper)
                                    onNotifySuccess(
                                        "Applied to Home & Lock (${previewBlurDp.roundToInt()}dp blur)!"
                                    )
                                }.onFailure { err ->
                                    onNotifyError(err.localizedMessage ?: "Failed to apply wallpaper")
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SECONDARY ACTION ROW: Save Blurred Copy to Gallery | Sync Tested Glass Optics to App
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiquidGlassButton(
                        text = "Save with Effects",
                        icon = Icons.Default.FileDownload,
                        glowColor = ElectricCyan,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_save_button"),
                        onClick = {
                            scope.launch {
                                onDownloadProgress(
                                    wallpaper.id,
                                    0.1f,
                                    "Saving with ${previewBlurDp.roundToInt()}dp blur..."
                                )
                                val res = WallpaperDownloader.downloadWallpaperWithEffects(
                                    context = context,
                                    wallpaper = wallpaper,
                                    blurRadiusDp = previewBlurDp,
                                    topBlurStrengthDp = previewTopBlurDp,
                                    dimAlpha = previewDimAlpha,
                                    onProgress = { p ->
                                        onDownloadProgress(
                                            wallpaper.id,
                                            p,
                                            "Saving with ${previewBlurDp.roundToInt()}dp blur..."
                                        )
                                    }
                                )
                                onDownloadProgress(null, 1f, "")
                                res.onSuccess { msg ->
                                    onRecordDownload(wallpaper)
                                    onNotifySuccess(msg)
                                }.onFailure { err ->
                                    onNotifyError(err.localizedMessage ?: "Failed to save wallpaper")
                                }
                            }
                        }
                    )

                    if (onSyncOpticsToApp != null) {
                        LiquidGlassButton(
                            text = "Sync Glass to App",
                            icon = Icons.Default.Sync,
                            glowColor = AmberGlow,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_sync_optics_button"),
                            onClick = {
                                onSyncOpticsToApp(
                                    previewBlurDp,
                                    previewTopBlurDp,
                                    previewGlassOpacity,
                                    previewSpecularShine
                                )
                                onNotifySuccess(
                                    "Synced Material 3 Exclusive preview settings to Pixora!"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODE 1: LOCK SCREEN INTERACTIVE MOCKUP OVERLAY
// ============================================================================

@Composable
private fun LockScreenInteractiveOverlay(
    wallpaperTitle: String,
    blurRadiusDp: Float,
    glassOpacity: Float,
    specularShine: Float,
    isControlsExpanded: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(top = 114.dp, bottom = if (isControlsExpanded) 380.dp else 160.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Lock Icon + Date + Giant Pure Liquid Glass Time Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .previewPureLiquidGlassSurface(
                        cornerRadius = 999.dp,
                        glassOpacity = glassOpacity,
                        specularShine = specularShine
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Wednesday, November 5",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Translucent Refractive Lock Screen Clock
            Text(
                text = "09:41",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 82.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-2).sp
                ),
                color = Color.White.copy(alpha = (0.78f + specularShine * 0.20f).coerceIn(0.75f, 0.98f))
            )
        }

        // Interactive Pure Liquid Glass Lock Screen Notification Stack
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LockScreenGlassNotificationCard(
                icon = Icons.Default.AutoAwesome,
                appName = "WALLPERS • M3 EXCLUSIVE",
                timeAgo = "now",
                title = wallpaperTitle,
                body = "Material 3 Exclusive 120Hz Preview • Tap below to apply to Lock or Home Screen.",
                glassOpacity = glassOpacity,
                specularShine = specularShine
            )

            if (!isControlsExpanded) {
                LockScreenGlassNotificationCard(
                    icon = Icons.Default.Notifications,
                    appName = "MATERIAL 3 EXCLUSIVE",
                    timeAgo = "2m ago",
                    title = "120Hz ProMotion Smoothness Ready",
                    body = "Use the bottom studio to apply to Lock Screen, Home Screen, or both.",
                    glassOpacity = glassOpacity,
                    specularShine = specularShine
                )
            }
        }

        // Bottom Lock Screen Flashlight & Camera Pure Liquid Glass Orbs
        if (!isControlsExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PreviewPureGlassIconButton(
                    icon = Icons.Default.FlashlightOn,
                    contentDescription = "Lock Screen Flashlight",
                    glassOpacity = glassOpacity,
                    specularShine = specularShine,
                    size = 50.dp,
                    onClick = {}
                )

                Box(
                    modifier = Modifier
                        .width(134.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.75f))
                )

                PreviewPureGlassIconButton(
                    icon = Icons.Default.CameraAlt,
                    contentDescription = "Lock Screen Camera",
                    glassOpacity = glassOpacity,
                    specularShine = specularShine,
                    size = 50.dp,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun LockScreenGlassNotificationCard(
    icon: ImageVector,
    appName: String,
    timeAgo: String,
    title: String,
    body: String,
    glassOpacity: Float,
    specularShine: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .elasticLiquidGlass(
                cornerRadius = 24.dp,
                glowColor = Color.White,
                secondaryGlowColor = Color.White,
                elevated = true
            )
            .previewPureLiquidGlassSurface(
                cornerRadius = 24.dp,
                glassOpacity = glassOpacity,
                specularShine = specularShine
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.24f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.68f)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.86f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================================
// MODE 2: HOME SCREEN & COLORLESS PURE LIQUID GLASS DOCK MOCKUP OVERLAY
// ============================================================================

@Composable
private fun HomeScreenInteractiveOverlay(
    blurRadiusDp: Float,
    glassOpacity: Float,
    specularShine: Float,
    isControlsExpanded: Boolean
) {
    val sampleIcons = remember {
        listOf(
            "Photos" to Icons.Default.PhotoLibrary,
            "Camera" to Icons.Default.CameraAlt,
            "Weather" to Icons.Default.WbSunny,
            "Music" to Icons.Default.MusicNote,
            "Wallpapers" to Icons.Default.Wallpaper,
            "8K AI" to Icons.Default.AutoAwesome,
            "Saved" to Icons.Default.Bookmark,
            "Settings" to Icons.Default.Settings
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(top = 114.dp, bottom = if (isControlsExpanded) 380.dp else 160.dp)
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Pure Liquid Glass Weather & Optical Telemetry Widget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .previewPureLiquidGlassSurface(
                            cornerRadius = 24.dp,
                            glassOpacity = glassOpacity,
                            specularShine = specularShine
                        )
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Cupertino",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "72°",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Clear Sky • H:76° L:58°",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.80f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .previewPureLiquidGlassSurface(
                            cornerRadius = 24.dp,
                            glassOpacity = glassOpacity,
                            specularShine = specularShine
                        )
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Glass Optics",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${blurRadiusDp.roundToInt()} dp",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Frost ${(glassOpacity * 100).roundToInt()}% • Shine ${(specularShine * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.80f)
                        )
                    }
                }
            }

            // 2 Rows of 4 Pure Liquid Glass Home Screen App Icons
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                sampleIcons.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowItems.forEach { (label, iconVec) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .previewPureLiquidGlassSurface(
                                            cornerRadius = 18.dp,
                                            glassOpacity = glassOpacity,
                                            specularShine = specularShine
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconVec,
                                        contentDescription = label,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Colorless Pure Liquid Glass Home Dock Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .previewPureLiquidGlassSurface(
                    cornerRadius = 999.dp,
                    glassOpacity = glassOpacity,
                    specularShine = specularShine
                )
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Icons.Default.Home,
                    Icons.Default.Storage,
                    Icons.Default.AutoAwesome,
                    Icons.Default.Bookmark,
                    Icons.Default.Tune
                ).forEachIndexed { idx, dockIcon ->
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == 0) Color.White.copy(alpha = 0.30f)
                                else Color.White.copy(alpha = 0.12f)
                            )
                            .border(
                                1.dp,
                                Color.White.copy(alpha = if (idx == 0) 0.90f else 0.45f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = dockIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODE 3: DRAGGABLE PURE LIQUID GLASS INSPECTION LENS OVERLAY
// ============================================================================

@Composable
private fun DraggableLiquidGlassLensOverlay(
    blurRadiusDp: Float,
    topBlurDp: Float,
    glassOpacity: Float,
    specularShine: Float
) {
    var lensOffsetX by remember { mutableFloatStateOf(0f) }
    var lensOffsetY by remember { mutableFloatStateOf(-120f) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Draggable Circular/Squircle Pure Liquid Glass Inspection Loupe
        Box(
            modifier = Modifier
                .offset { IntOffset(lensOffsetX.roundToInt(), lensOffsetY.roundToInt()) }
                .size(width = 230.dp, height = 165.dp)
                .testTag("draggable_liquid_glass_lens")
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        lensOffsetX = (lensOffsetX + dragAmount.x).coerceIn(-360f, 360f)
                        lensOffsetY = (lensOffsetY + dragAmount.y).coerceIn(-520f, 360f)
                    }
                }
                .previewPureLiquidGlassSurface(
                    cornerRadius = 38.dp,
                    glassOpacity = glassOpacity,
                    specularShine = specularShine
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "MATERIAL 3 EXCLUSIVE LENS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    text = "Drag me across the wallpaper",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LensMetricBadge("Blur ${blurRadiusDp.roundToInt()}dp")
                    LensMetricBadge("Top ${topBlurDp.roundToInt()}dp")
                    LensMetricBadge("Frost ${(glassOpacity * 100).roundToInt()}%")
                }
            }
        }
    }
}

@Composable
private fun LensMetricBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f))
            .border(0.8.dp, Color.White.copy(alpha = 0.65f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================================
// REUSABLE PURE LIQUID GLASS SURFACES & HELPERS FOR LIVE PREVIEW
// ============================================================================

@Composable
private fun LivePreviewTopHighToLowBlur(
    topBlurStrengthDp: Float,
    glassOpacity: Float,
    specularShine: Float,
    modifier: Modifier = Modifier
) {
    val intensity = (topBlurStrengthDp / 36f).coerceIn(0f, 1f)
    val dynamicHeight = (90f + intensity * 120f).dp
    val topFrostAlpha = (intensity * (0.28f + glassOpacity * 0.35f)).coerceIn(0.04f, 0.62f)
    val topDarkScrim = (intensity * 0.42f).coerceIn(0.05f, 0.48f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dynamicHeight)
            .drawWithContent {
                drawContent()
                // High-to-Low progressive optical blur gradient at the top of the screen
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black.copy(alpha = topDarkScrim),
                            0.30f to Color.White.copy(alpha = topFrostAlpha * 0.72f),
                            0.62f to Color.White.copy(alpha = topFrostAlpha * 0.32f),
                            0.85f to Color.White.copy(alpha = topFrostAlpha * 0.10f),
                            1.00f to Color.Transparent
                        )
                    )
                )
                // Specular top edge sheen
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.White.copy(alpha = (intensity * specularShine * 0.36f).coerceIn(0f, 0.45f)),
                            0.25f to Color.Transparent
                        )
                    )
                )
            }
    )
}

@Composable
private fun PreviewPureGlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    glassOpacity: Float,
    specularShine: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    isActive: Boolean = false,
    testTag: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .previewPureLiquidGlassSurface(
                cornerRadius = 999.dp,
                glassOpacity = if (isActive) (glassOpacity + 0.22f).coerceAtMost(0.85f) else glassOpacity,
                specularShine = specularShine
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PreviewOpticalPresetChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clip(CircleShape)
            .background(
                if (active) Color.White.copy(alpha = 0.30f)
                else Color.White.copy(alpha = 0.10f)
            )
            .border(
                width = if (active) 1.2.dp else 0.8.dp,
                color = Color.White.copy(alpha = if (active) 0.92f else 0.38f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Pure colorless Liquid Glass surface modifier driven directly by the interactive preview's
 * live `glassOpacity` and `specularShine` sliders so the user sees immediate visual feedback.
 */
private fun Modifier.previewPureLiquidGlassSurface(
    cornerRadius: Dp,
    glassOpacity: Float,
    specularShine: Float
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val clampedOpacity = glassOpacity.coerceIn(0.12f, 0.88f)
    val clampedShine = specularShine.coerceIn(0.10f, 1.0f)

    return this
        .clip(shape)
        .background(
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to Color.White.copy(alpha = (clampedOpacity * 0.72f).coerceIn(0.10f, 0.68f)),
                    0.45f to Color.Black.copy(alpha = (clampedOpacity * 0.48f).coerceIn(0.08f, 0.46f)),
                    1.00f to Color.White.copy(alpha = (clampedOpacity * 0.42f).coerceIn(0.08f, 0.48f))
                )
            )
        )
        .drawWithContent {
            drawContent()
            val radiusPx = cornerRadius.toPx()

            // Top optical dome specular highlight
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (clampedShine * 0.42f).coerceIn(0.06f, 0.48f)),
                        Color.White.copy(alpha = (clampedShine * 0.10f).coerceIn(0.02f, 0.15f)),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.48f
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )

            // Crisp refractive rim border
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (0.45f + clampedShine * 0.50f).coerceIn(0.35f, 0.98f)),
                        Color.White.copy(alpha = (0.18f + clampedShine * 0.25f).coerceIn(0.15f, 0.55f)),
                        Color.White.copy(alpha = (0.38f + clampedShine * 0.42f).coerceIn(0.28f, 0.88f))
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                style = Stroke(width = 1.3.dp.toPx())
            )
        }
}
