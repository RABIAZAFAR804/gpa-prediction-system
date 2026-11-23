package com.example.data.ai

import android.util.Log
import com.example.data.model.CourseRecord
import com.example.data.model.PredictionInput
import com.example.data.model.RiskLevel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class GpaPredictionOutput(
    val predictedGpa: Double,
    val confidencePct: Int,
    val riskLevel: RiskLevel,
    val prognosisSummary: String,
    val actionPlan: List<String>,
    val counterfactualTip: String
)

class GpaAiPredictor {
    private val predictionSchema = Schema.obj(
        properties = mapOf(
            "predictedGpa" to Schema.double(description = "Predicted semester GPA strictly between 0.00 and 4.00"),
            "confidencePct" to Schema.integer(description = "Prediction certainty percentage between 50 and 99"),
            "riskLevel" to Schema.enumeration(
                values = listOf("LOW", "MODERATE", "HIGH"),
                description = "Academic risk level"
            ),
            "prognosisSummary" to Schema.string(description = "Concise 2-3 sentence clinical academic prognosis"),
            "actionPlan" to Schema.array(
                items = Schema.string(),
                description = "3 to 4 specific high-impact academic actions"
            ),
            "counterfactualTip" to Schema.string(description = "One counterfactual improvement simulation tip")
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = predictionSchema
            }
        )
    }

    suspend fun predictGpa(
        input: PredictionInput,
        courses: List<CourseRecord>
    ): Result<GpaPredictionOutput> {
        return try {
            val courseContext = if (courses.isEmpty()) {
                "No individual course records logged yet."
            } else {
                courses.joinToString("; ") {
                    "${it.code} (${it.title}, ${it.credits}cr, score=${it.currentScore}%, attendance=${it.attendancePct}%, difficulty=${it.difficulty})"
                }
            }

            val prompt = """
                You are an academic predictive telemetry engine on a 4.00 university GPA scale.
                Evaluate this student's semester indicators and produce an accurate, calibrated GPA prediction and actionable study prognosis.

                Semester: ${input.semesterName}
                Previous Cumulative GPA: ${"%.2f".format(input.previousGpa)} / 4.00
                Credit Hours Enrolled: ${input.creditHours}
                Study Hours / Week: ${"%.1f".format(input.studyHoursPerWeek)}
                Attendance Percentage: ${"%.1f".format(input.attendancePct)}%
                Assignment Average: ${"%.1f".format(input.assignmentAvg)}%
                Midterm Average: ${"%.1f".format(input.midtermAvg)}%
                Average Sleep / Night: ${"%.1f".format(input.sleepHours)} hours
                Enrolled Courses Context: $courseContext

                Calibration guidance:
                - Weight midterm average, assignment average, attendance, previous GPA, and study/sleep sustainability realistically onto a 0.00 - 4.00 scale.
                - Use LOW risk when predictedGpa >= 3.40 and attendance >= 85%, MODERATE for borderline or high-variance profiles, HIGH when predictedGpa < 2.75 or attendance < 75% or midtermAvg < 68%.
                - Keep actionPlan to 3 concise, concrete bullets.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val rawJson = response.text
                ?: return Result.failure(IllegalStateException("AI prediction engine returned an empty response."))

            val json = JSONObject(rawJson)
            val predictedGpa = json.getDouble("predictedGpa").coerceIn(0.0, 4.0)
            val confidencePct = json.getInt("confidencePct").coerceIn(50, 99)
            val riskLevel = RiskLevel.fromWire(json.optString("riskLevel", "MODERATE"))
            val summary = json.optString("prognosisSummary").ifBlank {
                "Trajectory calibrated against current midterm, attendance, and workload telemetry."
            }
            val planArray = json.optJSONArray("actionPlan")
            val actionPlan = buildList {
                if (planArray != null) {
                    for (i in 0 until minOf(planArray.length(), 6)) {
                        val item = planArray.optString(i).trim()
                        if (item.isNotEmpty()) add(item)
                    }
                }
            }.ifEmpty {
                listOf("Prioritize high-credit course assessments", "Maintain 90%+ lecture attendance")
            }
            val counterfactualTip = json.optString("counterfactualTip").ifBlank {
                "Adding +3 focused study hours per week on core courses raises projected margin."
            }

            Result.success(
                GpaPredictionOutput(
                    predictedGpa = Math.round(predictedGpa * 100.0) / 100.0,
                    confidencePct = confidencePct,
                    riskLevel = riskLevel,
                    prognosisSummary = summary,
                    actionPlan = actionPlan,
                    counterfactualTip = counterfactualTip
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Firebase AI GPA prediction timed out", e)
            Result.failure(Exception("Prediction timed out after 60 seconds. Please retry.", e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI GPA prediction failed", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "GpaAiPredictor"
    }
}
