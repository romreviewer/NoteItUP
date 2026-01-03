package com.romreviewertools.noteitup.presentation.screens.settings

import androidx.compose.runtime.Composable

/**
 * Platform-specific notification permission request.
 * Returns a launcher that can be invoked to request permission.
 */
@Composable
expect fun rememberNotificationPermissionLauncher(
    onResult: (Boolean) -> Unit
): () -> Unit
