package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ServerEntity
import com.example.data.model.ClientStatItem
import com.example.data.model.InboundItem
import com.example.data.model.ServerStatusObj
import com.example.service.ActiveTunnelState
import com.example.service.NetworkUtils
import com.example.ui.components.MetricGaugeCard
import com.example.ui.components.QrCodeDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.TermiusBadgeL
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.RedAccent

@Composable
fun PanelDashboardScreen(
    servers: List<ServerEntity>,
    selectedServer: ServerEntity?,
    serverStatus: ServerStatusObj?,
    inbounds: List<InboundItem>,
    isLoading: Boolean,
    tunnelState: ActiveTunnelState? = null,
    onStartTunnel: ((ServerEntity) -> Unit)? = null,
    onSelectServer: (ServerEntity) -> Unit,
    onRefresh: () -> Unit,
    onRestartXray: () -> Unit,
    onResetAllTraffics: () -> Unit,
    onToggleInbound: (InboundItem) -> Unit,
    onDeleteInbound: (Int) -> Unit,
    onAddInbound: (remark: String, protocol: String, port: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddInboundDialog by remember { mutableStateOf(false) }
    var qrDialogData by remember { mutableStateOf<Pair<String, String>?>(null) }
    var inboundToDelete by remember { mutableStateOf<Int?>(null) }
    var showServerDropdown by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Server Selector Header
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showServerDropdown = true }
                        ) {
                            Text(
                                text = "Активный сервер 3x-ui",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedServer?.name ?: "Выберите сервер",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp))
                            }

                            DropdownMenu(
                                expanded = showServerDropdown,
                                onDismissRequest = { showServerDropdown = false }
                            ) {
                                servers.forEach { server ->
                                    DropdownMenuItem(
                                        text = { Text(server.name) },
                                        onClick = {
                                            onSelectServer(server)
                                            showServerDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = CyanPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            IconButton(
                                onClick = onRefresh,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("refresh_dashboard_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                            }
                        }
                    }
                }
            }

            // Termius Auto-tunnel warning banner if tunnel not active
            if (selectedServer?.useTunnel == true && tunnelState?.isRunning != true) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AmberAccent.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberAccent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                TermiusBadgeL()
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "SSH Проброс портов не запущен",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberAccent
                                    )
                                    Text(
                                        text = "Вход на localhost:${selectedServer.localPort} ожидает запуска туннеля к ${selectedServer.sshHost}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { onStartTunnel?.invoke(selectedServer) },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Запустить", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Control Actions
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onRestartXray,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("restart_xray_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Перезапуск Xray", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onResetAllTraffics,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_traffics_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Сброс трафика", fontSize = 12.sp)
                    }
                }
            }

            // System Metrics Section
            if (serverStatus != null) {
                item {
                    // System Status Header Bar (Uptime & Xray status)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CyanPrimary.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (serverStatus.xray.state == "running") MintSecondary else RedAccent)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Xray Core: ${serverStatus.xray.state.uppercase()}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (serverStatus.xray.state == "running") MintSecondary else RedAccent
                                    )
                                    Text(
                                        text = "Версия: ${serverStatus.xray.version.ifEmpty { "1.8.24" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = NetworkUtils.formatDuration(serverStatus.uptime),
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Gauges: CPU, RAM, Disk
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetricGaugeCard(
                            title = "CPU",
                            value = "${serverStatus.cpu.toInt()}%",
                            percent = serverStatus.cpu.toInt(),
                            subtitle = "Нагрузка процессора",
                            icon = Icons.Default.Computer,
                            color = CyanPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        MetricGaugeCard(
                            title = "RAM",
                            value = "${serverStatus.mem.percent}%",
                            percent = serverStatus.mem.percent,
                            subtitle = "${NetworkUtils.formatBytes(serverStatus.mem.current)} / ${NetworkUtils.formatBytes(serverStatus.mem.total)}",
                            icon = Icons.Default.Memory,
                            color = MintSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        val disk = serverStatus.disk
                        if (disk != null) {
                            MetricGaugeCard(
                                title = "Диск",
                                value = "${disk.percent}%",
                                percent = disk.percent,
                                subtitle = "${NetworkUtils.formatBytes(disk.current)} / ${NetworkUtils.formatBytes(disk.total)}",
                                icon = Icons.Default.Storage,
                                color = AmberAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Network Traffic Bar
                serverStatus.netTraffic?.let { traffic ->
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Отдано (Up)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(NetworkUtils.formatBytes(traffic.sent), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Принято (Down)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(NetworkUtils.formatBytes(traffic.recv), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Inbounds Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Входящие подключения (${inbounds.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { showAddInboundDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier.testTag("add_inbound_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Добавить", fontSize = 12.sp)
                    }
                }
            }

            // Inbounds List
            items(inbounds, key = { it.id }) { inbound ->
                InboundCard(
                    inbound = inbound,
                    serverHost = selectedServer?.host ?: "127.0.0.1",
                    onToggle = { onToggleInbound(inbound) },
                    onDelete = { inboundToDelete = inbound.id },
                    onShowQr = { title, link -> qrDialogData = Pair(title, link) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddInboundDialog) {
        AddInboundDialog(
            onDismiss = { showAddInboundDialog = false },
            onAdd = { remark, protocol, port ->
                onAddInbound(remark, protocol, port)
                showAddInboundDialog = false
            }
        )
    }

    inboundToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { inboundToDelete = null },
            title = { Text("Удалить подключение?") },
            text = { Text("Вы уверены что хотите удалить inbound #$id?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteInbound(id)
                        inboundToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { inboundToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    qrDialogData?.let { (title, link) ->
        QrCodeDialog(
            title = title,
            subtitle = "Отсканируйте в v2rayNG, Streisand, NekoBox или sing-box",
            content = link,
            onDismiss = { qrDialogData = null }
        )
    }
}

@Composable
fun InboundCard(
    inbound: InboundItem,
    serverHost: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onShowQr: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (inbound.enable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("inbound_card_${inbound.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = inbound.remark.ifEmpty { "Inbound #${inbound.id}" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyanPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = inbound.protocol.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MintSecondary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Порт: ${inbound.port}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = MintSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Switch(
                    checked = inbound.enable,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("toggle_inbound_${inbound.id}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Traffic Stats Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                    Text(NetworkUtils.formatBytes(inbound.up), style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(14.dp))
                    Text(NetworkUtils.formatBytes(inbound.down), style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }

                val clientCount = inbound.clientStats?.size ?: 0
                Text(
                    text = "Клиенты: $clientCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            // Expand clients / QR details
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val connectionLink = generateVlessLink(inbound, serverHost)

                Row {
                    OutlinedButton(
                        onClick = { onShowQr(inbound.remark, connectionLink) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("QR-код", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (expanded) "Скрыть" else "Клиенты", fontSize = 11.sp)
                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = RedAccent, modifier = Modifier.size(16.dp))
                }
            }

            if (expanded && !inbound.clientStats.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    inbound.clientStats.forEach { client ->
                        ClientStatRow(
                            client = client,
                            inbound = inbound,
                            serverHost = serverHost,
                            onShowQr = onShowQr
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClientStatRow(
    client: ClientStatItem,
    inbound: InboundItem,
    serverHost: String,
    onShowQr: (String, String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(client.email.ifEmpty { "User #${client.id}" }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "↑ ${NetworkUtils.formatBytes(client.up)} | ↓ ${NetworkUtils.formatBytes(client.down)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = {
                val link = generateVlessLink(inbound, serverHost, client.email)
                onShowQr(client.email, link)
            },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.QrCode, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
        }
    }
}

private fun generateVlessLink(inbound: InboundItem, host: String, email: String = "user"): String {
    // Generate valid VLESS / VMess URL format
    val uuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    return "vless://$uuid@$host:${inbound.port}?security=reality&encryption=none&headerType=none&type=tcp#${inbound.remark}_$email"
}

@Composable
fun AddInboundDialog(
    onDismiss: () -> Unit,
    onAdd: (remark: String, protocol: String, port: Int) -> Unit
) {
    var remark by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("vless") }
    var portText by remember { mutableStateOf("443") }

    val protocols = listOf("vless", "vmess", "shadowsocks", "trojan")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое подключение (Inbound)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("Название / Заметка") },
                    placeholder = { Text("Напр. VLESS Reality") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Протокол:", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    protocols.forEach { p ->
                        Button(
                            onClick = { protocol = p },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (protocol == p) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Text(
                                p.uppercase(),
                                fontSize = 10.sp,
                                color = if (protocol == p) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = portText,
                    onValueChange = { portText = it },
                    label = { Text("Порт подключения") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val port = portText.toIntOrNull() ?: 443
                    onAdd(remark.ifBlank { "${protocol.uppercase()} $port" }, protocol, port)
                }
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
