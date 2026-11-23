package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PredictionInput
import com.example.data.model.RiskLevel
import com.example.ui.MainDashboardState
import com.example.ui.PredictionRunState
import com.example.ui.components.Gpa3DOrbGauge
import com.example.ui.components.RiskChip
import com.example.ui.theme.LocalSyntheticaColors
import kotlin.math.roundToInt

@Composable
fun PredictScreen(
    state: MainDashboardState,
    runState: PredictionRunState,
    onRunPrediction: (PredictionInput) -> Unit,
    onSyncPending: () -> Unit,
    onDeletePending: (String) -> Unit
) {
    val synth = LocalSyntheticaColors.current
    var semesterName by remember(state.profile.semesterLabel) {
        mutableStateOf(state.profile.semesterLabel.ifBlank { "Fall 2026" })
    }
    var previousGpa by remember(state.profile.currentGpa) {
        mutableDoubleStateOf(state.profile.currentGpa)
    }
    var studyHours by remember(state.profile.weeklyStudyGoalHours) {
        mutableDoubleStateOf(state.profile.weeklyStudyGoalHours.toDouble())
    }
    var attendancePct by remember { mutableDoubleStateOf(91.0) }
    var assignmentAvg by remember { mutableDoubleStateOf(88.0) }
    var midtermAvg by remember { mutableDoubleStateOf(85.0) }
    var creditHours by remember { mutableIntStateOf(15) }
    var sleepHours by remember { mutableDoubleStateOf(7.2) }

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
            // Online vs Offline Model Notice
            if (!state.isOnline) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("offline_prediction_notice"),
                        shape = RoundedCornerShape(20.dp),
                        color = synth.riskModerate.copy(alpha = 0.14f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, synth.riskModerate.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudOff,
                                contentDescription = "Offline",
                                tint = synth.riskModerate
                            )
                            Column {
                                Text(
                                    text = "Prediction requires internet connection",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "The Gemini AI prediction service is unavailable offline. You can save your inputs to the Pending Sync queue and we will run the real forecast automatically when connectivity returns.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = synth.mutedText
                                )
                            }
                        }
                    }
                }
            }

            // Prediction Output Banner (when just computed or queued)
            item {
                when (runState) {
                    is PredictionRunState.Running -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = synth.executiveDark
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CircularProgressIndicator(color = synth.teal, strokeWidth = 3.dp)
                                Column {
                                    Text(
                                        text = "Running Gemini AI GPA Prognosis...",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Calibrating midterm velocity, credit load, and course telemetry.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }
                    }
                    is PredictionRunState.Success -> {
                        val pred = runState.prediction
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("prediction_result_card"),
                            shape = RoundedCornerShape(24.dp),
                            color = synth.executiveDark,
                            shadowElevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            listOf(synth.executiveDark, synth.executiveSecondary)
                                        )
                                    )
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "GEMINI AI FORECAST RESULT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = synth.teal
                                    )
                                    RiskChip(riskLevel = RiskLevel.fromWire(pred.riskLevel))
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Gpa3DOrbGauge(
                                        gpa = pred.predictedGpa,
                                        label = "Projected GPA",
                                        confidencePct = pred.confidencePct.toInt(),
                                        size = 128.dp
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = pred.semesterName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = pred.prognosisSummary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFE2E8F0)
                                        )
                                    }
                                }

                                if (pred.actionPlan.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "RECOMMENDED ACTION PLAN",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = synth.electricBlue
                                        )
                                        pred.actionPlan.forEach { step ->
                                            Text(
                                                text = "• $step",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                if (!pred.counterfactualTip.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = synth.purple.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            synth.purple.copy(alpha = 0.45f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Rounded.Lightbulb,
                                                contentDescription = null,
                                                tint = Color(0xFFD8B4FE),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = pred.counterfactualTip ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is PredictionRunState.QueuedOffline -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("prediction_queued_card"),
                            shape = RoundedCornerShape(20.dp),
                            color = synth.royalBlue.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, synth.royalBlue.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Schedule, contentDescription = null, tint = synth.royalBlue)
                                Column {
                                    Text(
                                        text = "Saved as Pending Sync",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "Your inputs for ${runState.pendingItem.input.semesterName} are queued locally. Prediction will execute via Gemini AI as soon as internet returns.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = synth.mutedText
                                    )
                                }
                            }
                        }
                    }
                    is PredictionRunState.Error -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = runState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    PredictionRunState.Idle -> {}
                }
            }

            // Input Controls Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Semester Simulation Inputs",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        OutlinedTextField(
                            value = semesterName,
                            onValueChange = { semesterName = it },
                            label = { Text("Semester Label") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("predict_semester_input")
                        )

                        ParameterSlider(
                            label = "PREVIOUS CUMULATIVE GPA",
                            valueText = String.format("%.2f / 4.00", previousGpa),
                            value = previousGpa.toFloat(),
                            range = 0.0f..4.0f,
                            onValueChange = { previousGpa = ((it * 100.0).roundToInt() / 100.0) }
                        )

                        ParameterSlider(
                            label = "WEEKLY STUDY HOURS",
                            valueText = String.format("%.1f hrs/wk", studyHours),
                            value = studyHours.toFloat(),
                            range = 2f..55f,
                            onValueChange = { studyHours = ((it * 2.0).roundToInt() / 2.0) }
                        )

                        ParameterSlider(
                            label = "ATTENDANCE RATE",
                            valueText = "${attendancePct.roundToInt()}%",
                            value = attendancePct.toFloat(),
                            range = 40f..100f,
                            onValueChange = { attendancePct = it.roundToInt().toDouble() }
                        )

                        ParameterSlider(
                            label = "ASSIGNMENT AVERAGE",
                            valueText = "${assignmentAvg.roundToInt()}%",
                            value = assignmentAvg.toFloat(),
                            range = 40f..100f,
                            onValueChange = { assignmentAvg = it.roundToInt().toDouble() }
                        )

                        ParameterSlider(
                            label = "MIDTERM EXAM AVERAGE",
                            valueText = "${midtermAvg.roundToInt()}%",
                            value = midtermAvg.toFloat(),
                            range = 40f..100f,
                            onValueChange = { midtermAvg = it.roundToInt().toDouble() }
                        )

                        ParameterSlider(
                            label = "ENROLLED CREDIT HOURS",
                            valueText = "$creditHours credits",
                            value = creditHours.toFloat(),
                            range = 3f..24f,
                            onValueChange = { creditHours = it.roundToInt() }
                        )

                        ParameterSlider(
                            label = "AVERAGE SLEEP / NIGHT",
                            valueText = String.format("%.1f hrs", sleepHours),
                            value = sleepHours.toFloat(),
                            range = 4f..10f,
                            onValueChange = { sleepHours = ((it * 2.0).roundToInt() / 2.0) }
                        )

                        Button(
                            onClick = {
                                onRunPrediction(
                                    PredictionInput(
                                        semesterName = semesterName.ifBlank { "Fall 2026" },
                                        studyHoursPerWeek = studyHours,
                                        attendancePct = attendancePct,
                                        assignmentAvg = assignmentAvg,
                                        midtermAvg = midtermAvg,
                                        previousGpa = previousGpa,
                                        creditHours = creditHours,
                                        sleepHours = sleepHours
                                    )
                                )
                            },
                            enabled = runState !is PredictionRunState.Running,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isOnline) synth.executiveDark else synth.royalBlue,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("run_prediction_submit_button")
                        ) {
                            Icon(
                                imageVector = if (state.isOnline) Icons.Rounded.AutoAwesome else Icons.Rounded.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (state.isOnline) {
                                    "Generate Gemini AI GPA Prediction"
                                } else {
                                    "Queue Prediction (Pending Sync)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Pending Sync Queue Section
            if (state.pendingPredictions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pending Sync Queue (${state.pendingPredictions.size})",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (state.isOnline) {
                            Button(
                                onClick = onSyncPending,
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = synth.teal)
                            ) {
                                Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Sync All Now")
                            }
                        }
                    }
                }

                items(state.pendingPredictions, key = { it.id }) { pending ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pending_prediction_item"),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = synth.riskModerate)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(pending.input.semesterName, style = MaterialTheme.typography.titleSmall)
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = synth.riskModerate.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = if (pending.status == "PENDING_SYNC") "Pending Sync" else pending.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = synth.riskModerate,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Study ${pending.input.studyHoursPerWeek}h/wk • Att ${pending.input.attendancePct.roundToInt()}% • Midterm ${pending.input.midtermAvg.roundToInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = synth.mutedText
                                )
                                if (!pending.errorMessage.isNullOrBlank()) {
                                    Text(
                                        text = pending.errorMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = synth.riskHigh
                                    )
                                }
                            }
                            IconButton(onClick = { onDeletePending(pending.id) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove queued item")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParameterSlider(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    val synth = LocalSyntheticaColors.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = synth.mutedText)
            Text(valueText, style = MaterialTheme.typography.labelLarge, color = synth.royalBlue)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = synth.executiveDark,
                activeTrackColor = synth.royalBlue,
                inactiveTrackColor = synth.royalBlue.copy(alpha = 0.16f)
            )
        )
    }
}
