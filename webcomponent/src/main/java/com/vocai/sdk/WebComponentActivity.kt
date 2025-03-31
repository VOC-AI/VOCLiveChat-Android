package com.vocai.sdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

internal class WebComponentActivity : AppCompatActivity() {

    lateinit var fragment: VocaiComponentFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        fragment = VocaiComponentFragment()
        supportFragmentManager.beginTransaction().apply {
            this.add(R.id.container, fragment)
        }.commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        if(!fragment.handleBackPress())
            super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        Vocai.getInstance().wrapper.onCancel?.invoke()
    }

}