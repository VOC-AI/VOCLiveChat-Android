package com.vocai.sdk.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.vocai.sdk.Vocai
import com.vocai.sdk.model.ComponentConfiguration
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

internal class NetworkClientProvider {

    private var configuration: ComponentConfiguration? = null
    private var json = Json {
        ignoreUnknownKeys = true
    }

    fun provide(): Retrofit {
        return Retrofit.Builder().baseUrl("https://apps.voc.ai").client(createHttpClient()).addConverterFactory(
            json.asConverterFactory("application/json".toMediaType()),
        ).build()
    }

    init {
        configuration = Vocai.getInstance().wrapper.config
    }

    private fun createHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().callTimeout(getTimeoutTime(),TimeUnit.MILLISECONDS)
            .addInterceptor(
                HttpLoggingInterceptor()
                .apply {
                    setLevel(HttpLoggingInterceptor.Level.BODY)
                })
            .retryOnConnectionFailure(isRetryOnFailure()).build()
    }

    fun isRetryOnFailure(): Boolean = configuration?.networkConfig?.retryOnFailure ?: false

    fun getTimeoutTime(): Long = configuration?.networkConfig?.timeoutTime ?: 0L

}