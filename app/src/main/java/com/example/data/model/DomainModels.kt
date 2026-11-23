package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

val BLOG_CATEGORIES = listOf(
    "Academic Success",
    "GPA Improvement",
    "Study Techniques",
    "Time Management",
    "Mental Wellness for Students",
    "AI in Education",
    "Exam Preparation",
    "University Life",
    "Career Development"
)

enum class UserRole(val wireValue: String) {
    STUDENT("STUDENT"),
    ADMIN("ADMIN");

    companion object {
        fun fromWire(raw: String?): UserRole =
            entries.firstOrNull { it.wireValue == raw } ?: STUDENT
    }
}

enum class RiskLevel(val wireValue: String, val label: String) {
    LOW("LOW", "Low Risk • On Target"),
    MODERATE("MODERATE", "Moderate Volatility"),
    HIGH("HIGH", "High GPA Alert");

    companion object {
        fun fromWire(raw: String?): RiskLevel =
            entries.firstOrNull { it.wireValue == raw } ?: MODERATE
    }
}

enum class CourseDifficulty(val wireValue: String, val label: String) {
    LOW("LOW", "Foundational"),
    MEDIUM("MEDIUM", "Core"),
    HIGH("HIGH", "Intensive");

    companion object {
        fun fromWire(raw: String?): CourseDifficulty =
            entries.firstOrNull { it.wireValue == raw } ?: MEDIUM
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Scholar",
    val major: String = "Computer Science & AI",
    val university: String = "Institute of Technology",
    val semesterLabel: String = "Fall 2026",
    val currentGpa: Double = 3.58,
    val targetGpa: Double = 3.85,
    val completedCredits: Long = 72L,
    val weeklyStudyGoalHours: Long = 22L,
    val role: String = "STUDENT",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CourseRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val title: String = "",
    val credits: Long = 3L,
    val currentScore: Double = 85.0,
    val attendancePct: Double = 90.0,
    val targetGrade: String = "A",
    val difficulty: String = "MEDIUM",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class GpaPrediction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val semesterName: String = "Fall 2026",
    val studyHoursPerWeek: Double = 18.0,
    val attendancePct: Double = 90.0,
    val assignmentAvg: Double = 88.0,
    val midtermAvg: Double = 85.0,
    val previousGpa: Double = 3.55,
    val creditHours: Long = 15L,
    val sleepHours: Double = 7.2,
    val predictedGpa: Double = 3.64,
    val confidencePct: Long = 88L,
    val riskLevel: String = "LOW",
    val prognosisSummary: String = "",
    val actionPlan: List<String> = emptyList(),
    val counterfactualTip: String? = null,
    val source: String = "GEMINI_AI",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BlogArticle(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val title: String = "",
    val subtitle: String = "",
    val category: String = "Academic Success",
    val tags: List<String> = emptyList(),
    val coverKey: String = "img_blog_ai_education",
    val readMinutes: Long = 5L,
    val contentMarkdown: String = "",
    val published: Boolean = true,
    val featured: Boolean = false,
    val popularScore: Long = 50L,
    val sampleContent: Boolean = true,
    val publishedDateLabel: String = "Nov 2026",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class PredictionInput(
    val semesterName: String,
    val studyHoursPerWeek: Double,
    val attendancePct: Double,
    val assignmentAvg: Double,
    val midtermAvg: Double,
    val previousGpa: Double,
    val creditHours: Int,
    val sleepHours: Double
)

data class PendingPredictionItem(
    val id: String,
    val input: PredictionInput,
    val createdAtMillis: Long,
    val status: String,
    val errorMessage: String? = null
)

data class BlogUiItem(
    val article: BlogArticle,
    val bookmarked: Boolean,
    val savedOffline: Boolean,
    val originScope: String // "SAMPLE" | "GLOBAL_ADMIN" | "LOCAL_WORKSPACE"
)
