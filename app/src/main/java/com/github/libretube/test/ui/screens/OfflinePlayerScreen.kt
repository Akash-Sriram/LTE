package com.github.libretube.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.github.libretube.test.extensions.toID
import com.github.libretube.test.ui.models.PlayerViewModel
import com.github.libretube.test.ui.components.VideoSurface
import com.github.libretube.test.ui.components.FullPlayer
import com.github.libretube.test.ui.sheets.*
import com.github.libretube.test.obj.VideoStats
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OfflinePlayerScreen(
    playerViewModel: PlayerViewModel,
    onClose: () -> Unit
) {
    // Ensure we are in fullscreen/offline mode state if needed
    // But PlayerViewModel takes care of isOffline flag.
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val queue by playerViewModel.queue.collectAsState()
        val chapters by playerViewModel.chapters.collectAsState()
        
        var showQueue by remember { mutableStateOf(false) }
        var showChapters by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showQuality by remember { mutableStateOf(false) }
        var showCaptions by remember { mutableStateOf(false) }
        var showAudioTracks by remember { mutableStateOf(false) }
        var showSortDialog by remember { mutableStateOf(false) }
        var showWatchOptionsDialog by remember { mutableStateOf(false) }
        var showSleepTimer by remember { mutableStateOf(false) }
        var showStats by remember { mutableStateOf(false) }

        // Video Surface
        VideoSurface(
            modifier = Modifier.fillMaxSize(),
            viewModel = playerViewModel
        )

        // Full Player UI
        FullPlayer(
            viewModel = playerViewModel,
            alpha = 1f,
            onChaptersClick = { showChapters = true },
            onVideoOptionsClick = { showSettings = true }, // Use showSettings for options
            onCommentsClick = {}
        )

        // Sheets
        if (showQueue) {
            QueueSheet(
                queue = queue,
                onItemClick = { item -> 
                    showQueue = false
                    playerViewModel.onQueueItemClicked(item)
                },
                onDismissRequest = { showQueue = false },
                onSortClick = { showSortDialog = true },
                onWatchOptionsClick = { showWatchOptionsDialog = true }
            )
        }
        
        if (showSortDialog) {
             val options = listOf(
                "Creation Date", "Most Views", "Uploader Name", "Shuffle", "Reverse"
             )
             AlertDialog(
                 onDismissRequest = { showSortDialog = false },
                 title = { Text("Sort by") },
                 text = {
                     Column {
                         options.forEachIndexed { index, title ->
                            TextButton(
                                onClick = {
                                    playerViewModel.sortQueue(index)
                                    showSortDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(title, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                            }
                         }
                     }
                 },
                 confirmButton = {},
                 dismissButton = {
                     TextButton(onClick = { showSortDialog = false }) {
                         Text("Cancel")
                     }
                 }
             )
        }

        if (showWatchOptionsDialog) {
             val options = listOf("Mark as watched", "Mark as unwatched", "Remove watched videos")
             AlertDialog(
                 onDismissRequest = { showWatchOptionsDialog = false },
                 title = { Text("Watch positions") },
                 text = {
                     Column {
                         options.forEachIndexed { index, title ->
                            TextButton(
                                onClick = {
                                    playerViewModel.updateWatchPositions(index)
                                    showWatchOptionsDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(title, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                            }
                         }
                     }
                 },
                 confirmButton = {},
                 dismissButton = {
                     TextButton(onClick = { showWatchOptionsDialog = false }) {
                         Text("Cancel")
                     }
                 }
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
             ConsolidatedOptionsSheet(
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
                },
                onSleepTimerClick = {
                    showSettings = false
                    showSleepTimer = true
                },
                onStatsClick = {
                    showSettings = false
                    showStats = true
                }
            )
        }

        if (showSleepTimer) {
             com.github.libretube.test.ui.sheets.SleepTimerSheetCompose(
                 onDismiss = { showSleepTimer = false }
             )
        }

        if (showStats) {
            val currentStream by playerViewModel.currentStream.collectAsState()
            val stats = remember(currentStream) {
                VideoStats(
                    videoId = currentStream?.url?.toID() ?: "Unknown",
                    videoInfo = "ExoPlayer (Hardware)",
                    videoQuality = "Auto (Adaptive)",
                    audioInfo = "AAC / Opus"
                )
            }
            com.github.libretube.test.ui.sheets.StatsSheetCompose(
                stats = stats,
                onDismiss = { showStats = false }
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
