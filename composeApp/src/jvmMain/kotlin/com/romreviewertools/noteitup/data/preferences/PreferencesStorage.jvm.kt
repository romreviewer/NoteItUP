package com.romreviewertools.noteitup.data.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.Properties

actual class PreferencesStorage {
    private val prefsFile: File
    private val properties = Properties()
    private val cache = MutableStateFlow<Map<String, String>>(emptyMap())

    init {
        val userHome = System.getProperty("user.home")
        val appDir = File(userHome, ".noteitup")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        prefsFile = File(appDir, "preferences.properties")

        if (prefsFile.exists()) {
            prefsFile.inputStream().use { stream ->
                properties.load(stream)
            }
        }

        // Initialize cache
        val initialCache = mutableMapOf<String, String>()
        properties.forEach { key, value ->
            initialCache[key.toString()] = value.toString()
        }
        cache.value = initialCache
    }

    actual fun getString(key: String, defaultValue: String): Flow<String> = cache
        .map { it[key] ?: properties.getProperty(key, defaultValue) }
        .distinctUntilChanged()

    actual suspend fun putString(key: String, value: String) {
        properties.setProperty(key, value)
        prefsFile.outputStream().use { stream ->
            properties.store(stream, "NoteItUP Preferences")
        }
        cache.value = cache.value + (key to value)
    }
}
