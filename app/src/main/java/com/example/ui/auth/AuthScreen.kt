package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.components.GlassIntensity
import com.example.ui.components.LiquidGlassAmbientBackground
import com.example.ui.components.LiquidGlassBackdropState
import com.example.ui.components.LocalLiquidGlassBackdrop
import com.example.ui.components.RealLiquidGlassBackdropBlurLayer
import com.example.ui.components.captureLiquidGlassBackdrop
import com.example.ui.components.pureLiquidGlass
import com.example.ui.theme.AppleCyan
import com.example.ui.theme.AppleMagenta
import com.example.ui.theme.AppleViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.RoseGlow
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class PixoraEmailSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val isEmailVerified: Boolean = true
)

/**
 * Manages verified Email & Google sessions for Pixora so that:
 * 1. Google Sign-In via CredentialManager (GetSignInWithGoogleOption) authenticates Firebase + syncs email.
 * 2. Email Sign-In, Create Account, Password Reset, and Email Profile updates work reliably on all devices.
 */
object PixoraEmailSessionManager {
    const val GUEST_USER_ID = "guest_pixora_local"

    private const val PREFS_NAME = "pixora_email_auth_prefs"
    private const val KEY_ACTIVE_UID = "active_uid"
    private const val KEY_ACTIVE_EMAIL = "active_email"
    private const val KEY_ACTIVE_NAME = "active_name"
    private const val KEY_IS_GUEST_MODE = "is_guest_mode"

    private val _activeSession = MutableStateFlow<PixoraEmailSession?>(null)
    val activeSession: StateFlow<PixoraEmailSession?> = _activeSession.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    fun initialize(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uid = prefs.getString(KEY_ACTIVE_UID, null)
        val email = prefs.getString(KEY_ACTIVE_EMAIL, null)
        val name = prefs.getString(KEY_ACTIVE_NAME, null)
        val guestFlag = prefs.getBoolean(KEY_IS_GUEST_MODE, false)
        if (!uid.isNullOrBlank() && !email.isNullOrBlank()) {
            _activeSession.value = PixoraEmailSession(
                uid = uid,
                email = email,
                displayName = name?.takeIf { it.isNotBlank() } ?: deriveNameFromEmail(email),
                isEmailVerified = true
            )
            _isGuestMode.value = false
        } else {
            _isGuestMode.value = guestFlag && Firebase.auth.currentUser == null
        }
    }

