package com.rc.webcomponent

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vocai.sdk.Vocai.Companion as VocaiSDK

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        VocaiSDK.getInstance().init(this,true) {
            Log.i("MainActivity","vocai sdk is canceled")
        }
        findViewById<TextView>(R.id.mChatStart).setOnClickListener {
            VocaiSDK.getInstance().startChat("19365","6731F71BE4B0187458389512", null,null,"ko-HK",hashMapOf(
                "email" to "boyuan.gao@shulex-tech.com"
            ))
        }

    }
}