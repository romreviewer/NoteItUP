package com.romreviewertools.noteitup

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.romreviewertools.noteitup.di.commonModules
import com.romreviewertools.noteitup.di.jvmModule
import com.romreviewertools.noteitup.window.DesktopWindow
import com.romreviewertools.noteitup.window.WindowContent
import com.romreviewertools.noteitup.window.WindowManager
import org.koin.core.context.startKoin

fun main() {
    val koinApp = startKoin {
        modules(commonModules + jvmModule)
    }

    application {
        val windowManager = koinApp.koin.get<WindowManager>()
        val windows by windowManager.windows.collectAsState()

        // Main window
        Window(
            onCloseRequest = {
                windowManager.closeAllWindows()
                exitApplication()
            },
            title = "NoteItUP - Diary App",
            state = rememberWindowState(
                size = DpSize(1200.dp, 800.dp)
            )
        ) {
            MenuBar {
                Menu("File") {
                    Item("New Entry in Window", onClick = {
                        windowManager.openWindow(DesktopWindow.Editor(isNewEntry = true))
                    })
                    Separator()
                    Item("Exit", onClick = ::exitApplication)
                }
                Menu("Window") {
                    Item("Calendar", onClick = {
                        windowManager.openWindow(DesktopWindow.Calendar)
                    })
                    Item("Statistics", onClick = {
                        windowManager.openWindow(DesktopWindow.Statistics)
                    })
                    Item("Search", onClick = {
                        windowManager.openWindow(DesktopWindow.Search)
                    })
                    Separator()
                    Item("Tags", onClick = {
                        windowManager.openWindow(DesktopWindow.Tags)
                    })
                    Item("Folders", onClick = {
                        windowManager.openWindow(DesktopWindow.Folders)
                    })
                    Separator()
                    Item("AI Settings", onClick = {
                        windowManager.openWindow(DesktopWindow.AISettings)
                    })
                    Item("Cloud Sync", onClick = {
                        windowManager.openWindow(DesktopWindow.CloudSync)
                    })
                }
                Menu("Help") {
                    Item("About NoteItUP", onClick = {
                        // Could open about dialog
                    })
                }
            }

            App()
        }

        // Additional windows
        windows.forEach { window ->
            val windowTitle = when (window) {
                is DesktopWindow.Editor -> if (window.isNewEntry) "New Entry" else "Edit Entry"
                is DesktopWindow.Calendar -> "Calendar"
                is DesktopWindow.Statistics -> "Statistics"
                is DesktopWindow.Search -> "Search"
                is DesktopWindow.Tags -> "Tags"
                is DesktopWindow.Folders -> "Folders"
                is DesktopWindow.AISettings -> "AI Settings"
                is DesktopWindow.CloudSync -> "Cloud Sync"
            }

            val windowSize = when (window) {
                is DesktopWindow.Editor -> DpSize(900.dp, 700.dp)
                is DesktopWindow.Calendar -> DpSize(800.dp, 600.dp)
                is DesktopWindow.Statistics -> DpSize(900.dp, 700.dp)
                is DesktopWindow.Search -> DpSize(700.dp, 600.dp)
                is DesktopWindow.Tags -> DpSize(600.dp, 500.dp)
                is DesktopWindow.Folders -> DpSize(600.dp, 500.dp)
                is DesktopWindow.AISettings -> DpSize(700.dp, 600.dp)
                is DesktopWindow.CloudSync -> DpSize(800.dp, 600.dp)
            }

            Window(
                onCloseRequest = { windowManager.closeWindow(window) },
                title = "$windowTitle - NoteItUP",
                state = rememberWindowState(size = windowSize)
            ) {
                WindowContent(
                    window = window,
                    windowManager = windowManager,
                    onClose = { windowManager.closeWindow(window) }
                )
            }
        }
    }
}
