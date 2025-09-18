package com.rc.webcomponent

import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vocai.sdk.LogUtil
import com.vocai.sdk.VocaiMessageCenter
import com.vocai.sdk.Vocai.Companion as VocaiSDK

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        VocaiSDK.getInstance().init(this, isDebug = true) {
            Log.i("MainActivity","vocai sdk is canceled")
        }

//        VocaiSDK.getInstance().startPollUnread("12693", "89757000001ZW")
//        VocaiSDK.getInstance().stopPollUnread()
        VocaiSDK.getInstance().subscribeUnread { hasUnread ->
            LogUtil.info("hasUnread:$hasUnread")
        }

        findViewById<TextView>(R.id.mChatStart).setOnClickListener {
            VocaiSDK.getInstance().setMaxFileUploadSize(10 * 1024 * 1024)
//            VocaiSDK.getInstance().startChat("12693","6603F148E4B0FDA74F2A353A", null,null,"zh-CN", "89757000001ZW")
//            val cookieManager = CookieManager.getInstance()
//            LogUtil.info("CurrentCookies" + cookieManager.getCookie("apps.voc.ai"))
//            VocaiSDK.getInstance().clearChat()
//            LogUtil.info("CurrentCookies" + cookieManager.getCookie("apps.voc.ai"))

            VocaiSDK.getInstance().startChat(
                id = "12693",
                token = "6603F148E4B0FDA74F2A353A",
                chatId = null,
                email = null,
                language = "zh-CN",
                userId = "89757000001ZW",
                extra =  hashMapOf("noBrand" to "true", "back_btn" to "true")
            )
        }

    }
}