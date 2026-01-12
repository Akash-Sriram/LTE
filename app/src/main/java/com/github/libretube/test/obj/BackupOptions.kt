package com.github.libretube.test.obj

import kotlinx.serialization.Serializable

/**
 * Options for selective backup/restore operations
 */
@Serializable
data class BackupOptions(
    val includeSubscriptions: Boolean = true,
    val includePlaylists: Boolean = true,
    val includeWatchHistory: Boolean = true,
    val includeWatchPositions: Boolean = true,
    val includeSearchHistory: Boolean = false,
    val includePlaylistBookmarks: Boolean = true,
    val includePreferences: Boolean = true,
    val includeGroups: Boolean = true
)

/**
 * Statistics from a restore operation
 */
data class RestoreStats(
    val subscriptionsImported: Int = 0,
    val playlistsImported: Int = 0,
    val watchHistoryImported: Int = 0,
    val watchPositionsImported: Int = 0,
    val searchHistoryImported: Int = 0,
    val playlistBookmarksImported: Int = 0,
    val groupsImported: Int = 0,
    val preferencesRestored: Boolean = false
) {
    val totalItemsImported: Int
        get() = subscriptionsImported + playlistsImported + watchHistoryImported +
                watchPositionsImported + searchHistoryImported + playlistBookmarksImported + groupsImported
}

/**
 * Backup type selection
 */
enum class BackupType {
    JSON,      // Portable JSON format
    DATABASE   // Raw database file
}
