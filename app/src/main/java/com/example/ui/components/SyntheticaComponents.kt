package com.example.ui.components

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.GetApp
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import coil.compose.AsyncImage
import com.example.MainActivity
import com.example.R
import com.example.data.model.RiskLevel
import com.example.ui.theme.LocalSyntheticaColors

@Composable
fun SyntheticaTopBar(
    title: String,
    subtitle: String,
    isOnline: Boolean,
    isSyncing: Boolean,
    pendingCount: Int,
    companionInstalled: Boolean,
    onRetrySync: () -> Unit,
    onInstallClick: () -> Unit
) {
    val synth = LocalSyntheticaColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subtitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = synth.royalBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Online/Offline + Sync Status Pill
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isOnline) synth.riskLow.copy(alpha = 0.12f) else synth.riskModerate.copy(alpha = 0.14f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onRetrySync() }
                        .testTag("network_status_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                strokeWidth = 2.dp,
                                color = synth.royalBlue
                            )
                        } else {
                            Icon(
                                imageVector = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                                contentDescription = if (isOnline) "Online" else "Offline",
                                tint = if (isOnline) synth.riskLow else synth.riskModerate,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = when {
                                isSyncing -> "Syncing"
                                !isOnline -> "Offline"
                                pendingCount > 0 -> "Sync ($pendingCount)"
                                else -> "Online"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isOnline) synth.riskLow else synth.riskModerate
                        )
                    }
                }

                // Install App / Installed Status Header Button
                if (!companionInstalled) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = synth.executiveDark,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onInstallClick() }
                            .testTag("header_install_app_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GetApp,
                                contentDescription = "Install App",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Install App",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = synth.teal.copy(alpha = 0.14f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onInstallClick() }
                            .testTag("header_installed_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Installed",
                                tint = synth.teal,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Installed",
                                style = MaterialTheme.typography.labelMedium,
                                color = synth.teal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineNotificationBanner(
    visible: Boolean,
    pendingCount: Int,
    onRetryClick: () -> Unit
) {
    val synth = LocalSyntheticaColors.current
    AnimatedVisibility(visible = visible) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("offline_notification_banner"),
            shape = RoundedCornerShape(16.dp),
            color = synth.executiveDark,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        synth.riskModerate.copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(synth.riskModerate.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudOff,
                        contentDescription = "Offline Mode",
                        tint = synth.riskModerate,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "You are offline. Your saved academic data and downloaded articles are still available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    if (pendingCount > 0) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "$pendingCount prediction request(s) queued as Pending Sync.",
                            style = MaterialTheme.typography.labelSmall,
                            color = synth.teal
                        )
                    }
                }
                FilledTonalButton(
                    onClick = onRetryClick,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.testTag("offline_retry_button")
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Retry connection", modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Retry", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun InstallPromotionBanner(
    visible: Boolean,
    onInstallClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val synth = LocalSyntheticaColors.current
    val infinite = rememberInfiniteTransition(label = "promo3D")
    val floatY by infinite.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    val tiltY by infinite.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiltY"
    )

    AnimatedVisibility(visible = visible) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("install_promo_banner"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                synth.royalBlue.copy(alpha = 0.08f),
                                synth.teal.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        synth.royalBlue.copy(alpha = 0.22f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .testTag("dismiss_install_banner")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss install banner",
                        tint = synth.mutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_install_companion),
                        contentDescription = "3D mobile companion preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(88.dp)
                            .graphicsLayer {
                                translationY = floatY
                                rotationY = tiltY
                                cameraDistance = 12f * density
                            }
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Take your academic companion anywhere.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Install GPA Predictor for faster access and offline features.",
                            style = MaterialTheme.typography.bodySmall,
                            color = synth.mutedText
                        )
                        Spacer(Modifier.height(2.dp))
                        Button(
                            onClick = onInstallClick,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = synth.executiveDark,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("promo_install_button")
                        ) {
                            Icon(Icons.Rounded.GetApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Install App", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstallCompanionDialog(
    alreadyInstalled: Boolean,
    onConfirmInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val synth = LocalSyntheticaColors.current
    var shortcutFeedback by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (alreadyInstalled) Icons.Rounded.CheckCircle else Icons.Rounded.DownloadForOffline,
                contentDescription = null,
                tint = if (alreadyInstalled) synth.teal else synth.royalBlue,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = if (alreadyInstalled) "GPA Predictor Installed" else "Install GPA Predictor",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.img_install_companion),
                    contentDescription = "GPA Predictor standalone companion preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Text(
                    text = if (alreadyInstalled) {
                        "GPA Predictor is configured for instant standalone launch, offline saved articles, and cached academic analytics."
                    } else {
                        "Pin GPA Predictor directly to your home screen for standalone access, offline reading, and queued AI predictions."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = synth.royalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Chrome / Web Companion: To install this app in Chrome, open the browser menu and select Install App or Add to Home Screen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (shortcutFeedback != null) {
                    Text(
                        text = shortcutFeedback ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = synth.teal
                    )
                }
            }
        },
        confirmButton = {
            if (!alreadyInstalled) {
                Button(
                    onClick = {
                        val pinned = requestHomeScreenPin(context)
                        shortcutFeedback = if (pinned) {
                            "Shortcut request sent to launcher!"
                        } else {
                            "Saved as installed companion."
                        }
                        onConfirmInstall()
                    },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = synth.executiveDark),
                    modifier = Modifier.testTag("dialog_confirm_install_button")
                ) {
                    Text("Pin & Install App")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (!alreadyInstalled) {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        }
    )
}

private fun requestHomeScreenPin(context: Context): Boolean {
    return try {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val shortcut = ShortcutInfoCompat.Builder(context, "gpa_predictor_home")
                .setShortLabel("GPA Predictor")
                .setLongLabel("Synthetica AI GPA Predictor")
                .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                .setIntent(
                    Intent(context, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                    }
                )
                .build()
            val callbackIntent = ShortcutManagerCompat.createShortcutResultIntent(context, shortcut)
            val successCallback = PendingIntent.getBroadcast(
                context,
                0,
                callbackIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            ShortcutManagerCompat.requestPinShortcut(context, shortcut, successCallback.intentSender)
        } else {
            false
        }
    } catch (_: Exception) {
        false
    }
}

@Composable
fun Gpa3DOrbGauge(
    gpa: Double,
    label: String,
    confidencePct: Int? = null,
    size: Dp = 144.dp
) {
    val synth = LocalSyntheticaColors.current
    val animatedProgress by animateFloatAsState(
        targetValue = (gpa / 4.0).toFloat().coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 220f),
        label = "gpaSweep"
    )
    val infinite = rememberInfiniteTransition(label = "orbGlow")
    val pulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val pad = stroke / 2f + 6.dp.toPx()
            val arcSize = Size(this.size.width - pad * 2, this.size.height - pad * 2)
            val topLeft = Offset(pad, pad)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        synth.royalBlue.copy(alpha = 0.22f * pulse),
                        synth.teal.copy(alpha = 0.08f * pulse),
                        Color.Transparent
                    )
                ),
                radius = this.size.minDimension * 0.5f
            )

            drawArc(
                color = Color.White.copy(alpha = 0.14f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(synth.royalBlue, synth.teal, synth.purple, synth.electricBlue)
                ),
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.2f", gpa),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = synth.electricBlue
            )
            if (confidencePct != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$confidencePct% certainty",
                    style = MaterialTheme.typography.labelSmall,
                    color = synth.teal
                )
            }
        }
    }
}

