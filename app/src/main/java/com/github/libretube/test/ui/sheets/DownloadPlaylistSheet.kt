package com.github.libretube.test.ui.sheets

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.github.libretube.test.R
import com.github.libretube.test.constants.IntentData
import com.github.libretube.test.enums.PlaylistType
import com.github.libretube.test.extensions.getWhileDigit
import com.github.libretube.test.helpers.LocaleHelper
import com.github.libretube.test.helpers.PreferenceHelper
import com.github.libretube.test.services.PlaylistDownloadEnqueueService

private const val PLAYLIST_DOWNLOAD_VIDEO_QUALITY = "playlist_download_video_quality"
private const val PLAYLIST_DOWNLOAD_AUDIO_QUALITY = "playlist_download_audio_quality"
private const val PLAYLIST_DOWNLOAD_AUDIO_LANGUAGE = "playlist_download_audio_language"
private const val PLAYLIST_DOWNLOAD_CAPTION_LANGUAGE = "playlist_download_caption_language"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadPlaylistBottomSheet(
    playlistId: String,
    playlistName: String,
    playlistType: PlaylistType,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    
    val possibleVideoQualities = stringArrayResource(R.array.defres).toList().let {
        it.subList(1, it.size)
    }
    val possibleAudioQualities = stringArrayResource(R.array.audioQualityBitrates).toList()

    val videoQualities = listOf(stringResource(R.string.no_video)) + possibleVideoQualities
    val audioQualities = listOf(stringResource(R.string.no_audio)) + possibleAudioQualities

    val savedVideoQuality = PreferenceHelper.getString(PLAYLIST_DOWNLOAD_VIDEO_QUALITY, "")
    val savedAudioQuality = PreferenceHelper.getString(PLAYLIST_DOWNLOAD_AUDIO_QUALITY, "")
    val savedAudioLanguage = PreferenceHelper.getString(PLAYLIST_DOWNLOAD_AUDIO_LANGUAGE, "")
    val savedCaptionLanguage = PreferenceHelper.getString(PLAYLIST_DOWNLOAD_CAPTION_LANGUAGE, "")

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        DownloadPlaylistSheet(
            playlistName = playlistName,
            videoQualities = videoQualities,
            audioQualities = audioQualities,
            initialVideoQuality = savedVideoQuality.takeIf { it.isNotEmpty() },
            initialAudioQuality = savedAudioQuality.takeIf { it.isNotEmpty() },
            initialSubtitleLanguage = savedCaptionLanguage.takeIf { it.isNotEmpty() },
            initialAudioLanguage = savedAudioLanguage.takeIf { it.isNotEmpty() },
            onDownload = { videoQuality, audioQuality, subtitleLang, audioLang ->
                val maxVideoQuality = videoQuality?.let { 
                    possibleVideoQualities.find { q -> q == it }
                }.getWhileDigit()
                
                val maxAudioQuality = audioQuality?.let { 
                    possibleAudioQualities.find { q -> q == it }
                }.getWhileDigit()

                if (maxVideoQuality == null && maxAudioQuality == null) {
                    Toast.makeText(context, R.string.nothing_selected, Toast.LENGTH_SHORT).show()
                } else {
                    // Save selections
                    PreferenceHelper.putString(PLAYLIST_DOWNLOAD_VIDEO_QUALITY, videoQuality.orEmpty())
                    PreferenceHelper.putString(PLAYLIST_DOWNLOAD_AUDIO_QUALITY, audioQuality.orEmpty())
                    PreferenceHelper.putString(PLAYLIST_DOWNLOAD_AUDIO_LANGUAGE, audioLang.orEmpty())
                    PreferenceHelper.putString(PLAYLIST_DOWNLOAD_CAPTION_LANGUAGE, subtitleLang.orEmpty())

                    val downloadEnqueueIntent =
                        Intent(context, PlaylistDownloadEnqueueService::class.java)
                            .putExtra(IntentData.playlistId, playlistId)
                            .putExtra(IntentData.playlistType, playlistType)
                            .putExtra(IntentData.playlistName, playlistName)
                            .putExtra(IntentData.audioLanguage, audioLang)
                            .putExtra(IntentData.maxVideoQuality, maxVideoQuality)
                            .putExtra(IntentData.maxAudioQuality, maxAudioQuality)
                            .putExtra(IntentData.captionLanguage, subtitleLang)

                    ContextCompat.startForegroundService(context, downloadEnqueueIntent)
                    onDismissRequest()
                }
            },
            onCancel = onDismissRequest
        )
    }
}

