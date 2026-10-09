package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.db.TunnelConfigEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

data class ActiveTunnelState(
    val isRunning: Boolean = false,
    val type: String = "SSH",
    val configId: Long = 0L,
    val tunnelName: String = "",
    val localPort: Int = 2370,
    val targetHost: String = "127.0.0.1",
    val targetPort: Int = 2370,
    val lanIp: String = "127.0.0.1",
    val statusMessage: String = "Остановлен",
    val isError: Boolean = false,
    val activeClients: Int = 0,
    val bytesRx: Long = 0L,
    val bytesTx: Long = 0L,
    val startedAt: Long = 0L
)

class TunnelService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    inner class LocalBinder : Binder() {
        fun getService(): TunnelService = this@TunnelService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_TUNNEL
        val configId = intent?.getLongExtra(EXTRA_CONFIG_ID, -1L) ?: -1L

        if (action == ACTION_STOP_ALL_TUNNELS) {
            stopTunnelInternal(null)
            stopSelf()
            return START_NOT_STICKY
        } else if (action == ACTION_STOP_TUNNEL) {
            if (configId > 0) {
                stopTunnelInternal(configId)
                if (activeTunnels.isEmpty()) {
                    stopSelf()
                }
            } else {
                stopTunnelInternal(null)
                stopSelf()
            }
            return START_NOT_STICKY
        }

