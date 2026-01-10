package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.libretube.test.ui.models.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsSheet(
    viewModel: PlayerViewModel,
    onDismissRequest: () -> Unit,
    onQualityClick: () -> Unit,
    onCaptionsClick: () -> Unit
) {
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    
    val availableSpeeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Player Settings",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Playback Speed Section
            Text(
                text = "Playback Speed",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn {
                items(availableSpeeds) { speed ->
                    SpeedOption(
                        speed = speed,
                        isSelected = speed == playbackSpeed,
                        onClick = {
                            viewModel.setPlaybackSpeed(speed)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Extra Settings
            ListItem(
                headlineContent = { Text("Quality") },
                modifier = Modifier.clickable { onQualityClick() },
                trailingContent = { Icon(androidx.compose.material.icons.Icons.Default.KeyboardArrowRight, contentDescription = null) }
            )
            ListItem(
                headlineContent = { Text("Captions") },
                modifier = Modifier.clickable { onCaptionsClick() },
                trailingContent = { Icon(androidx.compose.material.icons.Icons.Default.KeyboardArrowRight, contentDescription = null) }
            )
        }
    }
}

@Composable
fun SpeedOption(
    speed: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${speed}x",
            style = MaterialTheme.typography.bodyLarge
        )
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
