package com.example.ui.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.db.ServerEntity

object ServerWebViewStore {
    private val webViewMap = mutableMapOf<Long, WebView>()
    private val desktopModeMap = mutableMapOf<Long, Boolean>()

    fun getOrCreateWebView(
        context: Context,
        server: ServerEntity,
        initialUrl: String,
        onProgressChanged: (Int) -> Unit,
        onPageStarted: (String) -> Unit,
        onPageFinished: (String) -> Unit,
        onPanelVersionDetected: (String) -> Unit
    ): WebView {
        val serverId = server.id
        val existing = webViewMap[serverId]

        if (existing != null) {
            // Remove from previous parent container if attached
            (existing.parent as? ViewGroup)?.removeView(existing)

            // Re-bind clients so callbacks target current UI state listeners
            bindClients(
                webView = existing,
                server = server,
                onProgressChanged = onProgressChanged,
                onPageStarted = onPageStarted,
                onPageFinished = onPageFinished,
                onPanelVersionDetected = onPanelVersionDetected
            )

            // Load URL ONLY if webView was never loaded or is blank
            val currentUrl = existing.url
            if ((currentUrl.isNullOrBlank() || currentUrl == "about:blank") && initialUrl.isNotBlank()) {
                existing.loadUrl(initialUrl)
            }
            return existing
        }

        // Create new WebView attached to Application Context to avoid activity leaks
        val appContext = context.applicationContext
        val newWebView = WebView(appContext).apply {
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

                val isDesktop = desktopModeMap[serverId] ?: false
                userAgentString = if (isDesktop) {
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
                } else {
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }
            }
        }

        bindClients(
            webView = newWebView,
            server = server,
            onProgressChanged = onProgressChanged,
            onPageStarted = onPageStarted,
            onPageFinished = onPageFinished,
            onPanelVersionDetected = onPanelVersionDetected
        )

        if (initialUrl.isNotBlank()) {
            newWebView.loadUrl(initialUrl)
        }

        webViewMap[serverId] = newWebView
        return newWebView
    }

    private fun bindClients(
        webView: WebView,
        server: ServerEntity,
        onProgressChanged: (Int) -> Unit,
        onPageStarted: (String) -> Unit,
        onPageFinished: (String) -> Unit,
        onPanelVersionDetected: (String) -> Unit
    ) {
        val serverId = server.id

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                onProgressChanged(newProgress)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                url?.let { onPageStarted(it) }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                url?.let { onPageFinished(it) }

                CookieManager.getInstance().flush()

                val isDesktop = desktopModeMap[serverId] ?: false
                if (isDesktop) {
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

                // Auto-login credentials if username is provided
                if (server.username.isNotBlank()) {
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

                // Auto-detect 3x-ui panel version
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
                        onPanelVersionDetected(clean)
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
    }

    fun isDesktopMode(serverId: Long): Boolean {
        return desktopModeMap[serverId] ?: false
    }

    fun toggleDesktopMode(serverId: Long): Boolean {
        val current = desktopModeMap[serverId] ?: false
        val newMode = !current
        desktopModeMap[serverId] = newMode

        val webView = webViewMap[serverId]
        if (webView != null) {
            val desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
            val mobileUA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            webView.settings.apply {
                userAgentString = if (newMode) desktopUA else mobileUA
                useWideViewPort = true
                loadWithOverviewMode = true
            }
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
            webView.evaluateJavascript(js, null)
            webView.reload()
        }
        return newMode
    }

    fun getWebView(serverId: Long): WebView? {
        return webViewMap[serverId]
    }

    fun destroyWebView(serverId: Long) {
        val webView = webViewMap.remove(serverId)
        desktopModeMap.remove(serverId)
        if (webView != null) {
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.stopLoading()
            webView.clearHistory()
            webView.removeAllViews()
            webView.destroy()
        }
    }

    fun destroyAll() {
        val keys = webViewMap.keys.toList()
        for (id in keys) {
            destroyWebView(id)
        }
    }
}
