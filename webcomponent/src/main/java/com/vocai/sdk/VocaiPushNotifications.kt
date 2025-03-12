package com.vocai.sdk

import android.content.Context

/**
 * this class not used yet. should be complete in next version.
 */
internal object VocaiPushNotifications {

    private var pushToken: String? = null

    fun updateToken(newToken: String) {
        pushToken = newToken
        LogUtil.info("new token updated:$newToken")
    }

    internal fun showNotification(context: Context, title: String, content: String) {}

}