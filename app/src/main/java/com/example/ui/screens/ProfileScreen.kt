package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.GetApp
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.MainDashboardState
import com.example.ui.theme.LocalSyntheticaColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    state: MainDashboardState,
    onSaveProfile: (String, String, String, String, Double, Double, Long, Long) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleOfflineSimulation: (Boolean) -> Unit,
    onToggleAdminWorkspace: (Boolean) -> Unit,
    onOpenBlogAdmin: () -> Unit,
    onSyncNow: () -> Unit,
    onInstallAppClick: () -> Unit,
    onSignOut: () -> Unit
) {
    val synth = LocalSyntheticaColors.current
    var displayName by remember(state.profile.displayName) { mutableStateOf(state.profile.displayName) }
    var university by remember(state.profile.university) { mutableStateOf(state.profile.university) }
    var major by remember(state.profile.major) { mutableStateOf(state.profile.major) }
    var semester by remember(state.profile.semesterLabel) { mutableStateOf(state.profile.semesterLabel) }
    var currentGpa by remember(state.profile.currentGpa) { mutableStateOf(state.profile.currentGpa.toString()) }
    var targetGpa by remember(state.profile.targetGpa) { mutableStateOf(state.profile.targetGpa.toString()) }
    var credits by remember(state.profile.completedCredits) { mutableStateOf(state.profile.completedCredits.toString()) }
    var studyGoal by remember(state.profile.weeklyStudyGoalHours) { mutableStateOf(state.profile.weeklyStudyGoalHours.toString()) }

    val syncDateText = remember(state.syncStatus.lastSyncMillis) {
        if (state.syncStatus.lastSyncMillis <= 0L) "Cached locally"
        else SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(state.syncStatus.lastSyncMillis))
    }

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
            // Identity & Role Banner
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = synth.executiveDark
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (state.isBootstrapAdmin) "VERIFIED ADMIN ACCOUNT" else "SCHOLAR ACCOUNT",
                            style = MaterialTheme.typography.labelSmall,
                            color = synth.teal
                        )
                        Text(
                            text = state.profile.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            text = state.userEmail.ifBlank { state.currentUserId },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            // Academic Profile Form
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Academic Baseline", style = MaterialTheme.typography.headlineSmall)
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = university,
                            onValueChange = { university = it },
                            label = { Text("University") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = major,
                                onValueChange = { major = it },
                                label = { Text("Major") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = semester,
                                onValueChange = { semester = it },
                                label = { Text("Semester") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = currentGpa,
                                onValueChange = { currentGpa = it },
                                label = { Text("Current GPA") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = targetGpa,
                                onValueChange = { targetGpa = it },
                                label = { Text("Target GPA") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = credits,
                                onValueChange = { credits = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Completed Credits") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = studyGoal,
                                onValueChange = { studyGoal = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Study Goal (h/wk)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                onSaveProfile(
                                    displayName,
                                    major,
                                    university,
                                    semester,
                                    currentGpa.toDoubleOrNull()?.coerceIn(0.0, 4.0) ?: state.profile.currentGpa,
                                    targetGpa.toDoubleOrNull()?.coerceIn(0.0, 4.0) ?: state.profile.targetGpa,
                                    credits.toLongOrNull()?.coerceIn(0L, 300L) ?: state.profile.completedCredits,
                                    studyGoal.toLongOrNull()?.coerceIn(1L, 100L) ?: state.profile.weeklyStudyGoalHours
                                )
                            },
                            enabled = state.isOnline,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = synth.executiveDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (state.isOnline) "Save Profile to Cloud" else "Reconnect to Edit Cloud Profile")
                        }
                    }
                }
            }

            // Offline, Sync & Diagnostics Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Offline & Synchronization", style = MaterialTheme.typography.headlineSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (state.isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                                    contentDescription = null,
                                    tint = if (state.isOnline) synth.riskLow else synth.riskModerate
                                )
                                Column {
                                    Text(
                                        text = if (state.isOnline) "Online • Connected" else "Offline Mode Active",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "Last sync: $syncDateText • ${state.pendingPredictions.size} pending • ${state.offlineArticlesCount} saved blogs",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = synth.mutedText
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Test Offline Mode", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Preview offline banners, cached reading & pending prediction queue",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = synth.mutedText
                                )
                            }
                            Switch(
                                checked = state.isSimulatedOffline,
                                onCheckedChange = onToggleOfflineSimulation,
                                modifier = Modifier.testTag("profile_offline_toggle")
                            )
                        }

                        Button(
                            onClick = onSyncNow,
                            enabled = state.isOnline,
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sync Queued Data Now")
                        }
                    }
                }
            }

            // Role, Theme & Companion App Installation
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Workspace & App Settings", style = MaterialTheme.typography.headlineSmall)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = synth.royalBlue)
                                Text("Executive Dark Theme", style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(
                                checked = state.darkTheme,
                                onCheckedChange = onToggleDarkTheme,
                                modifier = Modifier.testTag("profile_dark_theme_toggle")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = synth.purple)
                                Column {
                                    Text("Admin Editorial Studio", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "Enable Blog Admin Management dashboard",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = synth.mutedText
                                    )
                                }
                            }
                            Switch(
                                checked = state.isAdminActive,
                                onCheckedChange = onToggleAdminWorkspace,
                                modifier = Modifier.testTag("profile_admin_toggle")
                            )
                        }

                        if (state.isAdminActive) {
                            OutlinedButton(
                                onClick = onOpenBlogAdmin,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Open Blog Admin Manager")
                            }
                        }

                        OutlinedButton(
                            onClick = onInstallAppClick,
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_install_app_button")
                        ) {
                            Icon(
                                imageVector = if (state.companionInstalled) Icons.Rounded.CheckCircle else Icons.Rounded.GetApp,
                                contentDescription = null,
                                tint = if (state.companionInstalled) synth.teal else synth.royalBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (state.companionInstalled) "App Installed • View Details" else "Install App to Home Screen")
                        }

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = synth.riskHigh),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_sign_out_button")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sign Out")
                        }
                    }
                }
            }
        }
    }
}
