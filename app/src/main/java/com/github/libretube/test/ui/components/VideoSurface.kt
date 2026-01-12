package com.github.libretube.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.libretube.test.ui.models.PlayerViewModel

@Composable
fun VideoSurface(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel,
    gesturesEnabled: Boolean = true
) {
    val playerController by viewModel.playerController.collectAsState()
    val resizeMode by viewModel.resizeMode.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { context ->
                androidx.media3.ui.PlayerView(context).apply {
                    useController = false // We use our own controls
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                if (playerController != null && view.player != playerController) {
                    // Attaching player to view
                    view.player = playerController
                }
                view.resizeMode = resizeMode
            },
            onRelease = { view ->
                view.player = null // Only detach, don't release singleton player
            },
            modifier = Modifier.matchParentSize()
        )
        
        // Gesture overlay on top of video (only when expanded)
        if (gesturesEnabled) {
            PlayerGestureOverlay(
                onSeek = { seekAmount ->
                    val currentPosition = viewModel.currentPosition.value
                    val newPosition = (currentPosition + seekAmount).coerceAtLeast(0)
                    viewModel.seekTo(newPosition)
                },
                onTap = { viewModel.toggleControls() }
            )
        }
        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
