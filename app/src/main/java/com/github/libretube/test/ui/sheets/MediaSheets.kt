package com.github.libretube.test.ui.sheets

import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.content.Context
import android.content.Intent
import com.github.libretube.test.enums.PlaylistType
import com.github.libretube.test.helpers.BackgroundHelper
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.libretube.test.ui.models.PlaylistViewModel
import com.github.libretube.test.constants.IntentData
import androidx.compose.runtime.livedata.observeAsState
import android.widget.Toast
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.libretube.test.R
import com.github.libretube.test.api.obj.StreamItem
import com.github.libretube.test.constants.PreferenceKeys
import com.github.libretube.test.db.DatabaseHelper
import com.github.libretube.test.db.DatabaseHolder
import com.github.libretube.test.db.obj.WatchPosition
import com.github.libretube.test.util.PlayingQueueMode
import com.github.libretube.test.extensions.toID
import com.github.libretube.test.extensions.toastFromMainDispatcher
import com.github.libretube.test.helpers.NavigationHelper
import com.github.libretube.test.helpers.PlayerHelper
import com.github.libretube.test.helpers.PreferenceHelper
import com.github.libretube.test.util.PlayingQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import com.github.libretube.test.api.PlaylistsHelper
import com.github.libretube.test.api.MediaServiceRepository
import com.github.libretube.test.enums.DownloadTab
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.platform.LocalContext as LocalContextAlias

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoOptionsSheet(
    streamItem: StreamItem,
    onDismissRequest: () -> Unit,
    onShareClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onMarkWatchedStatusChange: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val videoId = remember(streamItem) { streamItem.url?.toID() ?: "" }
    var isWatched by remember { mutableStateOf(false) }

    // Sub-sheet state
    var currentScreen by remember { mutableStateOf("MAIN") } // MAIN, ADD_TO_PLAYLIST, CREATE_PLAYLIST

    val playlistViewModel: PlaylistViewModel = viewModel(factory = PlaylistViewModel.Factory)
    val playlistState by playlistViewModel.uiState.observeAsState()

    LaunchedEffect(videoId) {
        withContext(Dispatchers.IO) {
            val position = DatabaseHelper.getWatchPositionBlocking(videoId) ?: 0L
            isWatched = DatabaseHelper.isVideoWatched(position, streamItem.duration ?: 0)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Box(modifier = Modifier.animateContentSize()) {
            when (currentScreen) {
                "MAIN" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        item {
                            Text(
                                text = streamItem.title ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(16.dp),
                                maxLines = 2
                            )
                        }

                        // Playback Options
                        if (PlayingQueue.getCurrent()?.url?.toID() != videoId) {
                            item {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.playOnBackground)) },
                                    leadingContent = { Icon(painterResource(R.drawable.ic_play), contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        onDismissRequest()
                                        NavigationHelper.navigateVideo(
                                            context,
                                            videoId = videoId,
                                            audioOnlyPlayerRequested = true
                                        )
                                    }
                                )
                            }

                            if (PlayingQueue.isNotEmpty() && PlayingQueue.queueMode == PlayingQueueMode.ONLINE) {
                                item {
                                    ListItem(
                                        headlineContent = { Text(stringResource(R.string.play_next)) },
                                        leadingContent = { Icon(Icons.Default.SkipNext, contentDescription = null) },
                                        modifier = Modifier.clickable {
                                            PlayingQueue.addAsNext(streamItem)
                                            onDismissRequest()
                                        }
                                    )
                                }
                                item {
                                    ListItem(
                                        headlineContent = { Text(stringResource(R.string.add_to_queue)) },
                                        leadingContent = { Icon(Icons.Default.Queue, contentDescription = null) },
                                        modifier = Modifier.clickable {
                                            PlayingQueue.add(streamItem)
                                            onDismissRequest()
                                        }
                                    )
                                }
                            }
                        }

                        // General Options
                        item {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.addToPlaylist)) },
                                leadingContent = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    playlistViewModel.savedStateHandle[IntentData.videoInfo] = streamItem
                                    playlistViewModel.fetchPlaylists()
                                    currentScreen = "ADD_TO_PLAYLIST"
                                }
                            )
                        }

                        if (!streamItem.isLive) {
                            item {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.download)) },
                                    leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        onDismissRequest()
                                        onDownloadClick()
                                    }
                                )
                            }
                        }

                        item {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.share)) },
                                leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    onDismissRequest()
                                    onShareClick()
                                }
                            )
                        }

                        // Watch Status Options
                        if (PlayerHelper.watchPositionsAny || PlayerHelper.watchHistoryEnabled) {
                            item {
                                ListItem(
                                    headlineContent = { 
                                        Text(stringResource(if (isWatched) R.string.mark_as_unwatched else R.string.mark_as_watched)) 
                                    },
                                    leadingContent = { 
                                        Icon(
                                            if (isWatched) Icons.Default.VisibilityOff else Icons.Default.Visibility, 
                                            contentDescription = null
                                        ) 
                                    },
                                    modifier = Modifier.clickable {
                                        scope.launch(Dispatchers.IO) {
                                            if (isWatched) {
                                                DatabaseHolder.Database.watchPositionDao().deleteByVideoId(videoId)
                                                DatabaseHolder.Database.watchHistoryDao().deleteByVideoId(videoId)
                                            } else {
                                                val watchPosition = WatchPosition(videoId, Long.MAX_VALUE)
                                                DatabaseHolder.Database.watchPositionDao().insert(watchPosition)
                                                if (PlayerHelper.watchHistoryEnabled) {
                                                    DatabaseHelper.addToWatchHistory(streamItem.toWatchHistoryItem(videoId))
                                                }
                                            }
                                            withContext(Dispatchers.Main) {
                                                onMarkWatchedStatusChange()
                                                onDismissRequest()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                "ADD_TO_PLAYLIST" -> {
                    playlistState?.let { state ->
                        if (state.saved != null) {
                            LaunchedEffect(Unit) {
                                onDismissRequest()
                                playlistViewModel.onDismissed()
                            }
                        }

                        AddToPlaylistSheet(
                            playlists = state.playlists,
                            lastSelectedId = state.lastSelectedPlaylistId,
                            onCreatePlaylistClick = {
                                currentScreen = "CREATE_PLAYLIST"
                            },
                            onAddClick = { index ->
                                playlistViewModel.onAddToPlaylist(index)
                            }
                        )
                    }
                }
                "CREATE_PLAYLIST" -> {
                    CreatePlaylistSheet(
                        onCreateClick = { name ->
                            scope.launch {
                                val playlistId = withContext(Dispatchers.IO) {
                                    runCatching { PlaylistsHelper.createPlaylist(name) }.getOrNull()
                                }
                                context.toastFromMainDispatcher(
                                    if (playlistId != null) R.string.playlistCreated else R.string.unknown_error
                                )
                                if (playlistId != null) {
                                    playlistViewModel.fetchPlaylists()
                                    currentScreen = "ADD_TO_PLAYLIST"
                                }
                            }
                        },
                        onCloneClick = { url ->
                            // Simple clone logic
                            scope.launch {
                                val playlistIdFromUrl = android.net.Uri.parse(url).getQueryParameter("list")
                                if (playlistIdFromUrl == null) {
                                    Toast.makeText(context, R.string.invalid_url, Toast.LENGTH_SHORT).show()
                                } else {
                                    val playlistId = withContext(Dispatchers.IO) {
                                        runCatching { PlaylistsHelper.clonePlaylist(playlistIdFromUrl) }.getOrNull()
                                    }
                                    if (playlistId != null) {
                                        playlistViewModel.fetchPlaylists()
                                        currentScreen = "ADD_TO_PLAYLIST"
                                    }
                                    context.toastFromMainDispatcher(
                                        if (playlistId != null) R.string.playlistCloned else R.string.server_error
                                    )
                                }
                            }
                        },
                        onCancel = {
                            currentScreen = "ADD_TO_PLAYLIST"
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistOptionsSheet(
    playlistId: String,
    playlistName: String,
    playlistType: PlaylistType,
    onDismissRequest: () -> Unit,
    onShareClick: () -> Unit,
    onEditDescriptionClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onSortClick: () -> Unit,
    onReorderClick: () -> Unit,
    onExportClick: () -> Unit,
    onBookmarkChange: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isBookmarked by remember { mutableStateOf(false) }

    // Sub-sheet state
    var currentScreen by remember { mutableStateOf("MAIN") } // MAIN, RENAME, DELETE, DESCRIPTION
    var currentDescription by remember { mutableStateOf("") }

    LaunchedEffect(playlistId) {
        withContext(Dispatchers.IO) {
            isBookmarked = DatabaseHolder.Database.playlistBookmarkDao().includes(playlistId)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Box(modifier = Modifier.animateContentSize()) {
            when (currentScreen) {
                "MAIN" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        item {
                            Text(
                                text = playlistName,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        item {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.playOnBackground)) },
                                leadingContent = { Icon(painterResource(R.drawable.ic_play), contentDescription = null) },
                                modifier = Modifier.clickable {
                                    onDismissRequest()
                                    scope.launch(Dispatchers.IO) {
                                        val playlist = runCatching { PlaylistsHelper.getPlaylist(playlistId) }.getOrNull()
                                        playlist?.relatedStreams?.firstOrNull()?.let {
                                            PlayingQueue.setStreams(playlist.relatedStreams)
                                            BackgroundHelper.playOnBackground(
                                                context,
                                                it.url!!.toID(),
                                                playlistId = playlistId
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        item {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.download)) },
                                leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    onDismissRequest()
                                    onDownloadClick()
                                }
                            )
                        }

                        item {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.tooltip_sort)) },
                                leadingContent = { Icon(Icons.Default.Sort, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    onDismissRequest()
                                    onSortClick()
                                }
                            )
                        }

                        if (playlistType != PlaylistType.PUBLIC) {
                            item {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.reorder_playlist)) },
                                    leadingContent = { Icon(Icons.Default.Reorder, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        onDismissRequest()
                                        onReorderClick()
                                    }
                                )
                            }
                        }

                        if (playlistType == PlaylistType.PUBLIC) {
                            item {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.share)) },
                                    leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        onDismissRequest()
                                        onShareClick()
                                    }
                                )
                            }
                            item {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.clonePlaylist)) },
                                    leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        onDismissRequest()
                                        scope.launch(Dispatchers.IO) {
                                            PlaylistsHelper.clonePlaylist(playlistId)
                                        }
                                    }
                                )
                            }
                            item {
                                ListItem(
                                    headlineContent = { 
                                        Text(stringResource(if (isBookmarked) R.string.remove_bookmark else R.string.add_to_bookmarks)) 
                                    },
                                    leadingContent = { 
                                        Icon(
                                            if (isBookmarked) Icons.Default.BookmarkRemove else Icons.Default.BookmarkAdd, 
                                            contentDescription = null
                                        ) 
                                    },
                                    modifier = Modifier.clickable {
                                        scope.launch(Dispatchers.IO) {
                                            if (isBookmarked) {
                                                DatabaseHolder.Database.playlistBookmarkDao().deleteById(playlistId)
                                            } else {
                                                val bookmark = try {
                                                    MediaServiceRepository.instance.getPlaylist(playlistId)
                                                } catch (e: Exception) {
                                                    null
                                                }?.toPlaylistBookmark(playlistId)
                                                bookmark?.let { DatabaseHolder.Database.playlistBookmarkDao().insert(it) }
                                            }
                                            withContext(Dispatchers.Main) {
                                                onBookmarkChange()
                                                onDismissRequest()
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            item {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.export_playlist)) },
                                    leadingContent = { Icon(Icons.Default.Output, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        onDismissRequest()
                                        onExportClick()
                                    }
                                )
                            }
                            item {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.renamePlaylist)) },
                                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        currentScreen = "RENAME"
                                    }
                                )
                            }
                            item {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.change_playlist_description)) },
                                    leadingContent = { Icon(Icons.Default.Description, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            val playlist = withContext(Dispatchers.IO) {
                                                runCatching { PlaylistsHelper.getPlaylist(playlistId) }.getOrNull()
                                            }
                                            currentDescription = playlist?.description ?: ""
                                            currentScreen = "DESCRIPTION"
                                        }
                                    }
                                )
                            }
                            item {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.deletePlaylist)) },
                                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        currentScreen = "DELETE"
                                    }
                                )
                            }
                        }
                    }
                }
                "RENAME" -> {
                    RenamePlaylistSheet(
                        currentName = playlistName,
                        onConfirm = { newName ->
                            scope.launch {
                                val success = withContext(Dispatchers.IO) {
                                    runCatching { PlaylistsHelper.renamePlaylist(playlistId, newName) }.getOrDefault(false)
                                }
                                if (success) {
                                    context.toastFromMainDispatcher(R.string.success)
                                    onBookmarkChange() // Refresh data
                                    onDismissRequest()
                                } else {
                                    context.toastFromMainDispatcher(R.string.server_error)
                                }
                            }
                        },
                        onCancel = { currentScreen = "MAIN" }
                    )
                }
                "DESCRIPTION" -> {
                    EditPlaylistDescriptionSheet(
                        currentDescription = currentDescription,
                        onConfirm = { newDescription ->
                            scope.launch {
                                val success = withContext(Dispatchers.IO) {
                                    runCatching { PlaylistsHelper.changePlaylistDescription(playlistId, newDescription) }.getOrDefault(false)
                                }
                                if (success) {
                                    context.toastFromMainDispatcher(R.string.success)
                                    onBookmarkChange() // Refresh data
                                    onDismissRequest()
                                } else {
                                    context.toastFromMainDispatcher(R.string.server_error)
                                }
                            }
                        },
                        onCancel = { currentScreen = "MAIN" }
                    )
                }
                "DELETE" -> {
                    DeletePlaylistConfirmationSheet(
                        onConfirm = {
                            scope.launch {
                                val success = withContext(Dispatchers.IO) {
                                    PlaylistsHelper.deletePlaylist(playlistId)
                                }
                                if (success) {
                                    context.toastFromMainDispatcher(R.string.success)
                                    onBookmarkChange() // Refresh data
                                    onDismissRequest()
                                } else {
                                    context.toastFromMainDispatcher(R.string.fail)
                                }
                            }
                        },
                        onCancel = { currentScreen = "MAIN" }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadOptionsSheet(
    streamItem: StreamItem,
    downloadTab: DownloadTab,
    onDismissRequest: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onGoToVideoClick: () -> Unit
) {
    val context = LocalContext.current
    val videoId = remember(streamItem) { streamItem.url?.toID() ?: "" }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            item {
                Text(
                    text = streamItem.title ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.playOnBackground)) },
                    leadingContent = { Icon(painterResource(R.drawable.ic_play), contentDescription = null) },
                    modifier = Modifier.clickable {
                        onDismissRequest()
                        BackgroundHelper.playOnBackgroundOffline(context, videoId, downloadTab)
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.share)) },
                    leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onDismissRequest()
                        onShareClick()
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.delete)) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onDismissRequest()
                        onDeleteClick()
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.go_to_video)) },
                    leadingContent = { Icon(Icons.Default.OpenInNew, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onDismissRequest()
                        onGoToVideoClick()
                    }
                )
            }

            val isSelectedCurrentlyPlaying = PlayingQueue.getCurrent()?.url?.toID() == videoId
            if (!isSelectedCurrentlyPlaying && PlayingQueue.isNotEmpty() && PlayingQueue.queueMode == PlayingQueueMode.OFFLINE) {
                item {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.play_next)) },
                        leadingContent = { Icon(Icons.Default.SkipNext, contentDescription = null) },
                        modifier = Modifier.clickable {
                            PlayingQueue.addAsNext(streamItem)
                            onDismissRequest()
                        }
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.add_to_queue)) },
                        leadingContent = { Icon(Icons.Default.Queue, contentDescription = null) },
                        modifier = Modifier.clickable {
                            PlayingQueue.add(streamItem)
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}