@Composable
fun RiskChip(riskLevel: RiskLevel) {
    val synth = LocalSyntheticaColors.current
    val color = when (riskLevel) {
        RiskLevel.LOW -> synth.riskLow
        RiskLevel.MODERATE -> synth.riskModerate
        RiskLevel.HIGH -> synth.riskHigh
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(999.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = riskLevel.label,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}

@Composable
fun BlogCoverImage(
    coverKey: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val drawableRes = when (coverKey) {
        "img_blog_gpa_strategy" -> R.drawable.img_blog_gpa_strategy
        "img_blog_wellness" -> R.drawable.img_blog_wellness
        else -> R.drawable.img_blog_ai_education
    }
    if (coverKey.startsWith("http://") || coverKey.startsWith("https://")) {
        AsyncImage(
            model = coverKey,
            contentDescription = contentDescription,
            placeholder = painterResource(id = drawableRes),
            error = painterResource(id = drawableRes),
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

@Composable
fun OriginBadge(
    sampleContent: Boolean,
    published: Boolean,
    originScope: String
) {
    val synth = LocalSyntheticaColors.current
    val (label, color) = when {
        sampleContent -> "Sample Editorial" to synth.purple
        !published -> "Draft • Unpublished" to synth.riskModerate
        originScope == "GLOBAL_ADMIN" -> "Admin Published" to synth.teal
        else -> "Admin Studio Post" to synth.royalBlue
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.14f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
