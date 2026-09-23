package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerSettings(
    val darkTheme: Boolean = true,
    val autoRotate: Boolean = true,
    val defaultPlaybackSpeed: Float = 1.0f,
    val resumePlaybackEnabled: Boolean = true,
    val showHiddenVideos: Boolean = false
)

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("mvp_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<PlayerSettings> = _settings.asStateFlow()

    private fun loadSettings(): PlayerSettings {
        return PlayerSettings(
            darkTheme = prefs.getBoolean(KEY_DARK_THEME, true),
            autoRotate = prefs.getBoolean(KEY_AUTO_ROTATE, true),
            defaultPlaybackSpeed = prefs.getFloat(KEY_DEFAULT_SPEED, 1.0f),
            resumePlaybackEnabled = prefs.getBoolean(KEY_RESUME_PLAYBACK, true),
            showHiddenVideos = prefs.getBoolean(KEY_SHOW_HIDDEN, false)
        )
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        _settings.value = _settings.value.copy(darkTheme = enabled)
    }

    fun setAutoRotate(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_ROTATE, enabled).apply()
        _settings.value = _settings.value.copy(autoRotate = enabled)
    }

    fun setDefaultPlaybackSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_DEFAULT_SPEED, speed).apply()
        _settings.value = _settings.value.copy(defaultPlaybackSpeed = speed)
    }

    fun setResumePlaybackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_RESUME_PLAYBACK, enabled).apply()
        _settings.value = _settings.value.copy(resumePlaybackEnabled = enabled)
    }

    fun setShowHiddenVideos(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_HIDDEN, show).apply()
        _settings.value = _settings.value.copy(showHiddenVideos = show)
    }

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_AUTO_ROTATE = "auto_rotate"
        private const val KEY_DEFAULT_SPEED = "default_speed"
        private const val KEY_RESUME_PLAYBACK = "resume_playback"
        private const val KEY_SHOW_HIDDEN = "show_hidden"
    }
}
