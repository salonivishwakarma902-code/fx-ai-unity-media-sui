package com.example.ui.screens

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WallpaperItem
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassChip
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.components.WALLPERS_DAILY_RINGTONES
import com.example.ui.components.WallpersRingtonePlayer
import com.example.ui.components.ios27LiquidStretch
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RoseGlow
import com.example.ui.viewmodel.Ai8KGenUiState
import com.example.util.Ai8KEngineMode
import com.example.util.CloudAiImageApiClient
import com.example.util.WallpaperDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Ai8KStylePreset(
    val id: String,
    val label: String,
    val stylePrompt: String,
    val samplePrompt: String
)

private val AI_8K_STYLE_PRESETS = listOf(
    Ai8KStylePreset(
        id = "m3_expressive_8k",
        label = "✨ 8K Material Expressive",
        stylePrompt = "Material 3 Exclusive Android 17 Expressive abstract geometry, royal indigo and electric cyan gradients, 8K UHD 7680x4320",
        samplePrompt = "Sculptural royal indigo and violet geometric ribbons floating in deep space with crisp studio lighting"
    ),
    Ai8KStylePreset(
        id = "amoled_prism_8k",
        label = "🌑 8K AMOLED Prism",
        stylePrompt = "True Pitch Black AMOLED background, glowing neon bioluminescent edges, extreme contrast, 8K UHD",
        samplePrompt = "Pitch black AMOLED void with glowing electric blue and magenta crystal shards suspended in zero gravity"
    ),
    Ai8KStylePreset(
        id = "hypercar_8k",
        label = "🏎️ 8K Supercar Studio",
        stylePrompt = "Automotive studio photography, wet carbon fiber reflections, laser rim lighting, 8K ray-traced detail",
        samplePrompt = "Futuristic matte black hypercar in a neon-lit rain-slicked Tokyo tunnel with cyan laser reflections"
    ),
    Ai8KStylePreset(
        id = "pastel_nature_8k",
        label = "🏞️ 8K Pastel Nature",
        stylePrompt = "Ethereal floating island sanctuary, golden hour volumetric rays, ultra-crisp 8K botanical detail",
        samplePrompt = "Floating emerald cliff temple above glowing clouds at sunset with cherry blossoms and waterfalls"
    ),
    Ai8KStylePreset(
        id = "cyber_anime_8k",
        label = "⚡ 8K Anime Cinema",
        stylePrompt = "Makoto Shinkai inspired 8K anime sky, luminous starlight clouds, cinematic lens flare, ultra-detailed",
        samplePrompt = "Lone silhouette standing on a glass rooftop overlooking a glowing futuristic megacity under a comet sky"
    ),
    Ai8KStylePreset(
        id = "cosmic_nebula_8k",
        label = "🌌 8K Deep Space",
        stylePrompt = "James Webb telescope 8K cosmic nebula, iridescent stardust pillars, event horizon gravitational lensing",
        samplePrompt = "Supermassive black hole surrounded by a glowing turquoise and gold accretion disk in an 8K starfield"
    )
)

private val AI_EDIT_QUICK_PROMPTS = listOf(
    "Transform into 8K Material 3 Expressive geometric art with royal indigo & cyan lighting",
    "Make background true pitch-black AMOLED with glowing neon rim highlights",
    "Convert to Makoto Shinkai 8K anime sunset with volumetric clouds and starlight",
    "Add futuristic cyberpunk rain reflections, holographic mist, and laser glow",
    "Enhance with golden-hour studio lighting and crystalline 8K micro-textures"
)

private val AI_8K_CATEGORIES = listOf(
    "8K", "AMOLED", "Abstract", "Nature", "Supercars", "Anime", "Space", "Minimal"
)

private fun createDefaultReferenceCanvasBitmap(): Bitmap {
    val width = 768
    val height = 1024
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(0xFF090D1A.toInt(), 0xFF142850.toInt(), 0xFF27496D.toInt(), 0xFF00A8CC.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val orbPaint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x8838BDF8.toInt()
    }
    canvas.drawCircle(width * 0.35f, height * 0.38f, 180f, orbPaint1)

    val orbPaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x88A855F7.toInt()
    }
    canvas.drawCircle(width * 0.68f, height * 0.62f, 210f, orbPaint2)
    return bmp
}

