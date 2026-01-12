package com.github.libretube.test.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.libretube.test.R
import com.github.libretube.test.ui.components.PlaylistCard
import com.github.libretube.test.ui.components.PlaylistCardState
import com.github.libretube.test.ui.components.ShimmerVideoRow
import com.github.libretube.test.ui.components.VideoCard
import com.github.libretube.test.ui.components.VideoCardState
import com.github.libretube.test.ui.components.ShimmerSpotlightItem
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.github.libretube.test.ui.theme.LibreTubeTheme
import androidx.compose.runtime.livedata.observeAsState
import com.github.libretube.test.extensions.toID
import com.github.libretube.test.extensions.formatShort
import com.github.libretube.test.ui.util.animateEnter

data class HomeScreenState(
    val featured: List<VideoCardState>? = null,
    val continueWatching: List<VideoCardState>? = null,
    val trending: List<VideoCardState>? = null,
    val bookmarks: List<PlaylistCardState>? = null,
    val playlists: List<PlaylistCardState>? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val trendingCategory: String? = null,
    val trendingRegionName: String? = null
)

@Composable
fun HomeScreen(
    navController: androidx.navigation.NavController,
    homeViewModel: com.github.libretube.test.ui.models.HomeViewModel,
    subscriptionsViewModel: com.github.libretube.test.ui.models.SubscriptionsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val trending by homeViewModel.trending.observeAsState()
    val feed by homeViewModel.feed.observeAsState()
    val bookmarks by homeViewModel.bookmarks.observeAsState()
    val playlists by homeViewModel.playlists.observeAsState()
    val continueWatching by homeViewModel.continueWatching.observeAsState()
    val isLoading by homeViewModel.isLoading.observeAsState(true)

    // Load feed on entry if needed
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                 val defaultItems = context.resources.getStringArray(R.array.homeTabItemsValues)
                 val visibleItems = com.github.libretube.test.helpers.PreferenceHelper.getStringSet(
                     com.github.libretube.test.constants.PreferenceKeys.HOME_TAB_CONTENT, 
                     defaultItems.toSet()
                 )
                 
                 homeViewModel.loadHomeFeed(
                    context = context,
                    subscriptionsViewModel = subscriptionsViewModel,
                    visibleItems = visibleItems,
                    onUnusualLoadTime = {}
                 )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    val state = HomeScreenState(
        featured = feed?.map { it.toVideoCardState() },
        continueWatching = continueWatching?.map { it.toVideoCardState() },
        trending = trending?.second?.streams?.take(10)?.map { it.toVideoCardState() },
        bookmarks = bookmarks?.map { it.toPlaylistCardState() },
        playlists = playlists?.map { it.toPlaylistCardState() },
        isLoading = isLoading,
        isRefreshing = isLoading && (feed != null || trending != null),
        trendingCategory = trending?.first?.name,
        trendingRegionName = com.github.libretube.test.helpers.PreferenceHelper.getTrendingRegion(context)
    )

    HomeContent(
        state = state,
        onVideoClick = { videoId ->
            com.github.libretube.test.helpers.NavigationHelper.navigateVideo(context, videoId)
        },
        onPlaylistClick = { playlistId ->
            com.github.libretube.test.helpers.NavigationHelper.navigatePlaylist(context, playlistId, com.github.libretube.test.enums.PlaylistType.PUBLIC)
        },
        onSectionClick = { section ->
            when (section) {
                "featured" -> navController.navigate(com.github.libretube.test.ui.navigation.Routes.Subscriptions)
                "watching" -> navController.navigate(com.github.libretube.test.ui.navigation.Routes.WatchHistory)
                "trending" -> navController.navigate(com.github.libretube.test.ui.navigation.Routes.Trends)
                "playlists" -> navController.navigate(
                    com.github.libretube.test.ui.navigation.Routes.libraryListing(com.github.libretube.test.ui.screens.LibraryListingType.PLAYLISTS.name)
                )
                "bookmarks" -> navController.navigate(
                    com.github.libretube.test.ui.navigation.Routes.libraryListing(com.github.libretube.test.ui.screens.LibraryListingType.BOOKMARKS.name)
                )
            }
        },
        onTrendingCategoryClick = { /* Show Dialog - TODO: Implement Composable Dialog */ },
        onTrendingRegionClick = { /* Show Dialog - TODO */ },
        modifier = Modifier,
        contentPadding = contentPadding
    )
}

// Extension functions helper (copied from HomeFragment)
private fun com.github.libretube.test.api.obj.StreamItem.toVideoCardState() = VideoCardState(
    videoId = url?.toID() ?: "",
    title = title ?: "",
    uploaderName = uploaderName ?: "",
    views = views?.formatShort() ?: "",
    duration = duration?.let { android.text.format.DateUtils.formatElapsedTime(it) } ?: "",
    thumbnailUrl = thumbnail,
    uploaderAvatarUrl = uploaderAvatar
)

private fun com.github.libretube.test.db.obj.PlaylistBookmark.toPlaylistCardState() = PlaylistCardState(
    playlistId = playlistId,
    title = playlistName ?: "",
    description = uploader ?: "",
    videoCount = videos.toLong(),
    thumbnailUrl = thumbnailUrl ?: ""
)

