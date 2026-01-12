package com.github.libretube.test.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.libretube.test.ui.models.PlayerViewModel
import com.github.libretube.test.ui.components.formatDuration

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PlayerProgressSection(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    
    // Local state for seeking to ensure smooth slider movement
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    val sliderPosition = if (isSeeking) seekPosition else currentPosition.toFloat()
    val sliderDuration = duration.toFloat().coerceAtLeast(1f)

    Column(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 0.dp)
    ) {
        Slider(
            value = sliderPosition,
            onValueChange = { 
                isSeeking = true
                seekPosition = it
            },
            onValueChangeFinished = {
                isSeeking = false
                viewModel.seekTo(seekPosition.toLong())
            },
            valueRange = 0f..sliderDuration,
            modifier = Modifier.height(16.dp), // More compact hit area
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    thumbSize = androidx.compose.ui.unit.DpSize(14.dp, 14.dp), // Slightly larger thumb for better touch
                    colors = SliderDefaults.colors(thumbColor = Color.White)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(3.dp), // Slightly thicker track for visibility
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        )
    }
}
