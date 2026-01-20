package com.psychological.assistant.data.database

import androidx.room.*
import com.psychological.assistant.data.model.TestResult
import com.psychological.assistant.data.model.TestType
import kotlinx.coroutines.flow.Flow

@Dao
interface TestResultDao {
    
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<TestResult>>
    
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    suspend fun getAllResultsSync(): List<TestResult>
    
    @Query("SELECT * FROM test_results WHERE testType = :testType ORDER BY timestamp DESC")
    fun getResultsByType(testType: TestType): Flow<List<TestResult>>
    
    @Query("SELECT * FROM test_results WHERE id = :id")
    suspend fun getResultById(id: Long): TestResult?
    
    @Query("SELECT * FROM test_results WHERE testType = :testType AND timestamp >= :startTime ORDER BY timestamp ASC")
    fun getStressProgression(testType: TestType, startTime: Long): Flow<List<TestResult>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TestResult): Long
    
    @Delete
    suspend fun deleteResult(result: TestResult)
    
    @Query("DELETE FROM test_results")
    suspend fun deleteAllResults()
}