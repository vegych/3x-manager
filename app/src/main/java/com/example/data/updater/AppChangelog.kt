package com.example.data.updater

import androidx.compose.ui.graphics.Color

enum class ReleaseType(val label: String, val badgeColor: Color) {
    MAJOR_FEATURE("Крупное обновление", Color(0xFF00E5FF)),
    IMPROVEMENT("Улучшения", Color(0xFF00E676)),
    BUG_FIX("Исправление ошибок", Color(0xFFFFB300))
}

data class ChangelogCategory(
    val title: String,
    val items: List<String>
)

data class ChangelogEntry(
    val version: String,
    val date: String,
    val releaseType: ReleaseType,
    val summary: String,
    val categories: List<ChangelogCategory>
)

object AppChangelog {
    val history = listOf(
        ChangelogEntry(
            version = "v1.1.0",
            date = "Октябрь 2026",
            releaseType = ReleaseType.MAJOR_FEATURE,
            summary = "Боковая панель вкладок открытых серверов, авто-обновления с GitHub и наглядные чейнджлоги",
            categories = listOf(
                ChangelogCategory(
                    title = "✨ Новые возможности",
                    items = listOf(
                        "Боковая панель открытых серверов (вкладки) справа: переключайтесь между открытыми веб-панелями без потери состояния сессии",
                        "Кнопка быстрого закрытия всех вкладок в боковом меню",
                        "Автоматическая тихая проверка релизов на GitHub с кнопкой версии в шапке",
                        "Встроенный загрузчик с индикатором прогресса и прямой установкой APK"
                    )
                ),
                ChangelogCategory(
                    title = "⚡ Улучшения интерфейса",
                    items = listOf(
                        "Кнопка новой версии размещена слева от переключателя скрытия IP",
                        "Кнопка быстрой остановки туннеля прямо в верхней панели",
                        "Удобный крестик закрытия в верхнем правом углу диалога настроек",
                        "Понятные и структурированные списки изменений по категориям"
                    )
                ),
                ChangelogCategory(
                    title = "🛠 Исправления и надежность",
                    items = listOf(
                        "Исправлена установка APK на Android 8.0–15+ (запрос разрешения на установку из неизвестных источников)",
                        "Точное распознавание версий GitHub (исключено скачивание под именем «update»)",
                        "Отключены навязчивые всплывающие сообщения (Snackbar) при переключении опций"
                    )
                )
            )
        ),
        ChangelogEntry(
            version = "v1.0.22",
            date = "Октябрь 2026",
            releaseType = ReleaseType.BUG_FIX,
            summary = "Исправления стабильности соединений и оптимизация интерфейса",
            categories = listOf(
                ChangelogCategory(
                    title = "🛠 Исправления ошибок",
                    items = listOf(
                        "Корректное завершение SSH-туннелей в зависимости от выбранной политики закрытия",
                        "Автоматическое повторное подключение при кратковременном разрыве сети",
                        "Оптимизация отображения карточек на экранах с разным масштабом шрифта"
                    )
                )
            )
        ),
        ChangelogEntry(
            version = "v1.0.1",
            date = "Октябрь 2026",
            releaseType = ReleaseType.IMPROVEMENT,
            summary = "Резервное копирование WebDAV, гибкая сортировка серверов и маскировка IP",
            categories = listOf(
                ChangelogCategory(
                    title = "✨ Новое",
                    items = listOf(
                        "Синхронизация и бэкап конфигураций через WebDAV (Яндекс Диск, Nextcloud)",
                        "Экспорт и импорт резервных копий в локальные JSON-файлы"
                    )
                ),
                ChangelogCategory(
                    title = "⚡ Улучшения",
                    items = listOf(
                        "Сортировка списка серверов: по частоте использования, по алфавиту и дате",
                        "Режим скрытия IP-адресов и портов для безопасности и скриншотов",
                        "Полупрозрачные подсказки в полях ввода SSH и портов"
                    )
                )
            )
        ),
        ChangelogEntry(
            version = "v1.0.0",
            date = "Октябрь 2026",
            releaseType = ReleaseType.MAJOR_FEATURE,
            summary = "Первый релиз 3x manager для Android",
            categories = listOf(
                ChangelogCategory(
                    title = "✨ Основные возможности",
                    items = listOf(
                        "Автоматический SSH-туннель для безопасного доступа к скрытым веб-панелям 3x-ui",
                        "Авторизация по SSH-ключам (включая passphrase) и паролям",
                        "Встроенный оптимизированный браузер с переключением вида ПК/Смартфон",
                        "Автоматический бесшовный вход в панель управления"
                    )
                )
            )
        )
    )
}
