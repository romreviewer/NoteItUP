package com.romreviewertools.noteitup.data.import

/**
 * Platform-specific TAR archive operations for importing Joplin data.
 */
expect class TarExtractor {
    /**
     * Extracts a TAR/JEX archive to a temporary directory.
     *
     * @param tarPath Path to the TAR/JEX file
     * @param outputDir Directory where files should be extracted
     * @return Result containing map of fileName -> extractedPath
     */
    suspend fun extractTar(
        tarPath: String,
        outputDir: String
    ): Result<Map<String, String>>
}
