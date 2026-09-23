package com.example.data.model

import android.net.Uri

data class VideoItem(
    val id: Long,
    val uriString: String,
    val title: String,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAddedSec: Long,
    val path: String,
    val folderName: String,
    val width: Int = 0,
    val height: Int = 0,
    val isFavorite: Boolean = false,
    val lastPositionMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L
) {
    val contentUri: Uri
        get() = Uri.parse(uriString)

    val progressPercent: Float
        get() = if (durationMs > 0 && lastPositionMs > 0) {
            (lastPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val isPartiallyWatched: Boolean
        get() = durationMs > 5000L && lastPositionMs > 3000L && (durationMs - lastPositionMs) > 4000L
}
