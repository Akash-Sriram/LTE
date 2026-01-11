package com.github.libretube.test.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.libretube.test.R
import com.github.libretube.test.api.TrendingCategory
import com.github.libretube.test.api.obj.StreamItem
import com.github.libretube.test.constants.PreferenceKeys
import com.github.libretube.test.helpers.LocaleHelper
import com.github.libretube.test.helpers.PreferenceHelper
import com.github.libretube.test.ui.components.VideoCard
import com.github.libretube.test.ui.components.VideoCardState
import com.github.libretube.test.ui.models.TrendsViewModel
import com.github.libretube.test.util.TextUtils
import kotlinx.coroutines.launch
import com.github.libretube.test.ui.sheets.DownloadBottomSheet
import com.github.libretube.test.ui.sheets.ShareBottomSheet
import com.github.libretube.test.enums.ShareObjectType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    navController: androidx.navigation.NavController,
    viewModel: com.github.libretube.test.ui.models.TrendsViewModel
) {
    val context = LocalContext.current
    // Fetch categories directly as in Fragment
    val categories = remember { com.github.libretube.test.api.MediaServiceRepository.instance.getTrendingCategories() }
    
    // TODO: Implement VideoOptionsBottomSheet as Composable or equivalent
    // For now, we omit the long click sheet or implement a basic one if needed
    
    TrendsContent(
        categories = categories,
        viewModel = viewModel,
        onVideoClick = { streamItem ->
            com.github.libretube.test.helpers.NavigationHelper.navigateVideo(context, streamItem.url)
        },
        onVideoLongClick = { streamItem ->
            // Handled in TrendsContent via state
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsContent(
    categories: List<com.github.libretube.test.api.TrendingCategory>,
    viewModel: com.github.libretube.test.ui.models.TrendsViewModel,
    onVideoClick: (com.github.libretube.test.api.obj.StreamItem) -> Unit,
    onVideoLongClick: (com.github.libretube.test.api.obj.StreamItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { categories.size })
    val scope = rememberCoroutineScope()
    var showRegionDialog by remember { mutableStateOf(false) }
    
    val trendingVideos by viewModel.trendingVideos.observeAsState(emptyMap())
    
    var showVideoOptions by remember { mutableStateOf(false) }
    var selectedStreamItem by remember { mutableStateOf<com.github.libretube.test.api.obj.StreamItem?>(null) }
    var showDownloadSheet by remember { mutableStateOf(false) }
    var showAddToPlaylist by remember { mutableStateOf(false) } // Still legacy trigger for now
    var showShareSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top bar with tabs and region button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (categories.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.weight(1f),
                    edgePadding = 0.dp
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(stringResource(category.titleRes)) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            IconButton(
                onClick = { showRegionDialog = true },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_region),
                    contentDescription = stringResource(R.string.region)
                )
            }
        }

        // Pager for trending content
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val category = categories[page]
            TrendingContentPage(
                category = category,
                trendingData = trendingVideos[category],
                onRefresh = {
                    viewModel.fetchTrending(context, category)
                },
                onVideoClick = onVideoClick,
                onVideoLongClick = { streamItem ->
                    selectedStreamItem = streamItem
                    showVideoOptions = true
                },
                onShowRegionDialog = { showRegionDialog = true }
            )
        }
    }

    if (showRegionDialog) {
        RegionSelectionDialog(
            onDismiss = { showRegionDialog = false },
            onRegionSelected = {
                showRegionDialog = false
                val currentCategory = categories[pagerState.currentPage]
                viewModel.fetchTrending(context, currentCategory)
            }
        )
    }

    // Fetch trending on first composition
    LaunchedEffect(Unit) {
        categories.forEach { category ->
            viewModel.fetchTrending(context, category)
        }
    }

    if (showVideoOptions && selectedStreamItem != null) {
        com.github.libretube.test.ui.sheets.VideoOptionsSheet(
            streamItem = selectedStreamItem!!,
            onDismissRequest = { showVideoOptions = false },
            onShareClick = {
                showShareSheet = true
            },
            onDownloadClick = {
                showDownloadSheet = true
            }
        )
    }

    if (showShareSheet && selectedStreamItem != null) {
        ShareBottomSheet(
            id = selectedStreamItem!!.url?.substringAfterLast("/") ?: "",
            title = selectedStreamItem!!.title ?: "",
            shareObjectType = ShareObjectType.VIDEO,
            initialTimestamp = "0",
            onDismissRequest = { showShareSheet = false }
        )
    }

    if (showDownloadSheet && selectedStreamItem != null) {
        DownloadBottomSheet(
            videoId = selectedStreamItem!!.url?.substringAfterLast("/") ?: "",
            onDismissRequest = { showDownloadSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingContentPage(
    category: TrendingCategory,
    trendingData: TrendsViewModel.TrendingStreams?,
    onRefresh: () -> Unit,
    onVideoClick: (StreamItem) -> Unit,
    onVideoLongClick: (StreamItem) -> Unit,
    onShowRegionDialog: () -> Unit
) {
    val context = LocalContext.current
    val isLoading = trendingData == null
    val streams = trendingData?.streams ?: emptyList()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            onRefresh()
        }
    }

    LaunchedEffect(trendingData) {
        isRefreshing = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                if (streams.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.change_region),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onShowRegionDialog) {
                            Text(stringResource(R.string.change))
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(300.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(streams, key = { it.url.orEmpty() }) { stream ->
                            VideoCard(
                                state = VideoCardState(
                                    videoId = stream.url?.substringAfterLast("/") ?: "",
                                    title = stream.title ?: "",
                                    uploaderName = stream.uploaderName ?: "",
                                    views = TextUtils.formatViewsString(
                                        context,
                                        stream.views ?: -1L,
                                        stream.uploaded ?: 0L
                                    ),
                                    duration = stream.duration?.let { 
                                        android.text.format.DateUtils.formatElapsedTime(it) 
                                    } ?: "",
                                    thumbnailUrl = stream.thumbnail,
                                    uploaderAvatarUrl = stream.uploaderAvatar
                                ),
                                onClick = { onVideoClick(stream) },
                                onLongClick = { onVideoLongClick(stream) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegionSelectionDialog(
    onDismiss: () -> Unit,
    onRegionSelected: () -> Unit
) {
    val context = LocalContext.current
    val countries = remember { LocaleHelper.getAvailableCountries() }
    val currentRegionPref = remember { PreferenceHelper.getTrendingRegion(context) }
    var selectedIndex by remember {
        mutableIntStateOf(countries.indexOfFirst { it.code == currentRegionPref })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.region)) },
        text = {
            Column {
                countries.forEachIndexed { index, country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = country.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    PreferenceHelper.putString(
                        PreferenceKeys.REGION,
                        countries[selectedIndex].code
                    )
                    onRegionSelected()
                }
            ) {
                Text(stringResource(R.string.okay))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
