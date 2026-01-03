package com.romreviewertools.noteitup.presentation.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationPermissionLauncher(
    onResult: (Boolean) -> Unit
): () -> Unit {
    // For Android 13+ (API 33+), we need to request POST_NOTIFICATIONS permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onResult(isGranted)
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    } else {
        // Pre-Android 13, notifications are allowed by default
        { onResult(true) }
    }
}
