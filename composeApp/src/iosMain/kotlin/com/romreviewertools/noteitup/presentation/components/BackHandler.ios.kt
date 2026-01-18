package com.romreviewertools.noteitup.presentation.components

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS usually handles back navigation via gesture/navigation controller.
    // Intercepting it in Compose requires deeper integration if using native navigation,
    // or it works out of the box if using Compose Navigation.
    // However, there's no native 'BackHandler' in Compose for iOS yet equivalent to Android's.
}
