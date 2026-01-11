package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.github.libretube.test.R
import com.github.libretube.test.helpers.ClipboardHelper
import com.github.libretube.test.obj.VideoStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsSheetCompose(
    stats: VideoStats,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.stats_for_nerds),
                style = MaterialTheme.typography.titleLarge
            )

            // Video ID Row with Copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Video ID",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stats.videoId,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace
                    )
                }
                IconButton(
                    onClick = {
                        ClipboardHelper.save(context, "text", stats.videoId)
                        // Toast or cleanup handled by helper? usually toasts.
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = stringResource(R.string.copy_tooltip)
                    )
                }
            }
            
            HorizontalDivider()

            // Video Quality
            StatRow(label = "Video Quality", value = stats.videoQuality)
            
            // Audio Info
            StatRow(label = "Audio Info", value = stats.audioInfo)
            
            // Video Info (General Format/Codec)
            StatRow(label = "Video Codec", value = stats.videoInfo)
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace
        )
    }
}
