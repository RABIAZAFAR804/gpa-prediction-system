package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.RiskLevel
import com.example.ui.MainDashboardState
import com.example.ui.components.BlogCoverImage
import com.example.ui.components.Gpa3DOrbGauge
import com.example.ui.components.InstallPromotionBanner
import com.example.ui.components.OriginBadge
import com.example.ui.components.RiskChip
import com.example.ui.theme.LocalSyntheticaColors

@Composable
fun HomeScreen(
    state: MainDashboardState,
    onNavigateToPredict: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToBlogs: (filterOfflineOnly: Boolean) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBlogAdmin: () -> Unit,
    onOpenBlogDetail: (String) -> Unit,
    onInstallClick: () -> Unit,
    onDismissInstallBanner: () -> Unit,
    onSyncNow: () -> Unit
) {
    val synth = LocalSyntheticaColors.current
    val latestPrediction = state.predictions.firstOrNull()
    val displayGpa = latestPrediction?.predictedGpa ?: state.profile.currentGpa
    val risk = RiskLevel.fromWire(latestPrediction?.riskLevel)
    val featuredBlog = state.blogs.firstOrNull { it.article.featured && it.article.published }
        ?: state.blogs.firstOrNull { it.article.published }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Install App Promotion Banner (persisted dismissal)
            item {
                InstallPromotionBanner(
                    visible = !state.installBannerDismissed && !state.companionInstalled,
                    onInstallClick = onInstallClick,
                    onDismiss = onDismissInstallBanner
                )
            }

            // 2. Executive AI Prognosis Hero Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = synth.executiveDark,
                    shadowElevation = 10.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(synth.executiveDark, synth.executiveSecondary)
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(synth.electricBlue.copy(alpha = 0.35f), Color.Transparent)
                                ),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "WELCOME BACK, ${state.profile.displayName.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = synth.teal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "${state.profile.major} • ${state.profile.semesterLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFCBD5E1),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                RiskChip(riskLevel = risk)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Gpa3DOrbGauge(
                                    gpa = displayGpa,
                                    label = if (latestPrediction != null) "Predicted GPA" else "Current GPA",
                                    confidencePct = latestPrediction?.confidencePct?.toInt(),
                                    size = 138.dp
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    HeroMetricPill(
                                        label = "BASELINE CUMULATIVE",
                                        value = String.format("%.2f GPA", state.profile.currentGpa),
                                        accent = synth.electricBlue
                                    )
                                    HeroMetricPill(
                                        label = "TARGET GOAL",
                                        value = String.format("%.2f GPA", state.profile.targetGpa),
                                        accent = synth.teal
                                    )
                                    HeroMetricPill(
                                        label = "WEEKLY STUDY TARGET",
                                        value = "${state.profile.weeklyStudyGoalHours} hrs/wk",
                                        accent = synth.purple
                                    )
                                }
                            }

                            if (latestPrediction != null) {
                                Text(
                                    text = latestPrediction.prognosisSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0)
                                )
                            } else {
                                Text(
                                    text = "Run your first Gemini AI forecast to compute semester risk telemetry and a personalized study roadmap.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFCBD5E1)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToPredict,
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = synth.royalBlue,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("home_run_prediction_button")
                                ) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Predict GPA")
                                }
                                FilledTonalButton(
                                    onClick = onNavigateToAnalytics,
                                    shape = RoundedCornerShape(999.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("home_analytics_button")
                                ) {
                                    Icon(Icons.Rounded.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Analytics")
                                }
                            }
                        }
                    }
                }
            }

            // 3. Pending Sync Status Card (when queued items exist)
            if (state.pendingPredictions.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = synth.riskModerate.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            synth.riskModerate.copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = "Pending Sync",
                                tint = synth.riskModerate
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${state.pendingPredictions.size} Prediction(s) Pending Sync",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = if (state.isOnline) {
                                        "Connection restored — tap Sync to compute real Gemini AI forecasts."
                                    } else {
                                        "Queued offline safely. Will sync automatically when online."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = synth.mutedText
                                )
                            }
                            if (state.isOnline) {
                                Button(
                                    onClick = onSyncNow,
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = synth.executiveDark)
                                ) {
                                    Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Sync")
                                }
                            }
                        }
                    }
                }
            }

            // 4. Quick Telemetry Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TelemetryStatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToAnalytics() },
                        icon = Icons.Rounded.BarChart,
                        accent = synth.royalBlue,
                        label = "CACHED COURSES",
                        value = "${state.courses.size}",
                        sublabel = "Semester records"
                    )
                    TelemetryStatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToHistory() },
                        icon = Icons.Rounded.History,
                        accent = synth.purple,
                        label = "SAVED FORECASTS",
                        value = "${state.predictions.size}",
                        sublabel = "Offline-cached"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TelemetryStatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToBlogs(true) },
                        icon = Icons.Rounded.DownloadDone,
                        accent = synth.teal,
                        label = "SAVED OFFLINE",
                        value = "${state.offlineArticlesCount}",
                        sublabel = "Downloaded blogs"
                    )
                    TelemetryStatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (state.isAdminActive) onNavigateToBlogAdmin()
                                else onNavigateToBlogs(false)
                            },
                        icon = if (state.isAdminActive) Icons.Rounded.AdminPanelSettings else Icons.Rounded.MenuBook,
                        accent = synth.electricBlue,
                        label = if (state.isAdminActive) "ADMIN STUDIO" else "KNOWLEDGE HUB",
                        value = if (state.isAdminActive) "Manage" else "${state.blogs.count { it.article.published }}",
                        sublabel = if (state.isAdminActive) "Blog CRUD editor" else "Editorial guides"
                    )
                }
            }

            // 5. Featured Knowledge Hub Article Preview
            if (featuredBlog != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Featured Editorial",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "All Articles →",
                                style = MaterialTheme.typography.labelLarge,
                                color = synth.royalBlue,
                                modifier = Modifier
                                    .clickable { onNavigateToBlogs(false) }
                                    .padding(4.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { onOpenBlogDetail(featuredBlog.article.id) }
                                .testTag("home_featured_blog_card"),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp
                        ) {
                            Column {
                                BlogCoverImage(
                                    coverKey = featuredBlog.article.coverKey,
                                    contentDescription = featuredBlog.article.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(164.dp)
                                )
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = synth.royalBlue.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = featuredBlog.article.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = synth.royalBlue,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        OriginBadge(
                                            sampleContent = featuredBlog.article.sampleContent,
                                            published = featuredBlog.article.published,
                                            originScope = featuredBlog.originScope
                                        )
                                    }
                                    Text(
                                        text = featuredBlog.article.title,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Text(
                                        text = featuredBlog.article.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = synth.mutedText,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${featuredBlog.article.authorName} • ${featuredBlog.article.readMinutes} min read",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = synth.mutedText
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = "Read article",
                                            tint = synth.royalBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMetricPill(label: String, value: String, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = accent
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TelemetryStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    accent: Color,
    label: String,
    value: String,
    sublabel: String
) {
    val synth = LocalSyntheticaColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = synth.mutedText
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = sublabel,
                style = MaterialTheme.typography.bodySmall,
                color = synth.mutedText
            )
        }
    }
}
