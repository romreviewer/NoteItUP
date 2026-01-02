package com.romreviewertools.noteitup.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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

        // Use sampling for thumbnails to reduce memory
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, options)

        // Calculate sample size for ~200px thumbnails
        val targetSize = 200
        val scale = maxOf(options.outWidth, options.outHeight) / targetSize.toFloat()
        val sampleSize = if (scale > 1) {
            var size = 1
            while (size * 2 <= scale) size *= 2
            size
        } else 1

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        BitmapFactory.decodeFile(filePath, decodeOptions)?.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
