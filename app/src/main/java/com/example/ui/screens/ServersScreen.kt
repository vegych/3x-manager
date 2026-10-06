package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ServerEntity
import com.example.service.ActiveTunnelState
import com.example.ui.components.TermiusBadgeL
import com.example.ui.i18n.RussianStrings
import com.example.ui.i18n.Strings
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.RedAccent
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun ServersScreen(
    servers: List<ServerEntity>,
    selectedServer: ServerEntity?,
    tunnelState: ActiveTunnelState? = null,
    maskIp: Boolean = false,
    sortMode: com.example.ui.viewmodel.ServerSortMode = com.example.ui.viewmodel.ServerSortMode.BY_USAGE,
    strings: Strings = RussianStrings,
    onSortModeChange: (com.example.ui.viewmodel.ServerSortMode) -> Unit = {},
    onStopTunnel: (() -> Unit)? = null,
    onSelectServer: (ServerEntity) -> Unit,
    onSaveServer: (ServerEntity) -> Unit,
    onDeleteServer: (ServerEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingServer by remember { mutableStateOf<ServerEntity?>(null) }
    var serverToDelete by remember { mutableStateOf<ServerEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header_sort") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.sortCountFormat(servers.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Сортировка: По частоте / По алфавиту / Новые
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        com.example.ui.viewmodel.ServerSortMode.values().forEach { mode ->
                            val isSelected = sortMode == mode
                            val modeTitle = when (mode) {
                                com.example.ui.viewmodel.ServerSortMode.BY_USAGE -> strings.sortUsage
                                com.example.ui.viewmodel.ServerSortMode.ALPHABETICAL -> strings.sortAlphabetical
                                com.example.ui.viewmodel.ServerSortMode.NEWEST -> strings.sortNewest
                            }
                            Surface(
                                onClick = { onSortModeChange(mode) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CyanPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary) else null
                            ) {
                                Text(
                                    text = modeTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Компактная плашка активного туннеля с кнопкой отключения
            if (tunnelState?.isRunning == true) {
                item(key = "active_tunnel_banner", contentType = "tunnel_banner") {
                    val activeServerName = remember(servers, tunnelState, selectedServer) {
                        val activeServer = servers.firstOrNull { it.id == tunnelState.configId || it.localPort == tunnelState.localPort } ?: selectedServer
                        activeServer?.name ?: "Active"
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RedAccent.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedAccent.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MintSecondary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${strings.tunnelBannerPrefix}$activeServerName",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = { onStopTunnel?.invoke() },
                                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.disconnectButton, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            if (servers.isEmpty()) {
                item(key = "empty_servers", contentType = "empty") {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp)
                        ) {
                            Text(
                                text = strings.noServersTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = strings.noServersSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(
                items = servers,
                key = { it.id },
                contentType = { "server_card" }
            ) { server ->
                val isSelected = selectedServer?.id == server.id
                val isTunnelActive = tunnelState?.isRunning == true &&
                        (tunnelState.configId == server.id || tunnelState.localPort == server.localPort)

                ServerCardSimple(
                    server = server,
                    isSelected = isSelected,
                    isTunnelActive = isTunnelActive,
                    maskIp = maskIp,
                    strings = strings,
                    onSelect = { onSelectServer(server) },
                    onEdit = {
                        editingServer = server
                        showDialog = true
                    },
                    onDelete = { serverToDelete = server }
                )
            }

            item(key = "bottom_spacer", contentType = "spacer") {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        // Floating Action Button "+" in bottom-right
        FloatingActionButton(
            onClick = {
                editingServer = null
                showDialog = true
            },
            containerColor = CyanPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_server_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.addServerTitle, modifier = Modifier.size(28.dp))
        }
    }

    if (showDialog) {
        ServerEditDialogClean(
            initialServer = editingServer,
            strings = strings,
            onDismiss = { showDialog = false },
            onSave = {
                onSaveServer(it)
                showDialog = false
            }
        )
    }

    serverToDelete?.let { server ->
        AlertDialog(
            onDismissRequest = { serverToDelete = null },
            title = { Text(strings.deleteServerDialogTitle) },
            text = { Text(strings.deleteServerDialogText(server.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteServer(server)
                        serverToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text(strings.btnDelete)
                }
            },
            dismissButton = {
                TextButton(onClick = { serverToDelete = null }) {
                    Text(strings.btnCancel)
                }
            }
        )
    }
}

@Composable
fun ServerCardSimple(
    server: ServerEntity,
    isSelected: Boolean,
    isTunnelActive: Boolean,
    maskIp: Boolean = false,
    strings: Strings = RussianStrings,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    val displaySshHost = remember(server.sshHost, maskIp) {
        com.example.ui.viewmodel.MainViewModel.maskIp(server.sshHost, maskIp)
    }
    val displayEffectiveUrl = remember(server, maskIp) {
        if (server.useTunnel) {
            strings.sshForwardRoute(server.name)
        } else {
            val maskedHost = com.example.ui.viewmodel.MainViewModel.maskIp(server.host, maskIp)
            val scheme = if (server.useHttps) "https" else "http"
            val path = if (server.basePath.isNotBlank()) "/${server.basePath.trim('/')}" else ""
            "$scheme://$maskedHost:${server.port}$path"
        }
    }

    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_card_${server.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Name (Label) and Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (server.useTunnel) {
                        TermiusBadgeL()
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Column {
                        Text(
                            text = server.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = displayEffectiveUrl,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (server.useTunnel) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTunnelActive) MintSecondary.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.15f),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = if (isTunnelActive) strings.tunnelOnBadge else strings.autoForwardBadge,
                            color = if (isTunnelActive) MintSecondary else AmberAccent,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Route summary (clean intermediate host without raw ports)
            if (server.useTunnel && server.sshHost.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = strings.sshTunnelBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanPrimary
                        )
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${server.sshUser}@$displaySshHost",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.actionOpen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = strings.actionEdit, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = strings.actionDelete, tint = RedAccent, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/**
 * Termius-inspired Clean Add/Edit Dialog:
 * 1. Название (Label)
 * 2. Ссылка на панель (URL)
 * 3. Логин от нее
 * 4. Пароль от нее
 * 5. Если localhost -> блок сервера для проброса портов (SSH хост, порт, юзер, SSH Key / Пароль)
 */
@Composable
fun ServerEditDialogClean(
    initialServer: ServerEntity?,
    strings: Strings = RussianStrings,
    onDismiss: () -> Unit,
    onSave: (ServerEntity) -> Unit
) {
    val context = LocalContext.current

    // Line 1: Название (Label)
    var labelName by remember {
        mutableStateOf(initialServer?.name ?: "")
    }

    // Line 2: Ссылка на панель (URL)
    var panelUrl by remember {
        mutableStateOf(
            initialServer?.let {
                val scheme = if (it.useHttps) "https" else "http"
                val basePath = if (it.basePath.isNotBlank()) "/${it.basePath.trim('/')}" else ""
                "$scheme://${it.host}:${it.port}$basePath"
            } ?: ""
        )
    }

    // Line 3: Логин от нее
    var panelUsername by remember { mutableStateOf(initialServer?.username ?: "") }

    // Line 4: Пароль от нее
    var panelPassword by remember { mutableStateOf(initialServer?.password ?: "") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Detected localhost
    val isLocalhost = panelUrl.contains("localhost", ignoreCase = true) ||
            panelUrl.contains("127.0.0.1")

    // SSH Port Forwarding Server Fields (shown ONLY if URL contains localhost)
    var sshHost by remember { mutableStateOf(initialServer?.sshHost ?: "") }
    var sshPortText by remember { mutableStateOf(initialServer?.let { if (it.sshPort == 22) "" else it.sshPort.toString() } ?: "") }
    var sshUser by remember { mutableStateOf(initialServer?.let { if (it.sshUser == "root") "" else it.sshUser } ?: "") }
    var sshAuthType by remember { mutableStateOf(initialServer?.sshAuthType ?: "KEY") } // "KEY" or "PASSWORD"
    var sshPassword by remember { mutableStateOf(initialServer?.sshPassword ?: "") }
    var isSshPasswordVisible by remember { mutableStateOf(false) }
    var sshKey by remember { mutableStateOf(initialServer?.sshKey ?: "") }
    var sshKeyPassphrase by remember { mutableStateOf(initialServer?.sshKeyPassphrase ?: "") }

    // File picker for SSH Key
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val keyContent = reader.readText()
                    sshKey = keyContent.trim()
                    sshAuthType = "KEY"
                    Toast.makeText(context, strings.toastKeySelected, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "${strings.toastKeyReadError}: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialServer == null) strings.addServerTitle else strings.editServerTitle,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                letterSpacing = (-0.5).sp
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Строка 1: Название (Label)
                item {
                    OutlinedTextField(
                        value = labelName,
                        onValueChange = { labelName = it },
                        label = { Text(strings.labelNameField) },
                        placeholder = { Text(strings.labelNamePlaceholder) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_name_input")
                    )
                }

                // Строка 2: Ссылка на панель
                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = panelUrl,
                                onValueChange = {
                                    panelUrl = it
                                    if (labelName.isBlank() && it.isNotBlank()) {
                                        val parsed = ServerEntity.parseUrl(it)
                                        labelName = if (parsed.isLocalhost) "local 3x-ui (${parsed.port})" else "3x-ui (${parsed.host})"
                                    }
                                },
                                label = { Text(strings.panelUrlField) },
                                placeholder = {
                                    Text(
                                        strings.panelUrlPlaceholder,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("panel_url_input")
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                    if (!clip.isNullOrBlank()) {
                                        panelUrl = clip.trim()
                                        if (labelName.isBlank()) {
                                            val parsed = ServerEntity.parseUrl(panelUrl)
                                            labelName = if (parsed.isLocalhost) "local 3x-ui (${parsed.port})" else "3x-ui (${parsed.host})"
                                        }
                                    }
                                },
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = strings.actionPaste, tint = CyanPrimary)
                            }
                        }
                    }
                }

                // Строка 3: Логин от панели
                item {
                    OutlinedTextField(
                        value = panelUsername,
                        onValueChange = { panelUsername = it },
                        label = { Text(strings.panelUsernameField) },
                        placeholder = {
                            Text(
                                "admin",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("panel_username_input")
                    )
                }

                // Строка 4: Пароль от панели
                item {
                    OutlinedTextField(
                        value = panelPassword,
                        onValueChange = { panelPassword = it },
                        label = { Text(strings.panelPasswordField) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("panel_password_input")
                    )
                }

                // Строка 5: ЕСЛИ ссылка содержит localhost -> показывает строки для добавления сервера для проброса портов
                if (isLocalhost) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CyanPrimary.copy(alpha = 0.07f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CyanPrimary.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TermiusBadgeL()
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = strings.portForwardHeader,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanPrimary
                                        )
                                        Text(
                                            text = strings.portForwardSub,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // SSH Сервер (Intermediate host)
                                OutlinedTextField(
                                    value = sshHost,
                                    onValueChange = { sshHost = it },
                                    label = { Text(strings.sshHostField) },
                                    placeholder = {
                                        Text(
                                            "vps.example.com",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ssh_host_input")
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    // SSH Пользователь
                                    OutlinedTextField(
                                        value = sshUser,
                                        onValueChange = { sshUser = it },
                                        label = { Text(strings.sshUserField) },
                                        placeholder = {
                                            Text(
                                                "root",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(2f)
                                    )

                                    // SSH Порт
                                    OutlinedTextField(
                                        value = sshPortText,
                                        onValueChange = { sshPortText = it },
                                        label = { Text(strings.sshPortField) },
                                        placeholder = {
                                            Text(
                                                "22",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Авторизация: SSH Key или Пароль
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { sshAuthType = "KEY" },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (sshAuthType == "KEY") CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            strings.authTypeKey,
                                            color = if (sshAuthType == "KEY") Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Button(
                                        onClick = { sshAuthType = "PASSWORD" },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (sshAuthType == "PASSWORD") CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            strings.authTypePassword,
                                            color = if (sshAuthType == "PASSWORD") Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                if (sshAuthType == "KEY") {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = if (sshKey.isNotBlank()) strings.keyAdded else strings.privateKeyPrompt,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (sshKey.isNotBlank()) MintSecondary else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        // Wide, clean action buttons: Вставить / Выбрать файл (no vertical wrapping)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                                    if (!clip.isNullOrBlank()) {
                                                        sshKey = clip.trim()
                                                        Toast.makeText(context, strings.toastKeyPasted, Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                            ) {
                                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(strings.actionPaste, fontSize = 12.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { filePickerLauncher.launch("*/*") },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                            ) {
                                                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(strings.actionPickFile, fontSize = 12.sp)
                                            }
                                        }

                                        OutlinedTextField(
                                            value = sshKey,
                                            onValueChange = { sshKey = it },
                                            placeholder = { Text("-----BEGIN OPENSSH PRIVATE KEY-----\n...") },
                                            maxLines = 3,
                                            shape = RoundedCornerShape(12.dp),
                                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        OutlinedTextField(
                                            value = sshKeyPassphrase,
                                            onValueChange = { sshKeyPassphrase = it },
                                            label = { Text(strings.keyPassphraseField) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            visualTransformation = PasswordVisualTransformation(),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    OutlinedTextField(
                                        value = sshPassword,
                                        onValueChange = { sshPassword = it },
                                        label = { Text(strings.sshPasswordField) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        visualTransformation = if (isSshPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { isSshPasswordVisible = !isSshPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isSshPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = ServerEntity.parseUrl(panelUrl)
                    val sPort = sshPortText.toIntOrNull() ?: 22

                    val finalName = if (labelName.isNotBlank()) {
                        labelName.trim()
                    } else if (isLocalhost) {
                        if (sshHost.isNotBlank()) "local ($sshHost)" else "local 3x-ui (${parsed.port})"
                    } else {
                        "3x-ui (${parsed.host})"
                    }

                    val server = (initialServer ?: ServerEntity(name = finalName, host = parsed.host)).copy(
                        name = finalName,
                        host = parsed.host,
                        port = parsed.port,
                        useHttps = parsed.useHttps,
                        basePath = parsed.basePath,
                        username = panelUsername.trim(),
                        password = panelPassword,
                        useTunnel = isLocalhost,
                        autoConnectTunnel = isLocalhost,
                        sshHost = if (isLocalhost) sshHost.trim() else "",
                        sshPort = sPort,
                        sshUser = sshUser.trim().ifBlank { "root" },
                        sshAuthType = sshAuthType,
                        sshPassword = sshPassword,
                        sshKey = sshKey.trim(),
                        sshKeyPassphrase = sshKeyPassphrase.trim(),
                        remoteTargetHost = "127.0.0.1",
                        remoteTargetPort = parsed.port,
                        localPort = parsed.port,
                        bindToLan = true
                    )
                    onSave(server)
                },
                enabled = panelUrl.isNotBlank() && (!isLocalhost || sshHost.isNotBlank()),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_server_button")
            ) {
                Text(strings.btnSave)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.btnCancel)
            }
        }
    )
}
