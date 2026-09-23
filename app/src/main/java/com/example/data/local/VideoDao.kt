package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.PlaybackRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query("SELECT * FROM playback_records")
    fun getAllRecords(): Flow<List<PlaybackRecord>>

    @Query("SELECT * FROM playback_records WHERE videoId = :videoId LIMIT 1")
    fun getRecord(videoId: Long): Flow<PlaybackRecord?>

    @Query("SELECT * FROM playback_records WHERE videoId = :videoId LIMIT 1")
    suspend fun getRecordDirect(videoId: Long): PlaybackRecord?

    @Query("SELECT * FROM playback_records WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 100): Flow<List<PlaybackRecord>>

    @Query("SELECT * FROM playback_records WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<PlaybackRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: PlaybackRecord)

    @Transaction
    suspend fun updatePlaybackPosition(
        videoId: Long,
        videoUri: String,
        title: String,
        lastPositionMs: Long,
        durationMs: Long,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val existing = getRecordDirect(videoId)
        if (existing != null) {
            insertOrUpdate(
                existing.copy(
                    videoUri = videoUri,
                    title = title,
                    lastPositionMs = lastPositionMs,
                    durationMs = durationMs,
                    lastPlayedTimestamp = timestamp
                )
            )
        } else {
            insertOrUpdate(
                PlaybackRecord(
                    videoId = videoId,
                    videoUri = videoUri,
                    title = title,
                    lastPositionMs = lastPositionMs,
                    durationMs = durationMs,
                    lastPlayedTimestamp = timestamp,
                    isFavorite = false
                )
            )
        }
    }

    @Transaction
    suspend fun toggleFavorite(
        videoId: Long,
        videoUri: String,
        title: String
    ): Boolean {
        val existing = getRecordDirect(videoId)
        val newFavStatus = if (existing != null) !existing.isFavorite else true
        if (existing != null) {
            insertOrUpdate(existing.copy(isFavorite = newFavStatus))
        } else {
            insertOrUpdate(
                PlaybackRecord(
                    videoId = videoId,
                    videoUri = videoUri,
                    title = title,
                    isFavorite = newFavStatus
                )
            )
        }
        return newFavStatus
    }

    @Transaction
    suspend fun setFavorite(
        videoId: Long,
        videoUri: String,
        title: String,
        isFav: Boolean
    ) {
        val existing = getRecordDirect(videoId)
        if (existing != null) {
            insertOrUpdate(existing.copy(isFavorite = isFav))
        } else {
            insertOrUpdate(
                PlaybackRecord(
                    videoId = videoId,
                    videoUri = videoUri,
                    title = title,
                    isFavorite = isFav
                )
            )
        }
    }

    @Query("UPDATE playback_records SET lastPlayedTimestamp = 0, lastPositionMs = 0")
    suspend fun clearPlaybackHistory()

    @Query("UPDATE playback_records SET isFavorite = 0")
    suspend fun clearAllFavorites()
}
