package com.psychological.assistant.config

/**
 * Конфигурация API
 * 
 * Для автоматического обновления токенов используйте DEFAULT_AUTHORIZATION_TOKEN.
 * Этот токен используется для получения access token, который обновляется автоматически.
 * 
 * Чтобы получить authorization token:
 * 1. Зарегистрируйтесь на https://developers.sber.ru/gigachat
 * 2. Создайте приложение
 * 3. Получите authorization token (base64 encoded client_id:client_secret)
 * 4. Вставьте токен ниже
 */
object ApiConfig {
    
    /**
     * Authorization token для получения access token через OAuth
     * Этот токен долгоживущий и используется для автоматического обновления access token
     * 
     * Формат: base64 encoded строка (client_id:client_secret)
     * Пример: "MDE5YmNkOWYtY2VkOS03MTFmLWIzNmYtOGU0YTAwZjJmZDc4OjMyOGFlNDdhLTI5OTYtNDA0OC1hNmU3LTQ2NmZlMDk4YThmZg=="
     */
    const val DEFAULT_AUTHORIZATION_TOKEN: String = "MDE5YmNkOWYtY2VkOS03MTFmLWIzNmYtOGU0YTAwZjJmZDc4OjMyOGFlNDdhLTI5OTYtNDA0OC1hNmU3LTQ2NmZlMDk4YThmZg=="
    
    /**
     * Токен доступа GigaChat API по умолчанию (устаревший)
     * @deprecated Используйте DEFAULT_AUTHORIZATION_TOKEN для автоматического обновления
     * 
     * Формат: просто токен без префикса "Bearer"
     * Пример: "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
     * 
     * Если оставить пустым, будет использоваться автоматическое обновление через authorization token
     */
    @Deprecated("Use DEFAULT_AUTHORIZATION_TOKEN instead")
    const val DEFAULT_GIGACHAT_TOKEN: String = ""

}
