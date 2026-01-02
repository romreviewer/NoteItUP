package com.romreviewertools.noteitup.data.media

sealed class ImagePickerResult {
    data class Success(val filePath: String) : ImagePickerResult()
    data class Error(val message: String) : ImagePickerResult()
    data object Cancelled : ImagePickerResult()
}

expect class ImagePicker {
    suspend fun pickImageFromGallery(): ImagePickerResult
    suspend fun takePhoto(): ImagePickerResult
    fun copyToAppStorage(sourcePath: String, fileName: String): Result<String>
    fun createThumbnail(imagePath: String, maxSize: Int): Result<String>
    fun deleteImage(filePath: String): Result<Unit>
    fun getImagesDirectory(): String
}
