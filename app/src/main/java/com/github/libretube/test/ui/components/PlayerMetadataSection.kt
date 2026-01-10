package com.github.libretube.test.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.github.libretube.test.ui.models.PlayerViewModel
import com.github.libretube.test.ui.models.PlayerCommandEvent

@Composable
fun PlayerMetadataSection(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val uploader by viewModel.uploader.collectAsState()
    val uploaderAvatar by viewModel.uploaderAvatar.collectAsState()
    val subscriberCount by viewModel.subscriberCount.collectAsState()
    val description by viewModel.description.collectAsState()
    val views by viewModel.views.collectAsState()
    val likes by viewModel.likes.collectAsState()
    val isSubscribed by viewModel.isSubscribed.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(16.dp)) {
        // Uploader Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = uploaderAvatar,
                contentDescription = "Uploader Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uploader,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subscriberCount != null) {
                    Text(
                        text = "${formatCount(subscriberCount!!)} subscribers",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Button(
                onClick = { viewModel.toggleSubscription() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSubscribed) Color.DarkGray else Color.White,
                    contentColor = if (isSubscribed) Color.White else Color.Black
                )
            ) {
                Text(if (isSubscribed) "Subscribed" else "Subscribe")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionButton(icon = Icons.Default.ThumbUp, label = formatCount(likes))
            ActionButton(icon = Icons.Default.ThumbDown, label = "Dislike")
            ActionButton(
                icon = Icons.Default.Share, 
                label = "Share",
                onClick = { viewModel.triggerPlayerCommand(PlayerCommandEvent.Share) }
            )
            ActionButton(
                icon = Icons.Default.Download, 
                label = "Download",
                onClick = { viewModel.triggerPlayerCommand(PlayerCommandEvent.Download) }
            )
            ActionButton(
                icon = Icons.Default.Add, 
                label = "Save",
                onClick = { viewModel.triggerPlayerCommand(PlayerCommandEvent.SaveToPlaylist) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description Section
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable { isExpanded = !isExpanded }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${formatCount(views)} views",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = description,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isExpanded && description.length > 100) {
                    Text(
                        text = "Show more",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}


