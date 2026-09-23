package com.example.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderItem
import com.example.data.model.VideoItem
import com.example.ui.components.EmptyVideosView
import com.example.ui.theme.VideoCyanPrimary
import com.example.ui.theme.VideoDarkSurface
import com.example.ui.theme.VideoTextPrimary
import com.example.ui.theme.VideoTextSecondary
import com.example.util.Formatters

@Composable
fun FoldersContent(
    folders: List<FolderItem>,
    selectedFolder: String?,
    folderVideos: List<VideoItem>,
    isGridView: Boolean,
    isLoading: Boolean,
    hasPermission: Boolean,
    onSelectFolder: (String?) -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onToggleFavorite: (VideoItem) -> Unit,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedFolder != null) {
        // Folder Detail View
        BackHandler {
            onSelectFolder(null)
        }

        Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onSelectFolder(null) },
                    modifier = Modifier.testTag("folder_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to folders",
                        tint = VideoTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = VideoCyanPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = selectedFolder,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VideoTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${folderVideos.size} ${if (folderVideos.size == 1) "video" else "videos"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VideoTextSecondary
                    )
                }
            }

            VideoListContent(
                videos = folderVideos,
                isGridView = isGridView,
                isLoading = false,
                hasPermission = hasPermission,
                onVideoClick = onVideoClick,
                onToggleFavorite = onToggleFavorite,
                onRefresh = onRefresh,
                onRequestPermission = onRequestPermission,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        // All Folders View
        Box(
            modifier = modifier
                .fillMaxSize()
                .testTag("folders_content")
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = VideoCyanPrimary
                    )
                }
                folders.isEmpty() -> {
                    EmptyVideosView(
                        onRefresh = onRefresh,
                        onRequestPermission = onRequestPermission,
                        hasPermission = hasPermission,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = folders,
                            key = { it.name }
                        ) { folder ->
                            FolderCardItem(
                                folder = folder,
                                onClick = { onSelectFolder(folder.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderCardItem(
    folder: FolderItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("folder_item_${folder.name}")
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VideoDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = VideoCyanPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = VideoTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${folder.videoCount} ${if (folder.videoCount == 1) "video" else "videos"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VideoTextSecondary,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = VideoTextSecondary
                    )

                    Text(
                        text = Formatters.formatFileSize(folder.totalSizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = VideoTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = VideoTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
