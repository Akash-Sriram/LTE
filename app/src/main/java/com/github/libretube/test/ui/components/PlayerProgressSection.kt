package com.github.libretube.test.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.libretube.test.ui.models.PlayerViewModel
import com.github.libretube.test.ui.components.formatDuration

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

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatDuration(sliderPosition.toLong()), color = Color.White, style = MaterialTheme.typography.bodySmall)
            Text(formatDuration(duration), color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
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
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.LightGray.copy(alpha = 0.5f)
            )
        )
    }
}
