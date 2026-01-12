package com.github.libretube.test.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.libretube.test.R
import com.github.libretube.test.repo.FeedProgress
import com.github.libretube.test.ui.components.VideoCard
import com.github.libretube.test.ui.components.VideoCardState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import com.github.libretube.test.extensions.toID
import com.github.libretube.test.extensions.formatShort
import com.github.libretube.test.ui.sheets.FilterSortBottomSheetCompose
import com.github.libretube.test.helpers.PreferenceHelper
import com.github.libretube.test.constants.PreferenceKeys
import com.github.libretube.test.api.obj.StreamItem
import com.github.libretube.test.ui.sheets.VideoOptionsSheet
import com.github.libretube.test.ui.sheets.DownloadBottomSheet
import com.github.libretube.test.ui.sheets.ShareBottomSheet
import com.github.libretube.test.enums.ShareObjectType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.libretube.test.ui.util.animateEnter

data class SubscriptionsScreenState(
    val videos: List<SubscriptionItemState>? = null,
    val channelGroups: List<String> = emptyList(),
    val selectedGroupIndex: Int = 0,
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false,
    val feedProgress: FeedProgress? = null,
    val isEmpty: Boolean = false
)

sealed class SubscriptionItemState {
    data class Video(val state: VideoCardState, val streamItem: StreamItem) : SubscriptionItemState()
    object AllCaughtUp : SubscriptionItemState()
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SubscriptionsScreen(
    navController: androidx.navigation.NavController,
    viewModel: com.github.libretube.test.ui.models.SubscriptionsViewModel,
    channelGroupsModel: com.github.libretube.test.ui.models.EditChannelGroupsModel = androidx.lifecycle.viewmodel.compose.viewModel(), // Helper default if simple
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val videoFeed by viewModel.videoFeed.observeAsState()
    val feedProgress by viewModel.feedProgress.observeAsState()
    val channelGroups by channelGroupsModel.groups.observeAsState(emptyList())

    // Preferences State
    var selectedFilterGroup by remember { 
        mutableIntStateOf(com.github.libretube.test.helpers.PreferenceHelper.getInt(com.github.libretube.test.constants.PreferenceKeys.SELECTED_CHANNEL_GROUP, 0))
    }
    var selectedSortOrder by remember { 
        mutableIntStateOf(com.github.libretube.test.helpers.PreferenceHelper.getInt(com.github.libretube.test.constants.PreferenceKeys.FEED_SORT_ORDER, 0))
    }
    var hideWatched by remember { 
        mutableStateOf(com.github.libretube.test.helpers.PreferenceHelper.getBoolean(com.github.libretube.test.constants.PreferenceKeys.HIDE_WATCHED_FROM_FEED, false))
    }
    var showUpcoming by remember { 
        mutableStateOf(com.github.libretube.test.helpers.PreferenceHelper.getBoolean(com.github.libretube.test.constants.PreferenceKeys.SHOW_UPCOMING_IN_FEED, true))
    }
    var showFilterSort by remember { mutableStateOf(false) }
    var showChannelGroups by remember { mutableStateOf(false) }
    var groupToEdit by remember { mutableStateOf<com.github.libretube.test.db.obj.SubscriptionGroup?>(null) }
    var showEditGroup by remember { mutableStateOf(false) }

    var showVideoOptions by remember { mutableStateOf(false) }
    var selectedVideo by remember { mutableStateOf<StreamItem?>(null) }
    var showDownloadSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    var processedFeed by remember { mutableStateOf<List<SubscriptionItemState>?>(null) }
    
    // Derived state for group names
    val groupNames = remember(channelGroups) {
        listOf(context.getString(R.string.all)) + channelGroups.map { it.name }
    }
    
    // Process Feed Effect
    LaunchedEffect(videoFeed, selectedFilterGroup, selectedSortOrder, hideWatched, showUpcoming, channelGroups) {
        if (videoFeed != null) {
            // Helper logic ported from Fragment
            val feed = videoFeed!!
                .filterByGroup(selectedFilterGroup, channelGroups)
                .let {
                    com.github.libretube.test.db.DatabaseHelper.filterByStreamTypeAndWatchPosition(it, hideWatched, showUpcoming)
                }
            
            val sortedFeed = feed.sortedBySelectedOrder(selectedSortOrder)
            
            val result = sortedFeed.map { SubscriptionItemState.Video(it.toVideoCardState(), it) }.toMutableList<SubscriptionItemState>()

            if (selectedSortOrder == 0) {
                 val lastCheckedFeedTime = com.github.libretube.test.helpers.PreferenceHelper.getLastCheckedFeedTime(seenByUser = true)
                 val caughtUpIndex = feed.indexOfFirst { it.uploaded <= lastCheckedFeedTime && !it.isUpcoming }
                 if (caughtUpIndex > 0 && !feed[caughtUpIndex - 1].isUpcoming) {
                     result.add(caughtUpIndex, SubscriptionItemState.AllCaughtUp)
                 }
            }
            processedFeed = result
        } else {
            processedFeed = null
        }
    }

    // Effect to fetch feed if empty
    LaunchedEffect(Unit) {
        if (viewModel.videoFeed.value == null) {
             viewModel.fetchFeed(context, forceRefresh = false)
        }
        if (viewModel.subscriptions.value == null) {
             viewModel.fetchSubscriptions(context)
        }
        // Fetch groups
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
             val groups = com.github.libretube.test.db.DatabaseHolder.Database.subscriptionGroupsDao().getAll()
                 .sortedBy { it.index }
             channelGroupsModel.groups.postValue(groups)
        }
    }

