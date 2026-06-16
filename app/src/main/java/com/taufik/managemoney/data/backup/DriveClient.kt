package com.taufik.managemoney.data.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class DriveFile(val id: String, val name: String? = null, val size: String? = null, val modifiedTime: String? = null)

@Serializable
private data class DriveFileList(val files: List<DriveFile> = emptyList())

/**
 * Klien ringan Google Drive v3 REST untuk folder privat app (`appDataFolder`).
 * Reuse OkHttp + kotlinx.serialization yang sudah ada (tanpa google-api-services-drive yang berat).
 * Bearer = access token dari [DriveAuthManager].
 */
@Singleton
class DriveClient @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) {
    private val apiBase = "https://www.googleapis.com/drive/v3/files"
    private val uploadBase = "https://www.googleapis.com/upload/drive/v3/files"

    /** Cari file backup bernama [name] di appDataFolder; null bila belum ada. */
    suspend fun findBackup(token: String, name: String): DriveFile? = withContext(Dispatchers.IO) {
        val url = apiBase.toHttpUrl().newBuilder()
            .addQueryParameter("spaces", "appDataFolder")
            .addQueryParameter("q", "name = '$name'")
            .addQueryParameter("fields", "files(id,name,size,modifiedTime)")
            .build()
        val req = Request.Builder().url(url).header("Authorization", "Bearer $token").get().build()
        client.newCall(req).execute().use { resp ->
            check(resp.isSuccessful) { "Drive list gagal: ${resp.code}" }
            val body = resp.body?.string().orEmpty()
            json.decodeFromString(DriveFileList.serializer(), body).files.firstOrNull()
        }
    }

    /** Buat backup baru (multipart) atau timpa [existingId] (media). Mengembalikan metadata file. */
    suspend fun upload(token: String, name: String, bytes: ByteArray, existingId: String?): DriveFile =
        withContext(Dispatchers.IO) {
            val req = if (existingId == null) {
                val metadata = """{"name":"$name","parents":["appDataFolder"]}"""
                val multipart = MultipartBody.Builder().setType("multipart/related".toMediaType())
                    .addPart(metadata.toRequestBody("application/json; charset=UTF-8".toMediaType()))
                    .addPart(bytes.toRequestBody("application/octet-stream".toMediaType()))
                    .build()
                val url = uploadBase.toHttpUrl().newBuilder()
                    .addQueryParameter("uploadType", "multipart")
                    .addQueryParameter("fields", "id,name,size,modifiedTime")
                    .build()
                Request.Builder().url(url).header("Authorization", "Bearer $token").post(multipart).build()
            } else {
                val url = "$uploadBase/$existingId".toHttpUrl().newBuilder()
                    .addQueryParameter("uploadType", "media")
                    .addQueryParameter("fields", "id,name,size,modifiedTime")
                    .build()
                Request.Builder().url(url).header("Authorization", "Bearer $token")
                    .patch(bytes.toRequestBody("application/octet-stream".toMediaType())).build()
            }
            client.newCall(req).execute().use { resp ->
                check(resp.isSuccessful) { "Drive upload gagal: ${resp.code}" }
                json.decodeFromString(DriveFile.serializer(), resp.body?.string().orEmpty())
            }
        }

    /** Unduh isi file backup. */
    suspend fun download(token: String, fileId: String): ByteArray = withContext(Dispatchers.IO) {
        val url = "$apiBase/$fileId".toHttpUrl().newBuilder().addQueryParameter("alt", "media").build()
        val req = Request.Builder().url(url).header("Authorization", "Bearer $token").get().build()
        client.newCall(req).execute().use { resp ->
            check(resp.isSuccessful) { "Drive download gagal: ${resp.code}" }
            resp.body?.bytes() ?: error("Body kosong")
        }
    }
}
