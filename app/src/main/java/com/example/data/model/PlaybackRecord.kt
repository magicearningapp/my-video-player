package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_records")
data class PlaybackRecord(
    @PrimaryKey
    val videoId: Long,
    val videoUri: String,
    val title: String,
    val lastPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L,
    val isFavorite: Boolean = false
)
