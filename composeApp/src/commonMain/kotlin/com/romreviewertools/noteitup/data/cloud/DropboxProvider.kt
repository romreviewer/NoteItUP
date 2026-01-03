package com.romreviewertools.noteitup.data.cloud

import com.romreviewertools.noteitup.domain.repository.CloudSyncRepository
import io.ktor.client.*
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
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

/**
 * Dropbox cloud provider implementation.
 * Uses OAuth2 for authentication and Dropbox API v2 for file operations.
 */
class DropboxProvider(
    private val httpClient: HttpClient,
    private val cloudSyncRepository: CloudSyncRepository,
    private val oAuthHandler: OAuthHandler,
    private val appKey: String,
    private val appSecret: String
) : CloudProvider {

    override val type: CloudProviderType = CloudProviderType.DROPBOX

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // PKCE code verifier - stored temporarily during OAuth flow
    private var codeVerifier: String? = null

    companion object {
        private const val AUTH_URL = "https://www.dropbox.com/oauth2/authorize"
        private const val TOKEN_URL = "https://api.dropboxapi.com/oauth2/token"
        private const val API_BASE = "https://api.dropboxapi.com/2"
        private const val CONTENT_BASE = "https://content.dropboxapi.com/2"
        private const val BACKUP_FOLDER = "/NoteItUP_Backups"
    }

    // Generate PKCE code verifier (43-128 characters)
    @OptIn(ExperimentalEncodingApi::class)
    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        Random.nextBytes(bytes)
        return Base64.UrlSafe.encode(bytes).trimEnd('=')
    }

    // Generate code challenge from verifier using S256 method
    @OptIn(ExperimentalEncodingApi::class)
    private fun generateCodeChallenge(verifier: String): String {
        // For simplicity, use plain method (S256 requires SHA256 which needs platform-specific code)
        // Dropbox supports both plain and S256
        return verifier
    }

    override fun isAuthenticated(): Flow<Boolean> {
        return cloudSyncRepository.getAccessToken(type).map { it != null }
    }

    override suspend fun getAuthUrl(): String {
        val redirectUri = oAuthHandler.getRedirectUri(type).encodeURLParameter()
        // Generate and store PKCE verifier
        codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier!!)

        return "$AUTH_URL?" +
                "client_id=$appKey&" +
                "redirect_uri=$redirectUri&" +
                "response_type=code&" +
                "token_access_type=offline&" +
                "code_challenge=$codeChallenge&" +
                "code_challenge_method=plain"
    }

    override suspend fun handleAuthCallback(code: String): CloudResult<Unit> {
        return try {
            val redirectUri = oAuthHandler.getRedirectUri(type)
            val verifier = codeVerifier ?: return CloudResult.Error("PKCE verifier not found. Please try again.")

            val response: HttpResponse = httpClient.submitForm(
                url = TOKEN_URL,
                formParameters = parameters {
                    append("code", code)
                    append("client_id", appKey)
                    append("client_secret", appSecret)
                    append("redirect_uri", redirectUri)
                    append("grant_type", "authorization_code")
                    append("code_verifier", verifier)
                }
            )

            // Clear verifier after use
            codeVerifier = null

            if (response.status.isSuccess()) {
                val tokenResponse: DropboxTokenResponse = json.decodeFromString(response.bodyAsText())
                cloudSyncRepository.saveTokens(
                    provider = type,
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                    expiresIn = tokenResponse.expiresIn ?: 14400 // Default 4 hours
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
        // Revoke token
        try {
            val tokenInfo = cloudSyncRepository.getTokenInfo(type)
            tokenInfo?.let {
                httpClient.post("$API_BASE/auth/token/revoke") {
                    header("Authorization", "Bearer ${it.accessToken}")
                }
            }
        } catch (e: Exception) {
            // Ignore revoke errors
        }
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
                    append("client_id", appKey)
                    append("client_secret", appSecret)
                    append("refresh_token", refreshToken)
                    append("grant_type", "refresh_token")
                }
            )

            if (response.status.isSuccess()) {
                val tokenResponse: DropboxTokenResponse = json.decodeFromString(response.bodyAsText())
                cloudSyncRepository.saveTokens(
                    provider = type,
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken ?: refreshToken,
                    expiresIn = tokenResponse.expiresIn ?: 14400
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
                // First ensure folder exists
                ensureBackupFolderExists(accessToken)

                val response: HttpResponse = httpClient.post("$API_BASE/files/list_folder") {
                    header("Authorization", "Bearer $accessToken")
                    contentType(ContentType.Application.Json)
                    setBody("""{"path": "$BACKUP_FOLDER", "include_deleted": false}""")
                }

                if (response.status.isSuccess()) {
                    val listResponse: DropboxListResponse = json.decodeFromString(response.bodyAsText())
                    val files = listResponse.entries
                        .filter { it.tag == "file" && it.name.endsWith(".noteitup") }
                        .map { entry ->
                            CloudFile(
                                id = entry.id ?: entry.pathLower ?: "",
                                name = entry.name,
                                modifiedAt = parseDropboxDateTime(entry.serverModified),
                                size = entry.size ?: 0L
                            )
                        }
                        .sortedByDescending { it.modifiedAt }
                    CloudResult.Success(files)
                } else if (response.status.value == 409) {
                    // Path not found - return empty list
                    CloudResult.Success(emptyList())
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
                // Ensure folder exists
                ensureBackupFolderExists(accessToken)

                val path = "$BACKUP_FOLDER/$fileName"
                val dropboxArg = json.encodeToString(
                    DropboxUploadArg.serializer(),
                    DropboxUploadArg(
                        path = path,
                        mode = "overwrite",
                        autorename = false,
                        mute = true
                    )
                )

                val response: HttpResponse = httpClient.post("$CONTENT_BASE/files/upload") {
                    header("Authorization", "Bearer $accessToken")
                    header("Dropbox-API-Arg", dropboxArg)
                    contentType(ContentType.Application.OctetStream)
                    setBody(data)
                }

                if (response.status.isSuccess()) {
                    val fileResponse: DropboxFileMetadata = json.decodeFromString(response.bodyAsText())
                    CloudResult.Success(
                        CloudFile(
                            id = fileResponse.id ?: fileResponse.pathLower ?: path,
                            name = fileResponse.name,
                            modifiedAt = parseDropboxDateTime(fileResponse.serverModified),
                            size = fileResponse.size ?: data.size.toLong()
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
                val dropboxArg = json.encodeToString(
                    DropboxDownloadArg.serializer(),
                    DropboxDownloadArg(path = fileId)
                )

                val response: HttpResponse = httpClient.post("$CONTENT_BASE/files/download") {
                    header("Authorization", "Bearer $accessToken")
                    header("Dropbox-API-Arg", dropboxArg)
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
                val response: HttpResponse = httpClient.post("$API_BASE/files/delete_v2") {
                    header("Authorization", "Bearer $accessToken")
                    contentType(ContentType.Application.Json)
                    setBody("""{"path": "$fileId"}""")
                }

                if (response.status.isSuccess()) {
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
                val response: HttpResponse = httpClient.post("$API_BASE/users/get_space_usage") {
                    header("Authorization", "Bearer $accessToken")
                }

                if (response.status.isSuccess()) {
                    val spaceUsage: DropboxSpaceUsage = json.decodeFromString(response.bodyAsText())
                    CloudResult.Success(
                        QuotaInfo(
                            used = spaceUsage.used,
                            total = spaceUsage.allocation?.allocated ?: Long.MAX_VALUE
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

    private suspend fun ensureBackupFolderExists(accessToken: String) {
        try {
            httpClient.post("$API_BASE/files/create_folder_v2") {
                header("Authorization", "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody("""{"path": "$BACKUP_FOLDER", "autorename": false}""")
            }
        } catch (e: Exception) {
            // Folder might already exist, ignore error
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

    private fun parseDropboxDateTime(dateTime: String?): Long {
        if (dateTime == null) return 0L
        // Dropbox uses ISO 8601 format: 2024-01-15T10:30:00Z
        return try {
            kotlinx.datetime.Instant.parse(dateTime).toEpochMilliseconds()
        } catch (e: Exception) {
            0L
        }
    }
}

// API Response Models

@Serializable
private data class DropboxTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    @SerialName("token_type") val tokenType: String = "bearer"
)

@Serializable
private data class DropboxListResponse(
    val entries: List<DropboxEntry> = emptyList(),
    val cursor: String? = null,
    @SerialName("has_more") val hasMore: Boolean = false
)

@Serializable
private data class DropboxEntry(
    @SerialName(".tag") val tag: String,
    val id: String? = null,
    val name: String,
    @SerialName("path_lower") val pathLower: String? = null,
    @SerialName("server_modified") val serverModified: String? = null,
    val size: Long? = null
)

@Serializable
private data class DropboxUploadArg(
    val path: String,
    val mode: String = "add",
    val autorename: Boolean = true,
    val mute: Boolean = false
)

@Serializable
private data class DropboxDownloadArg(
    val path: String
)

@Serializable
private data class DropboxFileMetadata(
    val id: String? = null,
    val name: String,
    @SerialName("path_lower") val pathLower: String? = null,
    @SerialName("server_modified") val serverModified: String? = null,
    val size: Long? = null
)

@Serializable
private data class DropboxSpaceUsage(
    val used: Long,
    val allocation: DropboxAllocation? = null
)

@Serializable
private data class DropboxAllocation(
    @SerialName(".tag") val tag: String? = null,
    val allocated: Long? = null
)
