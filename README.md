# 3x manager 🚀

<div align="center">

[![Language: English](https://img.shields.io/badge/Language-English-blue.svg)](#-english)
[![Language: Русский](https://img.shields.io/badge/Language-Русский-red.svg)](#-russian)
[![Platform: Android](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-green.svg)](https://developer.android.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(M3)-purple.svg)](https://developer.android.com/jetpack/compose)

**Modern, ultra-fast, and secure Android client for managing 3x-ui (Xray/V2Ray) panels with built-in automated SSH tunneling.**

[English](#-english) • [Русский](#-russian)

</div>

---

<a name="english"></a>
## 🇬🇧 English

### Overview
**3x manager** is an open-source Android application designed for system administrators and power users managing **3x-ui** (X-UI / Xray / V2Ray) VPN panels. 

It is tailored for hardened server setups where the 3x-ui web panel is bound strictly to `127.0.0.1` (localhost) and hidden behind SSH for maximum security. **3x manager** automatically establishes encrypted SSH port forwarding tunnels, connects to the web panel through an optimized embedded browser, and performs seamless one-click automated authentication.

---

### ✨ Key Features

#### 🛡️ Automated SSH Tunneling & Port Forwarding
- **Localhost Panel Access**: Connect to panels bound to `127.0.0.1` without exposing panel ports to the public Internet.
- **SSH Key & Password Authentication**: Full support for OpenSSH RSA, ECDSA, ED25519 private keys (with or without passphrases) as well as password authentication.
- **Intelligent Tunnel Lifecycles**:
  - *Close on panel exit*: Disconnects as soon as you return to the server list.
  - *Close on app exit*: Keeps the tunnel alive while navigating between panels and closes on app dismiss.
  - *Keep in background*: Runs a continuous background tunnel until manually disconnected.
- **Android Foreground Service**: Prevents system termination and maintains stable connection state.

#### 🌐 Built-in 3x-ui Web Client
- **One-Click Auto-Login**: Automatically fills credentials and signs into the 3x-ui dashboard.
- **Desktop (1280px) & Mobile Modes**: Switch instantly between full desktop PC layout (with side navigation, charts, and table views) and mobile layout.
- **Optimized WebView Engine**: Fast page load times, session cookie persistence, and smooth gesture navigation.

#### 👁️ Privacy & IP Masking
- **One-Tap IP Concealment**: Instantly mask server IPs (e.g., `194.87.***.***`) in the top app bar to safely share screenshots or use in public.
- **Zero-Distraction UI**: No intrusive toast popups when toggling privacy mode.

#### ☁️ Backup & Cloud Sync
- **WebDAV Cloud Backup**: Sync server configurations with Nextcloud, ownCloud, Yandex Disk, or personal WebDAV servers.
- **Local JSON Export/Import**: Backup and restore configuration files locally or share via Android system share sheet.

#### ⚡ High Performance & Ergonomics
- **60 / 120 FPS Fluid Scrolling**: Optimized Jetpack Compose lazy lists with zero layout lag and fast rendering.
- **Multi-language Support**: Full English and Russian interface with auto system locale detection.
- **Material Design 3**: Dynamic themes (Dark, Light, System) with smooth transitions.

#### 🔄 Auto-Updater
- **In-App GitHub Releases Updates**: Check for new releases directly within the app and install updates with 1 tap.

---

### 🛠️ Tech Stack
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose & Material 3
- **Architecture**: MVVM + Clean Architecture, Coroutines & StateFlow
- **Database**: Room (SQLite) with KSP
- **SSH Engine**: JSch (Java Secure Channel) + Java NIO TCP Forwarder
- **Networking**: Android WebView, CookieManager, OkHttp 4 (WebDAV client)

---

### 🚀 Building from Source

#### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17 or JDK 21
- Android SDK 36 (Min SDK 24 — Android 7.0+)

#### Build Commands
```bash
# Clone the repository
git clone https://github.com/your-username/3x-manager.git
cd 3x-manager

# Build Release APK
./gradlew assembleRelease

# Run Unit Tests
./gradlew testDebugUnitTest
```
The resulting APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

---

<a name="russian"></a>
## 🇷🇺 Русский

### Описание
**3x manager** — современное, сверхбыстрое и безопасное Android-приложение для управления серверами и веб-панелями **3x-ui** (X-UI / Xray / V2Ray) с встроенным автоматическим SSH-туннелированием.

Разработано специально для конфигураций с повышенными требованиями к безопасности, когда панель 3x-ui привязана исключительно к `127.0.0.1` (localhost) на удаленном сервере и закрыта от внешней сети. Приложение автоматически поднимает защищенный SSH-туннель (проброс портов), открывает панель во встроенном оптимизированном браузере и автоматически авторизуется в ней.

---

### ✨ Ключевые возможности

#### 🛡️ Безопасность и SSH-туннелирование
- **Доступ к локальным панелям (127.0.0.1)**: Подключение к панелям без необходимости открывать порт панели в глобальный интернет.
- **Поддержка ключей и паролей**: Аутентификация по SSH Private Key (RSA, ECDSA, ED25519 с поддержкой passphrase) и паролю.
- **Интеллектуальное управление туннелем**:
  - *Закрывать при выходе из панели* — туннель выключается при возврате к списку серверов.
  - *Закрывать при выходе из приложения* — туннель не обрывается при переходе между панелями.
  - *Держать в фоне* — туннель работает постоянно до ручного отключения.
- **Foreground Service**: Стабильная работа SSH-туннеля в фоне без выгрузки системой Android.

#### 🌐 Встроенный веб-клиент 3x-ui
- **Авторизация в один клик (Auto-login)**: Автоматическая подстановка логина и пароля в форму входа 3x-ui.
- **Режимы ПК (1280px) и Мобильный**:
  - Полноэкранный режим ПК (1280px) с развернутым левым сайдбаром, верхним меню и графиками нагрузки.
  - Адаптивный мобильный режим для небольших экранов.
- **Умная навигация**: Поддержка системных жестов «Назад», быстрое обновление страницы и безопасное управление сессионными Cookie.

#### 👁️ Приватность (Скрытие IP)
- Быстрое скрытие IP-адресов серверов в 1 клик через иконку в шапке приложения (например, `194.87.***.***` для защиты от посторонних глаз и скриншотов).
- Бесшумное переключение без всплывающих уведомлений.

#### ☁️ Резервное копирование и синхронизация
- **Облако WebDAV**: Поддержка Яндекс Диска, Nextcloud, ownCloud или персональных серверов.
- **Локальный файл JSON**: Экспорт и импорт бэкапов в память устройства или через системное меню «Поделиться».

#### ⚡ Высокая производительность и дизайн
- **Плавный скролл 60 / 120 FPS**: Оптимизированный рендеринг списков Jetpack Compose без задержек.
- **Мультиязычность**: Полная поддержка русского и английского языков с возможностью выбора в настройках.
- **Material Design 3**: Поддержка тем (Темная, Светлая, Системная).

#### 🔄 Автообновление
- Проверка новых релизов с GitHub Releases прямо в приложении и установка в 1 клик.

---

### 🛠️ Стек технологий
- **Язык**: Kotlin 2.0+
- **UI**: Jetpack Compose, Material 3
- **Архитектура**: MVVM, Clean Architecture, StateFlow, Coroutines
- **База данных**: Room (SQLite) + KSP
- **SSH Туннель**: JSch (Java Secure Channel) + Java NIO TCP Forwarder
- **Сеть / Web**: Android WebView, CookieManager, OkHttp 4 (WebDAV)

---

### 🚀 Сборка проекта

#### Требования
- Android Studio Ladybug / Meerkat или новее
- JDK 17 или JDK 21
- Android SDK 36 (Min SDK 24 — Android 7.0+)

#### Команды Gradle
```bash
# Клонировать репозиторий
git clone https://github.com/your-username/3x-manager.git
cd 3x-manager

# Сборка релизного APK
./gradlew assembleRelease

# Запуск тестов
./gradlew testDebugUnitTest
```

---

## 📄 License / Лицензия
Distributed under the [MIT License](LICENSE). / Распространяется под лицензией [MIT](LICENSE).
