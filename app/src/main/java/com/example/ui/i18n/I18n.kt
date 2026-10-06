package com.example.ui.i18n

import java.util.Locale

enum class AppLanguage(val code: String, val title: String) {
    SYSTEM("system", "По умолчанию / System"),
    RU("ru", "Русский"),
    EN("en", "English")
}

interface Strings {
    val appName: String
    val sortCountFormat: (Int) -> String
    val sortUsage: String
    val sortAlphabetical: String
    val sortNewest: String

    val tunnelBannerPrefix: String
    val disconnectButton: String
    val stopShort: String

    val noServersTitle: String
    val noServersSubtitle: String

    val tunnelOnBadge: String
    val autoForwardBadge: String
    val sshTunnelBadge: String
    val sshForwardRoute: (String) -> String

    val actionOpen: String
    val actionEdit: String
    val actionDelete: String

    val addServerTitle: String
    val editServerTitle: String
    val labelNameField: String
    val labelNamePlaceholder: String
    val panelUrlField: String
    val panelUrlPlaceholder: String
    val actionPaste: String
    val panelUsernameField: String
    val panelPasswordField: String

    val portForwardHeader: String
    val portForwardSub: String
    val sshHostField: String
    val sshUserField: String
    val sshPortField: String
    val authTypeKey: String
    val authTypePassword: String
    val keyAdded: String
    val privateKeyPrompt: String
    val actionPickFile: String
    val keyPassphraseField: String
    val sshPasswordField: String

    val btnSave: String
    val btnCancel: String
    val btnDelete: String

    val toastKeySelected: String
    val toastKeyPasted: String
    val toastKeyReadError: String

    val deleteServerDialogTitle: String
    val deleteServerDialogText: (String) -> String

    val noPanelSelectedTitle: String
    val noPanelSelectedSubtitle: String
    val btnToServers: String
    val desktopModeTooltip: String
    val mobileModeTooltip: String
    val reloadTooltip: String
    val tunnelConnectingStatus: String
    val tunnelActiveBanner: (Int) -> String
    val tunnelFailedStatus: String
    val btnRetryConnection: String

    val settingsTitle: String
    val tabGeneral: String
    val tabBackup: String
    val tabAbout: String

    val themeTitle: String
    val themeDark: String
    val themeLight: String
    val themeSystem: String

    val languageTitle: String

    val maskIpTitle: String
    val maskIpDescription: String

    val tunnelPolicyTitle: String
    val policyPanelExitTitle: String
    val policyPanelExitDesc: String
    val policyAppCloseTitle: String
    val policyAppCloseDesc: String
    val policyNeverTitle: String
    val policyNeverDesc: String

    val activeTunnelCardTitle: String
    val activeTunnelCardDesc: String
    val btnStopTunnel: String

    val localBackupTitle: String
    val localBackupDesc: String
    val btnSaveFile: String
    val btnLoadFile: String

    val webdavTitle: String
    val webdavDesc: String
    val webdavUrlField: String
    val webdavUserField: String
    val webdavPassField: String
    val btnUpload: String
    val btnDownload: String
    val webdavHint: String

    val versionPrefix: String
    val btnCheckUpdates: String
    val btnCheckingUpdates: String
    val changelogTitle: String
    val btnGitHub: String

    val updateAvailableTitle: String
    val updateAvailableText: (String, String) -> String
    val btnDownloadInstall: String
    val btnLater: String

    val msgServerSaved: (String) -> String
    val msgServerDeleted: (String) -> String
    val msgBackupRestored: (Int) -> String
    val msgWebdavSuccess: String
    val msgUpToDate: (String) -> String
    val msgUpdateAvailable: (String) -> String
}

object RussianStrings : Strings {
    override val appName = "3x manager"
    override val sortCountFormat: (Int) -> String = { count ->
        val rem100 = count % 100
        val rem10 = count % 10
        val word = when {
            rem100 in 11..19 -> "серверов"
            rem10 == 1 -> "сервер"
            rem10 in 2..4 -> "сервера"
            else -> "серверов"
        }
        "$count $word"
    }
    override val sortUsage = "По частоте"
    override val sortAlphabetical = "По алфавиту"
    override val sortNewest = "По дате"

