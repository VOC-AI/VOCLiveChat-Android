package com.vocai.sdk

import android.content.Context
import android.graphics.pdf.PdfRenderer
import com.vocai.sdk.core.ConfigLoader

class LanguageHelper(val context: Context) {

    lateinit var stringsMapper:HashMap<String,String>

    init {
        ConfigLoader().loadConfig(context)
    }

    fun getString(key:String):String {
        return key
    }

}