package com.example.data.backup

import android.content.Context
import android.util.Log
import com.example.data.db.ServerEntity
import com.example.data.db.TunnelConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object BackupManager {
    private const val TAG = "BackupManager"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Creates a formatted JSON backup of all servers, tunnels, and settings.
     */
    fun createBackupJson(
        servers: List<ServerEntity>,
        tunnels: List<TunnelConfigEntity> = emptyList(),
        closePolicy: String = "ON_APP_CLOSE"
    ): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("appName", "3x-ui manager")

        val serversArray = JSONArray()
        for (server in servers) {
            val sObj = JSONObject()
            sObj.put("name", server.name)
            sObj.put("host", server.host)
            sObj.put("port", server.port)
            sObj.put("useHttps", server.useHttps)
            sObj.put("basePath", server.basePath)
            sObj.put("username", server.username)
            sObj.put("password", server.password)
            sObj.put("useTunnel", server.useTunnel)
            sObj.put("autoConnectTunnel", server.autoConnectTunnel)
            sObj.put("sshHost", server.sshHost)
            sObj.put("sshPort", server.sshPort)
            sObj.put("sshUser", server.sshUser)
            sObj.put("sshAuthType", server.sshAuthType)
            sObj.put("sshPassword", server.sshPassword)
            sObj.put("sshKey", server.sshKey)
            sObj.put("sshKeyPassphrase", server.sshKeyPassphrase)
            sObj.put("localPort", server.localPort)
            sObj.put("remoteTargetHost", server.remoteTargetHost)
            sObj.put("remoteTargetPort", server.remoteTargetPort)
            sObj.put("bindToLan", server.bindToLan)
            serversArray.put(sObj)
        }
        root.put("servers", serversArray)

        val settingsObj = JSONObject()
        settingsObj.put("tunnel_close_policy", closePolicy)
        root.put("settings", settingsObj)

        return root.toString(2)
    }

    /**
     * Parses a JSON backup string into a list of ServerEntity objects and settings.
     */
    fun parseBackupJson(jsonString: String): Result<Pair<List<ServerEntity>, Map<String, String>>> {
        return try {
            val root = JSONObject(jsonString)
            val serversArray = root.optJSONArray("servers") ?: JSONArray()
            val serversList = mutableListOf<ServerEntity>()

            for (i in 0 until serversArray.length()) {
                val sObj = serversArray.getJSONObject(i)
                val server = ServerEntity(
                    name = sObj.optString("name", "3x-ui server"),
                    host = sObj.optString("host", "127.0.0.1"),
                    port = sObj.optInt("port", 2053),
                    useHttps = sObj.optBoolean("useHttps", true),
                    basePath = sObj.optString("basePath", ""),
                    username = sObj.optString("username", "admin"),
                    password = sObj.optString("password", ""),
                    useTunnel = sObj.optBoolean("useTunnel", false),
                    autoConnectTunnel = sObj.optBoolean("autoConnectTunnel", true),
                    sshHost = sObj.optString("sshHost", ""),
                    sshPort = sObj.optInt("sshPort", 22),
                    sshUser = sObj.optString("sshUser", "root"),
                    sshAuthType = sObj.optString("sshAuthType", "KEY"),
                    sshPassword = sObj.optString("sshPassword", ""),
                    sshKey = sObj.optString("sshKey", ""),
                    sshKeyPassphrase = sObj.optString("sshKeyPassphrase", ""),
                    localPort = sObj.optInt("localPort", 2370),
                    remoteTargetHost = sObj.optString("remoteTargetHost", "127.0.0.1"),
                    remoteTargetPort = sObj.optInt("remoteTargetPort", 2370),
                    bindToLan = sObj.optBoolean("bindToLan", true)
                )
                serversList.add(server)
            }

            val settingsMap = mutableMapOf<String, String>()
            val settingsObj = root.optJSONObject("settings")
            if (settingsObj != null) {
                val keys = settingsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    settingsMap[key] = settingsObj.optString(key, "")
                }
            }

            Result.success(Pair(serversList, settingsMap))
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing backup JSON", e)
            Result.failure(Exception("Неверный формат файла резервной копии: ${e.message}"))
        }
    }

    /**
     * Uploads the backup JSON to a WebDAV server using HTTP PUT.
     */
    suspend fun uploadToWebdav(
        webdavUrl: String,
        username: String,
        password: String,
        jsonContent: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            var url = webdavUrl.trim()
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            if (url.endsWith("/")) {
                url += "3xui_backup.json"
            }

            val requestBuilder = Request.Builder()
                .url(url)
                .put(jsonContent.toRequestBody("application/json; charset=utf-8".toMediaType()))

            if (username.isNotBlank() || password.isNotBlank()) {
                requestBuilder.header("Authorization", Credentials.basic(username.trim(), password))
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful || response.code == 201 || response.code == 204) {
                Result.success("Бэкап успешно выгружен на WebDAV (HTTP ${response.code})")
            } else {
                Result.failure(Exception("Ошибка WebDAV сервера: HTTP ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "WebDAV upload failed", e)
            Result.failure(Exception("Не удалось загрузить на WebDAV: ${e.message}"))
        }
    }

    /**
     * Downloads the backup JSON from a WebDAV server using HTTP GET.
     */
    suspend fun downloadFromWebdav(
        webdavUrl: String,
        username: String,
        password: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            var url = webdavUrl.trim()
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            if (url.endsWith("/")) {
                url += "3xui_backup.json"
            }

            val requestBuilder = Request.Builder()
                .url(url)
                .get()

            if (username.isNotBlank() || password.isNotBlank()) {
                requestBuilder.header("Authorization", Credentials.basic(username.trim(), password))
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("WebDAV вернул пустой ответ"))
                }
            } else {
                Result.failure(Exception("Ошибка WebDAV: HTTP ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "WebDAV download failed", e)
            Result.failure(Exception("Не удалось загрузить с WebDAV: ${e.message}"))
        }
    }
}
