package com.romreviewertools.noteitup

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.romreviewertools.noteitup.di.commonModules
import com.romreviewertools.noteitup.di.jvmModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(commonModules + jvmModule)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "NoteItUP",
        ) {
            App()
        }
    }
}
