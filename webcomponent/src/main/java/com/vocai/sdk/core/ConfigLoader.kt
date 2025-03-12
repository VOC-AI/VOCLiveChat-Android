package com.vocai.sdk.core

import android.content.Context
import android.content.res.AssetManager
import com.vocai.sdk.model.ComponentConfiguration
import com.vocai.sdk.model.StringsConfiguration
import kotlinx.serialization.json.Json
import java.io.IOException

internal class ConfigLoader {

    fun loadConfig(context: Context): ComponentConfiguration? {
        return context.assets.getConfig<ComponentConfiguration>("componentConfig.json")
    }

    fun loadStringsMapper(context: Context): StringsConfiguration {
        return context.assets.getConfig<StringsConfiguration>("strings_i18n.json")
            ?: StringsConfiguration()
    }

}

internal inline fun <reified T> AssetManager.getConfig(path: String): T? {
    try {
        val json = Json {
            ignoreUnknownKeys = true
        }
        return json.decodeFromString(this.open(path).reader().readText())
    } catch (e: IOException) {
        return null
    }
}