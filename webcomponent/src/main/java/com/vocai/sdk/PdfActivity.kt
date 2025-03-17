package com.vocai.sdk

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.vocai.sdk.databinding.ActivityPdfBinding
import com.vocai.sdk.helpers.OpenPdfHandler

internal class PdfActivity() : AppCompatActivity() {

    private lateinit var binding: ActivityPdfBinding
//    private var mLoadingDialog: LoadingDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
//        mLoadingDialog = LoadingDialogFragment()
//        mLoadingDialog!!.show(supportFragmentManager, "mActivityLoading")
    }

    private fun hideLoading() {
//        mLoadingDialog?.dismiss()
    }

}