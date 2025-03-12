package com.vocai.sdk.model

sealed class StateEvent {
    data class Error(val fileType: Int, val randomId: String, val throwable: Throwable) :
        StateEvent()

    data class StartUpload(val fileType: Int, val randomId: String,val fileName:String? = null) : StateEvent()

    data class UploadSuccess(val randomId: String, val url: String, val fileType: Int,val fileName:String? = null) :
        StateEvent()

}

internal const val FILE_TYPE_PIC = 1
internal const val FILE_TYPE_VIDEO = 2
internal const val FILE_TYPE_GALLERY = 3
internal const val FILE_TYPE_OTHER = 3