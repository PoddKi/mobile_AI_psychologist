package com.psychological.assistant.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.psychological.assistant.databinding.ActivityResultsBinding
import com.psychological.assistant.utils.ThemeManager

class ResultsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityResultsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Скрываем ActionBar для чистого вида
        supportActionBar?.hide()
        
        // Настраиваем заголовок
        binding.header.tvTitle.text = "Результаты теста"
        binding.header.btnBack.setOnClickListener {
            finish()
        }
        
        val resultText = intent.getStringExtra("RESULT_TEXT") ?: "Результат не получен"
        val resultDetails = intent.getStringExtra("RESULT_DETAILS") ?: ""
        val resultScore = intent.getIntExtra("RESULT_SCORE", 0)
        
        // Отображаем ИИ-заключение как основной результат
        binding.tvResult.text = resultText
        
        // Показываем количество вопросов, если есть
        if (resultScore > 0) {
            binding.tvScore.text = "Вопросов пройдено: $resultScore"
            binding.tvScore.visibility = android.view.View.VISIBLE
        } else {
            binding.tvScore.visibility = android.view.View.GONE
        }
        
        // Детали показываем только если есть дополнительная информация
        if (resultDetails.isNotEmpty() && resultDetails != "Тест проведен через ИИ-диалог.") {
            binding.tvDetails.text = resultDetails
            binding.tvDetails.visibility = android.view.View.VISIBLE
        } else {
            binding.tvDetails.visibility = android.view.View.GONE
        }
    }
}