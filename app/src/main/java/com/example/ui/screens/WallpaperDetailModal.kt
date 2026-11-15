package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WallpaperItem
import com.example.ui.components.GlassIntensity
import com.example.ui.components.LiquidGlassAmbientBackground
import com.example.ui.components.UpperHighToLowBlurOverlay
import com.example.ui.components.WallpaperEffectsStudioDialog
import com.example.ui.components.WallxAsyncImage
import com.example.ui.components.ios27LiquidStretch
import com.example.ui.components.premiumSmoothClick
import com.example.ui.components.pureLiquidGlass
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.viewmodel.ActiveDownloadState
import com.example.util.WallpaperDownloader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WallpaperDetailModal(
    wallpaper: WallpaperItem,
    isFavorited: Boolean = false,
    isFavorite: Boolean = isFavorited,
    favoriteIds: Set<String> = emptySet(),
    activeDownload: ActiveDownloadState? = null,
    relatedWallpapers: List<WallpaperItem> = emptyList(),
    recentlyAddedWallpapers: List<WallpaperItem> = emptyList(),
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    onToggleRelatedFavorite: (WallpaperItem) -> Unit = {},
    onDownloadProgress: (String?, Float, String) -> Unit = { _, _, _ -> },
    onRecordDownload: (WallpaperItem) -> Unit = {},
    onSelectRelated: (WallpaperItem) -> Unit = {},
    onNotifySuccess: (String) -> Unit = {},
    onNotifyError: (String) -> Unit = {},
    onSyncOpticsToApp: (Float, Float, Float, Float) -> Unit = { _, _, _, _ -> },
    onDownload8K: (Boolean) -> Unit = {}
) {
    BackHandler(onBack = onDismiss)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val glass = LocalLiquidGlassColors.current
    val isDownloadingThis = activeDownload?.wallpaperId == wallpaper.id

    var showApplyDestinationSheet by remember { mutableStateOf(false) }
    var isSettingWallpaper by remember { mutableStateOf(false) }
    var statusToast by remember { mutableStateOf<String?>(null) }
    var isFullScreen4KMode by remember { mutableStateOf(false) }
    var showEffectsStudio by remember { mutableStateOf(false) }

    // Pinch-to-zoom 4K inspection state
    var zoomScale by remember(wallpaper.id) { mutableFloatStateOf(1f) }
    var panOffset by remember(wallpaper.id) { mutableStateOf(Offset.Zero) }

    // Local state for instant 4K Download to device gallery
    var isDirectDownloading by remember { mutableStateOf(false) }
    var directDownloadProgress by remember { mutableFloatStateOf(0f) }
    var directDownloadSuccess by remember { mutableStateOf(false) }

    val primaryPurple = Color(0xFF5B2EFF)
    val accentBlue = Color(0xFF2563EB)

    fun executeActualWallpaperDownload(targetWall: WallpaperItem) {
        if (isDirectDownloading || isDownloadingThis) return
        isDirectDownloading = true
        directDownloadProgress = 0.08f
        directDownloadSuccess = false
        statusToast = "Saving 4K Ultra HD to Pictures/Pixora..."

        onDownload8K(true)
        onDownloadProgress(targetWall.id, 0.1f, "Downloading 4K Ultra HD...")

        scope.launch {
            val result = WallpaperDownloader.downloadWallpaperToDevice(
                context = context,
                wallpaper = targetWall,
                onProgress = { progress: Float ->
                    val clamped = progress.coerceIn(0.05f, 1f)
                    directDownloadProgress = clamped
                    onDownloadProgress(targetWall.id, clamped, "Saving 4K (${(clamped * 100).toInt()}%)...")
                }
            )
            isDirectDownloading = false
            onDownloadProgress(null, 1f, "Completed")
            result.onSuccess {
                directDownloadProgress = 1f
                directDownloadSuccess = true
                onRecordDownload(targetWall)
                val msg = "Saved '${targetWall.title}' in 4K to Gallery!"
                statusToast = msg
                onNotifySuccess(msg)
                delay(3200)
                statusToast = null
            }.onFailure { err: Throwable ->
                val errMsg = "Download failed: ${err.localizedMessage ?: "Try again"}"
                statusToast = errMsg
                onNotifyError(errMsg)
                delay(3000)
                statusToast = null
            }
        }
    }

    fun triggerInstant4KDownload(targetWall: WallpaperItem) {
        if (isDirectDownloading || isDownloadingThis) return
        executeActualWallpaperDownload(targetWall)
    }

    fun executeApplyWallpaperToDevice(targetWall: WallpaperItem, flag: Int, targetLabel: String) {
        if (isSettingWallpaper) return
        isSettingWallpaper = true
        showApplyDestinationSheet = false
        statusToast = "Applying Full-Screen 4K Wallpaper to $targetLabel..."
        scope.launch {
            val result = WallpaperDownloader.applyWallpaperToDevice(
                context = context,
                wallpaper = targetWall,
                targetFlags = flag,
                onProgress = { }
            )
            isSettingWallpaper = false
            result.onSuccess { msg: String ->
                statusToast = msg
                onNotifySuccess(msg)
                delay(2800)
                statusToast = null
            }.onFailure { err: Throwable ->
                val errMsg = "Could not set wallpaper: ${err.localizedMessage ?: "Error"}"
                statusToast = errMsg
                onNotifyError(errMsg)
                delay(2800)
                statusToast = null
            }
        }
    }

    fun applyWallpaperToDevice(targetWall: WallpaperItem, flag: Int, targetLabel: String) {
        if (isSettingWallpaper) return
        showApplyDestinationSheet = false
        executeApplyWallpaperToDevice(targetWall, flag, targetLabel)
    }

    val combinedDownloading = isDirectDownloading || isDownloadingThis
    val combinedProgress = if (isDirectDownloading) {
        directDownloadProgress
    } else {
        activeDownload?.progress ?: 0f
    }

    key(wallpaper.id) {
        val activeWall = wallpaper
        // Always load the highest 4K source URL for the clicked image only
        val highRes4KUrl = remember(activeWall.id, activeWall.imageUrl, activeWall.thumbnailUrl) {
            activeWall.imageUrl.ifBlank { activeWall.thumbnailUrl }
        }

        var isBackgroundBlurredOnTouch by remember(activeWall.id) { mutableStateOf(false) }
        val touchBlurDp = if (isBackgroundBlurredOnTouch) 60.dp else 24.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("wallpaper_detail_modal")
        ) {
            // TRUE EDGE-TO-EDGE FULL-SCREEN WALLPAPER CANVAS
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        isBackgroundBlurredOnTouch = !isBackgroundBlurredOnTouch
                    }
                    .pointerInput(activeWall.id) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (zoomScale * zoom).coerceIn(1f, 4f)
                            zoomScale = newScale
                            panOffset = if (newScale > 1.02f) {
                                Offset(
                                    x = (panOffset.x + pan.x).coerceIn(-600f, 600f),
                                    y = (panOffset.y + pan.y).coerceIn(-900f, 900f)
                                )
                            } else {
                                Offset.Zero
                            }
                        }
                    }
            ) {
                // 1. Ambient Blurred Backdrop (smoothly blurs on touch)
                WallxAsyncImage(
                    imageUrl = highRes4KUrl,
                    fallbackImageUrl = activeWall.thumbnailUrl,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    isFullQualityInspection = false,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(touchBlurDp)
                        .graphicsLayer {
                            scaleX = 1.06f
                            scaleY = 1.06f
                            alpha = if (isBackgroundBlurredOnTouch) 0.90f else 0.78f
                        }
                )

                // 2. Full Edge-to-Edge Wallpaper (stretched completely across screen bounds)
                WallxAsyncImage(
                    imageUrl = highRes4KUrl,
                    fallbackImageUrl = activeWall.thumbnailUrl,
                    contentDescription = activeWall.title,
                    contentScale = ContentScale.Crop,
                    isFullQualityInspection = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isBackgroundBlurredOnTouch) Modifier.blur(20.dp) else Modifier)
                        .graphicsLayer {
                            scaleX = zoomScale
                            scaleY = zoomScale
                            translationX = panOffset.x
                            translationY = panOffset.y
                        }
                )

                // Soft bottom readability gradient (hidden when UI is hidden)
                AnimatedVisibility(
                    visible = !isFullScreen4KMode,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(180)),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.55f)
                                    )
                                )
                            )
                    )
                }

                // SIMPLE TOP BAR: Back on Left, Hide/Show UI Toggle on Right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = !isFullScreen4KMode,
                        enter = fadeIn(tween(180)),
                        exit = fadeOut(tween(180))
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .pureLiquidGlass(cornerRadius = 24.dp, intensity = GlassIntensity.DeepTransparent)
                                .premiumSmoothClick(onClick = onDismiss)
                                .testTag("close_detail_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    if (isFullScreen4KMode) {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .pureLiquidGlass(cornerRadius = 24.dp, intensity = GlassIntensity.DeepTransparent)
                            .premiumSmoothClick { isFullScreen4KMode = !isFullScreen4KMode }
                            .testTag("detail_fullscreen_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFullScreen4KMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullScreen4KMode) "Show Controls" else "Hide Controls",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Toast / Progress Banner
                if (statusToast != null || combinedDownloading || isSettingWallpaper) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 70.dp, start = 20.dp, end = 20.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (combinedDownloading || isSettingWallpaper) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = statusToast
                                    ?: activeDownload?.statusText
                                    ?: "Processing Wallpaper...",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                // BOTTOM-LEFT: Clean Minimal Title & Category Only
                AnimatedVisibility(
                    visible = !isFullScreen4KMode,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(180)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .navigationBarsPadding()
                        .padding(start = 20.dp, bottom = 36.dp, end = 96.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = activeWall.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (activeWall.category.isNotBlank()) {
                            Text(
                                text = activeWall.category,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // RIGHT-SIDE FLOATING ACTION BAR: Clean 3 buttons only (Like, Download, Set)
                AnimatedVisibility(
                    visible = !isFullScreen4KMode,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(180)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 16.dp, bottom = 32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. LIKE
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleFavorite
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .pureLiquidGlass(
                                        cornerRadius = 25.dp,
                                        intensity = if (isFavorite) GlassIntensity.Illuminated else GlassIntensity.DeepTransparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isFavorite) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.55f),
                                        CircleShape
                                    )
                                    .testTag("right_side_like_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like Wallpaper",
                                    tint = if (isFavorite) Color(0xFFFF2D55) else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (isFavorite && activeWall.favoriteCount == 0L) 1L else activeWall.favoriteCount}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.White
                            )
                        }

                        // 2. DOWNLOAD
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = { triggerInstant4KDownload(activeWall) }
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .pureLiquidGlass(
                                        cornerRadius = 25.dp,
                                        intensity = if (directDownloadSuccess) GlassIntensity.Illuminated else GlassIntensity.DeepTransparent
                                    )
                                    .border(
                                        1.dp,
                                        if (directDownloadSuccess) Color(0xFF10B981) else Color.White.copy(alpha = 0.55f),
                                        CircleShape
                                    )
                                    .testTag("right_side_download_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (combinedDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.2.dp,
                                        color = Color(0xFF38BDF8)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (directDownloadSuccess) Icons.Default.CheckCircle else Icons.Default.Download,
                                        contentDescription = "Download Wallpaper",
                                        tint = if (directDownloadSuccess) Color(0xFF4ADE80) else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (directDownloadSuccess) "Saved" else "${activeWall.downloadCount}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.White
                            )
                        }

                        // 3. SET
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = { showApplyDestinationSheet = true }
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .pureLiquidGlass(
                                        cornerRadius = 25.dp,
                                        intensity = GlassIntensity.DeepTransparent
                                    )
                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.80f), CircleShape)
                                    .testTag("right_side_set_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wallpaper,
                                    contentDescription = "Set Wallpaper",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isSettingWallpaper) "Setting..." else "Set",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Apply Destination Modal Sheet (Home Screen, Lock Screen, or Both)
            if (showApplyDestinationSheet) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.58f))
                        .clickable { showApplyDestinationSheet = false },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding()
                            .clip(RoundedCornerShape(34.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = if (glass.isDark) {
                                        listOf(
                                            Color(0xF5161E35),
                                            Color(0xFA0C1222)
                                        )
                                    } else {
                                        listOf(
                                            Color.White.copy(alpha = 0.96f),
                                            Color(0xFFEEF2FF).copy(alpha = 0.98f)
                                        )
                                    }
                                )
                            )
                            .border(
                                width = 1.4.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.42f),
                                        Color.White.copy(alpha = 0.12f)
                                    )
                                ),
                                shape = RoundedCornerShape(34.dp)
                            )
                            .clickable(enabled = false) {}
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Set 4K Wallpaper",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (glass.isDark) Color.White else Color(0xFF141326)
                        )
                        Text(
                            text = "Choose where you want to apply '${activeWall.title}' in 4K Ultra HD",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (glass.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                        )

                        ApplyTargetOptionButton(
                            icon = Icons.Default.Home,
                            title = "Home Screen",
                            onClick = {
                                applyWallpaperToDevice(
                                    activeWall,
                                    android.app.WallpaperManager.FLAG_SYSTEM,
                                    "Home Screen"
                                )
                            }
                        )
                        ApplyTargetOptionButton(
                            icon = Icons.Default.Lock,
                            title = "Lock Screen",
                            onClick = {
                                applyWallpaperToDevice(
                                    activeWall,
                                    android.app.WallpaperManager.FLAG_LOCK,
                                    "Lock Screen"
                                )
                            }
                        )
                        ApplyTargetOptionButton(
                            icon = Icons.Default.PhoneAndroid,
                            title = "Both Home & Lock Screens",
                            isPrimary = true,
                            onClick = {
                                applyWallpaperToDevice(
                                    activeWall,
                                    android.app.WallpaperManager.FLAG_SYSTEM or android.app.WallpaperManager.FLAG_LOCK,
                                    "Home & Lock Screens"
                                )
                            }
                        )
                    }
                }
            }

            if (showEffectsStudio) {
                WallpaperEffectsStudioDialog(
                    wallpaper = activeWall,
                    onDismiss = { showEffectsStudio = false },
                    onApplyToDevice = { blurDp, dimAlpha, _, targetFlags ->
                        showEffectsStudio = false
                        isSettingWallpaper = true
                        statusToast = "Applying customized 4K wallpaper..."
                        scope.launch {
                            val res = WallpaperDownloader.applyWallpaperWithEffects(
                                context = context,
                                wallpaper = activeWall,
                                blurRadiusDp = blurDp,
                                topBlurStrengthDp = 0f,
                                dimAlpha = dimAlpha,
                                targetFlags = targetFlags,
                                onProgress = { }
                            )
                            isSettingWallpaper = false
                            res.onSuccess { msg ->
                                statusToast = msg
                                onNotifySuccess(msg)
                                delay(2400)
                                statusToast = null
                            }.onFailure { err ->
                                val errMsg = err.localizedMessage ?: "Failed to set wallpaper."
                                statusToast = errMsg
                                onNotifyError(errMsg)
                                delay(2400)
                                statusToast = null
                            }
                        }
                    },
                    onDownloadWithEffects = { blurDp, dimAlpha, _ ->
                        showEffectsStudio = false
                        scope.launch {
                            statusToast = "Saving customized 4K wallpaper..."
                            val res = WallpaperDownloader.downloadWallpaperWithEffects(
                                context = context,
                                wallpaper = activeWall,
                                blurRadiusDp = blurDp,
                                topBlurStrengthDp = 0f,
                                dimAlpha = dimAlpha,
                                onProgress = { }
                            )
                            res.onSuccess { msg ->
                                statusToast = msg
                                onNotifySuccess(msg)
                                delay(2400)
                                statusToast = null
                            }.onFailure { err ->
                                val errMsg = err.localizedMessage ?: "Failed to save wallpaper."
                                statusToast = errMsg
                                onNotifyError(errMsg)
                                delay(2400)
                                statusToast = null
                            }
                        }
                    },
                    onCopyColorHex = { hex ->
                        onNotifySuccess("Copied $hex to clipboard")
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailLiquidGlassActionItem(
    icon: ImageVector,
    label: String,
    isHighlighted: Boolean = false,
    iconTint: Color = Color.White,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .pureLiquidGlass(
                cornerRadius = 20.dp,
                intensity = if (isHighlighted) GlassIntensity.Illuminated else GlassIntensity.DeepTransparent
            )
            .premiumSmoothClick(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isHighlighted) Color.White else iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.5.sp
            ),
            color = Color.White
        )
    }
}

