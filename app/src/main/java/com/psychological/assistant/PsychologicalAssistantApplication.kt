package com.psychological.assistant

import android.app.Application
import com.psychological.assistant.utils.ThemeManager

class PsychologicalAssistantApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Применяем сохраненную тему при запуске приложения
        ThemeManager.applyTheme(this)
    }
}
