package com.romreviewertools.noteitup.data.encryption

import com.romreviewertools.noteitup.domain.model.ExportFormat
import com.romreviewertools.noteitup.domain.model.ExportOptions
import com.romreviewertools.noteitup.domain.usecase.ExportEntriesUseCase
import com.romreviewertools.noteitup.domain.usecase.ExportStats
import kotlin.time.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Buffer
import okio.ByteString.Companion.toByteString
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Metadata stored in the backup bundle (unencrypted).
 * Contains version info and the salt needed for decryption.
 */
@Serializable
data class BundleMetadata(
    val version: Int = BUNDLE_VERSION,
    val createdAt: Long,
    val appVersion: String = "1.0.0",
    val entryCount: Int,
    val folderCount: Int,
    val tagCount: Int,
    val salt: String, // Base64 encoded
    val encryptionAlgorithm: String = "AES-256-GCM"
) {
    companion object {
        const val BUNDLE_VERSION = 1
    }
}

/**
 * Service for creating and extracting encrypted backup bundles.
 *
 * Bundle format:
 * The bundle is a simple format with:
 * - First 4 bytes: metadata length (big-endian int)
 * - Next N bytes: metadata JSON (unencrypted)
 * - Remaining bytes: encrypted data
 */
class EncryptedBundleService(
    private val encryptionService: EncryptionService,
    private val exportEntriesUseCase: ExportEntriesUseCase
) {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    /**
     * Creates an encrypted backup bundle from all diary data.
     *
     * @param password User's encryption password
     * @return Encrypted bundle as ByteArray
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun createBundle(password: String): ByteArray {
        // 1. Export all data as JSON
        val exportOptions = ExportOptions(
            format = ExportFormat.JSON,
            includeEntries = true,
            includeFolders = true,
            includeTags = true
        )
        val (jsonData, stats) = exportEntriesUseCase.exportWithStats(exportOptions)

        // 2. Compress the JSON data
        val compressedData = compress(jsonData.encodeToByteArray())

        // 3. Generate salt and derive key
        val salt = encryptionService.generateSalt()
        val key = encryptionService.deriveKey(password, salt)

        // 4. Encrypt the compressed data
        val encryptedData = encryptionService.encrypt(compressedData, key)

        // 5. Create metadata
        val metadata = BundleMetadata(
            createdAt = Clock.System.now().toEpochMilliseconds(),
            entryCount = stats.entryCount,
            folderCount = stats.folderCount,
            tagCount = stats.tagCount,
            salt = Base64.encode(salt)
        )

        // 6. Package into final bundle
        return createFinalBundle(metadata, encryptedData)
    }

    /**
     * Extracts and decrypts a backup bundle.
     *
     * @param bundleData The encrypted bundle
     * @param password User's decryption password
     * @return Decrypted JSON data as String
     * @throws Exception if decryption fails
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun extractBundle(bundleData: ByteArray, password: String): String {
        // 1. Unpack the bundle
        val (metadata, encryptedData) = unpackBundle(bundleData)

        // 2. Derive key from password and stored salt
        val salt = Base64.decode(metadata.salt)
        val key = encryptionService.deriveKey(password, salt)

        // 3. Decrypt the data
        val compressedData = try {
            encryptionService.decrypt(encryptedData, key)
        } catch (e: Exception) {
            throw IllegalArgumentException("Decryption failed - wrong password or corrupted data", e)
        }

        // 4. Decompress to JSON
        return decompress(compressedData).decodeToString()
    }

    /**
     * Gets metadata from a bundle without decrypting it.
     * Useful for displaying backup info before restore.
     */
    fun getMetadata(bundleData: ByteArray): BundleMetadata {
        return unpackBundle(bundleData).first
    }

    private fun createFinalBundle(metadata: BundleMetadata, encryptedData: ByteArray): ByteArray {
        val metadataJson = json.encodeToString(metadata)
        val metadataBytes = metadataJson.encodeToByteArray()

        val buffer = Buffer()

        // Write metadata length (4 bytes, big-endian)
        buffer.writeInt(metadataBytes.size)

        // Write metadata
        buffer.write(metadataBytes)

        // Write encrypted data
        buffer.write(encryptedData)

        return buffer.readByteArray()
    }

    private fun unpackBundle(bundleData: ByteArray): Pair<BundleMetadata, ByteArray> {
        require(bundleData.size >= 4) { "Bundle too small" }

        val buffer = Buffer()
        buffer.write(bundleData)

        // Read metadata length
        val metadataLength = buffer.readInt()
        require(metadataLength > 0 && metadataLength < bundleData.size) {
            "Invalid metadata length: $metadataLength"
        }

        // Read metadata
        val metadataBytes = buffer.readByteArray(metadataLength.toLong())
        val metadataJson = metadataBytes.decodeToString()
        val metadata = json.decodeFromString<BundleMetadata>(metadataJson)

        // Read encrypted data
        val encryptedData = buffer.readByteArray()

        return metadata to encryptedData
    }

    /**
     * Simple compression using run-length encoding concepts.
     * For production, consider using a proper compression library.
     */
    private fun compress(data: ByteArray): ByteArray {
        // Simple implementation - just return as-is for now
        // In production, use proper ZIP compression
        val buffer = Buffer()
        buffer.write(data)
        return buffer.readByteArray()
    }

    private fun decompress(data: ByteArray): ByteArray {
        // Simple implementation - just return as-is for now
        val buffer = Buffer()
        buffer.write(data)
        return buffer.readByteArray()
    }
}
