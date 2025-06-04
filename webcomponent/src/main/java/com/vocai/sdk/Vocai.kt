package com.vocai.sdk

import android.content.Context
import java.util.UUID

class Vocai internal constructor() {

    internal val wrapper: VocaiWrapper = VocaiWrapper()

    companion object {

        fun getInstance(): Vocai = VocaiInstanceHolder.getInstance()
    }

    fun init(context: Context, isDebug: Boolean, onCancel: (() -> Unit)? = null) {
        wrapper.init(context, isDebug, onCancel)
    }

    fun setMaxFileUploadSize(value: Long) {
        return wrapper.setMaxFileUploadSize(value)
    }
    fun getMaxFileUploadSize(): Long {
        return wrapper.getMaxFileUploadSize()
    }

    fun setUrl(url: String) {
        wrapper.setUrl(url)
    }

    internal fun buildUrl(): String {
        return wrapper.buildUrl()
    }

    fun isDebug(): Boolean = wrapper.isDebugMode

    fun setDebug(debug: Boolean) {
        wrapper.setDebug(debug)
    }

    fun startChat(id: String, token: String) {
        wrapper.startChat(id, token)
    }

    fun startChat(
        id: String,
        token: String,
        chatId: String? = null,
        email: String? = null,
        language: String? = null,
        userId: String? = null,
        extra: HashMap<String, String>? = null
    ) {
        val appendMap = hashMapOf<String, String>().apply {
            if (chatId?.isNotEmpty() == true) {
                this["chatId"] = chatId
            }
            if (email?.isNotEmpty() == true) {
                this["email"] = email
            }

            this["userId"] = if (userId?.isNotEmpty() == true) {
                userId
            } else {
                wrapper.getOrCreateUserId()
            }

            if (language?.isNotEmpty() == true) {
                this["language"] = handleLanguage(language)
                this["lang"] = handleLanguage(language)
            }
        }
        val finalMap = extra?.apply {
            this.putAll(appendMap)
        } ?: appendMap
        wrapper.startChat(id, token, finalMap)
    }

    fun getId(): String {
        return wrapper.getId();
    }

    fun getUserId(): String {
        return wrapper.getUserId();
    }

    fun subscribeUnread(callback: (Boolean) -> Unit) {
        VocaiMessageCenter.instance.subscribe { hasUnread -> {
            callback(hasUnread)
        } }
    }

    private fun handleLanguage(langCode: String): String {
        return LanguageHelper.normalizeLanguage(langCode)
    }


}