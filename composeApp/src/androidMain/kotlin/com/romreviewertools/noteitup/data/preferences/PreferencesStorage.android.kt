package com.romreviewertools.noteitup.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

actual class PreferencesStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "noteitup_preferences",
        Context.MODE_PRIVATE
    )

    actual fun getString(key: String, defaultValue: String): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                trySend(prefs.getString(key, defaultValue) ?: defaultValue)
            }
        }

        // Emit initial value
        trySend(prefs.getString(key, defaultValue) ?: defaultValue)

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    actual suspend fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}
