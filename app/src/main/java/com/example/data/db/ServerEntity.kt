package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 2053,
    val useHttps: Boolean = false,
    val basePath: String = "",
    val username: String = "",
    val password: String = "",

    // Termius-style Auto Port Forwarding & SSH Tunnel settings
    val useTunnel: Boolean = false,
    val autoConnectTunnel: Boolean = true,
    val sshHost: String = "",
    val sshPort: Int = 22,
    val sshUser: String = "root",
    val sshAuthType: String = "KEY", // "KEY" or "PASSWORD"
    val sshPassword: String = "",
    val sshKey: String = "",
    val sshKeyPassphrase: String = "",
    val remoteTargetHost: String = "127.0.0.1",
    val remoteTargetPort: Int = 2053,
    val localPort: Int = 2053,
    val bindToLan: Boolean = true,

    val tunnelId: Long? = null,
    val isDefault: Boolean = false,
    val lastPingMs: Long = -1,
    val isOnline: Boolean = false,
    val usageCount: Int = 0,
    val lastConnectedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getEffectiveUrl(tunnelLocalPort: Int? = null): String {
        val cleanPath = basePath.trim().trim('/')
        val pathPart = if (cleanPath.isNotEmpty()) "/$cleanPath" else ""
        val scheme = if (useHttps) "https" else "http"

        return if (useTunnel) {
            val effectivePort = tunnelLocalPort ?: if (localPort > 0) localPort else port
            "$scheme://localhost:$effectivePort$pathPart"
        } else {
            "$scheme://$host:$port$pathPart"
        }
    }

    fun toTunnelConfig(): TunnelConfigEntity {
        val targetPort = if (remoteTargetPort > 0) remoteTargetPort else port
        val lPort = if (localPort > 0) localPort else port
        return TunnelConfigEntity(
            id = id,
            name = "SSH Туннель: $name",
            type = "SSH",
            sshHost = sshHost.ifBlank { host },
            sshPort = sshPort,
            sshUser = sshUser.ifBlank { "root" },
            sshAuthType = sshAuthType,
            sshPassword = sshPassword,
            sshKey = sshKey,
            sshKeyPassphrase = sshKeyPassphrase,
            remoteTargetHost = remoteTargetHost.ifBlank { "127.0.0.1" },
            remoteTargetPort = targetPort,
            localPort = lPort,
            bindToLan = bindToLan
        )
    }

    companion object {
        /**
         * Parses a URL like https://localhost:2370/4zQr4STYGUC1BDIMos/panel/ or http://194.87.21.55:2053/xui/
         */
        fun parseUrl(inputUrl: String): ParsedServerUrl {
            var url = inputUrl.trim()
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = if (url.contains(":443") || url.contains("https")) "https://$url" else "http://$url"
            }

            val isHttps = url.startsWith("https://")
            val withoutScheme = url.substringAfter("://")

            val hostAndPort = withoutScheme.substringBefore("/")
            val pathPart = if (withoutScheme.contains("/")) withoutScheme.substringAfter("/") else ""

            val host: String
            val port: Int
            if (hostAndPort.contains(":")) {
                host = hostAndPort.substringBefore(":")
                port = hostAndPort.substringAfter(":").toIntOrNull() ?: if (isHttps) 443 else 80
            } else {
                host = hostAndPort
                port = if (isHttps) 443 else 80
            }

            val isLocalhost = host.equals("localhost", ignoreCase = true) ||
                    host == "127.0.0.1" ||
                    host == "0.0.0.0"

            return ParsedServerUrl(
                originalUrl = inputUrl,
                useHttps = isHttps,
                host = host,
                port = port,
                basePath = pathPart.trimEnd('/'),
                isLocalhost = isLocalhost
            )
        }
    }
}

data class ParsedServerUrl(
    val originalUrl: String,
    val useHttps: Boolean,
    val host: String,
    val port: Int,
    val basePath: String,
    val isLocalhost: Boolean
)
