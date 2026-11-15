package com.example.ui.components

import android.app.WallpaperManager
import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WallpaperItem
import com.example.ui.components.GlassIntensity
import com.example.ui.components.pureLiquidGlass
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.util.WallpaperDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WallpaperEffectsStudioDialog(
    wallpaper: WallpaperItem,
    onDismiss: () -> Unit,
    onApplyToDevice: (blurDp: Float, dimAlpha: Float, filterName: String, targetFlags: Int) -> Unit,
    onDownloadWithEffects: (blurDp: Float, dimAlpha: Float, filterName: String) -> Unit,
    onCopyColorHex: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val glass = LocalLiquidGlassColors.current

    var blurDp by remember { mutableFloatStateOf(0f) }
    var dimAlpha by remember { mutableFloatStateOf(0f) }
    var selectedFilter by remember { mutableStateOf("None") }
    var dominantColors by remember { mutableStateOf(listOf("#5B2EFF", "#2563EB", "#10B981", "#F59E0B", "#0F172A")) }
    var rawBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val filterOptions = remember {
        listOf("None", "AMOLED Noir", "Cyberpunk", "Sunset", "Emerald", "Glacier")
    }

    LaunchedEffect(wallpaper.id) {
        withContext(Dispatchers.IO) {
            val bmp = WallpaperDownloader.fetchWallpaperBitmap(context, wallpaper) {}
            if (bmp != null) {
                rawBitmap = bmp
                val palette = WallpaperDownloader.extractDominantColorPalette(bmp)
                withContext(Dispatchers.Main) {
                    dominantColors = palette
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .pureLiquidGlass(cornerRadius = 28.dp, intensity = GlassIntensity.UltraGlossy)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Effects & Monet Studio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = glass.textPrimary
                            )
                            Text(
                                text = "Customize blur, vignette & color tones",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = glass.textPrimary
                        )
                    }
                }

                // Interactive Live Preview Window
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, if (glass.isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (blurDp > 0.5f) Modifier.blur(blurDp.dp) else Modifier)
                    ) {
                        WallxAsyncImage(
                            imageUrl = wallpaper.imageUrl.ifBlank { wallpaper.thumbnailUrl },
                            contentDescription = wallpaper.title,
                            contentScale = ContentScale.Crop,
                            isFullQualityInspection = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Tint overlay simulation
                    when (selectedFilter) {
                        "AMOLED Noir" -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f))
                        )
                        "Cyberpunk" -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF9333EA).copy(alpha = 0.22f))
                        )
                        "Sunset" -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFEA580C).copy(alpha = 0.22f))
                        )
                        "Emerald" -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF16A34A).copy(alpha = 0.22f))
                        )
                        "Glacier" -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0EA5E9).copy(alpha = 0.22f))
                        )
                    }

                    // Dimming overlay
                    if (dimAlpha > 0.01f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = dimAlpha))
                        )
                    }

                    // Live badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Live 4K Preview",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // 1. Icon Readability Blur Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Icon Readability Blur",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = glass.textPrimary
                        )
                        Text(
                            text = "${blurDp.toInt()} dp",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF6366F1)
                        )
                    }
                    Slider(
                        value = blurDp,
                        onValueChange = { blurDp = it },
                        valueRange = 0f..24f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF6366F1),
                            activeTrackColor = Color(0xFF6366F1)
                        )
                    )
                }

                // 2. Dimming Vignette Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vignette & Contrast Dimming",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = glass.textPrimary
                        )
                        Text(
                            text = "${(dimAlpha * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF6366F1)
                        )
                    }
                    Slider(
                        value = dimAlpha,
                        onValueChange = { dimAlpha = it },
                        valueRange = 0f..0.7f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF6366F1),
                            activeTrackColor = Color(0xFF6366F1)
                        )
                    )
                }

                // 3. Color Tone Filter Chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Color Tone Preset",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = glass.textPrimary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filterOptions) { filter ->
                            val isSelected = filter == selectedFilter
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFF6366F1)
                                        else (if (glass.isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF818CF8) else (if (glass.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                        CircleShape
                                    )
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = filter,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else glass.textPrimary
                                )
                            }
                        }
                    }
                }

                // 4. Material You Monet Palette Extractor
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monet Theme Palette",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = glass.textPrimary
                        )
                        Text(
                            text = "Tap to Copy Hex",
                            style = MaterialTheme.typography.labelSmall,
                            color = glass.textSecondary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dominantColors.forEach { hex ->
                            val parsedColor = runCatching {
                                Color(android.graphics.Color.parseColor(hex))
                            }.getOrDefault(Color.Gray)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { onCopyColorHex(hex) }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(parsedColor)
                                        .border(2.dp, Color.White, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = hex,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = glass.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons: Apply to Home / Lock / Save to Gallery
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Apply to Home
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6366F1))
                                .clickable {
                                    onApplyToDevice(blurDp, dimAlpha, selectedFilter, WallpaperManager.FLAG_SYSTEM)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Home Screen",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        // Apply to Lock
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (glass.isDark) Color(0xFF1E293B) else Color(0xFFEEF2FF))
                                .border(1.dp, if (glass.isDark) Color(0xFF334155) else Color(0xFFC7D2FE), RoundedCornerShape(12.dp))
                                .clickable {
                                    onApplyToDevice(blurDp, dimAlpha, selectedFilter, WallpaperManager.FLAG_LOCK)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (glass.isDark) Color.White else Color(0xFF4F46E5),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Lock Screen",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (glass.isDark) Color.White else Color(0xFF4F46E5)
                                )
                            }
                        }
                    }

                    // Save to Gallery with Effects
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (glass.isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .border(1.dp, if (glass.isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .clickable {
                                onDownloadWithEffects(blurDp, dimAlpha, selectedFilter)
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = glass.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save Customized 4K to Gallery",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = glass.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
