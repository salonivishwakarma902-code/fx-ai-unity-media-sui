package com.example.ui.components

import android.os.Build
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.theme.LocalLiquidGlassOptics
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RoseGlow

enum class GlassIntensity {
    DeepTransparent,
    AdaptiveTinted,
    Illuminated,
    CrystalClear,
    UltraGlossy
}

val LocalEnableLiquidStretch = compositionLocalOf { true }

class LiquidGlassBackdropState(
    val sharpGraphicsLayer: GraphicsLayer,
    val blurredGraphicsLayer: GraphicsLayer
) {
    var rootOffset by mutableStateOf(Offset.Zero)
}

val LocalLiquidGlassBackdrop = staticCompositionLocalOf<LiquidGlassBackdropState?> { null }

data class DockDestination<T>(
    val route: T,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

fun Modifier.captureLiquidGlassBackdrop(
    backdropState: LiquidGlassBackdropState,
    onRootOffsetChanged: (Offset) -> Unit = {}
): Modifier = this
    .onGloballyPositioned { coords ->
        val pos = coords.positionInRoot()
        backdropState.rootOffset = pos
        onRootOffsetChanged(pos)
    }
    .drawWithContent {
        backdropState.sharpGraphicsLayer.record {
            this@drawWithContent.drawContent()
        }
        drawLayer(backdropState.sharpGraphicsLayer)
    }

@Composable
fun RealLiquidGlassBackdropBlurLayer(
    elementOffsetInRoot: Offset,
    blurRadiusDp: Float = 36f,
    refractionDepth: Float = 72f,
    refractionStrength: Float = 140f,
    chromaticSplit: Float = 48f,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlassColors.current
    val optics = LocalLiquidGlassOptics.current
    val effectiveBlur = (blurRadiusDp.coerceIn(0f, 80f) * 0.45f).dp
    val tintColor = if (glass.isDark) {
        Color(0xFF0A1432).copy(alpha = (optics.glassOpacity + 0.22f).coerceIn(0.22f, 0.72f))
    } else {
        Color.White.copy(alpha = (optics.glassOpacity + 0.34f).coerceIn(0.32f, 0.82f))
    }

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        tintColor,
                        glass.glowCyan.copy(alpha = (chromaticSplit / 900f).coerceIn(0.04f, 0.16f)),
                        glass.glowViolet.copy(alpha = (refractionDepth / 900f).coerceIn(0.04f, 0.16f)),
                        tintColor
                    ),
                    start = Offset(
                        x = (elementOffsetInRoot.x * 0.05f).coerceIn(-80f, 80f),
                        y = 0f
                    ),
                    end = Offset(
                        x = 400f + (refractionStrength * 0.6f),
                        y = 400f
                    )
                )
            )
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && effectiveBlur > 0.dp) {
                    Modifier.blur(effectiveBlur)
                } else {
                    Modifier
                }
            )
    )
}

@Composable
fun LiquidGlassAmbientBackground(
    backgroundWallpaperUrl: String? = null,
    backgroundFallbackUrl: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    val optics = LocalLiquidGlassOptics.current

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    glass.backgroundPrimary,
                    glass.backgroundSecondary,
                    glass.backgroundPrimary
                )
            )
        )
    ) {
        if (!backgroundWallpaperUrl.isNullOrBlank()) {
            WallxAsyncImage(
                imageUrl = backgroundWallpaperUrl,
                fallbackImageUrl = backgroundFallbackUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                isFullQualityInspection = false,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (glass.isDark) 0.28f else 0.18f)
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.blur(24.dp)
                        } else {
                            Modifier
                        }
                    )
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha((0.55f * optics.ambientOrbsMotion.coerceIn(0.4f, 1.5f)).coerceIn(0.2f, 0.85f))
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glass.glowCyan.copy(alpha = if (glass.isDark) 0.20f else 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.18f, size.height * 0.16f),
                    radius = size.width * 0.72f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glass.glowViolet.copy(alpha = if (glass.isDark) 0.18f else 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.55f),
                    radius = size.width * 0.76f
                )
            )
        }

        content()
    }
}

