package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RightTabsDrawer
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.RussianStrings
import com.example.ui.i18n.Strings
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.WebPanelScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.RedAccent
import com.example.ui.viewmodel.AppThemeMode
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.TunnelClosePolicy
import kotlinx.coroutines.launch
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
    val coroutineScope = rememberCoroutineScope()
    var isTabsDrawerOpen by remember { mutableStateOf(false) }

    var currentScreen by remember { mutableStateOf(ScreenState.SERVERS) }
    var webPanelUrl by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    var showPanelUpdateDialog by remember { mutableStateOf(false) }

    val servers by viewModel.servers.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val openedServers by viewModel.openedServers.collectAsStateWithLifecycle()
    val tunnelState by viewModel.tunnelState.collectAsStateWithLifecycle()
    val closePolicy by viewModel.closePolicy.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val strings by viewModel.strings.collectAsStateWithLifecycle()
    val maskIp by viewModel.maskIp.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val panelUpdateInfo by viewModel.panelUpdateInfo.collectAsStateWithLifecycle()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()
    val isDownloadingUpdate by viewModel.isDownloadingUpdate.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (currentScreen == ScreenState.SERVERS) {
                    TopAppBar(
                        title = {
                            Text(
                                text = strings.appName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.5).sp
                            )
                        },
                        actions = {
                            // Кнопка открытых вкладок серверов (на правой стороне)
                            if (openedServers.isNotEmpty()) {
                                IconButton(
                                    onClick = { isTabsDrawerOpen = true }
                                ) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = CyanPrimary,
                                                contentColor = Color.Black
                                            ) {
                                                Text(
                                                    text = "${openedServers.size}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Layers,
                                            contentDescription = strings.openTabsDrawerTooltip,
                                            tint = CyanPrimary
                                        )
                                    }
                                }
                            }

                            // Компактная кнопка отключения туннелей прямо в основном меню
                            if (tunnelState.isRunning) {
                                Surface(
                                    onClick = { viewModel.stopTunnel(context) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = RedAccent.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, RedAccent.copy(alpha = 0.45f)),
                                    modifier = Modifier.padding(end = 4.dp)
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
                                            contentDescription = strings.disconnectButton,
                                            tint = RedAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = strings.stopShort,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RedAccent,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Кнопка новой версии ПАНЕЛИ 3x-ui (размещена СЛЕВА от версии обновления приложения)
                            if (panelUpdateInfo?.hasUpdate == true) {
                                Surface(
                                    onClick = { showPanelUpdateDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    color = AmberAccent.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.65f)),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Dns,
                                            contentDescription = null,
                                            tint = AmberAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = strings.panelUpdateBadge(panelUpdateInfo?.latestVersion ?: ""),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AmberAccent,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Кнопка новой версии (размещена слева от глаза скрытия IP)
                            if (updateInfo?.hasUpdate == true) {
                                Surface(
                                    onClick = { showUpdateDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    color = CyanPrimary.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.65f)),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = updateInfo?.latestVersion ?: "",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanPrimary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { viewModel.toggleMaskIp() }) {
                                Icon(
                                    imageVector = if (maskIp) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (maskIp) strings.maskIpTitle else strings.maskIpTitle,
                                    tint = if (maskIp) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { showSettingsDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = strings.settingsTitle
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
            modifier = Modifier.fillMaxSize()
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
                            strings = strings,
                            onSortModeChange = { viewModel.setSortMode(it) },
                            onStopTunnel = { viewModel.stopTunnel(context) },
                            onSelectServer = { server ->
                                viewModel.openServerTab(server, context)
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
                            openedServers = openedServers,
                            maskIp = maskIp,
                            strings = strings,
                            onStartTunnel = {
                                selectedServer?.let { s -> viewModel.startTunnelForServer(s, context) }
                            },
                            onOpenDrawer = {
                                isTabsDrawerOpen = true
                            },
                            onGoToServers = {
                                viewModel.onLeavePanel(context)
                                currentScreen = ScreenState.SERVERS
                            },
                            onPanelVersionDetected = { ver ->
                                selectedServer?.let { viewModel.onPanelVersionDetected(it, ver) }
                            }
                        )
                    }
                }
            }
        }

        // Выезжающая панель открытых серверов со СПРАВА (открывается только по кнопке, жест вытягивания отключен)
        RightTabsDrawer(
            isOpen = isTabsDrawerOpen && openedServers.isNotEmpty(),
            openedServers = openedServers,
            selectedServer = selectedServer,
            tunnelState = tunnelState,
            maskIp = maskIp,
            strings = strings,
            onSelectServer = { server ->
                viewModel.selectServer(server, context)
                webPanelUrl = server.getEffectiveUrl()
                currentScreen = ScreenState.WEB_PANEL
                isTabsDrawerOpen = false
            },
            onCloseTab = { server ->
                viewModel.closeServerTab(server, context)
                if (viewModel.openedServers.value.isEmpty()) {
                    currentScreen = ScreenState.SERVERS
                    isTabsDrawerOpen = false
                }
            },
            onCloseAllTabs = {
                viewModel.closeAllServerTabs(context)
                currentScreen = ScreenState.SERVERS
                isTabsDrawerOpen = false
            },
            onGoToHome = {
                currentScreen = ScreenState.SERVERS
                isTabsDrawerOpen = false
            },
            onCloseDrawer = {
                isTabsDrawerOpen = false
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            currentPolicy = closePolicy,
            currentTheme = themeMode,
            currentLanguage = appLanguage,
            strings = strings,
            tunnelState = tunnelState,
            onSelectPolicy = { policy -> viewModel.setClosePolicy(policy) },
            onSelectTheme = { mode -> viewModel.setThemeMode(mode) },
            onSelectLanguage = { lang -> viewModel.setAppLanguage(lang) },
            onStopTunnel = { viewModel.stopTunnel(context) },
            onOpenUpdateDialog = {
                showSettingsDialog = false
                showUpdateDialog = true
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Диалог авто-обновления приложения с GitHub Releases (открывается по клику на плашку версии или из настроек)
    if (showUpdateDialog && updateInfo != null) {
        val update = updateInfo!!
        var selectedChangelogTab by remember { mutableStateOf(0) } // 0: Эта версия, 1: Все версии

        AlertDialog(
            onDismissRequest = {
                if (!isDownloadingUpdate) {
                    showUpdateDialog = false
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = CyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (update.hasUpdate) strings.updateAvailableTitle else strings.changelogTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = {
                            if (isDownloadingUpdate) {
                                viewModel.dismissUpdate()
                            }
                            showUpdateDialog = false
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanPrimary.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Новая: ${update.latestVersion}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "Текущая: ${update.currentVersion}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (isDownloadingUpdate) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Загрузка 3x-manager-${update.latestVersion}.apk",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${((downloadProgress ?: 0f) * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanPrimary
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { downloadProgress ?: 0f },
                                    color = CyanPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Text(
                                    text = "По завершении загрузки сразу откроется окно установки",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Вкладки переключения: "В этой версии" vs "Все версии"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { selectedChangelogTab = 0 },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedChangelogTab == 0) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (selectedChangelogTab == 0) BorderStroke(1.dp, CyanPrimary) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "В версии ${update.latestVersion}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedChangelogTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedChangelogTab == 0) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            Surface(
                                onClick = { selectedChangelogTab = 1 },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedChangelogTab == 1) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (selectedChangelogTab == 1) BorderStroke(1.dp, CyanPrimary) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "История версий",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedChangelogTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedChangelogTab == 1) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            if (selectedChangelogTab == 0) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    val currentHistory = com.example.data.updater.AppChangelog.history.firstOrNull { it.version == update.latestVersion }
                                    if (currentHistory != null) {
                                        ChangelogCardContent(currentHistory)
                                    } else {
                                        Text(
                                            text = update.releaseNotes.ifBlank { "• Оптимизация работы и повышение стабильности\n• Исправления интерфейса и туннелей" },
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(com.example.data.updater.AppChangelog.history) { entry ->
                                        Card(
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                ChangelogCardContent(entry)
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
                if (isDownloadingUpdate) {
                    Button(
                        onClick = {
                            viewModel.dismissUpdate()
                            showUpdateDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(strings.btnCancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (update.hasUpdate) {
                            Button(
                                onClick = {
                                    if (!update.downloadUrl.isNullOrBlank()) {
                                        viewModel.startInAppUpdate(context, update.downloadUrl, update.latestVersion)
                                    } else {
                                        com.example.data.updater.AppUpdateManager.openBrowser(context, update.releasePageUrl)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.btnDownloadInstall)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    com.example.data.updater.AppUpdateManager.openBrowser(
                                        context,
                                        update.downloadUrl ?: update.releasePageUrl
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = strings.btnDownloadBrowser,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            TextButton(
                                onClick = { showUpdateDialog = false },
                                modifier = Modifier.weight(0.7f)
                            ) {
                                Text(strings.btnLater)
                            }
                        }
                    }
                }
            }
        )
    }

    // Диалог обновления панели 3x-ui (MHSanaei/3x-ui)
    if (showPanelUpdateDialog && panelUpdateInfo != null) {
        val panelInfo = panelUpdateInfo!!
        AlertDialog(
            onDismissRequest = { showPanelUpdateDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = AmberAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.panelUpdateDialogTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = { showPanelUpdateDialog = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Server & Versions card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (panelInfo.serverName.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${strings.panelServerLabel}: ",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = panelInfo.serverName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = strings.panelCurrentVersionLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = panelInfo.currentVersion.ifBlank { "v2.4.2" },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = strings.panelLatestVersionLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberAccent
                                    )
                                    Text(
                                        text = panelInfo.latestVersion,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                        fontWeight = FontWeight.Bold,
                                        color = AmberAccent
                                    )
                                }
                            }
                        }
                    }

                    // Release description
                    if (panelInfo.releaseNotes.isNotBlank()) {
                        Text(
                            text = "Что нового в ${panelInfo.latestVersion}:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = panelInfo.releaseNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        com.example.data.updater.PanelUpdateManager.openBrowser(context, panelInfo.releasePageUrl)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.btnOpenGitHubReleases, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPanelUpdateDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.btnLater)
                }
            }
        )
    }
}

@Composable
fun SettingsDialog(
    viewModel: MainViewModel,
    currentPolicy: TunnelClosePolicy,
    currentTheme: AppThemeMode,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    strings: Strings = RussianStrings,
    tunnelState: com.example.service.ActiveTunnelState,
    onSelectPolicy: (TunnelClosePolicy) -> Unit,
    onSelectTheme: (AppThemeMode) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onStopTunnel: () -> Unit,
    onOpenUpdateDialog: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Общие, 1 = Бэкап
    var policyExpanded by remember { mutableStateOf(false) }

    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()
    var showChangelogDialog by remember { mutableStateOf(false) }

    // WebDAV states (cached in remember)
    var webdavUrl by remember { mutableStateOf(viewModel.getSavedWebdavUrl()) }
    var webdavUser by remember { mutableStateOf(viewModel.getSavedWebdavUser()) }
    var webdavPass by remember { mutableStateOf(viewModel.getSavedWebdavPass()) }
    var isWebdavLoading by remember { mutableStateOf(false) }

    // File Export Launcher (Allows saving to local files or cloud via system storage picker)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val json = viewModel.getBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(json.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, strings.msgServerSaved("Backup"), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // File Import Launcher (Allows restoring from local files or cloud)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = BufferedReader(InputStreamReader(stream)).readText()
                    viewModel.restoreBackup(content) { res ->
                        if (res.isSuccess) {
                            Toast.makeText(context, strings.msgBackupRestored(res.getOrNull() ?: 0), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(strings.settingsTitle, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
                        text = { Text(strings.tabGeneral, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(strings.tabBackup, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smooth non-stuttering Column with verticalScroll (no laggy window popups)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(370.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (selectedTab == 0) {
                        // 1. Language Selection Segmented Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = strings.languageTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    val isSelected = currentLanguage == lang
                                    val title = when (lang) {
                                        AppLanguage.SYSTEM -> "Auto"
                                        AppLanguage.RU -> "RU"
                                        AppLanguage.EN -> "EN"
                                    }
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        ),
                                        border = if (isSelected) BorderStroke(1.5.dp, CyanPrimary) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSelectLanguage(lang) }
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                        ) {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Theme Selection Segmented Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = strings.themeTitle,
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
                                    val title = when (mode) {
                                        AppThemeMode.DARK -> strings.themeDark
                                        AppThemeMode.LIGHT -> strings.themeLight
                                        AppThemeMode.SYSTEM -> strings.themeSystem
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
                                                text = title,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Expandable Tunnel Close Policy Selector
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
                                    val policyTitle = when (currentPolicy) {
                                        TunnelClosePolicy.ON_PANEL_EXIT -> strings.policyPanelExitTitle
                                        TunnelClosePolicy.ON_APP_CLOSE -> strings.policyAppCloseTitle
                                        TunnelClosePolicy.NEVER -> strings.policyNeverTitle
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Tune, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(strings.tunnelPolicyTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(policyTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
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
                                            val pTitle = when (policy) {
                                                TunnelClosePolicy.ON_PANEL_EXIT -> strings.policyPanelExitTitle
                                                TunnelClosePolicy.ON_APP_CLOSE -> strings.policyAppCloseTitle
                                                TunnelClosePolicy.NEVER -> strings.policyNeverTitle
                                            }
                                            val pDesc = when (policy) {
                                                TunnelClosePolicy.ON_PANEL_EXIT -> strings.policyPanelExitDesc
                                                TunnelClosePolicy.ON_APP_CLOSE -> strings.policyAppCloseDesc
                                                TunnelClosePolicy.NEVER -> strings.policyNeverDesc
                                            }
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
                                                        text = pTitle,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = pDesc,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        Text(strings.activeTunnelCardTitle, fontWeight = FontWeight.Bold, color = MintSecondary, style = MaterialTheme.typography.bodyMedium)
                                        Text(strings.activeTunnelCardDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = onStopTunnel,
                                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.disconnectButton, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 4. Версия и Обновление приложения (GitHub Releases Auto-Updater)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${strings.appName} • ${strings.versionPrefix} v${com.example.BuildConfig.VERSION_NAME}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { showChangelogDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                    ) {
                                        Text(
                                            text = strings.changelogTitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.checkForAppUpdates(silent = false) { hasUpdate ->
                                                if (hasUpdate) {
                                                    onOpenUpdateDialog()
                                                }
                                            }
                                        },
                                        enabled = !isCheckingUpdate,
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                    ) {
                                        if (isCheckingUpdate) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Sync,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = strings.btnCheckUpdates,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Tab 1: Бэкап и восстановление (Локальный файл и WebDAV)
                        // A. Локальный бэкап (JSON-файл)
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Storage, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(strings.localBackupTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MintSecondary.copy(alpha = 0.2f)
                                    ) {
                                        Text("JSON", color = MintSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = strings.localBackupDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
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
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = strings.btnSaveFile,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            importLauncher.launch(arrayOf("application/json", "*/*"))
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = strings.btnLoadFile,
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        val json = viewModel.getBackupJson()
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "3x-ui Backup")
                                            putExtra(Intent.EXTRA_TEXT, json)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Backup"))
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Share JSON",
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        // B. WebDAV Синхронизация
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FolderShared, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.webdavTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = strings.webdavDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = webdavUrl,
                                    onValueChange = { webdavUrl = it },
                                    label = { Text(strings.webdavUrlField) },
                                    placeholder = { Text("https://webdav.yandex.ru/backup.json") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = webdavUser,
                                        onValueChange = { webdavUser = it },
                                        label = { Text(strings.webdavUserField) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = webdavPass,
                                        onValueChange = { webdavPass = it },
                                        label = { Text(strings.webdavPassField) },
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
                                                Toast.makeText(context, "URL is required", Toast.LENGTH_SHORT).show()
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
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        if (isWebdavLoading) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                        } else {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = strings.btnUpload,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (webdavUrl.isBlank()) {
                                                Toast.makeText(context, "URL is required", Toast.LENGTH_SHORT).show()
                                                return@OutlinedButton
                                            }
                                            isWebdavLoading = true
                                            viewModel.restoreBackupFromWebdav(webdavUrl, webdavUser, webdavPass) {
                                                isWebdavLoading = false
                                            }
                                        },
                                        enabled = !isWebdavLoading && webdavUrl.isNotBlank(),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = strings.btnDownload,
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )

    if (showChangelogDialog) {
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = CyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.changelogTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(
                        onClick = { showChangelogDialog = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(com.example.data.updater.AppChangelog.history) { entry ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                ChangelogCardContent(entry)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showChangelogDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Понятно")
                }
            }
        )
    }
}

@Composable
fun ChangelogCardContent(entry: com.example.data.updater.ChangelogEntry) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.version,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = entry.releaseType.badgeColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, entry.releaseType.badgeColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = entry.releaseType.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = entry.releaseType.badgeColor,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = entry.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (entry.summary.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.summary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        entry.categories.forEach { category ->
            Text(
                text = category.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanPrimary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
            category.items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 1.5.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
