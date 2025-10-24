package com.vocai.sdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

internal class WebComponentActivity : AppCompatActivity() {

    lateinit var fragment: VocaiComponentFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置沉浸式状态栏，避免挖孔屏顶部白条
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
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