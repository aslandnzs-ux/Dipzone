package com.example.data.repository

import com.example.data.local.DipzonDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class DipzonRepository(private val dao: DipzonDao) {

    val allSeries: Flow<List<SeriesEntity>> = dao.getAllPublishedSeries()
    val trendingSeries: Flow<List<SeriesEntity>> = dao.getTrendingSeries()
    val newSeries: Flow<List<SeriesEntity>> = dao.getNewSeries()
    val allWatchProgress: Flow<List<WatchProgressEntity>> = dao.getAllWatchProgress()
    val savedItems: Flow<List<SavedItemEntity>> = dao.getAllSavedItems()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val adminAllSeries: Flow<List<SeriesEntity>> = dao.getAllSeriesAdmin()

    fun getSeriesByCategory(category: String): Flow<List<SeriesEntity>> =
        dao.getSeriesByCategory(category)

    fun getSeriesById(seriesId: String): Flow<SeriesEntity?> =
        dao.getSeriesById(seriesId)

    suspend fun getSeriesByIdDirect(seriesId: String): SeriesEntity? =
        dao.getSeriesByIdDirect(seriesId)

    fun searchSeries(query: String): Flow<List<SeriesEntity>> =
        dao.searchSeries(query)

    fun getEpisodesForSeries(seriesId: String): Flow<List<EpisodeEntity>> =
        dao.getEpisodesForSeries(seriesId)

    suspend fun getEpisodesForSeriesDirect(seriesId: String): List<EpisodeEntity> =
        dao.getEpisodesForSeriesDirect(seriesId)

    suspend fun getEpisodeById(episodeId: String): EpisodeEntity? =
        dao.getEpisodeById(episodeId)

    suspend fun getEpisodeByNumber(seriesId: String, season: Int, episode: Int): EpisodeEntity? =
        dao.getEpisodeByNumber(seriesId, season, episode)

    fun getWatchProgressForSeries(seriesId: String): Flow<WatchProgressEntity?> =
        dao.getWatchProgressForSeries(seriesId)

    suspend fun getWatchProgressForSeriesDirect(seriesId: String): WatchProgressEntity? =
        dao.getWatchProgressForSeriesDirect(seriesId)

    suspend fun saveWatchProgress(progress: WatchProgressEntity) =
        dao.upsertWatchProgress(progress)

    suspend fun clearWatchHistory() =
        dao.clearWatchHistory()

    fun isSeriesSaved(seriesId: String): Flow<Boolean> =
        dao.isSeriesSaved(seriesId)

    suspend fun toggleSaveSeries(seriesId: String, currentlySaved: Boolean) {
        if (currentlySaved) {
            dao.deleteSavedItem(seriesId)
        } else {
            dao.insertSavedItem(SavedItemEntity(seriesId = seriesId))
        }
    }

    fun isSeriesLiked(seriesId: String): Flow<Boolean> =
        dao.isSeriesLiked(seriesId)

    suspend fun toggleLikeSeries(seriesId: String, currentlyLiked: Boolean) {
        if (currentlyLiked) {
            dao.deleteLikedItem(seriesId)
            dao.updateSeriesLikesCount(seriesId, -1)
        } else {
            dao.insertLikedItem(LikedItemEntity(seriesId = seriesId))
            dao.updateSeriesLikesCount(seriesId, 1)
        }
    }

    fun getCommentsForSeries(seriesId: String): Flow<List<CommentEntity>> =
        dao.getCommentsForSeries(seriesId)

    suspend fun addComment(
        seriesId: String,
        episodeId: String,
        userName: String,
        userAvatar: String,
        text: String,
        isSpoiler: Boolean
    ) {
        val comment = CommentEntity(
            id = "c_${System.currentTimeMillis()}",
            seriesId = seriesId,
            episodeId = episodeId,
            userName = userName,
            userAvatar = userAvatar,
            text = text,
            likesCount = 0,
            isSpoiler = isSpoiler,
            timestampFormatted = "Az önce",
            createdAt = System.currentTimeMillis()
        )
        dao.insertComment(comment)
    }

    suspend fun upvoteComment(commentId: String) =
        dao.upvoteComment(commentId)

    suspend fun deleteComment(commentId: String) =
        dao.deleteComment(commentId)

    suspend fun updateUserProfile(profile: UserProfileEntity) =
        dao.upsertUserProfile(profile)

    suspend fun logAnalyticsEvent(eventType: String, seriesId: String, episodeId: String, durationSeconds: Long = 0) {
        dao.insertAnalyticsEvent(
            AnalyticsEventEntity(
                eventType = eventType,
                seriesId = seriesId,
                episodeId = episodeId,
                durationSeconds = durationSeconds
            )
        )
    }

    suspend fun setSeriesPublished(seriesId: String, isPublished: Boolean) =
        dao.setSeriesPublishedStatus(seriesId, isPublished)

    suspend fun saveSeries(series: SeriesEntity) =
        dao.upsertSeries(series)

    suspend fun deleteSeries(seriesId: String) =
        dao.deleteSeries(seriesId)

    suspend fun saveEpisode(episode: EpisodeEntity) =
        dao.upsertEpisode(episode)

    suspend fun deleteEpisode(episodeId: String) =
        dao.deleteEpisode(episodeId)

    suspend fun getAdminCounts(): Triple<Int, Int, Int> {
        val series = dao.getTotalSeriesCount()
        val episodes = dao.getTotalEpisodesCount()
        val comments = dao.getTotalCommentsCount()
        return Triple(series, episodes, comments)
    }
}
