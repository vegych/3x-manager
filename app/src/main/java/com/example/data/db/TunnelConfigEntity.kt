package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tunnels")
data class TunnelConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "SSH", // "SSH" or "TCP_RELAY"
    val sshHost: String = "",
    val sshPort: Int = 22,
    val sshUser: String = "root",
    val sshAuthType: String = "KEY", // "KEY" or "PASSWORD"
    val sshPassword: String = "",
    val sshKey: String = "",
    val sshKeyPassphrase: String = "",
    val remoteTargetHost: String = "127.0.0.1",
    val remoteTargetPort: Int = 2053,
    val localPort: Int = 8080,
    val bindToLan: Boolean = true, // true -> 0.0.0.0 (LAN accessible), false -> 127.0.0.1
    val autoStart: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
