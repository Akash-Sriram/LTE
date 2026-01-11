package com.github.libretube.test.ui.sheets

import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.libretube.test.R
import com.github.libretube.test.ui.tools.SleepTimer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheetCompose(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var minutesInput by remember { mutableStateOf("") }
    
    // Timer state observation
    // We need to poll the time left similar to the legacy implementation
    var timeLeftMillis by remember { mutableStateOf(SleepTimer.timeLeftMillis) }
    
    LaunchedEffect(Unit) {
        while (true) {
            timeLeftMillis = SleepTimer.timeLeftMillis
            delay(1000)
        }
    }

    val isTimerRunning = timeLeftMillis > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.sleep_timer),
                style = MaterialTheme.typography.titleLarge
            )

            if (isTimerRunning) {
                // Running State
                Text(
                    text = DateUtils.formatElapsedTime(timeLeftMillis / 1000),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Button(
                    onClick = {
                        SleepTimer.disableSleepTimer()
                        timeLeftMillis = 0 // Immediate UI update
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.disable_sleep_timer))
                }
            } else {
                // Setup State
                OutlinedTextField(
                    value = minutesInput,
                    onValueChange = { if (it.all { char -> char.isDigit() }) minutesInput = it },
                    label = { Text(stringResource(R.string.time_in_minutes)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val minutes = minutesInput.toLongOrNull()
                        if (minutes != null && minutes > 0) {
                            SleepTimer.setup(context, minutes)
                            timeLeftMillis = SleepTimer.timeLeftMillis // Immediate UI update
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = minutesInput.isNotEmpty()
                ) {
                    Text(stringResource(R.string.start_sleep_timer))
                }
            }
        }
    }
}
