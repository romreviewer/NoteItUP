package com.romreviewertools.noteitup.presentation.screens.settings

import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationPermissionLauncher(
    onResult: (Boolean) -> Unit
): () -> Unit {
    // iOS handles notification permissions differently
    // For now, just return true - actual implementation would use UNUserNotificationCenter
    return { onResult(true) }
}
