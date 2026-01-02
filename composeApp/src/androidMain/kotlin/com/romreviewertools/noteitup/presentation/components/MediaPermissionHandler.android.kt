package com.romreviewertools.noteitup.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
actual fun MediaPermissionHandler(
    onImagePicked: (filePath: String) -> Unit,
    onImagePickCancelled: () -> Unit,
    onLocationPermissionResult: (granted: Boolean) -> Unit,
    content: @Composable (
        launchGallery: () -> Unit,
        launchCamera: () -> Unit,
        launchLocationPermission: () -> Unit
    ) -> Unit
) {
    val context = LocalContext.current

    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    // Helper function to copy URI content to app storage with compression
    fun copyUriToAppStorage(uri: Uri): String? {
        return try {
            val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
            val destFile = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")

            // Read and compress the image
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return null

            // Decode with sampling to reduce memory usage for large images
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate sample size for max 1920px dimension
            val maxDimension = 1920
            val scale = maxOf(options.outWidth, options.outHeight) / maxDimension.toFloat()
            val sampleSize = if (scale > 1) {
                var size = 1
                while (size * 2 <= scale) size *= 2
                size
            } else 1

            // Re-open stream and decode with sample size
            val decodeStream = context.contentResolver.openInputStream(uri)
                ?: return null
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            var bitmap = BitmapFactory.decodeStream(decodeStream, null, decodeOptions)
            decodeStream.close()

            if (bitmap == null) return null

            // Scale down if still too large
            if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = minOf(
                    maxDimension.toFloat() / bitmap.width,
                    maxDimension.toFloat() / bitmap.height
                )
                val newWidth = (bitmap.width * ratio).toInt()
                val newHeight = (bitmap.height * ratio).toInt()
                val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                if (scaled != bitmap) {
                    bitmap.recycle()
                    bitmap = scaled
                }
            }

            // Save compressed JPEG (quality 85)
            FileOutputStream(destFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
            }
            bitmap.recycle()

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Photo picker for gallery (Android 13+) or legacy picker
    val galleryLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                val filePath = copyUriToAppStorage(uri)
                if (filePath != null) {
                    onImagePicked(filePath)
                } else {
                    Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                    onImagePickCancelled()
                }
            } else {
                onImagePickCancelled()
            }
        }
    } else {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                val filePath = copyUriToAppStorage(uri)
                if (filePath != null) {
                    onImagePicked(filePath)
                } else {
                    Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                    onImagePickCancelled()
                }
            } else {
                onImagePickCancelled()
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val cameraFile = pendingCameraFile
        if (success && cameraFile != null && cameraFile.exists()) {
            onImagePicked(cameraFile.absolutePath)
        } else {
            cameraFile?.delete()
            onImagePickCancelled()
        }
        pendingCameraFile = null
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch camera
            val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
            val photoFile = File(imagesDir, "camera_${UUID.randomUUID()}.jpg")
            pendingCameraFile = photoFile
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            onImagePickCancelled()
        }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onLocationPermissionResult(fineGranted || coarseGranted)
    }

    // Storage permission launcher for older Android
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Launch gallery after permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                @Suppress("UNCHECKED_CAST")
                (galleryLauncher as androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>)
                    .launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                @Suppress("UNCHECKED_CAST")
                (galleryLauncher as androidx.activity.result.ActivityResultLauncher<String>)
                    .launch("image/*")
            }
        } else {
            Toast.makeText(context, "Storage permission denied", Toast.LENGTH_SHORT).show()
            onImagePickCancelled()
        }
    }

    val launchGallery: () -> Unit = {
        // Check if we need storage permission (Android 12 and below)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                @Suppress("UNCHECKED_CAST")
                (galleryLauncher as androidx.activity.result.ActivityResultLauncher<String>)
                    .launch("image/*")
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            // Android 13+ uses photo picker without permission
            @Suppress("UNCHECKED_CAST")
            (galleryLauncher as androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>)
                .launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    val launchCamera: () -> Unit = {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission) {
            val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
            val photoFile = File(imagesDir, "camera_${UUID.randomUUID()}.jpg")
            pendingCameraFile = photoFile
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val launchLocationPermission: () -> Unit = {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            onLocationPermissionResult(true)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    content(launchGallery, launchCamera, launchLocationPermission)
}
