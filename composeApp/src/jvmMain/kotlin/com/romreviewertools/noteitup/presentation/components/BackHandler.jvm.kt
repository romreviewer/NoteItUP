package com.romreviewertools.noteitup.presentation.components

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop: No system back button.
}
