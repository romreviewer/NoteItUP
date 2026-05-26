package com.romreviewertools.noteitup.data.cloud

import android.app.Activity
import android.util.Log
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import kotlinx.coroutines.CompletableDeferred

/**
 * Bridges the callback-based Dropbox Android SDK auth flow with Kotlin coroutines.
 *
 * Flow:
 *  1. MainActivity calls [init] with itself.
 *  2. [authorize] snapshots any prior credential, calls Auth.startOAuth2PKCE, and
 *     suspends on a CompletableDeferred.
 *  3. The SDK launches its AuthActivity (using the Dropbox app if installed,
 *     Chrome Custom Tabs otherwise) and stores the result via SharedPreferences.
 *  4. When MainActivity resumes, it calls [checkAuthResult], which reads
 *     Auth.getDbxCredential() and completes the Deferred — success if the token
 *     changed since the snapshot, cancelled otherwise.
 */
object DropboxAuthHelper {

    private const val TAG = "DropboxAuthHelper"

    private var activity: Activity? = null
    private var pendingDeferred: CompletableDeferred<NativeAuthResult>? = null
    private var preAuthAccessToken: String? = null

    fun init(activity: Activity) {
        this.activity = activity
    }

    fun clear() {
        activity = null
        pendingDeferred?.cancel()
        pendingDeferred = null
        preAuthAccessToken = null
    }

    /**
     * Starts the Dropbox SDK auth flow. Suspends until [checkAuthResult] observes
     * a fresh credential or detects cancellation.
     */
    suspend fun authorize(appKey: String): NativeAuthResult {
        Log.d(TAG, "authorize() called, appKey=${appKey.take(6)}...")
        if (appKey.isBlank()) {
            return NativeAuthResult.Failed("DROPBOX_APP_KEY is blank — set it in local.properties")
        }
        val currentActivity = activity ?: run {
            Log.e(TAG, "Activity not available")
            return NativeAuthResult.Failed("Activity not available")
        }

        preAuthAccessToken = runCatching { Auth.getDbxCredential()?.accessToken }.getOrNull()

        val deferred = CompletableDeferred<NativeAuthResult>()
        pendingDeferred = deferred

        return try {
            val requestConfig = DbxRequestConfig.newBuilder("NoteItUP").build()
            Auth.startOAuth2PKCE(currentActivity, appKey, requestConfig)
            deferred.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Dropbox auth", e)
            pendingDeferred = null
            preAuthAccessToken = null
            NativeAuthResult.Failed("Failed to start Dropbox auth: ${e.message}")
        }
    }

    /**
     * Called from MainActivity.onResume(). Reads the SDK's stored credential and
     * completes any pending [authorize] call.
     */
    fun checkAuthResult() {
        val deferred = pendingDeferred ?: return

        val credential = runCatching { Auth.getDbxCredential() }.getOrNull()
        val accessToken = credential?.accessToken
        val isFresh = accessToken != null && accessToken != preAuthAccessToken

        pendingDeferred = null
        val snapshot = preAuthAccessToken
        preAuthAccessToken = null

        if (isFresh && credential != null) {
            val expiresAt = credential.expiresAt ?: 0L
            val expiresIn = ((expiresAt - System.currentTimeMillis()) / 1000L).let {
                if (it > 0L) it else 14400L
            }
            Log.d(TAG, "Auth result OK, token received (expiresIn=${expiresIn}s)")
            deferred.complete(
                NativeAuthResult.SuccessTokens(
                    accessToken = credential.accessToken,
                    refreshToken = credential.refreshToken,
                    expiresIn = expiresIn
                )
            )
        } else {
            Log.d(TAG, "Auth cancelled (credential=${if (credential == null) "null" else "stale"}, snapshotWas=${snapshot != null})")
            deferred.complete(NativeAuthResult.Cancelled)
        }
    }
}
