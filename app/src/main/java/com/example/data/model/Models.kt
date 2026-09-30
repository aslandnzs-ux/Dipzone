package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String, // "Gerilim", "Bilim Kurgu", "Romantik", "Aksiyon", "Dram", "Fantastik"
    val posterUrl: String,
    val backdropUrl: String,
    val trailerUrl: String,
    val year: Int,
    val ageRating: String, // "16+", "18+", "13+"
    val totalEpisodes: Int,
    val totalSeasons: Int,
    val director: String,
    @androidx.room.ColumnInfo(name = "cast_members") val cast: String, // comma-separated actors
    val matchRate: Int = 98, // percentage match
    val viewsCount: Long = 142000L,
    val likesCount: Long = 28500L,
    val isTrending: Boolean = false,
    val isNew: Boolean = false,
    val isEditorChoice: Boolean = false,
    val isPublished: Boolean = true
)

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey val id: String, // seriesId_s1_e1
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
    val timestampFormatted: String = "2s önce",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "dipzon_user_1",
    val username: String = "Deniz Sinemasever",
    val email: String = "deniz@dipzon.com",
    val avatarUrl: String = "",
    val preferredGenres: String = "Gerilim,Bilim Kurgu,Aksiyon",
    val isPremium: Boolean = true,
    val premiumTier: String = "Dipzon VIP Ultra",
    val coins: Int = 120,
    val videoQuality: String = "1080p FHD",
    val dataSaver: Boolean = false,
    val subtitleLanguage: String = "Türkçe",
    val notificationsEnabled: Boolean = true
)

@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String, // "PLAY_START", "EPISODE_COMPLETE", "DROP_OFF", "SEARCH", "LIKE"
    val seriesId: String,
    val episodeId: String,
    val durationSeconds: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