    override val tunnelBannerPrefix = "Туннель: "
    override val disconnectButton = "Отключить"
    override val stopShort = "Откл."

    override val noServersTitle = "Нет сохраненных серверов"
    override val noServersSubtitle = "Нажмите «+» справа снизу, чтобы добавить панель 3x-ui по ссылке"

    override val tunnelOnBadge = "Туннель ВКЛ"
    override val autoForwardBadge = "Автопроброс"
    override val sshTunnelBadge = "SSH Туннель"
    override val sshForwardRoute: (String) -> String = { name -> "SSH Проброс ➔ $name" }

    override val actionOpen = "Открыть"
    override val actionEdit = "Редактировать"
    override val actionDelete = "Удалить"

    override val addServerTitle = "Добавить сервер 3x-ui"
    override val editServerTitle = "Редактировать сервер"
    override val labelNameField = "Название (Label)"
    override val labelNamePlaceholder = "local u1host"
    override val panelUrlField = "Ссылка на панель (URL) *"
    override val panelUrlPlaceholder = "https://localhost:2370/secret/panel/"
    override val actionPaste = "Вставить"
    override val panelUsernameField = "Логин от панели"
    override val panelPasswordField = "Пароль от панели"

    override val portForwardHeader = "Сервер для проброса портов"
    override val portForwardSub = "Укажите промежуточный SSH-сервер (Intermediate host):"
    override val sshHostField = "SSH Сервер (Intermediate host) *"
    override val sshUserField = "SSH Юзер"
    override val sshPortField = "Порт"
    override val authTypeKey = "SSH Key"
    override val authTypePassword = "Пароль"
    override val keyAdded = "✓ SSH-ключ добавлен"
    override val privateKeyPrompt = "Приватный ключ SSH:"
    override val actionPickFile = "Выбрать файл"
    override val keyPassphraseField = "Passphrase ключа (если есть)"
    override val sshPasswordField = "SSH Пароль"

    override val btnSave = "Сохранить"
    override val btnCancel = "Отмена"
    override val btnDelete = "Удалить"

    override val toastKeySelected = "SSH-ключ успешно выбран"
    override val toastKeyPasted = "Ключ вставлен"
    override val toastKeyReadError = "Ошибка чтения ключа"

    override val deleteServerDialogTitle = "Удалить сервер?"
    override val deleteServerDialogText: (String) -> String = { name -> "Удалить «$name» из списка?" }

    override val noPanelSelectedTitle = "Панель не выбрана"
    override val noPanelSelectedSubtitle = "Выберите панель в списке серверов"
    override val btnToServers = "К списку серверов"
    override val desktopModeTooltip = "Режим ПК"
    override val mobileModeTooltip = "Мобильный режим"
    override val reloadTooltip = "Обновить"
    override val tunnelConnectingStatus = "Подключение SSH-туннеля..."
    override val tunnelActiveBanner: (Int) -> String = { port -> "SSH-туннель активен (: $port)" }
    override val tunnelFailedStatus = "Не удалось установить SSH-туннель"
    override val btnRetryConnection = "Повторить подключение"

    override val settingsTitle = "Настройки"
    override val tabGeneral = "Общие"
    override val tabBackup = "Бэкап"
    override val tabAbout = "О программе"

    override val themeTitle = "Тема оформления"
    override val themeDark = "Темная"
    override val themeLight = "Светлая"
    override val themeSystem = "Системная"

    override val languageTitle = "Язык интерфейса"

    override val maskIpTitle = "Скрытие IP-адресов"
    override val maskIpDescription = "Заменять часть IP звездочками (194.87.***.***)"

    override val tunnelPolicyTitle = "Политика закрытия SSH-туннелей"
    override val policyPanelExitTitle = "Закрывать при выходе из панели"
    override val policyPanelExitDesc = "Туннель выключается при возврате к списку серверов"
    override val policyAppCloseTitle = "Закрывать при выходе из приложения"
    override val policyAppCloseDesc = "Туннель не обрывается при переходе между панелями"
    override val policyNeverTitle = "Не закрывать (держать в фоне)"
    override val policyNeverDesc = "Туннель работает постоянно до ручного отключения"

    override val activeTunnelCardTitle = "Активен фоновый туннель"
    override val activeTunnelCardDesc = "Проброс портов работает в фоновом режиме"
    override val btnStopTunnel = "Остановить туннель"

