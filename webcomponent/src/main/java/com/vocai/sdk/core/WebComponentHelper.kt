package com.vocai.sdk.core

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Looper
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentManager
import com.vocai.sdk.Constants
import com.vocai.sdk.Constants.WEB_TYPE_HIDE_LOADING
import com.vocai.sdk.Constants.WEB_TYPE_OPEN_FILE
import com.vocai.sdk.LogUtil
import com.vocai.sdk.PdfActivity
import com.vocai.sdk.Vocai
import com.vocai.sdk.bottomsheet.ChooserBottomSheet
import com.vocai.sdk.model.FILE_TYPE_ERROR
import com.vocai.sdk.model.FILE_TYPE_OTHER
import com.vocai.sdk.model.FILE_TYPE_PIC
import com.vocai.sdk.model.FILE_TYPE_VIDEO
import com.vocai.sdk.model.NavigateMessage
import com.vocai.sdk.util.StringUtils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class WebComponentHelper {

    private lateinit var mWebView: WebView
    private var fragmentManager: FragmentManager? = null

    var mBottomSheet: ChooserBottomSheet? = null
    var onFileChosen: ((NavigateMessage, String, Int, String?) -> Unit)? = null
    var onProgressUpdate: ((Int) -> Unit)? = null
    var onClosePage: (() -> Unit)? = null

    companion object {
        const val CUSTOM_USER_AGENT = "Vocai/1.0"
        const val TAG_CHOOSER = "chooser"
    }

    fun bind(webView: WebView) {
        this.mWebView = webView
        setUpWebSettings()
        setupWebClient()
        setupWebChromeClient()
        val url = Vocai.getInstance().buildUrl()
        LogUtil.info("start url->$url")
        this.mWebView.setBackgroundColor(Color.TRANSPARENT)
        this.mWebView.loadUrl(url)
        onProgressUpdate?.invoke(0)
        setupJsInterface()
        mBottomSheet = ChooserBottomSheet()
    }

    fun attachToPage(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun handleBackPress(): Boolean {
        if (this.mWebView.canGoBack()) {
            this.mWebView.goBack()
            return true
        } else return false
    }

    private fun setupJsInterface() {
        mWebView.addJavascriptInterface(VOCLivechatMessageHandler(mWebView.context) {
            val currentMessage = Json.decodeFromString<NavigateMessage>(it)
            LogUtil.info("收到页面请求:" + currentMessage.type )
            if (currentMessage.type == Constants.WEB_TYPE_INPUT_FILE) {
                fragmentManager?.let {
                    mBottomSheet?.apply {
                        onResultChosen = { file, type, fileName ->
                            onFileChosen?.invoke(currentMessage, file.absolutePath, type, fileName)
                            mBottomSheet?.dismiss()
                        }
                    }?.show(it, TAG_CHOOSER)
                }
            } else if (currentMessage.type == WEB_TYPE_OPEN_FILE) {
                android.os.Handler(Looper.getMainLooper()).post {
                    LogUtil.info("open file:${currentMessage.data?.url}")
                    val intent = Intent(mWebView.context, PdfActivity::class.java).apply {
                        this.putExtra("file", currentMessage.data?.url ?: "")
                    }
                    mWebView.context.startActivity(intent)
                }
            } else if(currentMessage.type == WEB_TYPE_HIDE_LOADING) {
                onProgressUpdate?.invoke(100)
            } else if(currentMessage.type == Constants.WEB_TYPE_CLOSE_PAGE) {
                android.os.Handler(Looper.getMainLooper()).post {
                    onClosePage?.invoke()
                }
            }
        }, "VOCLivechatMessageHandler")
    }

    fun handleFileError(message: String, randomId: String) {
//            setTimeout(function() {if(typeof handleUploadError==='function')
        val javascript =
            "javascript:setTimeout(function() {${getLoadingMethodByType(FILE_TYPE_ERROR)}('UploadError','${randomId}','${message}') }, 300)"
        LogUtil.info("execute postLoadingStateToWebView javascript-> $javascript")
        mWebView.post {
            mWebView.loadUrl(javascript)
        }
    }

    fun postLoadingStateToWebView(type: Int, randomId: String, fileName: String? = null) {
        var javascript = ""
        if (type == FILE_TYPE_ERROR) {
            handleFileError("Error Exceed", randomId)
            return
        }

        if (type != FILE_TYPE_OTHER)
            javascript = "javascript:${getLoadingMethodByType(type)}('${randomId}')"
        else {
            javascript = "javascript:${
                getLoadingMethodByType(type)
            }('${randomId}','${fileName ?: "file"}')"
        }
        LogUtil.info("execute postLoadingStateToWebView javascript-> $javascript")
        mWebView.post {
            mWebView.loadUrl(javascript)
        }
    }

    private fun getLoadingMethodByType(type: Int): String {
        return when (type) {
            FILE_TYPE_PIC -> "handleRecieveImageLoading"
            FILE_TYPE_VIDEO -> "handleRecieveVideoLoading"
            FILE_TYPE_OTHER -> "handleRecieveFileLoading"
            FILE_TYPE_ERROR -> "handleUploadError"
            else -> "handleRecieveImageLoading"
        }
    }

    fun postResultToWebView(url: String, type: Int, randomId: String, fileName: String? = null) {
        val hashMap = hashMapOf("reserveId" to randomId, "filename" to fileName)
        val javascript =
            "javascript:${getResultMethodByType(type)}('$url',${Json.encodeToString(hashMap)})"

        LogUtil.info("execute postResultToWebView javascript-> $javascript")
        mWebView.post {
            mWebView.loadUrl(javascript)
        }
    }

    fun saveChatId() {
        val jsCode = "javascript:getChatId()"

        mWebView.postDelayed( {
            mWebView.evaluateJavascript(jsCode) { result ->
                LogUtil.info("chatId: $result")

                if (!StringUtils.isEmptyString(result)) {
                    var chatId = result.trim().removeSurrounding("\"")
                    Vocai.getInstance().setChatId(chatId)
                }
            }
        }, 5000L)

    }

    private fun getResultMethodByType(type: Int): String {
        return when (type) {
            FILE_TYPE_PIC -> "handleRecieveImage"
            FILE_TYPE_VIDEO -> "handleRecieveVideo"
            FILE_TYPE_OTHER -> "handleRecieveFile"
            else -> "handleRecieveImage"
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebSettings() {
        mWebView.settings.apply {
            WebView.setWebContentsDebuggingEnabled(true)
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
            setSupportZoom(false)
            userAgentString = "${this.userAgentString} $CUSTOM_USER_AGENT"
        }
    }

    private fun setupWebClient() {
        mWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onProgressUpdate?.invoke(0)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
            }
        }
    }

    private fun setupWebChromeClient() {
        mWebView.webChromeClient = object : WebChromeClient() {

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                return super.onConsoleMessage(consoleMessage)
            }

        }
    }


}