package com.romreviewertools.noteitup.data.import

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.posix.memcpy

/**
 * iOS implementation of TarExtractor using pure Kotlin TAR parsing.
 *
 * TAR file format:
 * - Each entry consists of a 512-byte header followed by file content
 * - File content is padded to 512-byte boundaries
 * - Archive ends with two 512-byte blocks of zeros
 *
 * Header structure (POSIX ustar format):
 * - Offset 0-99: File name (100 bytes, null-terminated)
 * - Offset 100-107: File mode (8 bytes, octal)
 * - Offset 108-115: Owner UID (8 bytes, octal)
 * - Offset 116-123: Group GID (8 bytes, octal)
 * - Offset 124-135: File size (12 bytes, octal)
 * - Offset 136-147: Modification time (12 bytes, octal)
 * - Offset 148-155: Checksum (8 bytes)
 * - Offset 156: Type flag (1 byte: '0' or '\0' = regular file, '5' = directory)
 * - Offset 157-256: Link name (100 bytes)
 * - Offset 257-262: Magic ("ustar\0" for POSIX)
 * - ... (additional fields)
 */
@OptIn(ExperimentalForeignApi::class)
actual class TarExtractor {

    companion object {
        private const val BLOCK_SIZE = 512
        private const val NAME_OFFSET = 0
        private const val NAME_LENGTH = 100
        private const val SIZE_OFFSET = 124
        private const val SIZE_LENGTH = 12
        private const val TYPE_OFFSET = 156
        private const val PREFIX_OFFSET = 345
        private const val PREFIX_LENGTH = 155
    }

    actual suspend fun extractTar(
        tarPath: String,
        outputDir: String
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val fileManager = NSFileManager.defaultManager
            val fileMap = mutableMapOf<String, String>()

            // Create output directory
            fileManager.createDirectoryAtPath(outputDir, true, null, null)

            // Read TAR file
            val tarData = NSData.dataWithContentsOfURL(NSURL.fileURLWithPath(tarPath))
                ?: throw IllegalStateException("Failed to read TAR file: $tarPath")

            val tarBytes = ByteArray(tarData.length.toInt())
            tarBytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), tarData.bytes, tarData.length)
            }

            var offset = 0
            val totalLength = tarBytes.size

            while (offset + BLOCK_SIZE <= totalLength) {
                // Read header block
                val header = tarBytes.copyOfRange(offset, offset + BLOCK_SIZE)

                // Check if this is an empty block (end of archive)
                if (isEmptyBlock(header)) {
                    break
                }

                // Parse header
                val fileName = parseFileName(header)
                val fileSize = parseFileSize(header)
                val typeFlag = header[TYPE_OFFSET]

                // Skip if empty filename
                if (fileName.isBlank()) {
                    offset += BLOCK_SIZE
                    continue
                }

                // Move past header
                offset += BLOCK_SIZE

                // Handle based on type
                val isRegularFile = typeFlag == '0'.code.toByte() ||
                                   typeFlag == 0.toByte() ||
                                   typeFlag == '\u0000'.code.toByte()
                val isDirectory = typeFlag == '5'.code.toByte()

                if (isRegularFile && fileSize > 0) {
                    // Extract regular file
                    val outputPath = "$outputDir/$fileName"
                    val parentDir = outputPath.substringBeforeLast("/")

                    // Create parent directories
                    if (parentDir != outputPath) {
                        fileManager.createDirectoryAtPath(parentDir, true, null, null)
                    }

                    // Read file content
                    if (offset + fileSize <= totalLength) {
                        val fileContent = tarBytes.copyOfRange(offset, offset + fileSize)

                        // Write file
                        fileContent.usePinned { pinned ->
                            val nsData = NSData.create(
                                bytes = pinned.addressOf(0),
                                length = fileContent.size.toULong()
                            )
                            nsData?.writeToURL(NSURL.fileURLWithPath(outputPath), true)
                        }

                        fileMap[fileName] = outputPath
                    }
                } else if (isDirectory) {
                    // Create directory
                    val dirPath = "$outputDir/$fileName"
                    fileManager.createDirectoryAtPath(dirPath, true, null, null)
                }

                // Move to next entry (file content padded to 512-byte boundary)
                val contentBlocks = (fileSize + BLOCK_SIZE - 1) / BLOCK_SIZE
                offset += contentBlocks * BLOCK_SIZE
            }

            fileMap
        }
    }

    /**
     * Parse file name from TAR header.
     * Handles both standard name field and POSIX prefix field.
     */
    private fun parseFileName(header: ByteArray): String {
        // Read name field (100 bytes at offset 0)
        val name = extractString(header, NAME_OFFSET, NAME_LENGTH)

        // Read prefix field (155 bytes at offset 345) for long paths
        val prefix = extractString(header, PREFIX_OFFSET, PREFIX_LENGTH)

        return if (prefix.isNotEmpty()) {
            "$prefix/$name"
        } else {
            name
        }
    }

    /**
     * Parse file size from TAR header (octal ASCII string).
     */
    private fun parseFileSize(header: ByteArray): Int {
        val sizeStr = extractString(header, SIZE_OFFSET, SIZE_LENGTH).trim()

        return if (sizeStr.isEmpty()) {
            0
        } else {
            try {
                // TAR uses octal representation
                sizeStr.toInt(8)
            } catch (e: NumberFormatException) {
                0
            }
        }
    }

    /**
     * Extract null-terminated string from byte array.
     */
    private fun extractString(data: ByteArray, offset: Int, maxLength: Int): String {
        val end = minOf(offset + maxLength, data.size)
        val bytes = mutableListOf<Byte>()

        for (i in offset until end) {
            val b = data[i]
            if (b == 0.toByte()) break
            bytes.add(b)
        }

        return bytes.toByteArray().decodeToString().trim()
    }

    /**
     * Check if a block is empty (all zeros), indicating end of archive.
     */
    private fun isEmptyBlock(block: ByteArray): Boolean {
        return block.all { it == 0.toByte() }
    }
}