package com.tomatoketchup.ori.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface MessageApi {
    @POST("v1/messages")
    suspend fun sendMessage(@Body body: Map<String, Any>): Response<Unit>

    @POST("v1/messages/ack")
    suspend fun ack(@Body body: Map<String, Any>): Response<Unit>
}

object NetworkClient {
    fun create(context: Context, baseUrl: String = "https://example.com/"): MessageApi {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BASIC

        val ok = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(ok)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return retrofit.create(MessageApi::class.java)
    }
}
