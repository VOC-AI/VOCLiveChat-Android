package com.vocai.sdk.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import com.rajat.pdfviewer.PdfViewerActivity
import com.rajat.pdfviewer.util.saveTo
import com.vocai.sdk.LogUtil
import com.vocai.sdk.bottomsheet.ChooserBottomSheet
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream


internal class OpenPdfHandler {

    fun openPdf(context: Context, url: String, complete: (File) -> Unit) {
        LogUtil.info("start open pdf:$url")
        downloadFile(context, url) {
            Handler(Looper.getMainLooper()).post {
                LogUtil.info("file:$it")
                complete.invoke(it)
            }
        }
    }

    private fun downloadFile(context: Context, url: String, complete: (File) -> Unit) {
        Thread(Runnable {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            response.body?.byteStream().use { inputStream ->
                val cacheDir = File(context.cacheDir, "pdfs")
                cacheDir.mkdirs()
                val pdfFile = File(cacheDir, "downloaded.pdf")
                FileOutputStream(pdfFile).use { output ->
                    inputStream?.copyTo(output)
                }
                LogUtil.info("download complete pdf:${pdfFile.absolutePath}")
                complete.invoke(pdfFile)
            }
        }).start()
    }

//    private fun openPdfWithSystemApp(context: Context, pdfFile: File) {
//        // 通过 FileProvider 生成 URI
//        val uri = FileProvider.getUriForFile(
//            context,
//            "${context.packageName}${ChooserBottomSheet.AUTHORITIES_FILE_PROVIDER}", // 必须与 Manifest 中的 authorities 一致
//            pdfFile
//        )
//
//        // 创建 Intent 打开文件
//        val intent = Intent(Intent.ACTION_VIEW).apply {
//            setDataAndType(uri, "application/pdf")
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }
//
//        // 检查是否有可用的应用
//        if (intent.resolveActivity(context.packageManager) != null) {
//            context.startActivity(intent)
//        } else {
//            // 提示用户安装 PDF 阅读器
//            // 例如跳转到 Play Store 的 Adobe Acrobat 页面
//            val storeIntent = Intent(Intent.ACTION_VIEW).apply {
//                data = Uri.parse("market://details?id=com.adobe.reader")
//            }
//            context.startActivity(storeIntent)
//        }
//    }

}