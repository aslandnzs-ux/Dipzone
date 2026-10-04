package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DipzonDatabase
import com.example.data.remote.FirebaseContentService
import com.example.data.model.*
import com.example.data.repository.DipzonRepository
import com.example.data.repository.AdminSnapshot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

data class AdminAuthState(
    val isChecking: Boolean = false,
    val isAuthenticated: Boolean = false,
    val email: String = "",
    val error: String? = null
)

data class MediaUploadState(
    val isUploading: Boolean = false,
    val progress: Int = 0,
    val error: String? = null
)

class DipzonViewModel(application: Application) : AndroidViewModel(application) {

    val repository: DipzonRepository
    private val firebaseContent: FirebaseContentService
    private val firebaseAuth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }

    init {
        val db = DipzonDatabase.getDatabase(application, viewModelScope)
        val dao = db.dipzonDao()
        repository = DipzonRepository(dao)
        firebaseContent = FirebaseContentService(dao, viewModelScope)
        firebaseContent.startRealtimeSync()
        checkExistingAdminSession()
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

    val recentSearches: StateFlow<List<String>> = repository.recentSearches
        .map { rows -> rows.map { it.query } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trendingSearches: StateFlow<List<String>> = repository.trendingSearches
        .map { rows -> rows.map { it.query } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private val _adminStats = MutableStateFlow(AdminSnapshot())
    val adminStats: StateFlow<AdminSnapshot> = _adminStats.asStateFlow()

    val adminComments: StateFlow<List<CommentEntity>> = repository.adminComments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _adminAuthState = MutableStateFlow(AdminAuthState())
    val adminAuthState: StateFlow<AdminAuthState> = _adminAuthState.asStateFlow()

    private val _uploadState = MutableStateFlow(MediaUploadState())
    val uploadState: StateFlow<MediaUploadState> = _uploadState.asStateFlow()

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
        viewModelScope.launch(Dispatchers.IO) { repository.recordSearch(query) }
    }

    fun clearRecentSearch(query: String) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteSearch(query) }
    }

    fun clearAllRecentSearches() {
        viewModelScope.launch(Dispatchers.IO) { repository.clearSearches() }
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

        // Auto advance triggers when < 5 seconds left
        if (duration > 10 && seconds >= duration - 4 && _playerState.value.autoAdvanceSeconds == null) {
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
            val completed = current.currentPositionSeconds >= current.durationSeconds - 5
            repository.saveWatchProgress(
                WatchProgressEntity(
                    seriesId = series.id, episodeId = episode.id, seasonNumber = episode.seasonNumber,
                    episodeNumber = episode.episodeNumber, positionSeconds = current.currentPositionSeconds,
                    durationSeconds = current.durationSeconds, updatedAt = System.currentTimeMillis(), isCompleted = completed
                )
            )
            if (completed) repository.logAnalyticsEvent("EPISODE_COMPLETE", series.id, episode.id, current.durationSeconds)
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

    fun updateVideoQuality(quality: String) {
        val profile = userProfile.value ?: return
        viewModelScope.launch(Dispatchers.IO) { repository.updateUserProfile(profile.copy(videoQuality = quality)) }
    }

    fun updateSubtitleLanguage(language: String) {
        val profile = userProfile.value ?: return
        viewModelScope.launch(Dispatchers.IO) { repository.updateUserProfile(profile.copy(subtitleLanguage = language)) }
    }

    fun updatePreferredGenres(genres: String) {
        val profile = userProfile.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserProfile(profile.copy(preferredGenres = genres))
        }
    }

    // Admin & Moderation
    fun refreshAdminStats() {
        viewModelScope.launch(Dispatchers.IO) { _adminStats.value = repository.getAdminSnapshot() }
    }

    fun episodesForSeries(seriesId: String): Flow<List<EpisodeEntity>> = repository.getEpisodesForSeries(seriesId)

    fun toggleSeriesPublished(seriesId: String, currentStatus: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val series = repository.getSeriesByIdDirect(seriesId) ?: return@launch
            val updated = series.copy(isPublished = !currentStatus)
            repository.saveSeries(updated)
            firebaseContent.publishSeries(updated)
            refreshAdminStats()
        }
    }

    fun deleteSeries(seriesId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val episodes = repository.getEpisodesForSeriesDirect(seriesId)
            episodes.forEach { firebaseContent.deleteEpisode(it.id) }
            repository.deleteSeries(seriesId)
            firebaseContent.deleteSeries(seriesId)
            refreshAdminStats()
        }
    }

    fun addSeries(series: SeriesEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSeries(series)
            firebaseContent.publishSeries(series)
            refreshAdminStats()
        }
    }

    fun saveEpisode(episode: EpisodeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveEpisode(episode)
            firebaseContent.publishEpisode(episode)
            val parent = repository.getSeriesByIdDirect(episode.seriesId)
            if (parent != null) {
                val count = repository.getEpisodesForSeriesDirect(parent.id).size
                val updated = parent.copy(totalEpisodes = count)
                repository.saveSeries(updated)
                firebaseContent.publishSeries(updated)
            }
            refreshAdminStats()
        }
    }

    fun deleteEpisode(episode: EpisodeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEpisode(episode.id)
            firebaseContent.deleteEpisode(episode.id)
            val parent = repository.getSeriesByIdDirect(episode.seriesId)
            if (parent != null) {
                val count = repository.getEpisodesForSeriesDirect(parent.id).size
                val updated = parent.copy(totalEpisodes = count)
                repository.saveSeries(updated)
                firebaseContent.publishSeries(updated)
            }
            refreshAdminStats()
        }
    }

    fun deleteAdminComment(commentId: String) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteComment(commentId); refreshAdminStats() }
    }

    fun uploadMedia(uri: Uri, remotePath: String, onComplete: (Result<String>) -> Unit) {
        _uploadState.value = MediaUploadState(isUploading = true, progress = 0)
        firebaseContent.uploadMedia(
            uri = uri,
            remotePath = remotePath,
            onProgress = { p -> _uploadState.value = MediaUploadState(true, p) },
            onResult = { result ->
                _uploadState.value = MediaUploadState(false, if (result.isSuccess) 100 else 0, result.exceptionOrNull()?.message)
                onComplete(result)
            }
        )
    }

    fun resetUploadState() { _uploadState.value = MediaUploadState() }

    fun signInAdmin(email: String, password: String) {
        val auth = firebaseAuth
        if (auth == null) {
            _adminAuthState.value = AdminAuthState(error = "Firebase Auth yapılandırılmamış.")
            return
        }
        if (email.isBlank() || password.isBlank()) {
            _adminAuthState.value = AdminAuthState(error = "E-posta ve şifre gerekli.")
            return
        }
        _adminAuthState.value = AdminAuthState(isChecking = true, email = email)
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    _adminAuthState.value = AdminAuthState(error = "Giriş başarısız.")
                } else {
                    verifyAdminRole(user.uid, user.email.orEmpty())
                }
            }
            .addOnFailureListener { e -> _adminAuthState.value = AdminAuthState(error = e.localizedMessage ?: "Giriş başarısız.") }
    }

    fun signOutAdmin() {
        firebaseAuth?.signOut()
        _adminAuthState.value = AdminAuthState()
    }

    private fun checkExistingAdminSession() {
        val user = firebaseAuth?.currentUser ?: return
        verifyAdminRole(user.uid, user.email.orEmpty())
    }

    private fun verifyAdminRole(uid: String, email: String) {
        val db = firestore
        if (db == null) {
            firebaseAuth?.signOut()
            _adminAuthState.value = AdminAuthState(error = "Firestore yapılandırılmamış.")
            return
        }
        _adminAuthState.value = AdminAuthState(isChecking = true, email = email)
        db.collection("admins").document(uid).get()
            .addOnSuccessListener { doc ->
                val enabled = doc.exists() && doc.getBoolean("enabled") == true
                if (enabled) {
                    _adminAuthState.value = AdminAuthState(isAuthenticated = true, email = email)
                    refreshAdminStats()
                } else {
                    firebaseAuth?.signOut()
                    _adminAuthState.value = AdminAuthState(error = "Bu hesap yönetici olarak yetkilendirilmemiş.")
                }
            }
            .addOnFailureListener { e ->
                firebaseAuth?.signOut()
                _adminAuthState.value = AdminAuthState(error = e.localizedMessage ?: "Yetki kontrolü başarısız.")
            }
    }

}
