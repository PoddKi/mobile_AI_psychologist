package com.psychological.assistant.data.model

import com.google.gson.annotations.SerializedName

/**
 * Модель ответа от OAuth API при получении access token
 */
data class GigaChatTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("expires_at")
    val expiresAt: Long? = null,
    
    @SerializedName("expires_in")
    val expiresIn: Int? = null
)
