package com.vocai.sdk.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class NavigationInfo(
    val botId: Long? = null,
    val chatId: String? = null,
    val contact: String? = null,
    val url: String? = null
)