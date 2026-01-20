package com.psychological.assistant.ui

import android.content.Intent
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
import com.psychological.assistant.data.database.AppDatabase
import com.psychological.assistant.data.model.ChatMessage
import com.psychological.assistant.data.model.TestResult
import com.psychological.assistant.data.model.TestType
import com.psychological.assistant.databinding.ActivityTestQuestionsBinding
import com.psychological.assistant.repository.ChatRepository
import com.psychological.assistant.repository.TestRepository
import com.psychological.assistant.services.TestPromptsService
import com.psychological.assistant.utils.PreferencesHelper
import kotlinx.coroutines.launch

class TestQuestionsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTestQuestionsBinding
    private lateinit var testType: TestType
    private var chatRepository: ChatRepository? = null
    private lateinit var messagesAdapter: MessagesAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var testRepository: TestRepository
    private var questionCount = 0
    private var isWaitingForConclusion = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        com.psychological.assistant.utils.ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityTestQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        testType = TestType.valueOf(intent.getStringExtra("TEST_TYPE") ?: TestType.PERSONALITY_TYPE.name)
        
        val database = AppDatabase.getDatabase(applicationContext)
        testRepository = TestRepository(database.testResultDao())
        preferencesHelper = PreferencesHelper(this)
        
        // Скрываем ActionBar для чистого вида
        supportActionBar?.hide()
        
        // Настраиваем заголовок
        val title = when (testType) {
            TestType.PERSONALITY_TYPE -> "Тест на тип личности"
            TestType.STRESS_LEVEL -> "Тест на уровень стресса"
            TestType.RELATIONSHIPS -> "Анализ отношений"
            TestType.EMOTIONAL_INTELLIGENCE -> "Эмоциональный интеллект"
            TestType.PROFESSION -> "Определение профессии"
            TestType.STRESS_PROGRESSION -> "Прогрессия стресса"
            TestType.ADVICE -> "Попросить совета"
        }
        binding.header.tvTitle.text = title
        binding.header.btnBack.setOnClickListener {
            finish()
        }
        
        setupRecyclerView()
        binding.btnSend.isEnabled = false
        
        // Явно устанавливаем цвет текста для поля ввода (чтобы переопределить тему)
        binding.etAnswer.setTextColor(getColor(R.color.text_primary))
        binding.etAnswer.setHintTextColor(getColor(R.color.text_hint))
        
        ensureTokenAndStartTest()
    }
    
    private fun ensureTokenAndStartTest() {
        // Проверяем наличие authorization token
        val tokenManager = com.psychological.assistant.utils.TokenManager(this)
        val authToken = com.psychological.assistant.config.ApiConfig.DEFAULT_AUTHORIZATION_TOKEN
        
        android.util.Log.d("TestQuestionsActivity", "Token check: ${if (authToken.isEmpty() && preferencesHelper.authorizationToken.isNullOrBlank()) "empty" else "found"}")
        
        if (authToken.isEmpty() && preferencesHelper.authorizationToken.isNullOrBlank()) {
            // Если токен не задан ни в коде, ни в SharedPreferences, показываем диалог
            showTokenDialog()
        } else {
            // Если authorization token есть в ApiConfig, устанавливаем его
            if (authToken.isNotEmpty()) {
                tokenManager.setAuthorizationToken(authToken)
            }
            // Токен есть, сразу инициализируем
            android.util.Log.d("TestQuestionsActivity", "Initializing test")
            initializeTest()
        }
    }
    
    private fun initializeTest() {
        val systemPrompt = TestPromptsService.getSystemPrompt(testType)
        chatRepository = ChatRepository(this, systemPrompt)
        binding.btnSend.isEnabled = true
        setupClickListeners()
        startTest()
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
                    initializeTest()
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
            layoutManager = LinearLayoutManager(this@TestQuestionsActivity).apply {
                stackFromEnd = true
            }
            adapter = messagesAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val answerText = binding.etAnswer.text?.toString()?.trim()
            if (!answerText.isNullOrEmpty()) {
                sendAnswer(answerText)
                binding.etAnswer.text?.clear()
            }
        }
    }
    
    private fun startTest() {
        val repository = chatRepository ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSend.isEnabled = false
        
        lifecycleScope.launch {
            // Отправляем запрос на начало теста
            // Системный промпт уже установлен в ChatRepository и содержит инструкции
            android.util.Log.d("TestQuestionsActivity", "Starting test - sending initial message")
            val result = repository.sendMessage("Привет! Готов начать тест.")
            
            if (result.isSuccess) {
                val aiMessage = result.getOrNull() ?: "Ошибка получения ответа"
                android.util.Log.d("TestQuestionsActivity", "Received AI response: ${aiMessage.take(100)}...")
                addAIMessage(aiMessage)
                questionCount = 0 // Это первое сообщение от ИИ (приветствие и первый вопрос)
                binding.btnSend.isEnabled = true
            } else {
                val exception = result.exceptionOrNull()
                val errorMessage = exception?.message
                val fullError = "Ошибка API: ${errorMessage ?: getString(R.string.error_api)}"
                android.util.Log.e("TestQuestionsActivity", "API Error: $fullError", exception)
                Toast.makeText(
                    this@TestQuestionsActivity,
                    fullError,
                    Toast.LENGTH_LONG
                ).show()
                binding.btnSend.isEnabled = true
            }
            
            binding.progressBar.visibility = View.GONE
        }
    }
    
    private fun sendAnswer(answer: String) {
        val repository = chatRepository ?: return
        
        // Добавляем ответ пользователя
        addUserMessage(answer)
        questionCount++
        
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSend.isEnabled = false
        
        lifecycleScope.launch {
            val result = repository.sendMessage(answer)
            
            if (result.isSuccess) {
                val aiResponse = result.getOrNull() ?: "Ошибка получения ответа"
                
                // Проверяем, является ли ответ заключением или новым вопросом
                val isConclusion = checkIfConclusion(aiResponse)
                
                if (isConclusion || questionCount >= 7) {
                    // Это заключение или уже достаточно вопросов
                    addAIMessage(aiResponse)
                    if (!isConclusion && questionCount >= 7) {
                        // Если не заключение, но достаточно вопросов, запрашиваем его
                        requestConclusion(repository)
                    } else {
                        finishTest(aiResponse)
                    }
                } else {
                    // Это новый вопрос
                    addAIMessage(aiResponse)
                    binding.btnSend.isEnabled = true
                }
            } else {
                val errorMessage = result.exceptionOrNull()?.message
                Toast.makeText(
                    this@TestQuestionsActivity,
                    errorMessage ?: getString(R.string.error_api),
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnSend.isEnabled = true
            }
            
            binding.progressBar.visibility = View.GONE
        }
    }
    
    private fun checkIfConclusion(message: String): Boolean {
        // Проверяем по ключевым словам, является ли сообщение заключением
        val conclusionKeywords = listOf(
            "заключение", "вывод", "рекомендации", "итог", "результат",
            "ваш тип", "ваш уровень", "подходящие профессии", "сильные стороны",
            "области для развития", "совет", "рекомендую", "следует", "стоит",
            "варианты решения", "практические шаги"
        )
        val lowerMessage = message.lowercase()
        // Для совета (ADVICE) проверяем наличие рекомендаций и достаточную длину
        val hasKeywords = conclusionKeywords.any { lowerMessage.contains(it) }
        val isLongEnough = message.length > (if (testType == TestType.ADVICE) 300 else 400)
        return hasKeywords && isLongEnough
    }
    
    private fun requestConclusion(repository: ChatRepository) {
        binding.progressBar.visibility = View.VISIBLE
        isWaitingForConclusion = true
        
        lifecycleScope.launch {
            val conclusionPrompt = TestPromptsService.getConclusionPrompt(testType)
            val result = repository.sendMessage(conclusionPrompt)
            
            if (result.isSuccess) {
                val conclusion = result.getOrNull() ?: "Не удалось получить заключение"
                addAIMessage(conclusion)
                finishTest(conclusion)
            } else {
                // Если запрос заключения не сработал, используем последнее сообщение
                val lastMessage = messages.lastOrNull { !it.isUser }?.content ?: "Тест завершен"
                finishTest(lastMessage)
            }
            
            binding.progressBar.visibility = View.GONE
        }
    }
    
    private fun addUserMessage(content: String) {
        val userMessage = ChatMessage(content = content, isUser = true)
        messages.add(userMessage)
        messagesAdapter.notifyItemInserted(messages.size - 1)
        binding.recyclerViewMessages.smoothScrollToPosition(messages.size - 1)
    }
    
    private fun addAIMessage(content: String) {
        val aiMessage = ChatMessage(content = content, isUser = false)
        messages.add(aiMessage)
        messagesAdapter.notifyItemInserted(messages.size - 1)
        binding.recyclerViewMessages.smoothScrollToPosition(messages.size - 1)
    }
    
    private fun finishTest(conclusion: String) {
        binding.btnSend.isEnabled = false
        
        lifecycleScope.launch {
            // Создаем результат теста
            val detailsText = when (testType) {
                TestType.ADVICE -> "Консультация проведена через ИИ-диалог. Количество вопросов: $questionCount"
                else -> "Тест проведен через ИИ-диалог. Количество вопросов: $questionCount"
            }
            
            val result = TestResult(
                testType = testType,
                result = conclusion,
                score = questionCount, // Используем количество вопросов как "счет"
                details = detailsText,
                aiAnalysis = conclusion
            )
            
            testRepository.saveResult(result)
            
            val intent = Intent(this@TestQuestionsActivity, ResultsActivity::class.java).apply {
                putExtra("RESULT_ID", result.id)
                putExtra("RESULT_TEXT", result.result)
                putExtra("RESULT_DETAILS", result.details ?: "")
                putExtra("RESULT_SCORE", result.score ?: 0)
            }
            startActivity(intent)
            finish()
        }
    }
}
