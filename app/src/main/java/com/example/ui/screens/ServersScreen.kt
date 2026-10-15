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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.billing.BillingManager
import com.example.data.billing.BillingType
import kotlinx.coroutines.launch
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
    onRefreshBilling: ((ServerEntity) -> Unit)? = null,
    onExtendBilling: ((ServerEntity) -> Unit)? = null,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                val isTunnelActive = com.example.service.TunnelService.isTunnelRunning(server.id) ||
                        (tunnelState?.isRunning == true && (tunnelState.configId == server.id || tunnelState.localPort == server.localPort))

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
                    onDelete = { serverToDelete = server },
                    onRefreshBilling = { onRefreshBilling?.invoke(server) },
                    onExtendBilling = { onExtendBilling?.invoke(server) }
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
    onDelete: () -> Unit,
    onRefreshBilling: (() -> Unit)? = null,
    onExtendBilling: (() -> Unit)? = null
) {
    val borderColor = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    val displayHost = remember(server.host, maskIp) {
        com.example.ui.viewmodel.MainViewModel.maskIp(server.host, maskIp)
    }
    val displaySshHost = remember(server.sshHost, server.host, maskIp) {
        val target = if (server.sshHost.isNotBlank()) server.sshHost else server.host
        com.example.ui.viewmodel.MainViewModel.maskIp(target, maskIp)
    }
    val sshUser = if (server.sshUser.isNotBlank()) server.sshUser else "root"

    val subtitleText = if (server.useTunnel) {
        "$sshUser@$displaySshHost"
    } else {
        displayHost
    }

    val billingStatus = remember(server) {
        BillingManager.calculateStatus(server)
    }

    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_card_${server.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (server.useTunnel) {
                    TermiusBadgeL()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = server.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = (-0.3).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (server.useTunnel && isTunnelActive) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MintSecondary.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = strings.tunnelOnBadge,
                                    color = MintSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (billingStatus.type != BillingType.NONE && billingStatus.badgeText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val badgeBg = when {
                            billingStatus.isExpired -> RedAccent.copy(alpha = 0.16f)
                            billingStatus.isUrgent -> AmberAccent.copy(alpha = 0.20f)
                            else -> CyanPrimary.copy(alpha = 0.14f)
                        }
                        val badgeColor = when {
                            billingStatus.isExpired -> RedAccent
                            billingStatus.isUrgent -> AmberAccent
                            else -> CyanPrimary
                        }
                        val isApi = billingStatus.type == BillingType.TIMEWEB_CLOUD ||
                                billingStatus.type == BillingType.SELECTEL ||
                                billingStatus.type == BillingType.BEGET

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeBg,
                                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable(enabled = isApi && onRefreshBilling != null) {
                                    onRefreshBilling?.invoke()
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isApi) Icons.Default.AccountBalanceWallet else Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = billingStatus.badgeText,
                                        color = badgeColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.5.sp
                                    )
                                    if (isApi) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = strings.billingRefreshTooltip,
                                            tint = badgeColor.copy(alpha = 0.75f),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }

                            // Кнопка «Продлить» для серверов с фиксированной датой/интервалом
                            if (!isApi && onExtendBilling != null) {
                                val extendLabel = if (billingStatus.type == BillingType.MONTHLY_DAY) {
                                    "+1 мес"
                                } else {
                                    "+${if (server.billingIntervalDays > 0) server.billingIntervalDays else 30} дн."
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MintSecondary.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, MintSecondary.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable { onExtendBilling.invoke() }
                                        .testTag("extend_server_${server.id}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Продлить",
                                            tint = MintSecondary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = extendLabel,
                                            color = MintSecondary,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = strings.actionEdit, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = strings.actionDelete, tint = RedAccent, modifier = Modifier.size(16.dp))
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

    // Billing / Renewal State
    var billingType by remember {
        mutableStateOf(
            try {
                BillingType.valueOf(initialServer?.billingType ?: "NONE")
            } catch (_: Exception) {
                BillingType.NONE
            }
        )
    }
    var billingDayOfMonthText by remember {
        mutableStateOf((initialServer?.billingDayOfMonth ?: 4).toString())
    }
    var billingIntervalDaysText by remember {
        mutableStateOf((if (initialServer?.billingIntervalDays != null && initialServer.billingIntervalDays > 0) initialServer.billingIntervalDays else 30).toString())
    }
    var billingApiKey by remember {
        mutableStateOf(initialServer?.billingApiKey ?: "")
    }
    var billingCostMonthlyText by remember {
        mutableStateOf(initialServer?.let { if (it.billingCostMonthly > 0.0) it.billingCostMonthly.toString() else "" } ?: "")
    }
    var billingLastPaidDate by remember {
        mutableStateOf(initialServer?.billingLastPaidDate ?: 0L)
    }

    // Live API Test State inside Dialog
    val coroutineScope = rememberCoroutineScope()
    var isCheckingApi by remember { mutableStateOf(false) }
    var apiTestResult by remember { mutableStateOf<com.example.data.billing.BillingFetchResult?>(null) }

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

                // Строка 6: Оплата и продление сервера (Billing & Renewal)
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = strings.billingSectionHeader,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanPrimary
                                    )
                                    Text(
                                        text = strings.billingSectionSub,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Выбор типа: [Откл] [Число мес.] [Интервал] [Timeweb] [Selectel]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    BillingType.NONE to strings.billingTypeNone,
                                    BillingType.MONTHLY_DAY to strings.billingTypeMonthlyDay,
                                    BillingType.INTERVAL_DAYS to strings.billingTypeInterval,
                                    BillingType.TIMEWEB_CLOUD to "Timeweb",
                                    BillingType.SELECTEL to "Selectel",
                                    BillingType.BEGET to "Beget"
                                ).forEach { (type, label) ->
                                    val isSelected = billingType == type
                                    Surface(
                                        onClick = {
                                            billingType = type
                                            apiTestResult = null
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) CyanPrimary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                        border = if (isSelected) BorderStroke(1.dp, CyanPrimary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                                        )
                                    }
                                }
                            }

                            when (billingType) {
                                BillingType.NONE -> {
                                    Text(
                                        text = "Отслеживание оплаты отключено для этого сервера.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                BillingType.MONTHLY_DAY -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = billingDayOfMonthText,
                                            onValueChange = { billingDayOfMonthText = it.filter { ch -> ch.isDigit() }.take(2) },
                                            label = { Text(strings.billingDayLabel) },
                                            placeholder = { Text("4") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        // Quick Day Presets
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Быстрый выбор:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            listOf(1, 4, 10, 15, 20, 25, 30).forEach { day ->
                                                val isSelected = billingDayOfMonthText == day.toString()
                                                Surface(
                                                    onClick = { billingDayOfMonthText = day.toString() },
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                                    border = if (isSelected) BorderStroke(1.dp, CyanPrimary) else null
                                                ) {
                                                    Text(
                                                        text = day.toString(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 11.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = strings.billingDayHint,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        val dayVal = billingDayOfMonthText.toIntOrNull()?.coerceIn(1, 31) ?: 4
                                        val tempServer = remember(dayVal, billingLastPaidDate) {
                                            (initialServer ?: ServerEntity(name = "Preview", host = "127.0.0.1")).copy(
                                                billingType = BillingType.MONTHLY_DAY.name,
                                                billingDayOfMonth = dayVal,
                                                billingLastPaidDate = billingLastPaidDate
                                            )
                                        }
                                        val nextDue = remember(tempServer) { BillingManager.getNextDueDate(tempServer) }
                                        val nextDueStr = remember(nextDue) { BillingManager.fullDateFormat.format(nextDue.time) }
                                        val tempStatus = remember(tempServer) { BillingManager.calculateStatus(tempServer) }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = CyanPrimary.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.35f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "Следующая оплата: $nextDueStr",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = CyanPrimary
                                                        )
                                                        Text(
                                                            text = "Осталось: ${tempStatus.daysLeft ?: 0} дн.",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Button(
                                                        onClick = {
                                                            val nextMs = BillingManager.addPeriod(tempServer, billingLastPaidDate)
                                                            billingLastPaidDate = nextMs
                                                            Toast.makeText(context, "Оплата продлена на 1 месяц!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = MintSecondary, contentColor = Color.Black)
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text("+1 мес", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                if (billingLastPaidDate > 0L) {
                                                    TextButton(
                                                        onClick = {
                                                            billingLastPaidDate = 0L
                                                            Toast.makeText(context, "Сброшено к авто-расчету", Toast.LENGTH_SHORT).show()
                                                        },
                                                        contentPadding = PaddingValues(0.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text("Сбросить к текущей дате", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                BillingType.INTERVAL_DAYS -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = billingIntervalDaysText,
                                            onValueChange = { billingIntervalDaysText = it.filter { ch -> ch.isDigit() }.take(3) },
                                            label = { Text(strings.billingIntervalLabel) },
                                            placeholder = { Text("30") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        // Quick Presets 30, 31, 60, 90, 365
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            listOf(30, 31, 60, 90, 365).forEach { days ->
                                                val isSelected = billingIntervalDaysText == days.toString()
                                                Surface(
                                                    onClick = { billingIntervalDaysText = days.toString() },
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                                    border = if (isSelected) BorderStroke(1.dp, CyanPrimary) else null
                                                ) {
                                                    Text(
                                                        text = "$days дн.",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 11.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = strings.billingIntervalHint,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        val intervalVal = billingIntervalDaysText.toIntOrNull()?.coerceAtLeast(1) ?: 30
                                        val tempIntervalServer = remember(intervalVal, billingLastPaidDate) {
                                            (initialServer ?: ServerEntity(name = "Preview", host = "127.0.0.1")).copy(
                                                billingType = BillingType.INTERVAL_DAYS.name,
                                                billingIntervalDays = intervalVal,
                                                billingLastPaidDate = billingLastPaidDate
                                            )
                                        }
                                        val nextDueInterval = remember(tempIntervalServer) { BillingManager.getNextDueDate(tempIntervalServer) }
                                        val nextDueIntervalStr = remember(nextDueInterval) { BillingManager.fullDateFormat.format(nextDueInterval.time) }
                                        val tempIntervalStatus = remember(tempIntervalServer) { BillingManager.calculateStatus(tempIntervalServer) }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = CyanPrimary.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.35f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "Следующая оплата: $nextDueIntervalStr",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = CyanPrimary
                                                        )
                                                        Text(
                                                            text = "Осталось: ${tempIntervalStatus.daysLeft ?: 0} дн.",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Button(
                                                        onClick = {
                                                            val nextMs = BillingManager.addPeriod(tempIntervalServer, billingLastPaidDate)
                                                            billingLastPaidDate = nextMs
                                                            Toast.makeText(context, "Оплата продлена на $intervalVal дн.!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = MintSecondary, contentColor = Color.Black)
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text("+$intervalVal дн.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                if (billingLastPaidDate > 0L) {
                                                    TextButton(
                                                        onClick = {
                                                            billingLastPaidDate = 0L
                                                            Toast.makeText(context, "Сброшено к авто-расчету", Toast.LENGTH_SHORT).show()
                                                        },
                                                        contentPadding = PaddingValues(0.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text("Сбросить к текущей дате", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                BillingType.TIMEWEB_CLOUD -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = billingApiKey,
                                                onValueChange = {
                                                    billingApiKey = it
                                                    apiTestResult = null
                                                },
                                                label = { Text(strings.billingTimewebTokenLabel) },
                                                placeholder = { Text("eyJhbGciOiJSUzUxMiIs...") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                                    if (!clip.isNullOrBlank()) {
                                                        billingApiKey = clip.trim()
                                                        apiTestResult = null
                                                        Toast.makeText(context, "Токен Timeweb вставлен", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.ContentPaste, contentDescription = strings.actionPaste, tint = CyanPrimary)
                                            }
                                        }

                                        // Test API Button & Result
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (billingApiKey.isBlank()) {
                                                        Toast.makeText(context, "Введите токен Timeweb", Toast.LENGTH_SHORT).show()
                                                        return@OutlinedButton
                                                    }
                                                    isCheckingApi = true
                                                    apiTestResult = null
                                                    coroutineScope.launch {
                                                        val res = BillingManager.testBillingApi(
                                                            BillingType.TIMEWEB_CLOUD,
                                                            billingApiKey
                                                        )
                                                        isCheckingApi = false
                                                        apiTestResult = res
                                                    }
                                                },
                                                enabled = !isCheckingApi && billingApiKey.isNotBlank(),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                if (isCheckingApi) {
                                                    androidx.compose.material3.CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(14.dp), color = CyanPrimary)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(strings.billingCheckingApi, fontSize = 11.sp)
                                                } else {
                                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(strings.billingCheckApiBtn, fontSize = 11.sp)
                                                }
                                            }
                                        }

                                        apiTestResult?.let { result ->
                                            if (result.success) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MintSecondary.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, MintSecondary.copy(alpha = 0.4f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Text(
                                                            text = "✓ API подключен успешно!",
                                                            color = MintSecondary,
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                        Text(
                                                            text = "Баланс: ${result.balance} ${result.currency}" +
                                                                    (result.hoursLeft?.let { " | Осталось: $it ч (~${result.daysLeft} дн.)" } ?: ""),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            } else {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = RedAccent.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, RedAccent.copy(alpha = 0.4f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Ошибка: ${result.errorMessage ?: "Не удалось подключиться"}",
                                                        color = RedAccent,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                BillingType.SELECTEL -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = billingApiKey,
                                                onValueChange = {
                                                    billingApiKey = it
                                                    apiTestResult = null
                                                },
                                                label = { Text(strings.billingSelectelKeyLabel) },
                                                placeholder = { Text("DAusR7c5gZSowRsPMh2JFe7Zn_...") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                                    if (!clip.isNullOrBlank()) {
                                                        billingApiKey = clip.trim()
                                                        apiTestResult = null
                                                        Toast.makeText(context, "API-ключ Selectel вставлен", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.ContentPaste, contentDescription = strings.actionPaste, tint = CyanPrimary)
                                            }
                                        }

                                        OutlinedTextField(
                                            value = billingCostMonthlyText,
                                            onValueChange = {
                                                billingCostMonthlyText = it.filter { ch -> ch.isDigit() || ch == '.' }
                                                apiTestResult = null
                                            },
                                            label = { Text(strings.billingCostMonthlyLabel) },
                                            placeholder = { Text("напр. 790") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Text(
                                            text = strings.billingCostMonthlyHint,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        // Test API Button & Result
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (billingApiKey.isBlank()) {
                                                        Toast.makeText(context, "Введите API-ключ Selectel", Toast.LENGTH_SHORT).show()
                                                        return@OutlinedButton
                                                    }
                                                    isCheckingApi = true
                                                    apiTestResult = null
                                                    coroutineScope.launch {
                                                        val cost = billingCostMonthlyText.toDoubleOrNull() ?: 0.0
                                                        val res = BillingManager.testBillingApi(
                                                            BillingType.SELECTEL,
                                                            billingApiKey,
                                                            cost
                                                        )
                                                        isCheckingApi = false
                                                        apiTestResult = res
                                                    }
                                                },
                                                enabled = !isCheckingApi && billingApiKey.isNotBlank(),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                if (isCheckingApi) {
                                                    androidx.compose.material3.CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(14.dp), color = CyanPrimary)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(strings.billingCheckingApi, fontSize = 11.sp)
                                                } else {
                                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(strings.billingCheckApiBtn, fontSize = 11.sp)
                                                }
                                            }
                                        }

                                        apiTestResult?.let { result ->
                                            if (result.success) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MintSecondary.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, MintSecondary.copy(alpha = 0.4f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Text(
                                                            text = "✓ API подключен успешно!",
                                                            color = MintSecondary,
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                        Text(
                                                            text = "Баланс: ${result.balance} ₽" +
                                                                    (result.daysLeft?.let { " | Хватит примерно на $it дн." } ?: ""),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            } else {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = RedAccent.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, RedAccent.copy(alpha = 0.4f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Ошибка: ${result.errorMessage ?: "Не удалось подключиться"}",
                                                        color = RedAccent,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                BillingType.BEGET -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = billingApiKey,
                                                onValueChange = {
                                                    billingApiKey = it
                                                    apiTestResult = null
                                                },
                                                label = { Text(strings.billingBegetKeyLabel) },
                                                placeholder = { Text("login:password или token...") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                                    if (!clip.isNullOrBlank()) {
                                                        billingApiKey = clip.trim()
                                                        apiTestResult = null
                                                        Toast.makeText(context, "Ключ Beget вставлен", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.ContentPaste, contentDescription = strings.actionPaste, tint = CyanPrimary)
                                            }
                                        }

                                        OutlinedTextField(
                                            value = billingCostMonthlyText,
                                            onValueChange = { billingCostMonthlyText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                            label = { Text(strings.billingCostMonthlyLabel) },
                                            placeholder = { Text("600") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Text(
                                            text = "Укажите логин и пароль от API в формате login:password или API-токен Beget, и стоимость в месяц для расчета дней.",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        // Test API Button
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (billingApiKey.isBlank()) {
                                                        Toast.makeText(context, "Введите данные Beget", Toast.LENGTH_SHORT).show()
                                                        return@OutlinedButton
                                                    }
                                                    isCheckingApi = true
                                                    apiTestResult = null
                                                    coroutineScope.launch {
                                                        val cost = billingCostMonthlyText.toDoubleOrNull() ?: 0.0
                                                        val res = BillingManager.testBillingApi(
                                                            BillingType.BEGET,
                                                            billingApiKey,
                                                            cost
                                                        )
                                                        isCheckingApi = false
                                                        apiTestResult = res
                                                    }
                                                },
                                                enabled = !isCheckingApi && billingApiKey.isNotBlank(),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                if (isCheckingApi) {
                                                    androidx.compose.material3.CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(14.dp), color = CyanPrimary)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(strings.billingCheckingApi, fontSize = 11.sp)
                                                } else {
                                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(strings.billingCheckApiBtn, fontSize = 11.sp)
                                                }
                                            }
                                        }

                                        apiTestResult?.let { result ->
                                            if (result.success) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MintSecondary.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, MintSecondary.copy(alpha = 0.4f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Text(
                                                            text = "✓ API Beget подключен успешно!",
                                                            color = MintSecondary,
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                        Text(
                                                            text = "Баланс: ${result.balance} ₽" +
                                                                    (result.daysLeft?.let { " | Хватит примерно на $it дн." } ?: ""),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            } else {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = RedAccent.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, RedAccent.copy(alpha = 0.4f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Ошибка: ${result.errorMessage ?: "Не удалось подключиться"}",
                                                        color = RedAccent,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
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

                    val finalBillingType = billingType.name
                    val finalDay = billingDayOfMonthText.toIntOrNull()?.coerceIn(1, 31) ?: 1
                    val finalInterval = billingIntervalDaysText.toIntOrNull()?.coerceAtLeast(1) ?: 30
                    val finalCost = billingCostMonthlyText.toDoubleOrNull() ?: 0.0

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
                        bindToLan = true,
                        billingType = finalBillingType,
                        billingDayOfMonth = finalDay,
                        billingIntervalDays = finalInterval,
                        billingLastPaidDate = billingLastPaidDate,
                        billingApiKey = billingApiKey.trim(),
                        billingCostMonthly = finalCost,
                        cachedBalance = apiTestResult?.balance ?: initialServer?.cachedBalance,
                        cachedCurrency = apiTestResult?.currency ?: initialServer?.cachedCurrency ?: "RUB",
                        cachedHoursLeft = apiTestResult?.hoursLeft ?: initialServer?.cachedHoursLeft,
                        cachedDaysLeft = apiTestResult?.daysLeft ?: initialServer?.cachedDaysLeft,
                        billingLastCheckedAt = if (apiTestResult?.success == true) System.currentTimeMillis() else (initialServer?.billingLastCheckedAt ?: 0L)
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
