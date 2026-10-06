package com.example.data.updater

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

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
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
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

    private fun extractVersion(tag: String, name: String): String {
        val versionPattern = Pattern.compile("(\\d+\\.\\d+(\\.\\d+)?)")
        val matcherTag = versionPattern.matcher(tag)
        if (matcherTag.find()) {
            return matcherTag.group(1) ?: tag
        }
        val matcherName = versionPattern.matcher(name)
        if (matcherName.find()) {
            return matcherName.group(1) ?: name
        }
        return tag.removePrefix("v").trim()
    }

    private fun fetchReleaseInfo(repo: String): UpdateInfo? {
        val urlLatest = "https://api.github.com/repos/$REPO_OWNER/$repo/releases/latest"
        val request = Request.Builder()
            .url(urlLatest)
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "3x-manager-Android")
            .build()

        var json: JSONObject? = null
        try {
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    json = JSONObject(body)
                }
            } else {
                response.close()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get latest release, trying release list", e)
        }

        // Fallback to releases list if /latest is 404 or empty
        if (json == null) {
            try {
                val listUrl = "https://api.github.com/repos/$REPO_OWNER/$repo/releases"
                val listRequest = Request.Builder()
                    .url(listUrl)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "3x-manager-Android")
                    .build()
                val listResponse = httpClient.newCall(listRequest).execute()
                if (listResponse.isSuccessful) {
                    val body = listResponse.body?.string()
                    if (body != null) {
                        val arr = JSONArray(body)
                        if (arr.length() > 0) {
                            json = arr.getJSONObject(0)
                        }
                    }
                } else {
                    listResponse.close()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to list releases", e)
            }
        }

        if (json == null) return null

        val rawTag = json.optString("tag_name", "").trim()
        val releaseTitle = json.optString("name", "Новая версия")
        val cleanVersion = extractVersion(rawTag, releaseTitle)
        val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v").trim()
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

        val hasUpdate = isVersionNewer(cleanVersion, currentVersion)

        return UpdateInfo(
            latestVersion = if (cleanVersion.isNotBlank()) "v$cleanVersion" else rawTag,
            currentVersion = "v$currentVersion",
            hasUpdate = hasUpdate,
            releaseTitle = releaseTitle,
            releaseNotes = releaseNotes,
            downloadUrl = apkDownloadUrl,
            releasePageUrl = releasePageUrl
        )
    }

    /**
     * Compares semantic version strings like 1.0.22 vs 1.0.0
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
     * Downloads APK directly with progress and immediately triggers Android Package Installer
     */
    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        versionName: String,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val cleanV = versionName.removePrefix("v").trim()
            val fileName = "3x-manager-v$cleanV.apk"
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            if (!downloadDir.exists()) downloadDir.mkdirs()
            val apkFile = File(downloadDir, fileName)
            if (apkFile.exists()) apkFile.delete()

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "3x-manager-Android")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty body"))
            val contentLength = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { inputStream ->
                FileOutputStream(apkFile).use { outputStream ->
                    val buffer = ByteArray(32 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        downloadedBytes += read
                        if (contentLength > 0) {
                            val progress = downloadedBytes.toFloat() / contentLength.toFloat()
                            withContext(Dispatchers.Main) {
                                onProgress(progress.coerceIn(0f, 1f))
                            }
                        }
                    }
                    outputStream.flush()
                }
            }

            withContext(Dispatchers.Main) {
                onProgress(1f)
                installApk(context, apkFile)
            }

            Result.success(apkFile)
        } catch (e: Exception) {
            Log.e(TAG, "Download & Install failed", e)
            Result.failure(e)
        }
    }

    /**
     * Checks unknown app sources permission on Android 8.0+ and launches Package Installer
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                }
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            openBrowser(context, "https://github.com/$REPO_OWNER/$REPO_NAME/releases")
        }
    }

    /**
     * Enqueues download via system DownloadManager
     */
    fun downloadViaSystemManager(context: Context, downloadUrl: String, versionName: String) {
        try {
            val cleanV = versionName.removePrefix("v").trim()
            val fileName = "3x-manager-v$cleanV.apk"
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("3x manager $versionName")
                setDescription("Загрузка обновления $versionName")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType("application/vnd.android.package-archive")
            }
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        } catch (e: Exception) {
            Log.e(TAG, "DownloadManager failed, opening browser", e)
            openBrowser(context, downloadUrl)
        }
    }

    fun openBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open browser", e)
        }
    }
}
