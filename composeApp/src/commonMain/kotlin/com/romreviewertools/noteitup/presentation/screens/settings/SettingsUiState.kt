package com.romreviewertools.noteitup.presentation.screens.settings

import com.romreviewertools.noteitup.domain.model.AccentColor
import com.romreviewertools.noteitup.domain.model.FontSize
import com.romreviewertools.noteitup.domain.model.ReminderSettings
import com.romreviewertools.noteitup.domain.model.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.DEFAULT,
    val fontSize: FontSize = FontSize.MEDIUM,
    val reminderSettings: ReminderSettings = ReminderSettings(),
    val isLoading: Boolean = true
)
