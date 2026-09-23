package com.example.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.VideoItem
import com.example.ui.components.PermissionRationaleView
import com.example.ui.components.ResumePlaybackDialog
import com.example.ui.theme.VideoCyanPrimary
import com.example.ui.theme.VideoCyanPrimaryContainer
import com.example.ui.theme.VideoDarkBackground
import com.example.ui.theme.VideoDarkSurface
import com.example.ui.theme.VideoDarkSurfaceElevated
import com.example.ui.theme.VideoTextPrimary
import com.example.ui.theme.VideoTextSecondary
import com.example.ui.viewmodel.SortDirection
import com.example.ui.viewmodel.SortField
import com.example.ui.viewmodel.VideoViewModel
import com.example.util.PermissionHelper

enum class HomeTab(val label: String) {
    VIDEOS("Videos"),
    FOLDERS("Folders"),
    RECENT("Recent"),
    FAVORITES("Favorites")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VideoViewModel,
    onPlayVideo: (VideoItem, List<VideoItem>, Long) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Permission launcher for Android 14 and earlier
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.updatePermissionStatus()
    }

    LaunchedEffect(Unit) {
        if (!uiState.hasPermission) {
            permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
        }
    }

    // Search Screen Overlay
    if (uiState.isSearching) {
        SearchScreen(
            query = uiState.searchQuery,
            searchResults = uiState.searchResults,
            isGridView = uiState.isGridView,
            onQueryChange = { viewModel.setSearchQuery(it) },
            onCloseSearch = { viewModel.setIsSearching(false) },
            onVideoClick = { video ->
                viewModel.checkAndRequestPlayback(video) { v, pos ->
                    onPlayVideo(v, uiState.searchResults, pos)
                }
            },
            onToggleFavorite = { viewModel.toggleFavorite(it) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Video Player",
                        fontWeight = FontWeight.Bold,
                        color = VideoTextPrimary
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        tint = VideoCyanPrimary,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 8.dp)
                            .size(30.dp)
                    )
                },
                actions = {
                    // Search
                    IconButton(
                        onClick = { viewModel.setIsSearching(true) },
                        modifier = Modifier.testTag("search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = VideoTextPrimary
                        )
                    }

                    // Sort (only on video tabs)
                    if (selectedTab != 1) {
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier.testTag("sort_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = VideoTextPrimary
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(VideoDarkSurfaceElevated)
                            ) {
                                Text(
                                    text = "SORT BY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VideoCyanPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )

                                SortMenuItem(
                                    label = "Name",
                                    isSelected = uiState.sortOrder.field == SortField.NAME,
                                    onClick = {
                                        viewModel.setSortOrder(SortField.NAME, uiState.sortOrder.direction)
                                        showSortMenu = false
                                    }
                                )

                                SortMenuItem(
                                    label = "Date Added",
                                    isSelected = uiState.sortOrder.field == SortField.DATE_ADDED,
                                    onClick = {
                                        viewModel.setSortOrder(SortField.DATE_ADDED, uiState.sortOrder.direction)
                                        showSortMenu = false
                                    }
                                )

                                SortMenuItem(
                                    label = "Duration",
                                    isSelected = uiState.sortOrder.field == SortField.DURATION,
                                    onClick = {
                                        viewModel.setSortOrder(SortField.DURATION, uiState.sortOrder.direction)
                                        showSortMenu = false
                                    }
                                )

                                SortMenuItem(
                                    label = "File Size",
                                    isSelected = uiState.sortOrder.field == SortField.SIZE,
                                    onClick = {
                                        viewModel.setSortOrder(SortField.SIZE, uiState.sortOrder.direction)
                                        showSortMenu = false
                                    }
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = VideoDarkSurface
                                )

                                Text(
                                    text = "ORDER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VideoCyanPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )

                                SortMenuItem(
                                    label = "Ascending",
                                    isSelected = uiState.sortOrder.direction == SortDirection.ASCENDING,
                                    onClick = {
                                        viewModel.setSortOrder(uiState.sortOrder.field, SortDirection.ASCENDING)
                                        showSortMenu = false
                                    }
                                )

                                SortMenuItem(
                                    label = "Descending",
                                    isSelected = uiState.sortOrder.direction == SortDirection.DESCENDING,
                                    onClick = {
                                        viewModel.setSortOrder(uiState.sortOrder.field, SortDirection.DESCENDING)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Layout Mode (Grid / List)
                    IconButton(
                        onClick = { viewModel.toggleLayoutMode() },
                        modifier = Modifier.testTag("toggle_layout_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = if (uiState.isGridView) "Switch to List" else "Switch to Grid",
                            tint = VideoTextPrimary
                        )
                    }

                    // Settings
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = VideoTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VideoDarkBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = VideoDarkSurface,
                modifier = Modifier.testTag("home_bottom_nav")
            ) {
                val tabs = listOf(
                    Triple(0, "Videos", Icons.Default.VideoLibrary),
                    Triple(1, "Folders", Icons.Default.Folder),
                    Triple(2, "Recent", Icons.Default.History),
                    Triple(3, "Favorites", Icons.Default.Favorite)
                )

                tabs.forEach { (index, title, icon) ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedTab = index
                            if (index != 1) {
                                viewModel.selectFolder(null)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VideoDarkBackground,
                            selectedTextColor = VideoCyanPrimary,
                            indicatorColor = VideoCyanPrimary,
                            unselectedIconColor = VideoTextSecondary,
                            unselectedTextColor = VideoTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${title.lowercase()}")
                    )
                }
            }
        },
        containerColor = VideoDarkBackground,
        modifier = modifier.testTag("home_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!uiState.hasPermission) {
                PermissionRationaleView(
                    onRequestPermission = {
                        permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
                    },
                    onOpenSettings = {
                        PermissionHelper.openAppSettings(context)
                    },
                    isPartial = uiState.isPartialPermission
                )
            } else {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_content_animation"
                ) { tab ->
                    when (tab) {
                        0 -> {
                            // All Videos
                            VideoListContent(
                                videos = uiState.allVideos,
                                isGridView = uiState.isGridView,
                                isLoading = uiState.isLoading,
                                hasPermission = uiState.hasPermission,
                                onVideoClick = { video ->
                                    viewModel.checkAndRequestPlayback(video) { v, pos ->
                                        onPlayVideo(v, uiState.allVideos, pos)
                                    }
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onRefresh = { viewModel.refreshVideos() },
                                onRequestPermission = {
                                    permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
                                }
                            )
                        }
                        1 -> {
                            // Folders
                            FoldersContent(
                                folders = uiState.folders,
                                selectedFolder = uiState.selectedFolder,
                                folderVideos = uiState.folderVideos,
                                isGridView = uiState.isGridView,
                                isLoading = uiState.isLoading,
                                hasPermission = uiState.hasPermission,
                                onSelectFolder = { viewModel.selectFolder(it) },
                                onVideoClick = { video ->
                                    viewModel.checkAndRequestPlayback(video) { v, pos ->
                                        onPlayVideo(v, uiState.folderVideos, pos)
                                    }
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onRefresh = { viewModel.refreshVideos() },
                                onRequestPermission = {
                                    permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
                                }
                            )
                        }
                        2 -> {
                            // Recently Played
                            VideoListContent(
                                videos = uiState.recentVideos,
                                isGridView = uiState.isGridView,
                                isLoading = uiState.isLoading,
                                hasPermission = uiState.hasPermission,
                                onVideoClick = { video ->
                                    viewModel.checkAndRequestPlayback(video) { v, pos ->
                                        onPlayVideo(v, uiState.recentVideos, pos)
                                    }
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onRefresh = { viewModel.refreshVideos() },
                                onRequestPermission = {
                                    permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
                                },
                                emptyMessage = "No recently played videos"
                            )
                        }
                        3 -> {
                            // Favorites
                            VideoListContent(
                                videos = uiState.favoriteVideos,
                                isGridView = uiState.isGridView,
                                isLoading = uiState.isLoading,
                                hasPermission = uiState.hasPermission,
                                onVideoClick = { video ->
                                    viewModel.checkAndRequestPlayback(video) { v, pos ->
                                        onPlayVideo(v, uiState.favoriteVideos, pos)
                                    }
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onRefresh = { viewModel.refreshVideos() },
                                onRequestPermission = {
                                    permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
                                },
                                emptyMessage = "No favorite videos added yet"
                            )
                        }
                    }
                }
            }

            // Resume Playback Prompt Dialog
            uiState.promptResumeVideo?.let { promptVideo ->
                ResumePlaybackDialog(
                    positionMs = promptVideo.lastPositionMs,
                    durationMs = promptVideo.durationMs,
                    videoTitle = promptVideo.displayName,
                    onResume = {
                        viewModel.confirmResume { v, pos ->
                            val currentPlaylist = when (selectedTab) {
                                1 -> uiState.folderVideos
                                2 -> uiState.recentVideos
                                3 -> uiState.favoriteVideos
                                else -> uiState.allVideos
                            }
                            onPlayVideo(v, currentPlaylist, pos)
                        }
                    },
                    onStartOver = {
                        viewModel.startOver { v, pos ->
                            val currentPlaylist = when (selectedTab) {
                                1 -> uiState.folderVideos
                                2 -> uiState.recentVideos
                                3 -> uiState.favoriteVideos
                                else -> uiState.allVideos
                            }
                            onPlayVideo(v, currentPlaylist, pos)
                        }
                    },
                    onDismiss = {
                        viewModel.dismissResumeDialog()
                    }
                )
            }
        }
    }
}

@Composable
private fun SortMenuItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                color = if (isSelected) VideoCyanPrimary else VideoTextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        trailingIcon = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = VideoCyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        onClick = onClick
    )
}
