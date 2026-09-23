package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.VideoPlayerApplication
import com.example.data.mediastore.VideoScanner
import com.example.data.model.FolderItem
import com.example.data.model.PlaybackRecord
import com.example.data.model.VideoItem
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VideoRepository
import com.example.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortField {
    NAME, DATE_ADDED, DURATION, SIZE
}

enum class SortDirection {
    ASCENDING, DESCENDING
}

data class VideoSortOrder(
    val field: SortField = SortField.DATE_ADDED,
    val direction: SortDirection = SortDirection.DESCENDING
)

data class VideoUiState(
    val isLoading: Boolean = true,
    val allVideos: List<VideoItem> = emptyList(),
    val folders: List<FolderItem> = emptyList(),
    val recentVideos: List<VideoItem> = emptyList(),
    val favoriteVideos: List<VideoItem> = emptyList(),
    val selectedFolder: String? = null,
    val folderVideos: List<VideoItem> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<VideoItem> = emptyList(),
    val isSearching: Boolean = false,
    val isGridView: Boolean = true,
    val sortOrder: VideoSortOrder = VideoSortOrder(),
    val hasPermission: Boolean = false,
    val isPartialPermission: Boolean = false,
    val promptResumeVideo: VideoItem? = null
)

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as VideoPlayerApplication).database
    private val videoScanner = VideoScanner(application)
    val videoRepository = VideoRepository(videoScanner, db.videoDao())
    val settingsRepository = SettingsRepository(application)

    private val _rawVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    private val _selectedFolder = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearching = MutableStateFlow(false)
    private val _isGridView = MutableStateFlow(true)
    private val _sortOrder = MutableStateFlow(VideoSortOrder())
    private val _isLoading = MutableStateFlow(true)
    private val _hasPermission = MutableStateFlow(PermissionHelper.hasVideoPermission(application))
    private val _isPartialPermission = MutableStateFlow(PermissionHelper.isPartialAccess(application))
    private val _promptResumeVideo = MutableStateFlow<VideoItem?>(null)

    val uiState: StateFlow<VideoUiState> = combine(
        combine(_rawVideos, videoRepository.allPlaybackRecords, videoRepository.favoriteRecords) { raw, records, favs ->
            val recordMap = records.associateBy { it.videoId }
            raw.map { video ->
                val rec = recordMap[video.id]
                video.copy(
                    isFavorite = rec?.isFavorite ?: false,
                    lastPositionMs = rec?.lastPositionMs ?: 0L,
                    lastPlayedTimestamp = rec?.lastPlayedTimestamp ?: 0L
                )
            }
        },
        _selectedFolder,
        _searchQuery,
        _isSearching,
        combine(_isGridView, _sortOrder, _isLoading, _hasPermission, _isPartialPermission) { g, s, l, p, part ->
            Tuple5(g, s, l, p, part)
        }
    ) { videosWithRecords, selectedFolder, searchQuery, isSearching, tuple ->
        val (isGrid, sortOrder, isLoading, hasPerm, isPartial) = tuple

        val sortedVideos = sortVideos(videosWithRecords, sortOrder)
        val folders = videoRepository.groupVideosByFolder(videosWithRecords)

        val folderVideos = if (selectedFolder != null) {
            sortedVideos.filter { it.folderName.equals(selectedFolder, ignoreCase = true) }
        } else {
            emptyList()
        }

        val recentVideos = videosWithRecords
            .filter { it.lastPlayedTimestamp > 0 }
            .sortedByDescending { it.lastPlayedTimestamp }

        val favoriteVideos = videosWithRecords
            .filter { it.isFavorite }
            .sortedByDescending { it.dateAddedSec }

        val searchResults = if (searchQuery.isNotBlank()) {
            videosWithRecords.filter {
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.folderName.contains(searchQuery, ignoreCase = true)
            }
        } else {
            emptyList()
        }

        VideoUiState(
            isLoading = isLoading,
            allVideos = sortedVideos,
            folders = folders,
            recentVideos = recentVideos,
            favoriteVideos = favoriteVideos,
            selectedFolder = selectedFolder,
            folderVideos = folderVideos,
            searchQuery = searchQuery,
            searchResults = searchResults,
            isSearching = isSearching,
            isGridView = isGrid,
            sortOrder = sortOrder,
            hasPermission = hasPerm,
            isPartialPermission = isPartial,
            promptResumeVideo = _promptResumeVideo.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VideoUiState()
    )

    init {
        refreshVideos()
    }

    fun updatePermissionStatus() {
        val app = getApplication<Application>()
        _hasPermission.value = PermissionHelper.hasVideoPermission(app)
        _isPartialPermission.value = PermissionHelper.isPartialAccess(app)
        if (_hasPermission.value) {
            refreshVideos()
        }
    }

    fun refreshVideos() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val app = getApplication<Application>()
            val hasPerm = PermissionHelper.hasVideoPermission(app)
            _hasPermission.value = hasPerm
            _isPartialPermission.value = PermissionHelper.isPartialAccess(app)

            if (hasPerm) {
                val showHidden = settingsRepository.settings.value.showHiddenVideos
                val scanned = videoRepository.scanLocalVideos(showHidden)
                _rawVideos.value = scanned
            } else {
                _rawVideos.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun selectFolder(folderName: String?) {
        _selectedFolder.value = folderName
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setIsSearching(searching: Boolean) {
        _isSearching.value = searching
        if (!searching) {
            _searchQuery.value = ""
        }
    }

    fun toggleLayoutMode() {
        _isGridView.value = !_isGridView.value
    }

    fun setSortOrder(field: SortField, direction: SortDirection) {
        _sortOrder.value = VideoSortOrder(field, direction)
    }

    fun toggleFavorite(video: VideoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            videoRepository.toggleFavorite(video.id, video.uriString, video.displayName)
        }
    }

    fun checkAndRequestPlayback(
        video: VideoItem,
        onStartPlayback: (VideoItem, Long) -> Unit
    ) {
        val resumeEnabled = settingsRepository.settings.value.resumePlaybackEnabled
        if (resumeEnabled && video.isPartiallyWatched) {
            _promptResumeVideo.value = video
        } else {
            onStartPlayback(video, 0L)
        }
    }

    fun confirmResume(onStartPlayback: (VideoItem, Long) -> Unit) {
        val video = _promptResumeVideo.value
        _promptResumeVideo.value = null
        if (video != null) {
            onStartPlayback(video, video.lastPositionMs)
        }
    }

    fun startOver(onStartPlayback: (VideoItem, Long) -> Unit) {
        val video = _promptResumeVideo.value
        _promptResumeVideo.value = null
        if (video != null) {
            onStartPlayback(video, 0L)
        }
    }

    fun dismissResumeDialog() {
        _promptResumeVideo.value = null
    }

    private fun sortVideos(videos: List<VideoItem>, sortOrder: VideoSortOrder): List<VideoItem> {
        val comparator = when (sortOrder.field) {
            SortField.NAME -> compareBy<VideoItem> { it.displayName.lowercase() }
            SortField.DATE_ADDED -> compareBy<VideoItem> { it.dateAddedSec }
            SortField.DURATION -> compareBy<VideoItem> { it.durationMs }
            SortField.SIZE -> compareBy<VideoItem> { it.sizeBytes }
        }
        return if (sortOrder.direction == SortDirection.ASCENDING) {
            videos.sortedWith(comparator)
        } else {
            videos.sortedWith(comparator.reversed())
        }
    }

    private data class Tuple5<A, B, C, D, E>(
        val a: A, val b: B, val c: C, val d: D, val e: E
    )
}
