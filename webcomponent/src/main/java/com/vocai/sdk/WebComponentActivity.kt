package com.vocai.sdk

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

internal class WebComponentActivity : AppCompatActivity() {

    lateinit var fragment: VocaiComponentFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置挖孔屏延伸到刘海区域
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
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