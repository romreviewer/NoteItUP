package com.romreviewertools.noteitup.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
actual fun rememberFilePickerLauncher(
    mimeType: String,
    onFilePicked: (uri: String) -> Unit
): FilePickerLauncher {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onFilePicked(it.toString()) }
    }

    return FilePickerLauncher(
        launch = { launcher.launch(mimeType) }
    )
}
