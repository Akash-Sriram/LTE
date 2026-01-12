package com.github.libretube.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
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
    onChaptersClick: () -> Unit,
    onVideoOptionsClick: () -> Unit,
    onCommentsClick: () -> Unit
) {
    if (alpha <= 0f) return

    val currentStream by viewModel.currentStream.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val context = LocalContext.current
    
    // Background Color Logic
    LaunchedEffect(currentStream?.thumbnail) {
        val url = currentStream?.thumbnail ?: return@LaunchedEffect
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .build()
        
        val result = loader.execute(request)
        if (result is SuccessResult) {
            val bitmap = result.image.toBitmap()
            // Fix: Palette API doesn't support Hardware Bitmaps, copy to Software
            val softwareBitmap = bitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
            softwareBitmap?.let {
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
                    endY = 1000f
                )
            )
    ) { 
        // 1. VIDEO SURFACE CONTAINER 
        // This box holds the Space for the Video Surface + The Overlay Controls
        // It stays at the top.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding() // Reserve space for status bar
                .aspectRatio(16f / 9f) // Standard video aspect ratio
        ) {
            // Space reserved for the video surface which is rendered in DraggablePlayerPanel
        }

        // 2. SCROLLABLE METADATA CONTENT
        // Everything else scrolls BELOW the video
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Fill remaining space
                .verticalScroll(rememberScrollState())
        ) {
            
            PlayerMetadataSection(viewModel = viewModel)
            
            // Actions Row (Like, Share, Download, etc.) - If separated from Metadata
            // For now contained in Metadata or we can add it here
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            CommentsSection(
                viewModel = viewModel, 
                onViewAllClick = onCommentsClick
            )
            
            RelatedVideosSection(viewModel = viewModel)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
