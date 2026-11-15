package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class LiquidGlassOptics(
    val blurRadiusDp: Float = 2f,               // Default 2px blur in UI
    val dockBlurDp: Float = 60f,                // 60 blur in dock
    val upperBlurStrength: Float = 48f,         // High-to-low upper blur behind header & status bar
    val enable120HzSmoothness: Boolean = true,  // 120Hz ProMotion high-frame-rate smoothness
    val refractionStrength: Float = 145f,       // Adjustable Dock Refraction Strength (0..200)
    val refractionDepth: Float = 72f,           // Adjustable Dock Refraction Depth (0..100)
    val chromaticSplit: Float = 58f,            // Adjustable Dock Chromatic Split (0..100)
    val glassOpacity: Float = 0.24f,            // Pure Transparent Liquid Glass Opacity (0.10..0.90)
    val glowIntensity: Float = 1.50f,
    val specularShine: Float = 0.98f,
    val liquidStretch: Float = 1.35f,
    val tintHex: String = "#00D9FF",
    val ambientOrbsMotion: Float = 1.0f
)

data class LiquidGlassPreset(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val optics: LiquidGlassOptics
)

object LiquidGlassPresets {
    val hyperLiquidGlass = LiquidGlassPreset(
        id = "hyper_liquid",
        name = "Pure Liquid Glass (Default)",
        description = "Apple iOS-inspired pure liquid glass with high pure optical blur, refraction, and ambient light blending.",
        iconEmoji = "🌊",
        optics = LiquidGlassOptics(
            blurRadiusDp = 2f,
            dockBlurDp = 60f,
            upperBlurStrength = 48f,
            refractionStrength = 145f,
            refractionDepth = 72f,
            chromaticSplit = 58f,
            glassOpacity = 0.24f,
            glowIntensity = 1.65f,
            specularShine = 1.0f,
            liquidStretch = 1.35f,
            tintHex = "#00D9FF",
            ambientOrbsMotion = 1.2f
        )
    )

    val iPhone17Ultra = LiquidGlassPreset(
        id = "iphone17",
        name = "Apple iOS Crystal Clear",
        description = "Ultra-transparent crystal glass with vivid chromatic cyan, royal blue, and violet edge reflections.",
        iconEmoji = "💎",
        optics = LiquidGlassOptics(
            blurRadiusDp = 20f,
            dockBlurDp = 56f,
            upperBlurStrength = 44f,
            refractionStrength = 165f,
            refractionDepth = 82f,
            chromaticSplit = 68f,
            glassOpacity = 0.20f,
            glowIntensity = 1.75f,
            specularShine = 1.0f,
            liquidStretch = 1.40f,
            tintHex = "#087BFF",
            ambientOrbsMotion = 1.3f
        )
    )

    val obsidianAmoled = LiquidGlassPreset(
        id = "obsidian",
        name = "Deep Navy Optical Glass",
        description = "Deep navy cosmic background with intense luminous cyan and electric blue refraction.",
        iconEmoji = "🌌",
        optics = LiquidGlassOptics(
            blurRadiusDp = 16f,
            dockBlurDp = 60f,
            upperBlurStrength = 46f,
            refractionStrength = 150f,
            refractionDepth = 75f,
            chromaticSplit = 50f,
            glassOpacity = 0.34f,
            glowIntensity = 1.80f,
            specularShine = 0.95f,
            liquidStretch = 1.25f,
            tintHex = "#00D9FF",
            ambientOrbsMotion = 1.0f
        )
    )

    val cyberpunkNeon = LiquidGlassPreset(
        id = "cyberpunk",
        name = "Prismatic Chromatic Split",
        description = "Futuristic liquid glass with intense RGB chromatic split and radiant violet-cyan caustics.",
        iconEmoji = "⚡",
        optics = LiquidGlassOptics(
            blurRadiusDp = 18f,
            dockBlurDp = 58f,
            upperBlurStrength = 46f,
            refractionStrength = 185f,
            refractionDepth = 92f,
            chromaticSplit = 92f,
            glassOpacity = 0.25f,
            glowIntensity = 1.95f,
            specularShine = 1.0f,
            liquidStretch = 1.45f,
            tintHex = "#762BFF",
            ambientOrbsMotion = 1.5f
        )
    )

