package com.github.libretube.test.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.libretube.test.ui.models.PlayerViewModel

@Composable
fun PlayerControls(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    onQueueClick: () -> Unit,
    onChaptersClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onVideoOptionsClick: () -> Unit
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val title by viewModel.title.collectAsState()
    val uploader by viewModel.uploader.collectAsState()
    val chapters by viewModel.chapters.collectAsState()


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Controls (Close, Settings)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { /* Collapse handled by parent via Draggable */ }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }
        
        // Metadata
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(uploader, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        }

        // Center Controls (Play/Pause, Nex/Prev)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.skipPrevious() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(onClick = { viewModel.skipNext() }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White)
            }
        }

        // Bottom Controls (Seekbar, Time)
        Column {
             // Extra Actions Row (Queue, Chapters)
             Row(
                 modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                 horizontalArrangement = Arrangement.SpaceEvenly
             ) {
                 IconButton(onClick = onQueueClick) {
                     Icon(Icons.Default.List, contentDescription = "Queue", tint = Color.White)
                 }
                 if (chapters.isNotEmpty()) {
                     IconButton(onClick = onChaptersClick) {
                         Icon(Icons.Default.Menu, contentDescription = "Chapters", tint = Color.White)
                     }
                 }

                 IconButton(onClick = onVideoOptionsClick) {
                     Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                 }
             }

            PlayerProgressSection(viewModel = viewModel)
        }
    }
}

// formatDuration moved to DurationFormatter.kt