fun Modifier.pureLiquidGlass(
    cornerRadius: Dp = 24.dp,
    glowColor: Color = ElectricCyan,
    secondaryGlowColor: Color = NeonViolet,
    elevated: Boolean = false,
    intensity: GlassIntensity = GlassIntensity.AdaptiveTinted,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val glass = LocalLiquidGlassColors.current
    val optics = LocalLiquidGlassOptics.current
    val shape = RoundedCornerShape(cornerRadius)
    val isPressed = if (interactionSource != null) {
        interactionSource.collectIsPressedAsState().value
    } else {
        false
    }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pureGlassPressScale"
    )

    val baseAlpha = when (intensity) {
        GlassIntensity.DeepTransparent -> (optics.glassOpacity * 0.72f).coerceIn(0.12f, 0.36f)
        GlassIntensity.AdaptiveTinted -> (optics.glassOpacity + if (elevated) 0.08f else 0f).coerceIn(0.16f, 0.65f)
        GlassIntensity.Illuminated -> (optics.glassOpacity + 0.16f).coerceIn(0.22f, 0.72f)
        GlassIntensity.CrystalClear -> (optics.glassOpacity * 0.85f).coerceIn(0.14f, 0.48f)
        GlassIntensity.UltraGlossy -> (optics.glassOpacity + 0.22f).coerceIn(0.28f, 0.82f)
    }

    val fillColors = if (glass.isDark) {
        listOf(
            Color(0xFF1A284E).copy(alpha = (baseAlpha + 0.36f).coerceIn(0.35f, 0.90f)),
            Color(0xFF0E1733).copy(alpha = (baseAlpha + 0.42f).coerceIn(0.45f, 0.94f)),
            Color(0xFF142144).copy(alpha = (baseAlpha + 0.34f).coerceIn(0.38f, 0.90f))
        )
    } else {
        listOf(
            Color.White.copy(alpha = (baseAlpha + 0.54f).coerceIn(0.68f, 0.95f)),
            Color(0xFFF3F6FF).copy(alpha = (baseAlpha + 0.48f).coerceIn(0.62f, 0.92f)),
            Color.White.copy(alpha = (baseAlpha + 0.52f).coerceIn(0.66f, 0.94f))
        )
    }

    val borderTopAlpha = (0.42f + optics.specularShine * 0.44f).coerceIn(0.35f, 0.92f)

    this
        .graphicsLayer {
            if (interactionSource != null) {
                scaleX = pressScale
                scaleY = pressScale
            }
        }
        .clip(shape)
        .background(Brush.verticalGradient(colors = fillColors))
        .drawWithContent {
            drawContent()
            val radiusPx = cornerRadius.toPx()
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (optics.specularShine * 0.24f).coerceIn(0.06f, 0.30f)),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.42f
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = borderTopAlpha),
                        glowColor.copy(alpha = if (elevated) 0.52f else 0.32f),
                        secondaryGlowColor.copy(alpha = if (elevated) 0.42f else 0.22f),
                        Color.White.copy(alpha = borderTopAlpha * 0.72f)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                style = Stroke(width = if (elevated) 1.35.dp.toPx() else 1.dp.toPx())
            )
        }
}

fun Modifier.elasticLiquidGlass(
    cornerRadius: Dp = 24.dp,
    glowColor: Color = ElectricCyan,
    secondaryGlowColor: Color = NeonViolet,
    elevated: Boolean = false
): Modifier = this
    .ios27LiquidStretch(maxStretchScale = 0.04f)
    .pureLiquidGlass(
        cornerRadius = cornerRadius,
        glowColor = glowColor,
        secondaryGlowColor = secondaryGlowColor,
        elevated = elevated,
        intensity = if (elevated) GlassIntensity.Illuminated else GlassIntensity.AdaptiveTinted
    )

fun Modifier.ios27LiquidStretch(
    maxStretchScale: Float = 0.05f
): Modifier = composed {
    val enabled = LocalEnableLiquidStretch.current
    if (!enabled) return@composed this

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) (1f + maxStretchScale.coerceIn(0f, 0.14f)) else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ios27Stretch"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.premiumSmoothClick(
    pressedScale: Float = 0.96f,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "premiumClickScale"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

fun Modifier.shimmerSweepHighlight(
    shimmerColor: Color = Color.White.copy(alpha = 0.32f)
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmerSweep")
    val progress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    this.drawWithContent {
        drawContent()
        val width = size.width
        val height = size.height
        if (width > 0f && height > 0f) {
            val startX = width * progress
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, shimmerColor, Color.Transparent),
                    start = Offset(startX - width * 0.25f, 0f),
                    end = Offset(startX + width * 0.25f, height)
                )
            )
        }
    }
}

