package com.romreviewertools.noteitup.data.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import platform.Foundation.NSUserDefaults

actual class PreferencesStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val cache = MutableStateFlow<Map<String, String>>(emptyMap())

    init {
        // Initialize cache with stored values
        val keys = listOf(
            PreferencesKeys.THEME_MODE,
            PreferencesKeys.ACCENT_COLOR,
            PreferencesKeys.FONT_SIZE
        )
        val initialCache = keys.associateWith { key ->
            userDefaults.stringForKey(key) ?: ""
        }
        cache.value = initialCache
    }

    actual fun getString(key: String, defaultValue: String): Flow<String> = cache
        .map { it[key] ?: userDefaults.stringForKey(key) ?: defaultValue }
        .distinctUntilChanged()

    actual suspend fun putString(key: String, value: String) {
        userDefaults.setObject(value, forKey = key)
        cache.value = cache.value + (key to value)
    }
}
