package com.github.libretube.test.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import com.github.libretube.test.constants.IntentData
import com.github.libretube.test.ui.theme.LibreTubeTheme

class ZoomableImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bitmapUrl = intent.getStringExtra(IntentData.bitmapUrl)!!

        setContent {
            LibreTubeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        var isLoading by remember { mutableStateOf(true) }
                        
                        AsyncImage(
                            model = bitmapUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            onSuccess = { isLoading = false },
                            onLoading = { isLoading = true }
                        )
                        
                        if (isLoading) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
