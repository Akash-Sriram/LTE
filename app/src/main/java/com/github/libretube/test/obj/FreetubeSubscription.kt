package com.github.libretube.test.obj

import com.github.libretube.test.constants.IntentData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FreetubeSubscription(
    val name: String,
    @SerialName("id") val channelId: String,
    val url: String = "${IntentData.YOUTUBE_FRONTEND_URL}/channel/$channelId"
)

