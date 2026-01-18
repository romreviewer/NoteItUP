package com.romreviewertools.noteitup.data.review

import com.romreviewertools.noteitup.data.preferences.PreferencesKeys
import com.romreviewertools.noteitup.data.preferences.PreferencesStorage
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Repository for managing in-app review state.
 * Tracks when to show review prompts and user's response history.
 */
class ReviewStateRepository(
    private val preferencesStorage: PreferencesStorage
) {
    companion object {
        // Configuration
        const val MIN_ENTRIES_BEFORE_PROMPT = 5  // Show after 5 entries saved
        const val MAX_DISMISS_COUNT = 3          // Stop asking after 3 dismissals
        val MIN_DAYS_BETWEEN_PROMPTS = 7.days    // Wait 7 days between prompts
    }

    /**
     * Get the current number of entries saved by the user.
     */
    suspend fun getEntriesSavedCount(): Int {
        return preferencesStorage.getString(PreferencesKeys.REVIEW_ENTRIES_SAVED_COUNT, "0")
            .first()
            .toIntOrNull() ?: 0
    }

    /**
     * Increment the entries saved count.
     */
    suspend fun incrementEntriesSavedCount() {
        val current = getEntriesSavedCount()
        preferencesStorage.putString(PreferencesKeys.REVIEW_ENTRIES_SAVED_COUNT, (current + 1).toString())
    }

    /**
     * Check if the user has already rated the app.
     */
    suspend fun hasRated(): Boolean {
        return preferencesStorage.getString(PreferencesKeys.REVIEW_HAS_RATED, "false")
            .first()
            .toBooleanStrictOrNull() ?: false
    }

    /**
     * Mark that the user has rated the app.
     */
    suspend fun setHasRated(rated: Boolean) {
        preferencesStorage.putString(PreferencesKeys.REVIEW_HAS_RATED, rated.toString())
    }

    /**
     * Get the last time a review prompt was shown.
     */
    suspend fun getLastPromptTime(): Instant? {
        val millis = preferencesStorage.getString(PreferencesKeys.REVIEW_LAST_PROMPT_TIME, "0")
            .first()
            .toLongOrNull() ?: 0L
        return if (millis > 0) Instant.fromEpochMilliseconds(millis) else null
    }

    /**
     * Record that a review prompt was shown.
     */
    suspend fun recordPromptShown() {
        val now = Clock.System.now()
        preferencesStorage.putString(
            PreferencesKeys.REVIEW_LAST_PROMPT_TIME,
            now.toEpochMilliseconds().toString()
        )
    }

    /**
     * Get the number of times the user has dismissed the review prompt.
     */
    suspend fun getDismissCount(): Int {
        return preferencesStorage.getString(PreferencesKeys.REVIEW_DISMISS_COUNT, "0")
            .first()
            .toIntOrNull() ?: 0
    }

    /**
     * Increment the dismiss count.
     */
    suspend fun incrementDismissCount() {
        val current = getDismissCount()
        preferencesStorage.putString(PreferencesKeys.REVIEW_DISMISS_COUNT, (current + 1).toString())
    }

    /**
     * Check if the user has selected "never ask again".
     */
    suspend fun isNeverAskAgain(): Boolean {
        return preferencesStorage.getString(PreferencesKeys.REVIEW_NEVER_ASK_AGAIN, "false")
            .first()
            .toBooleanStrictOrNull() ?: false
    }

    /**
     * Set the "never ask again" preference.
     */
    suspend fun setNeverAskAgain(neverAsk: Boolean) {
        preferencesStorage.putString(PreferencesKeys.REVIEW_NEVER_ASK_AGAIN, neverAsk.toString())
    }

    /**
     * Check if we should show the review prompt.
     * Considers:
     * - Minimum entries saved
     * - User hasn't already rated
     * - Minimum time between prompts
     * - User hasn't dismissed too many times
     * - User hasn't selected "never ask again"
     */
    suspend fun shouldShowReviewPrompt(): Boolean {
        // Don't show if user opted out
        if (isNeverAskAgain()) return false

        // Don't show if user already rated
        if (hasRated()) return false

        // Don't show if user dismissed too many times
        if (getDismissCount() >= MAX_DISMISS_COUNT) return false

        // Don't show until minimum entries are saved
        if (getEntriesSavedCount() < MIN_ENTRIES_BEFORE_PROMPT) return false

        // Check minimum time between prompts
        val lastPrompt = getLastPromptTime()
        if (lastPrompt != null) {
            val timeSinceLastPrompt = Clock.System.now() - lastPrompt
            if (timeSinceLastPrompt < MIN_DAYS_BETWEEN_PROMPTS) return false
        }

        return true
    }
}