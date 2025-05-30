package com.vocai.sdk

import android.content.Context
import com.vocai.sdk.core.ConfigLoader

fun fixLang(str: String?): String? {
    if (str == null) return null
    val languageMap = mapOf(
        "ko" to "ko-KR",
        "zh-TW" to "zh-HK",
        "zh-Hans" to "zh-CN",
        "zh-Hant" to "zh-HK",
        "zh-Hant-HK" to "zh-HK",
        "zh-Hant-TW" to "zh-HK"
    )
    return languageMap[str]
}

fun fullyNormalizeLanguageCode(languageCode: String?): String? {
    if (languageCode.isNullOrEmpty()) return languageCode

    // 标准化标识符（使用 Java Locale）
    val parts = languageCode.split("-", "_")
    val locale = when (parts.size) {
        1 -> java.util.Locale(parts[0])
        2 -> java.util.Locale(parts[0], parts[1])
        else -> java.util.Locale(parts[0], parts[1], parts.drop(2).joinToString("_"))
    }

    var standardized = locale.toLanguageTag()

    // 替换下划线为连字符
    standardized = standardized.replace(Regex("_"), "-")

    // 尝试修复语言代码
    val fixed = fixLang(standardized)
    if (fixed != null) return fixed

    // 提取语言部分
    val firstComponent = standardized.split("-").firstOrNull() ?: return standardized

    // 再次标准化语言部分
    val secondLocale = java.util.Locale(firstComponent)
    val secondStandardized = secondLocale.toLanguageTag()
    val secondFixed = fixLang(secondStandardized)

    return secondFixed ?: secondStandardized
}


class LanguageHelper(val context: Context) {

    lateinit var stringsMapper:HashMap<String,String>

    init {
        ConfigLoader().loadConfig(context)
    }

    fun getString(key:String):String {
        return key
    }



    companion object {
        public fun normalizeLanguage(langCode: String): String {
            val ret = fullyNormalizeLanguageCode(langCode);
            if (ret == null) {
                return langCode
            }
            return ret as String
        }
    }

}