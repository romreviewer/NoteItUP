package com.romreviewertools.noteitup.data.cloud

import com.romreviewertools.noteitup.domain.repository.CloudSyncRepository
import io.ktor.client.*
import kotlin.time.Clock
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Google Drive cloud provider implementation.
 * Uses OAuth2 for authentication and Google Drive API v3 for file operations.
 */
class GoogleDriveProvider(
    private val httpClient: HttpClient,
    private val cloudSyncRepository: CloudSyncRepository,
    private val oAuthHandler: OAuthHandler,
    private val clientId: String,
    private val clientSecret: String
) : CloudProvider {

    override val type: CloudProviderType = CloudProviderType.GOOGLE_DRIVE

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val TOKEN_URL = "https://oauth2.googleapis.com/token"
        private const val DRIVE_API_BASE = "https://www.googleapis.com/drive/v3"
        private const val DRIVE_UPLOAD_BASE = "https://www.googleapis.com/upload/drive/v3"
        private const val SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val APP_FOLDER = "appDataFolder"
        private const val BACKUP_MIME_TYPE = "application/octet-stream"
    }

    override fun isAuthenticated(): Flow<Boolean> {
        return cloudSyncRepository.getAccessToken(type).map { it != null }
    }

    override suspend fun getAuthUrl(): String {
        val redirectUri = oAuthHandler.getRedirectUri(type)
        return "$AUTH_URL?" +
                "client_id=$clientId&" +
                "redirect_uri=$redirectUri&" +
                "response_type=code&" +
                "scope=$SCOPE&" +
                "access_type=offline&" +
                "prompt=consent"
    }

    override suspend fun handleAuthCallback(code: String, redirectUri: String?): CloudResult<Unit> {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = TOKEN_URL,
                formParameters = parameters {
                    append("code", code)
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    // For native Android auth codes, omit redirect_uri entirely
                    if (redirectUri != "native") {
                        append("redirect_uri", redirectUri ?: oAuthHandler.getRedirectUri(type))
                    }
                    append("grant_type", "authorization_code")
                }
            )

            if (response.status.isSuccess()) {
                val tokenResponse: TokenResponse = json.decodeFromString(response.bodyAsText())
                cloudSyncRepository.saveTokens(
                    provider = type,
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                    expiresIn = tokenResponse.expiresIn
                )
                CloudResult.Success(Unit)
            } else {
                CloudResult.Error("Failed to exchange code: ${response.status}", response.status.value)
            }
        } catch (e: Exception) {
            CloudResult.Error("Auth callback failed: ${e.message}")
        }
    }

    override suspend fun disconnect() {
        cloudSyncRepository.clearTokens(type)
    }

    override suspend fun refreshTokenIfNeeded(): CloudResult<Unit> {
        val tokenInfo = cloudSyncRepository.getTokenInfo(type) ?: return CloudResult.NotAuthenticated

        if (!tokenInfo.isExpired) {
            return CloudResult.Success(Unit)
        }

        val refreshToken = tokenInfo.refreshToken ?: return CloudResult.NotAuthenticated

        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = TOKEN_URL,
                formParameters = parameters {
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("refresh_token", refreshToken)
                    append("grant_type", "refresh_token")
                }
            )

            if (response.status.isSuccess()) {
                val tokenResponse: TokenResponse = json.decodeFromString(response.bodyAsText())
                cloudSyncRepository.saveTokens(
                    provider = type,
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken ?: refreshToken,
                    expiresIn = tokenResponse.expiresIn
                )
                CloudResult.Success(Unit)
            } else {
                CloudResult.Error("Token refresh failed: ${response.status}", response.status.value)
            }
        } catch (e: Exception) {
            CloudResult.Error("Token refresh error: ${e.message}")
        }
    }

    override suspend fun listBackups(): CloudResult<List<CloudFile>> {
        return withAuthenticatedRequest { accessToken ->
            try {
                val response: HttpResponse = httpClient.get("$DRIVE_API_BASE/files") {
                    header("Authorization", "Bearer $accessToken")
                    parameter("spaces", APP_FOLDER)
                    parameter("fields", "files(id,name,modifiedTime,size)")
                    parameter("orderBy", "modifiedTime desc")
                    parameter("q", "name contains '.noteitup'")
                }

                if (response.status.isSuccess()) {
                    val filesResponse: DriveFilesResponse = json.decodeFromString(response.bodyAsText())
                    val files = filesResponse.files.map { file ->
                        CloudFile(
                            id = file.id,
                            name = file.name,
                            modifiedAt = parseGoogleDateTime(file.modifiedTime),
                            size = file.size?.toLongOrNull() ?: 0L
                        )
                    }
                    CloudResult.Success(files)
                } else {
                    CloudResult.Error("Failed to list files: ${response.status}", response.status.value)
                }
            } catch (e: Exception) {
                CloudResult.Error("List backups error: ${e.message}")
            }
        }
    }

    override suspend fun uploadBackup(fileName: String, data: ByteArray): CloudResult<CloudFile> {
        return withAuthenticatedRequest { accessToken ->
            try {
                // Create metadata
                val metadata = DriveFileMetadata(
                    name = fileName,
                    parents = listOf(APP_FOLDER)
                )
                val metadataJson = json.encodeToString(DriveFileMetadata.serializer(), metadata)

                // Multipart upload
                val response: HttpResponse = httpClient.post("$DRIVE_UPLOAD_BASE/files?uploadType=multipart") {
                    header("Authorization", "Bearer $accessToken")
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append("metadata", metadataJson, Headers.build {
                                    append(HttpHeaders.ContentType, "application/json")
                                })
                                append("file", data, Headers.build {
                                    append(HttpHeaders.ContentType, BACKUP_MIME_TYPE)
                                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                })
                            }
                        )
                    )
                }

                if (response.status.isSuccess()) {
                    val fileResponse: DriveFile = json.decodeFromString(response.bodyAsText())
                    CloudResult.Success(
                        CloudFile(
                            id = fileResponse.id,
                            name = fileResponse.name,
                            modifiedAt = Clock.System.now().toEpochMilliseconds(),
                            size = data.size.toLong()
                        )
                    )
                } else {
                    CloudResult.Error("Upload failed: ${response.status}", response.status.value)
                }
            } catch (e: Exception) {
                CloudResult.Error("Upload error: ${e.message}")
            }
        }
    }

    override suspend fun downloadBackup(fileId: String): CloudResult<ByteArray> {
        return withAuthenticatedRequest { accessToken ->
            try {
                val response: HttpResponse = httpClient.get("$DRIVE_API_BASE/files/$fileId") {
                    header("Authorization", "Bearer $accessToken")
                    parameter("alt", "media")
                }

                if (response.status.isSuccess()) {
                    CloudResult.Success(response.body<ByteArray>())
                } else {
                    CloudResult.Error("Download failed: ${response.status}", response.status.value)
                }
            } catch (e: Exception) {
                CloudResult.Error("Download error: ${e.message}")
            }
        }
    }

    override suspend fun deleteBackup(fileId: String): CloudResult<Unit> {
        return withAuthenticatedRequest { accessToken ->
            try {
                val response: HttpResponse = httpClient.delete("$DRIVE_API_BASE/files/$fileId") {
                    header("Authorization", "Bearer $accessToken")
                }

                if (response.status.isSuccess() || response.status == HttpStatusCode.NoContent) {
                    CloudResult.Success(Unit)
                } else {
                    CloudResult.Error("Delete failed: ${response.status}", response.status.value)
                }
            } catch (e: Exception) {
                CloudResult.Error("Delete error: ${e.message}")
            }
        }
    }

    override suspend fun getQuotaInfo(): CloudResult<QuotaInfo> {
        return withAuthenticatedRequest { accessToken ->
            try {
                val response: HttpResponse = httpClient.get("$DRIVE_API_BASE/about") {
                    header("Authorization", "Bearer $accessToken")
                    parameter("fields", "storageQuota")
                }

                if (response.status.isSuccess()) {
                    val aboutResponse: DriveAboutResponse = json.decodeFromString(response.bodyAsText())
                    val quota = aboutResponse.storageQuota
                    CloudResult.Success(
                        QuotaInfo(
                            used = quota.usage?.toLongOrNull() ?: 0L,
                            total = quota.limit?.toLongOrNull() ?: Long.MAX_VALUE
                        )
                    )
                } else {
                    CloudResult.Error("Failed to get quota: ${response.status}", response.status.value)
                }
            } catch (e: Exception) {
                CloudResult.Error("Quota error: ${e.message}")
            }
        }
    }

    private suspend fun <T> withAuthenticatedRequest(block: suspend (String) -> CloudResult<T>): CloudResult<T> {
        // Ensure token is valid
        val refreshResult = refreshTokenIfNeeded()
        if (refreshResult is CloudResult.Error || refreshResult is CloudResult.NotAuthenticated) {
            return CloudResult.NotAuthenticated
        }

        val tokenInfo = cloudSyncRepository.getTokenInfo(type)
            ?: return CloudResult.NotAuthenticated

        return block(tokenInfo.accessToken)
    }

    private fun parseGoogleDateTime(dateTime: String?): Long {
        if (dateTime == null) return 0L
        // Google uses ISO 8601 format: 2024-01-15T10:30:00.000Z
        return try {
            // Simple parsing - extract timestamp
            kotlinx.datetime.Instant.parse(dateTime).toEpochMilliseconds()
        } catch (e: Exception) {
            0L
        }
    }
}

// API Response Models

@Serializable
private data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String = "Bearer"
)

@Serializable
private data class DriveFilesResponse(
    val files: List<DriveFile> = emptyList()
)

@Serializable
private data class DriveFile(
    val id: String,
    val name: String,
    val modifiedTime: String? = null,
    val size: String? = null
)

@Serializable
private data class DriveFileMetadata(
    val name: String,
    val parents: List<String>? = null
)

@Serializable
private data class DriveAboutResponse(
    val storageQuota: StorageQuota
)

@Serializable
private data class StorageQuota(
    val usage: String? = null,
    val limit: String? = null
)
