package com.romreviewertools.noteitup.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image
import java.io.File

@Composable
actual fun ImagePreview(
    filePath: String,
    contentDescription: String?,
    modifier: Modifier
) {
    val imageBitmap = remember(filePath) {
        loadImageBitmap(filePath)
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

private fun loadImageBitmap(filePath: String): ImageBitmap? {
    return try {
        val file = File(filePath)
        if (!file.exists()) return null

        val bytes = file.readBytes()
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
