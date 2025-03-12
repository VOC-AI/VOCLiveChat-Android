package com.vocai.sdk.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ComponentConfiguration(
    val api: ApiConfig,
    val networkConfig: NetworkConfig,
    val autoTitle: Boolean = false
)

@Keep
@Serializable
data class ApiConfig(
    val protocol: String? = null,
    val host: String? = null,
    val uploadPath: String? = null
)

@Keep
@Serializable
data class NetworkConfig(
    val retryOnFailure: Boolean,
    val timeoutTime: Long
)