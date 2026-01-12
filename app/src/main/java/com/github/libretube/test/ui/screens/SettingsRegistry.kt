package com.github.libretube.test.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.libretube.test.R
import com.github.libretube.test.constants.PreferenceKeys
import com.github.libretube.test.ui.models.PreferenceItem

object SettingsRegistry {

    @Composable
    fun getAppearanceItems(): List<PreferenceItem> {
        return listOf(
            PreferenceItem.Category(stringResource(R.string.app_theme)),
            PreferenceItem.List(
                key = PreferenceKeys.THEME_MODE,
                title = stringResource(R.string.app_theme),
                defaultValue = "system",
                entries = mapOf(
                    stringResource(R.string.systemDefault) to "system",
                    stringResource(R.string.lightTheme) to "light",
                    stringResource(R.string.darkTheme) to "dark"
                )
            ),
            PreferenceItem.Switch(
                key = PreferenceKeys.PURE_THEME,
                title = stringResource(R.string.pure_theme),
                summary = stringResource(R.string.pure_theme_summary),
                defaultValue = false
            ),
            PreferenceItem.Category(stringResource(R.string.navigation_bar)),
            PreferenceItem.List(
                key = PreferenceKeys.LABEL_VISIBILITY,
                title = stringResource(R.string.navLabelVisibility),
                defaultValue = "always",
                entries = mapOf(
                    stringResource(R.string.always) to "always",
                    stringResource(R.string.selected) to "selected",
                    stringResource(R.string.never) to "never"
                )
            ),
            PreferenceItem.Category(stringResource(R.string.misc)),
            PreferenceItem.Switch(
                key = PreferenceKeys.NEW_VIDEOS_BADGE,
                title = stringResource(R.string.new_videos_badge),
                summary = stringResource(R.string.new_videos_badge_summary),
                defaultValue = true
            )
        )
    }

    @Composable
    fun getPlayerItems(): List<PreferenceItem> {
        return listOf(
            PreferenceItem.Category(stringResource(R.string.behavior)),
            PreferenceItem.Switch(
                key = PreferenceKeys.AUTOPLAY,
                title = stringResource(R.string.player_autoplay),
                defaultValue = true
            ),
            PreferenceItem.Switch(
                key = PreferenceKeys.AUTO_FULLSCREEN,
                title = stringResource(R.string.autoRotatePlayer),
                summary = stringResource(R.string.autoRotatePlayer_summary),
                defaultValue = false
            ),
            PreferenceItem.Switch(
                key = PreferenceKeys.PAUSE_ON_SCREEN_OFF,
                title = stringResource(R.string.pauseOnScreenOff),
                summary = stringResource(R.string.pauseOnScreenOff_summary),
                defaultValue = true
            ),
            PreferenceItem.Category(stringResource(R.string.customization)),
            PreferenceItem.Switch(
                key = PreferenceKeys.PLAYER_SWIPE_CONTROLS,
                title = stringResource(R.string.swipe_controls),
                summary = stringResource(R.string.swipe_controls_summary),
                defaultValue = true
            ),
            PreferenceItem.Switch(
                key = PreferenceKeys.PLAYER_PINCH_CONTROL,
                title = stringResource(R.string.pinch_control),
                summary = stringResource(R.string.pinch_control_summary),
                defaultValue = true
            ),
            PreferenceItem.List(
                key = PreferenceKeys.DOUBLE_TAP_TO_SEEK,
                title = stringResource(R.string.double_tap_seek),
                summary = stringResource(R.string.double_tap_seek_summary),
                defaultValue = "10",
                entries = mapOf(
                    "5s" to "5",
                    "10s" to "10",
                    "15s" to "15",
                    "30s" to "30",
                    "60s" to "60"
                )
            ),
            PreferenceItem.Category(stringResource(R.string.captions)),
            PreferenceItem.Switch(
                key = PreferenceKeys.SYSTEM_CAPTION_STYLE,
                title = stringResource(R.string.system_caption_style),
                summary = stringResource(R.string.system_caption_style_summary),
                defaultValue = true
            ),
            PreferenceItem.Switch(
                key = PreferenceKeys.RICH_CAPTION_RENDERING,
                title = stringResource(R.string.rich_caption_rendering),
                summary = stringResource(R.string.rich_caption_rendering_summary),
                defaultValue = false
            ),
            PreferenceItem.Category(stringResource(R.string.quality)),
            PreferenceItem.List(
                key = PreferenceKeys.DEFAULT_RESOLUTION,
                title = stringResource(R.string.default_video_quality),
                summary = stringResource(R.string.default_video_quality_summary),
                defaultValue = "auto",
                entries = mapOf(
                    "Auto" to "auto",
                    "144p" to "144",
                    "240p" to "240",
                    "360p" to "360",
                    "480p" to "480",
                    "720p" to "720",
                    "1080p" to "1080",
                    "1440p" to "1440",
                    "2160p" to "2160"
                )
            )
        )
    }

