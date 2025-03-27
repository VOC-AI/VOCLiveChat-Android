package com.vocai.sdk

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.google.firebase.messaging.FirebaseMessaging
import com.vocai.sdk.core.ConfigLoader
import com.vocai.sdk.model.ComponentConfiguration
import com.vocai.sdk.model.StringsConfiguration
import java.net.URLEncoder

internal class VocaiWrapper {

    private var context: Context? = null
    private var id = DEFAULT_ID
    private var token = DEFAULT_TOKEN
    private var url = DEFAULT_URL
    private var extra: HashMap<String, String>? = null

    private val loader = ConfigLoader()
    internal var config: ComponentConfiguration? = null
    internal var stringsEntry: StringsConfiguration? = null
    internal var isDebugMode: Boolean = false
    internal var isEnabledFirebase: Boolean = false
    internal var language: String = DEFAULT_LANGUAGE
    internal var onCancel: (() -> Unit)? = null
    internal lateinit var strings: HashMap<String, String>

    companion object {
        internal const val DEFAULT_ID = "158"
        internal const val DEFAULT_TOKEN = "6539C961E4B0C98CA66D2BDE"
        internal const val DEFAULT_URL = "https://apps.voc.ai/live-chat"
        internal const val DEFAULT_LANGUAGE = "en"
    }

    fun init(context: Context, isDebug: Boolean, onCancel: (() -> Unit)? = null) {
        this.context = context.applicationContext
        this.onCancel = onCancel
        config = loader.loadConfig(context)
        isDebugMode = isDebug
        if (isEnabledFirebase) {
            checkFirebaseStateWhenSdkInit()
        }
        getLanguageCodeCompat()
        stringsEntry = loader.loadStringsMapper(context)
        strings =
            stringsEntry?.entities?.firstOrNull { it.language?.lowercase() == language.lowercase() }?.strings
                ?: getDefaultStrings()
    }

    fun getDefaultStrings(): HashMap<String, String> {
        return stringsEntry?.entities?.firstOrNull { it.language?.lowercase() == DEFAULT_LANGUAGE.lowercase() }?.strings
            ?: hashMapOf()
    }

    fun getLanguageCodeCompat() {
        context?.let {
            language = it.resources.configuration.locales.get(0).country
        }
    }

    fun checkFirebaseStateWhenSdkInit() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                LogUtil.info("firebase init failed. -> ${it.exception}")
                return@addOnCompleteListener
            }
            val token = it.result
            VocaiPushNotifications.updateToken(token)
        }
    }

    fun getContext(): Context? = context

    fun setDebug(debug: Boolean) {
        isDebugMode = debug
    }

    fun setUrl(url: String) {
        this.url = url
    }

    internal fun buildUrl(): String {
        var append = ""
        extra?.map {
            if (it.key.isNotEmpty() && it.value.isNotEmpty()) {
                append += "&${it.key}=${URLEncoder.encode(it.value, "utf-8")}"
            }
        }
        return "$url?disableFileInputModal=true&id=$id&token=$token$append"
    }

    fun startChat(id: String, token: String) {
        this.id = id
        this.token = token
        context?.startActivity(Intent(context, WebComponentActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
    }

    fun startChat(id: String, token: String, extra: HashMap<String, String>? = null) {
        this.id = id
        this.token = token
        this.extra = extra
        context?.startActivity(Intent(context, WebComponentActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
    }

}