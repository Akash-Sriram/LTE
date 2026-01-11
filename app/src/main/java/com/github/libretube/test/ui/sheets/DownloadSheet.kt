package com.github.libretube.test.ui.sheets

import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.libretube.test.R
import com.github.libretube.test.api.MediaServiceRepository
import com.github.libretube.test.api.obj.StreamFormat
import com.github.libretube.test.api.obj.Streams
import com.github.libretube.test.api.obj.Subtitle
import com.github.libretube.test.extensions.getWhileDigit
import com.github.libretube.test.helpers.DownloadHelper
import com.github.libretube.test.helpers.PreferenceHelper
import com.github.libretube.test.parcelable.DownloadData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val VIDEO_DOWNLOAD_QUALITY = "video_download_quality"
private const val VIDEO_DOWNLOAD_FORMAT = "video_download_format"
private const val AUDIO_DOWNLOAD_QUALITY = "audio_download_quality"
private const val AUDIO_DOWNLOAD_FORMAT = "audio_download_format"
private const val SUBTITLE_LANGUAGE = "subtitle_download_language"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadBottomSheet(
    videoId: String,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var streams by remember { mutableStateOf<Streams?>(null) }
    
    LaunchedEffect(videoId) {
        withContext(Dispatchers.IO) {
            try {
                streams = MediaServiceRepository.instance.getStreams(videoId)
            } catch (e: Exception) {
                // Error handling...
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        if (streams == null) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val videoStreams = remember(streams) { 
                streams!!.videoStreams.filter { !it.url.isNullOrEmpty() && !it.format.orEmpty().contains("HLS") }
                    .sortedWith(compareBy<StreamFormat> { it.videoOnly }.thenByDescending { it.quality.getWhileDigit() })
            }
            val audioStreams = remember(streams) {
                streams!!.audioStreams.filter { !it.url.isNullOrEmpty() }
                    .sortedByDescending { it.quality.getWhileDigit() }
            }
            val subtitles = remember(streams) {
                streams!!.subtitles.filter { !it.url.isNullOrEmpty() && !it.name.isNullOrEmpty() }.sortedBy { it.name }
            }

            val initialVideoIndex = remember(videoStreams) { (getSelIndex(videoStreams, VIDEO_DOWNLOAD_QUALITY, VIDEO_DOWNLOAD_FORMAT) ?: -1) + 1 }
            val initialAudioIndex = remember(audioStreams) { (getSelIndex(audioStreams, AUDIO_DOWNLOAD_QUALITY, AUDIO_DOWNLOAD_FORMAT) ?: -1) + 1 }
            val initialSubtitleIndex = remember(subtitles) { (subtitles.indexOfFirst { it.code == getSel(SUBTITLE_LANGUAGE) }.takeIf { it != -1 } ?: -1) + 1 }

            DownloadSheet(
                streams = streams,
                initialVideoIndex = initialVideoIndex,
                initialAudioIndex = initialAudioIndex,
                initialSubtitleIndex = initialSubtitleIndex,
                onDownloadConfirm = { vPos, aPos, sPos ->
                    val videoStream = videoStreams.getOrNull(vPos)
                    val audioStream = audioStreams.getOrNull(aPos)
                    val subtitle = subtitles.getOrNull(sPos)
                    
                    if (videoStream == null && audioStream == null && subtitle == null) {
                        Toast.makeText(context, R.string.nothing_selected, Toast.LENGTH_SHORT).show()
                    } else {
                        // Save selections
                        PreferenceHelper.putString(SUBTITLE_LANGUAGE, subtitle?.code.orEmpty())
                        PreferenceHelper.putString(VIDEO_DOWNLOAD_FORMAT, videoStream?.format.orEmpty())
                        PreferenceHelper.putString(VIDEO_DOWNLOAD_QUALITY, videoStream?.quality.orEmpty())
                        PreferenceHelper.putString(AUDIO_DOWNLOAD_FORMAT, audioStream?.format.orEmpty())
                        PreferenceHelper.putString(AUDIO_DOWNLOAD_QUALITY, audioStream?.quality.orEmpty())

                        val downloadData = DownloadData(
                            videoId = videoId,
                            videoFormat = videoStream?.format,
                            videoQuality = videoStream?.quality,
                            audioFormat = audioStream?.format,
                            audioQuality = audioStream?.quality,
                            audioLanguage = audioStream?.audioTrackLocale,
                            subtitleCode = subtitle?.code
                        )
                        DownloadHelper.startDownloadService(context, downloadData)
                        onDismissRequest()
                    }
                },
                onCancel = onDismissRequest
            )
        }
    }
}

private fun getSel(key: String) = PreferenceHelper.getString(key, "")

private fun getSelIndex(streams: List<StreamFormat>, qualityKey: String, formatKey: String): Int? {
    val quality = getSel(qualityKey)
    val format = getSel(formatKey)
    if (quality.isBlank()) return null

    streams.forEachIndexed { index, streamFormat ->
        if (quality == streamFormat.quality && format == streamFormat.format) return index
    }
    streams.forEachIndexed { index, streamFormat ->
        if (quality == streamFormat.quality) return index
    }
    val qualityInt = quality.getWhileDigit() ?: return null
    streams.forEachIndexed { index, streamFormat ->
        if ((streamFormat.quality.getWhileDigit() ?: Int.MAX_VALUE) < qualityInt) return index
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadSheet(
    streams: Streams?,
    initialVideoIndex: Int = 0,
    initialAudioIndex: Int = 0,
    initialSubtitleIndex: Int = 0,
    onDownloadConfirm: (Int, Int, Int) -> Unit,
    onCancel: () -> Unit
) {
    if (streams == null) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val context = LocalContext.current
    var videoIndex by remember { mutableIntStateOf(initialVideoIndex) }
    var audioIndex by remember { mutableIntStateOf(initialAudioIndex) }
    var subtitleIndex by remember { mutableIntStateOf(initialSubtitleIndex) }

    val videoOptions = remember(streams) {
        streams.videoStreams.filter { !it.url.isNullOrEmpty() && !it.format.orEmpty().contains("HLS") }
            .sortedWith(compareBy<StreamFormat> { it.videoOnly }.thenByDescending { it.quality.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0 })
    }

    val audioOptions = remember(streams) {
        streams.audioStreams.filter { !it.url.isNullOrEmpty() }
            .sortedByDescending { it.quality.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0 }
    }

    val subtitleOptions = remember(streams) {
        streams.subtitles.filter { !it.url.isNullOrEmpty() && !it.name.isNullOrEmpty() }.sortedBy { it.name }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        Text(
            text = "Download",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = streams.title.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Video Spinner
        DropdownSelector(
            label = "Video",
            options = listOf("No Video") + videoOptions.map { 
                val size = Formatter.formatShortFileSize(context, it.contentLength)
                "${it.quality} ${it.codec} ${if (it.videoOnly) "(No Audio)" else ""} ($size)"
            },
            selectedIndex = videoIndex,
            onSelect = { videoIndex = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Audio Spinner
        DropdownSelector(
            label = "Audio",
            options = listOf("No Audio") + audioOptions.map {
                val size = if (it.contentLength > 0) Formatter.formatShortFileSize(context, it.contentLength) else ""
                "${it.quality} ${it.format} ${it.audioTrackLocale.orEmpty()} ($size)"
            },
            selectedIndex = audioIndex,
            onSelect = { audioIndex = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitle Spinner
        if (subtitleOptions.isNotEmpty()) {
            DropdownSelector(
                label = "Subtitle",
                options = listOf("No Subtitle") + subtitleOptions.map { it.name.orEmpty() },
                selectedIndex = subtitleIndex,
                onSelect = { subtitleIndex = it }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = { onDownloadConfirm(videoIndex - 1, audioIndex - 1, subtitleIndex - 1) },
                modifier = Modifier.weight(1f),
                enabled = videoIndex > 0 || audioIndex > 0 || subtitleIndex > 0
            ) {
                Text("Download")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = options.getOrNull(selectedIndex) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}