    val crystallinePrism = LiquidGlassPreset(
        id = "crystalline",
        name = "Alabaster Lens Glass",
        description = "Crisp translucent water-lens optics over light backgrounds with glowing white rims.",
        iconEmoji = "✨",
        optics = LiquidGlassOptics(
            blurRadiusDp = 20f,
            dockBlurDp = 54f,
            upperBlurStrength = 42f,
            refractionStrength = 130f,
            refractionDepth = 64f,
            chromaticSplit = 42f,
            glassOpacity = 0.24f,
            glowIntensity = 1.40f,
            specularShine = 1.0f,
            liquidStretch = 1.20f,
            tintHex = "#00D9FF",
            ambientOrbsMotion = 1.0f
        )
    )

    val frostedVelvet = LiquidGlassPreset(
        id = "frosted",
        name = "Deep Frosted Blur Glass",
        description = "Ultra-high pure optical dock blur (64dp) with luminous emerald and cyan aurora highlights.",
        iconEmoji = "🌿",
        optics = LiquidGlassOptics(
            blurRadiusDp = 38f,
            dockBlurDp = 64f,
            upperBlurStrength = 52f,
            refractionStrength = 145f,
            refractionDepth = 72f,
            chromaticSplit = 48f,
            glassOpacity = 0.28f,
            glowIntensity = 1.50f,
            specularShine = 0.96f,
            liquidStretch = 1.15f,
            tintHex = "#10B981",
            ambientOrbsMotion = 0.8f
        )
    )

    val allPresets = listOf(
        hyperLiquidGlass,
        iPhone17Ultra,
        obsidianAmoled,
        cyberpunkNeon,
        crystallinePrism,
        frostedVelvet
    )
}

