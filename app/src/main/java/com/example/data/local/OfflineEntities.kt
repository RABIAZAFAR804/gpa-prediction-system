package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_articles")
data class OfflineArticleEntity(
    @PrimaryKey val id: String,
    val authorName: String,
    val authorRole: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val tagsCsv: String,
    val coverKey: String,
    val readMinutes: Long,
    val contentMarkdown: String,
    val sampleContent: Boolean,
    val publishedDateLabel: String,
    val savedAtMillis: Long
)

@Entity(tableName = "bookmarked_articles")
data class BookmarkedArticleEntity(
    @PrimaryKey val articleId: String,
    val bookmarkedAtMillis: Long
)

@Entity(tableName = "pending_predictions")
data class PendingPredictionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val semesterName: String,
    val studyHoursPerWeek: Double,
    val attendancePct: Double,
    val assignmentAvg: Double,
    val midtermAvg: Double,
    val previousGpa: Double,
    val creditHours: Int,
    val sleepHours: Double,
    val createdAtMillis: Long,
    val status: String, // "PENDING_SYNC", "SYNCING", "FAILED"
    val errorMessage: String? = null
)

@Entity(tableName = "cached_profiles")
data class CachedProfileEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val major: String,
    val university: String,
    val semesterLabel: String,
    val currentGpa: Double,
    val targetGpa: Double,
    val completedCredits: Long,
    val weeklyStudyGoalHours: Long,
    val role: String,
    val cachedAtMillis: Long
)

@Entity(tableName = "cached_courses")
data class CachedCourseEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val code: String,
    val title: String,
    val credits: Long,
    val currentScore: Double,
    val attendancePct: Double,
    val targetGrade: String,
    val difficulty: String,
    val cachedAtMillis: Long
)

@Entity(tableName = "cached_predictions")
data class CachedPredictionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val semesterName: String,
    val studyHoursPerWeek: Double,
    val attendancePct: Double,
    val assignmentAvg: Double,
    val midtermAvg: Double,
    val previousGpa: Double,
    val creditHours: Long,
    val sleepHours: Double,
    val predictedGpa: Double,
    val confidencePct: Long,
    val riskLevel: String,
    val prognosisSummary: String,
    val actionPlanJoined: String,
    val counterfactualTip: String?,
    val source: String,
    val createdAtMillis: Long
)
