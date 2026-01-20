package com.psychological.assistant.repository

import com.psychological.assistant.data.database.TestResultDao
import com.psychological.assistant.data.model.TestType
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для сбора статистики по тестам
 */
class StatisticsRepository(private val testResultDao: TestResultDao) {
    
    /**
     * Получает все результаты для статистики
     */
    fun getAllResults(): Flow<List<com.psychological.assistant.data.model.TestResult>> {
        return testResultDao.getAllResults()
    }
    
    /**
     * Получает количество тестов по типам
     */
    suspend fun getTestsByType(): Map<TestType, Int> {
        val results = testResultDao.getAllResultsSync()
        return results.groupingBy { it.testType }.eachCount()
    }
    
    /**
     * Получает результаты тестов на стресс для графика динамики
     */
    suspend fun getStressResults(): List<Pair<Long, Int>> {
        val results = testResultDao.getAllResultsSync()
            .filter { it.testType == TestType.STRESS_LEVEL || it.testType == TestType.STRESS_PROGRESSION }
            .sortedBy { it.timestamp }
        
        // Извлекаем "score" как уровень стресса (или используем timestamp для графика)
        return results.map { it.timestamp to (it.score ?: 0) }
    }
    
    /**
     * Получает общее количество пройденных тестов
     */
    suspend fun getTotalTestsCount(): Int {
        return testResultDao.getAllResultsSync().size
    }
    
    /**
     * Получает количество тестов за последние 30 дней
     */
    suspend fun getRecentTestsCount(days: Int = 30): Int {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return testResultDao.getAllResultsSync()
            .count { it.timestamp >= cutoffTime }
    }
}
