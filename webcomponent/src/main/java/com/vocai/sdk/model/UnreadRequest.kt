package com.vocai.sdk.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class UnreadResponse(

    val hasUnread: Boolean

)

@Serializable
@Keep
data class UnreadRequest (

    val userId: String

)

@Serializable
@Keep
data class CommitUserRequest (

    val userId: String,

    val chatId: String,

    val botId: Long

)
