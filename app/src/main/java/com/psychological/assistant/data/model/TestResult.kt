package com.psychological.assistant.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "test_results")
data class TestResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testType: TestType,
    val result: String,
    val score: Int? = null,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val aiAnalysis: String? = null
)