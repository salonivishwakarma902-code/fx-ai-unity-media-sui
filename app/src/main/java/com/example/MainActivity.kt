package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import com.example.ui.components.WallxAsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.WallpaperItem
import com.example.data.repository.WallxRepository
import com.example.ui.auth.PixoraEmailSessionManager
import com.example.ui.auth.WallxAuthScreen
import com.example.ui.auth.signOutWallx
import com.example.ui.components.DockDestination
import com.example.ui.components.LiquidGlassAmbientBackground
import com.example.ui.components.LiquidGlassBackdropState
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassChip
import com.example.ui.components.LiquidGlassCircularButton
import com.example.ui.components.LiquidGlassNavigationDock
import com.example.ui.components.LocalLiquidGlassBackdrop
import com.example.ui.components.WallpersNavigationDrawerDialog
import com.example.ui.components.captureLiquidGlassBackdrop
import com.example.ui.components.elasticLiquidGlass
import com.example.ui.screens.AdminCategoryManagementScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminSettingsScreen
import com.example.ui.screens.AdminUserManagementScreen
import com.example.ui.screens.AdminWallpaperManagementScreen
import com.example.ui.screens.EarnMoneyAndShareStartupPopup
import com.example.ui.screens.ExploreSubScreenMode
import com.example.ui.screens.FourKWallpapersSplashScreen
import com.example.ui.screens.GoPremiumModalDialog
import com.example.ui.screens.UserAiStudioScreen
import com.example.ui.screens.UserExploreScreen
import com.example.ui.screens.UserFavoritesScreen
import com.example.ui.screens.UserHomeScreen
import com.example.ui.screens.UserLibraryAndDownloadsScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.UserUploadsManagementScreen
import com.example.ui.screens.WallpaperDetailModal
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RoseGlow
import com.example.ui.theme.WallxTheme
import com.example.ui.theme.parseHexColor
import com.example.ui.viewmodel.AdminDockTab
import com.example.ui.viewmodel.UserDockTab
import com.example.ui.viewmodel.WallxViewModel
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

import android.content.Intent
import android.os.Build
import com.example.ui.components.BottomHighToLowBlurOverlay
import com.example.ui.components.UpperHighToLowBlurOverlay
import com.example.ui.components.ios27LiquidStretch
import com.example.ui.components.progressiveTopHighToLowBlur

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        PixoraEmailSessionManager.initialize(this)
        enableEdgeToEdge()
        configureHighRefreshRate(enable120Hz = true)
        setContent {
            WallxRootApp(
                onToggle120HzWindow = { enabled ->
                    configureHighRefreshRate(enable120Hz = enabled)
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        configureAppCheck(this, intent)
    }

    private fun configureHighRefreshRate(enable120Hz: Boolean) {
        try {
            val params = window.attributes
            params.preferredRefreshRate = if (enable120Hz) 120f else 60f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val supportedModes = display?.supportedModes.orEmpty()
                val targetMode = if (enable120Hz) {
                    supportedModes.maxByOrNull { it.refreshRate }
                } else {
                    supportedModes.minByOrNull { kotlin.math.abs(it.refreshRate - 60f) }
                }
                if (targetMode != null) {
                    params.preferredDisplayModeId = targetMode.modeId
                }
            }
            window.attributes = params
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                window.frameRateBoostOnTouchEnabled = enable120Hz
            }
        } catch (_: Exception) {
            // Gracefully ignore on virtual displays that lock display modes
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun WallxRootApp(onToggle120HzWindow: (Boolean) -> Unit = {}) {
    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val emailSession by PixoraEmailSessionManager.activeSession.collectAsStateWithLifecycle()
    val isGuestMode by PixoraEmailSessionManager.isGuestMode.collectAsStateWithLifecycle()
    val activeUid = currentUser?.uid
        ?: emailSession?.uid
        ?: if (isGuestMode) PixoraEmailSessionManager.GUEST_USER_ID else null
    val context = LocalContext.current

    if (activeUid == null) {
        WallxTheme(themeMode = "dark") {
            WallxAuthScreen(
                onAuthSuccess = {
                    PixoraEmailSessionManager.exitGuestMode(context)
                },
                onSkipForNow = {
                    PixoraEmailSessionManager.enterGuestMode(context)
                }
            )
        }
    } else {
        // Feature ViewModel is instantiated ONLY after activeUid != null, keyed by activeUid
        val viewModel: WallxViewModel = viewModel(
            key = activeUid,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[APPLICATION_KEY]) {
                        "APPLICATION_KEY missing from CreationExtras"
                    }
                    val db = FirebaseFirestore.getInstance()
                    android.util.Log.d("WallxRootApp", "Initializing Firestore with default database")
                    WallxViewModel(
                        repository = WallxRepository(db = db, appContext = app.applicationContext),
                        currentUserId = activeUid,
                        appContext = app.applicationContext
                    )
                }
            }
        )
        AuthenticatedWallxApp(
            viewModel = viewModel,
            onToggle120HzWindow = onToggle120HzWindow
        )
    }
}

