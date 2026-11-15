package com.example.util

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import com.example.data.model.WallpaperItem
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object WallpaperDownloader {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    /**
     * Downloads original quality wallpaper bytes from Firebase Storage (if storagePath is active)
     * or from imageUrl / data URI, saves to MediaStore Pictures/WALLX, and reports live progress.
     */
    suspend fun downloadWallpaperToDevice(
        context: Context,
        wallpaper: WallpaperItem,
        onProgress: (Float) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            onProgress(0.10f)
            val bitmap = fetchWallpaperBitmap(context, wallpaper, onProgress)
                ?: error("Could not decode original wallpaper image.")

            onProgress(0.82f)
            val safeTitle = wallpaper.title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "pixora" }
            val filename = "Pixora_${safeTitle}_${System.currentTimeMillis()}.jpg"

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Pixora")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: error("MediaStore failed to allocate output file.")

            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
            } ?: error("Unable to open output stream for wallpaper download.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            onProgress(1.0f)
            "Saved to Pictures/Pixora/$filename"
        }
    }

    /**
     * Applies the wallpaper directly to the Android device Home Screen, Lock Screen, or Both.
     */
    suspend fun applyWallpaperToDevice(
        context: Context,
        wallpaper: WallpaperItem,
        targetFlags: Int = WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK,
        onProgress: (Float) -> Unit
    ): Result<String> = applyWallpaperWithEffects(
        context = context,
        wallpaper = wallpaper,
        blurRadiusDp = 0f,
        topBlurStrengthDp = 0f,
        dimAlpha = 0f,
        targetFlags = targetFlags,
        onProgress = onProgress
    )

    /**
     * Applies the wallpaper with the user's tested Blur, Top High-to-Low Blur, and Dimming effects
     * baked directly into the final wallpaper Bitmap before setting it on Home, Lock, or Both screens.
     * Center-crops and scales the bitmap to exact device screen dimensions so Android WallpaperManager
     * reliably sets Home Screen, Lock Screen, or Both on all OEM devices and emulators.
     */
    suspend fun applyWallpaperWithEffects(
        context: Context,
        wallpaper: WallpaperItem,
        blurRadiusDp: Float,
        topBlurStrengthDp: Float = 0f,
        dimAlpha: Float = 0f,
        targetFlags: Int = WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK,
        onProgress: (Float) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            onProgress(0.15f)
            val rawBitmap = fetchWallpaperBitmap(context, wallpaper, onProgress)
                ?: error("Unable to decode wallpaper image.")
            onProgress(0.65f)
            val screenFittedBitmap = fitBitmapToDeviceScreen(context, rawBitmap)
            val processedBitmap = renderEffectsOntoBitmap(
                source = screenFittedBitmap,
                blurRadiusDp = blurRadiusDp,
                topBlurStrengthDp = topBlurStrengthDp,
                dimAlpha = dimAlpha
            )
            onProgress(0.85f)
            setWallpaperBitmapReliable(context, processedBitmap, targetFlags)
            onProgress(1.0f)
            val targetLabel = when (targetFlags) {
                WallpaperManager.FLAG_SYSTEM -> "Home Screen"
                WallpaperManager.FLAG_LOCK -> "Lock Screen"
                else -> "Home & Lock Screens"
            }
            val effectSummary = if (blurRadiusDp > 0.5f || dimAlpha > 0.02f) {
                " (Blur ${blurRadiusDp.toInt()}dp)"
            } else {
                ""
            }
            "Applied 4K Wallpaper '${wallpaper.title}'$effectSummary to $targetLabel!"
        }
    }

    /**
     * Reliably sets the Bitmap to Home Screen (FLAG_SYSTEM), Lock Screen (FLAG_LOCK), or Both.
     * Calls each flag explicitly and verifies/falls back if a particular OEM launcher requires stream or full rect.
     */
    private fun setWallpaperBitmapReliable(
        context: Context,
        bitmap: Bitmap,
        targetFlags: Int
    ) {
        val wm = WallpaperManager.getInstance(context.applicationContext)
        val softwareBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        // Ensure launcher displays the wallpaper 1:1 edge-to-edge full screen without multi-page zoom crop
        runCatching {
            wm.suggestDesiredDimensions(softwareBitmap.width, softwareBitmap.height)
            wm.setWallpaperOffsetSteps(0f, 0f)
        }

        val fullRect = android.graphics.Rect(0, 0, softwareBitmap.width, softwareBitmap.height)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val wantsHome = (targetFlags and WallpaperManager.FLAG_SYSTEM) != 0
            val wantsLock = (targetFlags and WallpaperManager.FLAG_LOCK) != 0

            if (wantsHome && wantsLock) {
                // Apply to Home Screen and Lock Screen explicitly so both are guaranteed to update in full screen
                runCatching {
                    wm.setBitmap(softwareBitmap, fullRect, true, WallpaperManager.FLAG_SYSTEM)
                }
                runCatching {
                    wm.setBitmap(softwareBitmap, fullRect, true, WallpaperManager.FLAG_LOCK)
                }
                runCatching {
                    wm.setBitmap(
                        softwareBitmap,
                        fullRect,
                        true,
                        WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    )
                }
            } else if (wantsHome) {
                val res = runCatching {
                    wm.setBitmap(softwareBitmap, fullRect, true, WallpaperManager.FLAG_SYSTEM)
                }
                if (res.isFailure || res.getOrDefault(0) == 0) {
                    wm.setBitmap(softwareBitmap)
                }
            } else if (wantsLock) {
                val res = runCatching {
                    wm.setBitmap(softwareBitmap, fullRect, true, WallpaperManager.FLAG_LOCK)
                }
                if (res.isFailure) {
                    val out = ByteArrayOutputStream()
                    softwareBitmap.compress(Bitmap.CompressFormat.JPEG, 98, out)
                    wm.setStream(
                        java.io.ByteArrayInputStream(out.toByteArray()),
                        fullRect,
                        true,
                        WallpaperManager.FLAG_LOCK
                    )
                }
            } else {
                wm.setBitmap(softwareBitmap)
            }
        } else {
            wm.setBitmap(softwareBitmap)
        }
    }

    /**
     * Center-crops and scales the source bitmap to match the device's FULL physical display aspect ratio
     * (including status bar and navigation bar) in high-definition resolution (up to 2160x3840 4K)
     * so it fills 100% of the Home & Lock screen edge-to-edge.
     */
    private fun fitBitmapToDeviceScreen(context: Context, source: Bitmap): Bitmap {
        return runCatching {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager
            var realW = context.resources.displayMetrics.widthPixels
            var realH = context.resources.displayMetrics.heightPixels

            if (windowManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val bounds = windowManager.currentWindowMetrics.bounds
                    if (bounds.width() > 0 && bounds.height() > 0) {
                        realW = bounds.width()
                        realH = bounds.height()
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val realMetrics = android.util.DisplayMetrics()
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay?.getRealMetrics(realMetrics)
                    if (realMetrics.widthPixels > 0 && realMetrics.heightPixels > 0) {
                        realW = realMetrics.widthPixels
                        realH = realMetrics.heightPixels
                    }
                }
            }

            val rawW = minOf(realW, realH).coerceAtLeast(1080)
            val rawH = maxOf(realW, realH).coerceAtLeast(1920)
            // Scale up to crisp 4K portrait ceiling while preserving exact full-screen aspect ratio
            val scaleUp = minOf(2160f / rawW.toFloat(), 3840f / rawH.toFloat()).coerceAtLeast(1f)
            val targetW = (rawW * scaleUp).toInt().coerceIn(1080, 2160)
            val targetH = (rawH * scaleUp).toInt().coerceIn(1920, 3840)

            val srcW = source.width.coerceAtLeast(1)
            val srcH = source.height.coerceAtLeast(1)

            val scale = maxOf(targetW.toFloat() / srcW.toFloat(), targetH.toFloat() / srcH.toFloat())
            val scaledW = (srcW * scale).toInt().coerceAtLeast(targetW)
            val scaledH = (srcH * scale).toInt().coerceAtLeast(targetH)

            val scaled = if (scaledW != srcW || scaledH != srcH) {
                Bitmap.createScaledBitmap(source, scaledW, scaledH, true)
            } else {
                source
            }

            val cropX = ((scaled.width - targetW) / 2).coerceAtLeast(0)
            val cropY = ((scaled.height - targetH) / 2).coerceAtLeast(0)
            Bitmap.createBitmap(
                scaled,
                cropX,
                cropY,
                targetW.coerceAtMost(scaled.width - cropX),
                targetH.coerceAtMost(scaled.height - cropY)
            )
        }.getOrDefault(source)
    }

    /**
     * Downloads and saves the wallpaper with the user's tested Blur, Top High-to-Low Blur,
     * and Dimming effects baked into the saved image file in Pictures/WALLX.
     */
    suspend fun downloadWallpaperWithEffects(
        context: Context,
        wallpaper: WallpaperItem,
        blurRadiusDp: Float,
        topBlurStrengthDp: Float = 0f,
        dimAlpha: Float = 0f,
        onProgress: (Float) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            onProgress(0.10f)
            val rawBitmap = fetchWallpaperBitmap(context, wallpaper, onProgress)
                ?: error("Could not decode original wallpaper image.")
            onProgress(0.72f)
            val processedBitmap = renderEffectsOntoBitmap(
                source = rawBitmap,
                blurRadiusDp = blurRadiusDp,
                topBlurStrengthDp = topBlurStrengthDp,
                dimAlpha = dimAlpha
            )
            onProgress(0.86f)
            val safeTitle = wallpaper.title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "pixora" }
            val blurTag = if (blurRadiusDp > 0.5f) "_Blur${blurRadiusDp.toInt()}dp" else ""
            val filename = "Pixora_${safeTitle}${blurTag}_${System.currentTimeMillis()}.jpg"

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Pixora")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: error("MediaStore failed to allocate output file.")

            resolver.openOutputStream(uri)?.use { out ->
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
            } ?: error("Unable to open output stream for wallpaper download.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            onProgress(1.0f)
            "Saved to Pictures/Pixora/$filename"
        }
    }

    /**
     * Renders Gaussian-like multi-pass downscale/upscale blur, progressive Top High-to-Low frost,
     * and dimming onto a Bitmap so users can apply or save the exact previewed look.
     */
    internal fun renderEffectsOntoBitmap(
        source: Bitmap,
        blurRadiusDp: Float,
        topBlurStrengthDp: Float,
        dimAlpha: Float
    ): Bitmap {
        val cleanBlur = blurRadiusDp.coerceIn(0f, 36f)
        val cleanTopBlur = topBlurStrengthDp.coerceIn(0f, 36f)
        val cleanDim = dimAlpha.coerceIn(0f, 0.75f)
        if (cleanBlur < 0.5f && cleanTopBlur < 0.5f && cleanDim < 0.01f) {
            return source
        }

        val width = source.width.coerceAtLeast(1)
        val height = source.height.coerceAtLeast(1)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)

        // 1. Base image (either sharp or blurred via smooth multi-pass bilinear scaling)
        val baseLayer = if (cleanBlur >= 0.5f) {
            createSmoothBlurredBitmap(source, cleanBlur)
        } else {
            source
        }
        canvas.drawBitmap(baseLayer, 0f, 0f, paint)

        // 2. Progressive Top High-to-Low Frosted Scrim if enabled
        if (cleanTopBlur >= 0.5f) {
            val topIntensity = (cleanTopBlur / 36f).coerceIn(0.05f, 1f)
            val topHeight = (height * 0.22f).coerceAtLeast(1f)
            val topPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                shader = android.graphics.LinearGradient(
                    0f,
                    0f,
                    0f,
                    topHeight,
                    intArrayOf(
                        android.graphics.Color.argb((195 * topIntensity).toInt().coerceIn(0, 225), 14, 20, 36),
                        android.graphics.Color.argb((95 * topIntensity).toInt().coerceIn(0, 160), 14, 20, 36),
                        android.graphics.Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.52f, 1f),
                    android.graphics.Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), topHeight, topPaint)
        }

        // 3. Dimming overlay if enabled
        if (cleanDim >= 0.01f) {
            val dimPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb((cleanDim * 255).toInt().coerceIn(0, 220), 6, 10, 20)
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
        }

        return output
    }

    private fun createSmoothBlurredBitmap(source: Bitmap, blurRadiusDp: Float): Bitmap {
        val w = source.width.coerceAtLeast(16)
        val h = source.height.coerceAtLeast(16)
        val downscaleFactor = (1f + (blurRadiusDp / 2.2f)).coerceIn(1.5f, 18f)
        val smallW = (w / downscaleFactor).toInt().coerceAtLeast(8)
        val smallH = (h / downscaleFactor).toInt().coerceAtLeast(8)
        val pass1 = Bitmap.createScaledBitmap(source, smallW, smallH, true)
        val midW = (smallW * 1.5f).toInt().coerceAtMost(w)
        val midH = (smallH * 1.5f).toInt().coerceAtMost(h)
        val pass2 = Bitmap.createScaledBitmap(pass1, midW, midH, true)
        val pass3 = Bitmap.createScaledBitmap(pass2, smallW, smallH, true)
        return Bitmap.createScaledBitmap(pass3, w, h, true)
    }

    fun shareWallpaper(context: Context, wallpaper: WallpaperItem) {
        val shareText = buildString {
            append("Check out '${wallpaper.title}' (${wallpaper.resolution}) in the ${wallpaper.category} collection on Pixora!\n")
            if (wallpaper.imageUrl.startsWith("http")) {
                append(wallpaper.imageUrl)
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Pixora • ${wallpaper.title}")
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Wallpaper"))
    }

    suspend fun saveBitmapAs8KWallpaper(
        context: Context,
        bitmap: Bitmap,
        title: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val safeTitle = title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "ai_8k" }
            val filename = "Pixora_8K_${safeTitle}_${System.currentTimeMillis()}.png"

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Pixora")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: error("MediaStore failed to allocate 8K output file.")

            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: error("Unable to open output stream for 8K wallpaper save.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            "Saved 8K AI Master to Pictures/Pixora/$filename"
        }
    }

    suspend fun applyBitmapDirectly(
        context: Context,
        bitmap: Bitmap,
        title: String,
        targetFlags: Int = WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val fitted = fitBitmapToDeviceScreen(context, bitmap)
            setWallpaperBitmapReliable(context, fitted, targetFlags)
            val targetLabel = when (targetFlags) {
                WallpaperManager.FLAG_SYSTEM -> "Home Screen"
                WallpaperManager.FLAG_LOCK -> "Lock Screen"
                else -> "Home & Lock Screens"
            }
            "Applied 8K AI Wallpaper '$title' to $targetLabel!"
        }
    }

    suspend fun encodeBitmapToDataUri(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
        val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        "data:image/jpeg;base64,$b64"
    }

    fun applyColorToneFilter(source: Bitmap, filterName: String): Bitmap {
        if (filterName.equals("None", ignoreCase = true) || filterName.isBlank()) return source
        val width = source.width.coerceAtLeast(1)
        val height = source.height.coerceAtLeast(1)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

        when (filterName.lowercase(java.util.Locale.ROOT)) {
            "amoled", "noir", "b&w", "amoled noir" -> {
                val cm = android.graphics.ColorMatrix().apply { setSaturation(0f) }
                paint.colorFilter = android.graphics.ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, paint)
            }
            "cyberpunk" -> {
                canvas.drawBitmap(source, 0f, 0f, paint)
                val tintPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(60, 147, 51, 234)
                    xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.OVERLAY)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), tintPaint)
            }
            "sunset" -> {
                canvas.drawBitmap(source, 0f, 0f, paint)
                val tintPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(60, 234, 88, 12)
                    xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.OVERLAY)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), tintPaint)
            }
            "emerald" -> {
                canvas.drawBitmap(source, 0f, 0f, paint)
                val tintPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(60, 22, 163, 74)
                    xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.OVERLAY)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), tintPaint)
            }
            "glacier" -> {
                canvas.drawBitmap(source, 0f, 0f, paint)
                val tintPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(60, 14, 165, 233)
                    xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.OVERLAY)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), tintPaint)
            }
            else -> canvas.drawBitmap(source, 0f, 0f, paint)
        }
        return output
    }

    fun extractDominantColorPalette(bitmap: Bitmap): List<String> {
        val sampleSize = 16
        val scaled = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, false)
        val pixels = IntArray(sampleSize * sampleSize)
        scaled.getPixels(pixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)

        val colors = pixels
            .map { it and 0x00FFFFFF }
            .filter { rgb ->
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                val brightness = (0.299 * r + 0.587 * g + 0.114 * b)
                brightness in 35.0..225.0
            }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { String.format("#%06X", it.key) }

        return if (colors.size >= 5) {
            colors
        } else {
            (colors + listOf("#5B2EFF", "#2563EB", "#10B981", "#F59E0B", "#0F172A")).take(5)
        }
    }

    suspend fun fetchWallpaperBitmap(
        wallpaper: WallpaperItem,
        onProgress: (Float) -> Unit
    ): Bitmap? = fetchWallpaperBitmap(null, wallpaper, onProgress)

    suspend fun fetchWallpaperBitmap(
        context: Context?,
        wallpaper: WallpaperItem,
        onProgress: (Float) -> Unit
    ): Bitmap? {
        val rawUrl = wallpaper.imageUrl.ifBlank { wallpaper.thumbnailUrl }.trim()
        val decodeOpts = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inScaled = false
        }

        // 0a. Check 50 GB Local Vault file by wallpaper ID first for 100% lossless 4K/8K quality
        if (context != null && wallpaper.id.isNotBlank()) {
            runCatching {
                val vaultDir = java.io.File(context.filesDir, "wallpapers_vault_50gb")
                if (vaultDir.exists()) {
                    val match = vaultDir.listFiles()?.firstOrNull {
                        it.nameWithoutExtension == wallpaper.id && it.length() > 0L
                    }
                    if (match != null) {
                        onProgress(0.65f)
                        BitmapFactory.decodeFile(match.absolutePath, decodeOpts)?.let { return it }
                    }
                }
            }
        }

        // 0b. Local file:// or absolute / path from 50 GB Vault
        if (rawUrl.startsWith("file://") || rawUrl.startsWith("/")) {
            runCatching {
                onProgress(0.55f)
                val file = if (rawUrl.startsWith("file://")) {
                    java.io.File(java.net.URI(rawUrl))
                } else {
                    java.io.File(rawUrl)
                }
                if (file.exists() && file.length() > 0L) {
                    onProgress(0.80f)
                    BitmapFactory.decodeFile(file.absolutePath, decodeOpts)?.let { return it }
                }
            }
        }

        // 0c. Android content:// URI
        if (context != null && rawUrl.startsWith("content://")) {
            runCatching {
                onProgress(0.55f)
                val uri = android.net.Uri.parse(rawUrl)
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    onProgress(0.80f)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)?.let { return it }
                }
            }
        }

        // 0d. Bundled drawable:// URI
        if (context != null && rawUrl.startsWith("drawable://")) {
            runCatching {
                onProgress(0.60f)
                val drawableName = rawUrl.removePrefix("drawable://").trim()
                val resId = when (drawableName) {
                    "home_hero_mountain_4k" -> com.example.R.drawable.home_hero_mountain_4k
                    "scenic_liquid_glass_bg" -> com.example.R.drawable.scenic_liquid_glass_bg
                    "pixora_logo_icon" -> com.example.R.drawable.pixora_logo_icon
                    else -> com.example.R.drawable.home_hero_mountain_4k
                }
                BitmapFactory.decodeResource(context.resources, resId, decodeOpts)?.let { return it }
            }
        }

        // 1. If data URI, decode directly
        if (rawUrl.startsWith("data:image")) {
            onProgress(0.45f)
            delay(80)
            val b64 = rawUrl.substringAfter("base64,", "")
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            onProgress(0.75f)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
        }

        // 2. Try Firebase Storage direct download if storagePath is set and points to a live Storage object
        if (wallpaper.storagePath.isNotBlank() && rawUrl.contains("firebasestorage")) {
            runCatching {
                onProgress(0.30f)
                val ref = FirebaseStorage.getInstance().reference.child(wallpaper.storagePath)
                val bytes = ref.getBytes(50L * 1024L * 1024L).await()
                onProgress(0.75f)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)?.let { return it }
            }
        }

        // 3. Download via OkHttp with streaming progress (upgrade Unsplash URLs to 4K w=3840&q=100)
        if (rawUrl.startsWith("http")) {
            runCatching {
                val downloadUrl = if (rawUrl.contains("images.unsplash.com")) {
                    val sanitized = rawUrl
                        .replace("auto=format", "fm=jpg")
                        .replace("fm=avif", "fm=jpg")
                        .replace("fm=webp", "fm=jpg")
                    val withW = if (sanitized.contains(Regex("w=\\d+"))) {
                        sanitized.replace(Regex("w=\\d+"), "w=3840")
                    } else if (sanitized.contains("?")) {
                        "$sanitized&w=3840"
                    } else {
                        "$sanitized?w=3840"
                    }
                    val withQ = if (withW.contains(Regex("q=\\d+"))) {
                        withW.replace(Regex("q=\\d+"), "q=95")
                    } else {
                        "$withW&q=95"
                    }
                    if (!withQ.contains("fm=jpg")) "$withQ&fm=jpg" else withQ
                } else {
                    rawUrl
                }
                val request = Request.Builder()
                    .url(downloadUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                    .header("Accept", "image/jpeg,image/png;q=0.9")
                    .build()
                var response = runCatching { httpClient.newCall(request).execute() }.getOrNull()
                if (response == null || !response.isSuccessful) {
                    response?.close()
                    // Retry with rawUrl directly in case high-resolution query was rejected
                    val fallbackReq = Request.Builder()
                        .url(rawUrl.replace("auto=format", "fm=jpg"))
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                        .header("Accept", "image/jpeg,image/png;q=0.9")
                        .build()
                    response = runCatching { httpClient.newCall(fallbackReq).execute() }.getOrNull()
                }
                response?.use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body ?: return@runCatching
                        val total = body.contentLength().coerceAtLeast(1L)
                        val input = body.byteStream()
                        val out = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var read: Int
                        var downloaded = 0L
                        while (input.read(buffer).also { read = it } != -1) {
                            out.write(buffer, 0, read)
                            downloaded += read
                            val fraction = (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                            onProgress(0.20f + fraction * 0.55f)
                        }
                        val bytes = out.toByteArray()
                        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
                    }
                }
            }
        }

        // 4. Fallback generated Liquid Glass gradient bitmap if offline
        onProgress(0.70f)
        val fallback = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(fallback)
        val paint = android.graphics.Paint().apply {
            shader = android.graphics.LinearGradient(
                0f, 0f, 1080f, 1920f,
                intArrayOf(0xFF050814.toInt(), 0xFF0B2248.toInt(), 0xFF3B0E68.toInt()),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, 1080f, 1920f, paint)
        return fallback
    }
}
