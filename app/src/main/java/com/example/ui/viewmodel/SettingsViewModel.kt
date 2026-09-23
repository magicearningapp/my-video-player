package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.VideoPlayerApplication
import com.example.data.mediastore.VideoScanner
import com.example.data.repository.PlayerSettings
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as VideoPlayerApplication).database
    private val videoRepository = VideoRepository(VideoScanner(application), db.videoDao())
    val settingsRepository = SettingsRepository(application)

    val settings: StateFlow<PlayerSettings> = settingsRepository.settings

    fun setDarkTheme(enabled: Boolean) {
        settingsRepository.setDarkTheme(enabled)
    }

    fun setAutoRotate(enabled: Boolean) {
        settingsRepository.setAutoRotate(enabled)
    }

    fun setDefaultPlaybackSpeed(speed: Float) {
        settingsRepository.setDefaultPlaybackSpeed(speed)
    }

    fun setResumePlayback(enabled: Boolean) {
        settingsRepository.setResumePlaybackEnabled(enabled)
    }

    fun setShowHiddenVideos(show: Boolean) {
        settingsRepository.setShowHiddenVideos(show)
    }

    fun clearPlaybackHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            videoRepository.clearHistory()
        }
    }

    fun clearFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            videoRepository.clearFavorites()
        }
    }
}
