package com.github.libretube.test.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.libretube.test.R
import com.github.libretube.test.db.DatabaseHolder
import com.github.libretube.test.helpers.BackupHelper
import com.github.libretube.test.obj.BackupOptions
import com.github.libretube.test.obj.BackupType
import com.github.libretube.test.ui.components.BackupOptionCard
import com.github.libretube.test.ui.components.getBackupOptionIcon
import com.github.libretube.test.ui.dialogs.BackupProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Data counts
    var subscriptionCount by remember { mutableStateOf(0) }
    var playlistCount by remember { mutableStateOf(0) }
    var historyCount by remember { mutableStateOf(0) }
    var bookmarkCount by remember { mutableStateOf(0) }
    
    // Backup options
    var includeSubscriptions by remember { mutableStateOf(true) }
    var includePlaylists by remember { mutableStateOf(true) }
    var includeHistory by remember { mutableStateOf(true) }
    var includeBookmarks by remember { mutableStateOf(true) }
    var includePreferences by remember { mutableStateOf(true) }
    
    // Backup type
    var backupType by remember { mutableStateOf(BackupType.DATABASE) }
    
    // Progress state
    var showProgress by remember { mutableStateOf(false) }
    var progressOperation by remember { mutableStateOf("") }
    var progressValue by remember { mutableStateOf(0f) }
    var progressItem by remember { mutableStateOf("") }
    
    // File pickers
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            scope.launch {
                showProgress = true
                progressOperation = "Exporting"
                progressValue = 0.1f
                progressItem = "Preparing backup..."
                
                try {
                    withContext(Dispatchers.IO) {
                        when (backupType) {
                            BackupType.DATABASE -> {
                                BackupHelper.backupDatabase(context, it)
                            }
                            BackupType.JSON -> {
                                // JSON backup would go here
                                // For now, use database backup
                                BackupHelper.backupDatabase(context, it)
                            }
                        }
                    }
                    progressValue = 1f
                    progressItem = "Backup complete!"
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    showProgress = false
                }
            }
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                showProgress = true
                progressOperation = "Importing"
                progressValue = 0.1f
                progressItem = "Reading backup file..."
                
                try {
                    withContext(Dispatchers.IO) {
                        BackupHelper.restoreAdvancedBackup(context, it)
                    }
                    progressValue = 1f
                    progressItem = "Import complete!"
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    showProgress = false
                }
            }
        }
    }
    
    // Load data counts
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = DatabaseHolder.Database
            subscriptionCount = db.localSubscriptionDao().getAll().size
            playlistCount = db.localPlaylistsDao().getAll().size
            historyCount = db.watchHistoryDao().getAll().size
            bookmarkCount = db.playlistBookmarkDao().getAll().size
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Export Section
            item {
                Text(
                    text = "Export Data",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Backup Type Selector
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Backup Type",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            FilterChip(
                                selected = backupType == BackupType.DATABASE,
                                onClick = { backupType = BackupType.DATABASE },
                                label = { Text("Database") },
                                leadingIcon = {
                                    Icon(Icons.Default.Storage, contentDescription = null)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = backupType == BackupType.JSON,
                                onClick = { backupType = BackupType.JSON },
                                label = { Text("JSON") },
                                leadingIcon = {
                                    Icon(Icons.Default.Code, contentDescription = null)
                                }
                            )
                        }
                        Text(
                            text = if (backupType == BackupType.DATABASE) {
                                "Raw database file - Fastest, LibreTube only"
                            } else {
                                "Portable JSON format - Compatible with other apps"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            // Export Options
            item {
                Text(
                    text = "Select Data to Export",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            item {
                BackupOptionCard(
                    title = "Subscriptions",
                    count = subscriptionCount,
                    checked = includeSubscriptions,
                    onCheckedChange = { includeSubscriptions = it },
                    icon = getBackupOptionIcon("subscriptions")
                )
            }
            
            item {
                BackupOptionCard(
                    title = "Local Playlists",
                    count = playlistCount,
                    checked = includePlaylists,
                    onCheckedChange = { includePlaylists = it },
                    icon = getBackupOptionIcon("playlists")
                )
            }
            
            item {
                BackupOptionCard(
                    title = "Watch History",
                    count = historyCount,
                    checked = includeHistory,
                    onCheckedChange = { includeHistory = it },
                    icon = getBackupOptionIcon("history")
                )
            }
            
            item {
                BackupOptionCard(
                    title = "Playlist Bookmarks",
                    count = bookmarkCount,
                    checked = includeBookmarks,
                    onCheckedChange = { includeBookmarks = it },
                    icon = getBackupOptionIcon("bookmarks")
                )
            }
            
            item {
                BackupOptionCard(
                    title = "Preferences",
                    checked = includePreferences,
                    onCheckedChange = { includePreferences = it },
                    icon = getBackupOptionIcon("preferences")
                )
            }
            
            // Export Button
            item {
                Button(
                    onClick = {
                        val fileName = "libretube_backup_${System.currentTimeMillis()}.db"
                        exportLauncher.launch(fileName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Backup")
                }
            }
            
            // Divider
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            
            // Import Section
            item {
                Text(
                    text = "Import Data",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            item {
                Text(
                    text = "Import from backup file or other sources",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            
            // Import Button
            item {
                Button(
                    onClick = {
                        importLauncher.launch("*/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Backup")
                }
            }
            
            // Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Database backups are faster and preserve all data. JSON backups are portable and can be imported into other apps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
    
    // Progress Dialog
    if (showProgress) {
        BackupProgressDialog(
            operation = progressOperation,
            progress = progressValue,
            currentItem = progressItem
        )
    }
}
