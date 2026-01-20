package com.psychological.assistant.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.psychological.assistant.databinding.ActivityMainBinding
import com.psychological.assistant.utils.ThemeManager

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Применяем сохраненную тему перед setContentView
        ThemeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        
        // Скрываем ActionBar для чистого вида
        supportActionBar?.hide()
        
        android.util.Log.d("MainActivity", "onCreate started")
        
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            android.util.Log.d("MainActivity", "Binding inflated successfully")
            
            setContentView(binding.root)
            android.util.Log.d("MainActivity", "Content view set")
            
            // Проверяем, был ли это переход с анимацией темы
            val shouldAnimateReveal = intent.getBooleanExtra("theme_transition", false)
            if (shouldAnimateReveal) {
                animateThemeReveal()
            }
            
            setupThemeSwitch()
            setupClickListeners()
            android.util.Log.d("MainActivity", "Click listeners setup completed")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "FATAL ERROR in onCreate", e)
            e.printStackTrace()
            // Не завершаем приложение, просто показываем ошибку
            try {
                android.widget.Toast.makeText(this, "Ошибка: ${e.javaClass.simpleName}: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            } catch (toastError: Exception) {
                android.util.Log.e("MainActivity", "Cannot show toast", toastError)
            }
        }
    }
    
    private fun animateThemeReveal() {
        // Плавное появление контента после перехода темы
        binding.root.alpha = 0f
        binding.root.animate()
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }
    
    private fun setupThemeSwitch() {
        // Устанавливаем эмодзи в соответствии с текущей темой
        updateThemeIcon()
        
        // Обработчик переключения темы
        binding.btnThemeToggle.setOnClickListener {
            // Сохраняем текущую тему ДО переключения
            val wasDarkBeforeToggle = ThemeManager.isDarkTheme(this)
            ThemeManager.toggleTheme(this)
            // Перезапускаем Activity для применения темы с анимацией
            recreateWithAnimation(wasDarkBeforeToggle)
        }
    }
    
    private fun recreateWithAnimation(wasDarkBeforeToggle: Boolean) {
        // Получаем координаты кнопки для circular reveal
        binding.btnThemeToggle.post {
            val buttonLocation = IntArray(2)
            binding.btnThemeToggle.getLocationInWindow(buttonLocation)
            
            val centerX = buttonLocation[0] + binding.btnThemeToggle.width / 2
            val centerY = buttonLocation[1] + binding.btnThemeToggle.height / 2
            
            // Вычисляем максимальный радиус для покрытия всего экрана
            val rootView = window.decorView.rootView
            val maxRadius = Math.hypot(
                Math.max(centerX, rootView.width - centerX).toDouble(),
                Math.max(centerY, rootView.height - centerY).toDouble()
            ).toFloat()
            
            // Создаем overlay view для анимации
            val overlay = View(this@MainActivity).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Используем цвет НОВОЙ темы (после toggleTheme)
                setBackgroundColor(
                    if (wasDarkBeforeToggle) {
                        // Была темная, теперь светлая - используем светлый цвет
                        0xFFF8F9FA.toInt()
                    } else {
                        // Была светлая, теперь темная - используем темный цвет
                        0xFF121212.toInt()
                    }
                )
                alpha = 0f
            }
            
            // Добавляем overlay поверх контента
            val rootViewGroup = rootView as? android.view.ViewGroup
            rootViewGroup?.addView(overlay)
            
            // Флаг для отслеживания, начался ли переход
            var hasStartedTransition = false
            
            // Анимация появления overlay (circular reveal)
            val revealAnimator = ViewAnimationUtils.createCircularReveal(
                overlay,
                centerX,
                centerY,
                0f,
                maxRadius
            ).apply {
                duration = 500
                interpolator = android.view.animation.DecelerateInterpolator()
                
                // Запускаем перезапуск Activity на 65% прогресса (325ms из 500ms)
                overlay.postDelayed({
                    if (!hasStartedTransition) {
                        hasStartedTransition = true
                        // Перезапускаем Activity в середине анимации
                        val intent = Intent(this@MainActivity, MainActivity::class.java).apply {
                            putExtra("theme_transition", true)
                        }
                        finish()
                        startActivity(intent)
                        // Используем мгновенный переход (без анимации), так как overlay уже покрыл экран
                        overridePendingTransition(0, 0)
                    }
                }, 325) // 65% от 500ms
                
                doOnEnd {
                    // Если переход еще не начался, запускаем его сейчас
                    if (!hasStartedTransition) {
                        hasStartedTransition = true
                        val intent = Intent(this@MainActivity, MainActivity::class.java).apply {
                            putExtra("theme_transition", true)
                        }
                        finish()
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    }
                }
            }
            
            // Запускаем анимацию
            overlay.alpha = 1f
            revealAnimator.start()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем иконку темы при возврате на экран
        if (::binding.isInitialized) {
            updateThemeIcon()
        }
    }
    
    private fun updateThemeIcon() {
        val isDark = ThemeManager.isDarkTheme(this)
        binding.btnThemeToggle.text = if (isDark) "🌚" else "🌝"
    }
    
    private fun setupClickListeners() {
        binding.cardTakeTest.setOnClickListener {
            val intent = Intent(this, TestActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardAskAdvice.setOnClickListener {
            val intent = Intent(this, TestQuestionsActivity::class.java).apply {
                putExtra("TEST_TYPE", com.psychological.assistant.data.model.TestType.ADVICE.name)
            }
            startActivity(intent)
        }
        
        binding.cardStatistics.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardViewHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}