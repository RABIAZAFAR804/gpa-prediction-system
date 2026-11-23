package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineDao {
    // Offline saved articles
    @Query("SELECT * FROM offline_articles ORDER BY savedAtMillis DESC")
    fun observeOfflineArticles(): Flow<List<OfflineArticleEntity>>

    @Query("SELECT * FROM offline_articles WHERE id = :id LIMIT 1")
    suspend fun getOfflineArticle(id: String): OfflineArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOfflineArticle(article: OfflineArticleEntity)

    @Query("DELETE FROM offline_articles WHERE id = :id")
    suspend fun deleteOfflineArticle(id: String)

    // Bookmarks
    @Query("SELECT * FROM bookmarked_articles")
    fun observeBookmarks(): Flow<List<BookmarkedArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkedArticleEntity)

    @Query("DELETE FROM bookmarked_articles WHERE articleId = :articleId")
    suspend fun removeBookmark(articleId: String)

    // Pending offline predictions queue
    @Query("SELECT * FROM pending_predictions WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observePendingPredictions(userId: String): Flow<List<PendingPredictionEntity>>

    @Query("SELECT * FROM pending_predictions WHERE userId = :userId ORDER BY createdAtMillis ASC")
    suspend fun getPendingPredictionsOnce(userId: String): List<PendingPredictionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPendingPrediction(item: PendingPredictionEntity)

    @Query("DELETE FROM pending_predictions WHERE id = :id")
    suspend fun deletePendingPrediction(id: String)

    // Cached Profile
    @Query("SELECT * FROM cached_profiles WHERE userId = :userId LIMIT 1")
    fun observeCachedProfile(userId: String): Flow<CachedProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedProfile(profile: CachedProfileEntity)

    // Cached Courses
    @Query("SELECT * FROM cached_courses WHERE userId = :userId ORDER BY code ASC")
    fun observeCachedCourses(userId: String): Flow<List<CachedCourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedCourses(courses: List<CachedCourseEntity>)

    @Query("DELETE FROM cached_courses WHERE userId = :userId")
    suspend fun clearCachedCoursesForUser(userId: String)

    // Cached Predictions
    @Query("SELECT * FROM cached_predictions WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeCachedPredictions(userId: String): Flow<List<CachedPredictionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedPredictions(predictions: List<CachedPredictionEntity>)

    @Query("DELETE FROM cached_predictions WHERE userId = :userId")
    suspend fun clearCachedPredictionsForUser(userId: String)
}