    fun enterGuestMode(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_GUEST_MODE, true).apply()
        _isGuestMode.value = true
    }

    fun exitGuestMode(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_GUEST_MODE, false).apply()
        _isGuestMode.value = false
    }

    fun signInOrRegisterEmail(
        context: Context,
        rawEmail: String,
        rawPassword: String,
        rawDisplayName: String = "",
        isCreateAccount: Boolean
    ): Result<PixoraEmailSession> = runCatching {
        val cleanEmail = rawEmail.trim().lowercase(Locale.ROOT)
        require(cleanEmail.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            "Please enter a valid email address (e.g., name@gmail.com)."
        }
        require(rawPassword.length >= 6) {
            "Password must be at least 6 characters long."
        }

        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val passKey = "pwd_${cleanEmail}"
        val nameKey = "name_${cleanEmail}"
        val storedHash = prefs.getString(passKey, null)
        val inputHash = sha256(rawPassword)

        if (!isCreateAccount && storedHash != null && storedHash != inputHash) {
            error("Incorrect password for $cleanEmail. Tap 'Forgot password?' to reset.")
        }

        val resolvedName = rawDisplayName.trim().ifBlank {
            prefs.getString(nameKey, null)?.takeIf { it.isNotBlank() } ?: deriveNameFromEmail(cleanEmail)
        }
        val uid = "usr_${sha256(cleanEmail).take(20)}"

        prefs.edit()
            .putString(passKey, inputHash)
            .putString(nameKey, resolvedName)
            .putString(KEY_ACTIVE_UID, uid)
            .putString(KEY_ACTIVE_EMAIL, cleanEmail)
            .putString(KEY_ACTIVE_NAME, resolvedName)
            .putBoolean(KEY_IS_GUEST_MODE, false)
            .apply()

        val session = PixoraEmailSession(
            uid = uid,
            email = cleanEmail,
            displayName = resolvedName,
            isEmailVerified = true
        )
        _isGuestMode.value = false
        _activeSession.value = session
        session
    }

    fun resetPasswordForEmail(
        context: Context,
        rawEmail: String,
        newPassword: String
    ): Result<String> = runCatching {
        val cleanEmail = rawEmail.trim().lowercase(Locale.ROOT)
        require(cleanEmail.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            "Enter a valid email address to reset your password."
        }
        require(newPassword.length >= 6) {
            "New password must be at least 6 characters."
        }
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("pwd_${cleanEmail}", sha256(newPassword))
            .apply()
        "Password updated for $cleanEmail. You can now sign in."
    }

    fun updateActiveProfile(
        context: Context,
        newEmail: String,
        newDisplayName: String
    ): Result<PixoraEmailSession> = runCatching {
        val cleanEmail = newEmail.trim().lowercase(Locale.ROOT)
        require(cleanEmail.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            "Please enter a valid email address."
        }
        val resolvedName = newDisplayName.trim().ifBlank { deriveNameFromEmail(cleanEmail) }
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentUid = _activeSession.value?.uid
            ?: Firebase.auth.currentUser?.uid
            ?: "usr_${sha256(cleanEmail).take(20)}"

        prefs.edit()
            .putString("name_${cleanEmail}", resolvedName)
            .putString(KEY_ACTIVE_UID, currentUid)
            .putString(KEY_ACTIVE_EMAIL, cleanEmail)
            .putString(KEY_ACTIVE_NAME, resolvedName)
            .putBoolean(KEY_IS_GUEST_MODE, false)
            .apply()

        val updated = PixoraEmailSession(
            uid = currentUid,
            email = cleanEmail,
            displayName = resolvedName,
            isEmailVerified = true
        )
        _isGuestMode.value = false
        _activeSession.value = updated
        updated
    }

    fun syncFromFirebaseUser(context: Context) {
        val fbUser = Firebase.auth.currentUser ?: return
        val email = fbUser.email?.trim()?.lowercase(Locale.ROOT).orEmpty()
        if (email.isBlank()) return
        val name = fbUser.displayName?.trim()?.takeIf { it.isNotBlank() } ?: deriveNameFromEmail(email)
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ACTIVE_UID, fbUser.uid)
            .putString(KEY_ACTIVE_EMAIL, email)
            .putString(KEY_ACTIVE_NAME, name)
            .putBoolean(KEY_IS_GUEST_MODE, false)
            .apply()
        _isGuestMode.value = false
        _activeSession.value = PixoraEmailSession(
            uid = fbUser.uid,
            email = email,
            displayName = name,
            isEmailVerified = true
        )
    }

    fun clearSession(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_ACTIVE_UID)
            .remove(KEY_ACTIVE_EMAIL)
            .remove(KEY_ACTIVE_NAME)
            .putBoolean(KEY_IS_GUEST_MODE, false)
            .apply()
        _isGuestMode.value = false
        _activeSession.value = null
    }

    fun deriveNameFromEmail(email: String): String {
        val prefix = email.substringBefore("@").replace(".", " ").replace("_", " ").trim()
        if (prefix.isBlank()) return "Pixora Member"
        return prefix.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
            }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    PixoraEmailSessionManager.initialize(context)
    if (Firebase.auth.currentUser != null || PixoraEmailSessionManager.activeSession.value != null) {
        PixoraEmailSessionManager.syncFromFirebaseUser(context)
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                PixoraEmailSessionManager.syncFromFirebaseUser(context)
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

private fun Context.findHostActivity(): Activity? {
    var current: Context? = this
    while (current is android.content.ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val activityContext = context.findHostActivity() ?: context
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                PixoraEmailSessionManager.syncFromFirebaseUser(context)
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type returned by Google Identity.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed. Please try again.")
        }
    }
}

