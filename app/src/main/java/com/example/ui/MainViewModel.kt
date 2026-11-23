package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.BLOG_CATEGORIES
import com.example.data.model.BlogArticle
import com.example.data.model.BlogUiItem
import com.example.data.model.CourseRecord
import com.example.data.model.GpaPrediction
import com.example.data.model.PendingPredictionItem
import com.example.data.model.PredictionInput
import com.example.data.model.UserProfile
import com.example.data.network.ConnectivityObserver
import com.example.data.repository.AcademicRepository
import com.example.data.repository.PredictionOutcome
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PredictionRunState {
    data object Idle : PredictionRunState
    data object Running : PredictionRunState
    data class Success(val prediction: GpaPrediction) : PredictionRunState
    data class QueuedOffline(val pendingItem: PendingPredictionItem) : PredictionRunState
    data class Error(val message: String) : PredictionRunState
}

data class SyncStatusUi(
    val isSyncing: Boolean = false,
    val lastMessage: String? = null,
    val lastSyncMillis: Long = 0L
)

data class MainDashboardState(
    val currentUserId: String,
    val userEmail: String,
    val profile: UserProfile,
    val courses: List<CourseRecord>,
    val predictions: List<GpaPrediction>,
    val pendingPredictions: List<PendingPredictionItem>,
    val blogs: List<BlogUiItem>,
    val offlineArticlesCount: Int,
    val isOnline: Boolean,
    val isSimulatedOffline: Boolean,
    val isAdminActive: Boolean,
    val isBootstrapAdmin: Boolean,
    val darkTheme: Boolean,
    val installBannerDismissed: Boolean,
    val companionInstalled: Boolean,
    val syncStatus: SyncStatusUi,
    val cloudError: String? = null
)

