package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.RiskLevel
import com.example.ui.MainDashboardState
import com.example.ui.components.RiskChip
import com.example.ui.theme.LocalSyntheticaColors
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    state: MainDashboardState,
    onSyncPending: () -> Unit,
    onDeletePending: (String) -> Unit,
    onDeletePrediction: (String) -> Unit
) {
    val synth = LocalSyntheticaColors.current
    val dateFmt = SimpleDateFormat("MMM dd, HH:mm", Locale.US)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (state.isOnline) "CLOUD + OFFLINE LEDGER" else "CACHED OFFLINE HISTORY",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.isOnline) synth.teal else synth.riskModerate
                        )
                        Text("Prediction History", style = MaterialTheme.typography.headlineMedium)
                    }
                    if (state.pendingPredictions.isNotEmpty() && state.isOnline) {
                        Button(
                            onClick = onSyncPending,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = synth.executiveDark)
                        ) {
                            Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sync (${state.pendingPredictions.size})")
                        }
                    }
                }
            }

            if (state.pendingPredictions.isNotEmpty()) {
                item {
                    Text(
                        text = "Pending Sync Queue",
                        style = MaterialTheme.typography.titleMedium,
                        color = synth.riskModerate
                    )
                }
                items(state.pendingPredictions, key = { it.id }) { pending ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = synth.riskModerate.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = synth.riskModerate)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${pending.input.semesterName} • Pending Sync",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Prev GPA ${pending.input.previousGpa} • Midterm ${pending.input.midtermAvg.roundToInt()}% • Study ${pending.input.studyHoursPerWeek}h",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = synth.mutedText
                                )
                            }
                            IconButton(onClick = { onDeletePending(pending.id) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete queued item")
                            }
                        }
                    }
                }
            }

            if (state.predictions.isEmpty() && state.pendingPredictions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.History,
                                contentDescription = null,
                                tint = synth.royalBlue,
                                modifier = Modifier.size(38.dp)
                            )
                            Text("No Saved Predictions Yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Run a Gemini AI GPA forecast on the Predict tab. Every result is automatically cached for offline viewing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = synth.mutedText
                            )
                        }
                    }
                }
            } else {
                items(state.predictions, key = { it.id }) { pred ->
                    val risk = RiskLevel.fromWire(pred.riskLevel)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = String.format("%.2f GPA", pred.predictedGpa),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = synth.royalBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    RiskChip(riskLevel = risk)
                                }
                                if (state.isOnline) {
                                    IconButton(onClick = { onDeletePrediction(pred.id) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete prediction")
                                    }
                                }
                            }
                            Text(
                                text = "${pred.semesterName} • ${pred.confidencePct}% certainty • ${if (pred.source == "SYNCED_OFFLINE_QUEUE") "Synced from Offline Queue" else "Gemini AI"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = synth.teal
                            )
                            Text(pred.prognosisSummary, style = MaterialTheme.typography.bodySmall)
                            val dateStr = pred.createdAt?.toDate()?.let { dateFmt.format(it) } ?: "Cached"
                            Text(
                                text = "Midterm ${pred.midtermAvg.roundToInt()}% • Att ${pred.attendancePct.roundToInt()}% • Study ${pred.studyHoursPerWeek}h/wk • $dateStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = synth.mutedText
                            )
                        }
                    }
                }
            }
        }
    }
}
