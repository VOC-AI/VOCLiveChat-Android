package com.vocai.sdk

import android.util.Log

object LogUtil {

    const val TAG = "VocalWebComponent"

    fun info(msg: String) {
        if (Vocai.getInstance().isDebug()) {
            Log.i(TAG, msg)
        }
    }

}