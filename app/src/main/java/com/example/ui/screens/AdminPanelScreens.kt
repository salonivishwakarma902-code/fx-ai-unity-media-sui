package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettingsModel
import com.example.ui.theme.AppDesignCatalog
import com.example.ui.theme.AppDesignStyle
import com.example.data.model.CategoryModel
import com.example.data.model.OFFICIAL_ADMIN_EMAIL
import com.example.data.model.PremiumPurchaseModel
import com.example.data.model.UserProfileModel
import com.example.data.model.WallpaperItem
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassChip
import com.example.ui.components.LiquidGlassCircularButton
import com.example.ui.components.LiquidGlassSlider
import com.example.ui.components.LiquidGlassSwitch
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.components.WallxAsyncImage
import com.example.ui.components.elasticLiquidGlass
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RoseGlow
import java.util.Locale
import java.util.UUID

// -----------------------------------------------------------------------------
// 1. ADMIN DASHBOARD SCREEN
// -----------------------------------------------------------------------------

@Composable
fun AdminDashboardScreen(
    users: List<UserProfileModel>,
    wallpapers: List<WallpaperItem>,
    categories: List<CategoryModel>,
    premiumPurchases: List<PremiumPurchaseModel> = emptyList(),
    selectedAppDesign: AppDesignStyle = AppDesignCatalog.ios27Crystal,
    publishedCount: Int = wallpapers.count { it.status.isBlank() || it.status.equals("published", ignoreCase = true) },
    pendingSyncCount: Int = 0,
    onSelectAppDesign: (AppDesignStyle) -> Unit = {},
    onSeedCloudCatalog: () -> Unit,
    onSwitchToUserPanel: () -> Unit,
    onSelectWallpaper: (WallpaperItem) -> Unit,
    onDeleteWallpaper: (WallpaperItem) -> Unit = {},
    onUpdatePremiumPurchaseStatus: (PremiumPurchaseModel, String) -> Unit = { _, _ -> }
) {
    val glass = LocalLiquidGlassColors.current
    val totalDownloads = remember(wallpapers) { wallpapers.sumOf { it.downloadCount } }
    val totalFavorites = remember(wallpapers) { wallpapers.sumOf { it.favoriteCount } }
    val topDownloaded = remember(wallpapers) { wallpapers.sortedByDescending { it.downloadCount }.take(5) }
    val mergedPremiumMembers = remember(premiumPurchases, users) {
        val byUserId = linkedMapOf<String, PremiumPurchaseModel>()
        premiumPurchases.forEach { p ->
            val key = p.userId.ifBlank { p.id }
            if (p.status != "revoked") {
                byUserId[key] = p
            }
        }
        users.filter { it.isPremium }.forEach { usr ->
            val key = usr.userId.ifBlank { usr.id }
            if (!byUserId.containsKey(key)) {
                byUserId[key] = PremiumPurchaseModel(
                    id = key,
                    userId = key,
                    userEmail = usr.email,
                    userDisplayName = usr.displayName.ifBlank { "Pixora Member" },
                    utrReference = usr.premiumUtr.ifBlank { "VIP_ACTIVE" },
                    planName = usr.premiumPlan.ifBlank { "₹299 VIP Creator Pass" },
                    amountInr = 299L,
                    status = "active",
                    createdAt = usr.premiumPurchasedAt ?: usr.updatedAt
                )
            }
        }
        byUserId.values.toList()
    }

    var activeSection by remember { mutableStateOf("overview") }
    var mediaQuickSearch by remember { mutableStateOf("") }
    val filteredDashWallpapers = remember(wallpapers, mediaQuickSearch) {
        val q = mediaQuickSearch.trim().lowercase(Locale.ROOT)
        if (q.isBlank()) wallpapers
        else wallpapers.filter {
            it.title.lowercase(Locale.ROOT).contains(q) ||
                it.category.lowercase(Locale.ROOT).contains(q)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_dashboard_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Executive Bento Command Hero
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                glowColor = AmberGlow,
                secondaryGlowColor = ElectricCyan,
                elevated = true,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (pendingSyncCount == 0) EmeraldGlow else AmberGlow)
                                )
                                Text(
                                    text = if (pendingSyncCount == 0) "USER PANEL CONNECTED • LIVE"
                                    else "USER PANEL LIVE • $pendingSyncCount QUEUED CLOUD",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (pendingSyncCount == 0) EmeraldGlow else AmberGlow
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pixora Admin Hub",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = glass.textPrimary
                            )
                            Text(
                                text = OFFICIAL_ADMIN_EMAIL,
                                style = MaterialTheme.typography.labelSmall,
                                color = glass.textSecondary
                            )
                        }
                        LiquidGlassChip(
                            label = "User Panel →",
                            selected = true,
                            accentColor = ElectricCyan,
                            onClick = onSwitchToUserPanel,
                            testTag = "switch_to_user_panel_chip"
                        )
                    }

                    // Live Admin <-> User Panel Bridge Summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = if (glass.isDark) 0.08f else 0.55f))
                            .border(
                                1.dp,
                                (if (pendingSyncCount == 0) EmeraldGlow else AmberGlow).copy(alpha = 0.45f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live in User Panel: $publishedCount Published • ${categories.count { it.enabled }} Categories",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = glass.textPrimary
                            )
                            Text(
                                text = if (pendingSyncCount == 0) {
                                    "All uploads, edits, drafts & layouts reflect immediately"
                                } else {
                                    "Live locally on User Panel • $pendingSyncCount change(s) pending cloud push"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = glass.textSecondary
                            )
                        }
                    }

                    // 2 TB Cloud Vault Bar
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        val usedBytes = remember(wallpapers) {
                            wallpapers.sumOf { it.fileSizeBytes.coerceAtLeast(2_400_000L) }
                        }
                        val usedMbStr = remember(usedBytes) {
                            val mb = usedBytes / (1024f * 1024f)
                            if (mb >= 1024f) {
                                String.format(Locale.US, "%.2f GB", mb / 1024f)
                            } else {
                                String.format(Locale.US, "%.1f MB", mb)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cloud Vault Quota",
                                style = MaterialTheme.typography.labelSmall,
                                color = glass.textSecondary
                            )
                            Text(
                                text = "$usedMbStr / 2 TB",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                        }
                        LinearProgressIndicator(
                            progress = { 0.04f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = EmeraldGlow,
                            trackColor = Color.White.copy(alpha = 0.14f)
                        )
                    }

                    LiquidGlassButton(
                        text = if (pendingSyncCount > 0) {
                            "Sync Pending ($pendingSyncCount) to Cloud & User Panel"
                        } else {
                            "Sync Catalog to Cloud & User Panel"
                        },
                        icon = Icons.Default.Sync,
                        glowColor = if (pendingSyncCount > 0) AmberGlow else ElectricCyan,
                        filledAccent = true,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "seed_catalog_button",
                        onClick = onSeedCloudCatalog
                    )
                }
            }
        }

        // Modular Structure Switcher Bar
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_dashboard_structure_tabs"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    LiquidGlassChip(
                        label = "Overview",
                        selected = activeSection == "overview",
                        accentColor = ElectricCyan,
                        leadingIcon = Icons.Default.Dashboard,
                        onClick = { activeSection = "overview" },
                        testTag = "admin_structure_overview"
                    )
                }
                item {
                    LiquidGlassChip(
                        label = "VIP (${mergedPremiumMembers.size})",
                        selected = activeSection == "vip",
                        accentColor = AmberGlow,
                        leadingIcon = Icons.Default.WorkspacePremium,
                        onClick = { activeSection = "vip" },
                        testTag = "admin_structure_vip"
                    )
                }
                item {
                    LiquidGlassChip(
                        label = "User Layouts",
                        selected = activeSection == "design",
                        accentColor = NeonViolet,
                        leadingIcon = Icons.Default.Palette,
                        onClick = { activeSection = "design" },
                        testTag = "admin_structure_design"
                    )
                }
                item {
                    LiquidGlassChip(
                        label = "Media (${wallpapers.size})",
                        selected = activeSection == "media",
                        accentColor = EmeraldGlow,
                        leadingIcon = Icons.Default.Wallpaper,
                        onClick = { activeSection = "media" },
                        testTag = "admin_structure_media"
                    )
                }
            }
        }

        when (activeSection) {
            "overview" -> {
                // 2x3 Bento KPI Matrix
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminMetricCard(
                                title = "Wallpapers",
                                value = wallpapers.size.toString(),
                                icon = Icons.Default.Wallpaper,
                                accent = ElectricCyan,
                                modifier = Modifier.weight(1f)
                            )
                            AdminMetricCard(
                                title = "Users",
                                value = users.size.coerceAtLeast(1).toString(),
                                icon = Icons.Default.People,
                                accent = NeonViolet,
                                modifier = Modifier.weight(1f)
                            )
                            AdminMetricCard(
                                title = "VIP Pass",
                                value = mergedPremiumMembers.size.toString(),
                                icon = Icons.Default.WorkspacePremium,
                                accent = AmberGlow,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminMetricCard(
                                title = "Downloads",
                                value = totalDownloads.toString(),
                                icon = Icons.Default.Download,
                                accent = EmeraldGlow,
                                modifier = Modifier.weight(1f)
                            )
                            AdminMetricCard(
                                title = "Favorites",
                                value = totalFavorites.toString(),
                                icon = Icons.Default.Favorite,
                                accent = RoseGlow,
                                modifier = Modifier.weight(1f)
                            )
                            AdminMetricCard(
                                title = "Categories",
                                value = categories.size.toString(),
                                icon = Icons.Default.Category,
                                accent = AmberGlow,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Active UI/UX Design Structure Banner
                item {
                    val accentColor = remember(selectedAppDesign.recommendedOptics.tintHex) {
                        runCatching {
                            Color(android.graphics.Color.parseColor(selectedAppDesign.recommendedOptics.tintHex))
                        }.getOrDefault(ElectricCyan)
                    }
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        glowColor = accentColor,
                        contentPadding = PaddingValues(16.dp),
                        onClick = { activeSection = "design" }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AdminDesignWireframePreview(
                                design = selectedAppDesign,
                                accentColor = accentColor,
                                modifier = Modifier.size(width = 62.dp, height = 62.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ACTIVE APP STRUCTURE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = accentColor
                                )
                                Text(
                                    text = selectedAppDesign.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = glass.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${selectedAppDesign.gridColumns}-Col Grid • ${selectedAppDesign.cardCornerDp}dp Radius • ${selectedAppDesign.layoutMode.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glass.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            LiquidGlassChip(
                                label = "Change →",
                                selected = true,
                                accentColor = accentColor,
                                onClick = { activeSection = "design" },
                                testTag = "overview_change_design_chip"
                            )
                        }
                    }
                }

                // Premium Members Snapshot
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👑 Premium Members (${mergedPremiumMembers.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = AmberGlow
                        )
                        if (mergedPremiumMembers.isNotEmpty()) {
                            LiquidGlassChip(
                                label = "Manage All →",
                                selected = false,
                                accentColor = AmberGlow,
                                onClick = { activeSection = "vip" }
                            )
                        }
                    }
                }
                if (mergedPremiumMembers.isEmpty()) {
                    item {
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp,
                            glowColor = AmberGlow,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Text(
                                text = "No Premium purchases yet. Any user who unlocks ₹299 VIP via QR will appear here with their email & UTR ID.",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                        }
                    }
                } else {
                    items(mergedPremiumMembers.take(3), key = { "prem_ov_${it.id}_${it.userId}" }) { buyer ->
                        AdminPremiumBuyerCard(
                            buyer = buyer,
                            onUpdateStatus = { newStatus -> onUpdatePremiumPurchaseStatus(buyer, newStatus) }
                        )
                    }
                }

                // Most Downloaded Wallpapers
                item {
                    Text(
                        text = "Top Performing Wallpapers",
                        style = MaterialTheme.typography.titleLarge,
                        color = glass.textPrimary
                    )
                }
                items(topDownloaded, key = { "top_${it.id}" }) { wall ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(12.dp),
                        onClick = { onSelectWallpaper(wall) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WallxAsyncImage(
                                imageUrl = wall.thumbnailUrl,
                                contentDescription = wall.title,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wall.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = glass.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${wall.category} • ${wall.resolution}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glass.glowCyan
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${wall.downloadCount} DLs",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EmeraldGlow
                                )
                                Text(
                                    text = "${wall.favoriteCount} Favs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoseGlow
                                )
                            }
                        }
                    }
                }

                // Latest Registered Users
                item {
                    Text(
                        text = "Recent Users (${users.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = glass.textPrimary
                    )
                }
                items(users.take(5), key = { "usr_${it.id.ifBlank { it.userId }}" }) { usr ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = usr.displayName.ifBlank { "Pixora User" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = glass.textPrimary
                                )
                                Text(
                                    text = usr.email,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glass.glowCyan
                                )
                            }
                            Text(
                                text = if (usr.isPremium) "VIP" else usr.role.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (usr.isPremium || usr.isAdminRole) AmberGlow else EmeraldGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            "vip" -> {
                item {
                    Text(
                        text = "👑 All Premium VIP Members (${mergedPremiumMembers.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = AmberGlow
                    )
                }
                if (mergedPremiumMembers.isEmpty()) {
                    item {
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            glowColor = AmberGlow,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Text(
                                text = "No Premium purchases yet. Any user who unlocks ₹299 VIP via QR will appear here with their email & UTR ID.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = glass.textSecondary
                            )
                        }
                    }
                } else {
                    items(mergedPremiumMembers, key = { "prem_dash_${it.id}_${it.userId}" }) { buyer ->
                        AdminPremiumBuyerCard(
                            buyer = buyer,
                            onUpdateStatus = { newStatus -> onUpdatePremiumPurchaseStatus(buyer, newStatus) }
                        )
                    }
                }
            }

            "design" -> {
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        glowColor = NeonViolet,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "✨ User Layout Studio (10 New Premium + Classic)",
                                style = MaterialTheme.typography.titleLarge,
                                color = glass.textPrimary
                            )
                            Text(
                                text = "Select any modern premium User Layout below to instantly transform the User Home grid, hero banner, card overlays, corners & theme live.",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                            AdminDesignStructureGrid(
                                selectedAppDesign = selectedAppDesign,
                                onSelectAppDesign = onSelectAppDesign
                            )
                        }
                    }
                }
            }

            "media" -> {
                item {
                    LiquidGlassTextField(
                        value = mediaQuickSearch,
                        onValueChange = { mediaQuickSearch = it },
                        placeholder = "Filter cloud wallpapers by title or category...",
                        leadingIcon = Icons.Default.Search
                    )
                }
                item {
                    Text(
                        text = "Active Cloud Wallpapers (${filteredDashWallpapers.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = glass.textPrimary
                    )
                }
                items(filteredDashWallpapers, key = { "dash_all_${it.id}" }) { wall ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        contentPadding = PaddingValues(12.dp),
                        onClick = { onSelectWallpaper(wall) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WallxAsyncImage(
                                imageUrl = wall.imageUrl.ifBlank { wall.thumbnailUrl },
                                contentDescription = wall.title,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wall.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = glass.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${wall.category} • ${wall.resolution} • ${wall.fileSize}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glass.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            LiquidGlassChip(
                                label = "Delete",
                                selected = true,
                                accentColor = RoseGlow,
                                leadingIcon = Icons.Default.Delete,
                                onClick = { onDeleteWallpaper(wall) },
                                testTag = "dash_delete_${wall.id}"
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}

@Composable
private fun AdminPremiumBuyerCard(
    buyer: PremiumPurchaseModel,
    onUpdateStatus: (String) -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        glowColor = AmberGlow,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Premium Buyer",
                        tint = AmberGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = buyer.userDisplayName.ifBlank { "Pixora Member" },
                        style = MaterialTheme.typography.titleMedium,
                        color = glass.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = buyer.userEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${buyer.planName.ifBlank { "₹299 VIP" }} • UTR: ${buyer.utrReference.ifBlank { "N/A" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberGlow
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (buyer.status == "verified") EmeraldGlow.copy(alpha = 0.24f)
                            else AmberGlow.copy(alpha = 0.24f)
                        )
                        .border(
                            1.dp,
                            if (buyer.status == "verified") EmeraldGlow else AmberGlow,
                            CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = buyer.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (buyer.status != "verified") {
                        LiquidGlassChip(
                            label = "Verify",
                            selected = true,
                            accentColor = EmeraldGlow,
                            onClick = { onUpdateStatus("verified") }
                        )
                    }
                    LiquidGlassChip(
                        label = "Revoke",
                        selected = false,
                        accentColor = RoseGlow,
                        onClick = { onUpdateStatus("revoked") }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDesignStructureGrid(
    selectedAppDesign: AppDesignStyle,
    onSelectAppDesign: (AppDesignStyle) -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    val premiumRows = remember { AppDesignCatalog.premiumUserLayouts.chunked(2) }
    val classicRows = remember { AppDesignCatalog.classicDesigns.chunked(2) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 10 NEW MODERN PREMIUM USER LAYOUTS SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✨ 10 New Modern Premium Layouts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = AmberGlow
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AmberGlow.copy(alpha = 0.18f))
                    .border(1.dp, AmberGlow.copy(alpha = 0.65f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "10 NEW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = AmberGlow
                )
            }
        }

        AdminDesignRowsSection(
            rows = premiumRows,
            selectedAppDesign = selectedAppDesign,
            glassTextPrimary = glass.textPrimary,
            glassTextSecondary = glass.textSecondary,
            onSelectAppDesign = onSelectAppDesign
        )

        Spacer(modifier = Modifier.height(4.dp))

        // CLASSIC & CORE LAYOUTS SECTION
        Text(
            text = "Classic & Core User Layouts (${AppDesignCatalog.classicDesigns.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = glass.textSecondary
        )

        AdminDesignRowsSection(
            rows = classicRows,
            selectedAppDesign = selectedAppDesign,
            glassTextPrimary = glass.textPrimary,
            glassTextSecondary = glass.textSecondary,
            onSelectAppDesign = onSelectAppDesign
        )
    }
}

@Composable
private fun AdminDesignRowsSection(
    rows: List<List<AppDesignStyle>>,
    selectedAppDesign: AppDesignStyle,
    glassTextPrimary: Color,
    glassTextSecondary: Color,
    onSelectAppDesign: (AppDesignStyle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { design ->
                    val isSelected = selectedAppDesign.id == design.id
                    val accentColor = remember(design.recommendedOptics.tintHex) {
                        runCatching {
                            Color(android.graphics.Color.parseColor(design.recommendedOptics.tintHex))
                        }.getOrDefault(ElectricCyan)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) accentColor.copy(alpha = 0.18f)
                                else Color.White.copy(alpha = 0.06f)
                            )
                            .border(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onSelectAppDesign(design) }
                            .padding(12.dp)
                            .testTag("design_structure_card_${design.id}"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> EmeraldGlow
                                            design.isNewPremiumLayout -> AmberGlow.copy(alpha = 0.25f)
                                            else -> accentColor.copy(alpha = 0.22f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when {
                                        isSelected -> "ACTIVE"
                                        design.isNewPremiumLayout -> "NEW • ${design.gridColumns}-COL"
                                        else -> "${design.gridColumns}-COL"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = if (!isSelected && design.isNewPremiumLayout) AmberGlow else Color.White
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active Design",
                                    tint = EmeraldGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        AdminDesignWireframePreview(
                            design = design,
                            accentColor = accentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                        )

                        Text(
                            text = design.name,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) accentColor else glassTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${design.layoutMode.uppercase()} • ${design.cardCornerDp}dp • ${design.recommendedTheme.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = glassTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AdminDesignWireframePreview(
    design: AppDesignStyle,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val bgTint = if (design.recommendedTheme == "dark") {
        Color(0xFF0A0F1E)
    } else {
        Color(0xFFEFF4FF)
    }
    val blockTint = accentColor.copy(alpha = if (design.recommendedTheme == "dark") 0.36f else 0.26f)
    val corner = (design.cardCornerDp / 3f).coerceIn(4f, 12f).dp
    val cols = design.gridColumns.coerceIn(1, 3)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgTint)
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Miniature Hero indicator if layout uses hero banner
            if (design.heroStyle != "minimal_header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (design.heroStyle == "compact_banner") 10.dp else 14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.22f))
                        .border(0.6.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(cols) { colIndex ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val topWeight = if (design.layoutMode == "bento" && colIndex == 0) 0.65f else 0.52f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(topWeight)
                                .clip(RoundedCornerShape(corner))
                                .background(blockTint)
                                .border(0.8.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(corner))
                        )
                        if (cols > 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f - topWeight)
                                    .clip(RoundedCornerShape(corner))
                                    .background(blockTint.copy(alpha = 0.65f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlassColors.current
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        glowColor = accent,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = glass.textPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = glass.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 2. ADMIN WALLPAPER MANAGEMENT & STORAGE UPLOAD SCREEN
// -----------------------------------------------------------------------------

@Composable
fun AdminWallpaperManagementScreen(
    wallpapers: List<WallpaperItem>,
    categories: List<CategoryModel>,
    uploadProgress: Float?,
    currentUserId: String,
    publishedCount: Int = wallpapers.count { it.status.isBlank() || it.status.equals("published", ignoreCase = true) },
    pendingSyncCount: Int = 0,
    onSyncCloudCatalog: () -> Unit = {},
    onOpenUserPanel: () -> Unit = {},
    onUploadMediaWallpapers: (List<Uri>, String, String, String, String, String, Boolean, Boolean) -> Unit,
    onCreateOrUpdateWallpaper: (WallpaperItem, Boolean) -> Unit,
    onDeleteWallpaper: (WallpaperItem) -> Unit,
    onRestoreDeletedWallpapers: () -> Unit = {}
) {
    val context = LocalContext.current
    val glass = LocalLiquidGlassColors.current

    var editingWallpaper by remember { mutableStateOf<WallpaperItem?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.name ?: "AMOLED") }
    var tagsInput by remember { mutableStateOf("amoled, ios27, 4k, liquid") }
    var resolutionInput by remember { mutableStateOf("3840x2160 (4K)") }
    var imageUrlInput by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("published") }
    var isFeatured by remember { mutableStateOf(true) }
    var isTrending by remember { mutableStateOf(true) }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var catalogSearchQuery by remember { mutableStateOf("") }
    var catalogFilterCategory by remember { mutableStateOf("All") }

    val filteredCatalogWallpapers = remember(wallpapers, catalogSearchQuery, catalogFilterCategory) {
        wallpapers.filter { wall ->
            val catMatch = catalogFilterCategory == "All" ||
                wall.category.equals(catalogFilterCategory, ignoreCase = true)
            val queryMatch = catalogSearchQuery.isBlank() ||
                wall.title.contains(catalogSearchQuery, ignoreCase = true) ||
                wall.category.contains(catalogSearchQuery, ignoreCase = true) ||
                wall.tags.any { it.contains(catalogSearchQuery, ignoreCase = true) }
            catMatch && queryMatch
        }
    }

    var pendingAutoPublishAfterPick by remember { mutableStateOf(false) }

    val triggerImmediateUpload: (List<Uri>) -> Unit = { urisToUpload ->
        if (urisToUpload.isNotEmpty()) {
            val autoTagsRaw = "${selectedCategory.lowercase(Locale.ROOT)}, 4k, liquid"
            onUploadMediaWallpapers(
                urisToUpload,
                title.ifBlank { "Liquid Glass Master" },
                description.ifBlank { "Curated $selectedCategory wallpaper" },
                selectedCategory,
                autoTagsRaw,
                status,
                isFeatured,
                isTrending
            )
            selectedUris = emptyList()
            title = ""
            description = ""
        }
    }

    // Single Image Photo Picker — immediately uploads to User Panel & Cloud Firestore upon selection
    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        pendingAutoPublishAfterPick = false
        if (uri != null) {
            triggerImmediateUpload(listOf(uri))
        }
    }

    // Multiple Image Photo Picker — immediately uploads all selected images to User Panel & Cloud Firestore
    val multiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(50)
    ) { uris ->
        pendingAutoPublishAfterPick = false
        if (uris.isNotEmpty()) {
            triggerImmediateUpload(uris)
        }
    }

    val adminUsedBytes = remember(wallpapers) {
        wallpapers.sumOf { it.fileSizeBytes.coerceAtLeast(2_400_000L) }
    }
    val adminUsedFormatted = remember(adminUsedBytes) {
        val mb = adminUsedBytes / (1024f * 1024f)
        if (mb >= 1024f) String.format(Locale.US, "%.2f GB", mb / 1024f)
        else String.format(Locale.US, "%.1f MB", mb)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_wallpapers_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                glowColor = if (pendingSyncCount == 0) EmeraldGlow else AmberGlow,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "User Panel Link • $publishedCount Live / ${(wallpapers.size - publishedCount).coerceAtLeast(0)} Drafts",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = glass.textPrimary
                            )
                            Text(
                                text = if (pendingSyncCount == 0) {
                                    "Connected permanently to User Panel & Cloud"
                                } else {
                                    "$pendingSyncCount upload/edit(s) live locally & queued for Cloud"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (pendingSyncCount == 0) EmeraldGlow else AmberGlow
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LiquidGlassChip(
                                label = if (pendingSyncCount > 0) "Sync ($pendingSyncCount)" else "Sync",
                                selected = pendingSyncCount > 0,
                                accentColor = if (pendingSyncCount > 0) AmberGlow else ElectricCyan,
                                leadingIcon = Icons.Default.Sync,
                                onClick = {
                                    if (selectedUris.isNotEmpty()) {
                                        triggerImmediateUpload(selectedUris)
                                    }
                                    onSyncCloudCatalog()
                                }
                            )
                            LiquidGlassChip(
                                label = "User →",
                                selected = true,
                                accentColor = EmeraldGlow,
                                onClick = {
                                    if (selectedUris.isNotEmpty()) {
                                        triggerImmediateUpload(selectedUris)
                                    }
                                    onOpenUserPanel()
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                elevated = true,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (editingWallpaper == null) "Upload / Publish to User Panel & Cloud (24/7 Live)"
                        else "Edit Wallpaper: ${editingWallpaper?.title}",
                        style = MaterialTheme.typography.titleLarge,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Supports Single & Batch Uploads (Up to 50 Files/Batch) • Publishes immediately to User Panel + Cloud Firestore (2 TB Quota)",
                        style = MaterialTheme.typography.labelSmall,
                        color = glass.glowCyan
                    )

                    // 2 TB Admin Cloud Firestore Storage Meter inside Upload Studio
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = if (glass.isDark) 0.06f else 0.45f))
                            .border(1.dp, EmeraldGlow.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CLOUD FIRESTORE 2 TB CATALOG",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = EmeraldGlow
                            )
                            Text(
                                text = "$adminUsedFormatted used of 2 TB (2,048 GB)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = glass.textPrimary
                            )
                        }
                        LinearProgressIndicator(
                            progress = {
                                (adminUsedBytes.toFloat() / (2f * 1024f * 1024f * 1024f * 1024f)).coerceIn(0.01f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = EmeraldGlow,
                            trackColor = Color.White.copy(alpha = 0.16f)
                        )
                    }

                    // Single & Multi Photo Picker Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassButton(
                            text = "Single Image",
                            icon = Icons.Default.AddPhotoAlternate,
                            modifier = Modifier.weight(1f),
                            testTag = "admin_pick_single_image_button",
                            onClick = {
                                singlePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        LiquidGlassButton(
                            text = "Multiple Images",
                            icon = Icons.Default.PhotoLibrary,
                            glowColor = NeonViolet,
                            modifier = Modifier.weight(1f),
                            testTag = "admin_pick_multi_image_button",
                            onClick = {
                                multiPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }

                    if (selectedUris.isNotEmpty()) {
                        LiquidGlassButton(
                            text = "Upload & Publish ${selectedUris.size} Wallpaper(s) to Firestore Now",
                            icon = Icons.Default.CloudUpload,
                            filledAccent = true,
                            glowColor = EmeraldGlow,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "admin_quick_upload_selected_button",
                            onClick = {
                                triggerImmediateUpload(selectedUris)
                            }
                        )
                        Text(
                            text = "${selectedUris.size} high-res image(s) ready • Tap above to publish now or customize title/category below",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldGlow
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedUris, key = { it.toString() }) { uri ->
                                WallxAsyncImage(
                                    imageUrl = uri.toString(),
                                    contentDescription = "Queued 4K Wallpaper Preview",
                                    isFullQualityInspection = true,
                                    modifier = Modifier
                                        .size(width = 76.dp, height = 110.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    LiquidGlassTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Wallpaper Title (e.g., Cyber Prism 8K)",
                        testTag = "admin_wallpaper_title_input"
                    )

                    // Category Horizontal Selector
                    Text(
                        text = "Category: $selectedCategory",
                        style = MaterialTheme.typography.labelLarge,
                        color = glass.textPrimary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories, key = { it.id }) { cat ->
                            LiquidGlassChip(
                                label = cat.name,
                                selected = selectedCategory.equals(cat.name, ignoreCase = true),
                                onClick = { selectedCategory = cat.name }
                            )
                        }
                    }

                    LiquidGlassTextField(
                        value = resolutionInput,
                        onValueChange = { resolutionInput = it },
                        placeholder = "Resolution (e.g., 3840x2160 (4K) or 7680x4320 (8K))"
                    )

                    Text(
                        text = "🔗 Or Add via Direct Image Link (URL):",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )

                    LiquidGlassTextField(
                        value = imageUrlInput,
                        onValueChange = { imageUrlInput = it },
                        placeholder = "Paste Direct Image URL (https://... or image link)",
                        testTag = "admin_wallpaper_url_input"
                    )

                    if (imageUrlInput.trim().isNotBlank()) {
                        LiquidGlassButton(
                            text = "Publish Wallpaper via Image Link Now",
                            icon = Icons.Default.CloudUpload,
                            filledAccent = true,
                            glowColor = ElectricCyan,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "admin_publish_url_button",
                            onClick = {
                                val resolvedUrl = imageUrlInput.trim()
                                val tags = listOf(selectedCategory.lowercase(Locale.ROOT), "4k", "link")
                                val item = WallpaperItem(
                                    id = "wall_link_${UUID.randomUUID().toString().replace("-", "").take(12)}",
                                    title = title.ifBlank { "$selectedCategory URL Wallpaper" },
                                    description = description.ifBlank { "Curated $selectedCategory wallpaper via direct link." },
                                    category = selectedCategory,
                                    tags = tags,
                                    resolution = resolutionInput.ifBlank { "3840x2160 (4K)" },
                                    imageUrl = resolvedUrl,
                                    thumbnailUrl = resolvedUrl,
                                    status = status,
                                    isFeatured = isFeatured,
                                    isTrending = isTrending,
                                    uploadedBy = currentUserId
                                )
                                onCreateOrUpdateWallpaper(item, true)
                                title = ""
                                description = ""
                                imageUrlInput = ""
                            }
                        )
                    }

                    // Visibility (Published vs Draft), Featured & Trending Switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassChip(
                            label = "Published",
                            selected = status == "published",
                            accentColor = EmeraldGlow,
                            onClick = { status = "published" },
                            modifier = Modifier.weight(1f)
                        )
                        LiquidGlassChip(
                            label = "Draft",
                            selected = status == "draft",
                            accentColor = AmberGlow,
                            onClick = { status = "draft" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    LiquidGlassSwitch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it },
                        label = "Feature on Homepage Hero & Rail"
                    )

                    LiquidGlassSwitch(
                        checked = isTrending,
                        onCheckedChange = { isTrending = it },
                        label = "Highlight in Trending Section"
                    )

                    if (uploadProgress != null) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Uploading to Cloud Firestore (24/7 Live)... ${(uploadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { uploadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = ElectricCyan
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (editingWallpaper != null) {
                            LiquidGlassButton(
                                text = "Cancel Edit",
                                glowColor = RoseGlow,
                                modifier = Modifier.weight(0.8f),
                                onClick = {
                                    editingWallpaper = null
                                    title = ""
                                    description = ""
                                    imageUrlInput = ""
                                    selectedUris = emptyList()
                                }
                            )
                        }

                        LiquidGlassButton(
                            text = when {
                                selectedUris.isNotEmpty() -> "Upload ${selectedUris.size} File(s)"
                                editingWallpaper != null -> "Save Changes"
                                else -> "Publish Wallpaper"
                            },
                            icon = Icons.Default.CloudUpload,
                            filledAccent = true,
                            modifier = Modifier.weight(1.2f),
                            testTag = "admin_publish_wallpaper_button",
                            onClick = {
                                if (selectedUris.isNotEmpty()) {
                                    triggerImmediateUpload(selectedUris)
                                } else if (imageUrlInput.trim().isNotBlank() || editingWallpaper != null) {
                                    val resolvedUrl = imageUrlInput.trim().ifBlank {
                                        editingWallpaper?.imageUrl ?: ""
                                    }
                                    if (resolvedUrl.isNotBlank()) {
                                        val tags = editingWallpaper?.tags?.ifEmpty {
                                            listOf(selectedCategory.lowercase(Locale.ROOT), "4k")
                                        } ?: listOf(selectedCategory.lowercase(Locale.ROOT), "4k")
                                        val item = (editingWallpaper ?: WallpaperItem(
                                            id = "wall_${UUID.randomUUID().toString().replace("-", "").take(12)}",
                                            uploadedBy = currentUserId
                                        )).copy(
                                            title = title.ifBlank { "$selectedCategory 4K Wallpaper" },
                                            description = description.ifBlank {
                                                editingWallpaper?.description?.ifBlank { "Curated $selectedCategory 4K wallpaper." }
                                                    ?: "Curated $selectedCategory 4K wallpaper."
                                            },
                                            category = selectedCategory,
                                            tags = tags,
                                            resolution = resolutionInput.ifBlank { "3840x2160 (4K)" },
                                            imageUrl = resolvedUrl,
                                            thumbnailUrl = resolvedUrl,
                                            status = status,
                                            isFeatured = isFeatured,
                                            isTrending = isTrending
                                        )
                                        onCreateOrUpdateWallpaper(item, editingWallpaper == null)
                                        editingWallpaper = null
                                        title = ""
                                        description = ""
                                        imageUrlInput = ""
                                    }
                                } else {
                                    pendingAutoPublishAfterPick = true
                                    singlePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // Existing Wallpapers Catalog Management List (All Home + Uploaded + Cloud Wallpapers)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All App Wallpapers (${filteredCatalogWallpapers.size}/${wallpapers.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = glass.textPrimary
                    )
                    LiquidGlassChip(
                        label = "Restore Deleted",
                        selected = false,
                        accentColor = ElectricCyan,
                        leadingIcon = Icons.Default.Sync,
                        onClick = onRestoreDeletedWallpapers
                    )
                }

                LiquidGlassTextField(
                    value = catalogSearchQuery,
                    onValueChange = { catalogSearchQuery = it },
                    placeholder = "Search any wallpaper to edit or delete...",
                    leadingIcon = Icons.Default.Search,
                    testTag = "admin_search_wallpapers_input"
                )

                val filterCatNames = remember(categories, wallpapers) {
                    (listOf("All") + categories.map { it.name } + wallpapers.map { it.category })
                        .distinctBy { it.lowercase(Locale.ROOT) }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterCatNames, key = { "adm_cat_filter_$it" }) { catName ->
                        LiquidGlassChip(
                            label = catName,
                            selected = catalogFilterCategory.equals(catName, ignoreCase = true),
                            onClick = { catalogFilterCategory = catName }
                        )
                    }
                }
            }
        }

        items(filteredCatalogWallpapers, key = { it.id }) { wall ->
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 32.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WallxAsyncImage(
                            imageUrl = wall.thumbnailUrl.ifBlank { wall.imageUrl },
                            contentDescription = wall.title,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = wall.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = glass.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val isWallPublished = wall.status.isBlank() || wall.status.equals("published", ignoreCase = true)
                            Text(
                                text = "${wall.category} • ${wall.resolution} • ${if (isWallPublished) "PUBLISHED" else "DRAFT"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isWallPublished) EmeraldGlow else AmberGlow
                            )
                            Text(
                                text = "${wall.downloadCount} DLs • ${wall.favoriteCount} Favs • ${wall.fileSize}",
                                style = MaterialTheme.typography.labelSmall,
                                color = glass.textSecondary
                            )
                        }
                        LiquidGlassCircularButton(
                            icon = Icons.Default.Edit,
                            contentDescription = "Edit ${wall.title}",
                            size = 40.dp,
                            iconSize = 18.dp,
                            tint = ElectricCyan,
                            onClick = {
                                editingWallpaper = wall
                                title = wall.title
                                description = wall.description
                                selectedCategory = wall.category
                                tagsInput = wall.tags.joinToString(", ")
                                resolutionInput = wall.resolution
                                imageUrlInput = wall.imageUrl
                                status = if (wall.status.equals("draft", ignoreCase = true)) "draft" else "published"
                                isFeatured = wall.isFeatured
                                isTrending = wall.isTrending
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LiquidGlassChip(
                            label = "Delete",
                            selected = true,
                            accentColor = RoseGlow,
                            leadingIcon = Icons.Default.Delete,
                            onClick = { onDeleteWallpaper(wall) },
                            testTag = "admin_delete_wallpaper_${wall.id}"
                        )
                    }

                    // Quick Toggles for Featured, Trending, and Published/Draft
                    val isPublishedNow = wall.status.isBlank() || wall.status.equals("published", ignoreCase = true)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LiquidGlassChip(
                            label = if (wall.isFeatured) "Featured ★" else "Feature",
                            selected = wall.isFeatured,
                            accentColor = AmberGlow,
                            leadingIcon = Icons.Default.Star,
                            onClick = {
                                onCreateOrUpdateWallpaper(wall.copy(isFeatured = !wall.isFeatured), false)
                            }
                        )
                        LiquidGlassChip(
                            label = if (wall.isTrending) "Trending 🔥" else "Trend",
                            selected = wall.isTrending,
                            accentColor = ElectricCyan,
                            leadingIcon = Icons.Default.LocalFireDepartment,
                            onClick = {
                                onCreateOrUpdateWallpaper(wall.copy(isTrending = !wall.isTrending), false)
                            }
                        )
                        LiquidGlassChip(
                            label = if (isPublishedNow) "Published" else "Draft",
                            selected = isPublishedNow,
                            accentColor = EmeraldGlow,
                            leadingIcon = if (isPublishedNow) Icons.Default.Public else Icons.Default.VisibilityOff,
                            onClick = {
                                val nextStatus = if (isPublishedNow) "draft" else "published"
                                onCreateOrUpdateWallpaper(wall.copy(status = nextStatus), false)
                            }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(110.dp)) }
    }
}

// -----------------------------------------------------------------------------
// 3. ADMIN CATEGORY MANAGEMENT SCREEN (Create, Edit, Delete, Reorder, Enable/Disable)
// -----------------------------------------------------------------------------

@Composable
fun AdminCategoryManagementScreen(
    categories: List<CategoryModel>,
    onSaveCategory: (CategoryModel, Boolean) -> Unit,
    onDeleteCategory: (String, String) -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    var editingCategory by remember { mutableStateOf<CategoryModel?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var accentHex by remember { mutableStateOf("#00F0FF") }
    var enabled by remember { mutableStateOf(true) }

    val categoryImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            thumbnailUrl = uri.toString()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_categories_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                elevated = true,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (editingCategory == null) "Create New Category"
                        else "Edit Category: ${editingCategory?.name}",
                        style = MaterialTheme.typography.titleLarge,
                        color = glass.textPrimary
                    )

                    LiquidGlassTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Category Name (e.g., Cyber Minimal)",
                        testTag = "admin_category_name_input"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidGlassTextField(
                            value = thumbnailUrl,
                            onValueChange = { thumbnailUrl = it },
                            placeholder = "Category Thumbnail URL or Pick Image",
                            modifier = Modifier.weight(1f)
                        )
                        LiquidGlassButton(
                            text = "Pick Image",
                            icon = Icons.Default.AddPhotoAlternate,
                            glowColor = ElectricCyan,
                            onClick = {
                                categoryImagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }

                    LiquidGlassSwitch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        label = "Category Enabled in User Panel"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (editingCategory != null) {
                            LiquidGlassButton(
                                text = "Cancel",
                                glowColor = RoseGlow,
                                modifier = Modifier.weight(0.8f),
                                onClick = {
                                    editingCategory = null
                                    name = ""
                                    description = ""
                                    thumbnailUrl = ""
                                }
                            )
                        }
                        LiquidGlassButton(
                            text = if (editingCategory == null) "Add Category" else "Save Category",
                            filledAccent = true,
                            modifier = Modifier.weight(1.2f),
                            testTag = "admin_save_category_button",
                            onClick = {
                                if (name.isNotBlank()) {
                                    val catId = editingCategory?.id ?: "cat_${name.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")}"
                                    val nextOrder = editingCategory?.order ?: 0L
                                    val model = CategoryModel(
                                        id = catId,
                                        name = name.trim(),
                                        description = description.ifBlank { "Curated ${name.trim()} collection" },
                                        thumbnailUrl = thumbnailUrl.ifBlank {
                                            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80"
                                        },
                                        order = nextOrder,
                                        enabled = enabled,
                                        wallpaperCount = editingCategory?.wallpaperCount ?: 1L,
                                        accentHex = accentHex
                                    )
                                    onSaveCategory(model, editingCategory == null)
                                    editingCategory = null
                                    name = ""
                                    description = ""
                                    thumbnailUrl = ""
                                }
                            }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Categories (${categories.size}) • Reorder & Visibility",
                style = MaterialTheme.typography.titleLarge,
                color = glass.textPrimary
            )
        }

        items(categories, key = { it.id }) { cat ->
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WallxAsyncImage(
                        imageUrl = cat.thumbnailUrl,
                        contentDescription = cat.name,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${cat.order}. ${cat.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = glass.textPrimary
                        )
                        Text(
                            text = if (cat.enabled) "ENABLED" else "DISABLED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (cat.enabled) EmeraldGlow else RoseGlow
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Move Up Order
                        LiquidGlassCircularButton(
                            icon = Icons.Default.ArrowUpward,
                            contentDescription = "Move Up",
                            size = 32.dp,
                            iconSize = 16.dp,
                            tint = glass.textPrimary,
                            onClick = {
                                val newOrder = (cat.order - 1L).coerceAtLeast(1L)
                                onSaveCategory(cat.copy(order = newOrder), false)
                            }
                        )
                        // Move Down Order
                        LiquidGlassCircularButton(
                            icon = Icons.Default.ArrowDownward,
                            contentDescription = "Move Down",
                            size = 32.dp,
                            iconSize = 16.dp,
                            tint = glass.textPrimary,
                            onClick = {
                                val newOrder = (cat.order + 1L).coerceAtMost(1000L)
                                onSaveCategory(cat.copy(order = newOrder), false)
                            }
                        )
                        // Toggle Enabled
                        LiquidGlassCircularButton(
                            icon = if (cat.enabled) Icons.Default.CheckCircle else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Category Visibility",
                            size = 32.dp,
                            iconSize = 16.dp,
                            tint = if (cat.enabled) EmeraldGlow else AmberGlow,
                            onClick = {
                                onSaveCategory(cat.copy(enabled = !cat.enabled), false)
                            }
                        )
                        // Edit
                        LiquidGlassCircularButton(
                            icon = Icons.Default.Edit,
                            contentDescription = "Edit Category",
                            size = 32.dp,
                            iconSize = 16.dp,
                            tint = ElectricCyan,
                            onClick = {
                                editingCategory = cat
                                name = cat.name
                                description = cat.description
                                thumbnailUrl = cat.thumbnailUrl
                                enabled = cat.enabled
                            }
                        )
                        // Delete
                        LiquidGlassCircularButton(
                            icon = Icons.Default.Delete,
                            contentDescription = "Delete Category",
                            size = 32.dp,
                            iconSize = 16.dp,
                            tint = RoseGlow,
                            onClick = { onDeleteCategory(cat.id, cat.name) }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}

// -----------------------------------------------------------------------------
// 4. ADMIN USER MANAGEMENT SCREEN
// -----------------------------------------------------------------------------

@Composable
fun AdminUserManagementScreen(
    users: List<UserProfileModel>,
    premiumPurchases: List<PremiumPurchaseModel> = emptyList(),
    onToggleUserStatus: (UserProfileModel) -> Unit,
    onToggleUserPremium: (UserProfileModel) -> Unit = {}
) {
    val glass = LocalLiquidGlassColors.current
    var userSearch by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Premium", "Free"

    val enrichedUsers = remember(users, premiumPurchases) {
        val activePurchaseByUid = premiumPurchases
            .filter { it.status != "revoked" }
            .associateBy { it.userId }
        val baseUsers = users.map { usr ->
            val uid = usr.userId.ifBlank { usr.id }
            val purchase = activePurchaseByUid[uid]
            if (purchase != null && (!usr.isPremium || usr.premiumUtr.isBlank())) {
                usr.copy(
                    isPremium = true,
                    premiumUtr = usr.premiumUtr.ifBlank { purchase.utrReference },
                    premiumPlan = usr.premiumPlan.ifBlank { purchase.planName },
                    premiumPurchasedAt = usr.premiumPurchasedAt ?: purchase.createdAt
                )
            } else {
                usr
            }
        }
        val existingUids = baseUsers.map { it.userId.ifBlank { it.id } }.toSet()
        val extraPremiumOnlyUsers = activePurchaseByUid.values
            .filter { it.userId !in existingUids }
            .map { p ->
                UserProfileModel(
                    id = p.userId,
                    userId = p.userId,
                    email = p.userEmail,
                    displayName = p.userDisplayName,
                    role = "user",
                    status = "active",
                    isPremium = true,
                    premiumUtr = p.utrReference,
                    premiumPlan = p.planName,
                    premiumPurchasedAt = p.createdAt
                )
            }
        (extraPremiumOnlyUsers + baseUsers).sortedByDescending { it.isPremium }
    }

    val premiumCount = remember(enrichedUsers) { enrichedUsers.count { it.isPremium } }
    val freeCount = remember(enrichedUsers, premiumCount) { (enrichedUsers.size - premiumCount).coerceAtLeast(0) }

    val filteredUsers = remember(enrichedUsers, userSearch, selectedFilter) {
        val byPlan = when (selectedFilter) {
            "Premium" -> enrichedUsers.filter { it.isPremium }
            "Free" -> enrichedUsers.filterNot { it.isPremium }
            else -> enrichedUsers
        }
        val q = userSearch.trim().lowercase(Locale.ROOT)
        if (q.isBlank()) {
            byPlan
        } else {
            byPlan.filter {
                it.displayName.lowercase(Locale.ROOT).contains(q) ||
                    it.email.lowercase(Locale.ROOT).contains(q) ||
                    it.userId.lowercase(Locale.ROOT).contains(q) ||
                    it.premiumUtr.lowercase(Locale.ROOT).contains(q)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_users_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Registered Users (${enrichedUsers.size}) • $premiumCount Premium",
                style = MaterialTheme.typography.headlineMedium,
                color = glass.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassTextField(
                value = userSearch,
                onValueChange = { userSearch = it },
                placeholder = "Search users by name, email, UID, or UTR...",
                leadingIcon = Icons.Default.Search,
                testTag = "admin_user_search_input"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassChip(
                    label = "All (${enrichedUsers.size})",
                    selected = selectedFilter == "All",
                    accentColor = ElectricCyan,
                    onClick = { selectedFilter = "All" },
                    testTag = "admin_users_filter_all"
                )
                LiquidGlassChip(
                    label = "👑 Premium ($premiumCount)",
                    selected = selectedFilter == "Premium",
                    accentColor = AmberGlow,
                    onClick = { selectedFilter = "Premium" },
                    testTag = "admin_users_filter_premium"
                )
                LiquidGlassChip(
                    label = "Free ($freeCount)",
                    selected = selectedFilter == "Free",
                    accentColor = NeonViolet,
                    onClick = { selectedFilter = "Free" },
                    testTag = "admin_users_filter_free"
                )
            }
        }

        items(filteredUsers, key = { it.id.ifBlank { it.userId } }) { usr ->
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                glowColor = if (usr.isPremium) AmberGlow else ElectricCyan,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = usr.displayName.ifBlank { "Pixora Member" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = glass.textPrimary
                                )
                                if (usr.isPremium) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(AmberGlow.copy(alpha = 0.25f))
                                            .border(1.dp, AmberGlow, CircleShape)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "👑 PREMIUM VIP",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = AmberGlow
                                        )
                                    }
                                }
                            }
                            Text(
                                text = usr.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = glass.glowCyan
                            )
                            Text(
                                text = "UID: ${usr.userId.ifBlank { usr.id }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = glass.textSecondary
                            )
                            if (usr.isPremium) {
                                Text(
                                    text = "${usr.premiumPlan.ifBlank { "₹299 VIP Creator Pass" }} • UTR: ${usr.premiumUtr.ifBlank { "Verified" }}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AmberGlow
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (usr.isDisabled) RoseGlow.copy(alpha = 0.22f)
                                    else EmeraldGlow.copy(alpha = 0.22f)
                                )
                                .border(
                                    1.dp,
                                    if (usr.isDisabled) RoseGlow else EmeraldGlow,
                                    CircleShape
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = usr.status.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Role: ${usr.role.uppercase()} • ${usr.downloadCount} DLs • ${usr.favoriteCount} Favs",
                            style = MaterialTheme.typography.labelSmall,
                            color = glass.textSecondary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LiquidGlassChip(
                                label = if (usr.isPremium) "Revoke VIP" else "Grant VIP",
                                selected = usr.isPremium,
                                accentColor = if (usr.isPremium) AmberGlow else EmeraldGlow,
                                leadingIcon = Icons.Default.WorkspacePremium,
                                onClick = { onToggleUserPremium(usr) }
                            )
                            if (!usr.email.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true)) {
                                LiquidGlassChip(
                                    label = if (usr.isDisabled) "Enable" else "Disable",
                                    selected = usr.isDisabled,
                                    accentColor = if (usr.isDisabled) EmeraldGlow else RoseGlow,
                                    leadingIcon = if (usr.isDisabled) Icons.Default.CheckCircle else Icons.Default.Block,
                                    onClick = { onToggleUserStatus(usr) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}

// -----------------------------------------------------------------------------
// 5. ADMIN HOMEPAGE MANAGEMENT & SYSTEM SETTINGS SCREEN
// -----------------------------------------------------------------------------

@Composable
fun AdminSettingsScreen(
    appSettings: AppSettingsModel,
    themePreference: String,
    dockBlurDp: Float = 60f,
    upperBlurStrength: Float = 48f,
    blurRadiusDp: Float = 6f,
    enable120HzSmoothness: Boolean = true,
    onUpdateDockBlur: (Float) -> Unit = {},
    onUpdateUpperBlurStrength: (Float) -> Unit = {},
    onUpdateBlurRadius: (Float) -> Unit = {},
    onUpdate120HzSmoothness: (Boolean) -> Unit = {},
    onSaveSettings: (AppSettingsModel) -> Unit,
    onUpdateThemePreference: (String) -> Unit,
    selectedAppDesign: AppDesignStyle = AppDesignCatalog.ios27Crystal,
    onSelectAppDesign: (AppDesignStyle) -> Unit = {},
    onLogout: () -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    val context = LocalContext.current

    var appName by remember(appSettings.appName) { mutableStateOf(appSettings.appName) }
    var heroTitle by remember(appSettings.heroBannerTitle) { mutableStateOf(appSettings.heroBannerTitle) }
    var heroSubtitle by remember(appSettings.heroBannerSubtitle) { mutableStateOf(appSettings.heroBannerSubtitle) }
    var heroImageUrl by remember(appSettings.heroBannerImageUrl) { mutableStateOf(appSettings.heroBannerImageUrl) }
    var announcementText by remember(appSettings.announcementText) { mutableStateOf(appSettings.announcementText) }
    var announcementEnabled by remember(appSettings.announcementEnabled) { mutableStateOf(appSettings.announcementEnabled) }
    var maintenanceMode by remember(appSettings.maintenanceMode) { mutableStateOf(appSettings.maintenanceMode) }
    var maxUploadMb by remember { mutableFloatStateOf(2048000f) }
    var showFeatured by remember(appSettings.showFeaturedSection) { mutableStateOf(appSettings.showFeaturedSection) }
    var showTrending by remember(appSettings.showTrendingSection) { mutableStateOf(appSettings.showTrendingSection) }
    var showCategories by remember(appSettings.showCategoriesSection) { mutableStateOf(appSettings.showCategoriesSection) }
    var showNewArrivals by remember(appSettings.showNewArrivalsSection) { mutableStateOf(appSettings.showNewArrivalsSection) }

    androidx.compose.runtime.CompositionLocalProvider(
        com.example.ui.components.LocalEnableLiquidStretch provides false
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_settings_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                elevated = true,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Homepage & Banner Management",
                        style = MaterialTheme.typography.titleLarge,
                        color = glass.textPrimary
                    )

                    LiquidGlassTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        placeholder = "App Name (Pixora)"
                    )

                    LiquidGlassTextField(
                        value = heroTitle,
                        onValueChange = { heroTitle = it },
                        placeholder = "Hero Banner Title"
                    )

                    LiquidGlassTextField(
                        value = heroSubtitle,
                        onValueChange = { heroSubtitle = it },
                        placeholder = "Hero Banner Subtitle"
                    )

                    LiquidGlassTextField(
                        value = heroImageUrl,
                        onValueChange = { heroImageUrl = it },
                        placeholder = "Hero Banner Image URL"
                    )

                    LiquidGlassTextField(
                        value = announcementText,
                        onValueChange = { announcementText = it },
                        placeholder = "Global Announcement Text"
                    )

                    LiquidGlassSwitch(
                        checked = announcementEnabled,
                        onCheckedChange = { announcementEnabled = it },
                        label = "Show Announcement Banner"
                    )

                    LiquidGlassSwitch(
                        checked = showFeatured,
                        onCheckedChange = { showFeatured = it },
                        label = "Show Featured Wallpapers Section"
                    )

                    LiquidGlassSwitch(
                        checked = showTrending,
                        onCheckedChange = { showTrending = it },
                        label = "Show Trending Section"
                    )

                    LiquidGlassSwitch(
                        checked = showCategories,
                        onCheckedChange = { showCategories = it },
                        label = "Show Categories Rail"
                    )

                    LiquidGlassSwitch(
                        checked = showNewArrivals,
                        onCheckedChange = { showNewArrivals = it },
                        label = "Show New Arrivals Section"
                    )
                }
            }
        }

        // Global User Layout Studio (10 New Modern Premium + Classic User Layouts)
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "✨ User Layout Studio (${AppDesignCatalog.allDesigns.size} Layouts)",
                        style = MaterialTheme.typography.titleLarge,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Pick any of the 10 New Modern Premium Layouts or Classic Layouts to immediately update User Home structure, columns, hero & glass theme.",
                        style = MaterialTheme.typography.labelSmall,
                        color = glass.textSecondary
                    )

                    AdminDesignStructureGrid(
                        selectedAppDesign = selectedAppDesign,
                        onSelectAppDesign = onSelectAppDesign
                    )
                }
            }
        }

        // System Controls: Dock Blur, Upper High-to-Low Blur, 120Hz Smoothness, Upload Limit, Maintenance Mode, Theme
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Material 3 Exclusive & 120Hz Display Engine",
                        style = MaterialTheme.typography.titleLarge,
                        color = glass.textPrimary
                    )

                    LiquidGlassSwitch(
                        checked = enable120HzSmoothness,
                        onCheckedChange = onUpdate120HzSmoothness,
                        label = "120Hz ProMotion Smoothness",
                        subtitle = "Ultra-low latency 120fps Material 3 Exclusive animations & touch responsiveness"
                    )

                    LiquidGlassSlider(
                        value = dockBlurDp.coerceIn(0f, 80f),
                        onValueChange = onUpdateDockBlur,
                        valueRange = 0f..80f,
                        steps = 0,
                        label = "Dock Frosted Blur Intensity",
                        valueText = "${dockBlurDp.toInt()} dp"
                    )

                    LiquidGlassSlider(
                        value = upperBlurStrength.coerceIn(0f, 64f),
                        onValueChange = onUpdateUpperBlurStrength,
                        valueRange = 0f..64f,
                        steps = 0,
                        label = "Upper High-to-Low Header Blur",
                        valueText = "${upperBlurStrength.toInt()} dp"
                    )

                    LiquidGlassSlider(
                        value = maxUploadMb.coerceIn(50f, 2048000f),
                        onValueChange = { maxUploadMb = it },
                        valueRange = 50f..2048000f,
                        steps = 0,
                        label = "Permanent 2 TB Wallpaper Storage & Cloud Quota",
                        valueText = when {
                            maxUploadMb >= 1024000f -> String.format(Locale.US, "%.1f TB (Permanent)", maxUploadMb / 1024000f)
                            maxUploadMb >= 1000f -> "${(maxUploadMb / 1000f).toInt()} GB"
                            else -> "${maxUploadMb.toInt()} MB"
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            250000f to "250 GB",
                            500000f to "500 GB",
                            1024000f to "1 TB",
                            2048000f to "2 TB Max"
                        ).forEach { (mbVal, label) ->
                            LiquidGlassChip(
                                label = label,
                                selected = kotlin.math.abs(maxUploadMb - mbVal) < 5000f,
                                accentColor = EmeraldGlow,
                                onClick = { maxUploadMb = mbVal },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    LiquidGlassSwitch(
                        checked = maintenanceMode,
                        onCheckedChange = { maintenanceMode = it },
                        label = "Maintenance Mode",
                        subtitle = "Displays a maintenance notice for non-admin users"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("dark" to "Dark iOS", "light" to "Light iOS", "system" to "Auto").forEach { (k, label) ->
                            LiquidGlassChip(
                                label = label,
                                selected = themePreference == k,
                                onClick = { onUpdateThemePreference(k) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    LiquidGlassButton(
                        text = "Save Global Settings & Sync to User Panel",
                        filledAccent = true,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "admin_save_settings_button",
                        onClick = {
                            onSaveSettings(
                                appSettings.copy(
                                    appName = appName,
                                    logoText = "",
                                    heroBannerTitle = heroTitle,
                                    heroBannerSubtitle = heroSubtitle,
                                    heroBannerImageUrl = heroImageUrl,
                                    announcementText = announcementText,
                                    announcementEnabled = announcementEnabled,
                                    maintenanceMode = maintenanceMode,
                                    maxUploadSizeMb = maxUploadMb.toLong().coerceIn(1L, 2048000L),
                                    showFeaturedSection = showFeatured,
                                    showTrendingSection = showTrending,
                                    showCategoriesSection = showCategories,
                                    showNewArrivalsSection = showNewArrivals
                                )
                            )
                        }
                    )
                }
            }
        }

        item {
            LiquidGlassButton(
                text = "Log Out of Administrator Session",
                icon = Icons.AutoMirrored.Filled.Logout,
                glowColor = RoseGlow,
                modifier = Modifier.fillMaxWidth(),
                testTag = "admin_logout_button",
                onClick = onLogout
            )
        }

        item { Spacer(modifier = Modifier.height(140.dp)) }
    }
    }
}
