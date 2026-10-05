package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TunnelConfigEntity
import com.example.service.ActiveTunnelState
import com.example.service.NetworkUtils
import com.example.ui.components.CopyableLinkCard
import com.example.ui.components.QrCodeDialog
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.RedAccent

@Composable
fun PortForwardScreen(
    tunnelState: ActiveTunnelState,
    tunnels: List<TunnelConfigEntity>,
    lanIp: String,
    onStartTunnel: (TunnelConfigEntity, Context) -> Unit,
    onStopTunnel: (Context) -> Unit,
    onSaveTunnel: (TunnelConfigEntity) -> Unit,
    onDeleteTunnel: (TunnelConfigEntity) -> Unit,
    onOpenWebPanel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var editingTunnel by remember { mutableStateOf<TunnelConfigEntity?>(null) }
    var tunnelToDelete by remember { mutableStateOf<TunnelConfigEntity?>(null) }
    var qrDialogUrl by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Main Bridge Status Card
            TunnelHeroCard(
                tunnelState = tunnelState,
                lanIp = lanIp,
                onToggle = {
                    if (tunnelState.isRunning) {
                        onStopTunnel(context)
                    } else {
                        val activeConfig = tunnels.firstOrNull { it.id == tunnelState.configId }
                            ?: tunnels.firstOrNull()
                        if (activeConfig != null) {
                            onStartTunnel(activeConfig, context)
                        } else {
                            editingTunnel = null
                            showEditDialog = true
                        }
                    }
                }
            )
        }

        // LAN Access Links (Visible if tunnel running or prepared)
        item {
            val localPort = if (tunnelState.isRunning) tunnelState.localPort else (tunnels.firstOrNull()?.localPort ?: 8080)
            val lanUrl = "http://$lanIp:$localPort"
            val localUrl = "http://127.0.0.1:$localPort"

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MintSecondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lan,
                                contentDescription = null,
                                tint = MintSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Доступ из локальной сети (LAN)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Откройте ссылку на ПК/ноутбуке в одной сети Wi-Fi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    CopyableLinkCard(
                        title = "Ссылка для локальной сети (Wi-Fi / LAN)",
                        url = lanUrl,
                        description = "Любое устройство в домашней сети может зайти на панель 3x-ui по этой ссылке",
                        onShowQr = { qrDialogUrl = lanUrl }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    CopyableLinkCard(
                        title = "Локальная ссылка (на этом телефоне)",
                        url = localUrl,
                        description = "Прямой доступ через localhost для этого устройства",
                        onShowQr = { qrDialogUrl = localUrl }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onOpenWebPanel(localUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_web_panel_button")
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Открыть панель во встроенном веб-браузере")
                    }
                }
            }
        }

        // Configurations header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Конфигурации проброса (${tunnels.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = {
                        editingTunnel = null
                        showEditDialog = true
                    },
                    modifier = Modifier.testTag("add_tunnel_config_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Создать", fontSize = 13.sp)
                }
            }
        }

        // Configurations List
        items(tunnels, key = { it.id }) { tunnel ->
            val isCurrentActive = tunnelState.isRunning && tunnelState.configId == tunnel.id
            TunnelConfigCard(
                tunnel = tunnel,
                isActive = isCurrentActive,
                onStart = { onStartTunnel(tunnel, context) },
                onStop = { onStopTunnel(context) },
                onEdit = {
                    editingTunnel = tunnel
                    showEditDialog = true
                },
                onDelete = { tunnelToDelete = tunnel }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showEditDialog) {
        TunnelEditDialog(
            initialConfig = editingTunnel,
            onDismiss = { showEditDialog = false },
            onSave = {
                onSaveTunnel(it)
                showEditDialog = false
            }
        )
    }

    tunnelToDelete?.let { config ->
        AlertDialog(
            onDismissRequest = { tunnelToDelete = null },
            title = { Text("Удалить конфигурацию?") },
            text = { Text("Удалить \"${config.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTunnel(config)
                        tunnelToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { tunnelToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    qrDialogUrl?.let { url ->
        QrCodeDialog(
            title = "LAN доступ к 3x-ui",
            subtitle = "Наведите камеру смартфона или планшета в вашей сети Wi-Fi",
            content = url,
            onDismiss = { qrDialogUrl = null }
        )
    }
}

@Composable
fun TunnelHeroCard(
    tunnelState: ActiveTunnelState,
    lanIp: String,
    onToggle: () -> Unit
) {
    val statusColor = when {
        tunnelState.isRunning -> MintSecondary
        tunnelState.isError -> RedAccent
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tunnelState.isRunning) CyanPrimary.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                2.dp,
                if (tunnelState.isRunning) MintSecondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (tunnelState.isRunning) "ПРОБРОС ПОРТОВ АКТИВЕН" else "МОСТ ОСТАНОВЛЕН",
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tunnelState.isRunning) RedAccent else MintSecondary
                    ),
                    modifier = Modifier.testTag("toggle_tunnel_button")
                ) {
                    Icon(
                        imageVector = if (tunnelState.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (tunnelState.isRunning) "Остановить" else "Запустить")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Routing diagram: [0.0.0.0:Port] ---> [Remote VPS:Port]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Вход из локалки (LAN)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "0.0.0.0:${tunnelState.localPort}",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Text(
                        text = "$lanIp:${tunnelState.localPort}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Панель на сервере",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${tunnelState.targetHost}:${tunnelState.targetPort}",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = MintSecondary
                    )
                    Text(
                        text = tunnelState.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status message
            Text(
                text = tunnelState.statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (tunnelState.isError) RedAccent else MaterialTheme.colorScheme.onSurface
            )

            if (tunnelState.isRunning) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Клиенты: ${tunnelState.activeClients}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (tunnelState.bytesRx > 0 || tunnelState.bytesTx > 0) {
                        Text(
                            text = "Rx: ${NetworkUtils.formatBytes(tunnelState.bytesRx)} | Tx: ${NetworkUtils.formatBytes(tunnelState.bytesTx)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TunnelConfigCard(
    tunnel: TunnelConfigEntity,
    isActive: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) CyanPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isActive) CyanPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .testTag("tunnel_card_${tunnel.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (tunnel.type == "SSH") CyanPrimary.copy(alpha = 0.2f)
                                else AmberAccent.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (tunnel.type == "SSH") Icons.Default.Security else Icons.Default.AltRoute,
                            contentDescription = null,
                            tint = if (tunnel.type == "SSH") CyanPrimary else AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = tunnel.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (tunnel.type == "SSH") "SSH Туннель"
                            else "Локальный TCP Реле",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                if (isActive) {
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Стоп", fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onStart,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Старт", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Forward detail
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Маршрут: :${tunnel.localPort} ➔ ${tunnel.remoteTargetHost}:${tunnel.remoteTargetPort}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    softWrap = true
                )
                Text(
                    text = if (tunnel.bindToLan) "Доступен в локальной сети (0.0.0.0)" else "Только на этом устройстве (127.0.0.1)",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanPrimary,
                    fontSize = 11.sp,
                    softWrap = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = RedAccent, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun TunnelEditDialog(
    initialConfig: TunnelConfigEntity?,
    onDismiss: () -> Unit,
    onSave: (TunnelConfigEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialConfig?.name ?: "") }
    var type by remember { mutableStateOf(initialConfig?.type ?: "SSH") }
    var sshHost by remember { mutableStateOf(initialConfig?.sshHost ?: "") }
    var sshPortText by remember { mutableStateOf(initialConfig?.sshPort?.toString() ?: "22") }
    var sshUser by remember { mutableStateOf(initialConfig?.sshUser ?: "root") }
    var sshPassword by remember { mutableStateOf(initialConfig?.sshPassword ?: "") }
    var sshKey by remember { mutableStateOf(initialConfig?.sshKey ?: "") }
    var remoteHost by remember { mutableStateOf(initialConfig?.remoteTargetHost ?: "127.0.0.1") }
    var remotePortText by remember { mutableStateOf(initialConfig?.remoteTargetPort?.toString() ?: "2053") }
    var localPortText by remember { mutableStateOf(initialConfig?.localPort?.toString() ?: "8080") }
    var bindToLan by remember { mutableStateOf(initialConfig?.bindToLan ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialConfig == null) "Создать проброс портов" else "Редактировать проброс")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название конфигурации") },
                        placeholder = { Text("Напр. SSH Туннель 3x-ui") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Тип проброса:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { type = "SSH" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == "SSH") CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SSH Туннель", color = if (type == "SSH") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                        }

                        Button(
                            onClick = { type = "TCP_RELAY" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == "TCP_RELAY") CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("TCP Реле", color = if (type == "TCP_RELAY") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                if (type == "SSH") {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = sshHost,
                                onValueChange = { sshHost = it },
                                label = { Text("SSH Хост (IP VPS)") },
                                singleLine = true,
                                modifier = Modifier.weight(2f)
                            )
                            OutlinedTextField(
                                value = sshPortText,
                                onValueChange = { sshPortText = it },
                                label = { Text("SSH Порт") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = sshUser,
                            onValueChange = { sshUser = it },
                            label = { Text("SSH Пользователь") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = sshPassword,
                            onValueChange = { sshPassword = it },
                            label = { Text("SSH Пароль") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = sshKey,
                            onValueChange = { sshKey = it },
                            label = { Text("SSH Private Key (опционально)") },
                            placeholder = { Text("-----BEGIN OPENSSH PRIVATE KEY-----") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Text("Направление перенаправления:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = remoteHost,
                            onValueChange = { remoteHost = it },
                            label = { Text("Целевой хост") },
                            placeholder = { Text("127.0.0.1") },
                            singleLine = true,
                            modifier = Modifier.weight(2f)
                        )
                        OutlinedTextField(
                            value = remotePortText,
                            onValueChange = { remotePortText = it },
                            label = { Text("Порт 3x-ui") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = localPortText,
                        onValueChange = { localPortText = it },
                        label = { Text("Локальный порт прослушивания") },
                        placeholder = { Text("8080") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Доступ из локальной сети (0.0.0.0)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Позволяет компьютерам и смартфонам в вашей Wi-Fi сети заходить на панель", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = bindToLan, onCheckedChange = { bindToLan = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sPort = sshPortText.toIntOrNull() ?: 22
                    val rPort = remotePortText.toIntOrNull() ?: 2053
                    val lPort = localPortText.toIntOrNull() ?: 8080

                    val newConfig = (initialConfig ?: TunnelConfigEntity(name = name)).copy(
                        name = if (name.isBlank()) "Туннель :$lPort" else name,
                        type = type,
                        sshHost = sshHost.trim(),
                        sshPort = sPort,
                        sshUser = sshUser.trim(),
                        sshPassword = sshPassword,
                        sshKey = sshKey.trim(),
                        remoteTargetHost = remoteHost.trim(),
                        remoteTargetPort = rPort,
                        localPort = lPort,
                        bindToLan = bindToLan
                    )
                    onSave(newConfig)
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
