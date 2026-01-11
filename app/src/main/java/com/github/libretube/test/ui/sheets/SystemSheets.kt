package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.libretube.logger.FileLogger
import com.github.libretube.test.R
import com.github.libretube.test.constants.PreferenceKeys
import com.github.libretube.test.helpers.ClipboardHelper
import com.github.libretube.test.helpers.PreferenceHelper
import com.github.libretube.test.util.LogcatRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.github.libretube.test.enums.ShareObjectType
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.background
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerSheet(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var logLines by remember { mutableStateOf<List<String>>(emptyList()) }
    var fullLog by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    
    val useRobustLogging = remember { PreferenceHelper.getBoolean(PreferenceKeys.ENABLE_ROBUST_LOGGING, false) }

    LaunchedEffect(Unit) {
        val logContent = withContext(Dispatchers.IO) {
            if (useRobustLogging) {
                LogcatRecorder.getLogContent(context)
            } else {
                FileLogger.getLogContent()
            }
        }
        
        fullLog = withContext(Dispatchers.Default) {
            val linesSize = logContent.count { it == '\n' }
            if (linesSize > 2000) {
                logContent.lineSequence().toList().takeLast(2000).joinToString("\n")
            } else {
                logContent
            }
        }
        
        logLines = fullLog.split("\n")
        isLoading = false
    }

    val filteredLines = remember(logLines, searchQuery) {
        if (searchQuery.isBlank()) {
            logLines
        } else {
            logLines.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.view_logs),
                    style = MaterialTheme.typography.titleLarge
                )
                
                Row {
                    IconButton(onClick = {
                        ClipboardHelper.save(context, text = filteredLines.joinToString("\n"), notify = true)
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            if (useRobustLogging) {
                                PreferenceHelper.putLong(PreferenceKeys.LOG_VIEWER_START_TIMESTAMP, System.currentTimeMillis())
                            } else {
                                FileLogger.clearLog()
                            }
                            withContext(Dispatchers.Main) {
                                logLines = emptyList()
                                fullLog = ""
                            }
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                    }
                }
            }

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search logs...") },
                leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Log Content
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.05f))
                        .padding(horizontal = 8.dp)
                ) {
                    items(filteredLines) { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = when {
                                line.contains(" E/") || line.contains("Error") -> Color.Red
                                line.contains(" W/") || line.contains("Warn") -> Color(0xFFFFA500)
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    id: String,
    title: String,
    shareObjectType: ShareObjectType,
    initialTimestamp: String,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        ShareSheet(
            id = id,
            title = title,
            shareObjectType = shareObjectType,
            initialTimestamp = initialTimestamp,
            onCopyClick = { link ->
                ClipboardHelper.save(context, text = link)
            },
            onShareClick = { link ->
                val intent = Intent(android.content.Intent.ACTION_SEND)
                    .putExtra(android.content.Intent.EXTRA_TEXT, link)
                    .putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                    .setType("text/plain")
                val shareIntent = android.content.Intent.createChooser(intent, context.getString(R.string.shareTo))
                context.startActivity(shareIntent)
                onDismissRequest()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateAvailableBottomSheet(
    updateName: String?,
    runNumber: String?,
    changelog: String?,
    onUpdate: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        UpdateAvailableSheet(
            updateName = updateName,
            runNumber = runNumber,
            changelog = changelog,
            onUpdate = onUpdate,
            onDismiss = onDismissRequest
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationBottomSheet(
    title: String,
    message: String,
    confirmText: String = "OK",
    cancelText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        ConfirmationSheet(
            title = title,
            message = message,
            confirmText = confirmText,
            cancelText = cancelText,
            isDestructive = isDestructive,
            onConfirm = {
                onConfirm()
                onDismissRequest()
            },
            onCancel = onDismissRequest
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerBottomSheet(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        ColorPickerSheet(
            initialColor = initialColor,
            onColorSelected = { color ->
                onColorSelected(color)
                onDismissRequest()
            },
            onDismiss = onDismissRequest
        )
    }
}
