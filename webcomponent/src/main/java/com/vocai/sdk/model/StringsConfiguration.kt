package com.vocai.sdk.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
class StringsConfiguration(
    val entities: ArrayList<StringsEntry> = arrayListOf()
)

@Keep
@Serializable
class StringsEntry(
    val countryCode: String? = null,
    val language: String? = null,
    val strings: HashMap<String, String>? = null
)