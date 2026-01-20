package com.psychological.assistant.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    
    private const val PREF_THEME = "theme_mode"
    private const val THEME_LIGHT = "light"
    private const val THEME_DARK = "dark"
    private const val THEME_SYSTEM = "system"
    
    /**
     * Применяет сохраненную тему
     */
    fun applyTheme(context: Context) {
        val theme = getSavedTheme(context)
        setTheme(theme)
    }
    
    /**
     * Устанавливает тему
     */
    fun setTheme(theme: String) {
        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    /**
     * Получает сохраненную тему
     */
    fun getSavedTheme(context: Context): String {
        val prefs = context.getSharedPreferences("psychological_assistant_prefs", Context.MODE_PRIVATE)
        return prefs.getString(PREF_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }
    
    /**
     * Сохраняет тему
     */
    fun saveTheme(context: Context, theme: String) {
        val prefs = context.getSharedPreferences("psychological_assistant_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_THEME, theme).apply()
        setTheme(theme)
    }
    
    /**
     * Переключает между светлой и темной темой
     */
    fun toggleTheme(context: Context) {
        val currentTheme = getSavedTheme(context)
        val newTheme = when (currentTheme) {
            THEME_LIGHT -> THEME_DARK
            THEME_DARK -> THEME_LIGHT
            else -> THEME_DARK // Если системная, переключаем на темную
        }
        saveTheme(context, newTheme)
    }
    
    /**
     * Проверяет, включена ли темная тема
     */
    fun isDarkTheme(context: Context): Boolean {
        val theme = getSavedTheme(context)
        return if (theme == THEME_SYSTEM) {
            // Проверяем системную тему
            val nightMode = AppCompatDelegate.getDefaultNightMode()
            nightMode == AppCompatDelegate.MODE_NIGHT_YES
        } else {
            theme == THEME_DARK
        }
    }
}
