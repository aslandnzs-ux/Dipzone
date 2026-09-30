package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DipzonDao {

    // === SERIES ===
    @Query("SELECT * FROM series WHERE isPublished = 1 ORDER BY isTrending DESC, likesCount DESC")
    fun getAllPublishedSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE isPublished = 1 AND isTrending = 1")
    fun getTrendingSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE isPublished = 1 AND isNew = 1")
    fun getNewSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE isPublished = 1 AND category = :category")
    fun getSeriesByCategory(category: String): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE id = :seriesId LIMIT 1")
    fun getSeriesById(seriesId: String): Flow<SeriesEntity?>

    @Query("SELECT * FROM series WHERE id = :seriesId LIMIT 1")
    suspend fun getSeriesByIdDirect(seriesId: String): SeriesEntity?

    @Query("""
        SELECT * FROM series 
        WHERE isPublished = 1 AND (
            title LIKE '%' || :query || '%' OR 
            description LIKE '%' || :query || '%' OR 
            cast_members LIKE '%' || :query || '%' OR 
            director LIKE '%' || :query || '%' OR 
            category LIKE '%' || :query || '%'
        )
    """)
    fun searchSeries(query: String): Flow<List<SeriesEntity>>

    // === EPISODES ===
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getEpisodesForSeries(seriesId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    suspend fun getEpisodesForSeriesDirect(seriesId: String): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE id = :episodeId LIMIT 1")
    suspend fun getEpisodeById(episodeId: String): EpisodeEntity?

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId AND seasonNumber = :season AND episodeNumber = :episode LIMIT 1")
    suspend fun getEpisodeByNumber(seriesId: String, season: Int, episode: Int): EpisodeEntity?

    // === WATCH PROGRESS & HISTORY ===
    @Query("SELECT * FROM watch_progress ORDER BY updatedAt DESC")
    fun getAllWatchProgress(): Flow<List<WatchProgressEntity>>

    @Query("SELECT * FROM watch_progress WHERE seriesId = :seriesId LIMIT 1")
    fun getWatchProgressForSeries(seriesId: String): Flow<WatchProgressEntity?>

    @Query("SELECT * FROM watch_progress WHERE seriesId = :seriesId LIMIT 1")
    suspend fun getWatchProgressForSeriesDirect(seriesId: String): WatchProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchProgress(progress: WatchProgressEntity)

    @Query("DELETE FROM watch_progress WHERE seriesId = :seriesId")
    suspend fun deleteWatchProgress(seriesId: String)

    @Query("DELETE FROM watch_progress")
    suspend fun clearWatchHistory()

    // === SAVED ITEMS (MY LIST) ===
    @Query("SELECT * FROM saved_items ORDER BY savedAt DESC")
    fun getAllSavedItems(): Flow<List<SavedItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_items WHERE seriesId = :seriesId)")
    fun isSeriesSaved(seriesId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedItem(item: SavedItemEntity)

    @Query("DELETE FROM saved_items WHERE seriesId = :seriesId")
    suspend fun deleteSavedItem(seriesId: String)

    // === LIKES ===
    @Query("SELECT EXISTS(SELECT 1 FROM liked_items WHERE seriesId = :seriesId)")
    fun isSeriesLiked(seriesId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikedItem(item: LikedItemEntity)

    @Query("DELETE FROM liked_items WHERE seriesId = :seriesId")
    suspend fun deleteLikedItem(seriesId: String)

    @Query("UPDATE series SET likesCount = likesCount + :delta WHERE id = :seriesId")
    suspend fun updateSeriesLikesCount(seriesId: String, delta: Int)

    // === COMMENTS ===
    @Query("SELECT * FROM comments WHERE seriesId = :seriesId ORDER BY createdAt DESC")
    fun getCommentsForSeries(seriesId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("UPDATE comments SET likesCount = likesCount + 1 WHERE id = :commentId")
    suspend fun upvoteComment(commentId: String)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: String)

    // === USER PROFILE ===
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    // === ADMIN & ANALYTICS ===
    @Query("SELECT * FROM series ORDER BY year DESC, isPublished DESC")
    fun getAllSeriesAdmin(): Flow<List<SeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSeries(series: SeriesEntity)

    @Query("UPDATE series SET isPublished = :isPublished WHERE id = :seriesId")
    suspend fun setSeriesPublishedStatus(seriesId: String, isPublished: Boolean)

    @Query("DELETE FROM series WHERE id = :seriesId")
    suspend fun deleteSeries(seriesId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEpisode(episode: EpisodeEntity)

    @Query("DELETE FROM episodes WHERE id = :episodeId")
    suspend fun deleteEpisode(episodeId: String)

    @Insert
    suspend fun insertAnalyticsEvent(event: AnalyticsEventEntity)

    @Query("SELECT COUNT(*) FROM analytics_events")
    suspend fun getTotalAnalyticsEventsCount(): Long

    @Query("SELECT COUNT(*) FROM series")
    suspend fun getTotalSeriesCount(): Int

    @Query("SELECT COUNT(*) FROM episodes")
    suspend fun getTotalEpisodesCount(): Int

    @Query("SELECT COUNT(*) FROM comments")
    suspend fun getTotalCommentsCount(): Int

    // Batch initial population
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllSeries(series: List<SeriesEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllEpisodes(episodes: List<EpisodeEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllComments(comments: List<CommentEntity>)
}
