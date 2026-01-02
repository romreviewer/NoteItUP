package com.romreviewertools.noteitup.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific composable to load and display an image from a file path.
 */
@Composable
expect fun ImagePreview(
    filePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
)
