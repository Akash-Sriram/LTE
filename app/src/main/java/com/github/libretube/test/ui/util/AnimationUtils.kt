package com.github.libretube.test.ui.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

fun Modifier.animateEnter(
    index: Int,
    delayPerItem: Int = 100 // Delay between items
): Modifier = composed {
    val alpha = remember { Animatable(0f) }
    val translationY = remember { Animatable(50f) } // Start 50px down (approx)
    val density = LocalDensity.current

    LaunchedEffect(key1 = Unit) {
        // Only stagger the first 15 items (approx one screen).
        // Subsequent items animate immediately (or with minimal delay) to avoid empty periods on scroll.
        val delay = if (index < 15) index * delayPerItem else 0
        
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    LaunchedEffect(key1 = Unit) {
        val delay = if (index < 15) index * delayPerItem else 0

        translationY.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            )
        )
    }

    this.graphicsLayer {
        this.alpha = alpha.value
        this.translationY = translationY.value * density.density
    }
}
