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
            )
        )
    }

    @Composable
    fun getContentItems(): List<PreferenceItem> {
        return listOf(
            PreferenceItem.Category(stringResource(R.string.sponsorblock)),
            PreferenceItem.Switch(
                key = PreferenceKeys.CONTRIBUTE_TO_SB,
                title = stringResource(R.string.enabled),
                summary = stringResource(R.string.sponsorblock_summary),
                defaultValue = false
            ),
            PreferenceItem.Category(stringResource(R.string.dearrow)),
            PreferenceItem.Switch(
                key = PreferenceKeys.DEARROW,
                title = stringResource(R.string.enabled),
                summary = stringResource(R.string.dearrow_summary),
                defaultValue = false
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
            )
        )
    }

    @Composable
    fun getBackupItems(): List<PreferenceItem> {
        return listOf(
            PreferenceItem.Category(stringResource(R.string.backup_restore)),
            PreferenceItem.Clickable(
                key = "export",
                title = stringResource(R.string.backup),
                summary = stringResource(R.string.export_subscriptions),
                onClick = {} // Trigger export
            ),
            PreferenceItem.Clickable(
                key = "import",
                title = stringResource(R.string.restore),
                summary = stringResource(R.string.import_from_yt),
                onClick = {} // Trigger import
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
