package com.vocai.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocai.sdk.LogUtil
import com.vocai.sdk.extension.toi18nString
import com.vocai.sdk.model.FILE_TYPE_ERROR
import com.vocai.sdk.model.NavigateMessage
import com.vocai.sdk.model.StateEvent
import com.vocai.sdk.network.WebComponentRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

class WebComponentViewModel() : ViewModel() {

    private val repository = WebComponentRepository()
    private val stateLiveData = MutableLiveData<StateEvent>()

    fun getStateLiveData(): MutableLiveData<StateEvent> = stateLiveData

    fun uploadFile(msg: NavigateMessage, file: File, type: Int, name: String? = null) {
        val randomId = System.nanoTime().toString()
        viewModelScope.launch(CoroutineExceptionHandler { _, e ->
            stateLiveData.postValue(StateEvent.Error(type, randomId, e))
        }) {
            stateLiveData.postValue(StateEvent.StartUpload(type, randomId, name))

            if (file.exists() && file.isFile) {
                val fileSizeInBytes = file.length()
                val fileSizeInMB = fileSizeInBytes / (1024 * 1024)
                val maxFileSize = 10 * 1024 * 1024
                val maxFileSizeInMB = maxFileSize /  (1024 * 1024)
                if (fileSizeInBytes > maxFileSize) {
                    val errText = "key_media_limit_exceed".toi18nString().replace("%@", "${maxFileSizeInMB}MB")
                    stateLiveData.postValue(StateEvent.Error(FILE_TYPE_ERROR, randomId, Throwable(errText)))
                    return@launch
                }
            }

            val result = async {
                repository.uploadFile(msg, file)
            }.await()
            LogUtil.info("upload $type result->$result")
            if (result.jobId.isNullOrEmpty()) {
                stateLiveData.postValue(
                    StateEvent.UploadSuccess(
                        randomId,
                        result.url.orEmpty(),
                        type, name
                    )
                )
            } else {
                //need start check state to get real file url.
                checkState(type, result.jobId, randomId, name, this)
            }
        }
    }

    private suspend fun checkState(
        type: Int,
        jobId: String,
        randomId: String,
        name: String? = null,
        scope: CoroutineScope
    ) {
        LogUtil.info("checkState $type jobId:$jobId randomId:$randomId")
        val result = scope.async {
            repository.checkState(jobId)
        }.await()
        LogUtil.info("checkState $type jobId:$jobId randomId:$randomId result:$result")
        if (!result.finished) {
            checkState(type, jobId, randomId, name, scope)
        } else {
            stateLiveData.postValue(
                StateEvent.UploadSuccess(
                    randomId,
                    result.url.orEmpty(),
                    type, name
                )
            )
        }
    }

}