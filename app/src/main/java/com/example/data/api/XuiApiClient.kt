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
        try {
            val url = buildUrl(baseUrl, "login")
            val formBody = FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .header("User-Agent", "3x-ui-Android-Manager/1.0")
                .header("Accept", "application/json, text/plain, */*")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful && response.code != 200) {
                return@withContext Result.failure(Exception("HTTP error ${response.code}: $bodyString"))
            }

            val adapter = moshi.adapter(BaseResponse::class.java)
            val parsed = try {
                adapter.fromJson(bodyString) ?: BaseResponse(success = false, msg = "Empty response")
            } catch (e: Exception) {
                // Some panels return simple redirect or non-json if already logged in
                if (response.code in 200..299 && bodyString.contains("true", ignoreCase = true)) {
                    BaseResponse(success = true, msg = "Logged in successfully")
                } else {
                    BaseResponse(success = response.isSuccessful, msg = bodyString)
                }
            }

            Result.success(parsed)
        } catch (e: Exception) {
            Log.d(tag, "Login notice: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getServerStatus(baseUrl: String): Result<ServerStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(baseUrl, "server/status")
            val request = Request.Builder()
                .url(url)
                .get()
                .header("Accept", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $body"))
            }

            val adapter = moshi.adapter(ServerStatusResponse::class.java)
            val parsed = adapter.fromJson(body)
                ?: return@withContext Result.failure(Exception("Failed to parse server status"))

            Result.success(parsed)
        } catch (e: Exception) {
            Log.d(tag, "Get server status notice: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getInbounds(baseUrl: String): Result<InboundsListResponse> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(baseUrl, "xui/API/inbounds/list")
            val request = Request.Builder()
                .url(url)
                .get()
                .header("Accept", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $body"))
            }

            val adapter = moshi.adapter(InboundsListResponse::class.java)
            val parsed = adapter.fromJson(body)
                ?: return@withContext Result.failure(Exception("Failed to parse inbounds list"))

            Result.success(parsed)
        } catch (e: Exception) {
            Log.d(tag, "Get inbounds notice: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun restartXray(baseUrl: String): Result<BaseResponse> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(baseUrl, "server/restartXrayService")
            val request = Request.Builder()
                .url(url)
                .post("{}".toRequestBody("application/json".toMediaType()))
                .header("Accept", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val adapter = moshi.adapter(BaseResponse::class.java)
            val parsed = adapter.fromJson(body) ?: BaseResponse(success = response.isSuccessful, msg = body)
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetAllTraffics(baseUrl: String): Result<BaseResponse> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(baseUrl, "xui/API/inbounds/resetAllTraffics")
            val request = Request.Builder()
                .url(url)
                .post("{}".toRequestBody("application/json".toMediaType()))
                .header("Accept", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val adapter = moshi.adapter(BaseResponse::class.java)
            val parsed = adapter.fromJson(body) ?: BaseResponse(success = response.isSuccessful, msg = body)
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInbound(baseUrl: String, inboundId: Int): Result<BaseResponse> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(baseUrl, "xui/API/inbounds/del/$inboundId")
            val request = Request.Builder()
                .url(url)
                .post("{}".toRequestBody("application/json".toMediaType()))
                .header("Accept", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val adapter = moshi.adapter(BaseResponse::class.java)
            val parsed = adapter.fromJson(body) ?: BaseResponse(success = response.isSuccessful, msg = body)
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
