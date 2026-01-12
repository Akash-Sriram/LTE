package com.github.libretube.test.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.github.libretube.test.BuildConfig
import com.github.libretube.test.R
import com.github.libretube.test.api.RetrofitInstance
import com.github.libretube.test.constants.IntentData.appUpdateChangelog
import com.github.libretube.test.constants.IntentData.appUpdateURL
import com.github.libretube.test.extensions.TAG
import com.github.libretube.test.extensions.toastFromMainDispatcher
import com.github.libretube.test.obj.update.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class UpdateChecker(private val context: Context) {
    suspend fun checkUpdate(isManualCheck: Boolean = false, updateViewModel: com.github.libretube.test.ui.models.UpdateViewModel? = null) {
        val currentVersionName = BuildConfig.VERSION_NAME


        // Check for updates based on the build type
        val isExperimental = BuildConfig.IS_EXPERIMENTAL
        
        // Extract run number regardless of prefix
        // Regex to extract run number (e.g. "Run 10", "Build-123", "Run10")
        val runPattern = Regex("(?:Run|Build)[\\s-]*(\\d+)", RegexOption.IGNORE_CASE)

        val currentRunNumber = runPattern.find(currentVersionName)?.groupValues?.get(1)?.toIntOrNull()
            ?: currentVersionName.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0

        try {
            val response = if (isExperimental) {
                 try {
                     RetrofitInstance.externalApi.getReleaseByTag("experimental")
                 } catch (e: Exception) {
                     // Fallback to latest if experimental tag not found (404)
                     RetrofitInstance.externalApi.getLatestRelease()
                 }
            } else {
                 RetrofitInstance.externalApi.getLatestRelease()
            }
            
            // Remote name format: "Nightly Build 9", "Experimental Build 9", "Run 10"
            // We use the same regex to extract the run number safely.
            // If pattern not found, we fallback to 0 to avoid false positives with dates (20250101).
            // Exception: if the name is JUST digits, we accept it.
            val remoteRunNumber = runPattern.find(response.name)?.groupValues?.get(1)?.toIntOrNull()
                ?: if (response.name.all { it.isDigit() }) response.name.toIntOrNull() ?: 0 else 0

            // Checking update

            if (remoteRunNumber > currentRunNumber) {
                // Find the APK asset
                val apkAsset = response.assets.find { it.name.endsWith(".apk") }
                if (apkAsset != null) {
                    withContext(Dispatchers.Main) {
                        if (updateViewModel != null) {
                            val sanitizedBody = sanitizeChangelog(response.body)
                            // Create a copy of UpdateInfo with sanitized body if possible, or just pass sanitized string
                            // Since showUpdate takes UpdateInfo, we might need to adjust or just pass sanitized body separately.
                            // Let's adjust UpdateViewModel.showUpdate to take sanitized body.
                            updateViewModel.showUpdate(response, apkAsset.browserDownloadUrl, remoteRunNumber.toString(), sanitizedBody)
                        } else {
                            // Fallback to legacy if no ViewModel provided (e.g. background task?)
                            // However, we want to move entirely to ViewModel
                            // For now, let's just use it.
                        }
                    }
                    // Update found
                } else {
                    // Update found but no APK asset
                    if (isManualCheck) {
                         withContext(Dispatchers.Main) {
                             context.toastFromMainDispatcher("Update found but no APK available.")
                         }
                    }
                }
            } else if (isManualCheck) {
                context.toastFromMainDispatcher(R.string.app_uptodate)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sanitizeChangelog(changelog: String): String {
        return changelog.substringBeforeLast("**Full Changelog**")
            .replace(Regex("in https://github\\.com/\\S+"), "")
            .lines().joinToString("\n") { line ->
                if (line.startsWith("##")) line.uppercase(Locale.ROOT) + " :" else line
            }
            .replace("## ", "")
            .replace(">", "")
            .replace("*", "•")
            .lines()
            .joinToString("\n") { it.trim() }
    }
}

