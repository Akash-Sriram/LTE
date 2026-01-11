package com.github.libretube.test.ui.models

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.github.libretube.test.obj.update.UpdateInfo

class UpdateViewModel : ViewModel() {
    private val _updateInfo = mutableStateOf<UpdateInfo?>(null)
    val updateInfo: State<UpdateInfo?> = _updateInfo

    private val _downloadUrl = mutableStateOf<String?>(null)
    val downloadUrl: State<String?> = _downloadUrl

    private val _runNumber = mutableStateOf<String?>(null)
    val runNumber: State<String?> = _runNumber

    private val _sanitizedBody = mutableStateOf<String?>(null)
    val sanitizedBody: State<String?> = _sanitizedBody

    fun showUpdate(info: UpdateInfo, url: String, run: String, sanitized: String? = null) {
        _updateInfo.value = info
        _downloadUrl.value = url
        _runNumber.value = run
        _sanitizedBody.value = sanitized
    }

    fun dismissUpdate() {
        _updateInfo.value = null
        _downloadUrl.value = null
        _runNumber.value = null
        _sanitizedBody.value = null
    }
}
