package com.romreviewertools.noteitup.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romreviewertools.noteitup.data.analytics.AnalyticsEvent
import com.romreviewertools.noteitup.data.analytics.AnalyticsService
import com.romreviewertools.noteitup.data.notification.NotificationManager
import com.romreviewertools.noteitup.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val notificationManager: NotificationManager,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenViewSettings)
        loadPreferences()
        loadReminderSettings()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.getPreferences().collect { prefs ->
                _uiState.update {
                    it.copy(
                        themeMode = prefs.themeMode,
                        accentColor = prefs.accentColor,
                        fontSize = prefs.fontSize,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadReminderSettings() {
        viewModelScope.launch {
            preferencesRepository.getReminderSettings().collect { settings ->
                _uiState.update { it.copy(reminderSettings = settings) }
            }
        }
    }

    fun processIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ChangeThemeMode -> updateThemeMode(intent.mode)
            is SettingsIntent.ChangeAccentColor -> updateAccentColor(intent.color)
            is SettingsIntent.ChangeFontSize -> updateFontSize(intent.size)
            is SettingsIntent.ToggleReminder -> toggleReminder(intent.enabled)
            is SettingsIntent.ChangeReminderTime -> changeReminderTime(intent.time)
            is SettingsIntent.DismissPermissionDialog -> dismissPermissionDialog()
            is SettingsIntent.OnNotificationPermissionResult -> handlePermissionResult(intent.granted)
        }
    }

    fun needsNotificationPermission(): Boolean {
        return !notificationManager.hasPermission()
    }

    private fun updateThemeMode(mode: com.romreviewertools.noteitup.domain.model.ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(mode)
        }
    }

    private fun updateAccentColor(color: com.romreviewertools.noteitup.domain.model.AccentColor) {
        viewModelScope.launch {
            preferencesRepository.updateAccentColor(color)
        }
    }

    private fun updateFontSize(size: com.romreviewertools.noteitup.domain.model.FontSize) {
        viewModelScope.launch {
            preferencesRepository.updateFontSize(size)
        }
    }

    private fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !notificationManager.hasPermission()) {
                // Need to request permission first
                _uiState.update { it.copy(showNotificationPermissionDialog = true) }
                return@launch
            }

            val currentSettings = _uiState.value.reminderSettings
            val newSettings = currentSettings.copy(enabled = enabled)
            preferencesRepository.updateReminderSettings(newSettings)

            if (enabled) {
                notificationManager.scheduleReminder(newSettings)
            } else {
                notificationManager.cancelReminder()
            }
        }
    }

    private fun dismissPermissionDialog() {
        _uiState.update { it.copy(showNotificationPermissionDialog = false) }
    }

    private fun handlePermissionResult(granted: Boolean) {
        _uiState.update { it.copy(
            showNotificationPermissionDialog = false,
            notificationPermissionDenied = !granted
        ) }

        if (granted) {
            // Permission granted, now enable reminders
            viewModelScope.launch {
                val currentSettings = _uiState.value.reminderSettings
                val newSettings = currentSettings.copy(enabled = true)
                preferencesRepository.updateReminderSettings(newSettings)
                notificationManager.scheduleReminder(newSettings)
            }
        }
    }

    private fun changeReminderTime(time: LocalTime) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.reminderSettings
            val newSettings = currentSettings.copy(time = time)
            preferencesRepository.updateReminderSettings(newSettings)

            if (newSettings.enabled) {
                notificationManager.scheduleReminder(newSettings)
            }
        }
    }
}
