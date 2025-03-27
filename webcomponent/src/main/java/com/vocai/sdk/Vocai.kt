package com.vocai.sdk

import android.content.Context

class Vocai internal constructor() {

    internal val wrapper: VocaiWrapper = VocaiWrapper()

    companion object {

        fun getInstance(): Vocai = VocaiInstanceHolder.getInstance()
    }

    fun init(context: Context, isDebug: Boolean, onCancel: (() -> Unit)? = null) {
        wrapper.init(context, isDebug, onCancel)
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
        extra: HashMap<String, String>? = null
    ) {
        val appendMap = hashMapOf<String, String>().apply {
            if (chatId?.isNotEmpty() == true) {
                this["chatId"] = chatId
            }
            if (email?.isNotEmpty() == true) {
                this["email"] = email
            }
            if (language?.isNotEmpty() == true) {
                this["language"] = language
            }
        }
        val finalMap = extra?.apply {
            this.putAll(appendMap)
        } ?: appendMap
        wrapper.startChat(id, token, finalMap)
    }

}