fun Modifier.staggeredBouncyEntrance(
    index: Int = 0
): Modifier = composed {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    val delayMs = (index.coerceIn(0, 12) * 35)
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 220, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "entranceAlpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 18f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "entranceOffset"
    )
    this.graphicsLayer {
        this.alpha = alpha
        translationY = offsetY
    }
}

fun Modifier.subtleFloatingBounce(
    amplitudePx: Float = 6f,
    durationMs: Int = 2000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "floatingBounce")
    val dy by infiniteTransition.animateFloat(
        initialValue = -amplitudePx,
        targetValue = amplitudePx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs.coerceAtLeast(600), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingDy"
    )
    this.graphicsLayer {
        translationY = dy
    }
}

fun Modifier.progressiveTopHighToLowBlur(
    height: Dp = 0.dp,
    includeDockBlur: Boolean = true
): Modifier = this

@Composable
fun UpperHighToLowBlurOverlay(
    modifier: Modifier = Modifier,
    height: Dp = 0.dp
) {
    val glass = LocalLiquidGlassColors.current
    val resolvedHeight = if (height > 0.dp) height else 86.dp
    val scrimBase = if (glass.isDark) glass.backgroundPrimary else glass.backgroundPrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(resolvedHeight)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        scrimBase.copy(alpha = 0.88f),
                        scrimBase.copy(alpha = 0.45f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun BottomHighToLowBlurOverlay(
    modifier: Modifier = Modifier,
    height: Dp = 108.dp
) {
    val glass = LocalLiquidGlassColors.current
    val scrimBase = glass.backgroundPrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        scrimBase.copy(alpha = 0.55f),
                        scrimBase.copy(alpha = 0.92f)
                    )
                )
            )
    )
}

@Composable
fun LiquidGlassScreenWithUpperBlur(
    modifier: Modifier = Modifier,
    headerExtraHeight: Dp = 76.dp,
    headerClearanceDp: Dp = 64.dp,
    headerContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.(Dp) -> Unit
) {
    val density = LocalDensity.current
    val statusBarTopDp = with(density) { WindowInsets.statusBars.getTop(density).toDp() }
    val totalClearance = statusBarTopDp + headerClearanceDp

    Box(modifier = modifier.fillMaxSize()) {
        content(totalClearance)

        UpperHighToLowBlurOverlay(
            modifier = Modifier.align(Alignment.TopCenter),
            height = statusBarTopDp + headerExtraHeight
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars),
            content = headerContent
        )
    }
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    glowColor: Color = ElectricCyan,
    secondaryGlowColor: Color = NeonViolet,
    elevated: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = modifier
        .pureLiquidGlass(
            cornerRadius = cornerRadius,
            glowColor = glowColor,
            secondaryGlowColor = secondaryGlowColor,
            elevated = elevated,
            intensity = if (elevated) GlassIntensity.Illuminated else GlassIntensity.AdaptiveTinted
        )
        .let { m ->
            if (onClick != null) m.premiumSmoothClick(onClick = onClick) else m
        }
        .padding(contentPadding)

    Column(
        modifier = baseModifier,
        content = content
    )
}

