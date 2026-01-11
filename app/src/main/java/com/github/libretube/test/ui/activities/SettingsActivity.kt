package com.github.libretube.test.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.libretube.test.BuildConfig
import com.github.libretube.test.R
import com.github.libretube.test.ui.base.BaseActivity
import com.github.libretube.test.ui.screens.SettingsGroupScreen
import com.github.libretube.test.ui.screens.SettingsRegistry
import com.github.libretube.test.ui.screens.SettingsScreen
import com.github.libretube.test.ui.theme.LibreTubeTheme
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LibreTubeTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route ?: "main"

                var showLogViewer by remember { mutableStateOf(false) }
                val updateViewModel: com.github.libretube.test.ui.models.UpdateViewModel by viewModels()
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    when (currentRoute) {
                                        "main" -> stringResource(R.string.settings)
                                        "appearance" -> stringResource(R.string.appearance)
                                        "general" -> stringResource(R.string.general)
                                        "player" -> stringResource(R.string.player)
                                        "content" -> stringResource(R.string.content_settings)
                                        "notifications" -> stringResource(R.string.notifications)
                                        "downloads" -> stringResource(R.string.downloads)
                                        "backup" -> stringResource(R.string.data_backup)
                                        else -> stringResource(R.string.settings)
                                    }
                                )
                            },
                            navigationIcon = {
                                if (currentRoute != "main") {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                } else {
                                    IconButton(onClick = { finish() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            }
                        )
                    }
                ) { padding ->
                    if (showLogViewer) {
                        com.github.libretube.test.ui.sheets.LogViewerSheet(
                            onDismissRequest = { showLogViewer = false }
                        )
                    }

                    // Update Available Dialog State
                    val updateInfo by updateViewModel.updateInfo
                    val downloadUrl by updateViewModel.downloadUrl
                    val runNumber by updateViewModel.runNumber
                    val sanitizedBody by updateViewModel.sanitizedBody
                    
                    if (updateInfo != null && downloadUrl != null) {
                        com.github.libretube.test.ui.sheets.UpdateAvailableBottomSheet(
                            updateName = updateInfo!!.name,
                            runNumber = runNumber,
                            changelog = sanitizedBody ?: updateInfo!!.body,
                            onUpdate = {
                                val updateManager = com.github.libretube.test.util.UpdateManager(context)
                                scope.launch {
                                    updateManager.handleUpdate(downloadUrl!!, scope)
                                }
                                updateViewModel.dismissUpdate()
                            },
                            onDismissRequest = {
                                updateViewModel.dismissUpdate()
                            }
                        )
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier.padding(padding)
                    ) {
                        composable("main") {
                            SettingsScreen(
                                onItemClick = { key ->
                                    when (key) {
                                        "appearance", "general", "player", "content", "notifications", "downloads", "data_backup" -> {
                                            val route = if (key == "data_backup") "backup" else key
                                            navController.navigate(route)
                                        }
                                        "update" -> {
                                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                com.github.libretube.test.util.UpdateChecker(context).checkUpdate(true, updateViewModel)
                                            }
                                        }
                                        "view_logs" -> {
                                            showLogViewer = true
                                        }
                                    }
                                },
                                versionName = BuildConfig.VERSION_NAME
                            )
                        }
                        composable("appearance") {
                            SettingsGroupScreen(
                                title = stringResource(R.string.appearance),
                                items = SettingsRegistry.getAppearanceItems(),
                                onNavigate = {}
                            )
                        }
                        composable("general") {
                            SettingsGroupScreen(
                                title = stringResource(R.string.general),
                                items = SettingsRegistry.getGeneralItems(),
                                onNavigate = {}
                            )
                        }
                        composable("player") {
                            SettingsGroupScreen(
                                title = stringResource(R.string.player),
                                items = SettingsRegistry.getPlayerItems(),
                                onNavigate = {}
                            )
                        }
                        composable("content") {
                            SettingsGroupScreen(
                                title = stringResource(R.string.content_settings),
                                items = SettingsRegistry.getContentItems(),
                                onNavigate = {}
                            )
                        }
                        composable("notifications") {
                            SettingsGroupScreen(
                                title = stringResource(R.string.notifications),
                                items = SettingsRegistry.getNotificationsItems(),
                                onNavigate = {}
                            )
                        }
                        composable("downloads") {
                            SettingsGroupScreen(
                                title = stringResource(R.string.downloads),
                                items = SettingsRegistry.getDownloadsItems(),
                                onNavigate = {}
                            )
                        }
                        composable("backup") {
                            SettingsGroupScreen(
                                title = stringResource(R.string.data_backup),
                                items = SettingsRegistry.getBackupItems(),
                                onNavigate = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
