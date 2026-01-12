package com.github.libretube.test.helpers

import com.github.libretube.test.constants.PreferenceKeys

/**
 * Helper for bandwidth-related settings
 */
object BandwidthHelper {
    /**
     * Check if bandwidth saver mode is enabled
     * When enabled, thumbnails and images should not be loaded
     */
    fun isBandwidthSaverEnabled(): Boolean {
        return PreferenceHelper.getBoolean(
            PreferenceKeys.BANDWIDTH_SAVER_MODE,
            false
        )
    }
}
