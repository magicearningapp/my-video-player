package com.example.ui.screens.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.data.model.VideoItem
import com.example.ui.viewmodel.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    video: VideoItem,
    playlist: List<VideoItem>,
    startPositionMs: Long,
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by playerViewModel.uiState.collectAsState()

    // Load video on initial composition
    LaunchedEffect(video.id) {
        playerViewModel.loadVideo(video, playlist, startPositionMs)
    }

    // Configure window: keep screen on, hide system bars, handle brightness
    DisposableEffect(Unit) {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Hide system bars for immersive video experience
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            // Restore window brightness
            val lp = window?.attributes
            if (lp != null) {
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = lp
            }
            // Restore system bars
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
            // Restore portrait orientation
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Apply brightness to window
    LaunchedEffect(uiState.brightnessPercent) {
        val window = activity?.window ?: return@LaunchedEffect
        val lp = window.attributes
        lp.screenBrightness = uiState.brightnessPercent.coerceIn(0.01f, 1f)
        window.attributes = lp
    }

    BackHandler {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_player_screen")
    ) {
        // Player Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    player = playerViewModel.getOrCreatePlayer()
                    resizeMode = uiState.resizeMode.mode
                }
            },
            update = { playerView ->
                playerView.player = playerViewModel.getOrCreatePlayer()
                playerView.resizeMode = uiState.resizeMode.mode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture detector wrapper
        PlayerGestureDetector(
            isLocked = uiState.isControlsLocked,
            onSingleTap = {
                playerViewModel.toggleControlsVisibility()
            },
            onDoubleTapLeft = {
                playerViewModel.seekRelative(-10000L)
            },
            onDoubleTapRight = {
                playerViewModel.seekRelative(10000L)
            },
            onDoubleTapCenter = {
                playerViewModel.togglePlayPause()
            },
            onAdjustVolume = { delta ->
                playerViewModel.adjustVolumeByDelta(delta)
            },
            onAdjustBrightness = { delta ->
                playerViewModel.adjustBrightnessByDelta(delta)
            }
        ) {
            // Player controls overlay
            PlayerControlsOverlay(
                uiState = uiState,
                onBack = onBack,
                onTogglePlayPause = { playerViewModel.togglePlayPause() },
                onSeek = { targetMs -> playerViewModel.seekTo(targetMs) },
                onRewind10 = { playerViewModel.seekRelative(-10000L) },
                onForward10 = { playerViewModel.seekRelative(10000L) },
                onPrevious = { playerViewModel.playPrevious() },
                onNext = { playerViewModel.playNext() },
                onToggleLock = { playerViewModel.toggleControlsLock() },
                onToggleAspectRatio = { playerViewModel.toggleAspectRatio() },
                onToggleRotate = {
                    val currentOrientation = activity?.requestedOrientation
                    if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                        currentOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    ) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        playerViewModel.toggleLandscape(false)
                    } else {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        playerViewModel.toggleLandscape(true)
                    }
                },
                onShowSpeedDialog = { playerViewModel.setShowSpeedDialog(true) },
                onShowSubtitleDialog = { playerViewModel.setShowSubtitleDialog(true) },
                onToggleFavorite = { playerViewModel.toggleFavoriteCurrentVideo() }
            )

            // Volume HUD
            VolumeHud(
                volumePercent = uiState.volumePercent,
                visible = uiState.showVolumeHud,
                modifier = Modifier.align(Alignment.Center)
            )

            // Brightness HUD
            BrightnessHud(
                brightnessPercent = uiState.brightnessPercent,
                visible = uiState.showBrightnessHud,
                modifier = Modifier.align(Alignment.Center)
            )

            // Seek Indicator HUD (+10s / -10s)
            SeekIndicatorHud(
                deltaSeconds = uiState.seekHudDeltaSeconds,
                visible = uiState.showSeekHud,
                modifier = Modifier.align(
                    if (uiState.seekHudDeltaSeconds > 0) Alignment.CenterEnd else Alignment.CenterStart
                )
            )
        }

        // Dialogs
        if (uiState.showSpeedDialog) {
            SpeedSelectorDialog(
                currentSpeed = uiState.playbackSpeed,
                onSelectSpeed = { playerViewModel.setPlaybackSpeed(it) },
                onDismiss = { playerViewModel.setShowSpeedDialog(false) }
            )
        }

        if (uiState.showSubtitleDialog) {
            SubtitleSelectorDialog(
                tracks = uiState.subtitleTracks,
                onSelectTrack = { playerViewModel.selectSubtitleTrack(it) },
                onDismiss = { playerViewModel.setShowSubtitleDialog(false) }
            )
        }

        if (uiState.showErrorDialog) {
            PlayerErrorDialog(
                errorMessage = uiState.errorMessage ?: "Unable to play this video.",
                errorDetails = uiState.errorDetails,
                onRetry = { playerViewModel.retryPlayback() },
                onDismiss = { playerViewModel.dismissErrorDialog() }
            )
        }
    }
}