@Composable
private fun DetailLiquidGlassTagChip(label: String) {
    Box(
        modifier = Modifier
            .pureLiquidGlass(cornerRadius = 14.dp, intensity = GlassIntensity.AdaptiveTinted)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
private fun ApplyTargetOptionButton(
    icon: ImageVector,
    title: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    val primaryPurple = Color(0xFF5B2EFF)
    val accentBlue = Color(0xFF2563EB)
    val secondaryBg = if (glass.isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.65f)
    val secondaryText = if (glass.isDark) Color.White else Color(0xFF1E1B3A)
    val secondaryIcon = if (glass.isDark) Color(0xFFC4B5FD) else primaryPurple
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .ios27LiquidStretch()
            .clip(CircleShape)
            .background(
                if (isPrimary) {
                    Brush.horizontalGradient(listOf(primaryPurple, accentBlue))
                } else {
                    Brush.horizontalGradient(listOf(secondaryBg, secondaryBg))
                }
            )
            .border(
                width = 1.2.dp,
                color = Color.White.copy(alpha = if (isPrimary) 0.85f else 0.72f),
                shape = CircleShape
            )
            .premiumSmoothClick(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isPrimary) Color.White else secondaryIcon,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = if (isPrimary) Color.White else secondaryText
            )
        }
    }
}
