package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Color picker sheet with ARGB sliders and hex input
 */
@Composable
fun ColorPickerSheet(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var alpha by remember { mutableFloatStateOf(initialColor.alpha) }
    var red by remember { mutableFloatStateOf(initialColor.red) }
    var green by remember { mutableFloatStateOf(initialColor.green) }
    var blue by remember { mutableFloatStateOf(initialColor.blue) }
    
    var hexInput by remember { 
        mutableStateOf(colorToHexString(initialColor))
    }
    var isHexValid by remember { mutableStateOf(true) }

    val currentColor = Color(red, green, blue, alpha)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "Color Picker",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Color preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(currentColor, RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hex input
        OutlinedTextField(
            value = hexInput,
            onValueChange = { newHex ->
                hexInput = newHex.uppercase()
                if (newHex.length == 9 && newHex.startsWith("#")) {
                    try {
                        val color = android.graphics.Color.parseColor(newHex)
                        alpha = android.graphics.Color.alpha(color) / 255f
                        red = android.graphics.Color.red(color) / 255f
                        green = android.graphics.Color.green(color) / 255f
                        blue = android.graphics.Color.blue(color) / 255f
                        isHexValid = true
                    } catch (e: IllegalArgumentException) {
                        isHexValid = false
                    }
                }
            },
            label = { Text("Hex Color") },
            placeholder = { Text("#AARRGGBB") },
            isError = !isHexValid,
            supportingText = if (!isHexValid) {
                { Text("Invalid color format") }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Alpha slider
        ColorSlider(
            label = "Alpha",
            value = alpha,
            onValueChange = { 
                alpha = it
                hexInput = colorToHexString(currentColor)
            },
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Red slider
        ColorSlider(
            label = "Red",
            value = red,
            onValueChange = { 
                red = it
                hexInput = colorToHexString(currentColor)
            },
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Green slider
        ColorSlider(
            label = "Green",
            value = green,
            onValueChange = { 
                green = it
                hexInput = colorToHexString(currentColor)
            },
            color = Color.Green
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Blue slider
        ColorSlider(
            label = "Blue",
            value = blue,
            onValueChange = { 
                blue = it
                hexInput = colorToHexString(currentColor)
            },
            color = Color.Blue
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { onColorSelected(currentColor) },
                modifier = Modifier.weight(1f)
            ) {
                Text("OK")
            }
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = (value * 255).toInt().toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color
            )
        )
    }
}

private fun colorToHexString(color: Color): String {
    val argb = color.toArgb()
    return String.format("#%08X", argb)
}
