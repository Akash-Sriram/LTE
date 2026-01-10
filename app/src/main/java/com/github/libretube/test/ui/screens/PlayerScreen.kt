package com.github.libretube.test.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.github.libretube.test.ui.models.PlayerViewModel
import kotlinx.coroutines.launch

import com.github.libretube.test.ui.components.VideoSurface
import com.github.libretube.test.ui.components.DraggablePlayerPanel
import com.github.libretube.test.ui.sheets.QueueSheet
import com.github.libretube.test.ui.sheets.ChaptersSheet
import com.github.libretube.test.ui.sheets.PlayerSettingsSheet
import com.github.libretube.test.ui.sheets.QualitySelectionSheet
import com.github.libretube.test.ui.sheets.SubtitleSelectionSheet
import com.github.libretube.test.ui.sheets.AudioTrackSelectionSheet

enum class PlayerState {
    Collapsed,
    Expanded
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = LocalContext.current.resources.displayMetrics.heightPixels.toFloat()
    
    // AnchoredDraggable State
    val anchors = remember(configuration) {
        DraggableAnchors {
            PlayerState.Collapsed at (screenHeightPx - with(density) { 60.dp.toPx() }) // Mini player height
            PlayerState.Expanded at 0f
        }
    }
    
    val draggableState: AnchoredDraggableState<PlayerState> = remember {
        AnchoredDraggableState(
            initialValue = PlayerState.Collapsed,
            anchors = anchors,
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = exponentialDecay(),
            confirmValueChange = { true }
        )
    }

    val scope = rememberCoroutineScope()
    // Back Handling
    BackHandler(enabled = draggableState.currentValue == PlayerState.Expanded) {
        scope.launch {
            draggableState.animateTo(PlayerState.Collapsed)
        }
    }

    // Movable Player Surface to prevent re-inflation
    val movableVideoSurface = remember(playerViewModel) {
        movableContentOf { modifier: Modifier ->
            VideoSurface(modifier = modifier, viewModel = playerViewModel)
        }
    }

    // Observe Minimize/Maximize Commands
    LaunchedEffect(playerViewModel) {
        playerViewModel.expandPlayerTrigger.collect {
            draggableState.animateTo(PlayerState.Expanded)
        }
    }
    LaunchedEffect(playerViewModel) {
        playerViewModel.collapsePlayerTrigger.collect {
            draggableState.animateTo(PlayerState.Collapsed)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val queue by playerViewModel.queue.collectAsState()
        val chapters by playerViewModel.chapters.collectAsState()
        
        var showQueue by remember { mutableStateOf(false) }
        var showChapters by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showQuality by remember { mutableStateOf(false) }
        var showCaptions by remember { mutableStateOf(false) }
        var showAudioTracks by remember { mutableStateOf(false) }

        DraggablePlayerPanel(
            state = draggableState,
            onClose = onClose,
            viewModel = playerViewModel,
            videoSurface = movableVideoSurface,
            onQueueClick = { showQueue = true },
            onChaptersClick = { showChapters = true },
            onSettingsClick = { showSettings = true }
        )

        if (showQueue) {
            QueueSheet(
                queue = queue,
                onItemClick = { item -> 
                    showQueue = false
                    playerViewModel.onQueueItemClicked(item)
                },
                onDismissRequest = { showQueue = false }
            )
        }

        if (showChapters) {
            ChaptersSheet(
                chapters = chapters,
                onChapterClick = { chapter ->
                    showChapters = false
                    playerViewModel.seekTo(chapter.start * 1000)
                },
                onDismissRequest = { showChapters = false }
            )
        }

        if (showSettings) {
            PlayerSettingsSheet(
                viewModel = playerViewModel,
                onDismissRequest = { showSettings = false },
                onQualityClick = { 
                    showSettings = false
                    showQuality = true
                },
                onCaptionsClick = {
                    showSettings = false
                    showCaptions = true
                },
                onAudioTrackClick = {
                    showSettings = false
                    showAudioTracks = true
                }
            )
        }

        if (showQuality) {
            playerViewModel.playerController.value?.let { controller ->
                QualitySelectionSheet(
                    player = controller,
                    onDismiss = { showQuality = false }
                )
            }
        }

        if (showCaptions) {
            playerViewModel.playerController.value?.let { controller ->
                SubtitleSelectionSheet(
                    player = controller,
                    onDismiss = { showCaptions = false }
                )
            }
        }

        if (showAudioTracks) {
            playerViewModel.playerController.value?.let { controller ->
                AudioTrackSelectionSheet(
                    player = controller,
                    onDismiss = { showAudioTracks = false }
                )
            }
        }
    }
}
