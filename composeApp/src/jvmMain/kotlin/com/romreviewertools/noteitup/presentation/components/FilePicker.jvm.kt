package com.romreviewertools.noteitup.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import javax.swing.SwingUtilities

@Composable
actual fun rememberFilePickerLauncher(
    mimeType: String,
    onFilePicked: (uri: String) -> Unit
): FilePickerLauncher {
    val scope = rememberCoroutineScope()

    return FilePickerLauncher(
        launch = {
            scope.launch(Dispatchers.IO) {
                SwingUtilities.invokeAndWait {
                    val dialog = FileDialog(null as Frame?, "Select file to import", FileDialog.LOAD)
                    dialog.isVisible = true
                    val file = dialog.file
                    val directory = dialog.directory
                    if (file != null && directory != null) {
                        onFilePicked("$directory$file")
                    }
                }
            }
        }
    )
}
