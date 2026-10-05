package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.backup.GoogleDriveBackupManager
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.WebPanelScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.RedAccent
import com.example.ui.viewmodel.AppThemeMode
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.TunnelClosePolicy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import java.io.BufferedReader
import java.io.InputStreamReader

enum class ScreenState {
    SERVERS,
    WEB_PANEL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(ScreenState.SERVERS) }
    var webPanelUrl by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val servers by viewModel.servers.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val tunnelState by viewModel.tunnelState.collectAsStateWithLifecycle()
    val closePolicy by viewModel.closePolicy.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val maskIp by viewModel.maskIp.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            if (currentScreen == ScreenState.SERVERS) {
                TopAppBar(
                    title = {
                        Text(
                            text = "3x manager",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                    },
                    actions = {
                        // Компактная кнопка отключения туннелей прямо в основном меню
                        if (tunnelState.isRunning) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RedAccent.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, RedAccent.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.stopTunnel(context) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(MintSecondary)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "Отключить туннель",
                                        tint = RedAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Откл.",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RedAccent,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.toggleMaskIp() }) {
                            Icon(
                                imageVector = if (maskIp) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (maskIp) "Показать IP" else "Скрыть IP",
                                tint = if (maskIp) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Настройки"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ScreenState.SERVERS -> {
                    ServersScreen(
                        servers = servers,
                        selectedServer = selectedServer,
                        tunnelState = tunnelState,
                        maskIp = maskIp,
                        sortMode = sortMode,
                        onSortModeChange = { viewModel.setSortMode(it) },
                        onStopTunnel = { viewModel.stopTunnel(context) },
                        onSelectServer = { server ->
                            viewModel.selectServer(server, context)
                            webPanelUrl = server.getEffectiveUrl()
                            currentScreen = ScreenState.WEB_PANEL
                        },
                        onSaveServer = { server ->
                            viewModel.saveServer(server)
                        },
                        onDeleteServer = { server ->
                            viewModel.deleteServer(server)
                        }
                    )
                }

                ScreenState.WEB_PANEL -> {
                    val targetUrl = if (tunnelState.isRunning) {
                        val path = selectedServer?.let { if (it.basePath.isNotBlank()) "/${it.basePath.trim('/')}" else "" } ?: ""
                        val scheme = if (selectedServer?.useHttps == true) "https" else "http"
                        "$scheme://127.0.0.1:${tunnelState.localPort}$path"
                    } else {
                        selectedServer?.getEffectiveUrl() ?: ""
                    }

                    WebPanelScreen(
                        initialUrl = if (webPanelUrl.isNotBlank()) webPanelUrl else targetUrl,
                        server = selectedServer,
                        tunnelState = tunnelState,
                        maskIp = maskIp,
                        onStartTunnel = {
                            selectedServer?.let { s -> viewModel.startTunnelForServer(s, context) }
                        },
                        onGoToServers = {
                            viewModel.onLeavePanel(context)
                            currentScreen = ScreenState.SERVERS
                        }
                    )
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            currentPolicy = closePolicy,
            currentTheme = themeMode,
            tunnelState = tunnelState,
            onSelectPolicy = { policy -> viewModel.setClosePolicy(policy) },
            onSelectTheme = { mode -> viewModel.setThemeMode(mode) },
            onStopTunnel = { viewModel.stopTunnel(context) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Диалог авто-обновления приложения с GitHub Releases
    updateInfo?.let { update ->
        if (update.hasUpdate) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdate() },
                icon = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = CyanPrimary) },
                title = { Text("Доступно обновление!", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Версия ${update.latestVersion} (у вас ${update.currentVersion})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )

                        Text(
                            text = "Что нового в этом обновлении:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = update.releaseNotes.ifBlank { "• Улучшения стабильности и оптимизация работы" },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissUpdate()
                            if (!update.downloadUrl.isNullOrBlank()) {
                                com.example.data.updater.AppUpdateManager.startDownload(context, update.downloadUrl)
                            } else {
                                com.example.data.updater.AppUpdateManager.openBrowser(context, update.releasePageUrl)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text("Скачать APK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdate() }) {
                        Text("Позже")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsDialog(
    viewModel: MainViewModel,
    currentPolicy: TunnelClosePolicy,
    currentTheme: AppThemeMode,
    tunnelState: com.example.service.ActiveTunnelState,
    onSelectPolicy: (TunnelClosePolicy) -> Unit,
    onSelectTheme: (AppThemeMode) -> Unit,
    onStopTunnel: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Общие, 1 = Бэкап
    var policyExpanded by remember { mutableStateOf(false) }

    // Google Drive state
    val googleAccount by viewModel.googleAccount.collectAsStateWithLifecycle()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()
    var isDriveLoading by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkGoogleAccount(context)
    }

    val signInClient = remember { GoogleDriveBackupManager.getSignInClient(context) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.setGoogleAccount(account)
            Toast.makeText(context, "Подключен: ${account.email}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Вход не выполнен: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // WebDAV states (cached in remember)
    var webdavUrl by remember { mutableStateOf(viewModel.getSavedWebdavUrl()) }
    var webdavUser by remember { mutableStateOf(viewModel.getSavedWebdavUser()) }
    var webdavPass by remember { mutableStateOf(viewModel.getSavedWebdavPass()) }
    var isWebdavLoading by remember { mutableStateOf(false) }

    // File Export Launcher (Allows saving to local files or Google Drive via system storage picker)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val json = viewModel.getBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(json.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Резервная копия сохранена", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // File Import Launcher (Allows restoring from local files or Google Drive)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = BufferedReader(InputStreamReader(stream)).readText()
                    viewModel.restoreBackup(content) { res ->
                        if (res.isSuccess) {
                            Toast.makeText(context, "Восстановлено серверов: ${res.getOrNull()}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Ошибка: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка чтения файла: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = CyanPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Настройки", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = CyanPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyanPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Общие", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Бэкап", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smooth non-stuttering Column with verticalScroll (no laggy window popups)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedTab == 0) {
                        // 1. Theme Selection Segmented Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Тема оформления",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppThemeMode.values().forEach { mode ->
                                    val isSelected = currentTheme == mode
                                    val icon = when (mode) {
                                        AppThemeMode.DARK -> Icons.Default.DarkMode
                                        AppThemeMode.LIGHT -> Icons.Default.LightMode
                                        AppThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                                    }
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        ),
                                        border = if (isSelected) BorderStroke(1.5.dp, CyanPrimary) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSelectTheme(mode) }
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = mode.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Expandable Tunnel Close Policy Selector
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { policyExpanded = !policyExpanded }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Tune, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Режим SSH-туннеля", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(currentPolicy.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Icon(
                                        imageVector = if (policyExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = CyanPrimary
                                    )
                                }

                                AnimatedVisibility(
                                    visible = policyExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 12.dp)
                                    ) {
                                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        TunnelClosePolicy.values().forEach { policy ->
                                            val isSelected = currentPolicy == policy
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) CyanPrimary.copy(alpha = 0.12f) else Color.Transparent)
                                                    .clickable {
                                                        onSelectPolicy(policy)
                                                        policyExpanded = false
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                                    contentDescription = null,
                                                    tint = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = policy.title,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = policy.description,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Status of active tunnel with manual stop if running
                        if (tunnelState.isRunning) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MintSecondary.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Туннель сейчас активен", fontWeight = FontWeight.Bold, color = MintSecondary, fontSize = 12.sp)
                                        Text("Порт :${tunnelState.localPort}", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = onStopTunnel,
                                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Отключить", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // 3. Версия и Обновление приложения (GitHub Releases Auto-Updater)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = "Версия приложения",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Текущая: v${com.example.BuildConfig.VERSION_NAME}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { showChangelogDialog = true },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Чейнджлог", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { viewModel.checkForAppUpdates(silent = false) },
                                            enabled = !isCheckingUpdate,
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            if (isCheckingUpdate) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 2.dp,
                                                    color = Color.White
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Sync,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Обновить", fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Section 2: Backup & Restore (Google Drive, WebDAV, Local File)
                        // A. Google Drive Direct Sync
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Sync, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Google Drive синхронизация", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                if (googleAccount == null) {
                                    Text(
                                        text = "Подключите Google Аккаунт для облачной синхронизации конфигураций",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            googleSignInLauncher.launch(signInClient.signInIntent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Войти через Google", fontWeight = FontWeight.SemiBold)
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = googleAccount?.email ?: "Аккаунт подключен",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MintSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = {
                                                isDriveLoading = true
                                                viewModel.uploadBackupToDrive(context) {
                                                    isDriveLoading = false
                                                }
                                            },
                                            enabled = !isDriveLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (isDriveLoading) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                            } else {
                                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Выгрузить", fontSize = 12.sp)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                isDriveLoading = true
                                                viewModel.restoreBackupFromDrive(context) {
                                                    isDriveLoading = false
                                                }
                                            },
                                            enabled = !isDriveLoading,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Загрузить", fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    TextButton(
                                        onClick = {
                                            signInClient.signOut().addOnCompleteListener {
                                                viewModel.setGoogleAccount(null)
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Выйти из Google", color = RedAccent, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // B. WebDAV Backup Section
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FolderShared, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Резервная копия по WebDAV",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Яндекс Диск, Nextcloud, ownCloud или частный сервер",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )

                        OutlinedTextField(
                            value = webdavUrl,
                            onValueChange = { webdavUrl = it },
                            label = { Text("URL WebDAV сервера") },
                            placeholder = { Text("https://webdav.yandex.ru/3xui_backup.json") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = webdavUser,
                                onValueChange = { webdavUser = it },
                                label = { Text("Логин") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = webdavPass,
                                onValueChange = { webdavPass = it },
                                label = { Text("Пароль") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    if (webdavUrl.isBlank()) {
                                        Toast.makeText(context, "Укажите URL WebDAV", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isWebdavLoading = true
                                    viewModel.uploadBackupToWebdav(webdavUrl, webdavUser, webdavPass) {
                                        isWebdavLoading = false
                                    }
                                },
                                enabled = !isWebdavLoading && webdavUrl.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isWebdavLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Выгрузить", fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    if (webdavUrl.isBlank()) {
                                        Toast.makeText(context, "Укажите URL WebDAV", Toast.LENGTH_SHORT).show()
                                        return@OutlinedButton
                                    }
                                    isWebdavLoading = true
                                    viewModel.restoreBackupFromWebdav(webdavUrl, webdavUser, webdavPass) {
                                        isWebdavLoading = false
                                    }
                                },
                                enabled = !isWebdavLoading && webdavUrl.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Загрузить", fontSize = 12.sp)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // C. Local File Export & Import
                        Text(
                            text = "Локальный файл JSON",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Сохранение и загрузка файла из памяти устройства",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    exportLauncher.launch("3xui_backup_${System.currentTimeMillis() / 1000}.json")
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Сохранить", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    importLauncher.launch(arrayOf("application/json", "*/*"))
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Восстановить", fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val json = viewModel.getBackupJson()
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "3x-ui Backup")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(intent, "Отправить / Поделиться"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Поделиться бэкапом", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Закрыть")
            }
        }
    )

    if (showChangelogDialog) {
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            title = {
                Text("История изменений", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(com.example.data.updater.AppChangelog.history) { entry ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.version,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanPrimary
                                    )
                                    Text(
                                        text = entry.date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                entry.highlights.forEach { item ->
                                    Text(
                                        text = "• $item",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showChangelogDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Понятно")
                }
            }
        )
    }
}
