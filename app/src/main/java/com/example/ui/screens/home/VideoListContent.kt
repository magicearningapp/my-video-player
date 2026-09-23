package com.example.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.VideoItem
import com.example.ui.components.EmptyVideosView
import com.example.ui.components.VideoCardGridItem
import com.example.ui.components.VideoCardListItem
import com.example.ui.theme.VideoCyanPrimary

@Composable
fun VideoListContent(
    videos: List<VideoItem>,
    isGridView: Boolean,
    isLoading: Boolean,
    hasPermission: Boolean,
    onVideoClick: (VideoItem) -> Unit,
    onToggleFavorite: (VideoItem) -> Unit,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String = "No videos found"
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("video_list_content")
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = VideoCyanPrimary
                )
            }
            videos.isEmpty() -> {
                EmptyVideosView(
                    onRefresh = onRefresh,
                    onRequestPermission = onRequestPermission,
                    hasPermission = hasPermission,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            isGridView -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 170.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = videos,
                        key = { it.id },
                        contentType = { "video_card_grid" }
                    ) { video ->
                        VideoCardGridItem(
                            video = video,
                            onClick = { onVideoClick(video) },
                            onToggleFavorite = { onToggleFavorite(video) }
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = videos,
                        key = { it.id },
                        contentType = { "video_card_list" }
                    ) { video ->
                        VideoCardListItem(
                            video = video,
                            onClick = { onVideoClick(video) },
                            onToggleFavorite = { onToggleFavorite(video) }
                        )
                    }
                }
            }
        }
    }
}
