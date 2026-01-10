package com.github.libretube.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
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
        val currentStream by viewModel.currentStream.collectAsState()
        val dominantColor by viewModel.dominantColor.collectAsState()
        val surfaceColor = MaterialTheme.colorScheme.surface
        val context = LocalContext.current

        LaunchedEffect(currentStream?.thumbnail) {
            val url = currentStream?.thumbnail ?: return@LaunchedEffect
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()
            
            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = result.image.toBitmap()
                bitmap.let {
                    val palette = Palette.from(it).generate()
                    val color = palette.getVibrantColor(palette.getDominantColor(0))
                    if (color != 0) {
                        viewModel.updateDominantColor(Color(color))
                    }
                }
            }
        }

        Column(
            modifier = modifier
                .graphicsLayer(alpha = alpha)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.3f),
                            surfaceColor
                        ),
                        startY = 0f,
                        endY = 1000f // Approximate height for gradient fade
                    )
                )
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
            
            RelatedVideosSection(viewModel = viewModel)
            
            CommentsSection(viewModel = viewModel)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
