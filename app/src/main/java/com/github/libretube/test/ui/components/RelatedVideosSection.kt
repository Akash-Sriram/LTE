package com.github.libretube.test.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.libretube.test.ui.models.PlayerViewModel

@Composable
fun RelatedVideosSection(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val relatedVideos by viewModel.relatedVideos.collectAsState()

    if (relatedVideos.isNotEmpty()) {
        Column(modifier = modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Related Videos",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            relatedVideos.forEach { video ->
                RecommendationCard(
                    item = video,
                    onClick = { viewModel.onQueueItemClicked(video) }
                )
            }
        }
    }
}