    @Composable
    fun getGeneralItems(): List<PreferenceItem> {
        return listOf(
            PreferenceItem.Category(stringResource(R.string.location)),
            PreferenceItem.List(
                key = PreferenceKeys.REGION,
                title = stringResource(R.string.region),
                defaultValue = "US",
                entries = com.github.libretube.test.helpers.LocaleHelper.getAvailableCountries()
                    .associate { it.name to it.code }
            ),
            PreferenceItem.List(
                key = PreferenceKeys.LANGUAGE,
                title = stringResource(R.string.translate),
                defaultValue = "en",
                entries = com.github.libretube.test.helpers.LocaleHelper.getAvailableLocales()
                    .associate { it.name to it.code }
            ),
            PreferenceItem.Category(stringResource(R.string.behavior)),
            PreferenceItem.Switch(
                key = PreferenceKeys.AUDIO_ONLY_MODE,
                title = stringResource(R.string.audio_only_mode),
                summary = stringResource(R.string.audio_only_mode_summary),
                defaultValue = false
            ),
            PreferenceItem.Switch(
                key = PreferenceKeys.INCLUDE_TIMESTAMP_IN_BACKUP_FILENAME,
                title = stringResource(R.string.time),
                defaultValue = true
            ),
            PreferenceItem.Category(stringResource(R.string.data_bandwidth)),
            PreferenceItem.Switch(
                key = PreferenceKeys.BANDWIDTH_SAVER_MODE,
                title = stringResource(R.string.bandwidth_saver),
                summary = stringResource(R.string.bandwidth_saver_summary),
                defaultValue = false
            ),
            PreferenceItem.Category(stringResource(R.string.advanced)),
            PreferenceItem.Switch(
                key = PreferenceKeys.AUTOMATIC_UPDATE_CHECKS,
                title = stringResource(R.string.auto_update_checks),
                summary = stringResource(R.string.auto_update_checks_summary),
                defaultValue = true
            ),
            PreferenceItem.Switch(
                key = PreferenceKeys.ENABLE_ROBUST_LOGGING,
                title = stringResource(R.string.extended_debug_logging),
                summary = stringResource(R.string.extended_debug_logging_summary),
                defaultValue = false
            ),
            PreferenceItem.Clickable(
                key = PreferenceKeys.CLEAR_CACHE,
                title = stringResource(R.string.clear_cache),
                summary = stringResource(R.string.clear_cache_summary),
                onClick = {} // Handle cache clearing
            )
        )
    }

