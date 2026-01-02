package com.romreviewertools.noteitup.data.notification

import com.romreviewertools.noteitup.domain.model.ReminderSettings

expect class NotificationManager {
    fun scheduleReminder(settings: ReminderSettings)
    fun cancelReminder()
    fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
}