/**
 * Sheet for downloading playlists with quality and language options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadPlaylistSheet(
    playlistName: String,
    videoQualities: List<String>,
    audioQualities: List<String>,
    initialVideoQuality: String?,
    initialAudioQuality: String?,
    initialSubtitleLanguage: String?,
    initialAudioLanguage: String?,
    onDownload: (
        videoQuality: String?,
        audioQuality: String?,
        subtitleLanguage: String?,
        audioLanguage: String?
    ) -> Unit,
    onCancel: () -> Unit
) {
    val availableLanguages = remember { LocaleHelper.getAvailableLocales() }
    
    var selectedVideoQualityIndex by remember { 
        mutableIntStateOf(
            videoQualities.indexOf(initialVideoQuality).takeIf { it >= 0 } ?: 0
        )
    }
    var selectedAudioQualityIndex by remember { 
        mutableIntStateOf(
            audioQualities.indexOf(initialAudioQuality).takeIf { it >= 0 } ?: 0
        )
    }
    var selectedSubtitleIndex by remember { 
        mutableIntStateOf(
            availableLanguages.indexOfFirst { it.code == initialSubtitleLanguage }
                .let { if (it >= 0) it + 1 else 0 }
        )
    }
    var selectedAudioLanguageIndex by remember { 
        mutableIntStateOf(
            availableLanguages.indexOfFirst { it.code == initialAudioLanguage }
                .let { if (it >= 0) it + 1 else 0 }
        )
    }

    var showVideoDropdown by remember { mutableStateOf(false) }
    var showAudioDropdown by remember { mutableStateOf(false) }
    var showSubtitleDropdown by remember { mutableStateOf(false) }
    var showAudioLanguageDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "Download Playlist",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = playlistName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Video Quality Dropdown
        ExposedDropdownMenuBox(
            expanded = showVideoDropdown,
            onExpandedChange = { showVideoDropdown = it }
        ) {
            OutlinedTextField(
                value = videoQualities[selectedVideoQualityIndex],
                onValueChange = {},
                readOnly = true,
                label = { Text("Video Quality") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showVideoDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showVideoDropdown,
                onDismissRequest = { showVideoDropdown = false }
            ) {
                videoQualities.forEachIndexed { index, quality ->
                    DropdownMenuItem(
                        text = { Text(quality) },
                        onClick = {
                            selectedVideoQualityIndex = index
                            showVideoDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Audio Quality Dropdown
        ExposedDropdownMenuBox(
            expanded = showAudioDropdown,
            onExpandedChange = { showAudioDropdown = it }
        ) {
            OutlinedTextField(
                value = audioQualities[selectedAudioQualityIndex],
                onValueChange = {},
                readOnly = true,
                label = { Text("Audio Quality") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAudioDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showAudioDropdown,
                onDismissRequest = { showAudioDropdown = false }
            ) {
                audioQualities.forEachIndexed { index, quality ->
                    DropdownMenuItem(
                        text = { Text(quality) },
                        onClick = {
                            selectedAudioQualityIndex = index
                            showAudioDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle Language Dropdown
        val subtitleOptions = listOf("No subtitle") + availableLanguages.map { it.name }
        ExposedDropdownMenuBox(
            expanded = showSubtitleDropdown,
            onExpandedChange = { showSubtitleDropdown = it }
        ) {
            OutlinedTextField(
                value = subtitleOptions[selectedSubtitleIndex],
                onValueChange = {},
                readOnly = true,
                label = { Text("Subtitle Language") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSubtitleDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showSubtitleDropdown,
                onDismissRequest = { showSubtitleDropdown = false }
            ) {
                subtitleOptions.forEachIndexed { index, language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            selectedSubtitleIndex = index
                            showSubtitleDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Audio Language Dropdown
        val audioLanguageOptions = listOf("Default language") + availableLanguages.map { it.name }
        ExposedDropdownMenuBox(
            expanded = showAudioLanguageDropdown,
            onExpandedChange = { showAudioLanguageDropdown = it }
        ) {
            OutlinedTextField(
                value = audioLanguageOptions[selectedAudioLanguageIndex],
                onValueChange = {},
                readOnly = true,
                label = { Text("Audio Language") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAudioLanguageDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showAudioLanguageDropdown,
                onDismissRequest = { showAudioLanguageDropdown = false }
            ) {
                audioLanguageOptions.forEachIndexed { index, language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            selectedAudioLanguageIndex = index
                            showAudioLanguageDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
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
                onClick = {
                    val videoQuality = if (selectedVideoQualityIndex > 0) 
                        videoQualities[selectedVideoQualityIndex] else null
                    val audioQuality = if (selectedAudioQualityIndex > 0) 
                        audioQualities[selectedAudioQualityIndex] else null
                    val subtitleLang = if (selectedSubtitleIndex > 0) 
                        availableLanguages[selectedSubtitleIndex - 1].code else null
                    val audioLang = if (selectedAudioLanguageIndex > 0) 
                        availableLanguages[selectedAudioLanguageIndex - 1].code else null
                    
                    onDownload(videoQuality, audioQuality, subtitleLang, audioLang)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Download")
            }
        }
    }
}
