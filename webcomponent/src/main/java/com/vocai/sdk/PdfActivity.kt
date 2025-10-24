package com.vocai.sdk

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.vocai.sdk.databinding.ActivityPdfBinding
import com.vocai.sdk.helpers.OpenPdfHandler
import com.vocai.sdk.widget.LoadingDialogFragment

internal class PdfActivity() : AppCompatActivity() {

    private lateinit var binding: ActivityPdfBinding
    private var mLoadingDialog: LoadingDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置沉浸式状态栏，避免挖孔屏顶部白条
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        binding = ActivityPdfBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        val file = intent.getStringExtra("file").orEmpty()
        binding.mCloseIv.setOnClickListener { finish() }
        LogUtil.info("start open pdf file:$file")
        if(file.isNotEmpty()) {
            showLoading()
            OpenPdfHandler().openPdf(this, file) {
                binding.pdfView.initWithFile(it)
                hideLoading()
            }
        } else {
            finish()
        }
    }

    private fun showLoading() {
        mLoadingDialog = LoadingDialogFragment()
        mLoadingDialog!!.show(supportFragmentManager, "mActivityLoading")
    }

    private fun hideLoading() {
        mLoadingDialog?.dismiss()
    }

}