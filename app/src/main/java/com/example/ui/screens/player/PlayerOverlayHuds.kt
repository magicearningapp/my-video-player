package com.example.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VideoCyanPrimary
import com.example.ui.theme.VideoDarkBackground
import com.example.ui.theme.VideoDarkSurface
import com.example.ui.theme.VideoDarkSurfaceElevated
import com.example.ui.theme.VideoHudBackground
import com.example.ui.theme.VideoTextPrimary
import com.example.ui.theme.VideoTextSecondary
import com.example.ui.viewmodel.SubtitleTrackInfo
import kotlin.math.roundToInt

@Composable
fun VolumeHud(
    volumePercent: Float,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val volumeInt = (volumePercent * 100).roundToInt()
        val icon = when {
            volumePercent <= 0.01f -> Icons.Default.VolumeMute
            volumePercent < 0.5f -> Icons.Default.VolumeDown
            else -> Icons.Default.VolumeUp
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(VideoHudBackground)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Volume",
                    tint = VideoCyanPrimary,
                    modifier = Modifier.size(28.dp)
                )

                LinearProgressIndicator(
                    progress = { volumePercent },
                    modifier = Modifier
                        .width(120.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = VideoCyanPrimary,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Text(
                    text = "$volumeInt%",
                    color = VideoTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun BrightnessHud(
    brightnessPercent: Float,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val brightnessInt = (brightnessPercent * 100).roundToInt()
        val icon = if (brightnessPercent < 0.5f) Icons.Default.BrightnessLow else Icons.Default.BrightnessHigh

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(VideoHudBackground)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Brightness",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(28.dp)
                )

                LinearProgressIndicator(
                    progress = { brightnessPercent },
                    modifier = Modifier
                        .width(120.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFFBBF24),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Text(
                    text = "$brightnessInt%",
                    color = VideoTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SeekIndicatorHud(
    deltaSeconds: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val isForward = deltaSeconds > 0
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0x99000000)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isForward) Icons.Default.FastForward else Icons.Default.FastRewind,
                    contentDescription = null,
                    tint = VideoCyanPrimary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = if (isForward) "+${deltaSeconds}s" else "${deltaSeconds}s",
                    color = VideoTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun SpeedSelectorDialog(
    currentSpeed: Float,
    onSelectSpeed: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Playback Speed", color = VideoTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                speeds.forEach { speed ->
                    val isSelected = speed == currentSpeed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) VideoCyanPrimary.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { onSelectSpeed(speed) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("speed_option_${speed}x"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (speed == 1.0f) "1.0x (Normal)" else "${speed}x",
                            color = if (isSelected) VideoCyanPrimary else VideoTextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = VideoCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VideoCyanPrimary,
                    contentColor = VideoDarkBackground
                )
            ) {
                Text("Close")
            }
        },
        containerColor = VideoDarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun SubtitleSelectorDialog(
    tracks: List<SubtitleTrackInfo>,
    onSelectTrack: (SubtitleTrackInfo?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Subtitles", color = VideoTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val hasAnySelected = tracks.any { it.isSelected }

                // Off option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!hasAnySelected) VideoCyanPrimary.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onSelectTrack(null) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("subtitle_off"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Off",
                        color = if (!hasAnySelected) VideoCyanPrimary else VideoTextPrimary,
                        fontWeight = if (!hasAnySelected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (!hasAnySelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = VideoCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                tracks.forEach { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (track.isSelected) VideoCyanPrimary.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { onSelectTrack(track) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("subtitle_track_${track.trackIndex}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = track.name,
                            color = if (track.isSelected) VideoCyanPrimary else VideoTextPrimary,
                            fontWeight = if (track.isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (track.isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = VideoCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VideoCyanPrimary,
                    contentColor = VideoDarkBackground
                )
            ) {
                Text("Close")
            }
        },
        containerColor = VideoDarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PlayerErrorDialog(
    errorMessage: String,
    errorDetails: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    var showFullDetails by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(errorMessage, color = VideoTextPrimary)
        },
        text = {
            Column {
                Text(
                    text = "An error occurred during playback. You can try again or check details.",
                    color = VideoTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (showFullDetails && errorDetails != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VideoDarkSurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = errorDetails,
                            color = VideoTextPrimary,
                            fontSize = 12.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VideoCyanPrimary,
                    contentColor = VideoDarkBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("error_retry_button")
            ) {
                Text("Try Again")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    if (!showFullDetails) {
                        showFullDetails = true
                    } else {
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("error_details_button")
            ) {
                Text(if (!showFullDetails) "Details" else "Dismiss", color = VideoTextPrimary)
            }
        },
        containerColor = VideoDarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
