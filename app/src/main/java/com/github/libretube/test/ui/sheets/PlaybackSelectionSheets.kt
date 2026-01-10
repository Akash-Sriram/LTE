package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import com.github.libretube.test.enums.PlayerCommand
import com.github.libretube.test.services.AbstractPlayerService
import androidx.core.os.bundleOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualitySelectionSheet(
    player: MediaController,
    onDismiss: () -> Unit
) {
    val tracks = player.currentTracks
    val resolutions = remember(tracks) {
        getAvailableResolutions(tracks)
    }
    val currentResolution = getCurrentVideoHeight(tracks)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        SelectionList(
            title = "Video Quality",
            items = resolutions,
            selectedItem = resolutions.find { it.value == currentResolution } ?: resolutions.firstOrNull { it.value == Int.MAX_VALUE },
            onItemSelected = { res ->
                player.sendCustomCommand(
                    AbstractPlayerService.runPlayerActionCommand,
                    bundleOf(PlayerCommand.SET_RESOLUTION.name to res.value)
                )
                onDismiss()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleSelectionSheet(
    player: MediaController,
    onDismiss: () -> Unit
) {
    val tracks = player.currentTracks
    val subtitleTracks = remember(tracks) {
        getTrackOptions(tracks, C.TRACK_TYPE_TEXT)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        SelectionList(
            title = "Subtitles",
            items = subtitleTracks,
            selectedItem = subtitleTracks.find { it.isSelected },
            onItemSelected = { option ->
                selectTrack(player, option.value, option.trackIndex)
                onDismiss()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTrackSelectionSheet(
    player: MediaController,
    onDismiss: () -> Unit
) {
    val tracks = player.currentTracks
    val audioTracks = remember(tracks) {
        getTrackOptions(tracks, C.TRACK_TYPE_AUDIO)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        SelectionList(
            title = "Audio Tracks",
            items = audioTracks,
            selectedItem = audioTracks.find { it.isSelected },
            onItemSelected = { option ->
                selectTrack(player, option.value, option.trackIndex)
                onDismiss()
            }
        )
    }
}

private fun selectTrack(player: MediaController, groupOrNull: Any?, trackIndex: Int) {
    if (groupOrNull == "none") {
        val newParameters = player.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .setIgnoredTextSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()
        player.trackSelectionParameters = newParameters
        return
    }

    val group = groupOrNull as? Tracks.Group ?: return
    val newParameters = player.trackSelectionParameters
        .buildUpon()
        .clearOverridesOfType(group.type)
        .addOverride(androidx.media3.common.TrackSelectionOverride(group.mediaTrackGroup, trackIndex))
        .build()
    
    player.trackSelectionParameters = newParameters
}

@Composable
fun <T> SelectionList(
    title: String,
    items: List<SelectionOption<T>>,
    selectedItem: SelectionOption<T>?,
    onItemSelected: (SelectionOption<T>) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )
        LazyColumn {
            items(items) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemSelected(item) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item == selectedItem) MaterialTheme.colorScheme.primary else Color.Unspecified
                    )
                    if (item == selectedItem) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

data class SelectionOption<T>(
    val label: String,
    val value: T,
    val trackIndex: Int = 0,
    val isSelected: Boolean = false
)

private fun getAvailableResolutions(tracks: Tracks): List<SelectionOption<Int>> {
    val resolutions = mutableListOf<SelectionOption<Int>>()
    tracks.groups.forEach { group ->
        if (group.type == C.TRACK_TYPE_VIDEO) {
            for (i in 0 until group.length) {
                val format = group.getTrackFormat(i)
                if (format.height > 0) {
                    resolutions.add(SelectionOption("${format.height}p", format.height, i))
                }
            }
        }
    }
    val sorted = resolutions.distinctBy { it.value }.sortedByDescending { it.value }
    return listOf(SelectionOption("Auto", Int.MAX_VALUE)) + sorted
}

private fun getCurrentVideoHeight(tracks: Tracks): Int {
    tracks.groups.forEach { group ->
        if (group.type == C.TRACK_TYPE_VIDEO) {
            for (i in 0 until group.length) {
                if (group.isTrackSelected(i)) return group.getTrackFormat(i).height
            }
        }
    }
    return Int.MAX_VALUE
}

private fun getTrackOptions(tracks: Tracks, trackType: Int): List<SelectionOption<Any>> {
    val options = mutableListOf<SelectionOption<Any>>()
    
    // Add "None" for subtitles
    if (trackType == C.TRACK_TYPE_TEXT) {
        options.add(SelectionOption("Off", "none", isSelected = !tracks.groups.any { it.type == C.TRACK_TYPE_TEXT && it.isSelected }))
    }

    tracks.groups.forEach { group ->
        if (group.type == trackType) {
            for (i in 0 until group.length) {
                val format = group.getTrackFormat(i)
                val label = format.language ?: format.label ?: "Track $i"
                options.add(SelectionOption(label, group, i, group.isTrackSelected(i)))
            }
        }
    }
    return options
}
