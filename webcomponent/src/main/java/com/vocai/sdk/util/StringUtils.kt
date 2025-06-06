package com.vocai.sdk.util

object StringUtils {


    fun isEmptyString(result: String?): Boolean {
        return when (result) {
            null -> true
            "null", "undefined" -> true
            "" -> true
            else -> {
                return false
            }
        }
    }
}