package com.vocai.sdk.core

import android.content.Context
import android.webkit.JavascriptInterface
import androidx.annotation.Keep

@Keep
class VOCLivechatMessageHandler(val context: Context, val onReceiveMessage: (String) -> Unit) {

    @JavascriptInterface
    fun receiveMessage(message: String) {
        onReceiveMessage.invoke(message)
    }

}