@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    glowColor: Color = ElectricCyan,
    secondaryGlowColor: Color = NeonViolet,
    filledAccent: Boolean = false,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val glass = LocalLiquidGlassColors.current
    val shape = RoundedCornerShape(18.dp)
    val contentColor = if (filledAccent) Color.White else glass.textPrimary

    val buttonModifier = modifier
        .height(48.dp)
        .let { m -> if (testTag != null) m.testTag(testTag) else m }
        .alpha(if (enabled) 1f else 0.45f)
        .clip(shape)
        .then(
            if (filledAccent) {
                Modifier
                    .background(
                        Brush.horizontalGradient(listOf(glowColor, secondaryGlowColor))
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.45f), shape)
            } else {
                Modifier.pureLiquidGlass(
                    cornerRadius = 18.dp,
                    glowColor = glowColor,
                    secondaryGlowColor = secondaryGlowColor,
                    elevated = false
                )
            }
        )
        .premiumSmoothClick(enabled = enabled, onClick = onClick)
        .padding(horizontal = 16.dp)

    Box(
        modifier = buttonModifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (filledAccent) Color.White else glowColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LiquidGlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = ElectricCyan,
    leadingIcon: ImageVector? = null,
    testTag: String? = null
) {
    val glass = LocalLiquidGlassColors.current
    val shape = CircleShape
    val bgColor by animateColorAsState(
        targetValue = if (selected) {
            accentColor.copy(alpha = if (glass.isDark) 0.24f else 0.16f)
        } else {
            if (glass.isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f)
        },
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) accentColor else glass.glassBorderSubtle,
        label = "chipBorder"
    )

    Row(
        modifier = modifier
            .height(36.dp)
            .let { m -> if (testTag != null) m.testTag(testTag) else m }
            .clip(shape)
            .background(bgColor)
            .border(1.1.dp, borderColor, shape)
            .premiumSmoothClick(pressedScale = 0.94f, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (selected) accentColor else glass.textSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (selected) (if (glass.isDark) Color.White else accentColor) else glass.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun LiquidGlassCircularButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    tint: Color = Color.White,
    isActive: Boolean = false,
    activeTint: Color = tint,
    testTag: String? = null
) {
    val glass = LocalLiquidGlassColors.current
    val resolvedTint = if (isActive) activeTint else tint
    val bgColor = if (isActive) {
        activeTint.copy(alpha = 0.20f)
    } else if (glass.isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.72f)
    }

    Box(
        modifier = modifier
            .size(size)
            .let { m -> if (testTag != null) m.testTag(testTag) else m }
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (isActive) activeTint.copy(alpha = 0.7f) else glass.glassBorderSubtle,
                shape = CircleShape
            )
            .premiumSmoothClick(pressedScale = 0.90f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = resolvedTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    testTag: String? = null
) {
    val glass = LocalLiquidGlassColors.current
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { m -> if (testTag != null) m.testTag(testTag) else m }
            .clip(shape)
            .background(
                if (glass.isDark) Color(0xFF0E1733).copy(alpha = 0.85f)
                else Color.White.copy(alpha = 0.88f)
            )
            .border(1.dp, glass.glassBorderSubtle, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = glass.textSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = glass.textSecondary.copy(alpha = 0.75f),
                    maxLines = if (singleLine) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = glass.textPrimary),
                cursorBrush = SolidColor(ElectricCyan),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color = ElectricCyan
) {
    val glass = LocalLiquidGlassColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = glass.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = glass.textSecondary
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = glass.textSecondary,
                uncheckedTrackColor = glass.glassFill
            )
        )
    }
}

@Composable
fun LiquidGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    label: String,
    valueText: String,
    modifier: Modifier = Modifier,
    accentColor: Color = ElectricCyan
) {
    val glass = LocalLiquidGlassColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = glass.textPrimary
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = glass.glassBorderSubtle
            )
        )
    }
}

@Composable
fun <T> LiquidGlassNavigationDock(
    items: List<DockDestination<T>>,
    selectedRoute: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = ElectricCyan,
    showDetachedSearchButton: Boolean = false,
    onSearchClick: (() -> Unit)? = null
) {
    val glass = LocalLiquidGlassColors.current
    val activeAccent = if (accentColor == Color(0xFF0F172A) && glass.isDark) ElectricCyan else accentColor

    Row(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .pureLiquidGlass(
                    cornerRadius = 32.dp,
                    glowColor = activeAccent,
                    secondaryGlowColor = NeonViolet,
                    elevated = true,
                    intensity = GlassIntensity.UltraGlossy
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.route == selectedRoute
                val pillPadding by animateDpAsState(
                    targetValue = if (isSelected) 12.dp else 8.dp,
                    label = "dockPillPad"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) activeAccent.copy(alpha = if (glass.isDark) 0.22f else 0.14f)
                            else Color.Transparent
                        )
                        .clickable { onSelect(item.route) }
                        .padding(vertical = 6.dp, horizontal = pillPadding / 2)
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) activeAccent else glass.textSecondary,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) (if (glass.isDark) Color.White else activeAccent) else glass.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (showDetachedSearchButton && onSearchClick != null) {
            LiquidGlassCircularButton(
                icon = Icons.Default.Search,
                contentDescription = "Search",
                size = 52.dp,
                iconSize = 22.dp,
                tint = activeAccent,
                onClick = onSearchClick
            )
        }
    }
}

