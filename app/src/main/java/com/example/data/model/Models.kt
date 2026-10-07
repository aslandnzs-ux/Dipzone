package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val posterUrl: String,
    val backdropUrl: String,
    val trailerUrl: String,
    val year: Int,
    val ageRating: String,
    val totalEpisodes: Int,
    val totalSeasons: Int,
    val director: String,
    @androidx.room.ColumnInfo(name = "cast_members") val cast: String,
    val matchRate: Int = 0,
    val viewsCount: Long = 0L,
    val likesCount: Long = 0L,
    val isTrending: Boolean = false,
    val isNew: Boolean = false,
    val isEditorChoice: Boolean = false,
    val isPublished: Boolean = false
)

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val durationSeconds: Int,
    val videoUrl: String,
    val thumbnailUrl: String,
    val synopsis: String,
    val isFree: Boolean = true,
    val publishedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_progress")
data class WatchProgressEntity(
    @PrimaryKey val seriesId: String,
    val episodeId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val positionSeconds: Long,
    val durationSeconds: Long,
    val updatedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

@Entity(tableName = "saved_items")
data class SavedItemEntity(
    @PrimaryKey val seriesId: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "liked_items")
data class LikedItemEntity(
    @PrimaryKey val seriesId: String,
    val likedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val seriesId: String,
    val episodeId: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val likesCount: Int = 0,
    val isSpoiler: Boolean = false,
    val timestampFormatted: String = "Az önce",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "dipzon_user_1",
    val username: String = "Dipzon Kullanıcısı",
    val email: String = "",
    val avatarUrl: String = "",
    val preferredGenres: String = "",
    val isPremium: Boolean = false,
    val premiumTier: String = "Ücretsiz",
    val coins: Int = 0,
    val videoQuality: String = "Otomatik",
    val dataSaver: Boolean = false,
    val subtitleLanguage: String = "Türkçe",
    val notificationsEnabled: Boolean = true
)

@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val seriesId: String,
    val episodeId: String,
    val durationSeconds: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long = System.currentTimeMillis(),
    val searchCount: Int = 1
)
