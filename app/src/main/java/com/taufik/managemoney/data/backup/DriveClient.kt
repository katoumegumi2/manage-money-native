package com.taufik.managemoney.data.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
 * Klien ringan Google Drive v3 REST, scope `drive.file`: hanya melihat file/folder yang dibuat app ini
 * (lintas perangkat, selama project OAuth sama). Backup disimpan di folder bernama jelas di My Drive user.
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
    private val fields = "id,name,size,modifiedTime"

    /** Cari file (bukan di tempat sampah) bernama [name]; terbaru lebih dulu. null bila belum ada. */
    private suspend fun findByName(token: String, name: String, mimeType: String? = null): DriveFile? =
        withContext(Dispatchers.IO) {
            val q = buildString {
                append("name = '").append(name.replace("'", "\\'")).append("' and trashed = false")
                if (mimeType != null) append(" and mimeType = '").append(mimeType).append("'")
            }
            val url = apiBase.toHttpUrl().newBuilder()
                .addQueryParameter("q", q)
                .addQueryParameter("orderBy", "modifiedTime desc")
                .addQueryParameter("fields", "files($fields)")
                .build()
            val req = Request.Builder().url(url).header("Authorization", "Bearer $token").get().build()
            client.newCall(req).execute().use { resp ->
                check(resp.isSuccessful) { "Drive list gagal: ${resp.code}" }
                val body = resp.body?.string().orEmpty()
                json.decodeFromString(DriveFileList.serializer(), body).files.firstOrNull()
            }
        }

    /**
     * Cari file backup bernama [name]. Dicari di seluruh Drive (bukan cuma foldernya) supaya
     * tetap ketemu walau user memindahkan file; `drive.file` membatasi ke file buatan app ini saja.
     */
    suspend fun findBackup(token: String, name: String): DriveFile? = findByName(token, name)

    /** Id folder [name] di My Drive; dibuat bila belum ada (atau sudah dibuang ke tempat sampah). */
    suspend fun ensureFolder(token: String, name: String): String {
        findByName(token, name, FOLDER_MIME)?.let { return it.id }
        return withContext(Dispatchers.IO) {
            val metadata = buildJsonObject {
                put("name", name)
                put("mimeType", FOLDER_MIME)
            }.toString()
            val url = apiBase.toHttpUrl().newBuilder().addQueryParameter("fields", fields).build()
            val req = Request.Builder().url(url).header("Authorization", "Bearer $token")
                .post(metadata.toRequestBody(JSON_MEDIA)).build()
            client.newCall(req).execute().use { resp ->
                check(resp.isSuccessful) { "Drive buat folder gagal: ${resp.code}" }
                json.decodeFromString(DriveFile.serializer(), resp.body?.string().orEmpty()).id
            }
        }
    }

    /**
     * Buat backup baru (multipart) di folder [parentId], atau timpa [existingId] (media).
     * Mengembalikan metadata file.
     */
    suspend fun upload(
        token: String,
        name: String,
        bytes: ByteArray,
        existingId: String?,
        parentId: String?,
    ): DriveFile =
        withContext(Dispatchers.IO) {
            val req = if (existingId == null) {
                val metadata = buildJsonObject {
                    put("name", name)
                    // Tanpa ini Drive menebak tipe dari isi biner (sempat terbaca "image/tiff").
                    put("mimeType", "application/octet-stream")
                    if (parentId != null) put("parents", JsonArray(listOf(JsonPrimitive(parentId))))
                }.toString()
                val multipart = MultipartBody.Builder().setType("multipart/related".toMediaType())
                    .addPart(metadata.toRequestBody(JSON_MEDIA))
                    .addPart(bytes.toRequestBody("application/octet-stream".toMediaType()))
                    .build()
                val url = uploadBase.toHttpUrl().newBuilder()
                    .addQueryParameter("uploadType", "multipart")
                    .addQueryParameter("fields", fields)
                    .build()
                Request.Builder().url(url).header("Authorization", "Bearer $token").post(multipart).build()
            } else {
                val url = "$uploadBase/$existingId".toHttpUrl().newBuilder()
                    .addQueryParameter("uploadType", "media")
                    .addQueryParameter("fields", fields)
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

    private companion object {
        const val FOLDER_MIME = "application/vnd.google-apps.folder"
        val JSON_MEDIA = "application/json; charset=UTF-8".toMediaType()
    }
}
