package com.romreviewertools.noteitup.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ImagePreview(
    filePath: String,
    contentDescription: String?,
    modifier: Modifier
) {
    // iOS implementation would use UIImage
    // For now, show a placeholder
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
    )
}
