package com.romreviewertools.noteitup.data.cloud

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.CompletableDeferred

/**
 * Singleton that bridges Android's AuthorizationClient (callback-based)
 * with Kotlin coroutines for Google Drive OAuth.
 *
 * Flow:
 * 1. MainActivity registers an ActivityResultLauncher and calls init()
 * 2. CloudSyncViewModel calls authorize() which returns a serverAuthCode via CompletableDeferred
 * 3. If user consent is needed, the launcher shows the native Google consent UI
 * 4. handleAuthResult() completes the deferred with the auth code
 */
object GoogleDriveAuthHelper {

    private const val TAG = "GoogleDriveAuthHelper"
    private const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"

    private var activity: Activity? = null
    private var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var pendingDeferred: CompletableDeferred<String?>? = null

    fun init(activity: Activity, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        this.activity = activity
        this.launcher = launcher
    }

    fun clear() {
        activity = null
        launcher = null
        pendingDeferred?.cancel()
        pendingDeferred = null
    }

    /**
     * Starts native Google authorization for Drive appdata scope.
     * Returns the serverAuthCode on success, or null on failure/cancellation.
     */
    suspend fun authorize(webClientId: String): String? {
        val currentActivity = activity ?: run {
            Log.e(TAG, "Activity not available")
            return null
        }
        val currentLauncher = launcher ?: run {
            Log.e(TAG, "Launcher not available")
            return null
        }

        val deferred = CompletableDeferred<String?>()
        pendingDeferred = deferred

        val authRequest = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DRIVE_APPDATA_SCOPE)))
            .requestOfflineAccess(webClientId)
            .build()

        val authClient: AuthorizationClient = Identity.getAuthorizationClient(currentActivity)

        authClient.authorize(authRequest)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    // User needs to grant consent — launch the native UI
                    val pendingIntent = result.pendingIntent
                    if (pendingIntent != null) {
                        try {
                            val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent).build()
                            currentLauncher.launch(intentSenderRequest)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to launch consent UI", e)
                            deferred.complete(null)
                        }
                    } else {
                        Log.e(TAG, "Resolution required but no PendingIntent")
                        deferred.complete(null)
                    }
                } else {
                    // Already authorized — extract server auth code
                    val code = result.serverAuthCode
                    if (code != null) {
                        deferred.complete(code)
                    } else {
                        Log.e(TAG, "Authorized but no serverAuthCode returned")
                        deferred.complete(null)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Authorization failed", e)
                deferred.complete(null)
            }

        return deferred.await()
    }

    /**
     * Called from the ActivityResultLauncher callback in MainActivity.
     * Completes the pending deferred with the auth result.
     */
    fun handleAuthResult(resultCode: Int, data: Intent?) {
        val deferred = pendingDeferred ?: return
        pendingDeferred = null

        if (resultCode == Activity.RESULT_OK) {
            val currentActivity = activity
            if (currentActivity != null && data != null) {
                try {
                    val authClient = Identity.getAuthorizationClient(currentActivity)
                    val authResult = authClient.getAuthorizationResultFromIntent(data)
                    val code = authResult.serverAuthCode
                    if (code != null) {
                        deferred.complete(code)
                    } else {
                        Log.e(TAG, "Auth result OK but no serverAuthCode")
                        deferred.complete(null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse auth result", e)
                    deferred.complete(null)
                }
            } else {
                deferred.complete(null)
            }
        } else {
            Log.d(TAG, "Auth cancelled or failed, resultCode=$resultCode")
            deferred.complete(null)
        }
    }
}
