package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.db.ServerEntity
import com.example.service.ActiveTunnelState
import com.example.ui.i18n.RussianStrings
import com.example.ui.i18n.Strings
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.delay

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPanelScreen(
    initialUrl: String,
    server: ServerEntity? = null,
    tunnelState: ActiveTunnelState? = null,
    openedServers: List<ServerEntity> = emptyList(),
    maskIp: Boolean = false,
    strings: Strings = RussianStrings,
    onStartTunnel: (() -> Unit)? = null,
    onGoToServers: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    onPanelVersionDetected: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var isDesktopMode by remember { mutableStateOf(false) }

    val defaultUserAgent = remember { WebSettings.getDefaultUserAgent(context) }
    val desktopUserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

    val isTunnelRequired = server?.useTunnel == true
    val isServerTunnelRunning = remember(server, tunnelState) {
        if (server == null || !server.useTunnel) false
        else com.example.service.TunnelService.isTunnelRunning(server.id) ||
             (tunnelState?.isRunning == true && (tunnelState.configId == server.id || tunnelState.localPort == server.localPort))
    }
    val isTunnelRunning = if (isTunnelRequired) isServerTunnelRunning else true
    val isTunnelError = tunnelState?.isError == true && (tunnelState?.configId == server?.id || tunnelState?.localPort == server?.localPort)
    val isConnecting = isTunnelRequired && !isTunnelRunning && !isTunnelError
    val canShowControls = !isTunnelRequired || isTunnelRunning

    val displayHost = remember(server?.sshHost, maskIp) {
        server?.sshHost?.let { com.example.ui.viewmodel.MainViewModel.maskIp(it, maskIp) } ?: ""
    }

    val effectivePort = remember(server, tunnelState) {
        if (server != null && server.localPort > 0) server.localPort else (tunnelState?.localPort ?: server?.port ?: 2053)
    }

    // Target URL calculation
    val effectiveUrl = remember(initialUrl, server, isTunnelRunning, effectivePort) {
        if (server?.useTunnel == true) {
            val path = if (server.basePath.isNotBlank()) "/${server.basePath.trim('/')}" else ""
            val scheme = if (server.useHttps) "https" else "http"
            "$scheme://127.0.0.1:$effectivePort$path"
        } else if (initialUrl.isNotBlank()) {
            initialUrl
        } else {
            server?.getEffectiveUrl() ?: ""
        }
    }

    LaunchedEffect(effectiveUrl) {
        if (effectiveUrl.isNotBlank() && effectiveUrl != currentUrl) {
            currentUrl = effectiveUrl
            if (!isTunnelRequired || isTunnelRunning) {
                if (webViewInstance?.url != effectiveUrl) {
                    webViewInstance?.loadUrl(effectiveUrl)
                }
            }
        }
    }

    // Auto-load as soon as tunnel starts
    LaunchedEffect(isTunnelRunning) {
        if (isTunnelRunning && effectiveUrl.isNotBlank()) {
            if (webViewInstance?.url != effectiveUrl) {
                webViewInstance?.loadUrl(effectiveUrl)
            }
        }
    }

    BackHandler(enabled = true) {
        if (canGoBack) {
            webViewInstance?.goBack()
        } else {
            onGoToServers?.invoke()
        }
    }

    if (server == null && initialUrl.isBlank()) {
        Box(modifier = modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(56.dp), tint = CyanPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Панель не выбрана", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Выберите панель в списке серверов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onGoToServers?.invoke() }) {
                    Text("Перейти к серверам")
                }
            }
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Ultra-compact header bar (only ~44dp) with status bar insets protection
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                // Back to servers list
                IconButton(
                    onClick = { onGoToServers?.invoke() },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "К серверам")
                }

                // Server title and live status dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                ) {
                    if (isTunnelRequired) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isTunnelRunning) MintSecondary else if (isTunnelError) RedAccent else AmberAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column {
                        Text(
                            text = server?.name ?: "Веб-конфигуратор",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isTunnelRequired) {
                            Text(
                                text = if (isTunnelRunning) {
                                    server?.name?.let { "Туннель ➔ $it" } ?: "Туннель активен"
                                } else if (isTunnelError) "Ошибка подключения"
                                else "Подключение к SSH...",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = if (isTunnelRunning) MintSecondary else if (isTunnelError) RedAccent else AmberAccent,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Open tabs drawer button on right side (Shown when there is at least 1 open tab)
                if (openedServers.isNotEmpty()) {
                    IconButton(
                        onClick = { onOpenDrawer?.invoke() },
                        modifier = Modifier.size(36.dp)
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
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (canShowControls) {
                    IconButton(
                        onClick = {
                            val newMode = !isDesktopMode
                            isDesktopMode = newMode
                            val desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
                            val mobileUA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                            webViewInstance?.settings?.apply {
                                userAgentString = if (newMode) desktopUA else mobileUA
                                useWideViewPort = true
                                loadWithOverviewMode = true
                            }
                            // Inject viewport and min-width directly to force desktop 3x-ui Ant Design layout
                            val js = if (newMode) {
                                """
                                (function() {
                                    var meta = document.querySelector('meta[name="viewport"]');
                                    if (!meta) {
                                        meta = document.createElement('meta');
                                        meta.name = 'viewport';
                                        document.head.appendChild(meta);
                                    }
                                    meta.setAttribute('content', 'width=1280, initial-scale=0.35, minimum-scale=0.25, maximum-scale=3.0, user-scalable=yes');
                                    if (document.documentElement) document.documentElement.style.minWidth = '1280px';
                                    if (document.body) document.body.style.minWidth = '1280px';
                                    window.dispatchEvent(new Event('resize'));
                                })();
                                """.trimIndent()
                            } else {
                                """
                                (function() {
                                    var meta = document.querySelector('meta[name="viewport"]');
                                    if (meta) {
                                        meta.setAttribute('content', 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no');
                                    }
                                    if (document.documentElement) document.documentElement.style.minWidth = '';
                                    if (document.body) document.body.style.minWidth = '';
                                    window.dispatchEvent(new Event('resize'));
                                })();
                                """.trimIndent()
                            }
                            webViewInstance?.evaluateJavascript(js, null)
                            webViewInstance?.reload()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isDesktopMode) Icons.Default.DesktopWindows else Icons.Default.PhoneAndroid,
                            contentDescription = if (isDesktopMode) strings.mobileModeTooltip else strings.desktopModeTooltip,
                            tint = if (isDesktopMode) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { webViewInstance?.reload() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = strings.reloadTooltip, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Slim 2dp loading indicator
        if (isLoading && canShowControls) {
            LinearProgressIndicator(
                progress = { pageProgress },
                color = CyanPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        }

        // Content Area: Either the Connecting Stub, Error Screen, or the 3x-ui WebView
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // 1. Error Screen (Shown if SSH connection failed instead of net::ERR_CONNECTION_REFUSED)
                isTunnelRequired && isTunnelError -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = RedAccent,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Не удалось подключиться к SSH",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tunnelState?.statusMessage ?: "Проверьте SSH-ключ, логин или доступность сервера",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RedAccent,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Цель: ${server?.sshUser}@$displayHost:${server?.sshPort}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { onGoToServers?.invoke() },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Назад к серверам")
                            }
                            Button(
                                onClick = { onStartTunnel?.invoke() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Повторить")
                            }
                        }
                    }
                }

                // 2. Connecting Stub (Заглушка) while SSH tunnel is connecting
                isTunnelRequired && !isTunnelRunning -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    ) {
                        CircularProgressIndicator(
                            color = CyanPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Подключение к SSH-серверу...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Устанавливаем защищенный туннель, панель откроется автоматически",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                Text(
                                    text = server?.name ?: "",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Туннель ➔ ${server?.name ?: "Сервер"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = CyanPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        TextButton(onClick = { onGoToServers?.invoke() }) {
                            Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // 3. Fullscreen WebView (Rendered and active ONLY when tunnel is running or not required)
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    setBackgroundColor(android.graphics.Color.parseColor("#0E141E"))
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )

                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    saveFormData = true
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    userAgentString = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        pageProgress = newProgress / 100f
                                        isLoading = newProgress < 100
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        url?.let { currentUrl = it }
                                        canGoBack = view?.canGoBack() == true
                                        canGoForward = view?.canGoForward() == true
                                        isLoading = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        url?.let { currentUrl = it }
                                        canGoBack = view?.canGoBack() == true
                                        canGoForward = view?.canGoForward() == true
                                        isLoading = false

                                        CookieManager.getInstance().flush()

                                        if (isDesktopMode) {
                                            val desktopScript = """
                                                (function() {
                                                    var meta = document.querySelector('meta[name="viewport"]');
                                                    if (!meta) {
                                                        meta = document.createElement('meta');
                                                        meta.name = 'viewport';
                                                        document.head.appendChild(meta);
                                                    }
                                                    meta.setAttribute('content', 'width=1280, initial-scale=0.35, minimum-scale=0.25, maximum-scale=3.0, user-scalable=yes');
                                                    if (document.documentElement) document.documentElement.style.minWidth = '1280px';
                                                    if (document.body) document.body.style.minWidth = '1280px';
                                                    window.dispatchEvent(new Event('resize'));
                                                })();
                                            """.trimIndent()
                                            view?.evaluateJavascript(desktopScript, null)
                                        }

                                        // Auto-login credentials for 3x-ui Vue 3
                                        if (server != null && server.username.isNotBlank()) {
                                            val autoLoginScript = """
                                                (function() {
                                                    var targetUser = '${server.username}';
                                                    var targetPass = '${server.password}';
                                                    if (!targetUser) return;

                                                    function fillVueInput(el, val) {
                                                        if (!el) return;
                                                        var descriptor = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value');
                                                        if (descriptor && descriptor.set) {
                                                            descriptor.set.call(el, val);
                                                        } else {
                                                            el.value = val;
                                                        }
                                                        el.dispatchEvent(new Event('input', { bubbles: true }));
                                                        el.dispatchEvent(new Event('change', { bubbles: true }));
                                                    }

                                                    function tryInject() {
                                                        var passEl = document.querySelector('input[type="password"]');
                                                        if (!passEl) return false;

                                                        var userEl = document.querySelector('input#username, input[name="username"], input[type="text"]:not([type="hidden"])');
                                                        if (userEl && passEl) {
                                                            fillVueInput(userEl, targetUser);
                                                            fillVueInput(passEl, targetPass);

                                                            setTimeout(function() {
                                                                var btn = document.querySelector('button[type="submit"], button.ant-btn-primary, button.login-btn');
                                                                if (btn && !btn.disabled) {
                                                                    btn.click();
                                                                }
                                                            }, 250);
                                                            return true;
                                                        }
                                                        return false;
                                                    }

                                                    if (!tryInject()) {
                                                        var tries = 0;
                                                        var interval = setInterval(function() {
                                                            tries++;
                                                            if (tryInject() || tries > 12) {
                                                                clearInterval(interval);
                                                            }
                                                        }, 350);
                                                    }
                                                })();
                                            """.trimIndent()
                                            view?.evaluateJavascript(autoLoginScript, null)
                                        }

                                        // Auto-detect 3x-ui panel version from page DOM/window
                                        val versionDetectScript = """
                                            (function() {
                                                return window.X_UI_CUR_VER || 
                                                       (document.querySelector('.ant-layout-header')?.innerText || '').match(/v\d+\.\d+\.\d+/)?.[0] ||
                                                       (document.body?.innerText || '').match(/3[Xx]-[Uu][Ii]\s+v?(\d+\.\d+\.\d+)/i)?.[1] ||
                                                       '';
                                            })();
                                        """.trimIndent()
                                        view?.evaluateJavascript(versionDetectScript) { res ->
                                            val clean = res?.trim('"', '\'', ' ') ?: ""
                                            if (clean.isNotBlank() && clean != "null" && clean != "undefined") {
                                                onPanelVersionDetected?.invoke(clean)
                                            }
                                        }
                                    }

                                    @SuppressLint("WebViewClientOnReceivedSslError")
                                    override fun onReceivedSslError(
                                        view: WebView?,
                                        handler: SslErrorHandler?,
                                        error: SslError?
                                    ) {
                                        handler?.proceed()
                                    }
                                }

                                loadUrl(effectiveUrl)
                                webViewInstance = this
                            }
                        },
                        update = { webView ->
                            webViewInstance = webView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0E141E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = CyanPrimary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Загрузка панели...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
