package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RoseGlow

fun copyTextToClipboard(context: Context, label: String, text: String, toastMessage: String) {
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }
}

fun shareTextLink(context: Context, subject: String, text: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, subject).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/**
 * Google Play Store & OEM App Stores Compliance Modals:
 * 1. Privacy Policy, Copyable Store Links & Data Safety Disclosure
 * 2. Terms of Service & AI Content Safety / UGC Reporting
 * 3. In-App Account & Cloud Data Deletion Confirmation
 */
private object PixoraComplianceUrls {
    const val PRIVACY_POLICY_URL = "https://lagguy.com/privacy-policy.html"
    const val PRIVACY_POLICY_HOSTING_URL = "https://ai-studio-applet-9fd2cb93.web.app/privacy_policy.html"
    const val TERMS_AND_CONDITIONS_URL = "https://lagguy.com/terms-and-conditions.html"
}

@Composable
fun PlayStorePrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showHtmlWebView by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF101626).copy(alpha = 0.98f),
                            Color(0xFF090D18).copy(alpha = 0.99f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            ElectricCyan.copy(alpha = 0.65f),
                            NeonViolet.copy(alpha = 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(20.dp)
                .testTag("play_store_privacy_dialog")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = "Privacy Policy",
                            tint = ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Privacy Policy & Store Approval",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Play Store • OEM Stores Verified",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldGlow
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Official Copyable Privacy Policy Link Box for App Store Console Submission
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.9f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "OFFICIAL PRIVACY POLICY LINK (FOR PLAY CONSOLE)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        ),
                        color = ElectricCyan
                    )
                    Text(
                        text = PixoraComplianceUrls.PRIVACY_POLICY_URL,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricCyan.copy(alpha = 0.2f))
                                .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .clickable {
                                    copyTextToClipboard(
                                        context = context,
                                        label = "Privacy Policy URL",
                                        text = PixoraComplianceUrls.PRIVACY_POLICY_URL,
                                        toastMessage = "Privacy Policy Link copied!"
                                    )
                                }
                                .testTag("copy_privacy_policy_url_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Link",
                                tint = ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copy Link",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonViolet.copy(alpha = 0.22f))
                                .border(1.dp, NeonViolet.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .clickable {
                                    shareTextLink(
                                        context = context,
                                        subject = "Pixora / Lagguy Privacy Policy Link",
                                        text = "Privacy Policy URL: ${PixoraComplianceUrls.PRIVACY_POLICY_URL}\nMirror URL: ${PixoraComplianceUrls.PRIVACY_POLICY_HOSTING_URL}\nTerms URL: ${PixoraComplianceUrls.TERMS_AND_CONDITIONS_URL}"
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Link",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share Link",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldGlow.copy(alpha = 0.2f))
                                .border(1.dp, EmeraldGlow.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .clickable { showHtmlWebView = !showHtmlWebView },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "View HTML",
                                tint = EmeraldGlow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (showHtmlWebView) "Summary" else "HTML View",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                if (showHtmlWebView) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth().height(340.dp),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    webViewClient = WebViewClient()
                                    settings.javaScriptEnabled = false
                                    loadUrl("file:///android_asset/privacy_policy.html")
                                }
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PolicySectionBlock(
                            title = "1. Data We Collect & Cloud Sync",
                            body = "Pixora collects only your basic sign-in profile (Display Name, Email Address, and UID) via Google Credential Manager or Email Sign-In strictly to authenticate your session and sync your favorite wallpapers and download history across devices."
                        )
                        PolicySectionBlock(
                            title = "2. Zero Third-Party Ads & Tracking",
                            body = "Pixora runs completely ad-free. We do not bundle any third-party advertising SDKs or collect your Android Advertising ID."
                        )
                        PolicySectionBlock(
                            title = "3. Zero Broad Storage Permissions",
                            body = "Complies with Google Play Photo & Video Permissions policy. We never request READ_EXTERNAL_STORAGE or READ_MEDIA_IMAGES. Wallpapers are applied via Android WallpaperManager or saved via system MediaStore."
                        )
                        PolicySectionBlock(
                            title = "4. Account & Cloud Data Deletion Rights",
                            body = "You can permanently delete your account and all cloud data (profile, favorites, and download history) at any time from Settings → Delete Account & Data, or by contacting d99312907@gmail.com."
                        )
                    }
                }

                LiquidGlassButton(
                    text = "Save & Acknowledge",
                    icon = Icons.Default.CheckCircle,
                    filledAccent = true,
                    glowColor = ElectricCyan,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
fun PlayStoreTermsAndAiPolicyDialog(
    onDismiss: () -> Unit,
    onReportSubmitted: (String) -> Unit
) {
    var reportNote by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 620.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF101626).copy(alpha = 0.98f),
                            Color(0xFF090D18).copy(alpha = 0.99f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            NeonViolet.copy(alpha = 0.65f),
                            ElectricCyan.copy(alpha = 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
                .testTag("play_store_terms_dialog")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Terms & AI Safety",
                            tint = NeonViolet,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Terms of Service & AI Safety",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Google Play AI UGC & Licensing Compliance",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PolicySectionBlock(
                        title = "Personal Wallpaper License",
                        body = "All curated and AI-generated wallpapers in Pixora are licensed for personal device customization (Home Screen and Lock Screen). Commercial redistribution without permission is prohibited."
                    )
                    PolicySectionBlock(
                        title = "AI Content Safety & Moderation (Google Play Policy)",
                        body = "Users may not use Pixora 8K AI Studio to generate sexually explicit, violent, hateful, or copyright-infringing imagery. If you encounter any inappropriate AI-generated or catalog image, you can flag it below for immediate removal."
                    )

                    LiquidGlassTextField(
                        value = reportNote,
                        onValueChange = { reportNote = it },
                        placeholder = "Optional: Describe any AI content or wallpaper to report…",
                        testTag = "ai_safety_report_input"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LiquidGlassButton(
                            text = "Flag / Report Content",
                            icon = Icons.Default.Flag,
                            glowColor = RoseGlow,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onReportSubmitted(
                                    if (reportNote.isBlank()) {
                                        "AI Content Safety report logged for moderator review."
                                    } else {
                                        "Report submitted: ${reportNote.take(60)}"
                                    }
                                )
                                onDismiss()
                            }
                        )
                        LiquidGlassButton(
                            text = "Close",
                            glowColor = EmeraldGlow,
                            filledAccent = true,
                            modifier = Modifier.weight(0.8f),
                            onClick = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayStoreDeleteAccountDialog(
    userEmail: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF121624))
                .border(1.dp, RoseGlow.copy(alpha = 0.65f), RoundedCornerShape(26.dp))
                .padding(22.dp)
                .testTag("delete_account_confirmation_dialog")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Account",
                        tint = RoseGlow,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Delete Account & Data?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                }

                Text(
                    text = "This will permanently delete your Pixora cloud profile ($userEmail), saved favorites, and download history from our servers, and sign you out immediately.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassButton(
                        text = "Cancel",
                        glowColor = ElectricCyan,
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    )
                    LiquidGlassButton(
                        text = "Delete Permanently",
                        icon = Icons.Default.DeleteForever,
                        glowColor = RoseGlow,
                        filledAccent = true,
                        modifier = Modifier.weight(1.2f),
                        testTag = "confirm_delete_account_button",
                        onClick = onConfirmDelete
                    )
                }
            }
        }
    }
}