@Composable
fun UserAiStudioScreen(
    aiState: Ai8KGenUiState,
    aiGeneratedHistory: List<WallpaperItem>,
    favoriteIds: Set<String>,
    onEnhancePrompt: (rawPrompt: String, stylePreset: String, onEnhanced: (String) -> Unit) -> Unit,
    onGenerate8KWallpaper: (userPrompt: String, titleInput: String, stylePreset: String, category: String) -> Unit,
    onEditImageWithAi: (sourceBitmap: Bitmap, editPrompt: String, titleInput: String, stylePreset: String, category: String) -> Unit = { _, _, _, _, _ -> },
    onResetAiState: () -> Unit,
    onSelectWallpaper: (WallpaperItem) -> Unit,
    onToggleFavorite: (WallpaperItem) -> Unit,
    onNotifySuccess: (String) -> Unit,
    onNotifyError: (String) -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Mode toggle: false = Create Image (Text-to-Image), true = Edit Image (Image + Prompt with gemini-nano-banana-2.1)
    var isEditMode by remember { mutableStateOf(false) }

    var promptText by remember {
        mutableStateOf(AI_8K_STYLE_PRESETS.first().samplePrompt)
    }
    var editInstructionText by remember {
        mutableStateOf(AI_EDIT_QUICK_PROMPTS.first())
    }
    var sourceEditBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }
    var sourceEditLabel by remember {
        mutableStateOf("No photo selected yet")
    }

    var titleText by remember {
        mutableStateOf("8K Liquid Prism")
    }
    var selectedStyle by remember {
        mutableStateOf(AI_8K_STYLE_PRESETS.first())
    }
    var selectedCategory by remember {
        mutableStateOf("8K")
    }
    var selectedEngine by remember {
        mutableStateOf(CloudAiImageApiClient.getSelectedEngine(context))
    }
    var customApiKeyInput by remember {
        mutableStateOf(CloudAiImageApiClient.getSavedCustomApiKey(context))
    }
    var showApiKeyPanel by remember {
        mutableStateOf(false)
    }
    var isSaving8KFile by remember { mutableStateOf(false) }

    // Zero-permission Android Photo Picker for selecting an image to edit with gemini-nano-banana-2.1
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val decoded = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }.getOrNull()
                }
                if (decoded != null) {
                    sourceEditBitmap = decoded
                    sourceEditLabel = "Selected from Device Gallery (${decoded.width}×${decoded.height})"
                    isEditMode = true
                    onNotifySuccess("Loaded image for Gemini Nano Banana 2.1 AI Editing!")
                } else {
                    onNotifyError("Could not decode the selected image.")
                }
            }
        }
    }

    val isBusy = aiState is Ai8KGenUiState.Generating8K || aiState is Ai8KGenUiState.EnhancingPrompt

    LaunchedEffect(aiState) {
        when (aiState) {
            is Ai8KGenUiState.Generating8K,
            is Ai8KGenUiState.Success,
            is Ai8KGenUiState.Error -> {
                runCatching { listState.animateScrollToItem(0) }
            }
            else -> Unit
        }
    }

    val playingRingtoneId by WallpersRingtonePlayer.playingRingtoneId.collectAsState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("user_ai_studio_list"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 112.dp, bottom = 128.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 0. ANDROID 17 DAILY RINGTONES STUDIO SOUNDBOARD (Matches WALLPERS Daily Ringtones + Music Dock Tab)
        item(key = "wallpers_daily_ringtones_studio_card") {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_ringtones_studio_card"),
                cornerRadius = 24.dp,
                glowColor = Color(0xFF4A56E2),
                secondaryGlowColor = ElectricCyan,
                elevated = true,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Daily Ringtones",
                                tint = Color(0xFF4A56E2),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Daily Ringtones • Android 17 Audio",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = glass.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap any ringtone card to play studio stereo chimes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glass.textSecondary
                                )
                            }
                        }

                        if (playingRingtoneId != null) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(RoseGlow.copy(alpha = 0.16f))
                                    .clickable { WallpersRingtonePlayer.stop() }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Stop Audio",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoseGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(WALLPERS_DAILY_RINGTONES, key = { "studio_${it.id}" }) { ringtone ->
                            val isPlaying = playingRingtoneId == ringtone.id
                            Box(
                                modifier = Modifier
                                    .width(136.dp)
                                    .height(78.dp)
                                    .ios27LiquidStretch(maxStretchScale = 0.08f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(ringtone.startColor, ringtone.endColor)
                                        )
                                    )
                                    .border(
                                        width = if (isPlaying) 2.dp else 0.dp,
                                        color = if (isPlaying) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        WallpersRingtonePlayer.toggleRingtone(ringtone) { started ->
                                            onNotifySuccess("Playing Daily Ringtone: ${started.title} (${started.durationLabel})")
                                        }
                                    }
                                    .padding(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = ringtone.title,
                                        tint = ringtone.endColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isPlaying) "♪ Playing..." else ringtone.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. LIVE 8K GENERATION STATUS / ERROR / RESULT CARD (Rendered at Top so it's immediately visible!)
        when (aiState) {
            is Ai8KGenUiState.Generating8K -> {
                item(key = "ai_8k_loading_top") {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_8k_loading_card"),
                        cornerRadius = 26.dp,
                        glowColor = ElectricCyan,
                        secondaryGlowColor = NeonViolet,
                        elevated = true,
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(
                                color = ElectricCyan,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = if (isEditMode) {
                                    "Editing Image with Gemini Nano Banana 2.1..."
                                } else {
                                    "Creating 8K Image with Gemini Nano Banana 2.1..."
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = glass.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Synthesizing ray-traced lighting & micro-textures with gemini-nano-banana-2.1 (typically 15–45 seconds).",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                        }
                    }
                }
            }

            is Ai8KGenUiState.Error -> {
                item(key = "ai_8k_error_top") {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_8k_error_card"),
                        cornerRadius = 24.dp,
                        glowColor = RoseGlow,
                        elevated = true,
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "8K Generation Error",
                                    tint = RoseGlow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "8K AI Synthesis Notice",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = glass.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = aiState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = glass.textSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                LiquidGlassButton(
                                    text = "Retry AI Render",
                                    icon = Icons.Default.Refresh,
                                    glowColor = ElectricCyan,
                                    filledAccent = true,
                                    testTag = "ai_8k_retry_button",
                                    onClick = {
                                        if (isEditMode) {
                                            val bmp = sourceEditBitmap ?: createDefaultReferenceCanvasBitmap().also {
                                                sourceEditBitmap = it
                                            }
                                            onEditImageWithAi(
                                                bmp,
                                                editInstructionText,
                                                titleText.ifBlank { "AI Edited 8K" },
                                                selectedStyle.stylePrompt,
                                                selectedCategory
                                            )
                                        } else {
                                            onGenerate8KWallpaper(
                                                promptText,
                                                titleText.ifBlank { "8K AI Masterpiece" },
                                                selectedStyle.stylePrompt,
                                                selectedCategory
                                            )
                                        }
                                    }
                                )
                                LiquidGlassButton(
                                    text = "Dismiss",
                                    glowColor = glass.glassBorderSubtle,
                                    onClick = onResetAiState
                                )
                            }
                        }
                    }
                }
            }

            is Ai8KGenUiState.Success -> {
                item(key = "ai_8k_result_top") {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_8k_result_card"),
                        cornerRadius = 28.dp,
                        glowColor = EmeraldGlow,
                        secondaryGlowColor = ElectricCyan,
                        elevated = true,
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "8K Ready",
                                        tint = EmeraldGlow,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = aiState.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = glass.textPrimary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "Gemini Nano Banana 2.1 • Added to Pixora Library",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldGlow
                                        )
                                    }
                                }
                            }

                            // High-Resolution 8K Generated Bitmap Preview with iOS 27 Stretch
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp)
                                    .ios27LiquidStretch(maxStretchScale = 0.08f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                                    .clickable {
                                        aiGeneratedHistory.firstOrNull()?.let { wall ->
                                            onSelectWallpaper(wall)
                                        }
                                    }
                            ) {
                                Image(
                                    bitmap = remember(aiState.bitmap) { aiState.bitmap.asImageBitmap() },
                                    contentDescription = aiState.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Top-Left 8K Master Badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                        .ios27LiquidStretch()
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .border(1.dp, ElectricCyan, CircleShape)
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "8K MASTER • GEMINI NANO BANANA 2.1",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            // Quick button to load this generated image directly into AI Edit Mode!
                            LiquidGlassButton(
                                text = "Edit This Image with AI Prompt",
                                icon = Icons.Default.AutoFixHigh,
                                glowColor = NeonViolet,
                                secondaryGlowColor = ElectricCyan,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "ai_edit_generated_image_button",
                                onClick = {
                                    sourceEditBitmap = aiState.bitmap
                                    sourceEditLabel = "Loaded from '${aiState.title}'"
                                    isEditMode = true
                                    onNotifySuccess("Loaded '${aiState.title}' into AI Image Editor!")
                                }
                            )

                            // Download 8K PNG + Apply Home / Lock Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                LiquidGlassButton(
                                    text = if (isSaving8KFile) "Saving 8K..." else "Download 8K PNG",
                                    icon = Icons.Default.Download,
                                    glowColor = EmeraldGlow,
                                    filledAccent = true,
                                    enabled = !isSaving8KFile,
                                    modifier = Modifier.weight(1f),
                                    testTag = "ai_8k_download_button",
                                    onClick = {
                                        scope.launch {
                                            isSaving8KFile = true
                                            val result = WallpaperDownloader.saveBitmapAs8KWallpaper(
                                                context = context,
                                                bitmap = aiState.bitmap,
                                                title = aiState.title
                                            )
                                            isSaving8KFile = false
                                            result.fold(
                                                onSuccess = { msg -> onNotifySuccess(msg) },
                                                onFailure = { err ->
                                                    onNotifyError(err.localizedMessage ?: "Failed to save 8K PNG")
                                                }
                                            )
                                        }
                                    }
                                )

                                LiquidGlassButton(
                                    text = "Set Home",
                                    icon = Icons.Default.Home,
                                    glowColor = ElectricCyan,
                                    modifier = Modifier.weight(0.75f),
                                    testTag = "ai_8k_apply_home_button",
                                    onClick = {
                                        scope.launch {
                                            val res = WallpaperDownloader.applyBitmapDirectly(
                                                context = context,
                                                bitmap = aiState.bitmap,
                                                title = aiState.title,
                                                targetFlags = WallpaperManager.FLAG_SYSTEM
                                            )
                                            res.fold(
                                                onSuccess = { onNotifySuccess(it) },
                                                onFailure = { onNotifyError(it.localizedMessage ?: "Failed to set wallpaper") }
                                            )
                                        }
                                    }
                                )

                                LiquidGlassButton(
                                    text = "Set Lock",
                                    icon = Icons.Default.Lock,
                                    glowColor = AmberGlow,
                                    modifier = Modifier.weight(0.75f),
                                    testTag = "ai_8k_apply_lock_button",
                                    onClick = {
                                        scope.launch {
                                            val res = WallpaperDownloader.applyBitmapDirectly(
                                                context = context,
                                                bitmap = aiState.bitmap,
                                                title = aiState.title,
                                                targetFlags = WallpaperManager.FLAG_LOCK
                                            )
                                            res.fold(
                                                onSuccess = { onNotifySuccess(it) },
                                                onFailure = { onNotifyError(it.localizedMessage ?: "Failed to set lock screen") }
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            else -> Unit
        }

        // 2. 8K AI Wallpaper Creator & Editor Hero Card
        item(key = "ai_8k_hero_form") {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_8k_hero_card"),
                cornerRadius = 26.dp,
                glowColor = ElectricCyan,
                secondaryGlowColor = NeonViolet,
                elevated = true,
                contentPadding = PaddingValues(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .ios27LiquidStretch()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(ElectricCyan, NeonViolet))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isEditMode) Icons.Default.AutoFixHigh else Icons.Default.AutoAwesome,
                                    contentDescription = "8K AI Studio",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Pixora AI Image Studio",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp
                                    ),
                                    color = glass.textPrimary
                                )
                                Text(
                                    text = "Create & edit images with Gemini Nano Banana 2.1",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glass.textSecondary
                                )
                            }
                        }

                        // 8K UHD Resolution Pill Badge
                        Box(
                            modifier = Modifier
                                .ios27LiquidStretch()
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.18f))
                                .border(1.dp, ElectricCyan, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HighQuality,
                                    contentDescription = "8K Quality",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "8K UHD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glass.textPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // CREATE IMAGE vs EDIT IMAGE Mode Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassChip(
                            label = "✨ Create Image",
                            selected = !isEditMode,
                            accentColor = ElectricCyan,
                            onClick = { isEditMode = false },
                            modifier = Modifier.weight(1f),
                            testTag = "mode_create_image_chip"
                        )
                        LiquidGlassChip(
                            label = "🪄 Edit Image",
                            selected = isEditMode,
                            accentColor = NeonViolet,
                            onClick = {
                                isEditMode = true
                                if (sourceEditBitmap == null) {
                                    val latestBmp = (aiState as? Ai8KGenUiState.Success)?.bitmap
                                    if (latestBmp != null) {
                                        sourceEditBitmap = latestBmp
                                        sourceEditLabel = "Loaded from latest AI creation"
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "mode_edit_image_chip"
                        )
                    }

                    // 8K Cloud AI Engine & API Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AI Image Model & Engine",
                                style = MaterialTheme.typography.labelLarge,
                                color = glass.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Active: ${selectedEngine.label} (${selectedEngine.badge})",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LiquidGlassChip(
                            label = if (showApiKeyPanel) "Hide API Key" else "🔑 API Key",
                            selected = showApiKeyPanel || customApiKeyInput.isNotBlank(),
                            accentColor = AmberGlow,
                            onClick = { showApiKeyPanel = !showApiKeyPanel },
                            testTag = "toggle_ai_api_key_button"
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Ai8KEngineMode.entries, key = { it.id }) { engine ->
                            LiquidGlassChip(
                                label = engine.label,
                                selected = selectedEngine == engine,
                                accentColor = EmeraldGlow,
                                onClick = {
                                    selectedEngine = engine
                                    CloudAiImageApiClient.saveSelectedEngine(context, engine)
                                    onNotifySuccess("Switched 8K AI Engine to ${engine.label}")
                                },
                                testTag = "ai_engine_chip_${engine.id}"
                            )
                        }
                    }

                    if (showApiKeyPanel) {
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp,
                            glowColor = AmberGlow,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Custom Gemini API Key (Optional)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = glass.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Gemini Nano Banana 2.1 via Firebase AI Logic & Flux 8K Cloud work automatically. Optionally paste a custom key:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glass.textSecondary
                                )
                                LiquidGlassTextField(
                                    value = customApiKeyInput,
                                    onValueChange = { customApiKeyInput = it },
                                    placeholder = "Paste Gemini API Key (AIza...) or leave blank",
                                    testTag = "ai_custom_api_key_input"
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    LiquidGlassButton(
                                        text = "Save API Key",
                                        onClick = {
                                            CloudAiImageApiClient.saveCustomApiKey(context, customApiKeyInput)
                                            showApiKeyPanel = false
                                            onNotifySuccess(
                                                if (customApiKeyInput.isNotBlank()) {
                                                    "Custom Gemini API Key saved & activated!"
                                                } else {
                                                    "Using Gemini Nano Banana 2.1 AI Logic!"
                                                }
                                            )
                                        },
                                        filledAccent = true,
                                        glowColor = EmeraldGlow,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("save_ai_api_key_button")
                                    )
                                    if (customApiKeyInput.isNotBlank()) {
                                        LiquidGlassButton(
                                            text = "Clear",
                                            onClick = {
                                                customApiKeyInput = ""
                                                CloudAiImageApiClient.saveCustomApiKey(context, "")
                                                onNotifySuccess("Cleared custom key; using Gemini Nano Banana 2.1")
                                            },
                                            filledAccent = false,
                                            glowColor = RoseGlow,
                                            modifier = Modifier.weight(0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 8K Style Preset Selector
                    Text(
                        text = "8K Optical Style Preset",
                        style = MaterialTheme.typography.labelLarge,
                        color = glass.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AI_8K_STYLE_PRESETS, key = { it.id }) { preset ->
                            val isSelected = preset.id == selectedStyle.id
                            LiquidGlassChip(
                                label = preset.label,
                                selected = isSelected,
                                accentColor = ElectricCyan,
                                onClick = {
                                    selectedStyle = preset
                                    if (!isEditMode && (promptText.isBlank() || AI_8K_STYLE_PRESETS.any { it.samplePrompt == promptText })) {
                                        promptText = preset.samplePrompt
                                    }
                                },
                                testTag = "ai_style_preset_${preset.id}"
                            )
                        }
                    }

                    // Target Category Selector
                    Text(
                        text = "Publish Category",
                        style = MaterialTheme.typography.labelLarge,
                        color = glass.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AI_8K_CATEGORIES, key = { it }) { category ->
                            LiquidGlassChip(
                                label = category,
                                selected = selectedCategory == category,
                                accentColor = NeonViolet,
                                onClick = { selectedCategory = category },
                                testTag = "ai_category_chip_$category"
                            )
                        }
                    }

                    // Wallpaper Title Input
                    Text(
                        text = "8K Wallpaper Title",
                        style = MaterialTheme.typography.labelLarge,
                        color = glass.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    LiquidGlassTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        placeholder = "e.g., Obsidian 8K Prism",
                        testTag = "ai_8k_title_input"
                    )

                    if (isEditMode) {
                        // =========================================================================
                        // IMAGE EDITING UI (Multimodal Image + Prompt -> Edited Bitmap via gemini-nano-banana-2.1)
                        // =========================================================================
                        Text(
                            text = "Source Image to Edit (gemini-nano-banana-2.1)",
                            style = MaterialTheme.typography.labelLarge,
                            color = glass.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LiquidGlassButton(
                                text = "Pick Photo",
                                icon = Icons.Default.AddPhotoAlternate,
                                glowColor = ElectricCyan,
                                filledAccent = true,
                                modifier = Modifier.weight(1f),
                                testTag = "pick_photo_for_ai_edit_button",
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            LiquidGlassButton(
                                text = "Use Sample Canvas",
                                icon = Icons.Default.Edit,
                                glowColor = NeonViolet,
                                modifier = Modifier.weight(1f),
                                testTag = "use_sample_canvas_for_ai_edit_button",
                                onClick = {
                                    sourceEditBitmap = createDefaultReferenceCanvasBitmap()
                                    sourceEditLabel = "8K Liquid Prism Base Canvas (768×1024)"
                                    onNotifySuccess("Loaded sample base canvas for AI editing!")
                                }
                            )
                        }

                        val activeSource = sourceEditBitmap
                        if (activeSource != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.dp, NeonViolet.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                            ) {
                                Image(
                                    bitmap = remember(activeSource) { activeSource.asImageBitmap() },
                                    contentDescription = "Source Image for AI Edit",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = sourceEditLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NeonViolet.copy(alpha = 0.10f))
                                    .border(1.dp, NeonViolet.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tap 'Pick Photo' to choose an image from your gallery, or 'Use Sample Canvas' to start editing immediately.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glass.textSecondary
                                )
                            }
                        }

                        Text(
                            text = "Quick AI Edit Ideas",
                            style = MaterialTheme.typography.labelLarge,
                            color = glass.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AI_EDIT_QUICK_PROMPTS, key = { it }) { idea ->
                                LiquidGlassChip(
                                    label = idea.take(34) + "...",
                                    selected = editInstructionText == idea,
                                    accentColor = NeonViolet,
                                    onClick = { editInstructionText = idea }
                                )
                            }
                        }

                        Text(
                            text = "Describe How to Edit This Image",
                            style = MaterialTheme.typography.labelLarge,
                            color = glass.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        LiquidGlassTextField(
                            value = editInstructionText,
                            onValueChange = { editInstructionText = it },
                            placeholder = "e.g., Add neon cyber reflections, convert sky to cosmic nebula...",
                            singleLine = false,
                            testTag = "ai_8k_edit_prompt_input"
                        )

                        LiquidGlassButton(
                            text = if (aiState is Ai8KGenUiState.Generating8K) "Editing Image..." else "Apply AI Edit (Gemini Nano Banana 2.1)",
                            icon = Icons.Default.AutoFixHigh,
                            glowColor = NeonViolet,
                            secondaryGlowColor = ElectricCyan,
                            filledAccent = true,
                            enabled = !isBusy && editInstructionText.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "ai_8k_apply_edit_button",
                            onClick = {
                                val targetBitmap = sourceEditBitmap ?: createDefaultReferenceCanvasBitmap().also {
                                    sourceEditBitmap = it
                                    sourceEditLabel = "8K Liquid Prism Base Canvas (768×1024)"
                                }
                                onEditImageWithAi(
                                    targetBitmap,
                                    editInstructionText,
                                    titleText.ifBlank { "AI Edited 8K" },
                                    selectedStyle.stylePrompt,
                                    selectedCategory
                                )
                            }
                        )
                    } else {
                        // =========================================================================
                        // TEXT-TO-IMAGE CREATION UI (gemini-nano-banana-2.1)
                        // =========================================================================
                        Text(
                            text = "Describe your 8K Wallpaper",
                            style = MaterialTheme.typography.labelLarge,
                            color = glass.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        LiquidGlassTextField(
                            value = promptText,
                            onValueChange = { promptText = it },
                            placeholder = "Describe colors, lighting, textures, and subject for 8K generation...",
                            singleLine = false,
                            testTag = "ai_8k_prompt_input"
                        )

                        // Live Streaming Prompt Enhancement Preview
                        AnimatedVisibility(visible = aiState is Ai8KGenUiState.EnhancingPrompt) {
                            val partial = (aiState as? Ai8KGenUiState.EnhancingPrompt)?.streamingText.orEmpty()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NeonViolet.copy(alpha = 0.14f))
                                    .border(1.dp, NeonViolet.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = NeonViolet
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = partial.ifBlank { "Enhancing prompt to 8K UHD specifications..." },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glass.textPrimary
                                )
                            }
                        }

                        // Action Buttons Row: Enhance Prompt + Create 8K Image
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LiquidGlassButton(
                                text = "Enhance 8K Prompt",
                                icon = Icons.Default.AutoAwesome,
                                glowColor = NeonViolet,
                                enabled = !isBusy,
                                modifier = Modifier.weight(0.46f),
                                testTag = "ai_8k_enhance_button",
                                onClick = {
                                    onEnhancePrompt(promptText, selectedStyle.stylePrompt) { enhanced ->
                                        promptText = enhanced
                                    }
                                }
                            )

                            LiquidGlassButton(
                                text = if (aiState is Ai8KGenUiState.Generating8K) "Rendering 8K..." else "Create 8K Image",
                                icon = Icons.Default.HighQuality,
                                glowColor = ElectricCyan,
                                secondaryGlowColor = NeonViolet,
                                filledAccent = true,
                                enabled = !isBusy && promptText.isNotBlank(),
                                modifier = Modifier.weight(0.54f),
                                testTag = "ai_8k_generate_button",
                                onClick = {
                                    onGenerate8KWallpaper(
                                        promptText,
                                        titleText.ifBlank { "8K AI Masterpiece" },
                                        selectedStyle.stylePrompt,
                                        selectedCategory
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. Previously Created & Edited 8K AI Wallpapers Gallery
        if (aiGeneratedHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = "Created 8K Wallpapers",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your Created & Edited 8K Wallpapers (${aiGeneratedHistory.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = glass.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val rows = aiGeneratedHistory.chunked(2)
            items(rows) { rowPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowPair.forEach { wall ->
                        WallpaperGridCard(
                            wallpaper = wall,
                            isFavorited = wall.id in favoriteIds,
                            onClick = { onSelectWallpaper(wall) },
                            onToggleFavorite = { onToggleFavorite(wall) },
                            modifier = Modifier.weight(1f),
                            cardHeight = 236.dp
                        )
                    }
                    if (rowPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(140.dp)) }
    }
}
