package com.example.data.repository

import com.example.data.local.VideoDao
import com.example.data.mediastore.VideoScanner
import com.example.data.model.FolderItem
import com.example.data.model.PlaybackRecord
import com.example.data.model.VideoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class VideoRepository(
    private val videoScanner: VideoScanner,
    private val videoDao: VideoDao
) {

    val allPlaybackRecords: Flow<List<PlaybackRecord>> = videoDao.getAllRecords()
    val favoriteRecords: Flow<List<PlaybackRecord>> = videoDao.getFavorites()
    val recentlyPlayedRecords: Flow<List<PlaybackRecord>> = videoDao.getRecentlyPlayed(100)

    suspend fun scanLocalVideos(showHidden: Boolean): List<VideoItem> {
        return videoScanner.scanVideos(showHidden)
    }

    suspend fun getPlaybackRecordDirect(videoId: Long): PlaybackRecord? {
        return videoDao.getRecordDirect(videoId)
    }

    suspend fun savePlaybackPosition(
        videoId: Long,
        videoUri: String,
        title: String,
        positionMs: Long,
        durationMs: Long
    ) {
        videoDao.updatePlaybackPosition(
            videoId = videoId,
            videoUri = videoUri,
            title = title,
            lastPositionMs = positionMs,
            durationMs = durationMs
        )
    }

    suspend fun toggleFavorite(videoId: Long, videoUri: String, title: String): Boolean {
        return videoDao.toggleFavorite(videoId, videoUri, title)
    }

    suspend fun clearHistory() {
        videoDao.clearPlaybackHistory()
    }

    suspend fun clearFavorites() {
        videoDao.clearAllFavorites()
    }

    fun groupVideosByFolder(videos: List<VideoItem>): List<FolderItem> {
        val grouped = videos.groupBy { it.folderName }
        return grouped.map { (folderName, folderVideos) ->
            val totalBytes = folderVideos.sumOf { it.sizeBytes }
            val latest = folderVideos.maxByOrNull { it.dateAddedSec }?.uriString
            FolderItem(
                name = folderName,
                videoCount = folderVideos.size,
                latestVideoUri = latest,
                totalSizeBytes = totalBytes
            )
        }.sortedByDescending { it.videoCount }
    }
}