class MainViewModel(
    private val repository: AcademicRepository,
    private val prefs: UserPreferencesRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val currentUserId: String,
    private val userDisplayName: String,
    private val userEmail: String
) : ViewModel() {

    private val _predictionRunState = MutableStateFlow<PredictionRunState>(PredictionRunState.Idle)
    val predictionRunState: StateFlow<PredictionRunState> = _predictionRunState.asStateFlow()

    private val _syncStatus = MutableStateFlow(SyncStatusUi())
    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private val _cloudError = MutableStateFlow<String?>(null)

    private val liveProfileFlow = repository.observeProfile(currentUserId)
        .catch { err ->
            val fsErr = unwrapFirestoreError(err)
            if (fsErr == null && err is CancellationException) throw err
            _cloudError.value = (fsErr ?: err).localizedMessage ?: "Cloud profile unavailable offline"
            emit(null)
        }

    private val cachedProfileFlow = repository.observeCachedProfile(currentUserId)

    private val effectiveProfileFlow = combine(liveProfileFlow, cachedProfileFlow) { live, cached ->
        live ?: cached ?: UserProfile(
            id = currentUserId,
            userId = currentUserId,
            displayName = userDisplayName.ifBlank { "Academic Scholar" },
            role = if (repository.isVerifiedBootstrapAdmin()) "ADMIN" else "STUDENT"
        )
    }

    private val liveCoursesFlow = repository.observeCourses(currentUserId)
        .catch { err ->
            val fsErr = unwrapFirestoreError(err)
            if (fsErr == null && err is CancellationException) throw err
            emit(emptyList())
        }

    private val cachedCoursesFlow = repository.observeCachedCourses(currentUserId)

    private val effectiveCoursesFlow = combine(liveCoursesFlow, cachedCoursesFlow) { live, cached ->
        live.ifEmpty { cached }
    }

    private val livePredictionsFlow = repository.observePredictions(currentUserId)
        .catch { err ->
            val fsErr = unwrapFirestoreError(err)
            if (fsErr == null && err is CancellationException) throw err
            emit(emptyList())
        }

    private val cachedPredictionsFlow = repository.observeCachedPredictions(currentUserId)

    private val effectivePredictionsFlow = combine(livePredictionsFlow, cachedPredictionsFlow) { live, cached ->
        live.ifEmpty { cached }
    }

    private val globalBlogsFlow = repository.observeGlobalBlogs(
        includeUnpublishedForAdmin = repository.isVerifiedBootstrapAdmin()
    ).catch { err ->
        val fsErr = unwrapFirestoreError(err)
        if (fsErr == null && err is CancellationException) throw err
        emit(emptyList())
    }

    private val customBlogsFlow = repository.observeCustomBlogs(currentUserId)
        .catch { err ->
            val fsErr = unwrapFirestoreError(err)
            if (fsErr == null && err is CancellationException) throw err
            emit(emptyList())
        }

    private val offlineArticlesFlow = repository.observeOfflineArticles()
    private val bookmarksFlow = repository.observeBookmarkIds()
    private val pendingPredictionsFlow = repository.observePendingPredictions(currentUserId)

    private val effectiveOnlineFlow = combine(
        connectivityObserver.networkOnlineFlow,
        prefs.offlineSimulationMode
    ) { realOnline, simulatedOffline ->
        Pair(realOnline && !simulatedOffline, simulatedOffline)
    }.distinctUntilChanged()

    private val combinedBlogsFlow = combine(
        globalBlogsFlow,
        customBlogsFlow,
        offlineArticlesFlow,
        bookmarksFlow
    ) { globalBlogs, customBlogs, offlineSaved, bookmarkIds ->
        val offlineIds = offlineSaved.map { it.id }.toSet()
        val sampleItems = repository.getBuiltInSampleBlogs().map { article ->
            BlogUiItem(
                article = article,
                bookmarked = article.id in bookmarkIds,
                savedOffline = article.id in offlineIds,
                originScope = "SAMPLE"
            )
        }
        val globalItems = globalBlogs.map { article ->
            BlogUiItem(
                article = article,
                bookmarked = article.id in bookmarkIds,
                savedOffline = article.id in offlineIds,
                originScope = "GLOBAL_ADMIN"
            )
        }
        val customItems = customBlogs.map { article ->
            BlogUiItem(
                article = article,
                bookmarked = article.id in bookmarkIds,
                savedOffline = article.id in offlineIds,
                originScope = "LOCAL_WORKSPACE"
            )
        }
        val combinedIds = (sampleItems + globalItems + customItems).map { it.article.id }.toSet()
        val offlineOnlyExtras = offlineSaved
            .filter { it.id !in combinedIds }
            .map { article ->
                BlogUiItem(
                    article = article,
                    bookmarked = article.id in bookmarkIds,
                    savedOffline = true,
                    originScope = if (article.sampleContent) "SAMPLE" else "GLOBAL_ADMIN"
                )
            }
        // Admin/custom posts first, then sample editorial articles
        customItems + globalItems + offlineOnlyExtras + sampleItems
    }

    private val academicDataFlow = combine(
        effectiveProfileFlow,
        effectiveCoursesFlow,
        effectivePredictionsFlow,
        pendingPredictionsFlow,
        combinedBlogsFlow
    ) { profile, courses, predictions, pending, blogs ->
        AcademicBundle(profile, courses, predictions, pending, blogs)
    }

    private val settingsFlow = combine(
        effectiveOnlineFlow,
        prefs.darkThemeEnabled,
        prefs.installBannerDismissed,
        prefs.companionInstalled,
        prefs.adminWorkspaceEnabled,
        prefs.lastSyncMillis
    ) { arr ->
        val onlinePair = arr[0] as Pair<*, *>
        val isOnline = onlinePair.first as Boolean
        val isSimulatedOffline = onlinePair.second as Boolean
        SettingsBundle(
            isOnline = isOnline,
            isSimulatedOffline = isSimulatedOffline,
            darkTheme = arr[1] as Boolean,
            installBannerDismissed = arr[2] as Boolean,
            companionInstalled = arr[3] as Boolean,
            adminWorkspace = arr[4] as Boolean,
            lastSyncMillis = arr[5] as Long
        )
    }

    val dashboardState: StateFlow<MainDashboardState> = combine(
        academicDataFlow,
        settingsFlow,
        offlineArticlesFlow,
        _syncStatus,
        _cloudError
    ) { academic, settings, offlineSaved, syncUi, cloudErr ->
        val isBootstrapAdmin = repository.isVerifiedBootstrapAdmin()
        val isAdminActive = isBootstrapAdmin || settings.adminWorkspace ||
            academic.profile.role.equals("ADMIN", ignoreCase = true)
        MainDashboardState(
            currentUserId = currentUserId,
            userEmail = userEmail,
            profile = academic.profile,
            courses = academic.courses,
            predictions = academic.predictions,
            pendingPredictions = academic.pending,
            blogs = academic.blogs,
            offlineArticlesCount = offlineSaved.size,
            isOnline = settings.isOnline,
            isSimulatedOffline = settings.isSimulatedOffline,
            isAdminActive = isAdminActive,
            isBootstrapAdmin = isBootstrapAdmin,
            darkTheme = settings.darkTheme,
            installBannerDismissed = settings.installBannerDismissed,
            companionInstalled = settings.companionInstalled,
            syncStatus = syncUi.copy(lastSyncMillis = settings.lastSyncMillis),
            cloudError = cloudErr
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MainDashboardState(
            currentUserId = currentUserId,
            userEmail = userEmail,
            profile = UserProfile(
                id = currentUserId,
                userId = currentUserId,
                displayName = userDisplayName.ifBlank { "Academic Scholar" }
            ),
            courses = emptyList(),
            predictions = emptyList(),
            pendingPredictions = emptyList(),
            blogs = repository.getBuiltInSampleBlogs().map {
                BlogUiItem(it, bookmarked = false, savedOffline = false, originScope = "SAMPLE")
            },
            offlineArticlesCount = 0,
            isOnline = connectivityObserver.isCurrentlyOnline(),
            isSimulatedOffline = false,
            isAdminActive = repository.isVerifiedBootstrapAdmin(),
            isBootstrapAdmin = repository.isVerifiedBootstrapAdmin(),
            darkTheme = false,
            installBannerDismissed = false,
            companionInstalled = false,
            syncStatus = SyncStatusUi()
        )
    )

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(userDisplayName.ifBlank { "Academic Scholar" })
        }

        // Auto-sync queued offline predictions whenever connectivity returns
        viewModelScope.launch {
            effectiveOnlineFlow.collect { (online, _) ->
                if (online) {
                    syncPendingQueue(silentIfEmpty = true)
                }
            }
        }
    }

    fun runPrediction(input: PredictionInput) {
        val snapshot = dashboardState.value
        viewModelScope.launch {
            _predictionRunState.value = PredictionRunState.Running
            val result = repository.runOrQueuePrediction(
                input = input,
                courses = snapshot.courses,
                isOnline = snapshot.isOnline
            )
            result.fold(
                onSuccess = { outcome ->
                    _predictionRunState.value = when (outcome) {
                        is PredictionOutcome.Completed -> {
                            prefs.updateLastSync(System.currentTimeMillis())
                            PredictionRunState.Success(outcome.prediction)
                        }
                        is PredictionOutcome.QueuedOffline -> {
                            PredictionRunState.QueuedOffline(outcome.pendingItem)
                        }
                    }
                },
                onFailure = { error ->
                    val message = error.localizedMessage ?: "Unable to compute AI prediction."
                    _predictionRunState.value = PredictionRunState.Error(message)
                }
            )
        }
    }

    fun resetPredictionRunState() {
        _predictionRunState.value = PredictionRunState.Idle
    }

    fun syncPendingQueue(silentIfEmpty: Boolean = false) {
        val snapshot = dashboardState.value
        if (!snapshot.isOnline) {
            if (!silentIfEmpty) {
                _bannerMessage.value = "Offline: connect to the internet to synchronize queued predictions."
            }
            return
        }
        if (snapshot.pendingPredictions.isEmpty()) {
            if (!silentIfEmpty) {
                viewModelScope.launch {
                    prefs.updateLastSync(System.currentTimeMillis())
                    _syncStatus.value = _syncStatus.value.copy(
                        isSyncing = false,
                        lastMessage = "All academic records are synchronized."
                    )
                }
            }
            return
        }

        viewModelScope.launch {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true, lastMessage = "Synchronizing queued requests...")
            val report = repository.syncPendingPredictions(snapshot.courses)
            val summary = when {
                report.syncedCount > 0 && report.failedCount == 0 ->
                    "Synced ${report.syncedCount} offline prediction(s) with Gemini AI."
                report.syncedCount > 0 && report.failedCount > 0 ->
                    "Synced ${report.syncedCount}; ${report.failedCount} failed (${report.lastError ?: "retry"})."
                report.failedCount > 0 ->
                    "Sync failed for ${report.failedCount} item(s): ${report.lastError ?: "Check connection"}."
                else -> "All records up to date."
            }
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                lastMessage = summary
            )
            _bannerMessage.value = summary
        }
    }

    fun deletePendingPrediction(id: String) {
        viewModelScope.launch {
            repository.deletePendingPrediction(id)
        }
    }

    fun deletePrediction(id: String) {
        viewModelScope.launch {
            repository.deletePrediction(id).onFailure {
                _bannerMessage.value = it.localizedMessage ?: "Failed to delete prediction."
            }
        }
    }

    fun addCourse(
        code: String,
        title: String,
        credits: Long,
        currentScore: Double,
        attendancePct: Double,
        targetGrade: String,
        difficulty: String
    ) {
        viewModelScope.launch {
            repository.addCourse(code, title, credits, currentScore, attendancePct, targetGrade, difficulty)
                .onSuccess { _bannerMessage.value = "Course $code added to semester analytics." }
                .onFailure { _bannerMessage.value = it.localizedMessage ?: "Could not add course." }
        }
    }

    fun loadDemoSemesterCourses() {
        viewModelScope.launch {
            val samples = listOf(
                Triple("CS 320", "Machine Learning Systems", 4L) to listOf(91.5, 95.0, "A", "HIGH"),
                Triple("MATH 240", "Linear Algebra & Optimization", 4L) to listOf(87.0, 91.0, "A-", "HIGH"),
                Triple("STAT 310", "Applied Probability", 3L) to listOf(93.2, 96.0, "A", "MEDIUM"),
                Triple("ENG 205", "Technical Communication", 3L) to listOf(94.8, 98.0, "A", "LOW")
            )
            samples.forEach { (meta, metrics) ->
                repository.addCourse(
                    code = meta.first,
                    title = meta.second,
                    credits = meta.third,
                    currentScore = metrics[0] as Double,
                    attendancePct = metrics[1] as Double,
                    targetGrade = metrics[2] as String,
                    difficulty = metrics[3] as String
                )
            }
            _bannerMessage.value = "Loaded 4 semester courses for GPA analytics."
        }
    }

    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            repository.deleteCourse(courseId).onFailure {
                _bannerMessage.value = it.localizedMessage ?: "Could not delete course."
            }
        }
    }

    fun updateProfile(
        displayName: String,
        major: String,
        university: String,
        semesterLabel: String,
        currentGpa: Double,
        targetGpa: Double,
        completedCredits: Long,
        weeklyStudyGoalHours: Long
    ) {
        viewModelScope.launch {
            repository.updateProfile(
                displayName = displayName,
                major = major,
                university = university,
                semesterLabel = semesterLabel,
                currentGpa = currentGpa,
                targetGpa = targetGpa,
                completedCredits = completedCredits,
                weeklyStudyGoalHours = weeklyStudyGoalHours
            ).onSuccess {
                _bannerMessage.value = "Academic profile updated."
            }.onFailure {
                _bannerMessage.value = it.localizedMessage ?: "Profile update failed."
            }
        }
    }

    fun toggleBookmark(item: BlogUiItem) {
        viewModelScope.launch {
            repository.toggleBookmark(item.article.id, item.bookmarked)
        }
    }

    fun toggleOfflineSave(item: BlogUiItem) {
        viewModelScope.launch {
            if (item.savedOffline) {
                repository.removeArticleOffline(item.article.id)
                _bannerMessage.value = "Removed \"${item.article.title}\" from offline storage."
            } else {
                repository.saveArticleOffline(item.article)
                _bannerMessage.value = "Downloaded for offline reading."
            }
        }
    }

    fun saveAdminBlogPost(
        existingId: String?,
        originScope: String,
        title: String,
        subtitle: String,
        authorName: String,
        authorRole: String,
        category: String,
        tagsRaw: String,
        coverKey: String,
        readMinutes: Long,
        contentMarkdown: String,
        published: Boolean,
        featured: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val tags = tagsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val publishGlobal = originScope == "GLOBAL_ADMIN" ||
                (existingId == null && repository.isVerifiedBootstrapAdmin())
            val res = repository.saveBlogPost(
                existingId = existingId,
                publishGloballyIfAdmin = publishGlobal,
                title = title,
                subtitle = subtitle,
                authorName = authorName,
                authorRole = authorRole,
                category = category,
                tags = tags,
                coverKey = coverKey,
                readMinutes = readMinutes,
                contentMarkdown = contentMarkdown,
                published = published,
                featured = featured
            )
            res.onSuccess {
                _bannerMessage.value = if (existingId == null) "Article created." else "Article updated."
                onComplete(true)
            }.onFailure {
                _bannerMessage.value = it.localizedMessage ?: "Failed to save article."
                onComplete(false)
            }
        }
    }

    fun togglePublishBlog(item: BlogUiItem) {
        if (item.originScope == "SAMPLE") return
        viewModelScope.launch {
            val isGlobal = item.originScope == "GLOBAL_ADMIN"
            repository.toggleBlogPublishState(
                blogId = item.article.id,
                isGlobalCollection = isGlobal,
                newPublished = !item.article.published
            ).onFailure {
                _bannerMessage.value = it.localizedMessage ?: "Could not update publish status."
            }
        }
    }

    fun deleteBlogPost(item: BlogUiItem) {
        if (item.originScope == "SAMPLE") return
        viewModelScope.launch {
            val isGlobal = item.originScope == "GLOBAL_ADMIN"
            repository.deleteBlogPost(item.article.id, isGlobal)
                .onSuccess { _bannerMessage.value = "Article deleted." }
                .onFailure { _bannerMessage.value = it.localizedMessage ?: "Could not delete article." }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkTheme(enabled) }
    }

    fun dismissInstallBanner() {
        viewModelScope.launch { prefs.dismissInstallBanner() }
    }

    fun markCompanionInstalled() {
        viewModelScope.launch {
            prefs.setCompanionInstalled(true)
            _bannerMessage.value = "GPA Predictor installed for instant offline access."
        }
    }

    fun setOfflineSimulation(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setOfflineSimulation(enabled)
            _bannerMessage.value = if (enabled) {
                "Offline simulation active — saved data & offline articles remain available."
            } else {
                "Offline simulation disabled — checking network connectivity."
            }
        }
    }

    fun setAdminWorkspace(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setAdminWorkspace(enabled)
            _bannerMessage.value = if (enabled) {
                "Admin Editorial Studio enabled."
            } else {
                "Switched to Student view."
            }
        }
    }

    fun clearBannerMessage() {
        _bannerMessage.value = null
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()

    private data class AcademicBundle(
        val profile: UserProfile,
        val courses: List<CourseRecord>,
        val predictions: List<GpaPrediction>,
        val pending: List<PendingPredictionItem>,
        val blogs: List<BlogUiItem>
    )

    private data class SettingsBundle(
        val isOnline: Boolean,
        val isSimulatedOffline: Boolean,
        val darkTheme: Boolean,
        val installBannerDismissed: Boolean,
        val companionInstalled: Boolean,
        val adminWorkspace: Boolean,
        val lastSyncMillis: Long
    )
}