@Composable
fun WallpersNavigationDrawerDialog(
    canAccessAdmin: Boolean,
    userName: String,
    userEmail: String,
    onSelectHome: () -> Unit,
    onSelectCategories: () -> Unit,
    onSelectTrending: () -> Unit,
    onSelectPopular: () -> Unit,
    onSelectSpotlight: () -> Unit,
    onSelectFavorites: () -> Unit,
    onSelectDownloads: () -> Unit,
    onSelectNotifications: () -> Unit,
    onSelectProfile: () -> Unit,
    onSelectSettings: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    val glass = LocalLiquidGlassColors.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.56f))
                .clickable(onClick = onDismiss)
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            LiquidGlassCard(
                modifier = Modifier
                    .width(310.dp)
                    .clickable(enabled = false) {},
                cornerRadius = 28.dp,
                glowColor = ElectricCyan,
                secondaryGlowColor = NeonViolet,
                elevated = true,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName.ifBlank { "Pixora Member" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = glass.textPrimary
                            )
                            Text(
                                text = userEmail.ifBlank { "Guest Session" },
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        LiquidGlassCircularButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Close Menu",
                            size = 36.dp,
                            iconSize = 18.dp,
                            tint = glass.textPrimary,
                            onClick = onDismiss
                        )
                    }

                    HorizontalDivider(color = glass.glassBorderSubtle)

                    DrawerMenuRow(Icons.Default.Home, "Home") { onDismiss(); onSelectHome() }
                    DrawerMenuRow(Icons.Default.Category, "Categories") { onDismiss(); onSelectCategories() }
                    DrawerMenuRow(Icons.Default.Whatshot, "Trending") { onDismiss(); onSelectTrending() }
                    DrawerMenuRow(Icons.Default.Explore, "Popular") { onDismiss(); onSelectPopular() }
                    DrawerMenuRow(Icons.Default.AutoAwesome, "8K AI Studio") { onDismiss(); onSelectSpotlight() }
                    DrawerMenuRow(Icons.Default.Bookmark, "Favorites") { onDismiss(); onSelectFavorites() }
                    DrawerMenuRow(Icons.Default.Download, "Downloads") { onDismiss(); onSelectDownloads() }
                    DrawerMenuRow(Icons.Default.Notifications, "Notifications") { onDismiss(); onSelectNotifications() }
                    DrawerMenuRow(Icons.Default.Person, "Profile") { onDismiss(); onSelectProfile() }
                    DrawerMenuRow(Icons.Default.Settings, "Settings") { onDismiss(); onSelectSettings() }

                    if (canAccessAdmin) {
                        HorizontalDivider(color = glass.glassBorderSubtle)
                        DrawerMenuRow(
                            icon = Icons.Default.AdminPanelSettings,
                            label = "Admin Studio",
                            tint = AmberGlow
                        ) {
                            onDismiss()
                            onOpenAdminPanel()
                        }
                    }

                    HorizontalDivider(color = glass.glassBorderSubtle)
                    DrawerMenuRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        label = "Sign Out",
                        tint = RoseGlow
                    ) {
                        onDismiss()
                        onSignOut()
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuRow(
    icon: ImageVector,
    label: String,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint ?: ElectricCyan,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = tint ?: glass.textPrimary
        )
    }
}

@Composable
fun WallxAsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackImageUrl: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    isFullQualityInspection: Boolean = false
) {
    val context = LocalContext.current
    val resolvedPrimary = imageUrl.ifBlank { fallbackImageUrl.orEmpty() }
    val resolvedFallback = fallbackImageUrl?.takeIf { it.isNotBlank() && it != resolvedPrimary }
    var activeModel by remember(resolvedPrimary, resolvedFallback) { mutableStateOf(resolvedPrimary) }

    val request = remember(activeModel, isFullQualityInspection) {
        ImageRequest.Builder(context)
            .data(activeModel.ifBlank { null })
            .crossfade(true)
            .apply {
                if (isFullQualityInspection) {
                    size(coil.size.Size.ORIGINAL)
                } else {
                    scale(Scale.FILL)
                }
            }
            .build()
    }

    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        contentScale = contentScale,
        onError = {
            if (resolvedFallback != null && activeModel != resolvedFallback) {
                activeModel = resolvedFallback
            }
        },
        modifier = modifier
    )
}