fun parseHexColor(hex: String, fallback: Color = ElectricCyan): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or colorInt)
        } else if (clean.length == 8) {
            Color(colorInt)
        } else {
            fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

data class LiquidGlassColors(
    val isDark: Boolean,
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val glassFill: Color,
    val glassFillElevated: Color,
    val glassBorderBright: Color,
    val glassBorderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val glowCyan: Color,
    val glowViolet: Color,
    val glowBlue: Color,
    val specularHighlight: Color
)

val LocalLiquidGlassColors = staticCompositionLocalOf {
    LiquidGlassColors(
        isDark = true,
        backgroundPrimary = GlassDarkBgPrimary,
        backgroundSecondary = GlassDarkBgSecondary,
        glassFill = Color(0xFF162038),
        glassFillElevated = Color(0xFF1E2B4A),
        glassBorderBright = Color(0x9980F8FF),
        glassBorderSubtle = Color(0x38FFFFFF),
        textPrimary = Color(0xFFF8FBFF),
        textSecondary = Color(0xFFB4C6E7),
        glowCyan = ElectricCyan,
        glowViolet = NeonViolet,
        glowBlue = CyberBlue,
        specularHighlight = SpecularWhite
    )
}

data class AppDesignStyle(
    val id: String,
    val name: String,
    val badge: String,
    val description: String,
    val gridColumns: Int,
    val cardCornerDp: Int,
    val cardHeightDp: Int,
    val layoutMode: String, // "editorial", "bento", "cinema", "pro_3col", "monochrome", "spatial"
    val recommendedTheme: String,
    val recommendedOptics: LiquidGlassOptics,
    val isNewPremiumLayout: Boolean = false,
    val heroStyle: String = "peek_carousel", // "peek_carousel", "compact_banner", "full_bleed", "spotlight", "minimal_header"
    val cardOverlayStyle: String = "clean_badge", // "clean_badge", "glass_pill", "floating_caption", "luxury_rim", "zero_clutter"
    val headerStyle: String = "classic" // "classic", "glass_pill", "centered_editorial"
)

object AppDesignCatalog {
    // -------------------------------------------------------------------------
    // 10 NEW MODERN PREMIUM USER LAYOUTS (Admin -> User Layout Studio)
    // -------------------------------------------------------------------------

    val premiumLiquidShowcase = AppDesignStyle(
        id = "premium_liquid_showcase",
        name = "Liquid Glass Showcase",
        badge = "New #1 • Glass",
        description = "2-column tall portrait showcase with peek hero carousel & frosted glass title pills.",
        gridColumns = 2,
        cardCornerDp = 28,
        cardHeightDp = 268,
        layoutMode = "editorial",
        recommendedTheme = "light",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 26f,
            dockBlurDp = 60f,
            upperBlurStrength = 22f,
            refractionStrength = 155f,
            refractionDepth = 76f,
            chromaticSplit = 62f,
            glassOpacity = 0.25f,
            tintHex = "#38BDF8"
        ),
        isNewPremiumLayout = true,
        heroStyle = "peek_carousel",
        cardOverlayStyle = "glass_pill",
        headerStyle = "glass_pill"
    )

    val premiumBentoMagazine = AppDesignStyle(
        id = "premium_bento_magazine",
        name = "Bento Magazine Pro",
        badge = "New #2 • Bento",
        description = "Asymmetric Bento masonry cards, compact editorial banner & bold magazine captions.",
        gridColumns = 2,
        cardCornerDp = 26,
        cardHeightDp = 264,
        layoutMode = "bento",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 22f,
            dockBlurDp = 60f,
            upperBlurStrength = 18f,
            refractionStrength = 150f,
            refractionDepth = 72f,
            chromaticSplit = 60f,
            glassOpacity = 0.28f,
            tintHex = "#6366F1"
        ),
        isNewPremiumLayout = true,
        heroStyle = "compact_banner",
        cardOverlayStyle = "floating_caption",
        headerStyle = "classic"
    )

    val premiumCinemaImax = AppDesignStyle(
        id = "premium_cinema_imax",
        name = "Cinema IMAX 8K",
        badge = "New #3 • Cinema",
        description = "Full-width 1-column 8K showcase with full-bleed spotlight hero & floating captions.",
        gridColumns = 1,
        cardCornerDp = 30,
        cardHeightDp = 330,
        layoutMode = "cinema",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 24f,
            dockBlurDp = 60f,
            upperBlurStrength = 20f,
            refractionStrength = 165f,
            refractionDepth = 80f,
            chromaticSplit = 68f,
            glassOpacity = 0.30f,
            tintHex = "#0EA5E9"
        ),
        isNewPremiumLayout = true,
        heroStyle = "full_bleed",
        cardOverlayStyle = "floating_caption",
        headerStyle = "centered_editorial"
    )

    val premiumFrostedDeck = AppDesignStyle(
        id = "premium_frosted_deck",
        name = "Frosted Capsule Deck",
        badge = "New #4 • Capsule",
        description = "Ultra-rounded 34dp squircle deck with floating glass header capsule & tag pills.",
        gridColumns = 2,
        cardCornerDp = 34,
        cardHeightDp = 250,
        layoutMode = "spatial",
        recommendedTheme = "light",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 28f,
            dockBlurDp = 60f,
            upperBlurStrength = 24f,
            refractionStrength = 160f,
            refractionDepth = 74f,
            chromaticSplit = 56f,
            glassOpacity = 0.24f,
            tintHex = "#06B6D4"
        ),
        isNewPremiumLayout = true,
        heroStyle = "compact_banner",
        cardOverlayStyle = "glass_pill",
        headerStyle = "glass_pill"
    )

    val premiumSwissGrid = AppDesignStyle(
        id = "premium_swiss_grid",
        name = "Minimalist Swiss Grid",
        badge = "New #5 • Swiss",
        description = "Clean distraction-free 3-column architectural grid with crisp corners & zero overlay clutter.",
        gridColumns = 3,
        cardCornerDp = 12,
        cardHeightDp = 196,
        layoutMode = "pro_3col",
        recommendedTheme = "light",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 18f,
            dockBlurDp = 60f,
            upperBlurStrength = 14f,
            refractionStrength = 135f,
            refractionDepth = 60f,
            chromaticSplit = 46f,
            glassOpacity = 0.22f,
            tintHex = "#334155"
        ),
        isNewPremiumLayout = true,
        heroStyle = "minimal_header",
        cardOverlayStyle = "zero_clutter",
        headerStyle = "classic"
    )

    val premiumObsidianGold = AppDesignStyle(
        id = "premium_obsidian_gold",
        name = "Obsidian Luxury Gold",
        badge = "New #6 • Luxury",
        description = "Deep OLED obsidian canvas with 24K amber-gold card rim borders & centered editorial top.",
        gridColumns = 2,
        cardCornerDp = 24,
        cardHeightDp = 262,
        layoutMode = "monochrome",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 22f,
            dockBlurDp = 60f,
            upperBlurStrength = 20f,
            refractionStrength = 158f,
            refractionDepth = 76f,
            chromaticSplit = 64f,
            glassOpacity = 0.30f,
            tintHex = "#F59E0B"
        ),
        isNewPremiumLayout = true,
        heroStyle = "spotlight",
        cardOverlayStyle = "luxury_rim",
        headerStyle = "centered_editorial"
    )

    val premiumNordicPastel = AppDesignStyle(
        id = "premium_nordic_pastel",
        name = "Nordic Pastel Air",
        badge = "New #7 • Air",
        description = "Airy Scandinavian spacing, soft emerald accents, compact hero & frosted glass pills.",
        gridColumns = 2,
        cardCornerDp = 28,
        cardHeightDp = 246,
        layoutMode = "editorial",
        recommendedTheme = "light",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 24f,
            dockBlurDp = 60f,
            upperBlurStrength = 20f,
            refractionStrength = 146f,
            refractionDepth = 68f,
            chromaticSplit = 52f,
            glassOpacity = 0.23f,
            tintHex = "#10B981"
        ),
        isNewPremiumLayout = true,
        heroStyle = "compact_banner",
        cardOverlayStyle = "glass_pill",
        headerStyle = "glass_pill"
    )

    val premiumCyberMatrix = AppDesignStyle(
        id = "premium_cyber_matrix",
        name = "Cyber Grid Matrix",
        badge = "New #8 • Matrix",
        description = "Rapid-scan 3-column 4K matrix with luminous neon magenta borders & compact header.",
        gridColumns = 3,
        cardCornerDp = 14,
        cardHeightDp = 188,
        layoutMode = "pro_3col",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 20f,
            dockBlurDp = 60f,
            upperBlurStrength = 18f,
            refractionStrength = 172f,
            refractionDepth = 84f,
            chromaticSplit = 78f,
            glassOpacity = 0.29f,
            tintHex = "#EC4899"
        ),
        isNewPremiumLayout = true,
        heroStyle = "minimal_header",
        cardOverlayStyle = "luxury_rim",
        headerStyle = "classic"
    )

    val premiumEditorialSpotlight = AppDesignStyle(
        id = "premium_editorial_spotlight",
        name = "Editorial Spotlight",
        badge = "New #9 • Editorial",
        description = "Centered gallery masthead, featured spotlight hero & curated 2-col tall artwork frame.",
        gridColumns = 2,
        cardCornerDp = 22,
        cardHeightDp = 274,
        layoutMode = "editorial",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 24f,
            dockBlurDp = 60f,
            upperBlurStrength = 20f,
            refractionStrength = 156f,
            refractionDepth = 74f,
            chromaticSplit = 66f,
            glassOpacity = 0.27f,
            tintHex = "#8B5CF6"
        ),
        isNewPremiumLayout = true,
        heroStyle = "spotlight",
        cardOverlayStyle = "floating_caption",
        headerStyle = "centered_editorial"
    )

    val premiumSpatialVision = AppDesignStyle(
        id = "premium_spatial_vision",
        name = "Spatial Vision Pro",
        badge = "New #10 • Spatial",
        description = "Depth-layered 32dp floating glass panels with 60dp dock blur & luminous aqua rim.",
        gridColumns = 2,
        cardCornerDp = 32,
        cardHeightDp = 258,
        layoutMode = "spatial",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 30f,
            dockBlurDp = 60f,
            upperBlurStrength = 24f,
            refractionStrength = 170f,
            refractionDepth = 82f,
            chromaticSplit = 74f,
            glassOpacity = 0.28f,
            tintHex = "#22D3EE"
        ),
        isNewPremiumLayout = true,
        heroStyle = "peek_carousel",
        cardOverlayStyle = "luxury_rim",
        headerStyle = "glass_pill"
    )

    val premiumUserLayouts: List<AppDesignStyle> = listOf(
        premiumLiquidShowcase,
        premiumBentoMagazine,
        premiumCinemaImax,
        premiumFrostedDeck,
        premiumSwissGrid,
        premiumObsidianGold,
        premiumNordicPastel,
        premiumCyberMatrix,
        premiumEditorialSpotlight,
        premiumSpatialVision
    )

    val ios27Crystal = AppDesignStyle(
        id = "ios27_crystal",
        name = "Pure Liquid Glass • iOS 27",
        badge = "Liquid Glass • Default",
        description = "Pure 3-column optical liquid glass with adjustable dock blur, refraction depth & chromatic split.",
        gridColumns = 3,
        cardCornerDp = 18,
        cardHeightDp = 204,
        layoutMode = "editorial",
        recommendedTheme = "light",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 28f,
            dockBlurDp = 60f,
            upperBlurStrength = 48f,
            refractionStrength = 145f,
            refractionDepth = 72f,
            chromaticSplit = 58f,
            glassOpacity = 0.24f,
            tintHex = "#00D9FF"
        )
    )

    val minimalBento = AppDesignStyle(
        id = "bento_minimal",
        name = "Liquid Glass • Bento Grid",
        badge = "Design 2 • Bento",
        description = "Asymmetric 2-column Bento masonry cards with crystal-clear liquid glass.",
        gridColumns = 2,
        cardCornerDp = 28,
        cardHeightDp = 256,
        layoutMode = "bento",
        recommendedTheme = "light",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 16f,
            dockBlurDp = 56f,
            upperBlurStrength = 44f,
            refractionStrength = 140f,
            refractionDepth = 65f,
            chromaticSplit = 50f,
            glassOpacity = 0.26f,
            tintHex = "#38BDF8"
        )
    )

    val cinemaImmersive = AppDesignStyle(
        id = "cinema_immersive",
        name = "Liquid Glass • Cinema 8K",
        badge = "Design 3 • Cinema",
        description = "Full-width tall 8K showcase cards with deep optical refraction.",
        gridColumns = 1,
        cardCornerDp = 30,
        cardHeightDp = 350,
        layoutMode = "cinema",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 20f,
            dockBlurDp = 60f,
            upperBlurStrength = 46f,
            refractionStrength = 155f,
            refractionDepth = 75f,
            chromaticSplit = 65f,
            glassOpacity = 0.30f,
            tintHex = "#087BFF"
        )
    )

    val proStudio3Col = AppDesignStyle(
        id = "pro_studio_3col",
        name = "Liquid Glass • 3-Col Matrix",
        badge = "Design 4 • Matrix",
        description = "High-density 3-column wallpaper matrix for rapid 4K & 8K scanning.",
        gridColumns = 3,
        cardCornerDp = 18,
        cardHeightDp = 178,
        layoutMode = "pro_3col",
        recommendedTheme = "light",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 16f,
            dockBlurDp = 56f,
            upperBlurStrength = 44f,
            refractionStrength = 130f,
            refractionDepth = 60f,
            chromaticSplit = 48f,
            glassOpacity = 0.28f,
            tintHex = "#00D9FF"
        )
    )

    val oledMonochrome = AppDesignStyle(
        id = "oled_monochrome",
        name = "Liquid Glass • OLED Dark",
        badge = "Design 5 • OLED",
        description = "Pitch-dark contrast with crisp silver-cyan chromatic liquid borders.",
        gridColumns = 2,
        cardCornerDp = 22,
        cardHeightDp = 236,
        layoutMode = "monochrome",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 18f,
            dockBlurDp = 60f,
            upperBlurStrength = 46f,
            refractionStrength = 145f,
            refractionDepth = 70f,
            chromaticSplit = 60f,
            glassOpacity = 0.32f,
            tintHex = "#E0F7FA"
        )
    )

    val spatialDeck = AppDesignStyle(
        id = "spatial_deck",
        name = "Liquid Glass • Spatial Squircle",
        badge = "Design 6 • Squircle",
        description = "Ultra-rounded 34dp spatial glass cards with prismatic depth.",
        gridColumns = 2,
        cardCornerDp = 34,
        cardHeightDp = 248,
        layoutMode = "spatial",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 20f,
            dockBlurDp = 60f,
            upperBlurStrength = 18f,
            refractionStrength = 165f,
            refractionDepth = 80f,
            chromaticSplit = 72f,
            glassOpacity = 0.26f,
            tintHex = "#762BFF"
        )
    )

    val cyberpunkNeon = AppDesignStyle(
        id = "cyberpunk_neon",
        name = "Cyberpunk • Neon Horizon",
        badge = "Design 7 • Cyberpunk",
        description = "High-voltage electric magenta & cyan laser borders on dark carbon OLED.",
        gridColumns = 2,
        cardCornerDp = 16,
        cardHeightDp = 240,
        layoutMode = "bento",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 22f,
            dockBlurDp = 60f,
            upperBlurStrength = 20f,
            refractionStrength = 175f,
            refractionDepth = 85f,
            chromaticSplit = 80f,
            glassOpacity = 0.28f,
            tintHex = "#FF007F"
        )
    )

    val emeraldAurora = AppDesignStyle(
        id = "emerald_aurora",
        name = "Nordic Aurora • Emerald Flow",
        badge = "Design 8 • Aurora",
        description = "Luminous northern lights emerald & teal caustics on frosted velvet glass.",
        gridColumns = 2,
        cardCornerDp = 26,
        cardHeightDp = 236,
        layoutMode = "editorial",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 24f,
            dockBlurDp = 60f,
            upperBlurStrength = 22f,
            refractionStrength = 150f,
            refractionDepth = 70f,
            chromaticSplit = 55f,
            glassOpacity = 0.25f,
            tintHex = "#10B981"
        )
    )

    val solarSunset = AppDesignStyle(
        id = "solar_sunset",
        name = "Solar Sunset • Golden Quartz",
        badge = "Design 9 • Sunset",
        description = "Warm amber gold and radiant crimson dusk gradients with specular glow.",
        gridColumns = 2,
        cardCornerDp = 24,
        cardHeightDp = 244,
        layoutMode = "editorial",
        recommendedTheme = "light",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 20f,
            dockBlurDp = 60f,
            upperBlurStrength = 18f,
            refractionStrength = 140f,
            refractionDepth = 65f,
            chromaticSplit = 50f,
            glassOpacity = 0.24f,
            tintHex = "#F59E0B"
        )
    )

    val tokyoViolet = AppDesignStyle(
        id = "tokyo_violet",
        name = "Tokyo Midnight • Cyber Violet",
        badge = "Design 10 • Tokyo",
        description = "Futuristic Tokyo neon lavender & grape ultraviolet lighting with 60px dock blur.",
        gridColumns = 2,
        cardCornerDp = 30,
        cardHeightDp = 250,
        layoutMode = "spatial",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 22f,
            dockBlurDp = 60f,
            upperBlurStrength = 20f,
            refractionStrength = 160f,
            refractionDepth = 78f,
            chromaticSplit = 70f,
            glassOpacity = 0.27f,
            tintHex = "#8B5CF6"
        )
    )

    val editorialSplit = AppDesignStyle(
        id = "editorial_split",
        name = "Editorial Split • 2-Col Gallery",
        badge = "Design 11 • Gallery",
        description = "Curated museum-grade 2-column tall gallery with crisp sapphire highlights.",
        gridColumns = 2,
        cardCornerDp = 20,
        cardHeightDp = 268,
        layoutMode = "editorial",
        recommendedTheme = "light",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 26f,
            dockBlurDp = 60f,
            upperBlurStrength = 22f,
            refractionStrength = 152f,
            refractionDepth = 74f,
            chromaticSplit = 62f,
            glassOpacity = 0.25f,
            tintHex = "#3B82F6"
        )
    )

    val bentoCascade = AppDesignStyle(
        id = "bento_cascade",
        name = "Bento Cascade • Dynamic Pro",
        badge = "Design 12 • Cascade",
        description = "Rhythmic asymmetric cascade layout with ultra-frosted 60dp floating dock.",
        gridColumns = 2,
        cardCornerDp = 32,
        cardHeightDp = 276,
        layoutMode = "bento",
        recommendedTheme = "dark",
        recommendedOptics = LiquidGlassOptics(
            blurRadiusDp = 28f,
            dockBlurDp = 60f,
            upperBlurStrength = 24f,
            refractionStrength = 168f,
            refractionDepth = 82f,
            chromaticSplit = 74f,
            glassOpacity = 0.29f,
            tintHex = "#14B8A6"
        )
    )

    val classicDesigns: List<AppDesignStyle> = listOf(
        ios27Crystal,
        minimalBento,
        cinemaImmersive,
        proStudio3Col,
        oledMonochrome,
        spatialDeck,
        cyberpunkNeon,
        emeraldAurora,
        solarSunset,
        tokyoViolet,
        editorialSplit,
        bentoCascade
    )

    val allDesigns: List<AppDesignStyle> = premiumUserLayouts + classicDesigns

    fun byId(id: String): AppDesignStyle =
        allDesigns.firstOrNull { it.id == id } ?: ios27Crystal
}

