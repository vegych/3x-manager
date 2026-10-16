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

    @Volatile
    private var autoCloseSponsorAdsEnabled: Boolean = true

    fun setAutoCloseSponsorAds(enabled: Boolean) {
        autoCloseSponsorAdsEnabled = enabled
    }

    private val SPONSOR_BLOCK_SCRIPT = """
        (function() {
            function injectSuppressionStyle() {
                if (document.getElementById('__instant_sponsor_block_style')) return;
                var style = document.createElement('style');
                style.id = '__instant_sponsor_block_style';
                style.textContent = `
                    .ant-modal-root:has(a[href*="sanaei"]),
                    .ant-modal-root:has(a[href*="nexora"]),
                    .ant-modal-wrap:has(a[href*="sanaei"]),
                    .ant-modal-wrap:has(a[href*="nexora"]),
                    .ant-modal:has(a[href*="sanaei"]),
                    .ant-modal:has(a[href*="nexora"]),
                    .ant-alert:has(a[href*="sanaei"]),
                    .ant-alert:has(a[href*="nexora"]),
                    .ant-card:has(a[href*="sanaei"]),
                    .ant-card:has(a[href*="nexora"]),
                    [class*="sponsor"],
                    [id*="sponsor"],
                    [class*="ad-banner"],
                    [class*="advertisement"],
                    .sponsor-card,
                    .sponsor-banner,
                    .sponsor-modal,
                    .__sponsor_hidden {
                        display: none !important;
                        visibility: hidden !important;
                        opacity: 0 !important;
                        pointer-events: none !important;
                        height: 0 !important;
                        max-height: 0 !important;
                        overflow: hidden !important;
                    }
                `;
                (document.head || document.documentElement).appendChild(style);
            }

            function dismissElement(el) {
                if (!el) return;
                el.classList.add('__sponsor_hidden');
                el.style.setProperty('display', 'none', 'important');
                el.style.setProperty('visibility', 'hidden', 'important');
                el.style.setProperty('opacity', '0', 'important');
                el.style.setProperty('pointer-events', 'none', 'important');

                // Click close icon if present to persist dismissal in panel state
                var closeBtn = el.querySelector('.ant-modal-close, .ant-alert-close-icon, .anticon-close, button[aria-label="Close"], button[aria-label="close"], button.close, [class*="close"]');
                if (closeBtn && typeof closeBtn.click === 'function') {
                    try { closeBtn.click(); } catch(e) {}
                }

                // If element is a modal or wrapped in modal, purge root and backdrop mask
                var modalWrap = el.closest('.ant-modal-wrap') || el.closest('.ant-modal-root');
                if (modalWrap) {
                    modalWrap.classList.add('__sponsor_hidden');
                    modalWrap.style.setProperty('display', 'none', 'important');
                    var mask = modalWrap.parentElement ? modalWrap.parentElement.querySelector('.ant-modal-mask') : null;
                    if (mask) {
                        mask.style.setProperty('display', 'none', 'important');
                        try { mask.parentNode.removeChild(mask); } catch(e) {}
                    }
                    document.body.classList.remove('ant-scrolling-effect');
                    document.body.style.removeProperty('overflow');
                    document.body.style.removeProperty('width');
                    try { modalWrap.parentNode.removeChild(modalWrap); } catch(e) {}
                } else {
                    try {
                        if (el.parentNode) el.parentNode.removeChild(el);
                    } catch(e) {}
                }
            }

            function scanAndPurge() {
                injectSuppressionStyle();

                // 1. Direct sponsor links (Nexora, Sanaei, Telegram sponsor channels)
                var sponsorLinks = document.querySelectorAll('a[href*="sanaei"], a[href*="nexora"], a[href*="sponsor"], a[href*="t.me/nexora"], a[href*="t.me/sanaei"]');
                sponsorLinks.forEach(function(link) {
                    var container = link.closest('.ant-modal, .ant-modal-wrap, .ant-alert, .ant-card, [class*="card"], [class*="alert"], div');
                    if (container && container !== document.body && container !== document.documentElement) {
                        dismissElement(container);
                    }
                });

                // 2. Alert banners or modal dialogs containing sponsor text
                var targets = document.querySelectorAll('.ant-alert, .ant-modal, .ant-card, [class*="sponsor"], [class*="ad-banner"], [class*="advertisement"]');
                targets.forEach(function(el) {
                    var txt = (el.innerText || '').toLowerCase();
                    if (txt.includes('спонсор') || txt.includes('sponsor') || txt.includes('nexora') || txt.includes('sanaei')) {
                        dismissElement(el);
                    }
                });
            }

            // Run immediately
            scanAndPurge();

            // MutationObserver for sub-millisecond execution on dynamic DOM changes
            if (window.MutationObserver && !window.__sponsorObserver) {
                window.__sponsorObserver = new MutationObserver(function() {
                    scanAndPurge();
                });
                var root = document.body || document.documentElement;
                if (root) {
                    window.__sponsorObserver.observe(root, { childList: true, subtree: true });
                }
            }

            // 30ms high-speed interval loop during first 3 seconds of load
            if (!window.__sponsorFastInterval) {
                var fastCycles = 0;
                window.__sponsorFastInterval = setInterval(function() {
                    fastCycles++;
                    scanAndPurge();
                    if (fastCycles > 100) {
                        clearInterval(window.__sponsorFastInterval);
                        setInterval(scanAndPurge, 300);
                    }
                }, 30);
            }
        })();
    """.trimIndent()

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
                if (autoCloseSponsorAdsEnabled && newProgress in 15..95) {
                    view?.evaluateJavascript(SPONSOR_BLOCK_SCRIPT, null)
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                url?.let { onPageStarted(it) }
                if (autoCloseSponsorAdsEnabled) {
                    view?.evaluateJavascript(SPONSOR_BLOCK_SCRIPT, null)
                }
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

                // Ultra-fast instant suppression & auto-close of sponsor banners (Nexora, SANAEI, Спонсор, etc.)
                if (autoCloseSponsorAdsEnabled) {
                    view?.evaluateJavascript(SPONSOR_BLOCK_SCRIPT, null)
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