private fun com.github.libretube.test.api.obj.Playlists.toPlaylistCardState() = PlaylistCardState(
    playlistId = id ?: "",
    title = name ?: "",
    description = shortDescription ?: "",
    videoCount = videos,
    thumbnailUrl = thumbnail ?: ""
)

@Composable
fun HomeContent(
    state: HomeScreenState,
    onVideoClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onSectionClick: (String) -> Unit,
    onTrendingCategoryClick: () -> Unit,
    onTrendingRegionClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val shimmerScrollState = androidx.compose.foundation.rememberScrollState()
    
    if (state.isLoading && !state.isRefreshing) {
        Column {
             // Mimic Home Structure with Shimmer
             Spacer(modifier = Modifier.height(16.dp))
             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .horizontalScroll(shimmerScrollState)
                     .padding(horizontal = 16.dp)
             ) {
                 repeat(3) {
                     ShimmerSpotlightItem()
                 }
             }
             Spacer(modifier = Modifier.height(24.dp))
             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .horizontalScroll(androidx.compose.foundation.rememberScrollState()) // Independent scroll
                     .padding(horizontal = 16.dp)
             ) {
                 repeat(3) {
                     ShimmerSpotlightItem()
                 }
             }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = 16.dp + contentPadding.calculateBottomPadding(),
            top = contentPadding.calculateTopPadding()
        )
    ) {
        var itemIndex = 0

        // Featured Section
        state.featured?.takeIf { it.isNotEmpty() }?.let { videos ->
            item {
                HomeSectionHeader(
                    title = stringResource(R.string.featured),
                    onClick = { onSectionClick("featured") },
                    modifier = Modifier.animateEnter(itemIndex++)
                )
            }
            item {
                HomeHorizontalVideoList(
                    videos = videos, 
                    onVideoClick = onVideoClick,
                    modifier = Modifier.animateEnter(itemIndex++)
                )
            }
        }

        // Continue Watching Section
        state.continueWatching?.takeIf { it.isNotEmpty() }?.let { videos ->
            item {
                HomeSectionHeader(
                    title = stringResource(R.string.continue_watching),
                    onClick = { onSectionClick("watching") },
                    modifier = Modifier.animateEnter(itemIndex++)
                )
            }
            item {
                HomeHorizontalVideoList(
                    videos = videos, 
                    onVideoClick = onVideoClick,
                    modifier = Modifier.animateEnter(itemIndex++)
                )
            }
        }

        // Trending Section
        state.trending?.takeIf { it.isNotEmpty() }?.let { videos ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                        .animateEnter(itemIndex++),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                         HomeSectionHeader(
                            title = stringResource(R.string.trending),
                            onClick = { onSectionClick("trending") }
                        )
                    }
                    
                    IconButton(onClick = onTrendingCategoryClick) {
                        Icon(painterResource(R.drawable.ic_frame), contentDescription = stringResource(R.string.category))
                    }
                    
                    IconButton(onClick = onTrendingRegionClick) {
                        Icon(painterResource(R.drawable.ic_region), contentDescription = stringResource(R.string.region))
                    }
                }
            }
            item {
                HomeHorizontalVideoList(
                    videos = videos, 
                    onVideoClick = onVideoClick,
                    modifier = Modifier.animateEnter(itemIndex++)
                )
            }
        }

        // Bookmarks Section
        state.bookmarks?.takeIf { it.isNotEmpty() }?.let { playlists ->
            item {
                HomeSectionHeader(
                    title = stringResource(R.string.bookmarks),
                    onClick = { onSectionClick("bookmarks") },
                    modifier = Modifier.animateEnter(itemIndex++)
                )
            }
            item {
                HomeHorizontalPlaylistList(
                    playlists = playlists, 
                    onPlaylistClick = onPlaylistClick,
                    modifier = Modifier.animateEnter(itemIndex++)
                )
            }
        }

        // Playlists Section
        state.playlists?.takeIf { it.isNotEmpty() }?.let { playlists ->
            item {
                HomeSectionHeader(
                    title = stringResource(R.string.playlists),
                    onClick = { onSectionClick("playlists") },
                    modifier = Modifier.animateEnter(itemIndex++)
                )
            }
            item {
                HomeHorizontalPlaylistList(
                    playlists = playlists, 
                    onPlaylistClick = onPlaylistClick,
                    modifier = Modifier.animateEnter(itemIndex++)
                )
            }
        }
    }
}

@Composable
fun HomeSectionHeader(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HomeHorizontalVideoList(
    videos: List<VideoCardState>,
    onVideoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(videos) { video ->
            com.github.libretube.test.ui.components.SpotlightCard(
                state = video,
                onClick = { onVideoClick(video.videoId) }
                // modifier is handled inside SpotlightCard to fixed width
            )
        }
    }
}

@Composable
fun HomeHorizontalPlaylistList(
    playlists: List<PlaylistCardState>,
    onPlaylistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(playlists) { playlist ->
            PlaylistCard(
                state = playlist,
                onClick = { onPlaylistClick(playlist.playlistId) },
                modifier = Modifier.width(280.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LibreTubeTheme {
        HomeContent(
            state = HomeScreenState(
                featured = listOf(
                    VideoCardState("1", "Hello World", "Channel", "1M views", "10:00", null, null)
                )
            ),
            onVideoClick = {},
            onPlaylistClick = {},
            onSectionClick = {},
            onTrendingCategoryClick = {},
            onTrendingRegionClick = {}
        )
    }
}
