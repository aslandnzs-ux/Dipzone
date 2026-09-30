package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DipzonDatabase
import com.example.data.model.*
import com.example.data.repository.DipzonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    DISCOVER,
    SEARCH,
    WATCHLIST,
    PROFILE,
    DETAIL,
    PLAYER,
    ADMIN
}

data class PlayerUiState(
    val currentSeries: SeriesEntity? = null,
    val currentEpisode: EpisodeEntity? = null,
    val episodes: List<EpisodeEntity> = emptyList(),
    val isPlaying: Boolean = true,
    val currentPositionSeconds: Long = 0,
    val durationSeconds: Long = 180,
    val isControlsVisible: Boolean = true,
    val showEpisodeSheet: Boolean = false,
    val showCommentSheet: Boolean = false,
    val showQualitySheet: Boolean = false,
    val showSpeedSheet: Boolean = false,
    val subtitlesEnabled: Boolean = false,
    val currentSubtitleText: String = "",
    val playbackSpeed: Float = 1.0f,
    val quality: String = "1080p FHD (Otomatik)",
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val autoAdvanceSeconds: Int? = null // countdown for next episode
)

class DipzonViewModel(application: Application) : AndroidViewModel(application) {

    val repository: DipzonRepository

    init {
        val db = DipzonDatabase.getDatabase(application, viewModelScope)
        repository = DipzonRepository(db.dipzonDao())
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenStack = mutableListOf<Screen>()

    // Active Selection
    private val _activeSeriesId = MutableStateFlow<String?>(null)
    val activeSeriesId: StateFlow<String?> = _activeSeriesId.asStateFlow()

    private val _activeEpisodeId = MutableStateFlow<String?>(null)
    val activeEpisodeId: StateFlow<String?> = _activeEpisodeId.asStateFlow()

    // Home / Catalog Flow
    val allSeries: StateFlow<List<SeriesEntity>> = repository.allSeries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trendingSeries: StateFlow<List<SeriesEntity>> = repository.trendingSeries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newSeries: StateFlow<List<SeriesEntity>> = repository.newSeries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchProgressList: StateFlow<List<WatchProgressEntity>> = repository.allWatchProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedItems: StateFlow<List<SavedItemEntity>> = repository.savedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Category Filter for Home
    private val _selectedHomeGenre = MutableStateFlow("Tümü")
    val selectedHomeGenre: StateFlow<String> = _selectedHomeGenre.asStateFlow()

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<SeriesEntity>> = _searchQuery
        .debounce(250)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.trendingSeries
            else repository.searchSeries(query.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches = MutableStateFlow(listOf("Karanlık Şafak", "Bilim Kurgu", "Kerem Bürsin", "Boğaz"))

    // Player State
    private val _playerState = MutableStateFlow(PlayerUiState())
    val playerState: StateFlow<PlayerUiState> = _playerState.asStateFlow()

    // Comments for active series
    val currentSeriesComments: StateFlow<List<CommentEntity>> = _activeSeriesId
        .flatMapLatest { id ->
            if (id != null) repository.getCommentsForSeries(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin State
    val adminAllSeries: StateFlow<List<SeriesEntity>> = repository.adminAllSeries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _adminStats = MutableStateFlow(Triple(0, 0, 0))
    val adminStats: StateFlow<Triple<Int, Int, Int>> = _adminStats.asStateFlow()

    // Onboarding State
    private val _showGenreOnboarding = MutableStateFlow(false)
    val showGenreOnboarding: StateFlow<Boolean> = _showGenreOnboarding.asStateFlow()

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (screenStack.isNotEmpty()) {
            val previous = screenStack.removeAt(screenStack.size - 1)
            // If leaving player, pause and save progress
            if (_currentScreen.value == Screen.PLAYER) {
                saveCurrentPlaybackProgress()
            }
            _currentScreen.value = previous
            true
        } else {
            if (_currentScreen.value != Screen.HOME) {
                _currentScreen.value = Screen.HOME
                true
            } else {
                false
            }
        }
    }

    fun openSeriesDetail(seriesId: String) {
        _activeSeriesId.value = seriesId
        navigateTo(Screen.DETAIL)
    }

    fun startPlaying(seriesId: String, episodeId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val series = repository.getSeriesByIdDirect(seriesId) ?: return@launch
            val episodes = repository.getEpisodesForSeriesDirect(seriesId)
            if (episodes.isEmpty()) return@launch

            val targetEpisode = if (episodeId != null) {
                episodes.firstOrNull { it.id == episodeId } ?: episodes.first()
            } else {
                // Resume from saved progress or first episode
                val progress = repository.getWatchProgressForSeriesDirect(seriesId)
                if (progress != null) {
                    episodes.firstOrNull { it.id == progress.episodeId } ?: episodes.first()
                } else {
                    episodes.first()
                }
            }

            val resumePos = if (episodeId == null) {
                repository.getWatchProgressForSeriesDirect(seriesId)?.positionSeconds ?: 0L
            } else {
                0L
            }

            _activeSeriesId.value = seriesId
            _activeEpisodeId.value = targetEpisode.id

            _playerState.value = PlayerUiState(
                currentSeries = series,
                currentEpisode = targetEpisode,
                episodes = episodes,
                isPlaying = true,
                currentPositionSeconds = resumePos,
                durationSeconds = targetEpisode.durationSeconds.toLong().coerceAtLeast(60L),
                isControlsVisible = true
            )

            // Check liked/saved state
            launch {
                repository.isSeriesLiked(seriesId).collect { liked ->
                    _playerState.update { it.copy(isLiked = liked) }
                }
            }
            launch {
                repository.isSeriesSaved(seriesId).collect { saved ->
                    _playerState.update { it.copy(isSaved = saved) }
                }
            }

            repository.logAnalyticsEvent("PLAY_START", seriesId, targetEpisode.id)
            navigateTo(Screen.PLAYER)
        }
    }

    fun setHomeGenre(genre: String) {
        _selectedHomeGenre.value = genre
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addRecentSearch(query: String) {
        if (query.isNotBlank() && !recentSearches.value.contains(query)) {
            recentSearches.update { (listOf(query) + it).take(8) }
        }
    }

    fun clearRecentSearch(query: String) {
        recentSearches.update { it.filter { item -> item != query } }
    }

    // Player Controls
    fun togglePlayPause() {
        _playerState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun seekRelative(secondsDelta: Long) {
        _playerState.update { current ->
            val newPos = (current.currentPositionSeconds + secondsDelta)
                .coerceIn(0L, current.durationSeconds)
            current.copy(currentPositionSeconds = newPos)
        }
    }

    fun seekTo(seconds: Long) {
        _playerState.update { it.copy(currentPositionSeconds = seconds.coerceIn(0L, it.durationSeconds)) }
    }

    fun updatePlaybackPosition(seconds: Long, duration: Long) {
        _playerState.update {
            it.copy(
                currentPositionSeconds = seconds,
                durationSeconds = if (duration > 0) duration else it.durationSeconds
            )
        }

        // Subtitle generation sync for demo
        val current = _playerState.value
        if (current.subtitlesEnabled) {
            val subText = when {
                seconds in 2..8 -> "[Sessiz adımlar ve gerilimli müzik yükselir]"
                seconds in 9..16 -> "Dedektif Kemal: 'Herkes sakin olsun. Bu kapıdan kimse çıkmayacak.'"
                seconds in 17..25 -> "Gizem: 'Ben hiçbir şey görmedim, yemin ederim!'"
                seconds in 26..34 -> "[Dışarıda fırtına ve dalga sesleri çarpıyor]"
                seconds in 35..45 -> "Dedektif Kemal: 'Telefonu bana ver. Katil aramızda.'"
                else -> ""
            }
            _playerState.update { it.copy(currentSubtitleText = subText) }
        }

        // Auto advance triggers when < 5 seconds left
        if (duration > 10 && seconds >= duration - 4 && current.autoAdvanceSeconds == null) {
            triggerAutoAdvanceCountdown()
        }
    }

    private fun triggerAutoAdvanceCountdown() {
        val current = _playerState.value
        val currentIndex = current.episodes.indexOfFirst { it.id == current.currentEpisode?.id }
        if (currentIndex in 0 until current.episodes.size - 1) {
            viewModelScope.launch {
                for (countdown in 4 downTo 1) {
                    _playerState.update { it.copy(autoAdvanceSeconds = countdown) }
                    delay(1000)
                }
                _playerState.update { it.copy(autoAdvanceSeconds = null) }
                playNextEpisode()
            }
        }
    }

    fun playNextEpisode() {
        val current = _playerState.value
        val episodes = current.episodes
        val currentIndex = episodes.indexOfFirst { it.id == current.currentEpisode?.id }
        if (currentIndex != -1 && currentIndex < episodes.size - 1) {
            val nextEpisode = episodes[currentIndex + 1]
            switchEpisode(nextEpisode)
        }
    }

    fun playPreviousEpisode() {
        val current = _playerState.value
        val episodes = current.episodes
        val currentIndex = episodes.indexOfFirst { it.id == current.currentEpisode?.id }
        if (currentIndex > 0) {
            val prevEpisode = episodes[currentIndex - 1]
            switchEpisode(prevEpisode)
        }
    }

    fun switchEpisode(episode: EpisodeEntity) {
        saveCurrentPlaybackProgress()
        _activeEpisodeId.value = episode.id
        _playerState.update {
            it.copy(
                currentEpisode = episode,
                currentPositionSeconds = 0,
                durationSeconds = episode.durationSeconds.toLong(),
                isPlaying = true,
                autoAdvanceSeconds = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            currentSeries?.let {
                repository.logAnalyticsEvent("EPISODE_START", it.id, episode.id)
            }
        }
    }

    val currentSeries: SeriesEntity? get() = _playerState.value.currentSeries

    fun toggleControls() {
        _playerState.update { it.copy(isControlsVisible = !it.isControlsVisible) }
    }

    fun setControlsVisibility(visible: Boolean) {
        _playerState.update { it.copy(isControlsVisible = visible) }
    }

    fun toggleSubtitles() {
        _playerState.update { it.copy(subtitlesEnabled = !it.subtitlesEnabled) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.update { it.copy(playbackSpeed = speed, showSpeedSheet = false) }
    }

    fun setQuality(quality: String) {
        _playerState.update { it.copy(quality = quality, showQualitySheet = false) }
    }

    fun setEpisodeSheetVisible(visible: Boolean) {
        _playerState.update { it.copy(showEpisodeSheet = visible) }
    }

    fun setCommentSheetVisible(visible: Boolean) {
        _playerState.update { it.copy(showCommentSheet = visible) }
    }

    fun setQualitySheetVisible(visible: Boolean) {
        _playerState.update { it.copy(showQualitySheet = visible) }
    }

    fun setSpeedSheetVisible(visible: Boolean) {
        _playerState.update { it.copy(showSpeedSheet = visible) }
    }

    // Likes & Saves
    fun toggleCurrentSeriesLike() {
        val series = _playerState.value.currentSeries ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleLikeSeries(series.id, _playerState.value.isLiked)
        }
    }

    fun toggleCurrentSeriesSave() {
        val series = _playerState.value.currentSeries ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleSaveSeries(series.id, _playerState.value.isSaved)
        }
    }

    fun toggleSeriesSave(seriesId: String, isCurrentlySaved: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleSaveSeries(seriesId, isCurrentlySaved)
        }
    }

    // Comments
    fun postComment(text: String, isSpoiler: Boolean) {
        val series = _playerState.value.currentSeries ?: return
        val episode = _playerState.value.currentEpisode
        val profile = userProfile.value
        val name = profile?.username ?: "Sinemasever"
        val avatar = profile?.avatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&q=80"

        viewModelScope.launch(Dispatchers.IO) {
            repository.addComment(
                seriesId = series.id,
                episodeId = episode?.id ?: "${series.id}_s1_e1",
                userName = name,
                userAvatar = avatar,
                text = text,
                isSpoiler = isSpoiler
            )
        }
    }

    fun upvoteComment(commentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upvoteComment(commentId)
        }
    }

    // Watch Progress persistence
    fun saveCurrentPlaybackProgress() {
        val current = _playerState.value
        val series = current.currentSeries ?: return
        val episode = current.currentEpisode ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveWatchProgress(
                WatchProgressEntity(
                    seriesId = series.id,
                    episodeId = episode.id,
                    seasonNumber = episode.seasonNumber,
                    episodeNumber = episode.episodeNumber,
                    positionSeconds = current.currentPositionSeconds,
                    durationSeconds = current.durationSeconds,
                    updatedAt = System.currentTimeMillis(),
                    isCompleted = current.currentPositionSeconds >= current.durationSeconds - 5
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearWatchHistory()
        }
    }

    // Profile Settings
    fun updateDataSaver(enabled: Boolean) {
        val profile = userProfile.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserProfile(profile.copy(dataSaver = enabled))
        }
    }

    fun updateNotifications(enabled: Boolean) {
        val profile = userProfile.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserProfile(profile.copy(notificationsEnabled = enabled))
        }
    }

    fun updatePreferredGenres(genres: String) {
        val profile = userProfile.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserProfile(profile.copy(preferredGenres = genres))
        }
    }

    // Admin & Moderation
    fun refreshAdminStats() {
        viewModelScope.launch(Dispatchers.IO) {
            val stats = repository.getAdminCounts()
            _adminStats.value = stats
        }
    }

    fun toggleSeriesPublished(seriesId: String, currentStatus: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setSeriesPublished(seriesId, !currentStatus)
        }
    }

    fun deleteSeries(seriesId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSeries(seriesId)
            refreshAdminStats()
        }
    }

    fun addSeries(series: SeriesEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSeries(series)
            refreshAdminStats()
        }
    }
}
