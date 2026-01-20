package com.psychological.assistant.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.psychological.assistant.data.database.AppDatabase
import com.psychological.assistant.data.model.TestType
import com.psychological.assistant.databinding.ActivityStatisticsBinding
import com.psychological.assistant.repository.StatisticsRepository
import com.psychological.assistant.utils.ThemeManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var statisticsRepository: StatisticsRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Скрываем ActionBar
        supportActionBar?.hide()
        
        // Настраиваем заголовок
        binding.header.tvTitle.text = "Статистика"
        binding.header.btnBack.setOnClickListener {
            finish()
        }
        
        // Инициализируем репозиторий
        val database = AppDatabase.getDatabase(applicationContext)
        statisticsRepository = StatisticsRepository(database.testResultDao())
        
        setupCharts()
        loadStatistics()
    }
    
    private fun setupCharts() {
        setupPieChart()
        setupBarChart()
        setupLineChart()
    }
    
    private fun setupPieChart() {
        binding.pieChart.description.isEnabled = false
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.setDrawEntryLabels(false)
        binding.pieChart.legend.isEnabled = true
        binding.pieChart.legend.textSize = 12f
        binding.pieChart.legend.textColor = getColor(com.psychological.assistant.R.color.text_primary)
        binding.pieChart.setHoleColor(Color.TRANSPARENT)
        binding.pieChart.setTransparentCircleColor(Color.TRANSPARENT)
    }
    
    private fun setupBarChart() {
        binding.barChart.description.isEnabled = false
        binding.barChart.legend.isEnabled = false
        binding.barChart.setDrawGridBackground(false)
        binding.barChart.setScaleEnabled(false)
        binding.barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barChart.xAxis.setDrawGridLines(false)
        binding.barChart.xAxis.granularity = 1f // Устанавливаем интервал между подписями
        binding.barChart.xAxis.labelRotationAngle = -45f // Поворачиваем подписи на -45 градусов
        binding.barChart.xAxis.textSize = 10f // Уменьшаем размер шрифта
        binding.barChart.xAxis.setLabelCount(10, false) // Максимальное количество подписей
        binding.barChart.axisLeft.setDrawGridLines(true)
        binding.barChart.axisRight.isEnabled = false
        binding.barChart.axisLeft.textColor = getColor(com.psychological.assistant.R.color.text_primary)
        binding.barChart.xAxis.textColor = getColor(com.psychological.assistant.R.color.text_primary)
    }
    
    private fun setupLineChart() {
        binding.lineChart.description.isEnabled = false
        binding.lineChart.legend.isEnabled = false
        binding.lineChart.setDrawGridBackground(false)
        binding.lineChart.setScaleEnabled(true)
        binding.lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.lineChart.xAxis.setDrawGridLines(false)
        binding.lineChart.axisLeft.setDrawGridLines(true)
        binding.lineChart.axisRight.isEnabled = false
        binding.lineChart.axisLeft.textColor = getColor(com.psychological.assistant.R.color.text_primary)
        binding.lineChart.xAxis.textColor = getColor(com.psychological.assistant.R.color.text_primary)
    }
    
    private fun loadStatistics() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Получаем статистику
                val testsByType = statisticsRepository.getTestsByType()
                val totalTests = statisticsRepository.getTotalTestsCount()
                val recentTests = statisticsRepository.getRecentTestsCount(30)
                val stressResults = statisticsRepository.getStressResults()
                
                // Обновляем UI
                binding.tvTotalTests.text = totalTests.toString()
                binding.tvRecentTests.text = recentTests.toString()
                
                // Обновляем графики
                updatePieChart(testsByType)
                updateBarChart(testsByType)
                updateLineChart(stressResults)
                
                // Показываем контент
                binding.scrollView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                
            } catch (e: Exception) {
                android.util.Log.e("StatisticsActivity", "Error loading statistics", e)
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun updatePieChart(testsByType: Map<TestType, Int>) {
        if (testsByType.isEmpty()) {
            binding.pieChart.visibility = View.GONE
            return
        }
        
        val entries = testsByType.map { (type, count) ->
            PieEntry(count.toFloat(), getTestTypeName(type))
        }
        
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#4A90E2"), // primary_blue
                Color.parseColor("#4CAF50"), // accent_green
                Color.parseColor("#FF9800"), // accent_orange
                Color.parseColor("#E91E63"), // accent_pink
                Color.parseColor("#9C27B0"), // accent_purple
                Color.parseColor("#00BCD4")  // teal
            )
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }
        
        val data = PieData(dataSet)
        binding.pieChart.data = data
        binding.pieChart.invalidate()
    }
    
    private fun updateBarChart(testsByType: Map<TestType, Int>) {
        if (testsByType.isEmpty()) {
            binding.barChart.visibility = View.GONE
            return
        }
        
        val entries = testsByType.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }
        
        val dataSet = BarDataSet(entries, "").apply {
            color = getColor(com.psychological.assistant.R.color.primary_blue)
            valueTextSize = 12f
            valueTextColor = getColor(com.psychological.assistant.R.color.text_primary)
        }
        
        val data = BarData(dataSet)
        binding.barChart.data = data
        
        // Настраиваем подписи осей
        binding.barChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                val types = testsByType.keys.toList()
                return if (index < types.size) {
                    getTestTypeVeryShortName(types[index])
                } else ""
            }
        }
        
        // Устанавливаем правильное количество подписей
        binding.barChart.xAxis.setLabelCount(testsByType.size, false)
        
        binding.barChart.invalidate()
    }
    
    private fun updateLineChart(stressResults: List<Pair<Long, Int>>) {
        if (stressResults.isEmpty()) {
            binding.lineChart.visibility = View.GONE
            binding.tvNoStressData.visibility = View.VISIBLE
            return
        }
        
        binding.tvNoStressData.visibility = View.GONE
        
        val entries = stressResults.mapIndexed { index, (_, score) ->
            Entry(index.toFloat(), score.toFloat())
        }
        
        val dataSet = LineDataSet(entries, "Уровень стресса").apply {
            color = getColor(com.psychological.assistant.R.color.accent_orange)
            lineWidth = 2f
            setCircleColor(getColor(com.psychological.assistant.R.color.accent_orange))
            circleRadius = 4f
            valueTextSize = 10f
            valueTextColor = getColor(com.psychological.assistant.R.color.text_primary)
        }
        
        val data = LineData(dataSet)
        binding.lineChart.data = data
        
        // Настраиваем подписи осей
        binding.lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index < stressResults.size) {
                    val date = Date(stressResults[index].first)
                    SimpleDateFormat("dd.MM", Locale.getDefault()).format(date)
                } else ""
            }
        }
        
        binding.lineChart.invalidate()
    }
    
    private fun getTestTypeName(testType: TestType): String {
        return when (testType) {
            TestType.PERSONALITY_TYPE -> "Тип личности"
            TestType.STRESS_LEVEL -> "Уровень стресса"
            TestType.RELATIONSHIPS -> "Отношения"
            TestType.EMOTIONAL_INTELLIGENCE -> "Эмоц. интеллект"
            TestType.PROFESSION -> "Профессия"
            TestType.STRESS_PROGRESSION -> "Прогрессия стресса"
            TestType.ADVICE -> "Советы"
        }
    }
    
    private fun getTestTypeShortName(testType: TestType): String {
        return when (testType) {
            TestType.PERSONALITY_TYPE -> "Личность"
            TestType.STRESS_LEVEL -> "Стресс"
            TestType.RELATIONSHIPS -> "Отношения"
            TestType.EMOTIONAL_INTELLIGENCE -> "EQ"
            TestType.PROFESSION -> "Профессия"
            TestType.STRESS_PROGRESSION -> "Прогрессия"
            TestType.ADVICE -> "Советы"
        }
    }
    
    private fun getTestTypeVeryShortName(testType: TestType): String {
        // Еще более короткие названия для лучшей читаемости
        return when (testType) {
            TestType.PERSONALITY_TYPE -> "Личн."
            TestType.STRESS_LEVEL -> "Стресс"
            TestType.RELATIONSHIPS -> "Отнош."
            TestType.EMOTIONAL_INTELLIGENCE -> "EQ"
            TestType.PROFESSION -> "Проф."
            TestType.STRESS_PROGRESSION -> "Прогр."
            TestType.ADVICE -> "Совет"
        }
    }
}
