package com.psychological.assistant.utils

import android.content.Context
import android.content.SharedPreferences
import com.psychological.assistant.config.ApiConfig

class PreferencesHelper(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "psychological_assistant_prefs",
        Context.MODE_PRIVATE
    )
    
    /**
     * Получает токен GigaChat API (устаревший метод, используется для обратной совместимости)
     * @deprecated Используйте TokenManager для получения валидного access token
     */
    @Deprecated("Use TokenManager.getValidAccessToken() instead")
    var gigaChatToken: String?
        get() {
            try {
                // Сначала проверяем токен из кода
                val defaultToken = ApiConfig.DEFAULT_GIGACHAT_TOKEN
                if (defaultToken.isNotEmpty()) {
                    android.util.Log.d("PreferencesHelper", "Using token from ApiConfig")
                    return defaultToken
                }
                
                // Если токена в коде нет, берем из SharedPreferences
                val prefsToken = prefs.getString(KEY_GIGACHAT_TOKEN, null)
                if (!prefsToken.isNullOrBlank()) {
                    android.util.Log.d("PreferencesHelper", "Using token from SharedPreferences")
                    return prefsToken
                }
                
                android.util.Log.d("PreferencesHelper", "No token found")
                return null
            } catch (e: Exception) {
                android.util.Log.e("PreferencesHelper", "Error getting token", e)
                return prefs.getString(KEY_GIGACHAT_TOKEN, null)
            }
        }
        set(value) = prefs.edit().putString(KEY_GIGACHAT_TOKEN, value).apply()
    
    /**
     * Authorization token (долгоживущий токен для получения access token)
     */
    var authorizationToken: String?
        get() = prefs.getString(KEY_AUTHORIZATION_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_AUTHORIZATION_TOKEN, value).apply()
    
    /**
     * Access token (короткоживущий токен для API запросов)
     */
    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()
    
    /**
     * Время истечения access token (в миллисекундах)
     */
    var accessTokenExpiryTime: Long?
        get() {
            val time = prefs.getLong(KEY_ACCESS_TOKEN_EXPIRY, -1L)
            return if (time == -1L) null else time
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_ACCESS_TOKEN_EXPIRY).apply()
            } else {
                prefs.edit().putLong(KEY_ACCESS_TOKEN_EXPIRY, value).apply()
            }
        }
    
    companion object {
        private const val KEY_GIGACHAT_TOKEN = "gigachat_token"
        private const val KEY_AUTHORIZATION_TOKEN = "authorization_token"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_ACCESS_TOKEN_EXPIRY = "access_token_expiry"
    }
}