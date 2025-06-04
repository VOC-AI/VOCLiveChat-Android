package com.vocai.sdk

import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import com.vocai.sdk.core.ConfigLoader
import com.vocai.sdk.model.ComponentConfiguration
import com.vocai.sdk.model.StringsConfiguration
import java.net.URLEncoder
import java.util.Locale
import java.util.UUID
import androidx.core.content.edit

internal class VocaiWrapper {

    private var context: Context? = null
    private var id = DEFAULT_ID
    private var token = DEFAULT_TOKEN
    private var url = DEFAULT_URL
    private var extra: HashMap<String, String>? = null
    private var userId: String? = null

    private val loader = ConfigLoader()
    internal var config: ComponentConfiguration? = null
    internal var stringsEntry: StringsConfiguration? = null
    internal var isDebugMode: Boolean = false
    internal var isEnabledFirebase: Boolean = false
    internal var language: String = DEFAULT_LANGUAGE
    internal var onCancel: (() -> Unit)? = null
    internal lateinit var strings: HashMap<String, String>
    internal var maxUploadFileSize: Long = 50 * 1024 * 1024

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
        determinStrings(language)
    }

    private fun determinStringsImpl(langCode: String): HashMap<String, String>? {
        val lang = langCode.lowercase()
        return stringsEntry?.entities?.firstOrNull { it.language?.lowercase() == lang.lowercase() }?.strings
    }

    private fun determinStrings(langCode: String) {
        stringsEntry = loader.loadStringsMapper(context as Context)
        val lang = langCode.lowercase()
        val stringsTemp = determinStringsImpl(lang)
        if (stringsTemp != null) {
            strings = stringsTemp
            return
        }
        val l = lang.replace(Regex("_"), "-")
            .split('-')
            .getOrNull(0)
        val stringsLangCodeMap = mapOf(
            "en-US" to "en",
            "zh-CN" to "cn",
            "ja-JP" to "ja",
            "fr-FR" to "fr",
            "de-DE" to "de",
            "pt-PT" to "pt",
            "es-ES" to "es",
            "ko-KR" to "ko",
            "it-IT" to "it",
            "zh" to "cn",
            "zh-TW" to "zh-HK",
            "zh-Hant" to "zh-HK",
            "zh-Hant-HK" to "zh-HK",
            "zh-Hant-TW" to "zh-HK",
            "jp" to "ja",
            "ar" to "ar"
        )
        val stringsLangCode = (stringsLangCodeMap[l]?.takeIf { it.isNotBlank() } ?: l) as String
        strings =
            stringsEntry?.entities?.firstOrNull { it.language?.lowercase() == stringsLangCode.lowercase() }?.strings
                ?: getDefaultStrings()
    }

    fun setMaxFileUploadSize(value: Long) {
        maxUploadFileSize = value
    }

    fun getDefaultStrings(): HashMap<String, String> {
        return stringsEntry?.entities?.firstOrNull { it.language?.lowercase() == DEFAULT_LANGUAGE.lowercase() }?.strings
            ?: hashMapOf()
    }

    private fun getLanguageCodeCompat() {
        context?.let {
            language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.resources.configuration.locales.get(0).country
            } else Locale.getDefault().country
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

    fun getMaxFileUploadSize(): Long {
        return maxUploadFileSize
    }

    internal fun buildUrl(): String {
        var append = ""
        extra?.map {
            if (it.key == "language" && it.value.isNotEmpty()) {
                determinStrings(it.value)
            }
            if (it.key.isNotEmpty() && it.value.isNotEmpty()) {
                append += "&${it.key}=${URLEncoder.encode(it.value, "utf-8")}"
            }
        }
        return "$url?disableFileInputModal=true&hideLoading=true&id=$id&token=$token$append"
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

    fun getId(): String {
        return id;
    }

    fun getOrCreateUserId(): String {
        if (userId != null) {
            return userId!!
        }

        userId = UUID.randomUUID().toString()
        return userId!!
    }

    fun getUserId(): String {
        return userId ?: ""
    }

//    fun getOrCreateUserId(): String {
//        return UUID.randomUUID().toString()
//    }
//
//    fun getUserId(): String {
//        return extra?.get("userId")
//            ?: extra?.get("email")
//            ?: UUID.randomUUID().toString()
//    }
}