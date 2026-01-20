package com.psychological.assistant.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.psychological.assistant.data.model.TestType
import com.psychological.assistant.databinding.ActivityTestBinding
import com.psychological.assistant.utils.ThemeManager

class TestActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTestBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Скрываем ActionBar для чистого вида
        supportActionBar?.hide()
        
        // Настраиваем заголовок
        binding.header.tvTitle.text = "Выбор теста"
        binding.header.btnBack.setOnClickListener {
            finish()
        }
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnPersonalityTest.setOnClickListener {
            startTest(TestType.PERSONALITY_TYPE)
        }
        
        binding.btnStressTest.setOnClickListener {
            startTest(TestType.STRESS_LEVEL)
        }
        
        binding.btnRelationshipsTest.setOnClickListener {
            startTest(TestType.RELATIONSHIPS)
        }
        
        binding.btnEmotionalIntelligenceTest.setOnClickListener {
            startTest(TestType.EMOTIONAL_INTELLIGENCE)
        }
        
        binding.btnProfessionTest.setOnClickListener {
            startTest(TestType.PROFESSION)
        }
    }
    
    private fun startTest(testType: TestType) {
        val intent = Intent(this, TestQuestionsActivity::class.java).apply {
            putExtra("TEST_TYPE", testType.name)
        }
        startActivity(intent)
    }
}