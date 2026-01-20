package com.psychological.assistant.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.psychological.assistant.data.model.TestResult
import com.psychological.assistant.data.model.TestType
import com.psychological.assistant.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private var results: List<TestResult>,
    private val onItemClick: (TestResult) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    fun updateResults(newResults: List<TestResult>) {
        results = newResults
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(result: TestResult) {
            binding.tvTestType.text = getTestTypeName(result.testType)
            
            // Форматируем дату
            val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru", "RU"))
            val date = Date(result.timestamp)
            binding.tvDate.text = dateFormat.format(date)
            
            // Показываем превью результата (первые 200 символов)
            val preview = result.result.take(200)
            binding.tvResultPreview.text = if (result.result.length > 200) {
                "$preview..."
            } else {
                preview
            }
            
            // Устанавливаем обработчик клика на всю карточку
            binding.root.setOnClickListener {
                android.util.Log.d("HistoryAdapter", "Card clicked for result: ${result.id}")
                onItemClick(result)
            }
            
            // Также делаем карточку кликабельной для лучшей поддержки
            binding.root.isClickable = true
            binding.root.isFocusable = true
        }

        private fun getTestTypeName(testType: TestType): String {
            return when (testType) {
                TestType.PERSONALITY_TYPE -> "Тест на тип личности"
                TestType.STRESS_LEVEL -> "Тест на уровень стресса"
                TestType.RELATIONSHIPS -> "Анализ отношений"
                TestType.EMOTIONAL_INTELLIGENCE -> "Эмоциональный интеллект"
                TestType.PROFESSION -> "Определение профессии"
                TestType.STRESS_PROGRESSION -> "Прогрессия стресса"
                TestType.ADVICE -> "Попросить совета"
            }
        }
    }
}
