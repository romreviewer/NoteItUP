package com.romreviewertools.noteitup.data.notification

import com.romreviewertools.noteitup.domain.model.ReminderSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import kotlin.coroutines.resume

actual class NotificationManager {
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    actual fun scheduleReminder(settings: ReminderSettings) {
        if (!settings.enabled) {
            cancelReminder()
            return
        }

        val content = UNMutableNotificationContent().apply {
            setTitle("Time to Write")
            setBody("Take a moment to reflect on your day")
            setSound(platform.UserNotifications.UNNotificationSound.defaultSound)
        }

        val dateComponents = NSDateComponents().apply {
            hour = settings.time.hour.toLong()
            minute = settings.time.minute.toLong()
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents,
            repeats = true
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            REMINDER_ID,
            content,
            trigger
        )

        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {
                println("Failed to schedule notification: ${error.localizedDescription}")
            }
        }
    }

    actual fun cancelReminder() {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(REMINDER_ID))
    }

    actual fun hasPermission(): Boolean {
        var hasPermission = false
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            hasPermission = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
        }
        return hasPermission
    }

    actual suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge

        notificationCenter.requestAuthorizationWithOptions(options) { granted, error ->
            if (error != null) {
                continuation.resume(false)
            } else {
                continuation.resume(granted)
            }
        }
    }

    companion object {
        const val REMINDER_ID = "diary_daily_reminder"
    }
}