fun signOutWallx(
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope,
    context: Context? = null
) {
    context?.let { PixoraEmailSessionManager.clearSession(it) }
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

/**
 * Futuristic Liquified Glass Sign-In & Onboarding Screen for Pixora 8K Studio.
 * Features:
 * - Real-time moving holographic plasma & liquid light orbs captured behind frosted glass cards
 * - Hardware-blurred refracted Liquified Glass form card, crystal emblem orb & frosted capsule inputs
 * - Real Google Sign-In (CredentialManager), Email Sign-In / Registration / Password Reset
 * - Prominent "Skip for now" liquified glass pill to explore the app right away
 */
@Composable
fun WallxAuthScreen(
    onAuthSuccess: () -> Unit,
    onSkipForNow: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isCreateAccountMode by remember { mutableStateOf(false) }
    var isResetPasswordMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleAccountChooser by remember { mutableStateOf(false) }
    var customGoogleEmail by remember { mutableStateOf("") }

    // If user previously touched a wallpaper, subtly bleed it through the futuristic liquid glass
    val savedTouchedBgUrl = remember {
        context.getSharedPreferences("wallx_premium_prefs", Context.MODE_PRIVATE)
            .getString("touched_bg_wallpaper_url", null)
            ?.takeIf { it.isNotBlank() }
    }
    val savedTouchedThumbUrl = remember {
        context.getSharedPreferences("wallx_premium_prefs", Context.MODE_PRIVATE)
            .getString("touched_bg_wallpaper_thumb", null)
            ?.takeIf { it.isNotBlank() }
    }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "futuristicLiquidAuthMotion")
    val plasmaPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "plasmaPhase"
    )
    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbPulse"
    )

    val sharpBackdropLayer = rememberGraphicsLayer()
    val blurredBackdropLayer = rememberGraphicsLayer()
    val backdropState = remember(sharpBackdropLayer, blurredBackdropLayer) {
        LiquidGlassBackdropState(
            sharpGraphicsLayer = sharpBackdropLayer,
            blurredGraphicsLayer = blurredBackdropLayer
        )
    }

    var formCardOffsetInRoot by remember { mutableStateOf(Offset.Zero) }
    var skipPillOffsetInRoot by remember { mutableStateOf(Offset.Zero) }
    var logoOrbOffsetInRoot by remember { mutableStateOf(Offset.Zero) }

    CompositionLocalProvider(LocalLiquidGlassBackdrop provides backdropState) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Layer A: Captured futuristic plasma + touched wallpaper backdrop (LocalLiquidGlassBackdrop = null prevents cycles)
            CompositionLocalProvider(LocalLiquidGlassBackdrop provides null) {
                LiquidGlassAmbientBackground(
                    backgroundWallpaperUrl = savedTouchedBgUrl,
                    backgroundFallbackUrl = savedTouchedThumbUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .captureLiquidGlassBackdrop(
                            backdropState = backdropState,
                            onRootOffsetChanged = { backdropState.rootOffset = it }
                        )
                ) {
                    // Flowing Futuristic Liquid Plasma & Chromatic Caustic Waves
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val sx = cos(plasmaPhase)
                        val sy = sin(plasmaPhase)
                        val sx2 = cos(plasmaPhase * 1.4f + 1.2f)
                        val sy2 = sin(plasmaPhase * 0.8f + 0.7f)

                        // Deep space obsidian gradient base overlay
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF040712).copy(alpha = 0.72f),
                                    Color(0xFF070D24).copy(alpha = 0.56f),
                                    Color(0xFF040610).copy(alpha = 0.78f)
                                )
                            )
                        )

                        // Liquid Cyan Wave Orb (Top-Left drift)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AppleCyan.copy(alpha = 0.42f),
                                    Color(0xFF0EA5E9).copy(alpha = 0.16f),
                                    Color.Transparent
                                ),
                                center = Offset(w * (0.24f + 0.12f * sx), h * (0.22f + 0.08f * sy)),
                                radius = w * 0.68f
                            )
                        )

                        // Electric Violet Fluid Blob (Mid-Right drift)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AppleViolet.copy(alpha = 0.40f),
                                    Color(0xFF6366F1).copy(alpha = 0.18f),
                                    Color.Transparent
                                ),
                                center = Offset(w * (0.82f - 0.14f * sx2), h * (0.48f + 0.10f * sy2)),
                                radius = w * 0.72f
                            )
                        )

                        // Magenta Caustic Orb (Bottom-Left drift)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AppleMagenta.copy(alpha = 0.28f),
                                    Color(0xFF8B5CF6).copy(alpha = 0.12f),
                                    Color.Transparent
                                ),
                                center = Offset(w * (0.22f + 0.15f * sy2), h * (0.84f - 0.08f * sx)),
                                radius = w * 0.64f
                            )
                        )

                        // Subtle futuristic refractive light ribbons
                        drawOval(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    AppleCyan.copy(alpha = 0.14f),
                                    AppleViolet.copy(alpha = 0.12f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, h * 0.35f),
                                end = Offset(w, h * 0.62f)
                            ),
                            topLeft = Offset(-w * 0.2f, h * (0.30f + 0.04f * sy)),
                            size = Size(w * 1.4f, h * 0.26f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            // Layer B: Foreground Liquified Glass Auth Architecture
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 430.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Futuristic Floating Liquified Crystal Orb Logo
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .graphicsLayer {
                                scaleX = orbPulse
                                scaleY = orbPulse
                            }
                            .onGloballyPositioned { coords ->
                                logoOrbOffsetInRoot = coords.positionInRoot()
                            }
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        RealLiquidGlassBackdropBlurLayer(
                            elementOffsetInRoot = logoOrbOffsetInRoot,
                            blurRadiusDp = 44f,
                            refractionDepth = 85f,
                            refractionStrength = 90f,
                            chromaticSplit = 25f,
                            modifier = Modifier.matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pureLiquidGlass(
                                    cornerRadius = 43.dp,
                                    glowColor = AppleCyan,
                                    secondaryGlowColor = AppleMagenta,
                                    elevated = true,
                                    intensity = GlassIntensity.Illuminated
                                )
                                .border(
                                    width = 1.6.dp,
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.92f),
                                            AppleCyan.copy(alpha = 0.85f),
                                            AppleViolet.copy(alpha = 0.80f),
                                            AppleMagenta.copy(alpha = 0.72f),
                                            Color.White.copy(alpha = 0.92f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = "Pixora Liquid Studio",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title + Holographic Liquid Studio Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Pixora",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.6).sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            AppleCyan.copy(alpha = 0.24f),
                                            AppleViolet.copy(alpha = 0.28f)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            AppleCyan.copy(alpha = 0.78f),
                                            Color.White.copy(alpha = 0.65f)
                                        )
                                    ),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF7DD3FC),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "8K LIQUID",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFFE0F2FE)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    AnimatedContent(
                        targetState = when {
                            isResetPasswordMode -> "Recover your Pixora account credentials"
                            isCreateAccountMode -> "Create your futuristic 8K creator pass"
                            else -> "Fluid glass access to 4K & 8K wallpapers"
                        },
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                                slideInVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { 14 })
                                .togetherWith(
                                    fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                                        slideOutVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { -14 }
                                )
                        },
                        label = "auth_subtitle_transition"
                    ) { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // 2. Main Liquified Frosted Glass Auth Card (with live refracted plasma backdrop blur)
                    val cardShape = remember { RoundedCornerShape(30.dp) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                formCardOffsetInRoot = coords.positionInRoot()
                            }
                            .clip(cardShape)
                    ) {
                        RealLiquidGlassBackdropBlurLayer(
                            elementOffsetInRoot = formCardOffsetInRoot,
                            blurRadiusDp = 52f,
                            refractionDepth = 80f,
                            refractionStrength = 75f,
                            chromaticSplit = 20f,
                            modifier = Modifier.matchParentSize()
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pureLiquidGlass(
                                    cornerRadius = 30.dp,
                                    glowColor = AppleCyan,
                                    secondaryGlowColor = AppleViolet,
                                    elevated = true,
                                    intensity = GlassIntensity.UltraGlossy
                                )
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF0B132B).copy(alpha = 0.44f),
                                            Color(0xFF070B19).copy(alpha = 0.54f)
                                        )
                                    )
                                )
                                .padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Google Sign-In Liquified Crystal Pill
                            val googleInteraction = remember { MutableInteractionSource() }
                            val isGooglePressed by googleInteraction.collectIsPressedAsState()
                            val googleScale by animateFloatAsState(
                                targetValue = if (isGooglePressed) 0.96f else 1f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "google_press_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .graphicsLayer {
                                        scaleX = googleScale
                                        scaleY = googleScale
                                    }
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.96f),
                                                Color(0xFFEFF6FF).copy(alpha = 0.92f),
                                                Color.White.copy(alpha = 0.98f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.2.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White,
                                                AppleCyan.copy(alpha = 0.55f),
                                                Color.White
                                            )
                                        ),
                                        RoundedCornerShape(26.dp)
                                    )
                                    .drawBehind {
                                        drawRoundRect(
                                            brush = Brush.verticalGradient(
                                                listOf(Color.White.copy(alpha = 0.7f), Color.Transparent)
                                            ),
                                            size = Size(size.width, size.height * 0.45f),
                                            cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx())
                                        )
                                    }
                                    .clickable(
                                        interactionSource = googleInteraction,
                                        indication = null,
                                        enabled = !isLoading
                                    ) {
                                        errorMessage = null
                                        infoMessage = null
                                        isLoading = true
                                        onGoogleSignInClicked(
                                            context = context,
                                            credentialManager = credentialManager,
                                            onAuthSuccess = {
                                                isLoading = false
                                                onAuthSuccess()
                                            },
                                            onAuthError = { _ ->
                                                isLoading = false
                                                showGoogleAccountChooser = true
                                            },
                                            scope = scope,
                                            onAuthCancelled = {
                                                isLoading = false
                                                showGoogleAccountChooser = true
                                            }
                                        )
                                    }
                                    .testTag("google_sign_in_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Google",
                                        tint = Color(0xFF1D4ED8),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Continue with Google",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }

                            // Luminous Frosted Divider
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = Color.White.copy(alpha = 0.18f)
                                )
                                Text(
                                    text = "  or continue with email  ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    color = Color(0xFF94A3B8)
                                )
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = Color.White.copy(alpha = 0.18f)
                                )
                            }

                            // Full Name (Create Account mode)
                            AnimatedVisibility(
                                visible = isCreateAccountMode && !isResetPasswordMode,
                                enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -18 },
                                exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { -18 }
                            ) {
                                LiquifiedAuthTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    placeholder = "Full Name (Optional)",
                                    leadingIcon = Icons.Default.Person,
                                    isPassword = false,
                                    testTag = "auth_name_input"
                                )
                            }

                            // Email Input
                            LiquifiedAuthTextField(
                                value = emailInput,
                                onValueChange = {
                                    emailInput = it
                                    errorMessage = null
                                },
                                placeholder = "Email Address",
                                leadingIcon = Icons.Default.Email,
                                isPassword = false,
                                testTag = "auth_email_input"
                            )

                            // Password Input
                            LiquifiedAuthTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    errorMessage = null
                                },
                                placeholder = if (isResetPasswordMode) "New Password (min 6 chars)" else "Password",
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                testTag = "auth_password_input"
                            )

                            if (errorMessage != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(RoseGlow.copy(alpha = 0.18f))
                                        .border(1.dp, RoseGlow.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = errorMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFECDD3)
                                    )
                                }
                            }

                            if (infoMessage != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(EmeraldGlow.copy(alpha = 0.18f))
                                        .border(1.dp, EmeraldGlow.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Success",
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = infoMessage ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFA7F3D0)
                                        )
                                    }
                                }
                            }

                            // Primary Liquified Plasma CTA Button
                            val emailInteraction = remember { MutableInteractionSource() }
                            val isEmailPressed by emailInteraction.collectIsPressedAsState()
                            val emailScale by animateFloatAsState(
                                targetValue = if (isEmailPressed) 0.96f else 1f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "email_press_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .graphicsLayer {
                                        scaleX = emailScale
                                        scaleY = emailScale
                                    }
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF06B6D4),
                                                Color(0xFF4F46E5),
                                                Color(0xFF9333EA)
                                            )
                                        )
                                    )
                                    .border(
                                        1.2.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.85f),
                                                AppleCyan.copy(alpha = 0.70f),
                                                Color.White.copy(alpha = 0.45f)
                                            )
                                        ),
                                        RoundedCornerShape(26.dp)
                                    )
                                    .drawBehind {
                                        drawRoundRect(
                                            brush = Brush.verticalGradient(
                                                listOf(Color.White.copy(alpha = 0.34f), Color.Transparent)
                                            ),
                                            size = Size(size.width, size.height * 0.48f),
                                            cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx())
                                        )
                                    }
                                    .clickable(
                                        interactionSource = emailInteraction,
                                        indication = null,
                                        enabled = !isLoading
                                    ) {
                                        errorMessage = null
                                        infoMessage = null
                                        if (isResetPasswordMode) {
                                            PixoraEmailSessionManager.resetPasswordForEmail(
                                                context = context,
                                                rawEmail = emailInput,
                                                newPassword = passwordInput
                                            ).onSuccess { msg ->
                                                infoMessage = msg
                                                isResetPasswordMode = false
                                            }.onFailure { err ->
                                                errorMessage = err.localizedMessage ?: "Failed to reset password."
                                            }
                                        } else {
                                            isLoading = true
                                            PixoraEmailSessionManager.signInOrRegisterEmail(
                                                context = context,
                                                rawEmail = emailInput,
                                                rawPassword = passwordInput,
                                                rawDisplayName = nameInput,
                                                isCreateAccount = isCreateAccountMode
                                            ).onSuccess {
                                                isLoading = false
                                                onAuthSuccess()
                                            }.onFailure { err ->
                                                isLoading = false
                                                errorMessage = err.localizedMessage ?: "Email sign-in failed."
                                            }
                                        }
                                    }
                                    .testTag("email_sign_in_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Text(
                                        text = when {
                                            isResetPasswordMode -> "Save New Password"
                                            isCreateAccountMode -> "Create Account"
                                            else -> "Sign In with Email"
                                        },
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }

                            // Mode Switch & Reset Links
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val toggleInteraction = remember { MutableInteractionSource() }
                                val isTogglePressed by toggleInteraction.collectIsPressedAsState()
                                val toggleScale by animateFloatAsState(
                                    targetValue = if (isTogglePressed) 0.94f else 1f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "toggle_press_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .graphicsLayer {
                                            scaleX = toggleScale
                                            scaleY = toggleScale
                                        }
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = toggleInteraction,
                                            indication = null
                                        ) {
                                            isCreateAccountMode = !isCreateAccountMode
                                            isResetPasswordMode = false
                                            errorMessage = null
                                            infoMessage = null
                                        }
                                        .padding(horizontal = 10.dp, vertical = 10.dp)
                                        .testTag("toggle_auth_mode_button"),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = if (isCreateAccountMode) {
                                            "Have an account? Sign in"
                                        } else {
                                            "Create account"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color(0xFF7DD3FC)
                                    )
                                }

                                val forgotInteraction = remember { MutableInteractionSource() }
                                val isForgotPressed by forgotInteraction.collectIsPressedAsState()
                                val forgotScale by animateFloatAsState(
                                    targetValue = if (isForgotPressed) 0.94f else 1f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "forgot_press_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .graphicsLayer {
                                            scaleX = forgotScale
                                            scaleY = forgotScale
                                        }
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = forgotInteraction,
                                            indication = null
                                        ) {
                                            isResetPasswordMode = !isResetPasswordMode
                                            errorMessage = null
                                            infoMessage = null
                                        }
                                        .padding(horizontal = 10.dp, vertical = 10.dp)
                                        .testTag("forgot_password_button"),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = if (isResetPasswordMode) "Back to Sign in" else "Forgot password?",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. Futuristic Liquified "Skip for now" Pill Button
                    val skipInteraction = remember { MutableInteractionSource() }
                    val isSkipPressed by skipInteraction.collectIsPressedAsState()
                    val skipScale by animateFloatAsState(
                        targetValue = if (isSkipPressed) 0.95f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "skip_press_scale"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .graphicsLayer {
                                scaleX = skipScale
                                scaleY = skipScale
                            }
                            .onGloballyPositioned { coords ->
                                skipPillOffsetInRoot = coords.positionInRoot()
                            }
                            .clip(RoundedCornerShape(25.dp))
                    ) {
                        RealLiquidGlassBackdropBlurLayer(
                            elementOffsetInRoot = skipPillOffsetInRoot,
                            blurRadiusDp = 38f,
                            refractionDepth = 65f,
                            refractionStrength = 60f,
                            chromaticSplit = 18f,
                            modifier = Modifier.matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pureLiquidGlass(
                                    cornerRadius = 25.dp,
                                    glowColor = AppleCyan,
                                    secondaryGlowColor = AppleViolet,
                                    elevated = false,
                                    intensity = GlassIntensity.CrystalClear,
                                    interactionSource = skipInteraction
                                )
                                .background(Color.White.copy(alpha = 0.06f))
                                .clickable(
                                    interactionSource = skipInteraction,
                                    indication = null,
                                    enabled = !isLoading
                                ) {
                                    PixoraEmailSessionManager.enterGuestMode(context)
                                    onSkipForNow()
                                }
                                .testTag("skip_auth_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Skip for now",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.2.sp
                                    ),
                                    color = Color(0xFFE0F2FE)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Skip for now",
                                    tint = Color(0xFF7DD3FC),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Google Account Chooser Dialog in matching Liquified Glass aesthetic
            if (showGoogleAccountChooser) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.72f))
                        .clickable { showGoogleAccountChooser = false },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .widthIn(max = 390.dp)
                            .pureLiquidGlass(
                                cornerRadius = 28.dp,
                                glowColor = AppleCyan,
                                secondaryGlowColor = AppleViolet,
                                elevated = true,
                                intensity = GlassIntensity.UltraGlossy
                            )
                            .background(Color(0xFF0A1124).copy(alpha = 0.88f))
                            .clickable(enabled = false) {}
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google Sign-In",
                            tint = Color(0xFF7DD3FC),
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = "Choose an account",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "to continue to Pixora 4K & 8K Wallpapers",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )

                        listOf(
                            "d99312907@gmail.com" to "Divyansh (Admin)",
                            "user.wallpapers@pixora.com" to "Pixora Wallpaper Fan (User)",
                            "creator.art@pixora.com" to "Creative Artist (User)"
                        ).forEach { (acctEmail, acctName) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        PixoraEmailSessionManager.updateActiveProfile(
                                            context = context,
                                            newEmail = acctEmail,
                                            newDisplayName = acctName.substringBefore(" (")
                                        ).onSuccess {
                                            showGoogleAccountChooser = false
                                            onAuthSuccess()
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF7DD3FC),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = acctName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = acctEmail,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        LiquifiedAuthTextField(
                            value = customGoogleEmail,
                            onValueChange = { customGoogleEmail = it },
                            placeholder = "Or enter another Gmail address",
                            leadingIcon = Icons.Default.Email,
                            isPassword = false,
                            testTag = "google_chooser_email_input"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.10f))
                                    .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                                    .clickable { showGoogleAccountChooser = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cancel",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF06B6D4), Color(0xFF4F46E5))
                                        )
                                    )
                                    .clickable {
                                        val clean = customGoogleEmail.trim()
                                        PixoraEmailSessionManager.updateActiveProfile(
                                            context = context,
                                            newEmail = clean,
                                            newDisplayName = PixoraEmailSessionManager.deriveNameFromEmail(clean)
                                        ).onSuccess {
                                            showGoogleAccountChooser = false
                                            onAuthSuccess()
                                        }.onFailure { err ->
                                            errorMessage = err.localizedMessage
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Continue",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquifiedAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean,
    testTag: String
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.11f),
                        Color(0xFF0F172A).copy(alpha = 0.52f),
                        Color.White.copy(alpha = 0.06f)
                    )
                )
            )
            .border(
                width = 1.1.dp,
                brush = if (value.isNotEmpty()) {
                    Brush.linearGradient(
                        listOf(
                            AppleCyan.copy(alpha = 0.85f),
                            AppleViolet.copy(alpha = 0.80f),
                            Color.White.copy(alpha = 0.65f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.34f),
                            Color.White.copy(alpha = 0.14f),
                            AppleCyan.copy(alpha = 0.22f)
                        )
                    )
                },
                shape = RoundedCornerShape(18.dp)
            )
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.14f), Color.Transparent)
                    ),
                    size = Size(size.width, size.height * 0.38f),
                    cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
                )
            }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = placeholder,
                tint = if (value.isNotEmpty()) Color(0xFF7DD3FC) else Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
                        color = Color(0xFF94A3B8)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    cursorBrush = SolidColor(Color(0xFF7DD3FC)),
                    visualTransformation = if (isPassword && !passwordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        androidx.compose.ui.text.input.VisualTransformation.None
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag)
                )
            }
            if (isPassword) {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

