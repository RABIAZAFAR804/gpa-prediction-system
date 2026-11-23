package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CourseDifficulty
import com.example.data.model.CourseRecord
import com.example.data.model.GpaPrediction
import com.example.ui.MainDashboardState
import com.example.ui.theme.LocalSyntheticaColors
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
    state: MainDashboardState,
    onAddCourse: (String, String, Long, Double, Double, String, String) -> Unit,
    onLoadDemoCourses: () -> Unit,
    onDeleteCourse: (String) -> Unit
) {
    val synth = LocalSyntheticaColors.current
    var showAddDialog by remember { mutableStateOf(false) }

    val weightedScore = remember(state.courses) {
        if (state.courses.isEmpty()) null
        else {
            val totalCr = state.courses.sumOf { it.credits }.coerceAtLeast(1L)
            state.courses.sumOf { it.currentScore * it.credits } / totalCr
        }
    }

    val avgAttendance = remember(state.courses) {
        if (state.courses.isEmpty()) null
        else state.courses.map { it.attendancePct }.average()
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
            // Analytics Header + Offline Cache Indicator
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (state.isOnline) "Live Telemetry" else "Cached Offline Analytics",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.isOnline) synth.teal else synth.riskModerate
                        )
                        Text(
                            text = "Academic Performance",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        enabled = state.isOnline,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = synth.executiveDark),
                        modifier = Modifier.testTag("analytics_add_course_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Course")
                    }
                }
            }

            // Summary KPI Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnalyticsMiniKpi(
                        modifier = Modifier.weight(1f),
                        label = "WEIGHTED SCORE",
                        value = weightedScore?.let { String.format("%.1f%%", it) } ?: "—",
                        accent = synth.royalBlue
                    )
                    AnalyticsMiniKpi(
                        modifier = Modifier.weight(1f),
                        label = "AVG ATTENDANCE",
                        value = avgAttendance?.let { "${it.roundToInt()}%" } ?: "—",
                        accent = synth.teal
                    )
                    AnalyticsMiniKpi(
                        modifier = Modifier.weight(1f),
                        label = "TOTAL CREDITS",
                        value = "${state.courses.sumOf { it.credits }}",
                        accent = synth.purple
                    )
                }
            }

            // GPA Trajectory Chart (Canvas)
            item {
                GpaTrajectoryChartCard(
                    predictions = state.predictions.reversed(),
                    baselineGpa = state.profile.currentGpa,
                    targetGpa = state.profile.targetGpa
                )
            }

            // Course Performance Bar Chart
            if (state.courses.isNotEmpty()) {
                item {
                    CourseBarChartCard(courses = state.courses)
                }
            }

            // Enrolled Courses Ledger
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enrolled Courses (${state.courses.size})",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    if (state.courses.isEmpty() && state.isOnline) {
                        FilledTonalButton(
                            onClick = onLoadDemoCourses,
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.testTag("load_demo_courses_button")
                        ) {
                            Text("Load Sample Courses")
                        }
                    }
                }
            }

            if (state.courses.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.School,
                                contentDescription = null,
                                tint = synth.royalBlue,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No Course Records Cached Yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (state.isOnline) {
                                    "Add your active semester courses or load sample semester records to unlock weighted GPA analytics."
                                } else {
                                    "You are offline with no cached courses yet. Reconnect to add courses."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = synth.mutedText
                            )
                        }
                    }
                }
            } else {
                items(state.courses, key = { it.id }) { course ->
                    CourseRecordCard(
                        course = course,
                        canDelete = state.isOnline,
                        onDelete = { onDeleteCourse(course.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCourseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { code, title, credits, score, att, target, diff ->
                onAddCourse(code, title, credits, score, att, target, diff)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AnalyticsMiniKpi(
    modifier: Modifier,
    label: String,
    value: String,
    accent: Color
) {
    val synth = LocalSyntheticaColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = synth.mutedText)
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent)
        }
    }
}

@Composable
private fun GpaTrajectoryChartCard(
    predictions: List<GpaPrediction>,
    baselineGpa: Double,
    targetGpa: Double
) {
    val synth = LocalSyntheticaColors.current
    val points = remember(predictions, baselineGpa) {
        if (predictions.isEmpty()) listOf(baselineGpa)
        else listOf(baselineGpa) + predictions.map { it.predictedGpa }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = synth.executiveDark,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GPA TRAJECTORY & TARGET BAND",
                        style = MaterialTheme.typography.labelSmall,
                        color = synth.teal
                    )
                    Text(
                        text = "${predictions.size} Forecast Run(s)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                Icon(Icons.Rounded.AutoGraph, contentDescription = null, tint = synth.electricBlue)
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val minY = 2.0f
                val maxY = 4.0f
                fun yFor(gpa: Double): Float {
                    val norm = ((gpa.toFloat() - minY) / (maxY - minY)).coerceIn(0f, 1f)
                    return size.height - norm * (size.height - 24.dp.toPx()) - 12.dp.toPx()
                }

                // Target dashed line
                val targetY = yFor(targetGpa)
                drawLine(
                    color = synth.teal.copy(alpha = 0.65f),
                    start = Offset(0f, targetY),
                    end = Offset(size.width, targetY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                )

                val stepX = if (points.size <= 1) size.width else size.width / (points.size - 1)
                val path = Path()
                points.forEachIndexed { idx, gpa ->
                    val x = if (points.size == 1) size.width / 2f else idx * stepX
                    val y = yFor(gpa)
                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                if (points.size > 1) {
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(listOf(synth.electricBlue, synth.teal, synth.purple)),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                points.forEachIndexed { idx, gpa ->
                    val x = if (points.size == 1) size.width / 2f else idx * stepX
                    val y = yFor(gpa)
                    drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(x, y))
                    drawCircle(color = synth.royalBlue, radius = 3.5.dp.toPx(), center = Offset(x, y))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Baseline: ${String.format("%.2f", baselineGpa)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = synth.electricBlue
                )
                Text(
                    text = "Target: ${String.format("%.2f", targetGpa)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = synth.teal
                )
            }
        }
    }
}

@Composable
private fun CourseBarChartCard(courses: List<CourseRecord>) {
    val synth = LocalSyntheticaColors.current
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
            Text("Course Mastery Distribution", style = MaterialTheme.typography.headlineSmall)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val barCount = courses.size.coerceAtLeast(1)
                val slotWidth = size.width / barCount
                val barWidth = (slotWidth * 0.54f).coerceAtMost(44.dp.toPx())
                courses.forEachIndexed { idx, c ->
                    val ratio = (c.currentScore / 100.0).toFloat().coerceIn(0.05f, 1f)
                    val barHeight = ratio * (size.height - 14.dp.toPx())
                    val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                    val top = size.height - barHeight
                    val barColor = when {
                        c.currentScore >= 88.0 -> synth.teal
                        c.currentScore >= 78.0 -> synth.royalBlue
                        else -> synth.riskModerate
                    }
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                courses.take(5).forEach {
                    Text(
                        text = it.code,
                        style = MaterialTheme.typography.labelSmall,
                        color = synth.mutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseRecordCard(
    course: CourseRecord,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val synth = LocalSyntheticaColors.current
    val diff = CourseDifficulty.fromWire(course.difficulty)
    val scoreColor = when {
        course.currentScore >= 88.0 -> synth.riskLow
        course.currentScore >= 76.0 -> synth.riskModerate
        else -> synth.riskHigh
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(scoreColor.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${course.currentScore.roundToInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    color = scoreColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("${course.code} • ${course.title}", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${course.credits} cr • Att ${course.attendancePct.roundToInt()}% • Target ${course.targetGrade} • ${diff.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = synth.mutedText
                )
            }
            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete course")
                }
            }
        }
    }
}

@Composable
private fun AddCourseDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Double, Double, String, String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("3") }
    var score by remember { mutableStateOf("89") }
    var attendance by remember { mutableStateOf("94") }
    var targetGrade by remember { mutableStateOf("A") }
    var difficulty by remember { mutableStateOf("MEDIUM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Semester Course", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code (e.g. CS 320)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Course Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = credits,
                        onValueChange = { credits = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Credits") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetGrade,
                        onValueChange = { targetGrade = it.take(3) },
                        label = { Text("Target") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = score,
                        onValueChange = { score = it },
                        label = { Text("Score %") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = attendance,
                        onValueChange = { attendance = it },
                        label = { Text("Attend %") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("LOW", "MEDIUM", "HIGH").forEach { diff ->
                        OutlinedButton(
                            onClick = { difficulty = diff },
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (difficulty == diff) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(diff, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        code.ifBlank { "CS 101" },
                        title.ifBlank { "Course" },
                        credits.toLongOrNull()?.coerceIn(1L, 12L) ?: 3L,
                        score.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 85.0,
                        attendance.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 90.0,
                        targetGrade.ifBlank { "A" },
                        difficulty
                    )
                },
                shape = RoundedCornerShape(999.dp)
            ) {
                Text("Save Course")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
