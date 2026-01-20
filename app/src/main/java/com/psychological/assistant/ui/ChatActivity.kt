package com.psychological.assistant.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.psychological.assistant.R
import com.psychological.assistant.data.model.ChatMessage
import com.psychological.assistant.databinding.ActivityChatBinding
import com.psychological.assistant.repository.ChatRepository
import com.psychological.assistant.utils.PreferencesHelper
import com.psychological.assistant.utils.ThemeManager
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChatBinding
    private var chatRepository: ChatRepository? = null
    private lateinit var messagesAdapter: MessagesAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var preferencesHelper: PreferencesHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Скрываем ActionBar для чистого вида
        supportActionBar?.hide()
        
        // Настраиваем заголовок
        binding.header.tvTitle.text = "AI Чат"
        binding.header.btnBack.setOnClickListener {
            finish()
        }
        
        preferencesHelper = PreferencesHelper(this)

        setupRecyclerView()
        binding.btnSend.isEnabled = false
        
        // Убеждаемся, что поле ввода доступно с самого начала
        binding.tilMessage.isEnabled = true
        binding.etMessage.isEnabled = true
        binding.etMessage.isFocusable = true
        binding.etMessage.isFocusableInTouchMode = true
        binding.etMessage.isClickable = true
        
        // Явно устанавливаем цвет текста для поля ввода (чтобы переопределить тему)
        binding.etMessage.setTextColor(getColor(R.color.text_primary))
        binding.etMessage.setHintTextColor(getColor(R.color.text_hint))
        
        // Настраиваем цвета контура для TextInputLayout
        // Цвет контура устанавливается автоматически через theme (colorPrimary при фокусе)
        // Если нужно изменить програмно, можно использовать setBoxStrokeColor()
        
        ensureTokenAndInit()
    }
    
    private fun ensureTokenAndInit() {
        // Проверяем наличие authorization token
        val tokenManager = com.psychological.assistant.utils.TokenManager(this)
        val authToken = com.psychological.assistant.config.ApiConfig.DEFAULT_AUTHORIZATION_TOKEN
        
        if (authToken.isEmpty() && preferencesHelper.authorizationToken.isNullOrBlank()) {
            showTokenDialog()
        } else {
            // Если authorization token есть в ApiConfig, устанавливаем его
            if (authToken.isNotEmpty()) {
                tokenManager.setAuthorizationToken(authToken)
            }
            initializeChat()
        }
    }
    
    private fun initializeChat() {
        chatRepository = ChatRepository(this, null) // Используем дефолтный системный промпт
        binding.btnSend.isEnabled = true
        
        // Убеждаемся, что поле ввода доступно
        binding.tilMessage.isEnabled = true
        binding.etMessage.isEnabled = true
        binding.etMessage.isFocusable = true
        binding.etMessage.isFocusableInTouchMode = true
        binding.etMessage.isClickable = true
        
        // Явно устанавливаем цвет текста (чтобы переопределить тему)
        binding.etMessage.setTextColor(getColor(R.color.text_primary))
        binding.etMessage.setHintTextColor(getColor(R.color.text_hint))
        
        // Пытаемся установить фокус на поле ввода
        binding.etMessage.post {
            binding.etMessage.requestFocus()
        }
        
        setupClickListeners()
    }
    
    private fun showTokenDialog() {
        val inputLayout = TextInputLayout(this).apply {
            hint = "Введите authorization token (base64)"
            setPadding(24, 16, 24, 0)
        }
        val input = TextInputEditText(inputLayout.context)
        inputLayout.addView(input)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Авторизация GigaChat")
            .setMessage("Введите authorization token для получения access token. Этот токен используется для автоматического обновления access token.")
            .setView(inputLayout)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val token = input.text?.toString()?.trim().orEmpty()
                if (token.isNotEmpty()) {
                    val tokenManager = com.psychological.assistant.utils.TokenManager(this)
                    tokenManager.setAuthorizationToken(token)
                    Toast.makeText(this, "Токен сохранен", Toast.LENGTH_SHORT).show()
                    initializeChat()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Токен не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter(messages)
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messagesAdapter
            // Плавная прокрутка
            overScrollMode = View.OVER_SCROLL_NEVER
            // Улучшенная производительность
            setHasFixedSize(false)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessage.text?.toString()?.trim()
            if (!messageText.isNullOrEmpty()) {
                sendMessage(messageText)
                binding.etMessage.text?.clear()
            }
        }
    }
    
    private fun sendMessage(messageText: String) {
        val repository = chatRepository ?: return
        
        val userMessage = ChatMessage(content = messageText, isUser = true)
        messages.add(userMessage)
        messagesAdapter.notifyItemInserted(messages.size - 1)
        binding.recyclerViewMessages.smoothScrollToPosition(messages.size - 1)
        
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSend.isEnabled = false
        
        lifecycleScope.launch {
            val result = repository.sendMessage(messageText)
            
            if (result.isSuccess) {
                val aiMessage = ChatMessage(
                    content = result.getOrNull() ?: "Ошибка получения ответа",
                    isUser = false
                )
                messages.add(aiMessage)
                messagesAdapter.notifyItemInserted(messages.size - 1)
                binding.recyclerViewMessages.smoothScrollToPosition(messages.size - 1)
            } else {
                val errorMessage = result.exceptionOrNull()?.message
                Toast.makeText(
                    this@ChatActivity,
                    errorMessage ?: getString(R.string.error_api),
                    Toast.LENGTH_SHORT
                ).show()
                messages.removeLastOrNull()
                messagesAdapter.notifyDataSetChanged()
            }
            
            binding.progressBar.visibility = View.GONE
            binding.btnSend.isEnabled = true
        }
    }
}