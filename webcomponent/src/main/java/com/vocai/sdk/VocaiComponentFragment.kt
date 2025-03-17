package com.vocai.sdk

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.vocai.sdk.core.WebComponentHelper
import com.vocai.sdk.model.StateEvent
import com.vocai.sdk.viewmodel.WebComponentViewModel
import java.io.File


internal class VocaiComponentFragment : Fragment() {

    private lateinit var viewModel: WebComponentViewModel

    private val componentHelper = WebComponentHelper()

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
        viewModel = ViewModelProvider(this).get(WebComponentViewModel::class.java)
        componentHelper.bind(view.findViewById<WebView>(R.id.mWebView))
        componentHelper.onFileChosen = { msg, filePath, type, fileName ->
            viewModel.uploadFile(msg, File(filePath), type, fileName)
        }
        componentHelper.attachToPage(childFragmentManager)
        initObserver()
//        askNotificationPermission()
    }

    fun handleBackPress():Boolean {
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