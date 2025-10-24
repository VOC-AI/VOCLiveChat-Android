package com.rc.webcomponent

import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.vocai.sdk.LogUtil
import com.vocai.sdk.VocaiMessageCenter
import com.vocai.sdk.Vocai.Companion as VocaiSDK

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置沉浸式状态栏，避免挖孔屏顶部白条
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

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