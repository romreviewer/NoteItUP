package com.romreviewertools.noteitup.presentation.screens.settings

import com.romreviewertools.noteitup.domain.model.AccentColor
import com.romreviewertools.noteitup.domain.model.FontSize
import com.romreviewertools.noteitup.domain.model.ThemeMode
import kotlinx.datetime.LocalTime

sealed interface SettingsIntent {
    data class ChangeThemeMode(val mode: ThemeMode) : SettingsIntent
    data class ChangeAccentColor(val color: AccentColor) : SettingsIntent
    data class ChangeFontSize(val size: FontSize) : SettingsIntent
    data class ToggleReminder(val enabled: Boolean) : SettingsIntent
    data class ChangeReminderTime(val time: LocalTime) : SettingsIntent

    // Notification permission
    data object DismissPermissionDialog : SettingsIntent
    data class OnNotificationPermissionResult(val granted: Boolean) : SettingsIntent
}
