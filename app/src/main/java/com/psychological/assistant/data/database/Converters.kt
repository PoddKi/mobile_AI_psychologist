package com.psychological.assistant.data.database

import androidx.room.TypeConverter
import com.psychological.assistant.data.model.TestType

class Converters {
    @TypeConverter
    fun fromTestType(value: TestType): String {
        return value.name
    }
    
    @TypeConverter
    fun toTestType(value: String): TestType {
        return TestType.valueOf(value)
    }
}