@Composable
fun PlayStoreEmailCenterDialog(
    currentName: String,
    currentEmail: String,
    onSaveProfile: (String, String) -> Unit,
    onSendSupportEmail: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var editedName by remember(currentName) { mutableStateOf(currentName) }
    var editedEmail by remember(currentEmail) { mutableStateOf(currentEmail) }
    var supportSubject by remember { mutableStateOf("Pixora 8K Support / Feedback") }
    var supportMessage by remember { mutableStateOf("") }
    var statusNotice by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF101626).copy(alpha = 0.98f),
                            Color(0xFF090D18).copy(alpha = 0.99f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            ElectricCyan.copy(alpha = 0.65f),
                            NeonViolet.copy(alpha = 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
                .testTag("email_center_dialog")
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Email Center",
                            tint = ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Email & Account Center",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Verified Email Session • Cloud & Support",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldGlow
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text(
                    text = "Profile & Account Email",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                )

                LiquidGlassTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    placeholder = "Display Name",
                    testTag = "email_dialog_name_input"
                )

                LiquidGlassTextField(
                    value = editedEmail,
                    onValueChange = { editedEmail = it },
                    placeholder = "Account Email Address",
                    testTag = "email_dialog_email_input"
                )

                LiquidGlassButton(
                    text = "Save Profile & Email",
                    icon = Icons.Default.CheckCircle,
                    filledAccent = true,
                    glowColor = ElectricCyan,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "save_email_profile_button",
                    onClick = {
                        onSaveProfile(editedName.trim(), editedEmail.trim())
                        statusNotice = "Updated email & profile to ${editedEmail.trim()}"
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text(
                    text = "Send Direct Support Email",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonViolet
                    )
                )

                LiquidGlassTextField(
                    value = supportSubject,
                    onValueChange = { supportSubject = it },
                    placeholder = "Email Subject"
                )

                LiquidGlassTextField(
                    value = supportMessage,
                    onValueChange = { supportMessage = it },
                    placeholder = "Write your message to Pixora Support (d99312907@gmail.com)…"
                )

                if (statusNotice != null) {
                    Text(
                        text = statusNotice.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldGlow
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LiquidGlassButton(
                        text = "Send Email Now",
                        glowColor = NeonViolet,
                        filledAccent = true,
                        modifier = Modifier.weight(1f),
                        testTag = "send_support_email_button",
                        onClick = {
                            onSendSupportEmail(supportSubject, supportMessage)
                            onDismiss()
                        }
                    )
                    LiquidGlassButton(
                        text = "Done",
                        glowColor = ElectricCyan,
                        modifier = Modifier.weight(0.6f),
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicySectionBlock(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall.copy(
                lineHeight = 18.sp,
                color = Color(0xFFE2E8F0)
            )
        )
    }
}

