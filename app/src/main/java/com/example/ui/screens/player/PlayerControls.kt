package com.example.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VideoCyanPrimary
import com.example.ui.theme.VideoTextPrimary
import com.example.ui.theme.VideoTextSecondary
import com.example.ui.viewmodel.PlayerUiState
import com.example.ui.viewmodel.VideoResizeMode
import com.example.util.Formatters

@Composable
fun PlayerControlsOverlay(
    uiState: PlayerUiState,
    onBack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onRewind10: () -> Unit,
    onForward10: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    onToggleRotate: () -> Unit,
    onShowSpeedDialog: () -> Unit,
    onShowSubtitleDialog: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {

        // When locked, only show unlock floating button
        if (uiState.isControlsLocked) {
            AnimatedVisibility(
                visible = uiState.areControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(24.dp)
            ) {
                IconButton(
                    onClick = onToggleLock,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC0F172A))
                        .testTag("unlock_controls_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlock controls",
                        tint = VideoCyanPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            return@Box
        }

        // Normal full controls
        AnimatedVisibility(
            visible = uiState.areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // Top scrim gradient & Top bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xCC000000), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("player_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = uiState.currentVideo?.displayName ?: "",
                                color = VideoTextPrimary,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Right top actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Favorite
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier.testTag("player_favorite_button")
                            ) {
                                Icon(
                                    imageVector = if (uiState.currentVideo?.isFavorite == true) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (uiState.currentVideo?.isFavorite == true) Color(0xFFEF4444) else Color.White
                                )
                            }

                            // Aspect Ratio Mode (Fit / Crop / Fill)
                            IconButton(
                                onClick = onToggleAspectRatio,
                                modifier = Modifier.testTag("player_aspect_ratio_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color(0x4D000000), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = "Aspect ratio",
                                        tint = VideoCyanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = uiState.resizeMode.label,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Playback Speed
                            IconButton(
                                onClick = onShowSpeedDialog,
                                modifier = Modifier.testTag("player_speed_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color(0x4D000000), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Playback speed",
                                        tint = VideoCyanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = Formatters.formatSpeed(uiState.playbackSpeed),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Center Controls (Previous, Rewind 10, Play/Pause, Forward 10, Next)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .testTag("player_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous video",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Rewind 10s
                    IconButton(
                        onClick = onRewind10,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .testTag("player_rewind_10_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Play / Pause / Buffering
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(VideoCyanPrimary)
                            .testTag("player_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = Color.Black,
                                strokeWidth = 3.dp
                            )
                        } else {
                            IconButton(
                                onClick = onTogglePlayPause,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                    }

                    // Forward 10s
                    IconButton(
                        onClick = onForward10,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .testTag("player_forward_10_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10, // Rotated or mirror
                            contentDescription = "Forward 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .testTag("player_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next video",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Left lock button
                IconButton(
                    onClick = onToggleLock,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .testTag("lock_controls_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Lock controls",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Bottom scrim gradient & Bottom bar (Seekbar, Current Time, Duration, Rotation, Subtitles)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC000000))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
                        // Time & Seek Bar
                        var isDraggingSlider by remember { mutableStateOf(false) }
                        var dragPosition by remember { mutableFloatStateOf(0f) }

                        val currentPos = if (isDraggingSlider) dragPosition.toLong() else uiState.currentPositionMs
                        val duration = uiState.durationMs.coerceAtLeast(1L)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Formatters.formatDuration(currentPos),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Slider(
                                value = currentPos.toFloat().coerceIn(0f, duration.toFloat()),
                                onValueChange = { newValue ->
                                    isDraggingSlider = true
                                    dragPosition = newValue
                                },
                                onValueChangeFinished = {
                                    isDraggingSlider = false
                                    onSeek(dragPosition.toLong())
                                },
                                valueRange = 0f..duration.toFloat(),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                                    .testTag("player_seek_bar"),
                                colors = SliderDefaults.colors(
                                    thumbColor = VideoCyanPrimary,
                                    activeTrackColor = VideoCyanPrimary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )

                            Text(
                                text = Formatters.formatDuration(duration),
                                color = VideoTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Bottom Actions: Subtitles, Screen Rotation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Subtitles button
                            if (uiState.hasSubtitles) {
                                IconButton(
                                    onClick = onShowSubtitleDialog,
                                    modifier = Modifier.testTag("player_subtitles_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ClosedCaption,
                                        contentDescription = "Subtitles",
                                        tint = VideoCyanPrimary
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(48.dp))
                            }

                            // Rotation toggle (Landscape / Portrait)
                            IconButton(
                                onClick = onToggleRotate,
                                modifier = Modifier.testTag("player_rotate_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = "Rotate orientation",
                                    tint = VideoCyanPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
