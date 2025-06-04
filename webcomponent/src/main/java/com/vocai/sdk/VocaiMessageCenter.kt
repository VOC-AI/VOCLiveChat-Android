package com.vocai.sdk

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

class VocaiMessageCenter private constructor() {

    // 用于管理协程生命周期
    private var pollingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 存储当前未读消息状态
    private val _hasUnreadFlow = MutableStateFlow(false)
    val hasUnreadFlow: StateFlow<Boolean> = _hasUnreadFlow

    // 存储订阅者回调
    private val subscribers = mutableListOf<(Boolean) -> Unit>()

    private val json = Json { ignoreUnknownKeys = true }  // 可配置

    // Retrofit 服务接口
    private val botApiService: BotApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://apps.voc.ai/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(BotApiService::class.java)
    }

    // 单例实现
    companion object {
        val instance: VocaiMessageCenter by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            VocaiMessageCenter()
        }
    }

    // 启动轮询
    fun startPolling(botId: String, userId:String, intervalMillis: Long = 10000) {
        if (pollingJob?.isActive == true) return

        pollingJob = scope.launch {
            while (isActive) {
                try {
                    val request = UnreadRequest(userId = userId)

                    val response = botApiService.checkUnread(botId, request)

                    LogUtil.info("response:$response")

                    val newState = response.hasUnread

                    // 检查状态是否变化
                    if (newState != _hasUnreadFlow.value) {
                        _hasUnreadFlow.value = newState
                        notifySubscribers(newState)
                    }
                } catch (e: Exception) {
                    e.message?.let { LogUtil.info(it) };
                }
                delay(intervalMillis)
            }
        }
    }

    // 停止轮询
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // 订阅状态变化
    fun subscribe(callback: (Boolean) -> Unit) {
        subscribers.add(callback)
        // 立即通知当前状态
        callback(_hasUnreadFlow.value)
    }

    // 取消订阅
    fun unsubscribe(callback: (Boolean) -> Unit) {
        subscribers.remove(callback)
    }

    // 通知所有订阅者
    private fun notifySubscribers(hasUnread: Boolean) {
        subscribers.forEach { it(hasUnread) }
    }

    // Retrofit API 接口
    interface BotApiService {

        @POST("api_v2/intelli/livechat/{botId}/unread")
        suspend fun checkUnread(@Path("botId") botId: String, @Body request: UnreadRequest): UnreadResponse

    }

    @Serializable
    data class UnreadResponse(

        val hasUnread: Boolean

    )

    @Serializable
    data class UnreadRequest (

        val userId: String

    )

}