package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.VideoPlayerApplication
import com.example.data.mediastore.VideoScanner
import com.example.data.model.VideoItem
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class VideoResizeMode(val mode: Int, val label: String) {
    FIT(AspectRatioFrameLayout.RESIZE_MODE_FIT, "Fit"),
    CROP(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, "Crop"),
    FILL(AspectRatioFrameLayout.RESIZE_MODE_FILL, "Fill")
}

data class SubtitleTrackInfo(
    val groupIndex: Int,
    val trackIndex: Int,
    val name: String,
    val language: String?,
    val isSelected: Boolean
)

data class PlayerUiState(
    val currentVideo: VideoItem? = null,
    val playlist: List<VideoItem> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val resizeMode: VideoResizeMode = VideoResizeMode.FIT,
    val areControlsVisible: Boolean = true,
    val isControlsLocked: Boolean = false,
    val isLandscape: Boolean = false,
    val volumePercent: Float = 0.5f,
    val brightnessPercent: Float = 0.5f,
    val showVolumeHud: Boolean = false,
    val showBrightnessHud: Boolean = false,
    val showSeekHud: Boolean = false,
    val seekHudDeltaSeconds: Int = 0,
    val hasSubtitles: Boolean = false,
    val subtitleTracks: List<SubtitleTrackInfo> = emptyList(),
    val showSubtitleDialog: Boolean = false,
    val showSpeedDialog: Boolean = false,
    val errorMessage: String? = null,
    val errorDetails: String? = null,
    val showErrorDialog: Boolean = false
)