    @Composable
    fun getContentItems(): List<PreferenceItem> {
        val segmentEntries = mapOf(
            stringResource(R.string.automatic) to "automatic",
            stringResource(R.string.disabled) to "off",
            stringResource(R.string.enabled) to "show",
            "Skip" to "skip"  // Hardcoded for now
        )

        return listOf(
            PreferenceItem.Category(stringResource(R.string.sponsorblock)),
            PreferenceItem.Switch(
                key = PreferenceKeys.CONTRIBUTE_TO_SB,
                title = stringResource(R.string.enabled),
                summary = stringResource(R.string.sponsorblock_summary),
                defaultValue = true
            ),
            PreferenceItem.Switch(
                key = "sb_enable_custom_colors",
                title = stringResource(R.string.sb_custom_colors),
                summary = stringResource(R.string.sb_custom_colors_summary),
                defaultValue = false
            ),
            PreferenceItem.Category(stringResource(R.string.category_segments)),
            PreferenceItem.List(
                key = "sponsor_category",
                title = stringResource(R.string.category_sponsor),
                summary = stringResource(R.string.category_sponsor_summary),
                defaultValue = "automatic",
                entries = segmentEntries
            ),
            PreferenceItem.Color(
                key = "sponsor_color",
                title = stringResource(R.string.color),
                defaultValue = android.graphics.Color.parseColor("#00d400")
            ),
            PreferenceItem.List(
                key = "interaction_category",
                title = stringResource(R.string.category_interaction),
                summary = stringResource(R.string.category_interaction_summary),
                defaultValue = "off",
                entries = segmentEntries
            ),
            PreferenceItem.Color(
                key = "interaction_color",
                title = stringResource(R.string.color),
                defaultValue = android.graphics.Color.parseColor("#cc00ff")
            ),
            PreferenceItem.List(
                key = "intro_category",
                title = stringResource(R.string.category_intro),
                summary = stringResource(R.string.category_intro_summary),
                defaultValue = "off",
                entries = segmentEntries
            ),
            PreferenceItem.Color(
                key = "intro_color",
                title = stringResource(R.string.color),
                defaultValue = android.graphics.Color.parseColor("#00ffff")
            ),
            PreferenceItem.List(
                key = "outro_category",
                title = stringResource(R.string.category_outro),
                summary = stringResource(R.string.category_outro_summary),
                defaultValue = "off",
                entries = segmentEntries
            ),
            PreferenceItem.Color(
                key = "outro_color",
                title = stringResource(R.string.color),
                defaultValue = android.graphics.Color.parseColor("#0202ED")
            ),
            PreferenceItem.List(
                key = PreferenceKeys.SB_CATEGORY_SELFPROMO,
                title = stringResource(R.string.category_selfpromo),
                summary = stringResource(R.string.category_selfpromo_summary),
                defaultValue = "off",
                entries = segmentEntries
            ),
            PreferenceItem.Color(
                key = "selfpromo_color",
                title = stringResource(R.string.color),
                defaultValue = android.graphics.Color.parseColor("#FFFF00")
            ),
            PreferenceItem.List(
                key = PreferenceKeys.SB_CATEGORY_MUSIC_OFFTOPIC,
                title = stringResource(R.string.category_music_offtopic),
                summary = stringResource(R.string.category_music_offtopic_summary),
                defaultValue = "off",
                entries = segmentEntries
            ),
            PreferenceItem.Color(
                key = "music_offtopic_color",
                title = stringResource(R.string.color),
                defaultValue = android.graphics.Color.parseColor("#FFA500")
            ),
            PreferenceItem.List(
                key = PreferenceKeys.SB_CATEGORY_PREVIEW,
                title = stringResource(R.string.category_preview),
                summary = stringResource(R.string.category_preview_summary),
                defaultValue = "off",
                entries = segmentEntries
            ),
            PreferenceItem.Color(
                key = "preview_color",
                title = stringResource(R.string.color),
                defaultValue = android.graphics.Color.parseColor("#008FD6")
            ),
            PreferenceItem.List(
                key = PreferenceKeys.SB_CATEGORY_FILLER,
                title = stringResource(R.string.category_filler),
                summary = stringResource(R.string.category_filler_summary),
                defaultValue = "off",
                entries = segmentEntries
            ),
            PreferenceItem.Color(
                key = "filler_color",
                title = stringResource(R.string.color),
                defaultValue = android.graphics.Color.parseColor("#7300FF")
            ),

            PreferenceItem.Category(stringResource(R.string.dearrow)),
            PreferenceItem.Switch(
                key = PreferenceKeys.DEARROW,
                title = stringResource(R.string.enabled),
                summary = stringResource(R.string.dearrow_summary),
                defaultValue = true
            ),
            PreferenceItem.Switch(
                key = PreferenceKeys.DEARROW_TITLES,
                title = stringResource(R.string.dearrow_titles),
                summary = stringResource(R.string.dearrow_titles_summary),
                defaultValue = true
            ),
            PreferenceItem.Switch(
                key = PreferenceKeys.DEARROW_THUMBNAILS,
                title = stringResource(R.string.dearrow_thumbnails),
                summary = stringResource(R.string.dearrow_thumbnails_summary),
                defaultValue = true
            ),
            PreferenceItem.Switch(
                key = "dearrow_exempt_live",
                title = stringResource(R.string.dearrow_exemption_live),
                summary = stringResource(R.string.dearrow_exemption_live_summary),
                defaultValue = true
            )
        )
    }

