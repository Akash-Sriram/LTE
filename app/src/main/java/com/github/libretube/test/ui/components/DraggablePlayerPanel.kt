package com.github.libretube.test.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.github.libretube.test.ui.models.PlayerViewModel
import com.github.libretube.test.ui.screens.PlayerState
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggablePlayerPanel(
    state: AnchoredDraggableState<PlayerState>,
    onClose: () -> Unit,
    viewModel: PlayerViewModel,
    videoSurface: @Composable (Modifier) -> Unit,
    onQueueClick: () -> Unit,
    onChaptersClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onVideoOptionsClick: () -> Unit
) {
    val offset = state.requireOffset()
    val maxOffset = state.anchors.positionOf(PlayerState.Collapsed)
    val progress = (offset / maxOffset).coerceIn(0f, 1f)
    
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val miniWidth = 120.dp
    val miniHeight = 67.5.dp
    val fullWidth = screenWidth
    val fullHeight = screenWidth * 9f / 16f

    Box(
        modifier = Modifier
            .offset { IntOffset(x = 0, y = offset.roundToInt()) }
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Vertical
            )
    ) {
        val scaleX = androidx.compose.ui.util.lerp(1f, miniWidth.value / fullWidth.value, progress)
        val scaleY = androidx.compose.ui.util.lerp(1f, miniHeight.value / fullHeight.value, progress)
        
        // Video container with graphicsLayer for high-performance scaling
        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.TopStart)
                .graphicsLayer {
                    this.scaleX = scaleX
                    this.scaleY = scaleY
                    this.transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                }
                .zIndex(1f)
        ) {
            // Apply inverse scale to video surface to prevent aspect-ratio distortion (squashing)
            videoSurface(
                Modifier
                    .size(fullWidth, fullHeight)
                    .graphicsLayer {
                        this.scaleX = 1f / scaleX
                        this.scaleY = 1f / scaleY
                        this.transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                    }
            )

            // Gesture Overlay: Captured in Mini Player mode to prevent SurfaceView touch swallowing
            if (progress > 0.8f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Transparent)
                        .clickable(enabled = false) {} // Just to swallow/allow drag
                )
            }
        }

        
        val currentStream by viewModel.currentStream.collectAsState()
        val isPlaying by viewModel.isPlaying.collectAsState()
        val playbackPosition by viewModel.playbackPosition.collectAsState()
        val duration by viewModel.duration.collectAsState()
        val playbackProgress = if (duration > 0) (playbackPosition.toFloat() / duration) else 0f
        
        MiniPlayer(
            modifier = Modifier
                .graphicsLayer(alpha = progress)
                .fillMaxWidth()
                .height(miniHeight)
                .padding(start = miniWidth),
            title = currentStream?.title ?: "",
            channelName = currentStream?.uploaderName ?: "",
            thumbnailUrl = currentStream?.thumbnail,
            isPlaying = isPlaying,
            progress = playbackProgress,
            onPlayPauseClick = { viewModel.togglePlayPause() },
            onClose = onClose
        )

        FullPlayer(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxSize(),
            alpha = (1f - progress * 2f).coerceIn(0f, 1f), // Fade out faster
            onQueueClick = onQueueClick,
            onChaptersClick = onChaptersClick,
            onSettingsClick = onSettingsClick,
            onVideoOptionsClick = onVideoOptionsClick
        )
    }
}
