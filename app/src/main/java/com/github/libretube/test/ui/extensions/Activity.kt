package com.github.libretube.test.ui.extensions

import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type.InsetsType
import androidx.core.view.WindowInsetsControllerCompat

import androidx.appcompat.app.AppCompatActivity
// import com.github.libretube.test.ui.fragments.PlayerFragment

fun Window.toggleSystemBars(@InsetsType types: Int, showBars: Boolean) {
    WindowCompat.getInsetsController(this, decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (showBars) {
            show(types)
        } else {
            hide(types)
        }
    }
}

// function runOnPlayerFragment deleted

