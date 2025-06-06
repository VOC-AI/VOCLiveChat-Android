package com.vocai.sdk

import com.vocai.sdk.model.CheckStateResponse
import com.vocai.sdk.model.FileUploadResponse
import com.vocai.sdk.model.UnreadResponse
import com.vocai.sdk.model.UnreadRequest
import com.vocai.sdk.model.CommitUserRequest
import com.vocai.sdk.network.NetworkClientProvider
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

class WebComponentService {

    private val networkService =
        NetworkClientProvider().provide().create(WebComponentApi::class.java)

    suspend fun uploadPicture(@Body body: RequestBody): FileUploadResponse =
        networkService.uploadPicture(body)

    suspend fun checkState(id: String): CheckStateResponse = networkService.checkState(id)

    suspend fun checkUnread(botId: String, request: UnreadRequest): UnreadResponse = networkService.checkUnread(botId, request)

    fun commitUserId(request: CommitUserRequest): Call<String> = networkService.commitUserId(request)
}

private interface WebComponentApi {

    @POST("api_v2/intelli/resource/upload/lead")
    suspend fun uploadPicture(@Body body: RequestBody): FileUploadResponse

    @GET("api_v2/intelli/resource/status")
    suspend fun checkState(@Query("jobId") id: String): CheckStateResponse

    @POST("api_v2/intelli/livechat/{botId}/unread")
    suspend fun checkUnread(@Path("botId") botId: String, @Body request: UnreadRequest): UnreadResponse

    @POST("/api_v2/intelli/livechat/commit")
     fun commitUserId(@Body request: CommitUserRequest): Call<String>

}