    SubscriptionsContent(
        state = SubscriptionsScreenState(
            videos = processedFeed,
            channelGroups = groupNames,
            selectedGroupIndex = selectedFilterGroup,
            isLoading = videoFeed == null,
            feedProgress = feedProgress,
            isEmpty = videoFeed?.isEmpty() == true
        ),
        onVideoClick = { videoId ->
             com.github.libretube.test.helpers.NavigationHelper.navigateVideo(context, videoId)
        },
        onVideoLongClick = { streamItem ->
            selectedVideo = streamItem
            showVideoOptions = true
        },
        onRefresh = {
             viewModel.fetchSubscriptions(context)
             viewModel.fetchFeed(context, forceRefresh = true)
        },
        onSortFilterClick = { 
            showFilterSort = true
        },
        onToggleSubsClick = {
            showChannelGroups = !showChannelGroups
        },
        onEditGroupsClick = { 
            showChannelGroups = true
        },
        onGroupClick = { index ->
             selectedFilterGroup = index
             com.github.libretube.test.helpers.PreferenceHelper.putInt(com.github.libretube.test.constants.PreferenceKeys.SELECTED_CHANNEL_GROUP, index)
        },
        onGroupLongClick = { index ->
             // Play by group logic
             // Simplified for now
        },
        contentPadding = contentPadding
    )

    if (showFilterSort) {
        FilterSortBottomSheetCompose(
            selectedSortOrder = selectedSortOrder,
            onSortOrderChange = {
                selectedSortOrder = it
                PreferenceHelper.putInt(PreferenceKeys.FEED_SORT_ORDER, it)
            },
            hideWatched = hideWatched,
            onHideWatchedChange = {
                hideWatched = it
                PreferenceHelper.putBoolean(PreferenceKeys.HIDE_WATCHED_FROM_FEED, it)
            },
            showUpcoming = showUpcoming,
            onShowUpcomingChange = {
                showUpcoming = it
                PreferenceHelper.putBoolean(PreferenceKeys.SHOW_UPCOMING_IN_FEED, it)
            },
            onDismissRequest = { showFilterSort = false }
        )
    }

    if (showChannelGroups) {
        com.github.libretube.test.ui.sheets.ChannelGroupsSheet(
            onDismissRequest = { 
                showChannelGroups = false
                // Refresh groups in model
                CoroutineScope(Dispatchers.IO).launch {
                    val groups = com.github.libretube.test.db.DatabaseHolder.Database.subscriptionGroupsDao().getAll()
                        .sortedBy { it.index }
                    channelGroupsModel.groups.postValue(groups)
                }
            },
            onEditGroup = { group ->
                groupToEdit = group
                showEditGroup = true
            }
        )
    }

    if (showEditGroup) {
        com.github.libretube.test.ui.sheets.EditChannelGroupSheet(
            groupToEdit = groupToEdit,
            onDismissRequest = { 
                showEditGroup = false
                // Refresh groups
                CoroutineScope(Dispatchers.IO).launch {
                    val groups = com.github.libretube.test.db.DatabaseHolder.Database.subscriptionGroupsDao().getAll()
                        .sortedBy { it.index }
                    channelGroupsModel.groups.postValue(groups)
                }
            }
        )
    }

    if (showVideoOptions && selectedVideo != null) {
        VideoOptionsSheet(
            streamItem = selectedVideo!!,
            onDismissRequest = { showVideoOptions = false },
            onShareClick = {
                showShareSheet = true
            },
            onDownloadClick = {
                showDownloadSheet = true
            },
            onMarkWatchedStatusChange = {
                // Refresh feed to hide watched if needed
                viewModel.fetchFeed(context, forceRefresh = false)
            }
        )
    }

