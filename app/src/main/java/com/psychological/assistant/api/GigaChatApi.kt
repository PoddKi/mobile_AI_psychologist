package com.psychological.assistant.api

import com.psychological.assistant.data.model.GigaChatRequest
import com.psychological.assistant.data.model.GigaChatResponse
import com.psychological.assistant.data.model.GigaChatTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GigaChatApi {
    
    @POST("v1/chat/completions")
    suspend fun sendMessage(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: GigaChatRequest
    ): Response<GigaChatResponse>
    
    /**
     * Получение access token через OAuth
     * Используется отдельный базовый URL: https://ngw.devices.sberbank.ru:9443/api/v2/oauth
     */
    @retrofit2.http.FormUrlEncoded
    @POST("v2/oauth")
    suspend fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Header("RqUID") rqUID: String,
        @retrofit2.http.Field("scope") scope: String = "GIGACHAT_API_PERS"
    ): Response<GigaChatTokenResponse>
}