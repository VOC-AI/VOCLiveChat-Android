package com.vocai.sdk.permission

import android.Manifest
import android.os.Build
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi

class StoragePermissionHandler(
    val caller: ActivityResultCaller
) {

    private var launcher: ActivityResultLauncher<Array<String>>

    init {
        launcher =
            caller.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

            }
    }

    fun requestGalleryPermission() {
        launcher.launch(getGalleryPermissionCompat())
    }

    fun requestCameraPermission() {

    }

    private fun getGalleryPermissionCompat(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 使用系统选择器，不需要权限
            emptyArray()
        } else {
            getGalleryPermission()
        }

    private fun getGalleryPermission(): Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

}