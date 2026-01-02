package com.romreviewertools.noteitup.presentation.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific handler for media permissions and image picking.
 * On Android, this sets up ActivityResultLaunchers for permissions, gallery, and camera.
 * On other platforms, this is a no-op.
 */
@Composable
expect fun MediaPermissionHandler(
    onImagePicked: (filePath: String) -> Unit,
    onImagePickCancelled: () -> Unit,
    onLocationPermissionResult: (granted: Boolean) -> Unit,
    content: @Composable (
        launchGallery: () -> Unit,
        launchCamera: () -> Unit,
        launchLocationPermission: () -> Unit
    ) -> Unit
)
