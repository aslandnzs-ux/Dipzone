package com.example.data.repository

import com.example.data.local.DipzonDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class DipzonRepository(private val dao: DipzonDao) {
    val allSeries = dao.getAllPublishedSeries()
    val trendingSeries = dao.getTrendingSeries()
    val newSeries = dao.getNewSeries()
    val allWatchProgress = dao.getAllWatchProgress()
    val savedItems = dao.getAllSavedItems()
    val userProfile = dao.getUserProfile()
    val adminAllSeries = dao.getAllSeriesAdmin()
    val adminComments = dao.getAllComments()
    val recentSearches = dao.getRecentSearches()
    val trendingSearches = dao.getTrendingSearches()

    fun getSeriesByCategory(category: String) = dao.getSeriesByCategory(category)
    fun getSeriesById(seriesId: String) = dao.getSeriesById(seriesId)
    suspend fun getSeriesByIdDirect(seriesId: String) = dao.getSeriesByIdDirect(seriesId)
    fun searchSeries(query: String) = dao.searchSeries(query)
    fun getEpisodesForSeries(seriesId: String) = dao.getEpisodesForSeries(seriesId)
    suspend fun getEpisodesForSeriesDirect(seriesId: String) = dao.getEpisodesForSeriesDirect(seriesId)
    suspend fun getEpisodeById(episodeId: String) = dao.getEpisodeById(episodeId)
    suspend fun getEpisodeByNumber(seriesId: String, season: Int, episode: Int) = dao.getEpisodeByNumber(seriesId, season, episode)
    fun getWatchProgressForSeries(seriesId: String) = dao.getWatchProgressForSeries(seriesId)
    suspend fun getWatchProgressForSeriesDirect(seriesId: String) = dao.getWatchProgressForSeriesDirect(seriesId)
    suspend fun saveWatchProgress(progress: WatchProgressEntity) = dao.upsertWatchProgress(progress)
    suspend fun clearWatchHistory() = dao.clearWatchHistory()
    fun isSeriesSaved(seriesId: String) = dao.isSeriesSaved(seriesId)

    suspend fun toggleSaveSeries(seriesId: String, currentlySaved: Boolean) {
        if (currentlySaved) dao.deleteSavedItem(seriesId) else dao.insertSavedItem(SavedItemEntity(seriesId))
    }

    fun isSeriesLiked(seriesId: String) = dao.isSeriesLiked(seriesId)

    suspend fun toggleLikeSeries(seriesId: String, currentlyLiked: Boolean) {
        if (currentlyLiked) {
            dao.deleteLikedItem(seriesId)
            dao.updateSeriesLikesCount(seriesId, -1)
        } else {
            dao.insertLikedItem(LikedItemEntity(seriesId))
            dao.updateSeriesLikesCount(seriesId, 1)
            logAnalyticsEvent("LIKE", seriesId, "")
        }
    }

    fun getCommentsForSeries(seriesId: String) = dao.getCommentsForSeries(seriesId)

    suspend fun addComment(seriesId: String, episodeId: String, userName: String, userAvatar: String, text: String, isSpoiler: Boolean) {
        dao.insertComment(
            CommentEntity(
                id = "c_${System.currentTimeMillis()}", seriesId = seriesId, episodeId = episodeId,
                userName = userName, userAvatar = userAvatar, text = text, isSpoiler = isSpoiler
            )
        )
    }

    suspend fun upvoteComment(commentId: String) = dao.upvoteComment(commentId)
    suspend fun deleteComment(commentId: String) = dao.deleteComment(commentId)
    suspend fun updateUserProfile(profile: UserProfileEntity) = dao.upsertUserProfile(profile)

    suspend fun logAnalyticsEvent(eventType: String, seriesId: String, episodeId: String, durationSeconds: Long = 0) {
        dao.insertAnalyticsEvent(AnalyticsEventEntity(eventType = eventType, seriesId = seriesId, episodeId = episodeId, durationSeconds = durationSeconds))
    }

    suspend fun recordSearch(query: String) {
        val clean = query.trim()
        if (clean.length < 2) return
        val existing = dao.getSearchHistory(clean)
        dao.upsertSearchHistory(
            SearchHistoryEntity(
                query = clean,
                searchedAt = System.currentTimeMillis(),
                searchCount = (existing?.searchCount ?: 0) + 1
            )
        )
        logAnalyticsEvent("SEARCH", clean, "")
    }

    suspend fun deleteSearch(query: String) = dao.deleteSearchHistory(query)
    suspend fun clearSearches() = dao.clearSearchHistory()
    suspend fun setSeriesPublished(seriesId: String, isPublished: Boolean) = dao.setSeriesPublishedStatus(seriesId, isPublished)
    suspend fun saveSeries(series: SeriesEntity) = dao.upsertSeries(series)

    suspend fun deleteSeries(seriesId: String) {
        dao.deleteEpisodesForSeries(seriesId)
        dao.deleteSeries(seriesId)
    }

    suspend fun saveEpisode(episode: EpisodeEntity) = dao.upsertEpisode(episode)
    suspend fun deleteEpisode(episodeId: String) = dao.deleteEpisode(episodeId)

    suspend fun getAdminSnapshot(): AdminSnapshot = AdminSnapshot(
        seriesCount = dao.getTotalSeriesCount(),
        episodeCount = dao.getTotalEpisodesCount(),
        commentCount = dao.getTotalCommentsCount(),
        playCount = dao.getPlayCount(),
        completionCount = dao.getCompletionCount(),
        likeCount = dao.getLikeEventCount(),
        searchCount = dao.getSearchEventCount(),
        savedWatchSeconds = dao.getSavedWatchSeconds()
    )
}

data class AdminSnapshot(
    val seriesCount: Int = 0,
    val episodeCount: Int = 0,
    val commentCount: Int = 0,
    val playCount: Long = 0,
    val completionCount: Long = 0,
    val likeCount: Long = 0,
    val searchCount: Long = 0,
    val savedWatchSeconds: Long = 0
)