    override val localBackupTitle = "Локальное сохранение (JSON)"
    override val localBackupDesc = "Экспорт/импорт файла настроек всех серверов"
    override val btnSaveFile = "Сохранить"
    override val btnLoadFile = "Загрузить"

    override val webdavTitle = "Облако WebDAV"
    override val webdavDesc = "Яндекс Диск, Nextcloud, ownCloud или персональный сервер"
    override val webdavUrlField = "URL WebDAV сервера"
    override val webdavUserField = "Логин WebDAV"
    override val webdavPassField = "Пароль (токен приложения)"
    override val btnUpload = "Выгрузить"
    override val btnDownload = "Загрузить"
    override val webdavHint = "Для Яндекс Диска: https://webdav.yandex.ru и пароль приложения из Яндекс ID"

    override val versionPrefix = "Версия"
    override val btnCheckUpdates = "Проверить обновления"
    override val btnCheckingUpdates = "Проверка..."
    override val changelogTitle = "История изменений"
    override val btnGitHub = "Страница на GitHub"

    override val updateAvailableTitle = "Доступно обновление!"
    override val updateAvailableText: (String, String) -> String = { current, latest ->
        "Доступна новая версия $latest (текущая: $current). Скачать и установить сейчас?"
    }
    override val btnDownloadInstall = "Скачать и установить"
    override val btnLater = "Позже"

    override val msgServerSaved: (String) -> String = { name -> "Сервер «$name» сохранен" }
    override val msgServerDeleted: (String) -> String = { name -> "Сервер «$name» удален" }
    override val msgBackupRestored: (Int) -> String = { count -> "Восстановлено серверов: $count" }
    override val msgWebdavSuccess = "Резервная копия выгружена на WebDAV"
    override val msgUpToDate: (String) -> String = { version -> "У вас установлена последняя версия ($version)" }
    override val msgUpdateAvailable: (String) -> String = { version -> "Доступно обновление: $version!" }
}

object EnglishStrings : Strings {
    override val appName = "3x manager"
    override val sortCountFormat: (Int) -> String = { count ->
        if (count == 1) "1 server" else "$count servers"
    }
    override val sortUsage = "By usage"
    override val sortAlphabetical = "Alphabetical"
    override val sortNewest = "Newest"

    override val tunnelBannerPrefix = "Tunnel: "
    override val disconnectButton = "Disconnect"
    override val stopShort = "Stop"

    override val noServersTitle = "No saved servers"
    override val noServersSubtitle = "Tap '+' at the bottom right to add a 3x-ui panel via link"

    override val tunnelOnBadge = "Tunnel ON"
    override val autoForwardBadge = "Auto forward"
    override val sshTunnelBadge = "SSH Tunnel"
    override val sshForwardRoute: (String) -> String = { name -> "SSH Forward ➔ $name" }

    override val actionOpen = "Open"
    override val actionEdit = "Edit"
    override val actionDelete = "Delete"

    override val addServerTitle = "Add 3x-ui Server"
    override val editServerTitle = "Edit Server"
    override val labelNameField = "Server Name (Label)"
    override val labelNamePlaceholder = "local u1host"
    override val panelUrlField = "Panel URL *"
    override val panelUrlPlaceholder = "https://localhost:2370/secret/panel/"
    override val actionPaste = "Paste"
    override val panelUsernameField = "Panel Username"
    override val panelPasswordField = "Panel Password"

    override val portForwardHeader = "Port Forwarding Server"
    override val portForwardSub = "Specify intermediate SSH server (Intermediate host):"
    override val sshHostField = "SSH Server (Intermediate host) *"
    override val sshUserField = "SSH User"
    override val sshPortField = "Port"
    override val authTypeKey = "SSH Key"
    override val authTypePassword = "Password"
    override val keyAdded = "✓ SSH Key added"
    override val privateKeyPrompt = "SSH Private Key:"
    override val actionPickFile = "Pick file"
    override val keyPassphraseField = "Key Passphrase (if any)"
    override val sshPasswordField = "SSH Password"

    override val btnSave = "Save"
    override val btnCancel = "Cancel"
    override val btnDelete = "Delete"

    override val toastKeySelected = "SSH key loaded successfully"
    override val toastKeyPasted = "Key pasted"
    override val toastKeyReadError = "Error reading key"

