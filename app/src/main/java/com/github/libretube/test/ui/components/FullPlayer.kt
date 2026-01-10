package com.github.libretube.test.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.github.libretube.test.ui.models.PlayerViewModel

@Composable
fun FullPlayer(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    alpha: Float,
    onQueueClick: () -> Unit,
    onChaptersClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    if (alpha > 0f) {
        val scrollState = rememberScrollState()
        Column(
            modifier = modifier
                .graphicsLayer(alpha = alpha)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) { 
            // Video area spacer (The VideoSurface is fixed/movable on top of this, 
            // but we need a spacer to push the controls down)
            Spacer(modifier = Modifier.height(250.dp)) 

            PlayerControls(
                viewModel = viewModel,
                modifier = Modifier.weight(1f),
                onQueueClick = onQueueClick,
                onChaptersClick = onChaptersClick,
                onSettingsClick = onSettingsClick
            )
            
            PlayerMetadataSection(viewModel = viewModel)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
