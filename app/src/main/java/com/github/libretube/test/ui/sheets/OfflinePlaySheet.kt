package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflinePlayBottomSheet(
    videoTitle: String,
    downloadInfo: List<String>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onCancel) {
        OfflinePlaySheet(
            videoTitle = videoTitle,
            downloadInfo = downloadInfo,
            onConfirm = onConfirm,
            onCancel = onCancel
        )
    }
}

@Composable
fun OfflinePlaySheet(
    videoTitle: String,
    downloadInfo: List<String>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "Play offline?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = videoTitle,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false).padding(bottom = 16.dp)
        ) {
            items(downloadInfo) { info ->
                Text(
                    text = "• $info",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            ) {
                Text("Yes")
            }
        }
    }
}
