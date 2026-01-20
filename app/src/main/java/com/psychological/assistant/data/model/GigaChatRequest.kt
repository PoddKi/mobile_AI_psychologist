package com.psychological.assistant.data.model

import com.google.gson.annotations.SerializedName

data class GigaChatRequest(
    val model: String = "GigaChat",
    val messages: List<ChatMessageRequest>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 2000
)

data class ChatMessageRequest(
    val role: String, // "user" or "assistant" or "system"
    val content: String
)

data class GigaChatResponse(
    val choices: List<Choice>,
    val created: Long,
    val model: String,
    val usage: Usage?
)

data class Choice(
    val message: ChatMessageRequest,
    val index: Int,
    val finish_reason: String?
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)