package com.vocai.sdk.network

import android.webkit.MimeTypeMap
import com.vocai.sdk.LogUtil
import com.vocai.sdk.WebComponentService
import com.vocai.sdk.model.CheckStateResponse
import com.vocai.sdk.model.FileUploadResponse
import com.vocai.sdk.model.NavigateMessage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class WebComponentRepository {

    private val webComponentService: WebComponentService = WebComponentService()

    suspend fun uploadFile(message: NavigateMessage, file: File): FileUploadResponse {
        val mimeType = getMimeTypePerFileName(fileName = file.name)
        LogUtil.info("upload file:$mimeType")
        val body = file.asRequestBody(mimeType?.toMediaType())
        val builder = MultipartBody.Builder()
        builder.addFormDataPart("botId", message.data?.botId.toString())
        builder.addFormDataPart("chatId", message.data?.chatId.orEmpty())
        builder.addFormDataPart("contact", message.data?.contact.orEmpty())
        builder.addPart(MultipartBody.Part.createFormData("file", file.name, body))
        return webComponentService.uploadPicture(builder.build())
    }

    private fun getMimeTypePerFileName(fileName: String): String? {
        val extension = fileName.substring(fileName.lastIndexOf(".") + 1).lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    suspend fun checkState(jobId: String): CheckStateResponse {
        return webComponentService.checkState(jobId)
    }

}