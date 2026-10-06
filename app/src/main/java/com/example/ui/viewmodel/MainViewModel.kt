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
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.i18n.getAppStrings
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

enum class ServerSortMode(val title: String) {
    BY_USAGE("По частоте"),
    ALPHABETICAL("По алфавиту"),
    NEWEST("По дате")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)
    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _appLanguage = MutableStateFlow(
        AppLanguage.values().find { it.name == prefs.getString("app_language", AppLanguage.SYSTEM.name) }
            ?: AppLanguage.SYSTEM
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _strings = MutableStateFlow(getAppStrings(_appLanguage.value))
    val strings: StateFlow<Strings> = _strings.asStateFlow()

    fun setAppLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
        _strings.value = getAppStrings(lang)
        prefs.edit().putString("app_language", lang.name).apply()
    }

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

    private val _sortMode = MutableStateFlow(
        ServerSortMode.values().find { it.name == prefs.getString("server_sort_mode", ServerSortMode.BY_USAGE.name) }
            ?: ServerSortMode.BY_USAGE
    )
    val sortMode: StateFlow<ServerSortMode> = _sortMode.asStateFlow()

    fun setSortMode(mode: ServerSortMode) {
        _sortMode.value = mode
        prefs.edit().putString("server_sort_mode", mode.name).apply()
    }

    val servers: StateFlow<List<ServerEntity>> = kotlinx.coroutines.flow.combine(
        repository.allServers,
        _sortMode
    ) { list, sort ->
        when (sort) {
            ServerSortMode.BY_USAGE -> list.sortedWith(
                compareByDescending<ServerEntity> { it.usageCount }
                    .thenByDescending { it.lastConnectedAt }
                    .thenBy { it.name.lowercase() }
            )
            ServerSortMode.ALPHABETICAL -> list.sortedBy { it.name.lowercase() }
            ServerSortMode.NEWEST -> list.sortedByDescending { it.createdAt }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tunnels: StateFlow<List<TunnelConfigEntity>> = repository.allTunnels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tunnelState: StateFlow<ActiveTunnelState> = TunnelService.tunnelState

    private val _selectedServer = MutableStateFlow<ServerEntity?>(null)
    val selectedServer: StateFlow<ServerEntity?> = _selectedServer.asStateFlow()

    private val _openedServers = MutableStateFlow<List<ServerEntity>>(emptyList())
    val openedServers: StateFlow<List<ServerEntity>> = _openedServers.asStateFlow()

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

    private val _updateInfo = MutableStateFlow<com.example.data.updater.UpdateInfo?>(null)
    val updateInfo: StateFlow<com.example.data.updater.UpdateInfo?> = _updateInfo.asStateFlow()

    private val _panelUpdateInfo = MutableStateFlow<com.example.data.updater.PanelUpdateInfo?>(null)
    val panelUpdateInfo: StateFlow<com.example.data.updater.PanelUpdateInfo?> = _panelUpdateInfo.asStateFlow()

    private val _isCheckingPanelUpdate = MutableStateFlow(false)
    val isCheckingPanelUpdate: StateFlow<Boolean> = _isCheckingPanelUpdate.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _isDownloadingUpdate = MutableStateFlow(false)
    val isDownloadingUpdate: StateFlow<Boolean> = _isDownloadingUpdate.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
            updateLanIp()
            kotlinx.coroutines.delay(2000)
            checkForAppUpdates(silent = true)
            _selectedServer.value?.let { checkPanelUpdates(it) }
        }

        viewModelScope.launch {
            repository.defaultServer.collect { def ->
                if (_selectedServer.value == null && def != null) {
                    _selectedServer.value = def
                    refreshPanelData(def)
                    checkPanelUpdates(def)
                }
            }
        }
    }

    fun updateLanIp() {
        _lanIp.value = NetworkUtils.getLocalIpAddress()
    }

    fun checkPanelUpdates(server: ServerEntity? = _selectedServer.value, onComplete: ((Boolean) -> Unit)? = null) {
        val target = server ?: return
        viewModelScope.launch {
            _isCheckingPanelUpdate.value = true
            val activeTunnel = tunnelState.value
            val effectiveTunnelPort = if (activeTunnel.isRunning && (activeTunnel.configId == target.id || activeTunnel.localPort == target.localPort)) {
                activeTunnel.localPort
            } else null

            val info = com.example.data.updater.PanelUpdateManager.checkPanelUpdates(target, effectiveTunnelPort)
            _panelUpdateInfo.value = info
            _isCheckingPanelUpdate.value = false
            onComplete?.invoke(info.hasUpdate)
        }
    }

    fun onPanelVersionDetected(server: ServerEntity, detectedVersion: String) {
        viewModelScope.launch {
            val info = com.example.data.updater.PanelUpdateManager.onPanelVersionDetected(server, detectedVersion)
            _panelUpdateInfo.value = info
        }
    }

    fun dismissPanelUpdate() {
        _panelUpdateInfo.value = null
    }

    fun checkForAppUpdates(silent: Boolean = false, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            val res = com.example.data.updater.AppUpdateManager.checkForUpdates()
            _isCheckingUpdate.value = false
            if (res.isSuccess) {
                val info = res.getOrThrow()
                _updateInfo.value = info
                onComplete?.invoke(info.hasUpdate)
            } else {
                onComplete?.invoke(false)
            }
        }
    }

    fun dismissUpdate() {
        _updateInfo.value = null
        _isDownloadingUpdate.value = false
        _downloadProgress.value = null
    }

    fun startInAppUpdate(context: Context, downloadUrl: String, versionName: String) {
        viewModelScope.launch {
            _isDownloadingUpdate.value = true
            _downloadProgress.value = 0f
            val res = com.example.data.updater.AppUpdateManager.downloadAndInstallApk(
                context = context,
                downloadUrl = downloadUrl,
                versionName = versionName,
                onProgress = { progress ->
                    _downloadProgress.value = progress
                }
            )
            _isDownloadingUpdate.value = false
            if (res.isFailure) {
                _downloadProgress.value = null
                // Fallback to opening the browser on error
                com.example.data.updater.AppUpdateManager.openBrowser(context, downloadUrl)
            }
        }
    }

    fun selectServer(server: ServerEntity, context: Context? = null) {
        _selectedServer.value = server
        // Add to opened servers list if not present or update it
        val currentList = _openedServers.value
        val exists = currentList.any { it.id == server.id }
        if (!exists) {
            _openedServers.value = currentList + server
        } else {
            _openedServers.value = currentList.map { if (it.id == server.id) server else it }
        }

        viewModelScope.launch {
            repository.recordServerUsage(server.id)
        }
        if (server.useTunnel && context != null) {
            val currentTunnel = tunnelState.value
            if (!currentTunnel.isRunning || currentTunnel.localPort != server.localPort) {
                val tunnelConfig = server.toTunnelConfig()
                startTunnel(tunnelConfig, context)
            }
        }
        refreshPanelData(server)
    }

    fun openServerTab(server: ServerEntity, context: Context? = null) {
        selectServer(server, context)
    }

    fun closeServerTab(server: ServerEntity, context: Context? = null) {
        val remaining = _openedServers.value.filter { it.id != server.id }
        _openedServers.value = remaining
        if (_selectedServer.value?.id == server.id) {
            if (remaining.isNotEmpty()) {
                val nextServer = remaining.last()
                selectServer(nextServer, context)
            } else {
                _selectedServer.value = null
                if (context != null && _closePolicy.value != TunnelClosePolicy.NEVER) {
                    stopTunnel(context)
                }
            }
        }
    }

    fun closeAllServerTabs(context: Context? = null) {
        _openedServers.value = emptyList()
        _selectedServer.value = null
        if (context != null && _closePolicy.value != TunnelClosePolicy.NEVER) {
            stopTunnel(context)
        }
    }

    fun startTunnelForServer(server: ServerEntity, context: Context) {
        val tunnelConfig = server.toTunnelConfig()
        startTunnel(tunnelConfig, context)
    }

    fun toggleServerTunnel(server: ServerEntity, context: Context) {
        if (tunnelState.value.isRunning && (tunnelState.value.configId == server.id || tunnelState.value.localPort == server.localPort)) {
            stopTunnel(context)
        } else {
            startTunnelForServer(server, context)
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

            // Check 3x-ui panel version updates
            checkPanelUpdates(target)

            _isLoading.value = false
        }
    }

    fun pingSelectedServer() {
        val server = _selectedServer.value ?: return
        viewModelScope.launch {
            val activeTunnel = tunnelState.value
            val tunnelPort = if (activeTunnel.isRunning) activeTunnel.localPort else null
            repository.pingServer(server, tunnelPort)
        }
    }

    fun saveServer(server: ServerEntity) {
        viewModelScope.launch {
            val id = repository.saveServer(server)
            if (_selectedServer.value?.id == id || _selectedServer.value == null) {
                _selectedServer.value = server.copy(id = id)
                refreshPanelData(server.copy(id = id))
            }
        }
    }

    fun deleteServer(server: ServerEntity) {
        viewModelScope.launch {
            repository.deleteServer(server)
            if (_selectedServer.value?.id == server.id) {
                _selectedServer.value = servers.value.firstOrNull { it.id != server.id }
            }
        }
    }

    fun setDefaultServer(server: ServerEntity) {
        viewModelScope.launch {
            repository.setDefaultServer(server.id)
        }
    }

    fun saveTunnel(tunnel: TunnelConfigEntity) {
        viewModelScope.launch {
            repository.saveTunnel(tunnel)
        }
    }

    fun deleteTunnel(tunnel: TunnelConfigEntity) {
        viewModelScope.launch {
            repository.deleteTunnel(tunnel)
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
            refreshPanelData(server)
        }
    }

    fun toggleInbound(inbound: InboundItem) {
        val updated = _inbounds.value.map {
            if (it.id == inbound.id) it.copy(enable = !it.enable) else it
        }
        _inbounds.value = updated
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
    }
}
