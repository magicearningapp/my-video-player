package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.player.SpeedSelectorDialog
import com.example.ui.theme.VideoCyanPrimary
import com.example.ui.theme.VideoCyanPrimaryContainer
import com.example.ui.theme.VideoDarkBackground
import com.example.ui.theme.VideoDarkSurface
import com.example.ui.theme.VideoDarkSurfaceElevated
import com.example.ui.theme.VideoTextPrimary
import com.example.ui.theme.VideoTextSecondary
import com.example.ui.viewmodel.SettingsViewModel
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearFavoritesDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        color = VideoTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VideoTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VideoDarkBackground
                )
            )
        },
        containerColor = VideoDarkBackground,
        modifier = modifier.testTag("settings_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Playback
            SettingsSectionHeader(title = "Playback")

            Card(
                colors = CardDefaults.cardColors(containerColor = VideoDarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsSwitchItem(
                        icon = Icons.Default.PlayCircleOutline,
                        title = "Resume Playback",
                        subtitle = "Remember and prompt to resume partial playback",
                        checked = settings.resumePlaybackEnabled,
                        onCheckedChange = { viewModel.setResumePlayback(it) },
                        testTag = "setting_resume_playback"
                    )

                    SettingsDivider()

                    SettingsClickableItem(
                        icon = Icons.Default.Speed,
                        title = "Default Playback Speed",
                        subtitle = Formatters.formatSpeed(settings.defaultPlaybackSpeed),
                        onClick = { showSpeedDialog = true },
                        testTag = "setting_default_speed"
                    )

                    SettingsDivider()

                    SettingsSwitchItem(
                        icon = Icons.Default.ScreenRotation,
                        title = "Auto-Rotate Player",
                        subtitle = "Automatically rotate player to match video aspect",
                        checked = settings.autoRotate,
                        onCheckedChange = { viewModel.setAutoRotate(it) },
                        testTag = "setting_auto_rotate"
                    )
                }
            }

            // Section: Display & Appearance
            SettingsSectionHeader(title = "Appearance & Content")

            Card(
                colors = CardDefaults.cardColors(containerColor = VideoDarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsSwitchItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Theme",
                        subtitle = "Sleek cinema dark interface for OLED / LCD",
                        checked = settings.darkTheme,
                        onCheckedChange = { viewModel.setDarkTheme(it) },
                        testTag = "setting_dark_theme"
                    )

                    SettingsDivider()

                    SettingsSwitchItem(
                        icon = Icons.Default.Visibility,
                        title = "Show Hidden Videos",
                        subtitle = "Include dot-prefixed files and .nomedia folders",
                        checked = settings.showHiddenVideos,
                        onCheckedChange = { viewModel.setShowHiddenVideos(it) },
                        testTag = "setting_show_hidden"
                    )
                }
            }

            // Section: Data & Storage
            SettingsSectionHeader(title = "History & Data")

            Card(
                colors = CardDefaults.cardColors(containerColor = VideoDarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsClickableItem(
                        icon = Icons.Default.History,
                        title = "Clear Playback History",
                        subtitle = "Reset recent playback positions and watch history",
                        onClick = { showClearHistoryDialog = true },
                        testTag = "setting_clear_history"
                    )

                    SettingsDivider()

                    SettingsClickableItem(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Clear Favorites",
                        subtitle = "Remove all videos from your favorites list",
                        onClick = { showClearFavoritesDialog = true },
                        testTag = "setting_clear_favorites"
                    )
                }
            }

            // Section: About
            SettingsSectionHeader(title = "About")

            Card(
                colors = CardDefaults.cardColors(containerColor = VideoDarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                SettingsClickableItem(
                    icon = Icons.Default.Info,
                    title = "About My Video Player",
                    subtitle = "Version 1.0 • Offline Native Local Video Player",
                    onClick = { showAboutDialog = true },
                    testTag = "setting_about"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Speed Dialog
        if (showSpeedDialog) {
            SpeedSelectorDialog(
                currentSpeed = settings.defaultPlaybackSpeed,
                onSelectSpeed = {
                    viewModel.setDefaultPlaybackSpeed(it)
                    showSpeedDialog = false
                },
                onDismiss = { showSpeedDialog = false }
            )
        }

        // Clear History Confirm Dialog
        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                title = { Text("Clear Playback History?", color = VideoTextPrimary) },
                text = {
                    Text(
                        "This will reset all watched positions and clear the Recently Played list.",
                        color = VideoTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearPlaybackHistory()
                            showClearHistoryDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("confirm_clear_history")
                    ) {
                        Text("Clear")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showClearHistoryDialog = false }) {
                        Text("Cancel", color = VideoTextPrimary)
                    }
                },
                containerColor = VideoDarkSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Clear Favorites Confirm Dialog
        if (showClearFavoritesDialog) {
            AlertDialog(
                onDismissRequest = { showClearFavoritesDialog = false },
                title = { Text("Clear All Favorites?", color = VideoTextPrimary) },
                text = {
                    Text(
                        "This will remove all videos from your Favorites tab.",
                        color = VideoTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearFavorites()
                            showClearFavoritesDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("confirm_clear_favorites")
                    ) {
                        Text("Clear")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showClearFavoritesDialog = false }) {
                        Text("Cancel", color = VideoTextPrimary)
                    }
                },
                containerColor = VideoDarkSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // About Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.PlayCircleOutline,
                        contentDescription = null,
                        tint = VideoCyanPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "My Video Player",
                        color = VideoTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Optimized specifically for realme C65 (MediaTek Helio G85, Android 14 / realme UI 5.0, 6.67\" 1604x720 display).",
                            color = VideoTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 100% Offline Local Playback\n• No Ads & No Trackers\n• Media3 ExoPlayer Engine\n• Gestures for Volume & Brightness\n• Dynamic Aspect Ratio (Fit, Crop, Fill)\n• Smart Resume Playback",
                            color = VideoCyanPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VideoCyanPrimary,
                            contentColor = VideoDarkBackground
                        )
                    ) {
                        Text("OK")
                    }
                },
                containerColor = VideoDarkSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = VideoCyanPrimary,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(VideoDarkSurfaceElevated.copy(alpha = 0.5f))
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VideoCyanPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = VideoTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = VideoTextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VideoDarkBackground,
                checkedTrackColor = VideoCyanPrimary,
                uncheckedThumbColor = VideoTextSecondary,
                uncheckedTrackColor = VideoDarkSurfaceElevated
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VideoCyanPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = VideoTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = VideoTextSecondary
            )
        }
    }
}
