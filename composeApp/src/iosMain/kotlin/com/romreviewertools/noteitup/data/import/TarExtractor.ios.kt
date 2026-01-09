package com.romreviewertools.noteitup.data.import

/**
 * iOS implementation of TarExtractor.
 *
 * NOTE: This is currently a stub implementation.
 * iOS does not have built-in TAR extraction support in Foundation framework.
 *
 * Possible implementation options:
 * 1. Use Swift Compression framework (iOS 15+)
 * 2. Use CocoaPods library like "LightUntar"
 * 3. Custom TAR parser implementation
 *
 * Until implemented, Joplin import is not available on iOS.
 * Day One import still works as it uses ZipExporter.
 */
actual class TarExtractor {
    actual suspend fun extractTar(
        tarPath: String,
        outputDir: String
    ): Result<Map<String, String>> {
        return Result.failure(
            NotImplementedError(
                "TAR extraction is not yet implemented for iOS. " +
                "Joplin import is currently only available on Android and Desktop. " +
                "Day One import works on all platforms."
            )
        )
    }
}
