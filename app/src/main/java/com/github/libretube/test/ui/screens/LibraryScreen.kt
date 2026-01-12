package com.github.libretube.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.libretube.test.R
import com.github.libretube.test.ui.components.VideoCard
import com.github.libretube.test.ui.components.VideoCardState
import com.github.libretube.test.ui.components.PlaylistCardState
import com.github.libretube.test.ui.components.LibraryShelfItem
import com.github.libretube.test.ui.sheets.DownloadPlaylistBottomSheet
import com.github.libretube.test.extensions.toID
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.github.libretube.test.ui.util.animateEnter

data class LibraryScreenState(
    val historyItems: List<VideoCardState> = emptyList(),
    val historyCount: Int = 0,
    val downloadCount: Int = 0,
    val playlists: List<PlaylistCardState> = emptyList(),
    val bookmarks: List<PlaylistCardState> = emptyList(),
    val isRefreshing: Boolean = false,
    val watchHistoryEnabled: Boolean = true,
    val downloadsCardVisible: Boolean = true
)

@Composable
fun LibraryScreen(
    navController: androidx.navigation.NavController,
    libraryViewModel: com.github.libretube.test.ui.models.LibraryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val historyItems by libraryViewModel.historyItems.collectAsState(initial = emptyList())
    val historyCount by libraryViewModel.historyCount.collectAsState(initial = 0)
    val downloadCount by libraryViewModel.downloadCount.collectAsState(initial = 0)
    val playlists by libraryViewModel.playlists.collectAsState(initial = emptyList())
    val bookmarks by libraryViewModel.bookmarks.collectAsState(initial = emptyList())
    val isRefreshing by libraryViewModel.isRefreshing.collectAsState(initial = false)

    var showPlaylistOptions by remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedPlaylistId by remember { androidx.compose.runtime.mutableStateOf("") }
    var selectedPlaylistName by remember { androidx.compose.runtime.mutableStateOf("") }
    var selectedPlaylistType by remember { androidx.compose.runtime.mutableStateOf(com.github.libretube.test.enums.PlaylistType.PUBLIC) }
    var showDownloadPlaylist by remember { androidx.compose.runtime.mutableStateOf(false) }

    val watchHistoryEnabled = com.github.libretube.test.helpers.PreferenceHelper.getBoolean(com.github.libretube.test.constants.PreferenceKeys.WATCH_HISTORY_TOGGLE, true)
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        libraryViewModel.refreshData()
    }
    
    // Resume effect to refresh data
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
             if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                 libraryViewModel.refreshData()
             }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val state = LibraryScreenState(
        historyItems = historyItems.map { it.toVideoCardState() },
        historyCount = historyCount,
        downloadCount = downloadCount,
        playlists = playlists.map { it.toPlaylistCardState() },
        bookmarks = bookmarks.map { it.toPlaylistCardState() },
        isRefreshing = isRefreshing,
        watchHistoryEnabled = watchHistoryEnabled,
        downloadsCardVisible = true // Assuming always visible or logic from Fragment
    )

    LibraryContent(
        state = state,
        onHistoryClick = {
            navController.navigate(com.github.libretube.test.ui.navigation.Routes.WatchHistory)
        },
        onDownloadsClick = {
            navController.navigate(com.github.libretube.test.ui.navigation.Routes.Downloads)
        },
        onRecentlyWatchedSeeAll = {
            navController.navigate(com.github.libretube.test.ui.navigation.Routes.WatchHistory)
        },
        onPlaylistsSeeAll = {
            navController.navigate(
                 com.github.libretube.test.ui.navigation.Routes.libraryListing(com.github.libretube.test.ui.screens.LibraryListingType.PLAYLISTS.name)
            )
        },
        onBookmarksSeeAll = {
            navController.navigate(
                 com.github.libretube.test.ui.navigation.Routes.libraryListing(com.github.libretube.test.ui.screens.LibraryListingType.BOOKMARKS.name)
            )
        },
        onCreatePlaylistClick = {
             // TODO: Create playlist dialog
        },
        onVideoClick = { videoId ->
             com.github.libretube.test.helpers.NavigationHelper.navigateVideo(context, videoId)
        },
        onPlaylistClick = { playlistId ->
             com.github.libretube.test.helpers.NavigationHelper.navigatePlaylist(context, playlistId, com.github.libretube.test.enums.PlaylistType.LOCAL)
        },
        onPlaylistLongClick = { id, name ->
            selectedPlaylistId = id
            selectedPlaylistName = name
            selectedPlaylistType = com.github.libretube.test.enums.PlaylistType.LOCAL
            showPlaylistOptions = true
        },
        onBookmarkClick = { playlistId ->
             com.github.libretube.test.helpers.NavigationHelper.navigatePlaylist(context, playlistId, com.github.libretube.test.enums.PlaylistType.PUBLIC)
        },
        onBookmarkLongClick = { id, name ->
            selectedPlaylistId = id
            selectedPlaylistName = name
            selectedPlaylistType = com.github.libretube.test.enums.PlaylistType.PUBLIC
            showPlaylistOptions = true
        },
        onRefresh = {
            libraryViewModel.refreshData()
        },
        contentPadding = contentPadding
    )

    if (showPlaylistOptions) {
        com.github.libretube.test.ui.sheets.PlaylistOptionsSheet(
            playlistId = selectedPlaylistId,
            playlistName = selectedPlaylistName,
            playlistType = selectedPlaylistType,
            onDismissRequest = { showPlaylistOptions = false },
            onShareClick = { /* TODO */ },
            onEditDescriptionClick = { /* TODO */ },
            onDownloadClick = {
                showDownloadPlaylist = true
            },
            onSortClick = { /* TODO */ },
            onReorderClick = {
                // Navigate to listing for reorder
            },
            onExportClick = { /* TODO */ },
            onBookmarkChange = {
                libraryViewModel.refreshData()
            }
        )
    }

    if (showDownloadPlaylist) {
        DownloadPlaylistBottomSheet(
            playlistId = selectedPlaylistId,
            playlistName = selectedPlaylistName,
            playlistType = selectedPlaylistType,
            onDismissRequest = { showDownloadPlaylist = false }
        )
    }
}