    override val deleteServerDialogTitle = "Delete server?"
    override val deleteServerDialogText: (String) -> String = { name -> "Delete '$name' from list?" }

    override val noPanelSelectedTitle = "No panel selected"
    override val noPanelSelectedSubtitle = "Select a panel from the servers list"
    override val btnToServers = "To server list"
    override val desktopModeTooltip = "Desktop mode"
    override val mobileModeTooltip = "Mobile mode"
    override val reloadTooltip = "Reload"
    override val tunnelConnectingStatus = "Connecting SSH tunnel..."
    override val tunnelActiveBanner: (Int) -> String = { port -> "SSH tunnel active (: $port)" }
    override val tunnelFailedStatus = "Failed to establish SSH tunnel"
    override val btnRetryConnection = "Retry connection"

    override val settingsTitle = "Settings"
    override val tabGeneral = "General"
    override val tabBackup = "Backup"
    override val tabAbout = "About"

    override val themeTitle = "Theme"
    override val themeDark = "Dark"
    override val themeLight = "Light"
    override val themeSystem = "System"

    override val languageTitle = "Interface Language"

    override val maskIpTitle = "Mask IP addresses"
    override val maskIpDescription = "Replace part of IP with asterisks (194.87.***.***)"

    override val tunnelPolicyTitle = "SSH Tunnel Closing Policy"
    override val policyPanelExitTitle = "Close when leaving panel"
    override val policyPanelExitDesc = "Tunnel stops when returning to server list"
    override val policyAppCloseTitle = "Close when exiting app"
    override val policyAppCloseDesc = "Tunnel stays active when switching panels"
    override val policyNeverTitle = "Keep running in background"
    override val policyNeverDesc = "Tunnel runs continuously until manually stopped"

    override val activeTunnelCardTitle = "Background Tunnel Active"
    override val activeTunnelCardDesc = "Port forwarding is running in background"
    override val btnStopTunnel = "Stop Tunnel"

    override val localBackupTitle = "Local Backup (JSON)"
    override val localBackupDesc = "Export or import server configurations file"
    override val btnSaveFile = "Save"
    override val btnLoadFile = "Load"

    override val webdavTitle = "WebDAV Cloud"
    override val webdavDesc = "Yandex Disk, Nextcloud, ownCloud or custom server"
    override val webdavUrlField = "WebDAV Server URL"
    override val webdavUserField = "WebDAV Username"
    override val webdavPassField = "Password (app token)"
    override val btnUpload = "Upload"
    override val btnDownload = "Download"
    override val webdavHint = "For Yandex Disk: https://webdav.yandex.ru and app password from Yandex ID"

    override val versionPrefix = "Version"
    override val btnCheckUpdates = "Check for updates"
    override val btnCheckingUpdates = "Checking..."
    override val changelogTitle = "Changelog"
    override val btnGitHub = "GitHub Page"

    override val updateAvailableTitle = "Update Available!"
    override val updateAvailableText: (String, String) -> String = { current, latest ->
        "A new version $latest is available (current: $current). Download and install now?"
    }
    override val btnDownloadInstall = "Download & Install"
    override val btnLater = "Later"

    override val msgServerSaved: (String) -> String = { name -> "Server \"$name\" saved" }
    override val msgServerDeleted: (String) -> String = { name -> "Server \"$name\" deleted" }
    override val msgBackupRestored: (Int) -> String = { count -> "Restored servers: $count" }
    override val msgWebdavSuccess = "Backup uploaded to WebDAV"
    override val msgUpToDate: (String) -> String = { version -> "You have the latest version ($version)" }
    override val msgUpdateAvailable: (String) -> String = { version -> "Update available: $version!" }
}

fun getAppStrings(language: AppLanguage): Strings {
    return when (language) {
        AppLanguage.RU -> RussianStrings
        AppLanguage.EN -> EnglishStrings
        AppLanguage.SYSTEM -> {
            val systemLang = Locale.getDefault().language.lowercase()
            if (systemLang.startsWith("ru") || systemLang.startsWith("be") || systemLang.startsWith("uk") || systemLang.startsWith("kk")) {
                RussianStrings
            } else {
                EnglishStrings
            }
        }
    }
}
