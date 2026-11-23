package com.example.data.repository

import com.example.data.ai.GpaAiPredictor
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.local.BookmarkedArticleEntity
import com.example.data.local.CachedCourseEntity
import com.example.data.local.CachedPredictionEntity
import com.example.data.local.CachedProfileEntity
import com.example.data.local.OfflineArticleEntity
import com.example.data.local.OfflineDao
import com.example.data.local.PendingPredictionEntity
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.BlogArticle
import com.example.data.model.CourseRecord
import com.example.data.model.GpaPrediction
import com.example.data.model.PendingPredictionItem
import com.example.data.model.PredictionInput
import com.example.data.model.SampleBlogs
import com.example.data.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed interface PredictionOutcome {
    data class Completed(val prediction: GpaPrediction) : PredictionOutcome
    data class QueuedOffline(val pendingItem: PendingPredictionItem) : PredictionOutcome
}

data class SyncReport(
    val syncedCount: Int,
    val failedCount: Int,
    val lastError: String? = null
)

class AcademicRepository(
    private val db: FirebaseFirestore,
    private val offlineDao: OfflineDao,
    private val aiPredictor: GpaAiPredictor,
    private val prefs: UserPreferencesRepository,
    private val auth: FirebaseAuth
) {
    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun isVerifiedBootstrapAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.email.equals("rabiazafar.web@gmail.com", ignoreCase = true) && user.isEmailVerified
    }

    // --- Profile ---
    fun observeProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .onEach { profile ->
                    if (profile != null) {
                        offlineDao.upsertCachedProfile(
                            CachedProfileEntity(
                                userId = profile.userId.ifBlank { userId },
                                displayName = profile.displayName,
                                major = profile.major,
                                university = profile.university,
                                semesterLabel = profile.semesterLabel,
                                currentGpa = profile.currentGpa,
                                targetGpa = profile.targetGpa,
                                completedCredits = profile.completedCredits,
                                weeklyStudyGoalHours = profile.weeklyStudyGoalHours,
                                role = profile.role,
                                cachedAtMillis = System.currentTimeMillis()
                            )
                        )
                    }
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val toLog = fsErr ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeCachedProfile(userId: String): Flow<UserProfile?> =
        offlineDao.observeCachedProfile(userId).map { entity ->
            entity?.let {
                UserProfile(
                    id = it.userId,
                    userId = it.userId,
                    displayName = it.displayName,
                    major = it.major,
                    university = it.university,
                    semesterLabel = it.semesterLabel,
                    currentGpa = it.currentGpa,
                    targetGpa = it.targetGpa,
                    completedCredits = it.completedCredits,
                    weeklyStudyGoalHours = it.weeklyStudyGoalHours,
                    role = it.role,
                    createdAt = Timestamp(Date(it.cachedAtMillis)),
                    updatedAt = Timestamp(Date(it.cachedAtMillis))
                )
            }
        }

    suspend fun ensureUserProfile(defaultName: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid)
            docPath = ref.path
            val snap = ref.get().await()
            if (!snap.exists()) {
                val safeName = defaultName.trim().take(80).ifBlank { "Academic Scholar" }
                val role = if (isVerifiedBootstrapAdmin()) "ADMIN" else "STUDENT"
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "major" to "Computer Science & AI",
                    "university" to "Metro Institute of Technology",
                    "semesterLabel" to "Fall 2026",
                    "currentGpa" to 3.58,
                    "targetGpa" to 3.85,
                    "completedCredits" to 68L,
                    "weeklyStudyGoalHours" to 22L,
                    "role" to role,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                ref.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        displayName: String,
        major: String,
        university: String,
        semesterLabel: String,
        currentGpa: Double,
        targetGpa: Double,
        completedCredits: Long,
        weeklyStudyGoalHours: Long
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid)
            docPath = ref.path
            val payload = mapOf(
                "displayName" to displayName.trim().take(80).ifBlank { "Scholar" },
                "major" to major.trim().take(80).ifBlank { "General Studies" },
                "university" to university.trim().take(100).ifBlank { "University" },
                "semesterLabel" to semesterLabel.trim().take(40).ifBlank { "Fall 2026" },
                "currentGpa" to currentGpa.coerceIn(0.0, 4.0),
                "targetGpa" to targetGpa.coerceIn(0.0, 4.0),
                "completedCredits" to completedCredits.coerceIn(0L, 300L),
                "weeklyStudyGoalHours" to weeklyStudyGoalHours.coerceIn(1L, 100L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    // --- Courses ---
    fun observeCourses(userId: String): Flow<List<CourseRecord>> = flow {
        val path = "users/$userId/courses"
        emitAll(
            db.collection("users").document(userId).collection("courses")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CourseRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.code }
                }
                .onEach { courses ->
                    val now = System.currentTimeMillis()
                    offlineDao.clearCachedCoursesForUser(userId)
                    if (courses.isNotEmpty()) {
                        offlineDao.upsertCachedCourses(
                            courses.map {
                                CachedCourseEntity(
                                    id = it.id,
                                    userId = userId,
                                    code = it.code,
                                    title = it.title,
                                    credits = it.credits,
                                    currentScore = it.currentScore,
                                    attendancePct = it.attendancePct,
                                    targetGrade = it.targetGrade,
                                    difficulty = it.difficulty,
                                    cachedAtMillis = now
                                )
                            }
                        )
                    }
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val toLog = fsErr ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeCachedCourses(userId: String): Flow<List<CourseRecord>> =
        offlineDao.observeCachedCourses(userId).map { list ->
            list.map {
                CourseRecord(
                    id = it.id,
                    userId = it.userId,
                    code = it.code,
                    title = it.title,
                    credits = it.credits,
                    currentScore = it.currentScore,
                    attendancePct = it.attendancePct,
                    targetGrade = it.targetGrade,
                    difficulty = it.difficulty
                )
            }
        }

    suspend fun addCourse(
        code: String,
        title: String,
        credits: Long,
        currentScore: Double,
        attendancePct: Double,
        targetGrade: String,
        difficulty: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/courses"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid).collection("courses").document()
            docPath = ref.path
            val payload = mapOf(
                "userId" to uid,
                "code" to code.trim().take(20).ifBlank { "CS 101" },
                "title" to title.trim().take(100).ifBlank { "Course Title" },
                "credits" to credits.coerceIn(1L, 12L),
                "currentScore" to currentScore.coerceIn(0.0, 100.0),
                "attendancePct" to attendancePct.coerceIn(0.0, 100.0),
                "targetGrade" to targetGrade.trim().take(5).ifBlank { "A" },
                "difficulty" to difficulty.takeIf { it in setOf("LOW", "MEDIUM", "HIGH") }.orEmpty().ifBlank { "MEDIUM" },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCourse(courseId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/courses/$courseId"
        return try {
            val uid = requireUserId()
            require(courseId.isNotBlank())
            val ref = db.collection("users").document(uid).collection("courses").document(courseId)
            docPath = ref.path
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Predictions & Offline Sync Queue ---
    fun observePredictions(userId: String): Flow<List<GpaPrediction>> = flow {
        val path = "users/$userId/predictions"
        emitAll(
            db.collection("users").document(userId).collection("predictions")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        GpaPrediction::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .onEach { items ->
                    offlineDao.clearCachedPredictionsForUser(userId)
                    if (items.isNotEmpty()) {
                        offlineDao.upsertCachedPredictions(
                            items.map {
                                CachedPredictionEntity(
                                    id = it.id,
                                    userId = userId,
                                    semesterName = it.semesterName,
                                    studyHoursPerWeek = it.studyHoursPerWeek,
                                    attendancePct = it.attendancePct,
                                    assignmentAvg = it.assignmentAvg,
                                    midtermAvg = it.midtermAvg,
                                    previousGpa = it.previousGpa,
                                    creditHours = it.creditHours,
                                    sleepHours = it.sleepHours,
                                    predictedGpa = it.predictedGpa,
                                    confidencePct = it.confidencePct,
                                    riskLevel = it.riskLevel,
                                    prognosisSummary = it.prognosisSummary,
                                    actionPlanJoined = it.actionPlan.joinToString("\n"),
                                    counterfactualTip = it.counterfactualTip,
                                    source = it.source,
                                    createdAtMillis = (it.createdAt?.toDate()?.time) ?: System.currentTimeMillis()
                                )
                            }
                        )
                    }
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val toLog = fsErr ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeCachedPredictions(userId: String): Flow<List<GpaPrediction>> =
        offlineDao.observeCachedPredictions(userId).map { list ->
            list.map {
                GpaPrediction(
                    id = it.id,
                    userId = it.userId,
                    semesterName = it.semesterName,
                    studyHoursPerWeek = it.studyHoursPerWeek,
                    attendancePct = it.attendancePct,
                    assignmentAvg = it.assignmentAvg,
                    midtermAvg = it.midtermAvg,
                    previousGpa = it.previousGpa,
                    creditHours = it.creditHours,
                    sleepHours = it.sleepHours,
                    predictedGpa = it.predictedGpa,
                    confidencePct = it.confidencePct,
                    riskLevel = it.riskLevel,
                    prognosisSummary = it.prognosisSummary,
                    actionPlan = it.actionPlanJoined.split("\n").filter { line -> line.isNotBlank() },
                    counterfactualTip = it.counterfactualTip,
                    source = it.source,
                    createdAt = Timestamp(Date(it.createdAtMillis)),
                    updatedAt = Timestamp(Date(it.createdAtMillis))
                )
            }
        }

    fun observePendingPredictions(userId: String): Flow<List<PendingPredictionItem>> =
        offlineDao.observePendingPredictions(userId).map { list ->
            list.map { entity ->
                PendingPredictionItem(
                    id = entity.id,
                    input = PredictionInput(
                        semesterName = entity.semesterName,
                        studyHoursPerWeek = entity.studyHoursPerWeek,
                        attendancePct = entity.attendancePct,
                        assignmentAvg = entity.assignmentAvg,
                        midtermAvg = entity.midtermAvg,
                        previousGpa = entity.previousGpa,
                        creditHours = entity.creditHours,
                        sleepHours = entity.sleepHours
                    ),
                    createdAtMillis = entity.createdAtMillis,
                    status = entity.status,
                    errorMessage = entity.errorMessage
                )
            }
        }

    suspend fun runOrQueuePrediction(
        input: PredictionInput,
        courses: List<CourseRecord>,
        isOnline: Boolean
    ): Result<PredictionOutcome> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!isOnline) {
            val pendingId = "pend_${UUID.randomUUID().toString().replace("-", "").take(12)}"
            val entity = PendingPredictionEntity(
                id = pendingId,
                userId = uid,
                semesterName = input.semesterName.trim().take(60).ifBlank { "Fall 2026" },
                studyHoursPerWeek = input.studyHoursPerWeek,
                attendancePct = input.attendancePct,
                assignmentAvg = input.assignmentAvg,
                midtermAvg = input.midtermAvg,
                previousGpa = input.previousGpa,
                creditHours = input.creditHours,
                sleepHours = input.sleepHours,
                createdAtMillis = System.currentTimeMillis(),
                status = "PENDING_SYNC",
                errorMessage = null
            )
            offlineDao.upsertPendingPrediction(entity)
            return Result.success(
                PredictionOutcome.QueuedOffline(
                    PendingPredictionItem(
                        id = entity.id,
                        input = input,
                        createdAtMillis = entity.createdAtMillis,
                        status = entity.status,
                        errorMessage = null
                    )
                )
            )
        }

        val aiResult = aiPredictor.predictGpa(input, courses)
        val output = aiResult.getOrElse { return Result.failure(it) }

        val saveResult = savePredictionToFirestore(
            uid = uid,
            input = input,
            predictedGpa = output.predictedGpa,
            confidencePct = output.confidencePct,
            riskLevel = output.riskLevel.wireValue,
            prognosisSummary = output.prognosisSummary,
            actionPlan = output.actionPlan,
            counterfactualTip = output.counterfactualTip,
            source = "GEMINI_AI"
        )
        return saveResult.map { PredictionOutcome.Completed(it) }
    }

    suspend fun syncPendingPredictions(courses: List<CourseRecord>): SyncReport {
        val uid = auth.currentUser?.uid ?: return SyncReport(0, 0, "Not signed in")
        val pending = offlineDao.getPendingPredictionsOnce(uid)
        if (pending.isEmpty()) {
            prefs.updateLastSync(System.currentTimeMillis())
            return SyncReport(0, 0)
        }

        var synced = 0
        var failed = 0
        var lastErr: String? = null

        for (item in pending) {
            offlineDao.upsertPendingPrediction(item.copy(status = "SYNCING", errorMessage = null))
            val input = PredictionInput(
                semesterName = item.semesterName,
                studyHoursPerWeek = item.studyHoursPerWeek,
                attendancePct = item.attendancePct,
                assignmentAvg = item.assignmentAvg,
                midtermAvg = item.midtermAvg,
                previousGpa = item.previousGpa,
                creditHours = item.creditHours,
                sleepHours = item.sleepHours
            )
            val aiResult = aiPredictor.predictGpa(input, courses)
            val output = aiResult.getOrNull()
            if (output == null) {
                failed++
                val msg = aiResult.exceptionOrNull()?.localizedMessage ?: "AI service unreachable"
                lastErr = msg
                offlineDao.upsertPendingPrediction(item.copy(status = "FAILED", errorMessage = msg))
                continue
            }

            val saved = savePredictionToFirestore(
                uid = uid,
                input = input,
                predictedGpa = output.predictedGpa,
                confidencePct = output.confidencePct,
                riskLevel = output.riskLevel.wireValue,
                prognosisSummary = output.prognosisSummary,
                actionPlan = output.actionPlan,
                counterfactualTip = output.counterfactualTip,
                source = "SYNCED_OFFLINE_QUEUE"
            )
            if (saved.isSuccess) {
                offlineDao.deletePendingPrediction(item.id)
                synced++
            } else {
                failed++
                val msg = saved.exceptionOrNull()?.localizedMessage ?: "Cloud sync failed"
                lastErr = msg
                offlineDao.upsertPendingPrediction(item.copy(status = "FAILED", errorMessage = msg))
            }
        }

        if (synced > 0) {
            prefs.updateLastSync(System.currentTimeMillis())
        }
        return SyncReport(synced, failed, lastErr)
    }

    suspend fun deletePendingPrediction(pendingId: String) {
        offlineDao.deletePendingPrediction(pendingId)
    }

    private suspend fun savePredictionToFirestore(
        uid: String,
        input: PredictionInput,
        predictedGpa: Double,
        confidencePct: Int,
        riskLevel: String,
        prognosisSummary: String,
        actionPlan: List<String>,
        counterfactualTip: String?,
        source: String
    ): Result<GpaPrediction> {
        var docPath = "users/$uid/predictions"
        return try {
            val ref = db.collection("users").document(uid).collection("predictions").document()
            docPath = ref.path
            val safeSemester = input.semesterName.trim().take(60).ifBlank { "Fall 2026" }
            val safeSummary = prognosisSummary.trim().take(1500).ifBlank { "AI prognosis generated." }
            val safePlan = actionPlan.take(8).map { it.take(240) }
            val safeTip = counterfactualTip?.trim()?.take(500)?.takeIf { it.isNotBlank() }

            val payload = mapOf(
                "userId" to uid,
                "semesterName" to safeSemester,
                "studyHoursPerWeek" to input.studyHoursPerWeek.coerceIn(0.0, 100.0),
                "attendancePct" to input.attendancePct.coerceIn(0.0, 100.0),
                "assignmentAvg" to input.assignmentAvg.coerceIn(0.0, 100.0),
                "midtermAvg" to input.midtermAvg.coerceIn(0.0, 100.0),
                "previousGpa" to input.previousGpa.coerceIn(0.0, 4.0),
                "creditHours" to input.creditHours.toLong().coerceIn(1L, 36L),
                "sleepHours" to input.sleepHours.coerceIn(0.0, 16.0),
                "predictedGpa" to predictedGpa.coerceIn(0.0, 4.0),
                "confidencePct" to confidencePct.toLong().coerceIn(0L, 100L),
                "riskLevel" to riskLevel,
                "prognosisSummary" to safeSummary,
                "actionPlan" to safePlan,
                "counterfactualTip" to safeTip,
                "source" to source,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            ref.set(payload).await()
            val model = GpaPrediction(
                id = ref.id,
                userId = uid,
                semesterName = safeSemester,
                studyHoursPerWeek = input.studyHoursPerWeek,
                attendancePct = input.attendancePct,
                assignmentAvg = input.assignmentAvg,
                midtermAvg = input.midtermAvg,
                previousGpa = input.previousGpa,
                creditHours = input.creditHours.toLong(),
                sleepHours = input.sleepHours,
                predictedGpa = predictedGpa,
                confidencePct = confidencePct.toLong(),
                riskLevel = riskLevel,
                prognosisSummary = safeSummary,
                actionPlan = safePlan,
                counterfactualTip = safeTip,
                source = source,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            Result.success(model)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deletePrediction(predictionId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/predictions/$predictionId"
        return try {
            val uid = requireUserId()
            require(predictionId.isNotBlank())
            val ref = db.collection("users").document(uid).collection("predictions").document(predictionId)
            docPath = ref.path
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Blogs (Global Admin Collection + Personal Editorial Workspace + Offline Room) ---
    fun observeGlobalBlogs(includeUnpublishedForAdmin: Boolean): Flow<List<BlogArticle>> = flow {
        val path = "blogs"
        val query = if (includeUnpublishedForAdmin && isVerifiedBootstrapAdmin()) {
            db.collection("blogs")
        } else {
            db.collection("blogs").whereEqualTo("published", true)
        }
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        BlogArticle::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.popularScore }
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val toLog = fsErr ?: (error as? Exception)
                    if (toLog != null && auth.currentUser != null) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeCustomBlogs(userId: String): Flow<List<BlogArticle>> = flow {
        val path = "users/$userId/custom_blogs"
        emitAll(
            db.collection("users").document(userId).collection("custom_blogs")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        BlogArticle::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val fsErr = unwrapFirestoreError(error)
                    if (fsErr == null && error is CancellationException) throw error
                    val toLog = fsErr ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeOfflineArticles(): Flow<List<BlogArticle>> =
        offlineDao.observeOfflineArticles().map { entities ->
            entities.map { entity ->
                BlogArticle(
                    id = entity.id,
                    authorId = "offline_cache",
                    authorName = entity.authorName,
                    authorRole = entity.authorRole,
                    title = entity.title,
                    subtitle = entity.subtitle,
                    category = entity.category,
                    tags = entity.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    coverKey = entity.coverKey,
                    readMinutes = entity.readMinutes,
                    contentMarkdown = entity.contentMarkdown,
                    published = true,
                    featured = false,
                    popularScore = 100L,
                    sampleContent = entity.sampleContent,
                    publishedDateLabel = entity.publishedDateLabel
                )
            }
        }

    fun observeBookmarkIds(): Flow<Set<String>> =
        offlineDao.observeBookmarks().map { list -> list.map { it.articleId }.toSet() }

    suspend fun toggleBookmark(articleId: String, currentlyBookmarked: Boolean) {
        if (currentlyBookmarked) {
            offlineDao.removeBookmark(articleId)
        } else {
            offlineDao.addBookmark(BookmarkedArticleEntity(articleId, System.currentTimeMillis()))
        }
    }

    suspend fun saveArticleOffline(article: BlogArticle) {
        offlineDao.saveOfflineArticle(
            OfflineArticleEntity(
                id = article.id,
                authorName = article.authorName,
                authorRole = article.authorRole,
                title = article.title,
                subtitle = article.subtitle,
                category = article.category,
                tagsCsv = article.tags.joinToString(","),
                coverKey = article.coverKey,
                readMinutes = article.readMinutes,
                contentMarkdown = article.contentMarkdown,
                sampleContent = article.sampleContent,
                publishedDateLabel = article.publishedDateLabel,
                savedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeArticleOffline(articleId: String) {
        offlineDao.deleteOfflineArticle(articleId)
    }

    suspend fun saveBlogPost(
        existingId: String?,
        publishGloballyIfAdmin: Boolean,
        title: String,
        subtitle: String,
        authorName: String,
        authorRole: String,
        category: String,
        tags: List<String>,
        coverKey: String,
        readMinutes: Long,
        contentMarkdown: String,
        published: Boolean,
        featured: Boolean
    ): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val useGlobal = publishGloballyIfAdmin && isVerifiedBootstrapAdmin()
        val collection = if (useGlobal) {
            db.collection("blogs")
        } else {
            db.collection("users").document(uid).collection("custom_blogs")
        }

        val dateLabel = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
        val safeTags = tags.map { it.trim() }.filter { it.isNotEmpty() }.take(10)
        val safeTitle = title.trim().take(140)
        val safeSubtitle = subtitle.trim().take(240)
        val safeContent = contentMarkdown.trim().take(12000)

        if (safeTitle.length < 4 || safeSubtitle.length < 4 || safeContent.length < 20) {
            return Result.failure(
                IllegalArgumentException("Title (4+), subtitle (4+), and article body (20+ chars) are required.")
            )
        }

        val docRef = if (existingId.isNullOrBlank()) collection.document() else collection.document(existingId)
        val docPath = docRef.path

        return try {
            if (existingId.isNullOrBlank()) {
                val payload = mapOf(
                    "authorId" to uid,
                    "authorName" to authorName.trim().take(80).ifBlank { "Academic Admin" },
                    "authorRole" to authorRole.trim().take(60).ifBlank { "Editorial Lead" },
                    "title" to safeTitle,
                    "subtitle" to safeSubtitle,
                    "category" to category.trim().take(50).ifBlank { "Academic Success" },
                    "tags" to safeTags,
                    "coverKey" to coverKey.trim().take(300).ifBlank { "img_blog_ai_education" },
                    "readMinutes" to readMinutes.coerceIn(1L, 60L),
                    "contentMarkdown" to safeContent,
                    "published" to published,
                    "featured" to featured,
                    "popularScore" to if (featured) 950L else 600L,
                    "sampleContent" to false,
                    "publishedDateLabel" to dateLabel,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val payload = mapOf(
                    "authorName" to authorName.trim().take(80).ifBlank { "Academic Admin" },
                    "authorRole" to authorRole.trim().take(60).ifBlank { "Editorial Lead" },
                    "title" to safeTitle,
                    "subtitle" to safeSubtitle,
                    "category" to category.trim().take(50).ifBlank { "Academic Success" },
                    "tags" to safeTags,
                    "coverKey" to coverKey.trim().take(300).ifBlank { "img_blog_ai_education" },
                    "readMinutes" to readMinutes.coerceIn(1L, 60L),
                    "contentMarkdown" to safeContent,
                    "published" to published,
                    "featured" to featured,
                    "popularScore" to if (featured) 950L else 600L,
                    "sampleContent" to false,
                    "publishedDateLabel" to dateLabel,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleBlogPublishState(
        blogId: String,
        isGlobalCollection: Boolean,
        newPublished: Boolean
    ): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val ref = if (isGlobalCollection) {
            db.collection("blogs").document(blogId)
        } else {
            db.collection("users").document(uid).collection("custom_blogs").document(blogId)
        }
        return try {
            ref.update(
                mapOf(
                    "published" to newPublished,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun deleteBlogPost(blogId: String, isGlobalCollection: Boolean): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val ref = if (isGlobalCollection) {
            db.collection("blogs").document(blogId)
        } else {
            db.collection("users").document(uid).collection("custom_blogs").document(blogId)
        }
        return try {
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            Result.failure(e)
        }
    }

    fun getBuiltInSampleBlogs(): List<BlogArticle> = SampleBlogs.articles

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
}
