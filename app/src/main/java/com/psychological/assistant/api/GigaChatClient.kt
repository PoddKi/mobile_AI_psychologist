package com.psychological.assistant.api

import com.psychological.assistant.data.model.ChatMessageRequest
import com.psychological.assistant.data.model.GigaChatRequest
import com.psychological.assistant.data.model.GigaChatResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object GigaChatClient {
    
    private const val BASE_URL = "https://gigachat.devices.sberbank.ru/api/"
    private const val OAUTH_BASE_URL = "https://ngw.devices.sberbank.ru:9443/api/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // TrustManager, который принимает все сертификаты
    // ВНИМАНИЕ: Только для разработки! В продакшене используйте правильную валидацию сертификатов
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })
    
    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, trustAllCerts, SecureRandom())
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true } // Принимаем все хосты
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // Отдельный Retrofit для OAuth API (другой базовый URL)
    private val oauthRetrofit = Retrofit.Builder()
        .baseUrl(OAUTH_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: GigaChatApi = retrofit.create(GigaChatApi::class.java)
    val oauthApi: GigaChatApi = oauthRetrofit.create(GigaChatApi::class.java)
    
    /**
     * Получает OAuth API для запросов токенов
     */
    fun getOAuthApi(): GigaChatApi = oauthApi
    
    fun createAuthorizationHeader(accessToken: String): String {
        return "Bearer $accessToken"
    }
    
    suspend fun sendMessage(
        accessToken: String,
        messages: List<ChatMessageRequest>
    ): Result<GigaChatResponse> {
        return try {
            android.util.Log.d("GigaChatClient", "=== Sending message to GigaChat ===")
            android.util.Log.d("GigaChatClient", "Token prefix: ${accessToken.take(20)}...")
            android.util.Log.d("GigaChatClient", "Messages count: ${messages.size}")
            android.util.Log.d("GigaChatClient", "API URL: $BASE_URL")
            android.util.Log.d("GigaChatClient", "Messages: ${messages.joinToString("\n") { "${it.role}: ${it.content.take(100)}..." }}")
            
            val request = GigaChatRequest(
                messages = messages,
                temperature = 0.7,
                max_tokens = 2000
            )
            
            val authHeader = createAuthorizationHeader(accessToken)
            android.util.Log.d("GigaChatClient", "Authorization header prefix: ${authHeader.take(30)}...")
            
            android.util.Log.d("GigaChatClient", "Making API call...")
            val response = api.sendMessage(
                authorization = authHeader,
                request = request
            )
            
            android.util.Log.d("GigaChatClient", "Response received!")
            android.util.Log.d("GigaChatClient", "Response code: ${response.code()}")
            android.util.Log.d("GigaChatClient", "Response message: ${response.message()}")
            android.util.Log.d("GigaChatClient", "Is successful: ${response.isSuccessful}")
            android.util.Log.d("GigaChatClient", "Has body: ${response.body() != null}")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                android.util.Log.d("GigaChatClient", "Response successful!")
                android.util.Log.d("GigaChatClient", "Choices count: ${responseBody.choices.size}")
                if (responseBody.choices.isNotEmpty()) {
                    val firstChoice = responseBody.choices[0]
                    android.util.Log.d("GigaChatClient", "First choice message: ${firstChoice.message.content.take(200)}...")
                }
                Result.success(responseBody)
            } else {
                // Получаем детали ошибки из response body
                val errorBody = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    android.util.Log.e("GigaChatClient", "Error reading error body", e)
                    null
                }
                
                val errorMessage = if (errorBody != null) {
                    "API Error: ${response.code()} - ${response.message()}\nBody: $errorBody"
                } else {
                    "API Error: ${response.code()} - ${response.message()}"
                }
                
                android.util.Log.e("GigaChatClient", "=== API ERROR ===")
                android.util.Log.e("GigaChatClient", "Status code: ${response.code()}")
                android.util.Log.e("GigaChatClient", "Status message: ${response.message()}")
                android.util.Log.e("GigaChatClient", "Error body: $errorBody")
                android.util.Log.e("GigaChatClient", "==================")
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e("GigaChatClient", "=== EXCEPTION ===")
            android.util.Log.e("GigaChatClient", "Exception type: ${e.javaClass.simpleName}")
            android.util.Log.e("GigaChatClient", "Exception message: ${e.message}")
            android.util.Log.e("GigaChatClient", "Stack trace:", e)
            android.util.Log.e("GigaChatClient", "==================")
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
}