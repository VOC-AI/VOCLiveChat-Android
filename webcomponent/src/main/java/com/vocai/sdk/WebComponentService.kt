package com.vocai.sdk

import com.vocai.sdk.model.CheckStateResponse
import com.vocai.sdk.model.FileUploadResponse
import com.vocai.sdk.network.NetworkClientProvider
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

class WebComponentService {

    private val networkService =
        NetworkClientProvider().provide().create(WebComponentApi::class.java)

    suspend fun uploadPicture(@Body body: RequestBody): FileUploadResponse =
        networkService.uploadPicture(body)

    suspend fun checkState(id: String): CheckStateResponse = networkService.checkState(id)

}

private interface WebComponentApi {

    @POST("api_v2/intelli/resource/upload/lead")
    suspend fun uploadPicture(@Body body: RequestBody): FileUploadResponse

    @GET("api_v2/intelli/resource/status")
    suspend fun checkState(@Query("jobId") id: String): CheckStateResponse


}