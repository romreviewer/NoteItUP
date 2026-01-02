package com.romreviewertools.noteitup.data.repository

import com.romreviewertools.noteitup.data.preferences.PreferencesKeys
import com.romreviewertools.noteitup.data.preferences.PreferencesStorage
import com.romreviewertools.noteitup.domain.model.AccentColor
import com.romreviewertools.noteitup.domain.model.AppPreferences
import com.romreviewertools.noteitup.domain.model.FontSize
import com.romreviewertools.noteitup.domain.model.ReminderSettings
import com.romreviewertools.noteitup.domain.model.ThemeMode
import com.romreviewertools.noteitup.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

class PreferencesRepositoryImpl(
    private val storage: PreferencesStorage
) : PreferencesRepository {

    override fun getPreferences(): Flow<AppPreferences> {
        return combine(
            storage.getString(PreferencesKeys.THEME_MODE, ThemeMode.SYSTEM.name),
            storage.getString(PreferencesKeys.ACCENT_COLOR, AccentColor.DEFAULT.name),
            storage.getString(PreferencesKeys.FONT_SIZE, FontSize.MEDIUM.name)
        ) { themeModeStr, accentColorStr, fontSizeStr ->
            AppPreferences(
                themeMode = ThemeMode.entries.find { it.name == themeModeStr } ?: ThemeMode.SYSTEM,
                accentColor = AccentColor.entries.find { it.name == accentColorStr } ?: AccentColor.DEFAULT,
                fontSize = FontSize.entries.find { it.name == fontSizeStr } ?: FontSize.MEDIUM
            )
        }
    }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        storage.putString(PreferencesKeys.THEME_MODE, mode.name)
    }

    override suspend fun updateAccentColor(color: AccentColor) {
        storage.putString(PreferencesKeys.ACCENT_COLOR, color.name)
    }

    override suspend fun updateFontSize(size: FontSize) {
        storage.putString(PreferencesKeys.FONT_SIZE, size.name)
    }

    override fun getReminderSettings(): Flow<ReminderSettings> {
        return combine(
            storage.getString(PreferencesKeys.REMINDER_ENABLED, "false"),
            storage.getString(PreferencesKeys.REMINDER_HOUR, "20"),
            storage.getString(PreferencesKeys.REMINDER_MINUTE, "0"),
            storage.getString(PreferencesKeys.REMINDER_DAYS, DayOfWeek.entries.joinToString(",") { it.name })
        ) { enabled, hour, minute, daysStr ->
            val days = daysStr.split(",")
                .mapNotNull { dayName -> DayOfWeek.entries.find { it.name == dayName } }
                .toSet()

            ReminderSettings(
                enabled = enabled.toBoolean(),
                time = LocalTime(hour.toIntOrNull() ?: 20, minute.toIntOrNull() ?: 0),
                daysOfWeek = days.ifEmpty { DayOfWeek.entries.toSet() }
            )
        }
    }

    override suspend fun updateReminderSettings(settings: ReminderSettings) {
        storage.putString(PreferencesKeys.REMINDER_ENABLED, settings.enabled.toString())
        storage.putString(PreferencesKeys.REMINDER_HOUR, settings.time.hour.toString())
        storage.putString(PreferencesKeys.REMINDER_MINUTE, settings.time.minute.toString())
        storage.putString(PreferencesKeys.REMINDER_DAYS, settings.daysOfWeek.joinToString(",") { it.name })
    }
}