    if (showDownloadSheet && selectedVideo != null) {
        DownloadBottomSheet(
            videoId = selectedVideo!!.url?.toID() ?: "",
            onDismissRequest = { showDownloadSheet = false }
        )
    }

    if (showShareSheet && selectedVideo != null) {
        ShareBottomSheet(
            id = selectedVideo!!.url?.toID() ?: "",
            title = selectedVideo!!.title ?: "",
            shareObjectType = ShareObjectType.VIDEO,
            initialTimestamp = "0",
            onDismissRequest = { showShareSheet = false }
        )
    }
}

// Helpers
private fun List<com.github.libretube.test.api.obj.StreamItem>.filterByGroup(
    groupIndex: Int, 
    groups: List<com.github.libretube.test.db.obj.SubscriptionGroup>
): List<com.github.libretube.test.api.obj.StreamItem> {
    if (groupIndex == 0) return this
    val group = groups.getOrNull(groupIndex - 1)
    return filter {
        val channelId = it.uploaderUrl.orEmpty().toID()
        group?.channels?.contains(channelId) != false
    }
}

private fun List<com.github.libretube.test.api.obj.StreamItem>.sortedBySelectedOrder(sortOrder: Int) = when (sortOrder) {
    0 -> this
    1 -> this.reversed()
    2 -> this.sortedBy { it.views }.reversed()
    3 -> this.sortedBy { it.views }
    4 -> this.sortedBy { it.uploaderName }
    5 -> this.sortedBy { it.uploaderName }.reversed()
    else -> this
}

private fun com.github.libretube.test.api.obj.StreamItem.toVideoCardState() = VideoCardState(
    videoId = url?.toID() ?: "",
    title = title ?: "",
    uploaderName = uploaderName ?: "",
    views = views?.formatShort() ?: "",
    duration = duration?.let { android.text.format.DateUtils.formatElapsedTime(it) } ?: "",
    thumbnailUrl = thumbnail,
    uploaderAvatarUrl = uploaderAvatar
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SubscriptionsContent(
    state: SubscriptionsScreenState,
    onVideoClick: (String) -> Unit,
    onVideoLongClick: (StreamItem) -> Unit,
    onRefresh: () -> Unit,
    onSortFilterClick: () -> Unit,
    onEditGroupsClick: () -> Unit,
    onToggleSubsClick: () -> Unit,
    onGroupClick: (Int) -> Unit,
    onGroupLongClick: (Int) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.subscriptions)) },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(painterResource(R.drawable.ic_refresh), contentDescription = "Refresh")
                    }
                    IconButton(onClick = onSortFilterClick) {
                        Icon(painterResource(R.drawable.ic_filter_sort), contentDescription = "Sort/Filter")
                    }
                    IconButton(onClick = onToggleSubsClick) {
                        Icon(painterResource(R.drawable.ic_subscriptions), contentDescription = "Toggle Subscriptions")
                    }
                    IconButton(onClick = onEditGroupsClick) {
                        Icon(painterResource(R.drawable.ic_settings), contentDescription = "Edit Groups")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Channel Groups Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.channelGroups) { index, groupName ->
                    val isSelected = state.selectedGroupIndex == index
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { onGroupClick(index) },
                                onLongClick = { onGroupLongClick(index) }
                            ),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = groupName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Feed Progress
            state.feedProgress?.let { progress ->
                if (progress.currentProgress < progress.total) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { progress.currentProgress.toFloat() / progress.total.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${progress.currentProgress}/${progress.total}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (state.isLoading) {
                // TODO: Add shimmer loading
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.isEmpty) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.emptyList))
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    contentPadding = PaddingValues(
                        bottom = 16.dp + contentPadding.calculateBottomPadding(),
                        top = contentPadding.calculateTopPadding()
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.videos ?: emptyList()) { index, item ->
                        when (item) {
                            is SubscriptionItemState.Video -> {
                                VideoCard(
                                    state = item.state,
                                    onClick = { onVideoClick(item.state.videoId) },
                                    modifier = Modifier
                                        .animateEnter(index)
                                        .combinedClickable(
                                            onClick = { onVideoClick(item.state.videoId) },
                                            onLongClick = { onVideoLongClick(item.streamItem) }
                                        )
                                )
                            }
                            is SubscriptionItemState.AllCaughtUp -> {
                                Box(modifier = Modifier.animateEnter(index)) {
                                    AllCaughtUpItem()
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
fun AllCaughtUpItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_done),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.all_caught_up),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.all_caught_up_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

