package com.romreviewertools.noteitup.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image as SkiaImage
import platform.Foundation.NSData
import platform.Foundation.NSFileManager

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
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )
    } else {
        // Fallback for missing/corrupted images
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun loadImageBitmap(filePath: String): ImageBitmap? {
    return try {
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(filePath)) {
            return null
        }

        // Read file as NSData
        val data = NSData.dataWithContentsOfFile(filePath) ?: return null

        // Convert NSData to ByteArray
        val bytes = ByteArray(data.length.toInt())
        bytes.usePinned { pinned ->
            data.getBytes(pinned.addressOf(0))
        }

        // Decode with Skia and convert to Compose ImageBitmap
        SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
