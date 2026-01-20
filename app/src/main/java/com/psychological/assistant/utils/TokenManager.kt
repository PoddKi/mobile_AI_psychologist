package com.psychological.assistant.utils

import android.content.Context
import android.util.Log
import com.psychological.assistant.api.GigaChatClient
import com.psychological.assistant.data.model.GigaChatTokenResponse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Менеджер для управления токенами GigaChat API
 * Автоматически обновляет access token при истечении
 */
class TokenManager(private val context: Context) {
    
    private val preferencesHelper = PreferencesHelper(context)
    private val mutex = Mutex()
    
    companion object {
        private const val TAG = "TokenManager"
        // Запас времени перед истечением токена (5 минут)
        private const val TOKEN_REFRESH_BUFFER_MS = 5 * 60 * 1000L
        // Время жизни токена по умолчанию (30 минут)
        private const val DEFAULT_TOKEN_EXPIRY_MS = 30 * 60 * 1000L
    }
    
    /**
     * Получает валидный access token
     * Автоматически обновляет токен, если он истек или скоро истечет
     */
    suspend fun getValidAccessToken(): String? {
        return mutex.withLock {
            val authToken = getAuthorizationToken() ?: run {
                Log.e(TAG, "Authorization token not found")
                return null
            }
            
            val currentAccessToken = preferencesHelper.accessToken
            val tokenExpiryTime = preferencesHelper.accessTokenExpiryTime
            
            // Проверяем, нужно ли обновить токен
            val needsRefresh = currentAccessToken == null || 
                    tokenExpiryTime == null || 
                    System.currentTimeMillis() >= (tokenExpiryTime - TOKEN_REFRESH_BUFFER_MS)
            
            if (needsRefresh) {
                Log.d(TAG, "Token needs refresh. Current token: ${currentAccessToken?.take(20)}...")
                refreshAccessToken(authToken)
            } else {
                Log.d(TAG, "Using existing valid token")
            }
            
            preferencesHelper.accessToken
        }
    }
    
    /**
     * Обновляет access token используя authorization token
     */
    private suspend fun refreshAccessToken(authorizationToken: String): Boolean {
        return try {
            Log.d(TAG, "Refreshing access token...")
            
            val rqUID = UUID.randomUUID().toString()
            val authHeader = "Bearer $authorizationToken"
            
            val response = GigaChatClient.getOAuthApi().getAccessToken(
                authorization = authHeader,
                rqUID = rqUID,
                scope = "GIGACHAT_API_PERS"
            )
            
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                val accessToken = tokenResponse.accessToken
                
                // Вычисляем время истечения
                val expiresAt = if (tokenResponse.expiresAt != null) {
                    tokenResponse.expiresAt * 1000 // конвертируем секунды в миллисекунды
                } else if (tokenResponse.expiresIn != null) {
                    System.currentTimeMillis() + (tokenResponse.expiresIn * 1000L)
                } else {
                    // Если время не указано, используем дефолтное (30 минут)
                    System.currentTimeMillis() + DEFAULT_TOKEN_EXPIRY_MS
                }
                
                // Сохраняем новый токен
                preferencesHelper.accessToken = accessToken
                preferencesHelper.accessTokenExpiryTime = expiresAt
                
                Log.d(TAG, "Token refreshed successfully. Expires at: ${java.util.Date(expiresAt)}")
                true
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to refresh token. Code: ${response.code()}, Body: $errorBody")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while refreshing token", e)
            false
        }
    }
    
    /**
     * Получает authorization token (долгоживущий токен для получения access token)
     */
    private fun getAuthorizationToken(): String? {
        // Сначала проверяем из ApiConfig
        val configToken = com.psychological.assistant.config.ApiConfig.DEFAULT_AUTHORIZATION_TOKEN
        if (configToken.isNotEmpty()) {
            return configToken
        }
        
        // Затем из SharedPreferences
        return preferencesHelper.authorizationToken
    }
    
    /**
     * Устанавливает authorization token
     */
    fun setAuthorizationToken(token: String) {
        preferencesHelper.authorizationToken = token
        // Сбрасываем access token, чтобы получить новый
        preferencesHelper.accessToken = null
        preferencesHelper.accessTokenExpiryTime = null
    }
    
    /**
     * Принудительно обновляет токен
     */
    suspend fun forceRefresh(): Boolean {
        val authToken = getAuthorizationToken() ?: return false
        return mutex.withLock {
            refreshAccessToken(authToken)
        }
    }
}
