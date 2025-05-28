package com.vocai.sdk.extension

import com.vocai.sdk.Vocai


fun String.toi18nString():String {

    return Vocai.getInstance().wrapper.strings.getOrElse(this) {
        this
    }
}