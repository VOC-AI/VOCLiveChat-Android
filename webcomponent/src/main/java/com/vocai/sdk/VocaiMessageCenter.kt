package com.vocai.sdk

import com.vocai.sdk.model.CommitUserRequest
import com.vocai.sdk.model.UnreadRequest
import com.vocai.sdk.util.StringUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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


    private val webComponentService: WebComponentService = WebComponentService()

    // 单例实现
    companion object {
        val instance: VocaiMessageCenter by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            VocaiMessageCenter()
        }
    }

    // 启动轮询， 如果userId发生变化，需要重新提交userId
    fun startPolling(botId: String, userId:String, intervalMillis: Long = 10000) {
        if (pollingJob?.isActive == true) return

        pollingJob = scope.launch {
            while (isActive) {
                try {
                    val request = UnreadRequest(userId = userId)
                    val response = webComponentService.checkUnread(botId, request)
                    val newState = response.hasUnread

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

    /**
     * 重新绑定会话的用户
     */
    fun bindLoginUser(botId: Long, userId: String) {
        val chatId = Vocai.getInstance().getChatId()
        if (StringUtils.isEmptyString(chatId)) {
            return
        }

        try {
            val request = CommitUserRequest(userId = userId, botId = botId, chatId = chatId.toString())
            val commitCall = webComponentService.commitUserId(request)
            commitCall.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        LogUtil.info("commit result:$result")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    t.message?.let { LogUtil.info(it) };
                }
            })

        } catch (e: Exception) {
            e.message?.let { LogUtil.info(it) };
        }
    }

    // 通知所有订阅者
    private fun notifySubscribers(hasUnread: Boolean) {
        subscribers.forEach { it(hasUnread) }
    }


}