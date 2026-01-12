package com.github.libretube.test.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> runSafely(
    onSuccess: (List<T>) -> Unit = { },
    ioBlock: suspend () -> List<T>
) {
    withContext(Dispatchers.IO) {
        val result = runCatching { ioBlock.invoke() }
            .onFailure { android.util.Log.e("RunSafely", "Error in runSafely", it) }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() } 
        
        if (result == null) {
             android.util.Log.w("RunSafely", "Result was null or empty")
             return@withContext
        }

        withContext(Dispatchers.Main) {
            if (result.isNotEmpty()) {
                onSuccess.invoke(result)
            }
        }
    }
}

