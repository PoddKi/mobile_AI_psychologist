package com.psychological.assistant.repository

import com.psychological.assistant.data.database.TestResultDao
import com.psychological.assistant.data.model.TestResult
import com.psychological.assistant.data.model.TestType
import kotlinx.coroutines.flow.Flow

class TestRepository(
    private val testResultDao: TestResultDao
) {
    
    fun getAllResults(): Flow<List<TestResult>> {
        return testResultDao.getAllResults()
    }
    
    fun getResultsByType(testType: TestType): Flow<List<TestResult>> {
        return testResultDao.getResultsByType(testType)
    }
    
    suspend fun getResultById(id: Long): TestResult? {
        return testResultDao.getResultById(id)
    }
    
    fun getStressProgression(days: Int = 30): Flow<List<TestResult>> {
        val startTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return testResultDao.getStressProgression(TestType.STRESS_PROGRESSION, startTime)
    }
    
    suspend fun saveResult(result: TestResult): Long {
        return testResultDao.insertResult(result)
    }
    
    suspend fun deleteResult(result: TestResult) {
        testResultDao.deleteResult(result)
    }
}