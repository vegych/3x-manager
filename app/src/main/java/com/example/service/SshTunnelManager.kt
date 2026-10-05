package com.example.service

import android.util.Log
import com.example.data.db.TunnelConfigEntity
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jcraft.jsch.UIKeyboardInteractive
import com.jcraft.jsch.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class SshTunnelManager {
    private val tag = "SshTunnelManager"
    private var jsch: JSch? = null
    private var session: Session? = null

    init {
        try {
            Security.removeProvider("BC")
            Security.insertProviderAt(BouncyCastleProvider(), 1)
            Log.d(tag, "BouncyCastleProvider registered as primary security provider")
        } catch (e: Throwable) {
            try {
                Security.addProvider(BouncyCastleProvider())
            } catch (_: Throwable) {}
        }
    }

    val isRunning: Boolean
        get() = session?.isConnected == true

    suspend fun startTunnel(config: TunnelConfigEntity): Result<Int> = withContext(Dispatchers.IO) {
        stopTunnel()
        try {
            val jschInstance = JSch()
            jsch = jschInstance

            val port = if (config.sshPort > 0) config.sshPort else 22
            val user = if (config.sshUser.isNotBlank()) config.sshUser.trim() else "root"
            val host = config.sshHost.trim()

            if (host.isBlank()) {
                return@withContext Result.failure(Exception("Укажите IP или домен SSH-сервера"))
            }

            // 1. Add SSH Key identity if using KEY authentication
            val isKeyAuth = config.sshAuthType == "KEY" || (config.sshKey.isNotBlank() && config.sshPassword.isBlank())

            if (isKeyAuth && config.sshKey.isNotBlank()) {
                val rawKey = config.sshKey.replace("\r\n", "\n").replace("\r", "\n").trim()
                val formattedKey = if (rawKey.endsWith("\n")) rawKey else "$rawKey\n"
                val keyBytes = formattedKey.toByteArray(Charsets.UTF_8)
                val passphraseBytes = if (config.sshKeyPassphrase.isNotBlank()) {
                    config.sshKeyPassphrase.trim().toByteArray(Charsets.UTF_8)
                } else null

                try {
                    jschInstance.addIdentity("user_ssh_key", keyBytes, null, passphraseBytes)
                    Log.i(tag, "Added private key identity to JSch")
                } catch (e: Exception) {
                    Log.e(tag, "Failed to parse SSH key", e)
                    return@withContext Result.failure(Exception("Не удалось прочитать SSH-ключ: ${e.message}"))
                }
            }

            // 2. Create Session
            val sshSession = jschInstance.getSession(user, host, port)

            // 3. UserInfo & UIKeyboardInteractive handler (Essential for OpenSSH password & keyboard-interactive prompts)
            val userInfo = object : UserInfo, UIKeyboardInteractive {
                override fun getPassphrase(): String = config.sshKeyPassphrase
                override fun getPassword(): String = config.sshPassword
                override fun promptPassword(message: String?): Boolean = true
                override fun promptPassphrase(message: String?): Boolean = true
                override fun promptYesNo(message: String?): Boolean = true
                override fun showMessage(message: String?) {
                    Log.d(tag, "SSH Message: $message")
                }
                override fun promptKeyboardInteractive(
                    destination: String?,
                    name: String?,
                    instruction: String?,
                    prompt: Array<out String>?,
                    echo: BooleanArray?
                ): Array<String> {
                    val count = prompt?.size ?: 0
                    Log.d(tag, "SSH keyboard-interactive prompt ($count prompts received)")
                    return Array(count) { config.sshPassword }
                }
            }
            sshSession.userInfo = userInfo

            if (config.sshPassword.isNotBlank()) {
                sshSession.setPassword(config.sshPassword)
            }

            // 4. Configure Session & PreferredAuthentications
            sshSession.setConfig("StrictHostKeyChecking", "no")

            if (isKeyAuth) {
                sshSession.setConfig("PreferredAuthentications", "publickey,password,keyboard-interactive")
            } else {
                sshSession.setConfig("PreferredAuthentications", "password,keyboard-interactive")
            }

            sshSession.setConfig(
                "server_host_key",
                "ssh-ed25519,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521,rsa-sha2-512,rsa-sha2-256,ssh-rsa"
            )
            sshSession.setConfig(
                "PubkeyAcceptedAlgorithms",
                "ssh-ed25519,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521,rsa-sha2-512,rsa-sha2-256,ssh-rsa"
            )

            sshSession.serverAliveInterval = 25000
            sshSession.serverAliveCountMax = 4

            Log.i(tag, "Connecting to SSH $user@$host:$port (AuthType: ${if (isKeyAuth) "KEY" else "PASSWORD"})...")
            sshSession.connect(15000)

            // 5. Port Forwarding
            val targetHost = if (config.remoteTargetHost.isNotBlank()) config.remoteTargetHost else "127.0.0.1"
            val targetPort = if (config.remoteTargetPort > 0) config.remoteTargetPort else config.localPort
            val localPort = if (config.localPort > 0) config.localPort else targetPort

            val bindHost = if (config.bindToLan) "0.0.0.0" else "127.0.0.1"
            Log.d(tag, "Binding PortForwardingL: $bindHost:$localPort -> $targetHost:$targetPort")

            val boundPort = try {
                sshSession.setPortForwardingL(bindHost, localPort, targetHost, targetPort)
            } catch (e: Exception) {
                Log.w(tag, "Binding to $bindHost failed, falling back to localhost", e)
                sshSession.setPortForwardingL(localPort, targetHost, targetPort)
            }

            session = sshSession
            Log.i(tag, "SSH Tunnel successfully active on $bindHost:$boundPort -> $targetHost:$targetPort")
            Result.success(boundPort)
        } catch (e: JSchException) {
            val msg = e.message ?: "JSchException"
            Log.e(tag, "SSH Connection failed: $msg", e)
            stopTunnel()
            val friendlyMsg = when {
                msg.contains("Auth fail", ignoreCase = true) ->
                    "Ошибка авторизации: не подошел ${if (config.sshAuthType == "KEY") "SSH-ключ" else "пароль"} для ${config.sshUser}@${config.sshHost}"
                msg.contains("timeout", ignoreCase = true) || msg.contains("timed out", ignoreCase = true) ->
                    "Таймаут подключения к ${config.sshHost}:${config.sshPort}"
                msg.contains("Connection refused", ignoreCase = true) ->
                    "Порт SSH ${config.sshPort} закрыт на ${config.sshHost}"
                msg.contains("UnknownHost", ignoreCase = true) ->
                    "Не найден сервер ${config.sshHost}"
                msg.contains("Address already in use", ignoreCase = true) ->
                    "Порт ${config.localPort} уже занят"
                else -> "Ошибка SSH: $msg"
            }
            Result.failure(Exception(friendlyMsg))
        } catch (e: Exception) {
            Log.e(tag, "Failed to start SSH tunnel", e)
            stopTunnel()
            Result.failure(e)
        }
    }

    fun stopTunnel() {
        try {
            session?.let { s ->
                if (s.isConnected) {
                    s.disconnect()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error disconnecting SSH session", e)
        } finally {
            session = null
            jsch = null
        }
    }
}
