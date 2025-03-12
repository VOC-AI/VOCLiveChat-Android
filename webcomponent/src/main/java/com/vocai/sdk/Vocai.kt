package com.vocai.sdk

import android.content.Context
import androidx.fragment.app.FragmentManager

class Vocai internal constructor(){

    internal val wrapper: VocaiWrapper = VocaiWrapper()

    companion object {

        fun getInstance():Vocai = VocaiInstanceHolder.getInstance()
    }

    fun init(context: Context, isDebug: Boolean) {
        wrapper.init(context, isDebug)
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

    fun startChat(id: String, token: String, extra: HashMap<String, String>? = null) {
        wrapper.startChat(id, token, extra)
    }

}