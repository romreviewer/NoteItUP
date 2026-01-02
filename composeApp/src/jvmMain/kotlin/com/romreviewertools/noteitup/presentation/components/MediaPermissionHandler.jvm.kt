package com.romreviewertools.noteitup.presentation.components

import androidx.compose.runtime.Composable

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
    // JVM/Desktop doesn't need permission handling
    // For now, these are stubs - JVM would need JFileChooser integration
    content(
        launchGallery = { onImagePickCancelled() },
        launchCamera = { onImagePickCancelled() },
        launchLocationPermission = { onLocationPermissionResult(false) }
    )
}
