package com.vocai.sdk.core

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
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
    private val loadingTimeoutHandler = android.os.Handler(Looper.getMainLooper())
    private var loadingTimeoutRunnable: Runnable? = null
    
    // 视频全屏支持
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalSystemUiVisibility = 0

    var mBottomSheet: ChooserBottomSheet? = null
    var onFileChosen: ((NavigateMessage, String, Int, String?) -> Unit)? = null
    var onProgressUpdate: ((Int) -> Unit)? = null
    var onClosePage: (() -> Unit)? = null

    companion object {
        const val CUSTOM_USER_AGENT = "Vocai/1.0"
        const val TAG_CHOOSER = "chooser"
        const val LOADING_TIMEOUT_MS = 15000L // 15秒超时
    }

    fun bind(webView: WebView) {
        this.mWebView = webView
        setUpWebSettings()
        setupWebClient()
        setupWebChromeClient()
        setupDownloadListener()
        val url = Vocai.getInstance().buildUrl()
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
        val javascript =
            "javascript:setTimeout(function() {${getLoadingMethodByType(FILE_TYPE_ERROR)}('UploadError','${randomId}','${message}') }, 300)"
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

        mWebView.post {
            mWebView.loadUrl(javascript)
        }
    }

    fun saveChatId() {
        val jsCode = "javascript:getChatId()"

        mWebView.postDelayed( {
            mWebView.evaluateJavascript(jsCode) { result ->
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

    @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt")
    private fun setUpWebSettings() {
        mWebView.settings.apply {
            WebView.setWebContentsDebuggingEnabled(false)
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            domStorageEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            userAgentString = "${this.userAgentString} $CUSTOM_USER_AGENT"

            // 支持加载外部链接的额外设置
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowFileAccess = true
            allowContentAccess = true
            databaseEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            loadsImagesAutomatically = true
            mediaPlaybackRequiresUserGesture = false

            blockNetworkImage = false
            blockNetworkLoads = false
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportMultipleWindows(true)
        }
    }

    private fun setupWebClient() {
        mWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                
                // 处理特殊 scheme (tel:, mailto:, sms: 等)
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        view?.context?.startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        // 如果无法处理，让 WebView 尝试
                        return false
                    }
                }
                
                // 检查是否是 PDF 文件
                if (url.endsWith(".pdf", ignoreCase = true) || 
                    url.contains(".pdf?", ignoreCase = true) ||
                    url.contains(".pdf#", ignoreCase = true)) {
                    android.os.Handler(Looper.getMainLooper()).post {
                        val intent = Intent(mWebView.context, PdfActivity::class.java).apply {
                            this.putExtra("file", url)
                        }
                        mWebView.context.startActivity(intent)
                    }
                    return true
                }
                
                // 检查是否是外部链接，如果是则在外部浏览器中打开
                if (isExternalUrl(url)) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        view?.context?.startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        LogUtil.info("Failed to open external URL: ${e.message}")
                        // 如果无法打开，让 WebView 尝试
                        return false
                    }
                }
                
                // 其他 HTTP/HTTPS 链接在 WebView 中打开
                return false
            }
            
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) return false
                
                // 处理特殊 scheme
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        view?.context?.startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        return false
                    }
                }
                
                // 检查是否是 PDF 文件
                if (url.endsWith(".pdf", ignoreCase = true) || 
                    url.contains(".pdf?", ignoreCase = true) ||
                    url.contains(".pdf#", ignoreCase = true)) {
                    android.os.Handler(Looper.getMainLooper()).post {
                        val intent = Intent(mWebView.context, PdfActivity::class.java).apply {
                            this.putExtra("file", url)
                        }
                        mWebView.context.startActivity(intent)
                    }
                    return true
                }
                
                // 检查是否是外部链接，如果是则在外部浏览器中打开
                if (isExternalUrl(url)) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        view?.context?.startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        LogUtil.info("Failed to open external URL: ${e.message}")
                        // 如果无法打开，让 WebView 尝试
                        return false
                    }
                }
                
                return false
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onProgressUpdate?.invoke(0)
                startLoadingTimeout()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                cancelLoadingTimeout()
                onProgressUpdate?.invoke(100)
                
                // 注入 JavaScript 来设置默认使用后置摄像头
                injectCameraScript(view)
            }
            
            private fun injectCameraScript(view: WebView?) {
                val script = """
                    (function() {
                        // 重写 getUserMedia 以默认使用后置摄像头
                        const originalGetUserMedia = navigator.mediaDevices.getUserMedia.bind(navigator.mediaDevices);
                        
                        navigator.mediaDevices.getUserMedia = function(constraints) {
                            if (constraints && constraints.video) {
                                // 如果是 video 请求，设置默认为后置摄像头
                                if (typeof constraints.video === 'boolean') {
                                    constraints.video = {
                                        facingMode: { ideal: 'environment' }  // environment = 后置，user = 前置
                                    };
                                } else if (typeof constraints.video === 'object' && !constraints.video.facingMode) {
                                    constraints.video.facingMode = { ideal: 'environment' };
                                }
                            }
                            return originalGetUserMedia(constraints);
                        };
                    })();
                """.trimIndent()
                
                view?.evaluateJavascript(script, null)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    cancelLoadingTimeout()
                    onProgressUpdate?.invoke(100)
                }
            }
        }
    }

    private fun startLoadingTimeout() {
        cancelLoadingTimeout()
        loadingTimeoutRunnable = Runnable {
            onProgressUpdate?.invoke(100)
        }
        loadingTimeoutHandler.postDelayed(loadingTimeoutRunnable!!, LOADING_TIMEOUT_MS)
    }

    private fun cancelLoadingTimeout() {
        loadingTimeoutRunnable?.let {
            loadingTimeoutHandler.removeCallbacks(it)
            loadingTimeoutRunnable = null
        }
    }

    private fun setupWebChromeClient() {
        mWebView.webChromeClient = object : WebChromeClient() {

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress == 100) {
                    cancelLoadingTimeout()
                    onProgressUpdate?.invoke(100)
                }
            }
            
            // 视频全屏播放支持
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    onHideCustomView()
                    return
                }
                
                customView = view
                customViewCallback = callback
                
                // 保存原始的 UI visibility
                originalSystemUiVisibility = mWebView.systemUiVisibility
                
                // 获取根视图（通常是 DecorView）
                val decorView = (mWebView.context as? android.app.Activity)?.window?.decorView as? FrameLayout
                
                // 隐藏 WebView，显示全屏视频
                mWebView.visibility = View.GONE
                
                // 将视频 View 添加到 DecorView
                decorView?.addView(customView, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ))
                
                // 设置全屏模式
                (mWebView.context as? android.app.Activity)?.window?.decorView?.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
                
                LogUtil.info("Video fullscreen mode enabled")
            }
            
            override fun onHideCustomView() {
                if (customView == null) {
                    return
                }
                
                // 移除全屏视频 View
                val decorView = (mWebView.context as? android.app.Activity)?.window?.decorView as? FrameLayout
                decorView?.removeView(customView)
                
                // 恢复 WebView 显示
                mWebView.visibility = View.VISIBLE
                
                // 恢复原始的 UI visibility
                (mWebView.context as? android.app.Activity)?.window?.decorView?.systemUiVisibility = originalSystemUiVisibility
                
                // 清理
                customViewCallback?.onCustomViewHidden()
                customView = null
                customViewCallback = null
                
                LogUtil.info("Video fullscreen mode disabled")
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                return super.onConsoleMessage(consoleMessage)
            }

            // 处理 target="_blank" 和 window.open() 的链接
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: android.os.Message?
            ): Boolean {
                val newWebView = WebView(view?.context ?: return false)
                
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString()
                        if (url != null) {
                            if (url.endsWith(".pdf", ignoreCase = true) || 
                                url.contains(".pdf?", ignoreCase = true) ||
                                url.contains(".pdf#", ignoreCase = true)) {
                                android.os.Handler(Looper.getMainLooper()).post {
                                    val intent = Intent(mWebView.context, PdfActivity::class.java).apply {
                                        this.putExtra("file", url)
                                    }
                                    mWebView.context.startActivity(intent)
                                }
                            } else if (isExternalUrl(url)) {
                                // 外部链接在外部浏览器中打开
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    mWebView.context.startActivity(intent)
                                } catch (e: Exception) {
                                    LogUtil.info("Failed to open external URL: ${e.message}")
                                    // 如果无法打开，在主WebView中加载
                                    mWebView.post {
                                        mWebView.loadUrl(url)
                                    }
                                }
                            } else {
                                // 内部链接在主WebView中加载
                                mWebView.post {
                                    mWebView.loadUrl(url)
                                }
                            }
                        }
                        return true
                    }

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        if (url != null) {
                            if (url.endsWith(".pdf", ignoreCase = true) || 
                                url.contains(".pdf?", ignoreCase = true) ||
                                url.contains(".pdf#", ignoreCase = true)) {
                                android.os.Handler(Looper.getMainLooper()).post {
                                    val intent = Intent(mWebView.context, PdfActivity::class.java).apply {
                                        this.putExtra("file", url)
                                    }
                                    mWebView.context.startActivity(intent)
                                }
                            } else if (isExternalUrl(url)) {
                                // 外部链接在外部浏览器中打开
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    mWebView.context.startActivity(intent)
                                } catch (e: Exception) {
                                    LogUtil.info("Failed to open external URL: ${e.message}")
                                    // 如果无法打开，在主WebView中加载
                                    mWebView.post {
                                        mWebView.loadUrl(url)
                                    }
                                }
                            } else {
                                // 内部链接在主WebView中加载
                                mWebView.post {
                                    mWebView.loadUrl(url)
                                }
                            }
                        }
                        return true
                    }
                }

                val transport = resultMsg?.obj as? android.webkit.WebView.WebViewTransport
                transport?.webView = newWebView
                resultMsg?.sendToTarget()

                return true
            }

        }
    }
    
    /**
     * 设置下载监听器，支持视频下载
     */
    private fun setupDownloadListener() {
        mWebView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse(url)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                mWebView.context.startActivity(intent)
            } catch (e: Exception) {
                LogUtil.info("Download error: ${e.message}")
            }
        }
    }

    /**
     * 检查URL是否是外部链接（不是应用内部域名）
     */
    private fun isExternalUrl(url: String): Boolean {
        try {
            val uri = android.net.Uri.parse(url)
            val host = uri.host ?: return false
            
            // 获取应用的基础URL
            val baseUrl = Vocai.getInstance().wrapper.buildUrl()
            val baseUri = android.net.Uri.parse(baseUrl)
            val baseHost = baseUri.host ?: return false
            
            // 检查是否是外部域名（不是基础域名的子域名）
            // 例如：apps.voc.ai 和 www.voc.ai 是内部域名
            // 而 www.plaud.ai 是外部域名
            val baseDomain = extractBaseDomain(baseHost)
            val urlDomain = extractBaseDomain(host)
            
            return baseDomain != urlDomain
        } catch (e: Exception) {
            LogUtil.info("Error checking external URL: ${e.message}")
            // 如果解析失败，默认认为是外部链接，在外部浏览器中打开
            return true
        }
    }
    
    /**
     * 提取基础域名（例如：从 www.plaud.ai 提取 plaud.ai）
     */
    private fun extractBaseDomain(host: String): String {
        val parts = host.split(".")
        return when {
            parts.size >= 2 -> {
                // 处理类似 apps.voc.ai 的情况，返回 voc.ai
                // 或者 www.plaud.ai 返回 plaud.ai
                parts.takeLast(2).joinToString(".")
            }
            else -> host
        }
    }


}