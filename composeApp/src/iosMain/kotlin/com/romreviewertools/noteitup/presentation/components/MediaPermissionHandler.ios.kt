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
    // iOS handles permissions differently through the native layer
    // For now, these are stubs - iOS implementation would need native integration
    content(
        launchGallery = { onImagePickCancelled() },
        launchCamera = { onImagePickCancelled() },
        launchLocationPermission = { onLocationPermissionResult(false) }
    )
}
