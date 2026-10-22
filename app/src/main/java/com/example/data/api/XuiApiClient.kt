package com.example.data.api

import android.util.Log
import com.example.data.model.BaseResponse
import com.example.data.model.InboundsListResponse
import com.example.data.model.ServerStatusResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class XuiApiClient {
    private val tag = "XuiApiClient"
    private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = createUnsafeOkHttpClient()

    private fun createUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    val hostKey = url.host
                    val list = cookieStore.getOrPut(hostKey) { mutableListOf() }
                    for (cookie in cookies) {
                        list.removeAll { it.name == cookie.name }
                        list.add(cookie)
                    }
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieStore[url.host] ?: emptyList()
                }
            })
            .build()
    }

    fun clearCookies() {
        cookieStore.clear()
    }

    private fun buildUrl(baseUrl: String, endpoint: String): String {
        val cleanBase = baseUrl.trim().trimEnd('/')
        val cleanEndpoint = endpoint.trim().trimStart('/')
        return "$cleanBase/$cleanEndpoint"
    }

    suspend fun login(
        baseUrl: String,
        username: String,
        password: String
    ): Result<BaseResponse> = withContext(Dispatchers.IO) {
        val endpoints = listOf("login", "panel/login", "xui/login")
        var lastException: Exception? = null

        for (ep in endpoints) {
            try {
                val url = buildUrl(baseUrl, ep)
                val formBody = FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(formBody)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                    .header("Accept", "application/json, text/plain, */*")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val bodyString = response.body?.string() ?: ""

                if (response.isSuccessful || response.code in 200..299) {
                    val adapter = moshi.adapter(BaseResponse::class.java)
                    val parsed = try {
                        adapter.fromJson(bodyString) ?: BaseResponse(success = true, msg = "Logged in")
                    } catch (_: Exception) {
                        BaseResponse(success = true, msg = "Logged in")
                    }
                    val hostKey = url.toHttpUrlOrNull()?.host ?: ""
                    val hasCookies = cookieStore[hostKey]?.isNotEmpty() == true
                    if (parsed.success || bodyString.contains("true", ignoreCase = true) || hasCookies) {
                        return@withContext Result.success(parsed)
                    }
                } else if (response.code != 404) {
                    lastException = Exception("HTTP error ${response.code}: $bodyString")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        Result.failure(lastException ?: Exception("Login failed"))
    }

    private fun Request.Builder.withApiKey(apiKey: String): Request.Builder {
        val key = apiKey.trim()
        if (key.isNotBlank()) {
            header("x-api-key", key)
            header("X-Api-Key", key)
            header("Authorization", "Bearer $key")
            header("token", key)
        }
        return this
    }

    suspend fun getServerStatus(baseUrl: String, apiKey: String = ""): Result<ServerStatusResponse> = withContext(Dispatchers.IO) {
        val endpoints = listOf(
            "server/status",
            "panel/api/server/status",
            "xui/API/server/status",
            "api/server/status"
        )
        var lastException: Exception? = null

        for (ep in endpoints) {
            try {
                val url = buildUrl(baseUrl, ep)
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .header("Accept", "application/json, text/plain, */*")
                    .withApiKey(apiKey)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val adapter = moshi.adapter(ServerStatusResponse::class.java)
                    val parsed = adapter.fromJson(body)
                    if (parsed != null && (parsed.success || parsed.obj != null)) {
                        return@withContext Result.success(parsed)
                    }
                } else if (response.code != 404) {
                    lastException = Exception("HTTP ${response.code}: $body")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        Result.failure(lastException ?: Exception("Failed to fetch server status"))
    }

    suspend fun getInbounds(baseUrl: String, apiKey: String = ""): Result<InboundsListResponse> = withContext(Dispatchers.IO) {
        val endpoints = listOf(
            "xui/API/inbounds/list",
            "panel/api/inbounds/list",
            "api/inbounds/list",
            "inbounds/list"
        )
        var lastException: Exception? = null

        for (ep in endpoints) {
            try {
                val url = buildUrl(baseUrl, ep)
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .header("Accept", "application/json, text/plain, */*")
                    .withApiKey(apiKey)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val adapter = moshi.adapter(InboundsListResponse::class.java)
                    val parsed = adapter.fromJson(body)
                    if (parsed != null && (parsed.success || parsed.obj != null)) {
                        return@withContext Result.success(parsed)
                    }
                } else if (response.code != 404) {
                    lastException = Exception("HTTP ${response.code}: $body")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        Result.failure(lastException ?: Exception("Failed to fetch inbounds list"))
    }

    suspend fun restartXray(baseUrl: String, apiKey: String = ""): Result<BaseResponse> = withContext(Dispatchers.IO) {
        val endpoints = listOf(
            "server/restartXrayService",
            "panel/api/server/restartXrayService",
            "xui/API/server/restartXrayService"
        )
        var lastException: Exception? = null

        for (ep in endpoints) {
            try {
                val url = buildUrl(baseUrl, ep)
                val request = Request.Builder()
                    .url(url)
                    .post("{}".toRequestBody("application/json".toMediaType()))
                    .header("Accept", "application/json, text/plain, */*")
                    .withApiKey(apiKey)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""
                val adapter = moshi.adapter(BaseResponse::class.java)
                val parsed = adapter.fromJson(body) ?: BaseResponse(success = response.isSuccessful, msg = body)
                if (response.isSuccessful && parsed.success) {
                    return@withContext Result.success(parsed)
                } else if (response.code != 404) {
                    lastException = Exception("HTTP ${response.code}: $body")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        Result.failure(lastException ?: Exception("Failed to restart Xray"))
    }

    suspend fun resetAllTraffics(baseUrl: String, apiKey: String = ""): Result<BaseResponse> = withContext(Dispatchers.IO) {
        val endpoints = listOf(
            "xui/API/inbounds/resetAllTraffics",
            "panel/api/inbounds/resetAllTraffics",
            "api/inbounds/resetAllTraffics"
        )
        var lastException: Exception? = null

        for (ep in endpoints) {
            try {
                val url = buildUrl(baseUrl, ep)
                val request = Request.Builder()
                    .url(url)
                    .post("{}".toRequestBody("application/json".toMediaType()))
                    .header("Accept", "application/json, text/plain, */*")
                    .withApiKey(apiKey)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""
                val adapter = moshi.adapter(BaseResponse::class.java)
                val parsed = adapter.fromJson(body) ?: BaseResponse(success = response.isSuccessful, msg = body)
                if (response.isSuccessful && parsed.success) {
                    return@withContext Result.success(parsed)
                } else if (response.code != 404) {
                    lastException = Exception("HTTP ${response.code}: $body")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        Result.failure(lastException ?: Exception("Failed to reset all traffics"))
    }

    suspend fun deleteInbound(baseUrl: String, inboundId: Int, apiKey: String = ""): Result<BaseResponse> = withContext(Dispatchers.IO) {
        val endpoints = listOf(
            "xui/API/inbounds/del/$inboundId",
            "panel/api/inbounds/del/$inboundId",
            "api/inbounds/del/$inboundId"
        )
        var lastException: Exception? = null

        for (ep in endpoints) {
            try {
                val url = buildUrl(baseUrl, ep)
                val request = Request.Builder()
                    .url(url)
                    .post("{}".toRequestBody("application/json".toMediaType()))
                    .header("Accept", "application/json, text/plain, */*")
                    .withApiKey(apiKey)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""
                val adapter = moshi.adapter(BaseResponse::class.java)
                val parsed = adapter.fromJson(body) ?: BaseResponse(success = response.isSuccessful, msg = body)
                if (response.isSuccessful && parsed.success) {
                    return@withContext Result.success(parsed)
                } else if (response.code != 404) {
                    lastException = Exception("HTTP ${response.code}: $body")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        Result.failure(lastException ?: Exception("Failed to delete inbound"))
    }

    suspend fun getPanelUpdateInfo(baseUrl: String, apiKey: String = ""): Result<com.example.data.model.PanelUpdateInfoResponse> = withContext(Dispatchers.IO) {
        val endpoints = listOf(
            "server/getPanelUpdateInfo",
            "panel/api/server/getPanelUpdateInfo",
            "xui/API/server/getPanelUpdateInfo"
        )
        var lastException: Exception? = null

        for (ep in endpoints) {
            try {
                val url = buildUrl(baseUrl, ep)
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .header("Accept", "application/json, text/plain, */*")
                    .withApiKey(apiKey)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val adapter = moshi.adapter(com.example.data.model.PanelUpdateInfoResponse::class.java)
                    val parsed = adapter.fromJson(body)
                    if (parsed != null && parsed.success) {
                        return@withContext Result.success(parsed)
                    }
                } else if (response.code != 404) {
                    lastException = Exception("HTTP ${response.code}: $body")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        Result.failure(lastException ?: Exception("Failed to parse panel update info"))
    }
}
