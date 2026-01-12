package com.github.libretube.test.ui.components

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

@Composable
fun PlayerGestureOverlay(
    modifier: Modifier = Modifier,
    onSeek: (Long) -> Unit,
    onTap: () -> Unit = {},
    enabled: Boolean = true
) {
    if (!enabled) return

    val context = LocalContext.current
    val view = LocalView.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    
    var dragStartY by remember { mutableFloatStateOf(0f) }
    var dragStartX by remember { mutableFloatStateOf(0f) }
    var isDraggingVolume by remember { mutableStateOf(false) }
    var isDraggingBrightness by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { offset ->
                        // Double tap to seek: left side = -10s, right side = +10s
                        val screenWidth = size.width
                        val seekAmount = if (offset.x < screenWidth / 2) {
                            -10000L // Seek backward 10 seconds
                        } else {
                            10000L // Seek forward 10 seconds
                        }
                        onSeek(seekAmount)
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        dragStartY = offset.y
                        dragStartX = offset.x
                        val screenWidth = size.width
                        
                        // Determine if dragging on left (brightness) or right (volume) side
                        isDraggingBrightness = offset.x < screenWidth / 2
                        isDraggingVolume = offset.x >= screenWidth / 2
                    },
                    onDragEnd = {
                        isDraggingVolume = false
                        isDraggingBrightness = false
                    },
                    onDragCancel = {
                        isDraggingVolume = false
                        isDraggingBrightness = false
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        
                        if (isDraggingVolume) {
                            // Adjust volume
                            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            
                            // Negative dragAmount = swipe up = increase volume
                            val volumeChange = (-dragAmount / 40f).toInt() // Increased divisor for more precision
                            val newVolume = (currentVolume + volumeChange).coerceIn(0, maxVolume)
                            
                            audioManager.setStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                newVolume,
                                0
                            )
                        } else if (isDraggingBrightness) {
                            // Adjust brightness
                            val window = (view.context as? android.app.Activity)?.window
                            window?.let {
                                val layoutParams = it.attributes
                                val currentBrightness = layoutParams.screenBrightness
                                
                                // Negative dragAmount = swipe up = increase brightness
                                val brightnessChange = -dragAmount / 2000f // Increased divisor for more precision
                                val newBrightness = (currentBrightness + brightnessChange).coerceIn(0f, 1f)
                                
                                layoutParams.screenBrightness = newBrightness
                                it.attributes = layoutParams
                            }
                        }
                    }
                )
            }
    )
}