val LocalLiquidGlassOptics = staticCompositionLocalOf {
    LiquidGlassOptics()
}

val LocalAppDesignStyle = staticCompositionLocalOf {
    AppDesignCatalog.ios27Crystal
}

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF02131A),
    secondary = NeonViolet,
    onSecondary = Color.White,
    tertiary = CyberBlue,
    background = GlassDarkBgPrimary,
    onBackground = Color(0xFFF8FBFF),
    surface = Color(0xFF0B1228),
    onSurface = Color(0xFFF8FBFF),
    surfaceVariant = Color(0xFF152042),
    onSurfaceVariant = Color(0xFFB4C6E7),
    error = RoseGlow
)

private val LightColorScheme = lightColorScheme(
    primary = GalleryBlueAccent,
    onPrimary = Color.White,
    secondary = Color(0xFF6366F1),
    onSecondary = Color.White,
    tertiary = CyberBlue,
    background = GlassLightBgPrimary,
    onBackground = GalleryInkPrimary,
    surface = Color(0xFFFFFFFF),
    onSurface = GalleryInkPrimary,
    surfaceVariant = Color(0xFFE5EAF6),
    onSurfaceVariant = GalleryInkSecondary,
    error = RoseGlow
)

@Composable
fun WallxTheme(
    themeMode: String = "light", // "light", "dark", "system"
    optics: LiquidGlassOptics = LiquidGlassOptics(),
    appDesignStyle: AppDesignStyle = AppDesignCatalog.ios27Crystal,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        "dark" -> true
        "system" -> systemDark
        else -> false
    }

    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme
    val activeTint = parseHexColor(optics.tintHex, if (useDark) ElectricCyan else GalleryBlueAccent)

    val glassColors = if (useDark) {
        val isMono = appDesignStyle.id == "oled_monochrome"
        LiquidGlassColors(
            isDark = true,
            backgroundPrimary = if (isMono) Color(0xFF000000) else AppleDeepNavy,
            backgroundSecondary = if (isMono) Color(0xFF080B12) else GlassDarkBgSecondary,
            glassFill = if (isMono) Color(0x38161B26) else LiquidGlassTranslucentFillDark,
            glassFillElevated = if (isMono) Color(0x4D222B3D) else GlassDarkSurfaceElevated,
            glassBorderBright = Color(0xF2FFFFFF),
            glassBorderSubtle = Color(0x6600D9FF),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFFB4C6E7),
            glowCyan = if (isMono) Color.White else AppleCyan,
            glowViolet = if (isMono) Color(0xFFCBD5E1) else AppleViolet,
            glowBlue = if (isMono) Color.White else AppleElectricBlue,
            specularHighlight = Color(0xF5FFFFFF)
        )
    } else {
        LiquidGlassColors(
            isDark = false,
            backgroundPrimary = GlassLightBgPrimary,
            backgroundSecondary = GlassLightBgSecondary,
            glassFill = Color(0x99FFFFFF),
            glassFillElevated = Color(0xCCFFFFFF),
            glassBorderBright = Color(0xF8FFFFFF),
            glassBorderSubtle = Color(0x99BAE6FD),
            textPrimary = Color(0xFF0A0F1D),
            textSecondary = Color(0xFF4A5568),
            glowCyan = AppleCyan,
            glowViolet = AppleViolet,
            glowBlue = AppleElectricBlue,
            specularHighlight = Color(0xF8FFFFFF)
        )
    }

    CompositionLocalProvider(
        LocalLiquidGlassColors provides glassColors,
        LocalLiquidGlassOptics provides optics,
        LocalAppDesignStyle provides appDesignStyle
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
