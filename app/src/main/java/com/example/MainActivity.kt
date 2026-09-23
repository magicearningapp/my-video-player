package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.VideoItem
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.player.VideoPlayerScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VideoDarkBackground
import com.example.ui.viewmodel.PlayerViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.VideoViewModel

sealed interface Screen {
    object Home : Screen
    object Settings : Screen
    data class Player(
        val video: VideoItem,
        val playlist: List<VideoItem>,
        val startPositionMs: Long
    ) : Screen
}

class MainActivity : ComponentActivity() {

    private val videoViewModel: VideoViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by settingsViewModel.settings.collectAsState()

            MyApplicationTheme(darkTheme = settings.darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VideoDarkBackground
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                    // Handle incoming Intent to view video
                    LaunchedEffect(intent) {
                        handleIncomingIntent(intent) { video ->
                            currentScreen = Screen.Player(
                                video = video,
                                playlist = listOf(video),
                                startPositionMs = 0L
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_navigation"
                    ) { screen ->
                        when (screen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    viewModel = videoViewModel,
                                    onPlayVideo = { video, playlist, startPos ->
                                        currentScreen = Screen.Player(
                                            video = video,
                                            playlist = playlist,
                                            startPositionMs = startPos
                                        )
                                    },
                                    onOpenSettings = {
                                        currentScreen = Screen.Settings
                                    }
                                )
                            }
                            is Screen.Settings -> {
                                SettingsScreen(
                                    viewModel = settingsViewModel,
                                    onBack = {
                                        currentScreen = Screen.Home
                                    }
                                )
                            }
                            is Screen.Player -> {
                                VideoPlayerScreen(
                                    video = screen.video,
                                    playlist = screen.playlist,
                                    startPositionMs = screen.startPositionMs,
                                    playerViewModel = playerViewModel,
                                    onBack = {
                                        playerViewModel.releasePlayer()
                                        videoViewModel.refreshVideos()
                                        currentScreen = Screen.Home
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?, onPlay: (VideoItem) -> Unit) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            val uri: Uri = intent.data!!
            var name = "External Video"
            var size = 0L

            try {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: name
                        if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val video = VideoItem(
                id = System.currentTimeMillis(),
                uriString = uri.toString(),
                title = name,
                displayName = name,
                durationMs = 0L,
                sizeBytes = size,
                dateAddedSec = System.currentTimeMillis() / 1000,
                path = uri.path ?: "",
                folderName = "External"
            )
            onPlay(video)
        }
    }
}
