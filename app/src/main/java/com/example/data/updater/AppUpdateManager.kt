package com.example.data.updater

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val latestVersion: String,
    val currentVersion: String,
    val hasUpdate: Boolean,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String?,
    val releasePageUrl: String
)

object AppUpdateManager {
    private const val TAG = "AppUpdateManager"
    private const val REPO_OWNER = "vegych"
    private const val REPO_NAME = "3x-ui-manager"
    private const val FALLBACK_REPO_NAME = "3x-manager"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    suspend fun checkForUpdates(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            var info = fetchReleaseInfo(REPO_NAME)
            if (info == null) {
                info = fetchReleaseInfo(FALLBACK_REPO_NAME)
            }

            if (info != null) {
                Result.success(info)
            } else {
                Result.failure(Exception("Релизы на GitHub пока не найдены"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking updates", e)
            Result.failure(e)
        }
    }

    private fun fetchReleaseInfo(repo: String): UpdateInfo? {
        val url = "https://api.github.com/repos/$REPO_OWNER/$repo/releases/latest"
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "3x-manager-Android")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            response.close()
            return null
        }

        val body = response.body?.string() ?: return null
        val json = JSONObject(body)

        val tagName = json.optString("tag_name", "").trim()
        val cleanTag = tagName.removePrefix("v").trim()
        val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v").trim()
        val releaseTitle = json.optString("name", "Новая версия")
        val releaseNotes = json.optString("body", "")
        val releasePageUrl = json.optString("html_url", "https://github.com/$REPO_OWNER/$repo/releases")

        var apkDownloadUrl: String? = null
        val assets = json.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name", "")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkDownloadUrl = asset.optString("browser_download_url")
                    break
                }
            }
        }

        val hasUpdate = isVersionNewer(cleanTag, currentVersion)

        return UpdateInfo(
            latestVersion = tagName.ifBlank { "v$cleanTag" },
            currentVersion = "v$currentVersion",
            hasUpdate = hasUpdate,
            releaseTitle = releaseTitle,
            releaseNotes = releaseNotes,
            downloadUrl = apkDownloadUrl,
            releasePageUrl = releasePageUrl
        )
    }

    /**
     * Compares version strings like 1.0.5 vs 1.0.0
     */
    private fun isVersionNewer(latest: String, current: String): Boolean {
        if (latest.isBlank()) return false
        if (current.isBlank()) return true
        if (latest == current) return false

        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    /**
     * Downloads APK via Android DownloadManager or browser with exact version naming
     */
    fun startDownload(context: Context, downloadUrl: String, versionName: String = "") {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            if (downloadManager != null) {
                val uri = Uri.parse(downloadUrl)
                val urlFileName = downloadUrl.substringAfterLast("/").substringBefore("?").trim()
                val targetFileName = if (urlFileName.endsWith(".apk", ignoreCase = true)) {
                    urlFileName
                } else if (versionName.isNotBlank()) {
                    val cleanV = if (versionName.startsWith("v", ignoreCase = true)) versionName else "v$versionName"
                    "3x-manager-$cleanV.apk"
                } else {
                    "3x-manager-update.apk"
                }

                val title = if (versionName.isNotBlank()) "3x manager $versionName" else "3x manager"

                val request = DownloadManager.Request(uri)
                    .setTitle(title)
                    .setDescription("Загрузка $targetFileName...")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, targetFileName)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

                downloadManager.enqueue(request)
            } else {
                openBrowser(context, downloadUrl)
            }
        } catch (e: Exception) {
            openBrowser(context, downloadUrl)
        }
    }

    fun openBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
