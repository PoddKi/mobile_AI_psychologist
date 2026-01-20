# AI Психологический Ассистент

Мобильное приложение для Android на Kotlin, предоставляющее AI-чат ассистента с функциями проведения психологических тестов.

## Возможности

-  **AI Чат** - Общение с психологическим ассистентом на базе GigaChat
-  **Тест на тип личности** - Определение типа личности пользователя через AI-диалог
-  **Тест на уровень стресса** - Измерение текущего уровня стресса
-  **Анализ отношений** - Оценка качества межличностных отношений
-  **Эмоциональный интеллект** - Определение уровня эмоционального интеллекта
-  **Определение профессии** - Подбор подходящих профессиональных направлений
-  **Прогрессия стресса** - Отслеживание изменений уровня стресса во времени
-  **Попросить совета** - Получение персональных советов от AI-психолога
-  **Статистика и графики** - Визуализация результатов тестов и динамики
-  **Темная тема** - Поддержка светлой и темной темы с плавной анимацией перехода

## Технологии

- **Kotlin** - Основной язык программирования
- **Android Jetpack**:
  - ViewBinding
  - Room Database
  - Lifecycle Components
  - Navigation Component
- **Retrofit** - Для работы с API
- **Coroutines** - Асинхронное программирование
- **Flow** - Реактивные потоки данных
- **GigaChat API** - ИИ чат-бот
- **MPAndroidChart** - Библиотека для графиков и диаграмм
- **Material Design Components** - Современный UI

## Настройка GigaChat API

Приложение использует автоматическое обновление токенов через OAuth2. Для работы необходимо:

1. Зарегистрируйтесь на [GigaChat](https://developers.sber.ru/gigachat)
2. Получите authorization token (base64 encoded client_id:client_secret)
3. Укажите токен в `ApiConfig.kt` или введите при первом запуске

### Настройка authorization token

В файле `app/src/main/java/com/psychological/assistant/config/ApiConfig.kt` установите:

```kotlin
const val DEFAULT_AUTHORIZATION_TOKEN: String = "ваш_authorization_token_здесь"
```

Приложение автоматически получает и обновляет access token каждые 30 минут.

## Установка и сборка

1. Клонируйте репозиторий
2. Откройте проект в Android Studio
3. Синхронизируйте Gradle
4. Укажите токен GigaChat
5. Запустите приложение

## Структура проекта

```
app/src/main/java/com/psychological/assistant/
├── api/                    # GigaChat API клиент
│   ├── GigaChatApi.kt     # Интерфейс Retrofit
│   └── GigaChatClient.kt  # HTTP клиент с SSL настройками
├── config/                 # Конфигурация
│   └── ApiConfig.kt       # Authorization token
├── data/
│   ├── database/          # Room база данных
│   │   ├── AppDatabase.kt
│   │   ├── TestResultDao.kt
│   │   └── Converters.kt
│   └── model/             # Модели данных
│       ├── ChatMessage.kt
│       ├── GigaChatRequest.kt
│       ├── GigaChatTokenResponse.kt
│       ├── TestResult.kt
│       └── TestType.kt
├── repository/            # Репозитории данных
│   ├── ChatRepository.kt
│   ├── StatisticsRepository.kt
│   └── TestRepository.kt
├── services/              # Бизнес-логика
│   └── TestPromptsService.kt  # Промпты для AI тестов
├── ui/                    # UI компоненты
│   ├── MainActivity.kt
│   ├── ChatActivity.kt
│   ├── TestActivity.kt
│   ├── TestQuestionsActivity.kt
│   ├── ResultsActivity.kt
│   ├── HistoryActivity.kt
│   ├── StatisticsActivity.kt
│   ├── MessagesAdapter.kt
│   └── HistoryAdapter.kt
└── utils/                 # Утилиты
    ├── PreferencesHelper.kt
    ├── ThemeManager.kt
    └── TokenManager.kt
```

## Минимальные требования

- Android 7.0 (API 24) или выше
- Интернет соединение для работы с GigaChat API

## Лицензия

Этот проект создан в образовательных целях.

## Особенности

- **AI-диалоговые тесты** - Все тесты проводятся через диалог с AI, а не статические вопросы
- **Автоматическое обновление токенов** - Access token обновляется автоматически каждые 30 минут
- **Темная тема** - Поддержка светлой и темной темы с плавной анимацией перехода
- **Статистика** - Графики и аналитика по пройденным тестам
- **История тестов** - Сохранение всех результатов с возможностью просмотра

## Примечания

- Для работы с GigaChat API требуется валидный authorization token
- База данных хранит результаты тестов локально на устройстве
- Все тесты используют AI для генерации вопросов и анализа ответов