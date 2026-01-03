package com.romreviewertools.noteitup.presentation.screens.settings

import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationPermissionLauncher(
    onResult: (Boolean) -> Unit
): () -> Unit {
    // Desktop doesn't require notification permission
    return { onResult(true) }
}
