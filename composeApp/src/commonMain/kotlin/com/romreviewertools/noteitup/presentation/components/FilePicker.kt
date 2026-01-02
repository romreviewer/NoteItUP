package com.romreviewertools.noteitup.presentation.components

import androidx.compose.runtime.Composable

data class FilePickerLauncher(
    val launch: () -> Unit
)

@Composable
expect fun rememberFilePickerLauncher(
    mimeType: String,
    onFilePicked: (uri: String) -> Unit
): FilePickerLauncher
