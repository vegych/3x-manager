package com.example.data.backup

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GoogleDriveBackupManager {
    private const val TAG = "GoogleDriveBackup"
    private const val BACKUP_FILENAME = "3xui_backup.json"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val DRIVE_SCOPE = Scope("https://www.googleapis.com/auth/drive.file")
    private val DRIVE_APPDATA_SCOPE = Scope("https://www.googleapis.com/auth/drive.appdata")

    fun getSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(DRIVE_SCOPE, DRIVE_APPDATA_SCOPE)
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null && GoogleSignIn.hasPermissions(account, DRIVE_SCOPE)) {
            account
        } else {
            null
        }
    }

    private suspend fun getAccessToken(context: Context, account: GoogleSignInAccount): String = withContext(Dispatchers.IO) {
        val act = account.account ?: throw Exception("Google Account не найден")
        val scopeString = "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/drive.appdata"
        GoogleAuthUtil.getToken(context, act, scopeString)
    }

    private suspend fun findBackupFileId(token: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.googleapis.com/drive/v3/files?q=name='$BACKUP_FILENAME' and trashed=false&fields=files(id,name,modifiedTime)"
            val req = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val res = httpClient.newCall(req).execute()
            if (!res.isSuccessful) return@withContext null
            val body = res.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val files = json.optJSONArray("files") ?: return@withContext null
            if (files.length() > 0) {
                return@withContext files.getJSONObject(0).getString("id")
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "findBackupFileId error", e)
            null
        }
    }

    suspend fun uploadBackup(context: Context, account: GoogleSignInAccount, jsonContent: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = getAccessToken(context, account)
            val existingId = findBackupFileId(token)

            val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

            if (existingId != null) {
                // Update existing file content
                val updateUrl = "https://www.googleapis.com/upload/drive/v3/files/$existingId?uploadType=media"
                val req = Request.Builder()
                    .url(updateUrl)
                    .addHeader("Authorization", "Bearer $token")
                    .patch(jsonContent.toRequestBody(mediaTypeJson))
                    .build()

                val res = httpClient.newCall(req).execute()
                if (res.isSuccessful) {
                    Result.success("Бэкап успешно обновлен на Google Диске")
                } else {
                    Result.failure(Exception("Ошибка обновления Google Drive: HTTP ${res.code}"))
                }
            } else {
                // Create new backup file via multipart upload
                val metadata = JSONObject().apply {
                    put("name", BACKUP_FILENAME)
                    put("mimeType", "application/json")
                }.toString()

                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(metadata.toRequestBody("application/json; charset=UTF-8".toMediaType()))
                    .addPart(jsonContent.toRequestBody(mediaTypeJson))
                    .build()

                val createUrl = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
                val req = Request.Builder()
                    .url(createUrl)
                    .addHeader("Authorization", "Bearer $token")
                    .post(body)
                    .build()

                val res = httpClient.newCall(req).execute()
                if (res.isSuccessful) {
                    Result.success("Бэкап успешно сохранен на Google Диск")
                } else {
                    Result.failure(Exception("Ошибка загрузки на Google Drive: HTTP ${res.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadBackup error", e)
            Result.failure(Exception("Ошибка Google Drive: ${e.message}"))
        }
    }

    suspend fun downloadBackup(context: Context, account: GoogleSignInAccount): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = getAccessToken(context, account)
            val fileId = findBackupFileId(token)
                ?: return@withContext Result.failure(Exception("Файл бэкапа 3xui_backup.json не найден на Google Диске"))

            val downloadUrl = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
            val req = Request.Builder()
                .url(downloadUrl)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val res = httpClient.newCall(req).execute()
            if (res.isSuccessful) {
                val content = res.body?.string()
                if (!content.isNullOrBlank()) {
                    Result.success(content)
                } else {
                    Result.failure(Exception("Файл на Google Диске пуст"))
                }
            } else {
                Result.failure(Exception("Не удалось скачать бэкап: HTTP ${res.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "downloadBackup error", e)
            Result.failure(Exception("Ошибка Google Drive: ${e.message}"))
        }
    }
}
