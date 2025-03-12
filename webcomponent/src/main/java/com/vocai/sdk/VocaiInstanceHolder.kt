package com.vocai.sdk

internal object VocaiInstanceHolder {

    private var INSTANCE: Vocai? = null

    internal fun getInstance(): Vocai {
        if (INSTANCE == null) INSTANCE = Vocai()
        return INSTANCE!!
    }

    internal fun reset() {
        INSTANCE = null
    }

}