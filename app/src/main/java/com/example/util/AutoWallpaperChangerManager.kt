package com.example.util

import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.data.model.WallpaperItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class AutoWallpaperConfig(
    val enabled: Boolean = false,
    val intervalHours: Int = 3,
    val targetScreen: String = "BOTH", // "BOTH", "HOME", "LOCK"
    val sourcePool: String = "ALL", // "ALL", "FAVORITES", "NATURE", "ANIME", "CARS", "MINIMAL", "AESTHETIC"
    val lastChangedTime: Long = 0L,
    val lastWallpaperTitle: String = ""
)

object AutoWallpaperChangerManager {

    private const val PREFS_NAME = "pixora_auto_wallpaper_prefs"
    private const val KEY_ENABLED = "auto_enabled"
    private const val KEY_INTERVAL = "auto_interval"
    private const val KEY_SCREEN = "auto_screen"
    private const val KEY_POOL = "auto_pool"
    private const val KEY_LAST_TIME = "auto_last_time"
    private const val KEY_LAST_TITLE = "auto_last_title"

    private val _config = MutableStateFlow(AutoWallpaperConfig())
    val config: StateFlow<AutoWallpaperConfig> = _config.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp
        _config.value = AutoWallpaperConfig(
            enabled = sp.getBoolean(KEY_ENABLED, false),
            intervalHours = sp.getInt(KEY_INTERVAL, 3),
            targetScreen = sp.getString(KEY_SCREEN, "BOTH") ?: "BOTH",
            sourcePool = sp.getString(KEY_POOL, "ALL") ?: "ALL",
            lastChangedTime = sp.getLong(KEY_LAST_TIME, 0L),
            lastWallpaperTitle = sp.getString(KEY_LAST_TITLE, "") ?: ""
        )
    }

    fun updateConfig(
        enabled: Boolean? = null,
        intervalHours: Int? = null,
        targetScreen: String? = null,
        sourcePool: String? = null
    ) {
        val current = _config.value
        val updated = current.copy(
            enabled = enabled ?: current.enabled,
            intervalHours = intervalHours ?: current.intervalHours,
            targetScreen = targetScreen ?: current.targetScreen,
            sourcePool = sourcePool ?: current.sourcePool
        )
        _config.value = updated
        prefs?.edit()?.apply {
            putBoolean(KEY_ENABLED, updated.enabled)
            putInt(KEY_INTERVAL, updated.intervalHours)
            putString(KEY_SCREEN, updated.targetScreen)
            putString(KEY_POOL, updated.sourcePool)
            apply()
        }
    }

    /**
     * Instantly changes the device wallpaper to the next eligible wallpaper based on the selected pool.
     */
    suspend fun changeWallpaperNow(
        context: Context,
        wallpapers: List<WallpaperItem>,
        favoriteWallpapers: List<WallpaperItem>
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cfg = _config.value
            val poolCandidates = when (cfg.sourcePool) {
                "FAVORITES" -> {
                    if (favoriteWallpapers.isNotEmpty()) favoriteWallpapers else wallpapers
                }
                "ALL" -> wallpapers
                else -> {
                    val matching = wallpapers.filter {
                        it.category.equals(cfg.sourcePool, ignoreCase = true) ||
                            it.tags.any { tag -> tag.equals(cfg.sourcePool, ignoreCase = true) }
                    }
                    if (matching.isNotEmpty()) matching else wallpapers
                }
            }

            if (poolCandidates.isEmpty()) {
                error("No wallpapers available in pool '${cfg.sourcePool}'.")
            }

            // Pick a candidate different from the current one if possible
            val eligible = poolCandidates.filter { it.title != cfg.lastWallpaperTitle }.ifEmpty { poolCandidates }
            val selected = eligible.random()

            val bitmap = WallpaperDownloader.fetchWallpaperBitmap(selected) { }
                ?: error("Could not fetch wallpaper for auto changer.")

            val targetFlags = when (cfg.targetScreen.uppercase()) {
                "HOME" -> WallpaperManager.FLAG_SYSTEM
                "LOCK" -> WallpaperManager.FLAG_LOCK
                else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            val wm = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wm.setBitmap(bitmap, null, true, targetFlags)
            } else {
                wm.setBitmap(bitmap)
            }

            val now = System.currentTimeMillis()
            prefs?.edit()?.apply {
                putLong(KEY_LAST_TIME, now)
                putString(KEY_LAST_TITLE, selected.title)
                apply()
            }

            _config.value = _config.value.copy(
                lastChangedTime = now,
                lastWallpaperTitle = selected.title
            )

            val screenLabel = when (cfg.targetScreen.uppercase()) {
                "HOME" -> "Home Screen"
                "LOCK" -> "Lock Screen"
                else -> "Home & Lock Screens"
            }

            "Changed wallpaper to '${selected.title}' on $screenLabel"
        }
    }
}
