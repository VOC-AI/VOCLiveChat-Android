package com.vocai.sdk

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.vocai.sdk.core.WebComponentHelper
import com.vocai.sdk.model.ComponentConfiguration
import com.vocai.sdk.model.FILE_TYPE_ERROR
import com.vocai.sdk.model.StateEvent
import com.vocai.sdk.viewmodel.WebComponentViewModel
import com.vocai.sdk.widget.LoadingDialogFragment
import java.io.File


internal class VocaiComponentFragment : Fragment() {

    private var configuration: ComponentConfiguration? = null
    private lateinit var viewModel: WebComponentViewModel

    private val componentHelper = WebComponentHelper()
    private var mLoadingIv: ImageView? = null
    private var mLoadingLayout: FrameLayout? = null

    init {
        configuration = Vocai.getInstance().wrapper.config
    }


    private fun showLoading() {
        mLoadingLayout?.visibility = View.VISIBLE
//        mLoadingIv?.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        mLoadingLayout?.visibility = View.GONE
//        mLoadingIv?.visibility = View.GONE
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_web_component, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mLoadingLayout = view.findViewById(R.id.mProgressLayout)
//        mLoadingIv = view.findViewById(R.id.mProgressIv)
//        mLoadingIv?.let {
//            Glide.with(requireContext()).asGif().load(R.raw.loading).into(it)
//        }
        viewModel = ViewModelProvider(this).get(WebComponentViewModel::class.java)
        componentHelper.onProgressUpdate = {
            Handler(Looper.getMainLooper()).post {
                if (it == 0) {
                    showLoading()
                } else if (it == 100) {
                    hideLoading()
                }
            }
        }
        componentHelper.bind(view.findViewById<WebView>(R.id.mWebView))
        componentHelper.onFileChosen = { msg, filePath, type, fileName ->
            val file = File(filePath)
            viewModel.uploadFile(msg, file, type, fileName)
        }
        componentHelper.attachToPage(childFragmentManager)
        initObserver()
    }

    fun handleBackPress(): Boolean {
        return componentHelper.handleBackPress()
    }

    private fun initObserver() {
        viewModel.getStateLiveData().observe(viewLifecycleOwner) {
            when (it) {
                is StateEvent.UploadSuccess -> {
                    componentHelper.postResultToWebView(
                        it.url,
                        it.fileType,
                        it.randomId,
                        it.fileName
                    )
                }

                is StateEvent.StartUpload -> {
                    componentHelper.postLoadingStateToWebView(it.fileType, it.randomId, it.fileName)
                }

                is StateEvent.Error -> {
                    LogUtil.info("get error for some action. ${it.randomId} ${it.throwable}")
                    componentHelper.handleFileError(it.throwable.message.toString(), it.randomId)
                    LogUtil.info(Log.getStackTraceString(it.throwable))
                }

                else -> {
                    // nothing need to handle.
                }
            }
        }
    }


    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}