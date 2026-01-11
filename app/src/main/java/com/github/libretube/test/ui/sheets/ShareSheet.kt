package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.libretube.test.enums.ShareObjectType

@Composable
fun ShareSheet(
    id: String,
    title: String,
    shareObjectType: ShareObjectType,
    initialTimestamp: String,
    onCopyClick: (String) -> Unit,
    onShareClick: (String) -> Unit
) {
    var withTimestamp by remember { mutableStateOf(false) }
    var timestamp by remember { mutableStateOf(initialTimestamp) }

    val linkText = remember(withTimestamp, timestamp) {
        generateLinkText(id, shareObjectType, withTimestamp, timestamp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (shareObjectType == ShareObjectType.VIDEO) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Include timestamp")
                Switch(
                    checked = withTimestamp,
                    onCheckedChange = { withTimestamp = it }
                )
            }

            if (withTimestamp) {
                OutlinedTextField(
                    value = timestamp,
                    onValueChange = { timestamp = it },
                    label = { Text("Timestamp (seconds)") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Text(
                text = linkText,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onCopyClick(linkText) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Copy")
            }

            Button(
                onClick = { onShareClick(linkText) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Share")
            }
        }
    }
}

private fun generateLinkText(
    id: String,
    shareObjectType: ShareObjectType,
    withTimestamp: Boolean,
    timestamp: String
): String {
    val YOUTUBE_FRONTEND_URL = "https://www.youtube.com"
    val YOUTUBE_SHORT_URL = "https://youtu.be"

    return when (shareObjectType) {
        ShareObjectType.VIDEO -> {
            val baseUrl = "$YOUTUBE_SHORT_URL/$id"
            if (withTimestamp && timestamp.isNotEmpty()) {
                "$baseUrl?t=$timestamp"
            } else {
                baseUrl
            }
        }
        ShareObjectType.PLAYLIST -> "$YOUTUBE_FRONTEND_URL/playlist?list=$id"
        else -> "$YOUTUBE_FRONTEND_URL/channel/$id"
    }
}
