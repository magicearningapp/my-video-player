package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.VideoCyanPrimary
import com.example.ui.theme.VideoDarkBackground
import com.example.ui.theme.VideoTextPrimary
import com.example.ui.theme.VideoTextSecondary

@Composable
fun PermissionRationaleView(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    isPartial: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isPartial) Icons.Default.Lock else Icons.Default.VideoLibrary,
            contentDescription = null,
            tint = VideoCyanPrimary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isPartial) "Limited Media Access" else "Access Your Local Videos",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = VideoTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isPartial) {
                "On Android 14, you selected only certain videos. To view all videos stored on your device, grant full video access in Settings."
            } else {
                "My Video Player is completely offline and needs permission to discover and play video files stored on your device."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = VideoTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = VideoCyanPrimary,
                contentColor = VideoDarkBackground
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
                .testTag("grant_permission_button")
        ) {
            Text(if (isPartial) "Manage Video Access" else "Grant Permission")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onOpenSettings,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
                .testTag("open_settings_button")
        ) {
            Text("Open App Settings", color = VideoTextPrimary)
        }
    }
}

@Composable
fun EmptyVideosView(
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    hasPermission: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.VideoLibrary,
            contentDescription = null,
            tint = VideoCyanPrimary.copy(alpha = 0.6f),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No videos found",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = VideoTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasPermission) {
                "No video files were detected in device storage. If you recently added videos or have hidden files, tap Refresh."
            } else {
                "To see your local videos, please grant media access permission for local video files."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = VideoTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (hasPermission) {
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VideoCyanPrimary,
                    contentColor = VideoDarkBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("refresh_videos_button")
            ) {
                Text("Refresh")
            }
        } else {
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VideoCyanPrimary,
                    contentColor = VideoDarkBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("grant_media_access_button")
            ) {
                Text("Grant Media Access")
            }
        }
    }
}
