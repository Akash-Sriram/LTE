package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.github.libretube.test.api.obj.StreamItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    queue: List<StreamItem>,
    onItemClick: (StreamItem) -> Unit,
    onDismissRequest: () -> Unit,
    onSortClick: () -> Unit = {},
    onWatchOptionsClick: () -> Unit = {}
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Queue",
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    IconButton(onClick = onSortClick) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Sort")
                    }
                    IconButton(onClick = onWatchOptionsClick) {
                        // Using same icon for now or just generic more
                        // Icon(Icons.Default.Settings, contentDescription = "Watch Options")
                    }
                }
            }
            LazyColumn {
                itemsIndexed(queue) { index, item ->
                    QueueItem(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
fun QueueItem(item: StreamItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        AsyncImage(
            model = item.thumbnail,
            contentDescription = null,
            modifier = Modifier
                .width(120.dp)
                .height(68.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = item.title ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.uploaderName ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
