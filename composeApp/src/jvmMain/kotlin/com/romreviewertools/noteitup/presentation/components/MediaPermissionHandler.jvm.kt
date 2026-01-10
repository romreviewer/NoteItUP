package com.romreviewertools.noteitup.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.romreviewertools.noteitup.data.media.ImagePicker
import com.romreviewertools.noteitup.data.media.ImagePickerResult
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
actual fun MediaPermissionHandler(
    onImagePicked: (filePath: String) -> Unit,
    onImagePickCancelled: () -> Unit,
    onLocationPermissionResult: (granted: Boolean) -> Unit,
    content: @Composable (
        launchGallery: () -> Unit,
        launchCamera: () -> Unit,
        launchLocationPermission: () -> Unit
    ) -> Unit
) {
    val imagePicker: ImagePicker = koinInject()
    val scope = rememberCoroutineScope()

    val launchGallery: () -> Unit = {
        scope.launch {
            when (val result = imagePicker.pickImageFromGallery()) {
                is ImagePickerResult.Success -> onImagePicked(result.filePath)
                is ImagePickerResult.Cancelled -> onImagePickCancelled()
                is ImagePickerResult.Error -> {
                    println("Image picker error: ${result.message}")
                    onImagePickCancelled()
                }
            }
        }
    }

    val launchCamera: () -> Unit = {
        // Desktop doesn't have camera access - just cancel
        println("Camera not available on desktop platform")
        onImagePickCancelled()
    }

    val launchLocationPermission: () -> Unit = {
        // Desktop doesn't have GPS - always return false
        onLocationPermissionResult(false)
    }

    content(launchGallery, launchCamera, launchLocationPermission)
}
