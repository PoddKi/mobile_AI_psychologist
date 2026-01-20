package com.psychological.assistant.repository

import android.content.Context
import com.psychological.assistant.api.GigaChatClient
import com.psychological.assistant.data.model.ChatMessageRequest
import com.psychological.assistant.data.model.GigaChatResponse
import com.psychological.assistant.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatRepository(
    private val context: Context,
    private val systemPrompt: String? = null
) {
    
    private val tokenManager = TokenManager(context)
    private val _conversationHistory = MutableStateFlow<List<ChatMessageRequest>>(emptyList())
    val conversationHistory: StateFlow<List<ChatMessageRequest>> = _conversationHistory
    
    init {
        // Инициализация с системным сообщением
        val defaultSystemPrompt = """Ты психологический ассистент. Твоя задача - помогать пользователям с психологическими тестами, 
                |определением типа личности, уровнем стресса, анализом отношений, эмоционального интеллекта и подбором профессии. 
                |Будь дружелюбным, профессиональным и поддерживающим. Используй научный подход к психологии.""".trimMargin()
        
        val systemMessage = ChatMessageRequest(
            role = "system",
            content = systemPrompt ?: defaultSystemPrompt
        )
        _conversationHistory.value = listOf(systemMessage)
    }
    
    suspend fun sendMessage(userMessage: String): Result<String> {
        android.util.Log.d("ChatRepository", "=== ChatRepository.sendMessage ===")
        android.util.Log.d("ChatRepository", "User message: ${userMessage.take(100)}...")
        android.util.Log.d("ChatRepository", "Current history size: ${_conversationHistory.value.size}")
        
        // Получаем валидный access token (автоматически обновляется при необходимости)
        val accessToken = tokenManager.getValidAccessToken()
        if (accessToken == null) {
            android.util.Log.e("ChatRepository", "Failed to get access token")
            return Result.failure(Exception("Не удалось получить токен доступа. Проверьте authorization token."))
        }
        
        android.util.Log.d("ChatRepository", "Token length: ${accessToken.length}")
        
        val userMsg = ChatMessageRequest(role = "user", content = userMessage)
        val currentHistory = _conversationHistory.value.toMutableList()
        currentHistory.add(userMsg)
        
        android.util.Log.d("ChatRepository", "History before send (${currentHistory.size} messages):")
        currentHistory.forEachIndexed { index, msg ->
            android.util.Log.d("ChatRepository", "  [$index] ${msg.role}: ${msg.content.take(50)}...")
        }
        
        android.util.Log.d("ChatRepository", "Calling GigaChatClient.sendMessage...")
        val result = GigaChatClient.sendMessage(accessToken, currentHistory)
        
        android.util.Log.d("ChatRepository", "Result isSuccess: ${result.isSuccess}")
        
        return if (result.isSuccess) {
            val response = result.getOrNull()
            if (response == null) {
                android.util.Log.e("ChatRepository", "ERROR: Empty response body")
                return Result.failure(Exception("Empty response"))
            }
            
            android.util.Log.d("ChatRepository", "Response received successfully")
            android.util.Log.d("ChatRepository", "Choices count: ${response.choices.size}")
            
            val assistantMessage = response.choices.firstOrNull()?.message
            if (assistantMessage == null) {
                android.util.Log.e("ChatRepository", "ERROR: No message in response choices")
                android.util.Log.e("ChatRepository", "Response choices: ${response.choices}")
                return Result.failure(Exception("No message in response"))
            }
            
            android.util.Log.d("ChatRepository", "Assistant message received: ${assistantMessage.content.take(200)}...")
            android.util.Log.d("ChatRepository", "Full message length: ${assistantMessage.content.length}")
            
            currentHistory.add(assistantMessage)
            _conversationHistory.value = currentHistory
            
            android.util.Log.d("ChatRepository", "=== SUCCESS ===")
            Result.success(assistantMessage.content)
        } else {
            val exception = result.exceptionOrNull()
            android.util.Log.e("ChatRepository", "=== ERROR ===")
            android.util.Log.e("ChatRepository", "Exception type: ${exception?.javaClass?.simpleName}")
            android.util.Log.e("ChatRepository", "Exception message: ${exception?.message}")
            if (exception != null) {
                android.util.Log.e("ChatRepository", "Stack trace:", exception)
            }
            android.util.Log.e("ChatRepository", "==============")
            Result.failure(exception ?: Exception("Unknown error"))
        }
    }
    
    fun clearHistory() {
        val defaultSystemPrompt = """Ты психологический ассистент. Твоя задача - помогать пользователям с психологическими тестами, 
                |определением типа личности, уровнем стресса, анализом отношений, эмоционального интеллекта и подбором профессии. 
                |Будь дружелюбным, профессиональным и поддерживающим. Используй научный подход к психологии.""".trimMargin()
        
        val systemMessage = ChatMessageRequest(
            role = "system",
            content = systemPrompt ?: defaultSystemPrompt
        )
        _conversationHistory.value = listOf(systemMessage)
    }
}