package com.romreviewertools.noteitup.data.notification

import com.romreviewertools.noteitup.domain.model.ReminderSettings

actual class NotificationManager {
    // Desktop notifications are not implemented
    // Could use system tray notifications in the future

    actual fun scheduleReminder(settings: ReminderSettings) {
        // No-op on desktop
        println("Reminder scheduling not available on desktop")
    }

    actual fun cancelReminder() {
        // No-op on desktop
    }

    actual fun hasPermission(): Boolean {
        return false
    }

    actual suspend fun requestPermission(): Boolean {
        return false
    }
}
