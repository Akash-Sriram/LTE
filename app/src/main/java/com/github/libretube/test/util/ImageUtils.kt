package com.github.libretube.test.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun rememberContentWithCrossfade(model: Any?): ImageRequest {
    val context = LocalContext.current
    return remember(model) {
        ImageRequest.Builder(context)
            .data(model)
            .crossfade(true)
            .build()
    }
}
