package com.vocai.sdk

import android.content.Context
import android.text.format.DateUtils
import android.webkit.CookieManager
import java.util.Date

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

    fun setChatId(chatId: String) {
        wrapper.setChatId(chatId)
    }

    fun getChatId(): String? {
        return wrapper.getChatId()
    }

    fun startPollUnread(botId: String, userId:String) {
        VocaiMessageCenter.instance.startPolling(botId, userId)
    }

    fun stopPollUnread() {
        VocaiMessageCenter.instance.stopPolling()
    }

    fun subscribeUnread(callback: (Boolean) -> Unit) {
        VocaiMessageCenter.instance.subscribe { hasUnread ->
            callback(hasUnread)
        }
    }

    fun bindLoginUser(userId: String) {
        VocaiMessageCenter.instance.bindLoginUser(getId().toLong(), userId)
    }

    fun clearChat() {
        val cookieManager = CookieManager.getInstance()
        val domain = "apps.voc.ai"
        val prefix = "shulex_chatbot"
        // 获取 apps.voc.ai 域名下的所有 cookie
        val cookies = cookieManager.getCookie(domain) ?: return

        // 分割、去空格、处理每个 cookie
        cookies.split(";")
            .map { it.trim() }
            .forEach { cookie ->
                if (cookie.startsWith(prefix, ignoreCase = true)) {
                    val cookieName = cookie.substringBefore("=")
                    // 设置为空字符串
                    cookieManager.setCookie(domain, "$cookieName=; Path=/")
                }
            }

        cookieManager.flush()
    }

    private fun handleLanguage(langCode: String): String {
        return LanguageHelper.normalizeLanguage(langCode)
    }


}