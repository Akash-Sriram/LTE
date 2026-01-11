package com.github.libretube.test.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import com.github.libretube.test.extensions.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import androidx.core.net.toUri
import com.github.libretube.test.extensions.toastFromMainDispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * UpdateManager handles downloading and installing APKs using the Android Session API.
 */
class UpdateManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Downloads an APK from the given URL to a temporary file.
     */
    suspend fun downloadApk(url: String, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        com.github.libretube.logger.FileLogger.d("UpdateManager", "Starting download: $url -> ${outputFile.path}")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "update_download_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             val channel = android.app.NotificationChannel(
                channelId,
                "Update Download",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading Update")
            .setOngoing(true)
            .setProgress(100, 0, false)
        
        notificationManager.notify(1, builder.build())

        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    com.github.libretube.logger.FileLogger.e("UpdateManager", "Download failed: HTTP ${response.code}")
                    return@withContext false
                }
                
                val body = response.body ?: return@withContext false
                val contentLength = body.contentLength()
                
                body.byteStream().use { inputStream ->
                    outputFile.outputStream().use { outputStream ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var totalBytesRead: Long = 0
                        var lastProgress = 0
                        
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            
                            if (contentLength > 0) {
                                val progress = (totalBytesRead * 100 / contentLength).toInt()
                                if (progress > lastProgress) {
                                    lastProgress = progress
                                    builder.setProgress(100, progress, false)
                                    notificationManager.notify(1, builder.build())
                                }
                            }
                        }
                    }
                }
            }
            notificationManager.cancel(1)
            com.github.libretube.logger.FileLogger.d("UpdateManager", "Download finished successfully")
            true
        } catch (e: Exception) {
            com.github.libretube.logger.FileLogger.e("UpdateManager", "Error downloading APK", e)
            // Error downloading APK
            builder.setContentText("Download failed")
                .setOngoing(false)
                .setProgress(0, 0, false)
            notificationManager.notify(1, builder.build())
            false
        }
    }

    /**
     * Installs an APK from the given file using the PackageInstaller Session API.
     */
    fun installApk(apkFile: File) {
        val packageInstaller = context.packageManager.packageInstaller
        val sessionParams = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            sessionParams.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }

        var sessionId = -1
        try {
            sessionId = packageInstaller.createSession(sessionParams)
            val session = packageInstaller.openSession(sessionId)
            
            apkFile.inputStream().use { inputStream ->
                session.openWrite("package", 0, apkFile.length()).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    session.fsync(outputStream)
                }
            }

            val intent = Intent(context, UpdateReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )

            session.commit(pendingIntent.intentSender)
            session.close()
            // Installation session committed
        } catch (e: Exception) {
            // Error installing APK via Session API
            if (sessionId != -1) {
                packageInstaller.abandonSession(sessionId)
            }
        }
    }

    /**
     * Handles the complete update flow: permission check, download, and installation trigger.
     */
    suspend fun handleUpdate(url: String, lifecycleScope: kotlinx.coroutines.CoroutineScope) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        com.github.libretube.test.R.string.toast_install_permission_required,
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    context.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = ("package:" + context.packageName).toUri()
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
                return
            }
        }

        val outputFile = File(context.getExternalFilesDir(null), "LibreTube-Update.apk")
        if (outputFile.exists()) {
            outputFile.delete()
        }

        withContext(Dispatchers.Main) {
            context.toastFromMainDispatcher(com.github.libretube.test.R.string.downloading)
        }
        
        val downloadResult = withContext(Dispatchers.IO) {
            downloadApk(url, outputFile)
        }
        com.github.libretube.logger.FileLogger.d("UpdateManager", "Download result: $downloadResult")

        if (downloadResult) {
            withContext(Dispatchers.Main) {
                context.toastFromMainDispatcher("Download complete. Preparing install...")
            }

            try {
                // Start installation via Session API
                withContext(Dispatchers.IO) {
                    installApk(outputFile)
                }
            } catch (e: Exception) {
                com.github.libretube.logger.FileLogger.e("UpdateManager", "Installation failed", e)
                withContext(Dispatchers.Main) {
                    context.toastFromMainDispatcher("Installation failed: ${e.message}")
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                context.toastFromMainDispatcher("Download failed")
            }
        }
    }
}

/**
 * Receiver to handle the status of the installation session.
 */
class UpdateReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        
        // Received Update Status

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }
                if (confirmIntent != null) {
                    confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirmIntent)
                } else {
                    // Confirm Intent is null
                    android.widget.Toast.makeText(context, "Update failed: Confirmation missing", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                // Installation successful
                android.widget.Toast.makeText(context, "Installation successful", android.widget.Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Installation failed
                android.widget.Toast.makeText(context, "Installation failed: $message", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
}