@OptIn(UnstableApi::class)
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as VideoPlayerApplication).database
    private val videoRepository = VideoRepository(VideoScanner(application), db.videoDao())
    private val settingsRepository = SettingsRepository(application)
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var exoPlayer: ExoPlayer? = null
    private var progressTrackingJob: Job? = null
    private var hudDismissJob: Job? = null
    private var controlsAutoHideJob: Job? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _uiState.value = _uiState.value.copy(isBuffering = true)
                }
                Player.STATE_READY -> {
                    val dur = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
                    _uiState.value = _uiState.value.copy(
                        isBuffering = false,
                        durationMs = dur
                    )
                }
                Player.STATE_ENDED -> {
                    _uiState.value = _uiState.value.copy(isBuffering = false, isPlaying = false)
                    // Auto-advance to next video if available
                    playNext()
                }
                Player.STATE_IDLE -> {
                    _uiState.value = _uiState.value.copy(isBuffering = false)
                }
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            extractSubtitleTracks(tracks)
        }

        override fun onPlayerError(error: PlaybackException) {
            _uiState.value = _uiState.value.copy(
                isBuffering = false,
                isPlaying = false,
                errorMessage = "Unable to play this video.",
                errorDetails = error.localizedMessage ?: "Unknown ExoPlayer error (${error.errorCodeName})",
                showErrorDialog = true
            )
        }
    }

    init {
        // Initialize volume
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        _uiState.value = _uiState.value.copy(volumePercent = (curVol.toFloat() / maxVol).coerceIn(0f, 1f))
    }

    fun getOrCreatePlayer(): ExoPlayer {
        if (exoPlayer == null) {
            val app = getApplication<Application>()
            exoPlayer = ExoPlayer.Builder(app)
                .setSeekBackIncrementMs(10000L)
                .setSeekForwardIncrementMs(10000L)
                .build().apply {
                    addListener(playerListener)
                    val speed = settingsRepository.settings.value.defaultPlaybackSpeed
                    playbackParameters = PlaybackParameters(speed)
                    _uiState.value = _uiState.value.copy(playbackSpeed = speed)
                }
        }
        return exoPlayer!!
    }

    fun loadVideo(video: VideoItem, playlist: List<VideoItem>, startPositionMs: Long = 0L) {
        val player = getOrCreatePlayer()
        val index = playlist.indexOfFirst { it.id == video.id }.coerceAtLeast(0)

        _uiState.value = _uiState.value.copy(
            currentVideo = video,
            playlist = playlist,
            currentIndex = index,
            errorMessage = null,
            errorDetails = null,
            showErrorDialog = false,
            currentPositionMs = startPositionMs,
            areControlsVisible = true
        )

        try {
            val mediaItem = MediaItem.Builder()
                .setUri(video.contentUri)
                .setMediaId(video.id.toString())
                .build()

            player.setMediaItem(mediaItem)
            player.prepare()
            if (startPositionMs > 0L) {
                player.seekTo(startPositionMs)
            }
            player.playWhenReady = true

            startProgressTracker()
            resetControlsAutoHide()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Unable to play this video.",
                errorDetails = e.localizedMessage ?: "File open error",
                showErrorDialog = true
            )
        }
    }

    fun retryPlayback() {
        val current = _uiState.value.currentVideo ?: return
        loadVideo(current, _uiState.value.playlist, _uiState.value.currentPositionMs)
    }

    fun dismissErrorDialog() {
        _uiState.value = _uiState.value.copy(showErrorDialog = false)
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        resetControlsAutoHide()
    }

    fun seekRelative(deltaMs: Long) {
        val player = exoPlayer ?: return
        val current = player.currentPosition
        val duration = player.duration.coerceAtLeast(0L)
        val target = (current + deltaMs).coerceIn(0L, duration)
        player.seekTo(target)
        _uiState.value = _uiState.value.copy(
            currentPositionMs = target,
            showSeekHud = true,
            seekHudDeltaSeconds = (deltaMs / 1000).toInt()
        )
        triggerSeekHudDismiss()
        resetControlsAutoHide()
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        val duration = player.duration.coerceAtLeast(0L)
        val target = positionMs.coerceIn(0L, duration)
        player.seekTo(target)
        _uiState.value = _uiState.value.copy(currentPositionMs = target)
        resetControlsAutoHide()
    }

    fun playPrevious() {
        val list = _uiState.value.playlist
        if (list.isEmpty()) return
        val currentPos = exoPlayer?.currentPosition ?: 0L
        if (currentPos > 3000L) {
            // Seek to start of current video
            exoPlayer?.seekTo(0L)
            return
        }
        val prevIndex = _uiState.value.currentIndex - 1
        if (prevIndex >= 0) {
            loadVideo(list[prevIndex], list, 0L)
        }
    }

    fun playNext() {
        val list = _uiState.value.playlist
        if (list.isEmpty()) return
        val nextIndex = _uiState.value.currentIndex + 1
        if (nextIndex < list.size) {
            loadVideo(list[nextIndex], list, 0L)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.playbackParameters = PlaybackParameters(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed, showSpeedDialog = false)
        resetControlsAutoHide()
    }

    fun toggleAspectRatio() {
        val currentMode = _uiState.value.resizeMode
        val nextMode = when (currentMode) {
            VideoResizeMode.FIT -> VideoResizeMode.CROP
            VideoResizeMode.CROP -> VideoResizeMode.FILL
            VideoResizeMode.FILL -> VideoResizeMode.FIT
        }
        _uiState.value = _uiState.value.copy(resizeMode = nextMode)
        resetControlsAutoHide()
    }

    fun toggleControlsVisibility() {
        if (_uiState.value.isControlsLocked) {
            // If controls are locked, toggle shows only the lock icon
            _uiState.value = _uiState.value.copy(areControlsVisible = !_uiState.value.areControlsVisible)
            return
        }
        val newVisible = !_uiState.value.areControlsVisible
        _uiState.value = _uiState.value.copy(areControlsVisible = newVisible)
        if (newVisible) {
            resetControlsAutoHide()
        }
    }

    fun toggleControlsLock() {
        val newLock = !_uiState.value.isControlsLocked
        _uiState.value = _uiState.value.copy(
            isControlsLocked = newLock,
            areControlsVisible = true
        )
        resetControlsAutoHide()
    }

    fun toggleLandscape(isLandscape: Boolean) {
        _uiState.value = _uiState.value.copy(isLandscape = isLandscape)
    }

    fun setShowSpeedDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSpeedDialog = show)
        if (show) controlsAutoHideJob?.cancel() else resetControlsAutoHide()
    }

    fun setShowSubtitleDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSubtitleDialog = show)
        if (show) controlsAutoHideJob?.cancel() else resetControlsAutoHide()
    }

    fun selectSubtitleTrack(trackInfo: SubtitleTrackInfo?) {
        val player = exoPlayer ?: return
        if (trackInfo == null) {
            // Turn off subtitles
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .build()
        } else {
            val trackGroups = player.currentTracks.groups
            if (trackInfo.groupIndex < trackGroups.size) {
                val group = trackGroups[trackInfo.groupIndex]
                player.trackSelectionParameters = player.trackSelectionParameters
                    .buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, trackInfo.trackIndex))
                    .build()
            }
        }
        setShowSubtitleDialog(false)
    }

    fun adjustVolumeByDelta(delta: Float) {
        val current = _uiState.value.volumePercent
        val newVol = (current + delta).coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(
            volumePercent = newVol,
            showVolumeHud = true,
            showBrightnessHud = false
        )

        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetIndex = (newVol * max).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, 0)

        triggerHudDismiss()
    }

    fun adjustBrightnessByDelta(delta: Float) {
        val current = _uiState.value.brightnessPercent
        val newBrightness = (current + delta).coerceIn(0.01f, 1f)
        _uiState.value = _uiState.value.copy(
            brightnessPercent = newBrightness,
            showBrightnessHud = true,
            showVolumeHud = false
        )
        triggerHudDismiss()
    }

    fun setBrightnessPercent(value: Float) {
        _uiState.value = _uiState.value.copy(brightnessPercent = value.coerceIn(0.01f, 1f))
    }

    private fun extractSubtitleTracks(tracks: Tracks) {
        val subtitleList = mutableListOf<SubtitleTrackInfo>()
        for ((groupIndex, group) in tracks.groups.withIndex()) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (trackIndex in 0 until group.length) {
                    val format = group.getTrackFormat(trackIndex)
                    val name = format.label ?: format.language ?: "Track ${subtitleList.size + 1}"
                    subtitleList.add(
                        SubtitleTrackInfo(
                            groupIndex = groupIndex,
                            trackIndex = trackIndex,
                            name = name,
                            language = format.language,
                            isSelected = group.isTrackSelected(trackIndex)
                        )
                    )
                }
            }
        }
        _uiState.value = _uiState.value.copy(
            hasSubtitles = subtitleList.isNotEmpty(),
            subtitleTracks = subtitleList
        )
    }

    private fun triggerHudDismiss() {
        hudDismissJob?.cancel()
        hudDismissJob = viewModelScope.launch {
            delay(1500)
            _uiState.value = _uiState.value.copy(
                showVolumeHud = false,
                showBrightnessHud = false
            )
        }
    }

    private fun triggerSeekHudDismiss() {
        viewModelScope.launch {
            delay(1000)
            _uiState.value = _uiState.value.copy(showSeekHud = false)
        }
    }

    private fun resetControlsAutoHide() {
        controlsAutoHideJob?.cancel()
        if (_uiState.value.isPlaying) {
            controlsAutoHideJob = viewModelScope.launch {
                delay(4000)
                if (_uiState.value.isPlaying && !_uiState.value.showSpeedDialog && !_uiState.value.showSubtitleDialog) {
                    _uiState.value = _uiState.value.copy(areControlsVisible = false)
                }
            }
        }
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                val player = exoPlayer
                if (player != null && player.playbackState == Player.STATE_READY) {
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val dur = player.duration.coerceAtLeast(0L)
                    _uiState.value = _uiState.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur
                    )

                    // Persist playback position to Room locally
                    val currentVideo = _uiState.value.currentVideo
                    if (currentVideo != null && dur > 0L) {
                        videoRepository.savePlaybackPosition(
                            videoId = currentVideo.id,
                            videoUri = currentVideo.uriString,
                            title = currentVideo.displayName,
                            positionMs = pos,
                            durationMs = dur
                        )
                    }
                }
                delay(1000)
            }
        }
    }

    fun toggleFavoriteCurrentVideo() {
        val video = _uiState.value.currentVideo ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val newFav = videoRepository.toggleFavorite(video.id, video.uriString, video.displayName)
            _uiState.value = _uiState.value.copy(
                currentVideo = video.copy(isFavorite = newFav)
            )
        }
    }

    fun releasePlayer() {
        progressTrackingJob?.cancel()
        controlsAutoHideJob?.cancel()
        hudDismissJob?.cancel()
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}
