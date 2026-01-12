package com.github.libretube.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.github.libretube.test.R
import com.github.libretube.test.ui.theme.LibreTubeTheme

import androidx.compose.runtime.Immutable

@Immutable
data class VideoCardState(
    val videoId: String,
    val title: String,
    val uploaderName: String,
    val views: String,
    val duration: String,
    val thumbnailUrl: String?,
    val uploaderAvatarUrl: String?,
    val watchProgress: Float? = null // 0.0 to 1.0
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VideoCard(
    state: VideoCardState,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            )
            .padding(bottom = 16.dp) // Spacing between items
    ) {
        // Thumbnail Section - 16:9 aspect ratio with Large rounded corners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp) // Inset thumbnail
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.large) // 24dp rounded corners
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = com.github.libretube.test.util.rememberContentWithCrossfade(state.thumbnailUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Duration Badge - Pill shape
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.7f), 
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = state.duration,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            
            // Watch Progress
            state.watchProgress?.let { progress ->
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    drawStopIndicator = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp) // Align with thumbnail inset
        ) {
            // Channel Avatar
            if (!state.uploaderAvatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = com.github.libretube.test.util.rememberContentWithCrossfade(state.uploaderAvatarUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp) // Larger avatar
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleMedium.copy( // Bolder title
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${state.uploaderName} • ${state.views}",
                    style = MaterialTheme.typography.bodyMedium, // Larger metadata
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Optional: Menu icon (3 dots) could go here if needed
        }
    }
}

@Preview
@Composable
private fun VideoCardPreview() {
    LibreTubeTheme {
        VideoCard(
            state = VideoCardState(
                videoId = "123",
                title = "Amazing Video Title That Is Very Long And Wraps To Two Lines",
                views = "1.2M views • 2 days ago",
                duration = "10:05",
                thumbnailUrl = "",
                uploaderName = "Uploader Name",
                uploaderAvatarUrl = ""
            ),
            onClick = {}
        )
    }
}