// Extensions for LibraryScreen
private fun com.github.libretube.test.db.obj.WatchHistoryItem.toVideoCardState() = VideoCardState(
    videoId = videoId,
    title = title ?: "",
    uploaderName = uploader ?: "",
    views = "", // History usually doesn't show views, maybe timestamp?
    duration = duration?.let { android.text.format.DateUtils.formatElapsedTime(it) } ?: "",
    thumbnailUrl = thumbnailUrl,
    uploaderAvatarUrl = uploaderAvatar
)

private fun com.github.libretube.test.api.obj.Playlists.toPlaylistCardState() = PlaylistCardState(
    playlistId = id ?: "",
    title = name ?: "",
    description = shortDescription ?: "",
    videoCount = videos,
    thumbnailUrl = thumbnail ?: ""
)

private fun com.github.libretube.test.db.obj.PlaylistBookmark.toPlaylistCardState() = PlaylistCardState(
    playlistId = playlistId,
    title = playlistName ?: "",
    description = uploader ?: "",
    videoCount = videos.toLong(),
    thumbnailUrl = thumbnailUrl ?: ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(
    state: LibraryScreenState,
    onHistoryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onRecentlyWatchedSeeAll: () -> Unit,
    onPlaylistsSeeAll: () -> Unit,
    onBookmarksSeeAll: () -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onVideoClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onPlaylistLongClick: (String, String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onBookmarkLongClick: (String, String) -> Unit,
    onRefresh: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.library)) }
            )
        }
    ) { padding ->
        val pullRefreshState = rememberPullToRefreshState()
        
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 16.dp + contentPadding.calculateBottomPadding(),
                        top = contentPadding.calculateTopPadding()
                    )
                ) {
                    var itemIndex = 0
                    
                    // Quick Access Dashboard
                    item {
                        DashboardSection(
                            state = state,
                            onHistoryClick = onHistoryClick,
                            onDownloadsClick = onDownloadsClick,
                            modifier = Modifier.animateEnter(itemIndex++)
                        )
                    }

                    // Recently Watched
                    if (state.watchHistoryEnabled && state.historyItems.isNotEmpty()) {
                        item {
                            LibrarySectionHeader(
                                title = stringResource(R.string.recently_watched),
                                onSeeAllClick = onRecentlyWatchedSeeAll,
                                modifier = Modifier.animateEnter(itemIndex++)
                            )
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.animateEnter(itemIndex++)
                            ) {
                                items(state.historyItems) { video ->
                                    VideoCard(
                                        state = video,
                                        onClick = { onVideoClick(video.videoId) },
                                        modifier = Modifier.width(280.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Playlists
                    item {
                        LibrarySectionHeader(
                            title = stringResource(R.string.playlists),
                            onSeeAllClick = onPlaylistsSeeAll,
                            onAddClick = onCreatePlaylistClick,
                            modifier = Modifier.animateEnter(itemIndex++)
                        )
                    }
                    if (state.playlists.isEmpty()) {
                        item { EmptyLibraryState() }
                    } else {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.animateEnter(itemIndex++)
                            ) {
                                items(state.playlists) { playlist ->
                                    com.github.libretube.test.ui.components.PlaylistCard(
                                        state = playlist,
                                        onClick = { onPlaylistClick(playlist.playlistId) },
                                        modifier = Modifier.width(280.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Bookmarks
                    if (state.bookmarks.isNotEmpty()) {
                        item {
                            LibrarySectionHeader(
                                title = stringResource(R.string.bookmarks),
                                onSeeAllClick = onBookmarksSeeAll,
                                modifier = Modifier.animateEnter(itemIndex++)
                            )
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.animateEnter(itemIndex++)
                            ) {
                                items(state.bookmarks) { bookmark ->
                                    com.github.libretube.test.ui.components.PlaylistCard(
                                        state = bookmark,
                                        onClick = { onBookmarkClick(bookmark.playlistId) },
                                        modifier = Modifier.width(280.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardSection(
    state: LibraryScreenState,
    onHistoryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.watchHistoryEnabled) {
            DashboardCard(
                title = stringResource(R.string.watch_history),
                count = stringResource(R.string.count_videos, state.historyCount),
                icon = R.drawable.ic_time_outlined,
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = onHistoryClick,
                modifier = Modifier.weight(1f)
            )
        }
        
        if (state.downloadsCardVisible) {
            DashboardCard(
                title = stringResource(R.string.downloads),
                count = stringResource(R.string.count_files, state.downloadCount),
                icon = R.drawable.ic_download,
                iconTint = MaterialTheme.colorScheme.tertiary,
                onClick = onDownloadsClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    count: String,
    icon: Int,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = count,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun LibrarySectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    onAddClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        
        if (onAddClick != null) {
            IconButton(onClick = onAddClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.tooltip_create_playlist),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        TextButton(onClick = onSeeAllClick) {
            Text(
                text = stringResource(R.string.see_all),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyLibraryState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_list),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = stringResource(R.string.emptyList),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
