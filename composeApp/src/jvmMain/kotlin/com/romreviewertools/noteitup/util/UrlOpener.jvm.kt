package com.romreviewertools.noteitup.util

import java.awt.Desktop
import java.net.URI

actual class UrlOpener {
    actual fun openUrl(url: String) {
        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI(url))
            }
        }
    }
}
