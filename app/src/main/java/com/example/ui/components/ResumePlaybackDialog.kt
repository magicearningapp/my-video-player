package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.VideoCyanPrimary
import com.example.ui.theme.VideoDarkBackground
import com.example.ui.theme.VideoDarkSurface
import com.example.ui.theme.VideoTextPrimary
import com.example.ui.theme.VideoTextSecondary
import com.example.util.Formatters

@Composable
fun ResumePlaybackDialog(
    positionMs: Long,
    durationMs: Long,
    videoTitle: String,
    onResume: () -> Unit,
    onStartOver: () -> Unit,
    onDismiss: () -> Unit
) {
    val formattedPos = Formatters.formatDuration(positionMs)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Resume Playback",
                style = MaterialTheme.typography.titleLarge,
                color = VideoTextPrimary
            )
        },
        text = {
            Text(
                text = "Resume from $formattedPos?",
                style = MaterialTheme.typography.bodyLarge,
                color = VideoTextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VideoCyanPrimary,
                    contentColor = VideoDarkBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("resume_button")
            ) {
                Text("Resume")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onStartOver,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("start_over_button")
            ) {
                Text("Start Over", color = VideoTextPrimary)
            }
        },
        containerColor = VideoDarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
