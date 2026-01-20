package com.psychological.assistant.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.psychological.assistant.data.database.AppDatabase
import com.psychological.assistant.data.model.TestResult
import com.psychological.assistant.databinding.ActivityHistoryBinding
import com.psychological.assistant.repository.TestRepository
import com.psychological.assistant.utils.ThemeManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var testRepository: TestRepository
    private lateinit var historyAdapter: HistoryAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Скрываем ActionBar для чистого вида
        supportActionBar?.hide()
        
        // Настраиваем заголовок
        binding.header.tvTitle.text = "История тестов"
        binding.header.btnBack.setOnClickListener {
            finish()
        }
        
        val database = AppDatabase.getDatabase(applicationContext)
        testRepository = TestRepository(database.testResultDao())
        
        setupRecyclerView()
        loadHistory()
    }
    
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList()) { result ->
            openResult(result)
        }
        
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }
    
    private fun loadHistory() {
        lifecycleScope.launch {
            testRepository.getAllResults().collectLatest { results ->
                if (results.isEmpty()) {
                    binding.recyclerViewHistory.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                } else {
                    binding.recyclerViewHistory.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
                    // Обновляем данные в адаптере
                    historyAdapter.updateResults(results)
                }
            }
        }
    }
    
    private fun openResult(result: TestResult) {
        android.util.Log.d("HistoryActivity", "Opening result: ${result.id}, type: ${result.testType}")
        val intent = Intent(this, ResultsActivity::class.java).apply {
            putExtra("RESULT_ID", result.id)
            putExtra("RESULT_TEXT", result.result)
            putExtra("RESULT_DETAILS", result.details ?: "")
            putExtra("RESULT_SCORE", result.score ?: 0)
        }
        startActivity(intent)
    }
}
