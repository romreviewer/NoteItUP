package com.romreviewertools.noteitup.presentation.components

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFilePickerLauncher(
    mimeType: String,
    onFilePicked: (uri: String) -> Unit
): FilePickerLauncher {
    // iOS file picking requires UIDocumentPickerViewController
    // For now, return a no-op launcher - can be enhanced with proper iOS interop
    return FilePickerLauncher(
        launch = {
            // TODO: Implement iOS file picker using UIDocumentPickerViewController
            println("iOS file picker not yet implemented")
        }
    )
}
