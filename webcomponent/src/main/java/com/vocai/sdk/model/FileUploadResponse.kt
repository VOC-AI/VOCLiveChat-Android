package com.vocai.sdk.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class FileUploadResponse(
    val name: String? = null,
    val url: String? = null,
    val jobId: String? = null
)

@Serializable
@Keep
data class CheckStateResponse(
    val finished: Boolean = false,
    val status: String? = null,
    val success: Boolean? = false,
    val url: String? = null
)