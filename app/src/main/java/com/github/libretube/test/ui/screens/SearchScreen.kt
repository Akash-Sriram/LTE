package com.github.libretube.test.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.github.libretube.test.R
import com.github.libretube.test.api.obj.ContentItem
import com.github.libretube.test.api.obj.StreamItem
import com.github.libretube.test.extensions.formatShort
import com.github.libretube.test.ui.components.VideoCard
import com.github.libretube.test.ui.components.VideoCardState

import android.text.format.DateUtils
import com.github.libretube.test.util.TextUtils
import com.github.libretube.test.api.JsonHelper
import androidx.compose.ui.platform.LocalContext
import com.github.libretube.test.ui.sheets.DownloadBottomSheet
import com.github.libretube.test.ui.sheets.DownloadPlaylistBottomSheet
import com.github.libretube.test.extensions.toID
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.github.libretube.test.ui.sheets.ShareBottomSheet
import com.github.libretube.test.enums.ShareObjectType
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    searchResults: LazyPagingItems<ContentItem>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    searchSuggestion: Pair<String, Boolean>?,
    onSuggestionClick: (String) -> Unit,
    onVideoClick: (ContentItem) -> Unit,
    onVideoLongClick: (ContentItem) -> Unit,
    onChannelClick: (ContentItem) -> Unit,
    onChannelLongClick: (ContentItem) -> Unit,
    onPlaylistClick: (ContentItem) -> Unit,
    onPlaylistLongClick: (ContentItem) -> Unit,
    onUploaderClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showChannelOptions by remember { mutableStateOf(false) }
    var showAddChannelToGroup by remember { mutableStateOf(false) }
    var selectedChannel by remember { mutableStateOf<ContentItem?>(null) }
    
    var showVideoOptions by remember { mutableStateOf(false) }
    var selectedVideo by remember { mutableStateOf<StreamItem?>(null) }
    var showDownloadVideo by remember { mutableStateOf(false) }
    
    var showPlaylistOptions by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<ContentItem?>(null) }
    var showDownloadPlaylist by remember { mutableStateOf(false) }

    var showShareSheet by remember { mutableStateOf(false) }
    var shareObjectType by remember { mutableStateOf(ShareObjectType.VIDEO) }
    var selectedShareItem by remember { mutableStateOf<Pair<String, String>?>(null) } // id to title

    Column(modifier = modifier.fillMaxSize()) {
        // Filter Chips
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "all" to R.string.all,
                "videos" to R.string.videos,
                "channels" to R.string.channels,
                "playlists" to R.string.playlists,
                "music_songs" to R.string.music_songs,
                "music_videos" to R.string.music_videos,
                "music_albums" to R.string.music_albums,
                "music_playlists" to R.string.music_playlists,
                "music_artists" to R.string.music_artists
            )
            
            filters.forEach { (id, labelRes) ->
                FilterChip(
                    selected = selectedFilter == id,
                    onClick = { onFilterSelected(id) },
                    label = { Text(stringResource(labelRes)) }
                )
            }
        }

        // Search Suggestion
        searchSuggestion?.let { (suggestion, corrected) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable(enabled = !corrected) { onSuggestionClick(suggestion) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (corrected) stringResource(R.string.showing_results_for) 
                               else stringResource(R.string.did_you_mean),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Search Results
        LazyPagingItemsList(
            searchResults = searchResults,
            context = context,
            onVideoClick = onVideoClick,
            onVideoLongClick = { item ->
                // Ensure item is cast to StreamItem or use properties
                // ContentItem has url, title, etc. but we might need dummy mapping
                // for VideoOptionsSheet or update VideoOptionsSheet to take more general data
                selectedVideo = item as? StreamItem ?: StreamItem(
                    url = item.url,
                    title = item.title,
                    uploaderName = item.uploaderName,
                    views = item.views,
                    uploaded = item.uploaded,
                    duration = item.duration,
                    thumbnail = item.thumbnail,
                    uploaderAvatar = item.uploaderAvatar
                )
                showVideoOptions = true
            },
            onChannelClick = onChannelClick,
            onChannelLongClick = { item ->
                selectedChannel = item
                showChannelOptions = true
            },
            onPlaylistClick = onPlaylistClick,
            onPlaylistLongClick = { item ->
                selectedPlaylist = item
                showPlaylistOptions = true
            },
            onUploaderClick = onUploaderClick
        )
    }

    if (showChannelOptions && selectedChannel != null) {
        val channel = selectedChannel!!
        com.github.libretube.test.ui.sheets.ChannelOptionsSheet(
            channelId = channel.url.toID(),
            channelName = channel.name,
            isSubscribed = true, // Simplified for now, should ideally check sub status
            onDismissRequest = { showChannelOptions = false },
            onShareClick = {
                selectedShareItem = (channel.url?.toID() ?: "") to (channel.name ?: "")
                shareObjectType = ShareObjectType.CHANNEL
                showShareSheet = true
            },
            onAddToGroupClick = {
                showChannelOptions = false
                showAddChannelToGroup = true
            },
            onPlayLatestClick = {
                // Logic from legacy ChannelOptionsBottomSheet
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        val fullChannel = com.github.libretube.test.api.MediaServiceRepository.instance.getChannel(channel.url.toID())
                        fullChannel.relatedStreams.firstOrNull()?.url?.toID()?.let {
                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                com.github.libretube.test.helpers.NavigationHelper.navigateVideo(context, it)
                            }
                        }
                    } catch (e: Exception) {
                        // Log error
                    }
                }
            },
            onPlayBackgroundClick = {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        val fullChannel = com.github.libretube.test.api.MediaServiceRepository.instance.getChannel(channel.url.toID())
                        fullChannel.relatedStreams.firstOrNull()?.url?.toID()?.let {
                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                com.github.libretube.test.helpers.BackgroundHelper.playOnBackground(context, it)
                            }
                        }
                    } catch (e: Exception) {
                        // Log error
                    }
                }
            }
        )
    }

    if (showAddChannelToGroup && selectedChannel != null) {
        com.github.libretube.test.ui.sheets.AddChannelToGroupSheet(
            channelId = selectedChannel!!.url.toID(),
            onDismissRequest = { showAddChannelToGroup = false }
        )
    }

    if (showVideoOptions && selectedVideo != null) {
        com.github.libretube.test.ui.sheets.VideoOptionsSheet(
            streamItem = selectedVideo!!,
            onDismissRequest = { showVideoOptions = false },
            onShareClick = {
                selectedShareItem = (selectedVideo!!.url?.toID() ?: "") to (selectedVideo!!.title ?: "")
                shareObjectType = ShareObjectType.VIDEO
                showShareSheet = true
            },
            onDownloadClick = {
                showDownloadVideo = true
            }
        )
    }

    if (showDownloadVideo && selectedVideo != null) {
        DownloadBottomSheet(
            videoId = selectedVideo!!.url?.toID() ?: "",
            onDismissRequest = { showDownloadVideo = false }
        )
    }

    if (showPlaylistOptions && selectedPlaylist != null) {
        val playlist = selectedPlaylist!!
        com.github.libretube.test.ui.sheets.PlaylistOptionsSheet(
            playlistId = playlist.url.toID(),
            playlistName = playlist.name ?: "",
            playlistType = com.github.libretube.test.enums.PlaylistType.PUBLIC, // Search results are usually public
            onDismissRequest = { showPlaylistOptions = false },
            onShareClick = {
                selectedShareItem = playlist.url.toID() to (playlist.name ?: "")
                shareObjectType = ShareObjectType.PLAYLIST
                showShareSheet = true
            },
            onEditDescriptionClick = {},
            onDownloadClick = {
                showDownloadPlaylist = true
            },
            onSortClick = {},
            onReorderClick = {},
            onExportClick = {},
            onBookmarkChange = {}
        )
    }

    if (showDownloadPlaylist && selectedPlaylist != null) {
        DownloadPlaylistBottomSheet(
            playlistId = selectedPlaylist!!.url.toID(),
            playlistName = selectedPlaylist!!.name ?: "",
            playlistType = com.github.libretube.test.enums.PlaylistType.PUBLIC,
            onDismissRequest = { showDownloadPlaylist = false }
        )
    }

    if (showShareSheet && selectedShareItem != null) {
        ShareBottomSheet(
            id = selectedShareItem!!.first,
            title = selectedShareItem!!.second,
            shareObjectType = shareObjectType,
            initialTimestamp = "0",
            onDismissRequest = { showShareSheet = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyPagingItemsList(
    searchResults: LazyPagingItems<ContentItem>,
    context: android.content.Context,
    onVideoClick: (ContentItem) -> Unit,
    onVideoLongClick: (ContentItem) -> Unit,
    onChannelClick: (ContentItem) -> Unit,
    onChannelLongClick: (ContentItem) -> Unit,
    onPlaylistClick: (ContentItem) -> Unit,
    onPlaylistLongClick: (ContentItem) -> Unit,
    onUploaderClick: (String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            count = searchResults.itemCount,
            key = searchResults.itemKey { it.url },
            contentType = searchResults.itemContentType { it.type }
        ) { index ->
            val item = searchResults[index] ?: return@items
            when (item.type) {
                StreamItem.TYPE_STREAM -> {
                    VideoCard(
                        state = VideoCardState(
                            videoId = item.url,
                            title = item.title ?: "",
                            uploaderName = item.uploaderName ?: "",
                            views = TextUtils.formatViewsString(context, item.views, item.uploaded),
                            duration = if (item.duration > 0) DateUtils.formatElapsedTime(item.duration) else "",
                            thumbnailUrl = item.thumbnail,
                            uploaderAvatarUrl = item.uploaderAvatar
                        ),
                        onClick = { onVideoClick(item) },
                        modifier = Modifier.combinedClickable(
                            onClick = { onVideoClick(item) },
                            onLongClick = { onVideoLongClick(item) }
                        )
                    )
                }
                StreamItem.TYPE_CHANNEL -> {
                    ChannelSearchRow(
                        item = item,
                        onClick = { onChannelClick(item) },
                        onLongClick = { onChannelLongClick(item) }
                    )
                }
                StreamItem.TYPE_PLAYLIST -> {
                    PlaylistSearchRow(
                        item = item,
                        onClick = { onPlaylistClick(item) },
                        onLongClick = { onPlaylistLongClick(item) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChannelSearchRow(
    item: ContentItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.thumbnail,
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name ?: "",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subscribers = item.subscribers.formatShort()
            val infoText = if (item.subscribers >= 0 && item.videos >= 0) {
                stringResource(R.string.subscriberAndVideoCounts, subscribers, item.videos)
            } else if (item.subscribers >= 0) {
                stringResource(R.string.subscribers, subscribers)
            } else if (item.videos >= 0) {
                stringResource(R.string.videoCount, item.videos)
            } else {
                ""
            }
            Text(
                text = infoText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Subscription button can be added here if needed, but it requires state management
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistSearchRow(
    item: ContentItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .aspectRatio(16f/9f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = item.thumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0x99000000))
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_playlist),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.videos.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = item.name ?: "",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.uploaderName ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