        // Must call startForeground immediately on Android 8+
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification("3X-UI Туннель", "Подключение к SSH-серверу..."),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification("3X-UI Туннель", "Подключение к SSH-серверу...")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground notification", e)
        }

        val config = extractConfigFromIntent(intent)
        if (config != null) {
            startTunnelInternal(config)
        } else {
            Log.w(TAG, "No config found in intent")
        }

        return START_NOT_STICKY
    }

    private fun extractConfigFromIntent(intent: Intent?): TunnelConfigEntity? {
        if (intent == null) return null

        val sshHost = intent.getStringExtra(EXTRA_SSH_HOST) ?: return null
        val localPort = intent.getIntExtra(EXTRA_LOCAL_PORT, 2370)
        val targetPort = intent.getIntExtra(EXTRA_TARGET_PORT, localPort)
        val configId = intent.getLongExtra(EXTRA_CONFIG_ID, 0L)

        return TunnelConfigEntity(
            id = configId,
            name = intent.getStringExtra(EXTRA_CONFIG_NAME) ?: "SSH Tunnel",
            type = intent.getStringExtra(EXTRA_TYPE) ?: "SSH",
            localPort = localPort,
            bindToLan = intent.getBooleanExtra(EXTRA_BIND_TO_LAN, false),
            sshHost = sshHost,
            sshPort = intent.getIntExtra(EXTRA_SSH_PORT, 22),
            sshUser = intent.getStringExtra(EXTRA_SSH_USER) ?: "root",
            sshAuthType = intent.getStringExtra(EXTRA_SSH_AUTH_TYPE) ?: "KEY",
            sshPassword = intent.getStringExtra(EXTRA_SSH_PASSWORD) ?: "",
            sshKey = intent.getStringExtra(EXTRA_SSH_KEY) ?: "",
            sshKeyPassphrase = intent.getStringExtra(EXTRA_SSH_KEY_PASSPHRASE) ?: "",
            remoteTargetHost = intent.getStringExtra(EXTRA_TARGET_HOST) ?: "127.0.0.1",
            remoteTargetPort = targetPort,
            autoStart = true
        )
    }

    fun startTunnel(config: TunnelConfigEntity) {
        startTunnelInternal(config)
    }

    private fun startTunnelInternal(config: TunnelConfigEntity) {
        val lanIp = NetworkUtils.getLocalIpAddress()

        // Check if already active
        if (activeTunnels[config.id]?.isRunning == true && sshManager.isRunning(config.id)) {
            Log.i(TAG, "Tunnel #${config.id} is already running on port ${config.localPort}")
            return
        }

        val initState = ActiveTunnelState(
            isRunning = false,
            type = config.type,
            configId = config.id,
            tunnelName = config.name,
            localPort = config.localPort,
            targetHost = config.remoteTargetHost,
            targetPort = config.remoteTargetPort,
            lanIp = lanIp,
            statusMessage = "Подключение к ${config.sshHost}...",
            isError = false
        )
        activeTunnels[config.id] = initState
        activeStatesMap[config.id] = false
        _tunnelState.value = initState

        serviceScope.launch {
            try {
                if (config.type == "SSH") {
                    Log.i(TAG, "Starting SSH tunnel #${config.id}: ${config.sshUser}@${config.sshHost}:${config.sshPort} forward :${config.localPort} -> ${config.remoteTargetHost}:${config.remoteTargetPort}")
                    val result = sshManager.startTunnel(config)
                    if (result.isSuccess) {
                        val boundPort = result.getOrNull() ?: config.localPort
                        val activeState = initState.copy(
                            isRunning = true,
                            localPort = boundPort,
                            statusMessage = "Активен ➔ ${config.name}",
                            isError = false,
                            startedAt = System.currentTimeMillis()
                        )
                        activeTunnels[config.id] = activeState
                        activeStatesMap[config.id] = true
                        _tunnelState.value = activeState

                        val runningCount = activeTunnels.values.count { it.isRunning }
                        updateNotification("3X-UI Туннели", "Активно туннелей в фоне: $runningCount")
                        Log.i(TAG, "SSH Tunnel #${config.id} connected successfully on port $boundPort")
                    } else {
                        val errorMsg = result.exceptionOrNull()?.message ?: "Не удалось подключиться по SSH"
                        Log.e(TAG, "SSH Tunnel #${config.id} connection failed: $errorMsg")
                        val errState = initState.copy(
                            isRunning = false,
                            statusMessage = errorMsg,
                            isError = true
                        )
                        activeTunnels[config.id] = errState
                        activeStatesMap[config.id] = false
                        _tunnelState.value = errState
                        updateNotification("Ошибка проброса", errorMsg)
                    }
                } else {
                    val result = tcpRelayManager.startRelay(
                        scope = serviceScope,
                        localPort = config.localPort,
                        targetHost = config.remoteTargetHost,
                        targetPort = config.remoteTargetPort,
                        bindToLan = config.bindToLan
                    )
                    if (result.isSuccess) {
                        val activeState = initState.copy(
                            isRunning = true,
                            statusMessage = "TCP реле активно на :${config.localPort}",
                            isError = false,
                            startedAt = System.currentTimeMillis()
                        )
                        activeTunnels[config.id] = activeState
                        activeStatesMap[config.id] = true
                        _tunnelState.value = activeState
                        updateNotification("TCP Реле активно", "Порт: ${config.localPort}")
                    } else {
                        val errorMsg = result.exceptionOrNull()?.message ?: "Ошибка привязки порта"
                        val errState = initState.copy(
                            isRunning = false,
                            statusMessage = errorMsg,
                            isError = true
                        )
                        activeTunnels[config.id] = errState
                        activeStatesMap[config.id] = false
                        _tunnelState.value = errState
                        updateNotification("Ошибка TCP реле", errorMsg)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in tunnel #${config.id}", e)
                val errState = initState.copy(
                    isRunning = false,
                    statusMessage = e.message ?: "Ошибка подключения",
                    isError = true
                )
                activeTunnels[config.id] = errState
                activeStatesMap[config.id] = false
                _tunnelState.value = errState
                updateNotification("Ошибка", e.message ?: "Ошибка подключения")
            }
        }
    }

    fun stopTunnel(configId: Long? = null) {
        stopTunnelInternal(configId)
    }

    private fun stopTunnelInternal(configId: Long? = null) {
        stopTunnelInternalStatic(configId)
        if (activeTunnels.isEmpty()) {
            try {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } catch (_: Exception) {}
        } else {
            val runningCount = activeTunnels.values.count { it.isRunning }
            updateNotification("3X-UI Туннели", "Активно туннелей в фоне: $runningCount")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "3X-UI Проброс портов",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Статус фоновых SSH-туннелей"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, text: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        try {
            val notification = buildNotification(title, text)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {}
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val policyName = prefs.getString("tunnel_close_policy", com.example.ui.viewmodel.TunnelClosePolicy.ON_APP_CLOSE.name)
        if (policyName != com.example.ui.viewmodel.TunnelClosePolicy.NEVER.name) {
            Log.i(TAG, "Task removed by user with policy $policyName. Stopping all tunnels.")
            stopTunnelInternal(null)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTunnelInternal(null)
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "TunnelService"
        const val CHANNEL_ID = "tunnel_service_channel"
        const val NOTIFICATION_ID = 2053

        const val ACTION_START_TUNNEL = "com.example.service.START_TUNNEL"
        const val ACTION_STOP_TUNNEL = "com.example.service.STOP_TUNNEL"
        const val ACTION_STOP_ALL_TUNNELS = "com.example.service.STOP_ALL_TUNNELS"

        const val EXTRA_CONFIG_ID = "extra_config_id"
        const val EXTRA_CONFIG_NAME = "extra_config_name"
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_LOCAL_PORT = "extra_local_port"
        const val EXTRA_BIND_TO_LAN = "extra_bind_to_lan"
        const val EXTRA_SSH_HOST = "extra_ssh_host"
        const val EXTRA_SSH_PORT = "extra_ssh_port"
        const val EXTRA_SSH_USER = "extra_ssh_user"
        const val EXTRA_SSH_AUTH_TYPE = "extra_ssh_auth_type"
        const val EXTRA_SSH_PASSWORD = "extra_ssh_password"
        const val EXTRA_SSH_KEY = "extra_ssh_key"
        const val EXTRA_SSH_KEY_PASSPHRASE = "extra_ssh_key_passphrase"
        const val EXTRA_TARGET_HOST = "extra_target_host"
        const val EXTRA_TARGET_PORT = "extra_target_port"

        private val sshManager = SshTunnelManager()
        private val tcpRelayManager = TcpRelayManager()

        private val activeTunnels = ConcurrentHashMap<Long, ActiveTunnelState>()
        private val activeStatesMap = ConcurrentHashMap<Long, Boolean>()

        private val _tunnelState = MutableStateFlow(ActiveTunnelState())
        val tunnelState: StateFlow<ActiveTunnelState> = _tunnelState.asStateFlow()

        private val _tunnelStoppedEvent = MutableSharedFlow<Long?>(extraBufferCapacity = 32)
        val tunnelStoppedEvent: SharedFlow<Long?> = _tunnelStoppedEvent.asSharedFlow()

        fun isTunnelRunning(configId: Long): Boolean {
            return activeTunnels[configId]?.isRunning == true
        }

        fun getTunnelState(configId: Long): ActiveTunnelState? {
            return activeTunnels[configId]
        }

        fun stopTunnelInternalStatic(configId: Long? = null) {
            if (configId != null && configId > 0) {
                sshManager.stopTunnel(configId)
                activeTunnels.remove(configId)
                activeStatesMap.remove(configId)
                _tunnelStoppedEvent.tryEmit(configId)
                Log.i(TAG, "Stopped tunnel for configId $configId")
            } else {
                sshManager.stopAllTunnels()
                tcpRelayManager.stopRelay()
                activeTunnels.clear()
                activeStatesMap.clear()
                _tunnelStoppedEvent.tryEmit(null)
                Log.i(TAG, "Stopped all tunnels")
            }

            if (activeTunnels.isEmpty()) {
                _tunnelState.value = ActiveTunnelState(
                    isRunning = false,
                    statusMessage = "Остановлен",
                    isError = false,
                    activeClients = 0
                )
            } else {
                val lastActive = activeTunnels.values.lastOrNull { it.isRunning } ?: activeTunnels.values.last()
                _tunnelState.value = lastActive
            }
        }

        fun start(context: Context, config: TunnelConfigEntity) {
            val intent = Intent(context, TunnelService::class.java).apply {
                action = ACTION_START_TUNNEL
                putExtra(EXTRA_CONFIG_ID, config.id)
                putExtra(EXTRA_CONFIG_NAME, config.name)
                putExtra(EXTRA_TYPE, config.type)
                putExtra(EXTRA_LOCAL_PORT, config.localPort)
                putExtra(EXTRA_BIND_TO_LAN, config.bindToLan)
                putExtra(EXTRA_SSH_HOST, config.sshHost)
                putExtra(EXTRA_SSH_PORT, config.sshPort)
                putExtra(EXTRA_SSH_USER, config.sshUser)
                putExtra(EXTRA_SSH_AUTH_TYPE, config.sshAuthType)
                putExtra(EXTRA_SSH_PASSWORD, config.sshPassword)
                putExtra(EXTRA_SSH_KEY, config.sshKey)
                putExtra(EXTRA_SSH_KEY_PASSPHRASE, config.sshKeyPassphrase)
                putExtra(EXTRA_TARGET_HOST, config.remoteTargetHost)
                putExtra(EXTRA_TARGET_PORT, config.remoteTargetPort)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start TunnelService", e)
            }
        }

        fun stop(context: Context, configId: Long? = null) {
            stopTunnelInternalStatic(configId)
            val intent = Intent(context, TunnelService::class.java).apply {
                action = if (configId != null && configId > 0) ACTION_STOP_TUNNEL else ACTION_STOP_ALL_TUNNELS
                if (configId != null) putExtra(EXTRA_CONFIG_ID, configId)
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }

        fun stopAll(context: Context) {
            stopTunnelInternalStatic(null)
            val intent = Intent(context, TunnelService::class.java).apply {
                action = ACTION_STOP_ALL_TUNNELS
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }
}
