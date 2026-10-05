package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ServerEntity
import com.example.data.db.TunnelConfigEntity
import com.example.data.model.InboundItem
import com.example.data.model.ServerStatusObj
import com.example.data.repository.AppRepository
import com.example.service.ActiveTunnelState
import com.example.service.NetworkUtils
import com.example.service.TunnelService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TunnelClosePolicy(val title: String, val description: String) {
    ON_PANEL_EXIT("Закрывать при выходе из панели", "Туннель выключается при возврате к списку серверов"),
    ON_APP_CLOSE("Закрывать при выходе из приложения", "Туннель не обрывается при переходе между панелями и серверами"),
    NEVER("Не закрывать (держать в фоне)", "Туннель работает постоянно в фоне до ручного отключения")
}

enum class AppThemeMode(val title: String) {
    DARK("Темная"),
    LIGHT("Светлая"),
    SYSTEM("Системная")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)
    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        fun maskIp(ipOrHost: String, mask: Boolean): String {
            if (!mask || ipOrHost.isBlank()) return ipOrHost
            val parts = ipOrHost.split(".")
            if (parts.size == 4) {
                return "${parts[0]}.${parts[1]}.***.***"
            }
            return if (ipOrHost.length > 6) {
                "${ipOrHost.take(3)}***${ipOrHost.takeLast(3)}"
            } else {
                "***"
            }
        }
    }

    private val _themeMode = MutableStateFlow(
        AppThemeMode.values().find { it.name == prefs.getString("app_theme_mode", AppThemeMode.DARK.name) }
            ?: AppThemeMode.DARK
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("app_theme_mode", mode.name).apply()
        viewModelScope.launch {
            _userMessage.emit("Тема оформления: ${mode.title}")
        }
    }

    private val _maskIp = MutableStateFlow(prefs.getBoolean("mask_ip", false))
    val maskIp: StateFlow<Boolean> = _maskIp.asStateFlow()

    fun setMaskIp(mask: Boolean) {
        _maskIp.value = mask
        prefs.edit().putBoolean("mask_ip", mask).apply()
    }

    fun toggleMaskIp() {
        setMaskIp(!_maskIp.value)
    }

    private val _closePolicy = MutableStateFlow(
        TunnelClosePolicy.values().find { it.name == prefs.getString("tunnel_close_policy", TunnelClosePolicy.ON_APP_CLOSE.name) }
            ?: TunnelClosePolicy.ON_APP_CLOSE
    )
    val closePolicy: StateFlow<TunnelClosePolicy> = _closePolicy.asStateFlow()

    fun setClosePolicy(policy: TunnelClosePolicy) {
        _closePolicy.value = policy
        prefs.edit().putString("tunnel_close_policy", policy.name).apply()
        viewModelScope.launch {
            _userMessage.emit("Режим туннеля: ${policy.title}")
        }
    }

    fun onLeavePanel(context: Context) {
        if (_closePolicy.value == TunnelClosePolicy.ON_PANEL_EXIT) {
            stopTunnel(context)
        }
    }

    fun onAppClose(context: Context) {
        if (_closePolicy.value != TunnelClosePolicy.NEVER) {
            stopTunnel(context)
        }
    }

    fun getBackupJson(): String {
        return com.example.data.backup.BackupManager.createBackupJson(
            servers = servers.value,
            tunnels = tunnels.value,
            closePolicy = closePolicy.value.name
        )
    }

    fun restoreBackup(jsonString: String, onResult: (Result<Int>) -> Unit) {
        viewModelScope.launch {
            val parseResult = com.example.data.backup.BackupManager.parseBackupJson(jsonString)
            if (parseResult.isSuccess) {
                val (restoredServers, settings) = parseResult.getOrThrow()
                var count = 0
                for (server in restoredServers) {
                    repository.saveServer(server)
                    count++
                }
                settings["tunnel_close_policy"]?.let { policyName ->
                    TunnelClosePolicy.values().find { it.name == policyName }?.let {
                        setClosePolicy(it)
                    }
                }
                _userMessage.emit("Восстановлено серверов: $count")
                onResult(Result.success(count))
            } else {
                val error = parseResult.exceptionOrNull() ?: Exception("Ошибка парсинга")
                _userMessage.emit(error.message ?: "Ошибка восстановления")
                onResult(Result.failure(error))
            }
        }
    }

    fun getSavedWebdavUrl(): String = prefs.getString("webdav_url", "") ?: ""
    fun getSavedWebdavUser(): String = prefs.getString("webdav_user", "") ?: ""
    fun getSavedWebdavPass(): String = prefs.getString("webdav_pass", "") ?: ""

    fun saveWebdavConfig(url: String, user: String, pass: String) {
        prefs.edit()
            .putString("webdav_url", url)
            .putString("webdav_user", user)
            .putString("webdav_pass", pass)
            .apply()
    }

    fun uploadBackupToWebdav(url: String, user: String, pass: String, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            saveWebdavConfig(url, user, pass)
            val json = getBackupJson()
            val res = com.example.data.backup.BackupManager.uploadToWebdav(url, user, pass, json)
            if (res.isSuccess) {
                _userMessage.emit("Резервная копия выгружена на WebDAV")
            } else {
                _userMessage.emit(res.exceptionOrNull()?.message ?: "Ошибка WebDAV")
            }
            onResult(res)
        }
    }

    fun restoreBackupFromWebdav(url: String, user: String, pass: String, onResult: (Result<Int>) -> Unit) {
        viewModelScope.launch {
            saveWebdavConfig(url, user, pass)
            val res = com.example.data.backup.BackupManager.downloadFromWebdav(url, user, pass)
            if (res.isSuccess) {
                restoreBackup(res.getOrThrow(), onResult)
            } else {
                val error = res.exceptionOrNull() ?: Exception("Не удалось загрузить с WebDAV")
                _userMessage.emit(error.message ?: "Ошибка WebDAV")
                onResult(Result.failure(error))
            }
        }
    }

    private val _googleAccount = MutableStateFlow<com.google.android.gms.auth.api.signin.GoogleSignInAccount?>(null)
    val googleAccount: StateFlow<com.google.android.gms.auth.api.signin.GoogleSignInAccount?> = _googleAccount.asStateFlow()

    fun checkGoogleAccount(context: Context) {
        _googleAccount.value = com.example.data.backup.GoogleDriveBackupManager.getLastSignedInAccount(context)
    }

    fun setGoogleAccount(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount?) {
        _googleAccount.value = account
    }

    fun uploadBackupToDrive(context: Context, onResult: (Result<String>) -> Unit) {
        val account = _googleAccount.value
        if (account == null) {
            onResult(Result.failure(Exception("Сначала подключите Google Аккаунт")))
            return
        }
        viewModelScope.launch {
            val json = getBackupJson()
            val res = com.example.data.backup.GoogleDriveBackupManager.uploadBackup(context, account, json)
            if (res.isSuccess) {
                _userMessage.emit("Бэкап синхронизирован с Google Диском")
            } else {
                _userMessage.emit(res.exceptionOrNull()?.message ?: "Ошибка Google Диска")
            }
            onResult(res)
        }
    }

    fun restoreBackupFromDrive(context: Context, onResult: (Result<Int>) -> Unit) {
        val account = _googleAccount.value
        if (account == null) {
            onResult(Result.failure(Exception("Сначала подключите Google Аккаунт")))
            return
        }
        viewModelScope.launch {
            val res = com.example.data.backup.GoogleDriveBackupManager.downloadBackup(context, account)
            if (res.isSuccess) {
                restoreBackup(res.getOrThrow(), onResult)
            } else {
                val error = res.exceptionOrNull() ?: Exception("Не удалось скачать бэкап с Google Диска")
                _userMessage.emit(error.message ?: "Ошибка Google Диска")
                onResult(Result.failure(error))
            }
        }
    }

    val servers: StateFlow<List<ServerEntity>> = repository.allServers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tunnels: StateFlow<List<TunnelConfigEntity>> = repository.allTunnels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tunnelState: StateFlow<ActiveTunnelState> = TunnelService.tunnelState

    private val _selectedServer = MutableStateFlow<ServerEntity?>(null)
    val selectedServer: StateFlow<ServerEntity?> = _selectedServer.asStateFlow()

    private val _serverStatus = MutableStateFlow<ServerStatusObj?>(null)
    val serverStatus: StateFlow<ServerStatusObj?> = _serverStatus.asStateFlow()

    private val _inbounds = MutableStateFlow<List<InboundItem>>(emptyList())
    val inbounds: StateFlow<List<InboundItem>> = _inbounds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _lanIp = MutableStateFlow("127.0.0.1")
    val lanIp: StateFlow<String> = _lanIp.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
            updateLanIp()
        }

        viewModelScope.launch {
            repository.defaultServer.collect { def ->
                if (_selectedServer.value == null && def != null) {
                    _selectedServer.value = def
                    refreshPanelData(def)
                }
            }
        }
    }

    fun updateLanIp() {
        _lanIp.value = NetworkUtils.getLocalIpAddress()
    }

    fun selectServer(server: ServerEntity, context: Context? = null) {
        _selectedServer.value = server
        if (server.useTunnel && context != null) {
            val currentTunnel = tunnelState.value
            if (!currentTunnel.isRunning || currentTunnel.localPort != server.localPort) {
                val tunnelConfig = server.toTunnelConfig()
                startTunnel(tunnelConfig, context)
            }
        }
        refreshPanelData(server)
    }

    fun startTunnelForServer(server: ServerEntity, context: Context) {
        val tunnelConfig = server.toTunnelConfig()
        startTunnel(tunnelConfig, context)
    }

    fun toggleServerTunnel(server: ServerEntity, context: Context) {
        if (tunnelState.value.isRunning && (tunnelState.value.configId == server.id || tunnelState.value.localPort == server.localPort)) {
            stopTunnel(context)
            viewModelScope.launch {
                _userMessage.emit("Туннель для «${server.name}» остановлен")
            }
        } else {
            startTunnelForServer(server, context)
            viewModelScope.launch {
                _userMessage.emit("Запуск туннеля для «${server.name}»...")
            }
        }
    }

    fun refreshPanelData(server: ServerEntity? = _selectedServer.value) {
        val target = server ?: return
        viewModelScope.launch {
            _isLoading.value = true
            updateLanIp()

            val activeTunnel = tunnelState.value
            val effectiveTunnelPort = if (activeTunnel.isRunning) activeTunnel.localPort else null

            // Ping
            val ping = repository.pingServer(target, effectiveTunnelPort)

            // Status
            val statusRes = repository.fetchServerStatus(target, effectiveTunnelPort)
            if (statusRes.isSuccess) {
                _serverStatus.value = statusRes.getOrNull()
            }

            // Inbounds
            val inboundsRes = repository.fetchInbounds(target, effectiveTunnelPort)
            if (inboundsRes.isSuccess) {
                _inbounds.value = inboundsRes.getOrNull() ?: emptyList()
            }

            _isLoading.value = false
        }
    }

    fun pingSelectedServer() {
        val server = _selectedServer.value ?: return
        viewModelScope.launch {
            val activeTunnel = tunnelState.value
            val tunnelPort = if (activeTunnel.isRunning) activeTunnel.localPort else null
            val ping = repository.pingServer(server, tunnelPort)
            if (ping >= 0) {
                _userMessage.emit("Пинг до ${server.name}: ${ping}мс")
            } else {
                _userMessage.emit("Сервер ${server.name} недоступен (таймаут)")
            }
        }
    }

    fun saveServer(server: ServerEntity) {
        viewModelScope.launch {
            val id = repository.saveServer(server)
            _userMessage.emit("Сервер \"${server.name}\" сохранен")
            if (_selectedServer.value?.id == id || _selectedServer.value == null) {
                _selectedServer.value = server.copy(id = id)
                refreshPanelData(server.copy(id = id))
            }
        }
    }

    fun deleteServer(server: ServerEntity) {
        viewModelScope.launch {
            repository.deleteServer(server)
            _userMessage.emit("Сервер \"${server.name}\" удален")
            if (_selectedServer.value?.id == server.id) {
                _selectedServer.value = servers.value.firstOrNull { it.id != server.id }
            }
        }
    }

    fun setDefaultServer(server: ServerEntity) {
        viewModelScope.launch {
            repository.setDefaultServer(server.id)
            _userMessage.emit("${server.name} установлен по умолчанию")
        }
    }

    fun saveTunnel(tunnel: TunnelConfigEntity) {
        viewModelScope.launch {
            repository.saveTunnel(tunnel)
            _userMessage.emit("Конфиг проброса \"${tunnel.name}\" сохранен")
        }
    }

    fun deleteTunnel(tunnel: TunnelConfigEntity) {
        viewModelScope.launch {
            repository.deleteTunnel(tunnel)
            _userMessage.emit("Конфиг \"${tunnel.name}\" удален")
        }
    }

    fun startTunnel(config: TunnelConfigEntity, context: Context) {
        TunnelService.start(context, config)
    }

    fun stopTunnel(context: Context) {
        TunnelService.stop(context)
    }

    private var tunnelServiceInstance: TunnelService? = null
    fun setTunnelService(service: TunnelService?) {
        tunnelServiceInstance = service
    }
    fun getTunnelServiceBinder(): TunnelService? = tunnelServiceInstance

    fun restartXray() {
        val server = _selectedServer.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val activeTunnel = tunnelState.value
            val tunnelPort = if (activeTunnel.isRunning) activeTunnel.localPort else null
            val res = repository.restartXray(server, tunnelPort)
            _isLoading.value = false
            _userMessage.emit(res.getOrNull()?.msg ?: "Служба Xray перезапущена")
            refreshPanelData(server)
        }
    }

    fun resetAllTraffics() {
        val server = _selectedServer.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val activeTunnel = tunnelState.value
            val tunnelPort = if (activeTunnel.isRunning) activeTunnel.localPort else null
            val res = repository.resetAllTraffics(server, tunnelPort)
            _isLoading.value = false
            _userMessage.emit(res.getOrNull()?.msg ?: "Статистика трафика сброшена")
            refreshPanelData(server)
        }
    }

    fun toggleInbound(inbound: InboundItem) {
        val updated = _inbounds.value.map {
            if (it.id == inbound.id) it.copy(enable = !it.enable) else it
        }
        _inbounds.value = updated
        viewModelScope.launch {
            _userMessage.emit("Подключение #${inbound.id} ${if (!inbound.enable) "включено" else "выключено"}")
        }
    }

    fun deleteInbound(inboundId: Int) {
        val server = _selectedServer.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val activeTunnel = tunnelState.value
            val tunnelPort = if (activeTunnel.isRunning) activeTunnel.localPort else null
            repository.deleteInbound(server, inboundId, tunnelPort)
            _inbounds.value = _inbounds.value.filter { it.id != inboundId }
            _isLoading.value = false
            _userMessage.emit("Подключение удалено")
        }
    }

    fun addInbound(remark: String, protocol: String, port: Int) {
        val newId = (_inbounds.value.maxOfOrNull { it.id } ?: 0) + 1
        val newItem = InboundItem(
            id = newId,
            remark = remark,
            protocol = protocol,
            port = port,
            enable = true,
            up = 0L,
            down = 0L,
            total = 0L
        )
        _inbounds.value = listOf(newItem) + _inbounds.value
        viewModelScope.launch {
            _userMessage.emit("Подключение \"$remark\" добавлено")
        }
    }
}
