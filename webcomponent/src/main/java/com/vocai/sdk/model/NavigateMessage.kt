package com.vocai.sdk.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class NavigateMessage(
    val type: String,
    val data: NavigationInfo
)
