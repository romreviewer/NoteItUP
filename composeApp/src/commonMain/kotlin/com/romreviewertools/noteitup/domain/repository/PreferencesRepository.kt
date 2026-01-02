package com.romreviewertools.noteitup.domain.repository

import com.romreviewertools.noteitup.domain.model.AccentColor
import com.romreviewertools.noteitup.domain.model.AppPreferences
import com.romreviewertools.noteitup.domain.model.FontSize
import com.romreviewertools.noteitup.domain.model.ReminderSettings
import com.romreviewertools.noteitup.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getPreferences(): Flow<AppPreferences>
    suspend fun updateThemeMode(mode: ThemeMode)
    suspend fun updateAccentColor(color: AccentColor)
    suspend fun updateFontSize(size: FontSize)

    fun getReminderSettings(): Flow<ReminderSettings>
    suspend fun updateReminderSettings(settings: ReminderSettings)
}
