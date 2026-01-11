package com.github.libretube.test.ui.activities

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.text.HtmlCompat
import androidx.core.text.parseAsHtml
import com.github.libretube.test.R
import com.github.libretube.test.helpers.ClipboardHelper
import com.github.libretube.test.helpers.IntentHelper
import com.github.libretube.test.ui.base.BaseActivity
import com.github.libretube.test.ui.screens.AboutScreen
import com.github.libretube.test.ui.theme.LibreTubeTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LibreTubeTheme {
                AboutScreen(
                    onDonateClick = { IntentHelper.openLinkFromHref(this, supportFragmentManager, DONATE_URL) },
                    onWebsiteClick = { IntentHelper.openLinkFromHref(this, supportFragmentManager, WEBSITE_URL) },
                    onTranslateClick = { IntentHelper.openLinkFromHref(this, supportFragmentManager, WEBLATE_URL) },
                    onGithubClick = { IntentHelper.openLinkFromHref(this, supportFragmentManager, GITHUB_URL) },
                    onLicenseClick = { showLicense() },
                    onDeviceInfoClick = { showDeviceInfo() }
                )
            }
        }
    }

    private fun showLicense() {
        val licenseHtml = assets.open("gpl3.html")
            .bufferedReader()
            .use { it.readText() }
            .parseAsHtml(HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)

        MaterialAlertDialogBuilder(this)
            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
            .setMessage(licenseHtml)
            .create()
            .show()
    }

    private fun showDeviceInfo() {
        val metrics = Resources.getSystem().displayMetrics

        val text = "Manufacturer: ${Build.MANUFACTURER}\n" +
                "Board: ${Build.BOARD}\n" +
                "Arch: ${Build.SUPPORTED_ABIS[0]}\n" +
                "Android SDK: ${Build.VERSION.SDK_INT}\n" +
                "OS: Android ${Build.VERSION.RELEASE}\n" +
                "Display: ${metrics.widthPixels}x${metrics.heightPixels}\n" +
                "Font scale: ${Resources.getSystem().configuration.fontScale}"

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.device_info)
            .setMessage(text)
            .setNegativeButton(R.string.copy_tooltip) { _, _ ->
                ClipboardHelper.save(this@AboutActivity, text = text)
            }
            .setPositiveButton(R.string.okay, null)
            .show()
    }

    companion object {
        const val DONATE_URL = "https://github.com/libre-tube/LibreTube#donate"
        private const val WEBSITE_URL = "https://libretube.dev"
        const val GITHUB_URL = "https://github.com/libre-tube/LibreTube"
        private const val WEBLATE_URL = "https://hosted.weblate.org/projects/libretube/libretube/"
    }
}
