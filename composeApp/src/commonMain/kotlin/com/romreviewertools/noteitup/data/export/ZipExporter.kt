package com.romreviewertools.noteitup.data.export

/**
 * Platform-specific ZIP file operations for exporting/importing diary data with images.
 */
expect class ZipExporter {
    /**
     * Creates a ZIP archive containing JSON data and image files.
     *
     * @param jsonContent The JSON string containing exported data
     * @param imageFiles List of pairs (fileName, filePath) for images to include
     * @param outputPath The path where the ZIP file should be created
     * @return Result containing the output path on success
     */
    suspend fun createZip(
        jsonContent: String,
        imageFiles: List<Pair<String, String>>,
        outputPath: String
    ): Result<String>

    /**
     * Extracts a ZIP archive containing diary data and images.
     *
     * @param zipPath The path to the ZIP file to extract
     * @param outputDir The directory where images should be extracted
     * @return Result containing a Pair of (jsonContent, Map of imageFileName -> extractedPath)
     */
    suspend fun extractZip(
        zipPath: String,
        outputDir: String
    ): Result<Pair<String, Map<String, String>>>
}
