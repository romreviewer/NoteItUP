package com.romreviewertools.noteitup.window

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages multiple windows in the Desktop application
 */
class WindowManager {
    private val _windows = MutableStateFlow<List<DesktopWindow>>(emptyList())
    val windows: StateFlow<List<DesktopWindow>> = _windows.asStateFlow()

    fun openWindow(window: DesktopWindow) {
        val currentWindows = _windows.value.toMutableList()

        // Check if window type already exists (for single-instance windows)
        if (window.singleInstance) {
            val existingIndex = currentWindows.indexOfFirst {
                it::class == window::class
            }
            if (existingIndex != -1) {
                // Window already exists, don't open duplicate
                return
            }
        }

        currentWindows.add(window)
        _windows.value = currentWindows
    }

    fun closeWindow(window: DesktopWindow) {
        _windows.value = _windows.value.filter { it.id != window.id }
    }

    fun closeAllWindows() {
        _windows.value = emptyList()
    }
}

/**
 * Base class for desktop windows
 */
sealed class DesktopWindow(
    val id: String = java.util.UUID.randomUUID().toString(),
    val singleInstance: Boolean = false
) {
    data class Editor(
        val entryId: String? = null,
        val isNewEntry: Boolean = true
    ) : DesktopWindow(singleInstance = false)

    data object Calendar : DesktopWindow(singleInstance = true)

    data object Statistics : DesktopWindow(singleInstance = true)

    data object Search : DesktopWindow(singleInstance = true)

    data object Tags : DesktopWindow(singleInstance = true)

    data object Folders : DesktopWindow(singleInstance = true)

    data object AISettings : DesktopWindow(singleInstance = true)

    data object CloudSync : DesktopWindow(singleInstance = true)
}