    @Composable
    fun getNotificationsItems(): List<PreferenceItem> {
        return listOf(
            PreferenceItem.Category(stringResource(R.string.notifications)),
            PreferenceItem.Switch(
                key = PreferenceKeys.NOTIFICATION_ENABLED,
                title = stringResource(R.string.notify_new_streams),
                summary = stringResource(R.string.notify_new_streams_summary),
                defaultValue = true
            ),
            PreferenceItem.Switch(
                key = PreferenceKeys.SHOW_STREAM_THUMBNAILS,
                title = stringResource(R.string.show_stream_thumbnails),
                summary = stringResource(R.string.show_stream_thumbnails_summary),
                defaultValue = true
            )
        )
    }

    @Composable
    fun getDownloadsItems(): List<PreferenceItem> {
        return listOf(
            PreferenceItem.Category(stringResource(R.string.downloads)),
            PreferenceItem.Clickable(
                key = "download_path",
                title = stringResource(R.string.location),
                summary = com.github.libretube.test.helpers.RoomPreferenceDataStore.getString(PreferenceKeys.DOWNLOAD_PATH, ""),
                onClick = {} // Handle path picker in Activity/ViewModel
            ),
            PreferenceItem.List(
                key = PreferenceKeys.MAX_CONCURRENT_DOWNLOADS,
                title = stringResource(R.string.concurrent_downloads),
                defaultValue = "1",
                entries = mapOf("1" to "1", "2" to "2", "3" to "3", "4" to "4", "5" to "5")
            ),
            PreferenceItem.Category(stringResource(R.string.external_provider)),
            PreferenceItem.Clickable(
                key = PreferenceKeys.EXTERNAL_DOWNLOADER_PACKAGE,
                title = stringResource(R.string.external_downloader),
                summary = stringResource(R.string.external_downloader_summary),
                onClick = {} // Handle package name input
            ),
            PreferenceItem.Category(stringResource(R.string.quality_defaults)),
            PreferenceItem.List(
                key = PreferenceKeys.DEFAULT_DOWNLOAD_VIDEO_QUALITY,
                title = stringResource(R.string.default_download_video_quality),
                defaultValue = "1080",
                entries = mapOf(
                    "Best" to "best",
                    "2160p" to "2160",
                    "1440p" to "1440",
                    "1080p" to "1080",
                    "720p" to "720",
                    "480p" to "480",
                    "360p" to "360"
                )
            ),
            PreferenceItem.List(
                key = PreferenceKeys.DEFAULT_DOWNLOAD_AUDIO_QUALITY,
                title = stringResource(R.string.default_download_audio_quality),
                defaultValue = "best",
                entries = mapOf(
                    "Best" to "best",
                    "High (256kbps)" to "256",
                    "Medium (128kbps)" to "128",
                    "Low (64kbps)" to "64"
                )
            )
        )
    }

    @Composable
    fun getBackupItems(): List<PreferenceItem> {
        return listOf(
            PreferenceItem.Category(stringResource(R.string.backup_restore)),
            PreferenceItem.Clickable(
                key = "backup_restore_screen",
                title = stringResource(R.string.backup_restore),
                summary = "Manage backups and restore data",
                onClick = {} // Navigation handled in SettingsGroupScreen
            )
        )
    }

    @Composable
    fun getAboutItems(versionName: String): List<PreferenceItem> {
        return listOf(
            PreferenceItem.Category(stringResource(R.string.about)),
            PreferenceItem.Clickable(
                key = "update",
                title = stringResource(R.string.version),
                summary = stringResource(R.string.version_format, versionName),
                onClick = {} // Trigger update check
            ),
            PreferenceItem.Clickable(
                key = "view_logs",
                title = stringResource(R.string.view_logs),
                onClick = {} // Trigger log viewer
            )
        )
    }
}