@Composable
private fun AuthenticatedWallxApp(
    viewModel: WallxViewModel,
    onToggle120HzWindow: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val emailSession by PixoraEmailSessionManager.activeSession.collectAsStateWithLifecycle()
    val authUser = remember { Firebase.auth.currentUser }
    val currentUserName = userProfile?.displayName
        ?: emailSession?.displayName
        ?: authUser?.displayName
        ?: emailSession?.email?.substringBefore("@")
        ?: authUser?.email?.substringBefore("@")
        ?: "Divyansh"
    val currentUserEmail = userProfile?.email
        ?: emailSession?.email
        ?: authUser?.email
        ?: "d99312907@gmail.com"
    val isAdminPanelActive by viewModel.isAdminPanelActive.collectAsStateWithLifecycle()
    val userTab by viewModel.userTab.collectAsStateWithLifecycle()
    val adminTab by viewModel.adminTab.collectAsStateWithLifecycle()
    val publishedWallpapers by viewModel.publishedWallpapers.collectAsStateWithLifecycle()
    val adminWallpapers by viewModel.adminAllWallpapers.collectAsStateWithLifecycle()
    val enabledCategories by viewModel.enabledCategories.collectAsStateWithLifecycle()
    val adminCategories by viewModel.adminAllCategories.collectAsStateWithLifecycle()
    val userFavorites by viewModel.userFavorites.collectAsStateWithLifecycle()
    val userDownloads by viewModel.userDownloads.collectAsStateWithLifecycle()
    val adminUsers by viewModel.adminAllUsers.collectAsStateWithLifecycle()
    val adminPremiumPurchases by viewModel.adminAllPremiumPurchases.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val selectedWallpaper by viewModel.selectedWallpaper.collectAsStateWithLifecycle()
    val touchedBgWallpaper by viewModel.touchedBackgroundWallpaper.collectAsStateWithLifecycle()
    val activeDownload by viewModel.activeDownload.collectAsStateWithLifecycle()
    val uploadProgress by viewModel.uploadProgress.collectAsStateWithLifecycle()
    val bannerNotification by viewModel.bannerNotification.collectAsStateWithLifecycle()
    val errorNotification by viewModel.errorNotification.collectAsStateWithLifecycle()
    val optics by viewModel.glassOptics.collectAsStateWithLifecycle()
    val themeMode by viewModel.activeThemeMode.collectAsStateWithLifecycle()
    val selectedAppDesign by viewModel.selectedAppDesign.collectAsStateWithLifecycle()
    val ai8KGenState by viewModel.ai8KState.collectAsStateWithLifecycle()
    val aiGeneratedHistory by viewModel.aiGeneratedHistory.collectAsStateWithLifecycle()
    val isPremiumUser by viewModel.isPremiumUser.collectAsStateWithLifecycle()
    val customQrCodeUri by viewModel.customQrCodeImageUri.collectAsStateWithLifecycle()
    val recentlyAddedWallpapers by viewModel.recentlyAddedWallpapers.collectAsStateWithLifecycle()
    val userUploadedWallpapers by viewModel.userUploadedWallpapers.collectAsStateWithLifecycle()
    val cachedFavoriteWallpapers by viewModel.cachedFavoriteWallpapers.collectAsStateWithLifecycle()
    val isCatalogLoading by viewModel.isCatalogLoading.collectAsStateWithLifecycle()
    val catalogSyncError by viewModel.catalogSyncError.collectAsStateWithLifecycle()
    val pendingCloudSyncCount by viewModel.pendingCloudSyncCount.collectAsStateWithLifecycle()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.onAppResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val favoriteIds = remember(userFavorites, cachedFavoriteWallpapers) {
        (userFavorites.map { it.wallpaperId } + cachedFavoriteWallpapers.map { it.id }).toSet()
    }
    val canAccessAdmin = viewModel.isAuthorizedAdmin || userProfile?.isAdminRole == true

    // Auto-dismiss transient toast notifications after 4 seconds
    LaunchedEffect(bannerNotification, errorNotification) {
        if (bannerNotification != null || errorNotification != null) {
            delay(4000)
            viewModel.clearNotifications()
        }
    }

    // BackHandler for sub-tabs returning to Home / Dashboard
    BackHandler(
        enabled = selectedWallpaper == null && (
            (!isAdminPanelActive && userTab != UserDockTab.HOME) ||
                (isAdminPanelActive && adminTab != AdminDockTab.DASHBOARD)
            )
    ) {
        if (isAdminPanelActive) {
            viewModel.selectAdminTab(AdminDockTab.DASHBOARD)
        } else {
            if (userTab == UserDockTab.SEARCH || userTab == UserDockTab.FAVORITES || userTab == UserDockTab.PREMIUM || userTab == UserDockTab.PROFILE) {
                viewModel.selectUserTab(UserDockTab.HOME)
            } else {
                viewModel.selectUserTab(UserDockTab.HOME)
            }
        }
    }

    var showWallpersDrawerDialog by remember { mutableStateOf(false) }
    var showAiStudioOverlay by remember { mutableStateOf(false) }
    var showInitialSplash by remember { mutableStateOf(true) }
    var showStartupEarnMoneyPopup by remember { mutableStateOf(false) }
    var showStartupQrUpgradeModal by remember { mutableStateOf(false) }
    var exploreSubModeRequest by remember { mutableStateOf(ExploreSubScreenMode.CATEGORIES_GRID) }
    var exploreRequestVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(500)
        showInitialSplash = false
        delay(120)
        if (!isPremiumUser) {
            showStartupEarnMoneyPopup = true
        }
    }

    val userDockItems = remember(isPremiumUser) {
        listOf(
            DockDestination(UserDockTab.HOME, "Home", Icons.Default.Home, "dock_user_home"),
            DockDestination(UserDockTab.SEARCH, "Search", Icons.Default.Search, "dock_user_search"),
            DockDestination(UserDockTab.FAVORITES, "Save", Icons.Default.Bookmark, "dock_user_favorites"),
            if (isPremiumUser) {
                DockDestination(UserDockTab.PREMIUM, "Earn", Icons.Default.Paid, "dock_user_earn")
            } else {
                DockDestination(UserDockTab.PREMIUM, "Premium", Icons.Default.WorkspacePremium, "dock_user_premium")
            },
            DockDestination(UserDockTab.PROFILE, "Profile", Icons.Default.Person, "dock_user_profile")
        )
    }

    val adminDockItems = remember {
        listOf(
            DockDestination(AdminDockTab.DASHBOARD, "Dashboard", Icons.Default.Dashboard, "dock_admin_dashboard"),
            DockDestination(AdminDockTab.WALLPAPERS, "Wallpapers", Icons.Default.Wallpaper, "dock_admin_wallpapers"),
            DockDestination(AdminDockTab.CATEGORIES, "Categories", Icons.Default.Category, "dock_admin_categories"),
            DockDestination(AdminDockTab.USERS, "Users", Icons.Default.People, "dock_admin_users"),
            DockDestination(AdminDockTab.SETTINGS, "Settings", Icons.Default.Tune, "dock_admin_settings")
        )
    }

    WallxTheme(
        themeMode = themeMode,
        optics = optics,
        appDesignStyle = selectedAppDesign
    ) {
        val glass = LocalLiquidGlassColors.current
        val sharpBackdropLayer = rememberGraphicsLayer()
        val blurredBackdropLayer = rememberGraphicsLayer()
        val backdropState = remember(sharpBackdropLayer, blurredBackdropLayer) {
            LiquidGlassBackdropState(
                sharpGraphicsLayer = sharpBackdropLayer,
                blurredGraphicsLayer = blurredBackdropLayer
            )
        }

        LaunchedEffect(optics.enable120HzSmoothness) {
            onToggle120HzWindow(optics.enable120HzSmoothness)
        }

        CompositionLocalProvider(LocalLiquidGlassBackdrop provides backdropState) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Capture Ambient Background + Main Screen Content into sharpBackdropLayer
                // with LocalLiquidGlassBackdrop = null inside so no child can ever create a RenderNode cycle.
                CompositionLocalProvider(LocalLiquidGlassBackdrop provides null) {
                    LiquidGlassAmbientBackground(
                        backgroundWallpaperUrl = touchedBgWallpaper?.let { it.imageUrl.ifBlank { it.thumbnailUrl } },
                        backgroundFallbackUrl = touchedBgWallpaper?.thumbnailUrl,
                        modifier = Modifier
                            .fillMaxSize()
                            .captureLiquidGlassBackdrop(
                                backdropState = backdropState,
                                onRootOffsetChanged = { backdropState.rootOffset = it }
                            )
                    ) {
                        // 1. FULL-BLEED MAIN CONTENT AREA
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .progressiveTopHighToLowBlur(
                                    height = 0.dp,
                                    includeDockBlur = true
                                )
                        ) {
                    if (appSettings.maintenanceMode && !canAccessAdmin) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LiquidGlassCard(
                                cornerRadius = 28.dp,
                                glowColor = AmberGlow,
                                contentPadding = PaddingValues(24.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Scheduled System Maintenance",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = glass.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Pixora is currently undergoing scheduled maintenance by the administrator. Please check back shortly.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = glass.textSecondary
                                    )
                                }
                            }
                        }
                    } else if (isAdminPanelActive && canAccessAdmin) {
                        Box(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(top = 64.dp)
                        ) {
                            AnimatedContent(
                                targetState = adminTab,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                                        scaleIn(
                                            initialScale = 0.96f,
                                            animationSpec = spring(
                                                dampingRatio = 0.78f,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )) togetherWith
                                        (fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
                                            scaleOut(
                                                targetScale = 0.98f,
                                                animationSpec = tween(160, easing = FastOutSlowInEasing)
                                            ))
                                },
                                label = "adminLiquidTabTransition"
                            ) { targetAdminTab ->
                            when (targetAdminTab) {
                                AdminDockTab.DASHBOARD -> AdminDashboardScreen(
                                    users = adminUsers,
                                    wallpapers = adminWallpapers,
                                    categories = adminCategories,
                                    premiumPurchases = adminPremiumPurchases,
                                    selectedAppDesign = selectedAppDesign,
                                    publishedCount = publishedWallpapers.size,
                                    pendingSyncCount = pendingCloudSyncCount,
                                    onSelectAppDesign = viewModel::selectAppDesign,
                                    onSeedCloudCatalog = viewModel::syncPendingChangesToCloudNow,
                                    onSwitchToUserPanel = { viewModel.setAdminPanelMode(false) },
                                    onSelectWallpaper = viewModel::openWallpaperDetails,
                                    onDeleteWallpaper = viewModel::deleteWallpaperAdmin,
                                    onUpdatePremiumPurchaseStatus = viewModel::updatePremiumPurchaseStatusAdmin
                                )

                                AdminDockTab.WALLPAPERS -> AdminWallpaperManagementScreen(
                                    wallpapers = adminWallpapers,
                                    categories = adminCategories,
                                    uploadProgress = uploadProgress,
                                    currentUserId = viewModel.currentUserId,
                                    publishedCount = publishedWallpapers.size,
                                    pendingSyncCount = pendingCloudSyncCount,
                                    onSyncCloudCatalog = viewModel::syncPendingChangesToCloudNow,
                                    onOpenUserPanel = { viewModel.setAdminPanelMode(false) },
                                    onUploadMediaWallpapers = { uris, title, desc, cat, tags, status, feat, trend ->
                                        viewModel.uploadMultipleWallpapersAdmin(
                                            context, uris, title, desc, cat, tags, status, feat, trend
                                        )
                                    },
                                    onCreateOrUpdateWallpaper = viewModel::createOrUpdateWallpaperDirectAdmin,
                                    onDeleteWallpaper = viewModel::deleteWallpaperAdmin,
                                    onRestoreDeletedWallpapers = viewModel::restoreAllDeletedWallpapersAdmin
                                )

                                AdminDockTab.CATEGORIES -> AdminCategoryManagementScreen(
                                    categories = adminCategories,
                                    onSaveCategory = viewModel::saveCategoryAdmin,
                                    onDeleteCategory = viewModel::deleteCategoryAdmin
                                )

                                AdminDockTab.USERS -> AdminUserManagementScreen(
                                    users = adminUsers,
                                    premiumPurchases = adminPremiumPurchases,
                                    onToggleUserStatus = viewModel::toggleUserDisabledAdmin,
                                    onToggleUserPremium = viewModel::toggleUserPremiumAdmin
                                )

                                AdminDockTab.SETTINGS -> AdminSettingsScreen(
                                    appSettings = appSettings,
                                    themePreference = themeMode,
                                    dockBlurDp = optics.dockBlurDp,
                                    upperBlurStrength = optics.upperBlurStrength,
                                    blurRadiusDp = optics.blurRadiusDp,
                                    enable120HzSmoothness = optics.enable120HzSmoothness,
                                    onUpdateDockBlur = viewModel::updateDockBlur,
                                    onUpdateUpperBlurStrength = viewModel::updateUpperBlurStrength,
                                    onUpdateBlurRadius = viewModel::updateBlurRadius,
                                    onUpdate120HzSmoothness = viewModel::update120HzSmoothness,
                                    onSaveSettings = viewModel::saveAppSettingsAdmin,
                                    onUpdateThemePreference = viewModel::updateThemePreference,
                                    selectedAppDesign = selectedAppDesign,
                                    onSelectAppDesign = viewModel::selectAppDesign,
                                    onLogout = {
                                        signOutWallx(
                                            credentialManager = credentialManager,
                                            onSignOutComplete = {},
                                            scope = scope,
                                            context = context
                                        )
                                    }
                                )
                            }
                            }
                        }
                    } else {
                        val tabOrder = remember {
                            listOf(
                                UserDockTab.HOME,
                                UserDockTab.SEARCH,
                                UserDockTab.FAVORITES,
                                UserDockTab.PREMIUM,
                                UserDockTab.PROFILE,
                                UserDockTab.AI_STUDIO,
                                UserDockTab.EXPLORE
                            )
                        }
                        AnimatedContent(
                            targetState = userTab,
                            transitionSpec = {
                                val initialIdx = tabOrder.indexOf(initialState).coerceAtLeast(0)
                                val targetIdx = tabOrder.indexOf(targetState).coerceAtLeast(0)
                                val direction = if (targetIdx >= initialIdx) 1 else -1
                                (slideInHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = 0.80f,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    initialOffsetX = { fullWidth -> (fullWidth * 0.09f * direction).toInt() }
                                ) + fadeIn(
                                    animationSpec = tween(210, easing = FastOutSlowInEasing)
                                ) + scaleIn(
                                    initialScale = 0.965f,
                                    animationSpec = spring(
                                        dampingRatio = 0.78f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )) togetherWith (slideOutHorizontally(
                                    animationSpec = tween(170, easing = FastOutSlowInEasing),
                                    targetOffsetX = { fullWidth -> (-fullWidth * 0.06f * direction).toInt() }
                                ) + fadeOut(
                                    animationSpec = tween(150, easing = FastOutSlowInEasing)
                                ) + scaleOut(
                                    targetScale = 0.98f,
                                    animationSpec = tween(160, easing = FastOutSlowInEasing)
                                ))
                            },
                            label = "userLiquidTabTransition"
                        ) { targetTab ->
                        when (targetTab) {
                            UserDockTab.HOME -> UserHomeScreen(
                                wallpapers = publishedWallpapers,
                                categories = enabledCategories,
                                selectedCategory = selectedCategory,
                                favoriteIds = favoriteIds,
                                appSettings = appSettings,
                                searchQuery = searchQuery,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onSelectCategory = viewModel::selectCategory,
                                onSelectWallpaper = viewModel::openWallpaperDetails,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onQuickSearchNavigate = {
                                    exploreSubModeRequest = ExploreSubScreenMode.SEARCH_AND_FILTERS
                                    exploreRequestVersion++
                                    viewModel.selectUserTab(UserDockTab.SEARCH)
                                },
                                onOpenMenuDrawer = { showWallpersDrawerDialog = true },
                                onOpenProfile = { viewModel.selectUserTab(UserDockTab.PROFILE) },
                                onOpenRingtonesStudio = { viewModel.selectUserTab(UserDockTab.AI_STUDIO) },
                                onOpenArtworksScreen = { _ -> viewModel.selectUserTab(UserDockTab.FAVORITES) },
                                onNotifyRingtone = viewModel::showInfoNotification,
                                recentlyAddedWallpapers = recentlyAddedWallpapers,
                                userUploadedWallpapers = userUploadedWallpapers,
                                isPremiumUser = isPremiumUser,
                                customQrCodeUri = customQrCodeUri,
                                onUnlockPremiumViaQr = viewModel::unlockPremiumViaQrCode,
                                onUpdateCustomQrCode = { uri -> viewModel.updateCustomQrCodeImage(context, uri) },
                                onUploadPremiumWallpaper = { uri, title, cat, desc ->
                                    viewModel.uploadPremiumUserWallpaper(context, uri, title, cat, desc)
                                },
                                isAuthorizedAdmin = canAccessAdmin,
                                onOpenAdminPanel = { viewModel.setAdminPanelMode(true, AdminDockTab.WALLPAPERS) },
                                isCatalogLoading = isCatalogLoading,
                                catalogSyncError = catalogSyncError,
                                onRefreshCatalog = viewModel::refreshWallpapersNow,
                                 userName = currentUserName,
                                 userEmail = currentUserEmail
                            )

                            UserDockTab.SEARCH -> UserExploreScreen(
                                wallpapers = publishedWallpapers,
                                categories = enabledCategories,
                                searchQuery = searchQuery,
                                searchHistory = searchHistory,
                                favoriteIds = favoriteIds,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onCommitSearch = viewModel::commitSearchTerm,
                                onClearHistory = viewModel::clearSearchHistory,
                                onSelectWallpaper = viewModel::openWallpaperDetails,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onCreateCategory = { newCat ->
                                    viewModel.saveCategoryAdmin(newCat, true)
                                },
                                onOpenMenuDrawer = { showWallpersDrawerDialog = true },
                                initialSubMode = ExploreSubScreenMode.SEARCH_AND_FILTERS,
                                modeRequestKey = exploreRequestVersion
                            )

                            UserDockTab.EXPLORE -> UserExploreScreen(
                                wallpapers = publishedWallpapers,
                                categories = enabledCategories,
                                searchQuery = searchQuery,
                                searchHistory = searchHistory,
                                favoriteIds = favoriteIds,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onCommitSearch = viewModel::commitSearchTerm,
                                onClearHistory = viewModel::clearSearchHistory,
                                onSelectWallpaper = viewModel::openWallpaperDetails,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onCreateCategory = { newCat ->
                                    viewModel.saveCategoryAdmin(newCat, true)
                                },
                                onOpenMenuDrawer = { showWallpersDrawerDialog = true },
                                initialSubMode = exploreSubModeRequest,
                                modeRequestKey = exploreRequestVersion
                            )

                            UserDockTab.AI_STUDIO -> {
                                if (showAiStudioOverlay) {
                                    BackHandler { showAiStudioOverlay = false }
                                    UserAiStudioScreen(
                                        aiState = ai8KGenState,
                                        aiGeneratedHistory = aiGeneratedHistory,
                                        favoriteIds = favoriteIds,
                                        onEnhancePrompt = viewModel::enhancePromptFor8KWallpaper,
                                        onGenerate8KWallpaper = viewModel::generate8KWallpaperImage,
                                        onEditImageWithAi = viewModel::editImageWithPrompt,
                                        onResetAiState = viewModel::resetAi8KState,
                                        onSelectWallpaper = viewModel::openWallpaperDetails,
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onNotifySuccess = viewModel::showInfoNotification,
                                        onNotifyError = viewModel::showErrorNotification
                                    )
                                } else {
                                    UserLibraryAndDownloadsScreen(
                                        favorites = userFavorites,
                                        downloads = userDownloads,
                                        allWallpapers = publishedWallpapers,
                                        uploadedWallpapers = userUploadedWallpapers,
                                        availableCategories = enabledCategories.map { it.name },
                                        isPremiumUser = isPremiumUser,
                                        onOpenPremiumModal = { showStartupEarnMoneyPopup = true },
                                        favoriteIds = favoriteIds,
                                        onSelectWallpaper = viewModel::openWallpaperDetails,
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onUploadWallpaper = { uri, title, cat, desc ->
                                            viewModel.uploadPremiumUserWallpaper(context, uri, title, cat, desc)
                                        },
                                        onOpenAiStudioModal = { showAiStudioOverlay = true },
                                        onOpenSettings = { viewModel.selectUserTab(UserDockTab.PROFILE) }
                                    )
                                }
                            }

                            UserDockTab.FAVORITES -> UserFavoritesScreen(
                                favorites = userFavorites,
                                downloads = userDownloads,
                                allWallpapers = publishedWallpapers,
                                cachedFavoriteWallpapers = cachedFavoriteWallpapers,
                                onSelectWallpaper = viewModel::openWallpaperDetails,
                                onRemoveFavorite = { wallId ->
                                    (cachedFavoriteWallpapers + publishedWallpapers)
                                        .find { it.id == wallId }
                                        ?.let(viewModel::toggleFavorite)
                                },
                                onToggleFavoriteItem = viewModel::toggleFavorite,
                                onBackToHome = { viewModel.selectUserTab(UserDockTab.HOME) }
                            )

                            UserDockTab.PREMIUM -> {
                                if (!isPremiumUser) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        GoPremiumModalDialog(
                                            isPremiumUser = false,
                                            customQrCodeUri = customQrCodeUri,
                                            startWithQrStep = true,
                                            onUpdateCustomQrCode = { uri -> viewModel.updateCustomQrCodeImage(context, uri) },
                                            onUpgradeClick = { utr ->
                                                viewModel.unlockPremiumViaQrCode(utr)
                                            },
                                            onDismiss = { viewModel.selectUserTab(UserDockTab.HOME) }
                                        )
                                    }
                                } else {
                                    UserUploadsManagementScreen(
                                        userName = userProfile?.displayName?.ifBlank { "Creator" } ?: "Creator",
                                        uploadedWallpapers = userUploadedWallpapers,
                                        totalDownloadsCount = userDownloads.size,
                                        totalFavoritesCount = userFavorites.size,
                                        isUploading = uploadProgress != null,
                                        availableCategories = enabledCategories.map { it.name },
                                        onUploadWallpaper = { uri, title, cat, desc ->
                                            viewModel.uploadPremiumUserWallpaper(context, uri, title, cat, desc)
                                        },
                                        onRequestPayout = { upiId, amountStr ->
                                            viewModel.showInfoNotification("Payout request of $amountStr submitted to UPI ID: $upiId!")
                                        },
                                        onSelectWallpaper = viewModel::openWallpaperDetails,
                                        onBackToHome = { viewModel.selectUserTab(UserDockTab.HOME) }
                                    )
                                }
                            }

                            UserDockTab.PROFILE -> UserProfileScreen(
                                userProfile = userProfile,
                                currentUserId = viewModel.currentUserId,
                                favoritesCount = userFavorites.size,
                                downloadsCount = userDownloads.size,
                                themePreference = themeMode,
                                optics = optics,
                                isAuthorizedAdmin = canAccessAdmin,
                                onUpdateThemePreference = viewModel::updateThemePreference,
                                onUpdateBlurRadius = viewModel::updateBlurRadius,
                                onUpdateDockBlur = viewModel::updateDockBlur,
                                onUpdateUpperBlurStrength = viewModel::updateUpperBlurStrength,
                                onUpdate120HzSmoothness = viewModel::update120HzSmoothness,
                                onUpdateGlassOpacity = viewModel::updateGlassOpacity,
                                onUpdateGlowIntensity = viewModel::updateGlowIntensity,
                                onUpdateSpecularShine = viewModel::updateSpecularShine,
                                onUpdateLiquidStretch = viewModel::updateLiquidStretch,
                                onUpdateAmbientOrbsMotion = viewModel::updateAmbientOrbsMotion,
                                onUpdateRefractionStrength = viewModel::updateRefractionStrength,
                                onUpdateRefractionDepth = viewModel::updateRefractionDepth,
                                onUpdateChromaticSplit = viewModel::updateChromaticSplit,
                                onUpdateTintHex = viewModel::updateTintHex,
                                onApplyPreset = viewModel::applyOpticsPreset,
                                onResetOptics = viewModel::resetOpticsToDefault,
                                onOpenAdminPanel = { viewModel.setAdminPanelMode(true) },
                                onLogout = {
                                    signOutWallx(
                                        credentialManager = credentialManager,
                                        onSignOutComplete = {},
                                        scope = scope,
                                        context = context
                                    )
                                },
                                onNavigateBack = { viewModel.selectUserTab(UserDockTab.HOME) },
                                onOpenAiStudio = {
                                    showAiStudioOverlay = true
                                    viewModel.selectUserTab(UserDockTab.AI_STUDIO)
                                },
                                onNotifyMessage = viewModel::showInfoNotification,
                                onDeleteAccountAndData = {
                                    viewModel.deleteUserAccountAndData {
                                        signOutWallx(
                                            credentialManager = credentialManager,
                                            onSignOutComplete = {},
                                            scope = scope,
                                            context = context
                                        )
                                    }
                                },
                                onUpdateEmailProfile = { newName, newEmail ->
                                    viewModel.updateUserProfileAndEmail(context, newName, newEmail)
                                },
                                selectedAppDesign = selectedAppDesign,
                                onSelectAppDesign = viewModel::selectAppDesign,
                                onOpenStorageDownloadsManager = {
                                    showAiStudioOverlay = false
                                    viewModel.selectUserTab(UserDockTab.AI_STUDIO)
                                },
                                isPremiumUser = isPremiumUser,
                                customQrCodeUri = customQrCodeUri,
                                onUnlockPremiumViaQr = viewModel::unlockPremiumViaQrCode,
                                onUpdateCustomQrCode = { uri -> viewModel.updateCustomQrCodeImage(context, uri) },
                                onUploadPremiumWallpaper = { uri, title, cat, desc ->
                                    viewModel.uploadPremiumUserWallpaper(context, uri, title, cat, desc)
                                },
                                onOpenEarnMoneyPopup = { showStartupEarnMoneyPopup = true },
                                userUploadedWallpapers = userUploadedWallpapers
                            )
                        }
                        }
                    }
                    }
                }

                // 2. UPPER HIGH-TO-LOW LIQUID GLASS BLUR OVERLAY + FLOATING TOP HEADER BAR (Admin & AI Studio)
                if (isAdminPanelActive || (userTab == UserDockTab.AI_STUDIO && showAiStudioOverlay)) {
                    UpperHighToLowBlurOverlay(
                        modifier = Modifier.align(Alignment.TopCenter),
                        height = 0.dp
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    if (isAdminPanelActive || (userTab == UserDockTab.AI_STUDIO && showAiStudioOverlay)) {
                        val headerTitle = when {
                            isAdminPanelActive -> "Pixora Admin"
                            else -> "8K AI Studio"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = glass.textPrimary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (!isAdminPanelActive && userTab != UserDockTab.AI_STUDIO) {
                                    Box(
                                        modifier = Modifier
                                            .elasticLiquidGlass(
                                                cornerRadius = 999.dp,
                                                glowColor = ElectricCyan,
                                                secondaryGlowColor = NeonViolet,
                                                elevated = true
                                            )
                                            .clickable {
                                                viewModel.selectUserTab(UserDockTab.AI_STUDIO)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("header_ai_8k_button")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "8K AI Creator",
                                                tint = ElectricCyan,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "8K AI",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = glass.textPrimary,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }

                                if (canAccessAdmin) {
                                    Box(
                                        modifier = Modifier
                                            .elasticLiquidGlass(
                                                cornerRadius = 999.dp,
                                                glowColor = if (isAdminPanelActive) AmberGlow else ElectricCyan,
                                                elevated = true
                                            )
                                            .clickable {
                                                viewModel.setAdminPanelMode(!isAdminPanelActive)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("header_toggle_admin_mode_button")
                                    ) {
                                        Text(
                                            text = if (isAdminPanelActive) "Admin" else "User",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = glass.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Circular Frosted Liquid Glass Search Button
                                LiquidGlassCircularButton(
                                    icon = Icons.Default.Search,
                                    contentDescription = "Search Wallpapers",
                                    size = 44.dp,
                                    iconSize = 20.dp,
                                    tint = glass.textPrimary,
                                    onClick = {
                                        if (isAdminPanelActive) {
                                            viewModel.selectAdminTab(AdminDockTab.WALLPAPERS)
                                        } else {
                                            showAiStudioOverlay = false
                                            exploreSubModeRequest = ExploreSubScreenMode.SEARCH_AND_FILTERS
                                            exploreRequestVersion++
                                            viewModel.selectUserTab(UserDockTab.EXPLORE)
                                        }
                                    },
                                    testTag = "header_search_button"
                                )
                            }
                        }
                    }

                    // Floating Notification Banner with Smooth Material 3 Transition
                    AnimatedVisibility(
                        visible = bannerNotification != null || errorNotification != null,
                        enter = fadeIn(tween(180, easing = FastOutSlowInEasing)) +
                            slideInVertically(tween(220, easing = FastOutSlowInEasing)) { -it / 2 },
                        exit = fadeOut(tween(150, easing = FastOutSlowInEasing)) +
                            slideOutVertically(tween(180, easing = FastOutSlowInEasing)) { -it / 2 }
                    ) {
                        val isErr = errorNotification != null
                        val textMsg = errorNotification ?: bannerNotification ?: ""
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(CircleShape)
                                .background(if (isErr) Color(0xFFDC2626) else Color(0xFF059669))
                                .clickable { viewModel.clearNotifications() }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = textMsg,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss notification",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 3. BOTTOM HIGH-TO-LOW LIQUID GLASS BLUR OVERLAY (Behind the Dock)
                BottomHighToLowBlurOverlay(
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // 4. IPHONE-STYLE FLOATING FROSTED CAPSULE DOCK AT BOTTOM
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isAdminPanelActive && canAccessAdmin) {
                        LiquidGlassNavigationDock(
                            items = adminDockItems,
                            selectedRoute = adminTab,
                            onSelect = viewModel::selectAdminTab,
                            accentColor = Color(0xFF0F172A),
                            showDetachedSearchButton = false
                        )
                    } else {
                        LiquidGlassNavigationDock(
                            items = userDockItems,
                            selectedRoute = userTab,
                            onSelect = { tab ->
                                showAiStudioOverlay = false
                                if (tab == UserDockTab.EXPLORE) {
                                    exploreSubModeRequest = ExploreSubScreenMode.CATEGORIES_GRID
                                    exploreRequestVersion++
                                }
                                viewModel.selectUserTab(tab)
                            },
                            accentColor = Color(0xFF0F172A),
                            showDetachedSearchButton = false,
                            onSearchClick = null
                        )
                    }
                }
            }

            if (showWallpersDrawerDialog) {
                WallpersNavigationDrawerDialog(
                    canAccessAdmin = canAccessAdmin,
                    userName = currentUserName,
                    userEmail = currentUserEmail,
                    onSelectHome = { viewModel.selectUserTab(UserDockTab.HOME) },
                    onSelectCategories = { viewModel.selectUserTab(UserDockTab.EXPLORE) },
                    onSelectTrending = { viewModel.selectUserTab(UserDockTab.EXPLORE) },
                    onSelectPopular = { viewModel.selectUserTab(UserDockTab.EXPLORE) },
                    onSelectSpotlight = { viewModel.selectUserTab(UserDockTab.AI_STUDIO) },
                    onSelectFavorites = { viewModel.selectUserTab(UserDockTab.FAVORITES) },
                    onSelectDownloads = { viewModel.selectUserTab(UserDockTab.FAVORITES) },
                    onSelectNotifications = { viewModel.showInfoNotification("No new notifications") },
                    onSelectProfile = { viewModel.selectUserTab(UserDockTab.PROFILE) },
                    onSelectSettings = { viewModel.selectUserTab(UserDockTab.PROFILE) },
                    onOpenAdminPanel = { viewModel.setAdminPanelMode(true, AdminDockTab.WALLPAPERS) },
                    onSignOut = {
                        PixoraEmailSessionManager.clearSession(context)
                        Firebase.auth.signOut()
                    },
                    onDismiss = { showWallpersDrawerDialog = false }
                )
            }

            // Fullscreen Connected Wallpaper Detail Modal with Pop-In / Pop-Out Open-Close Animation
            var lastSelectedWallpaper by remember { mutableStateOf<WallpaperItem?>(null) }
            if (selectedWallpaper != null) {
                lastSelectedWallpaper = selectedWallpaper
            }
            AnimatedVisibility(
                visible = selectedWallpaper != null,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing)
                ) + scaleIn(
                    initialScale = 0.72f,
                    animationSpec = spring(
                        dampingRatio = 0.68f,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
                ) + scaleOut(
                    targetScale = 0.72f,
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                )
            ) {
                val detailItem = selectedWallpaper ?: lastSelectedWallpaper
                if (detailItem != null) {
                    val combinedCatalog = remember(publishedWallpapers, recentlyAddedWallpapers) {
                        (recentlyAddedWallpapers + publishedWallpapers).distinctBy { it.id }
                    }
                    val related = remember(detailItem, combinedCatalog) {
                        val sameCategory = combinedCatalog.filter {
                            it.id != detailItem.id && it.category.equals(detailItem.category, ignoreCase = true)
                        }
                        if (sameCategory.size >= 3) {
                            sameCategory.take(6)
                        } else {
                            (sameCategory + combinedCatalog.filter { it.id != detailItem.id })
                                .distinctBy { it.id }
                                .take(6)
                        }
                    }
                    WallpaperDetailModal(
                        wallpaper = detailItem,
                        isFavorited = detailItem.id in favoriteIds,
                        relatedWallpapers = related,
                        recentlyAddedWallpapers = recentlyAddedWallpapers,
                        favoriteIds = favoriteIds,
                        activeDownload = activeDownload,
                        onToggleFavorite = { viewModel.toggleFavorite(detailItem) },
                        onToggleRelatedFavorite = viewModel::toggleFavorite,
                        onDownloadProgress = viewModel::setDownloadProgress,
                        onRecordDownload = viewModel::recordWallpaperDownload,
                        onSelectRelated = viewModel::openWallpaperDetails,
                        onNotifySuccess = viewModel::showInfoNotification,
                        onNotifyError = viewModel::showErrorNotification,
                        onSyncOpticsToApp = { blurDp, upperBlurDp, glassOpacity, specularShine ->
                            viewModel.updateBlurRadius(blurDp)
                            viewModel.updateDockBlur(blurDp)
                            viewModel.updateUpperBlurStrength(upperBlurDp)
                            viewModel.updateGlassOpacity(glassOpacity)
                            viewModel.updateSpecularShine(specularShine)
                        },
                        onDismiss = viewModel::closeWallpaperDetails
                    )
                }
            }

            // Pop Up Window: "Earn Money by Uploading Wallpapers 💸" (hidden once Premium is unlocked)
            if (!showInitialSplash && showStartupEarnMoneyPopup && !isPremiumUser) {
                EarnMoneyAndShareStartupPopup(
                    isPremiumUser = false,
                    onUploadOrUpgradeClick = {
                        showStartupEarnMoneyPopup = false
                        showStartupQrUpgradeModal = true
                    },
                    onShareAppClick = {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Pixora 4K & 8K Wallpapers • Earn Money 💸")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "💸 Earn money by uploading 4K wallpapers on Pixora! Get ₹25 per upload and ₹2.50 per download directly to your UPI! 🚀\n\nYouTube: https://youtube.com/@nkphonk1?si=LdwDyXDIgKK6JWiW\nTelegram: https://t.me/SUPPER_SAIYAN_PHONK_NK"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Pixora 4K to Everyone"))
                        } catch (_: Exception) {
                        }
                    },
                    onDismiss = { showStartupEarnMoneyPopup = false }
                )
            }

            // Direct ₹299 QR Payment Modal when triggered from Startup Popup (hidden once Premium is unlocked)
            if (showStartupQrUpgradeModal && !isPremiumUser) {
                GoPremiumModalDialog(
                    isPremiumUser = false,
                    customQrCodeUri = customQrCodeUri,
                    startWithQrStep = true,
                    onUpdateCustomQrCode = { uri -> viewModel.updateCustomQrCodeImage(context, uri) },
                    onUpgradeClick = { verifiedUtr ->
                        viewModel.unlockPremiumViaQrCode(verifiedUtr)
                        showStartupQrUpgradeModal = false
                    },
                    onDismiss = { showStartupQrUpgradeModal = false }
                )
            }

            // Screen 1: 4K Wallpapers Splash Screen with 120Hz Bouncy Scale-Reveal
            AnimatedVisibility(
                visible = showInitialSplash,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(280, easing = FastOutSlowInEasing)) +
                    scaleOut(targetScale = 1.08f, animationSpec = tween(280, easing = FastOutSlowInEasing))
            ) {
                FourKWallpapersSplashScreen(
                    onDismissOrComplete = { showInitialSplash = false }
                )
            }
        }
        }
    }
}
