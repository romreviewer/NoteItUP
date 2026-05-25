package com.romreviewertools.noteitup.data.cloud

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
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
    private var pendingDeferred: CompletableDeferred<NativeAuthResult>? = null

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
     * Returns the serverAuthCode on success, or a Failed/Cancelled result describing why.
     */
    suspend fun authorize(webClientId: String): NativeAuthResult {
        Log.d(TAG, "authorize() called, clientId=${webClientId.take(10)}...")
        println("[GoogleDriveAuthHelper] authorize() clientId=${webClientId.take(10)}...")
        val currentActivity = activity ?: run {
            Log.e(TAG, "Activity not available")
            return NativeAuthResult.Failed("Activity not available")
        }
        val currentLauncher = launcher ?: run {
            Log.e(TAG, "Launcher not available")
            return NativeAuthResult.Failed("Launcher not available")
        }

        val deferred = CompletableDeferred<NativeAuthResult>()
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
                            Log.d(TAG, "Launching consent UI for user authorization")
                            val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent).build()
                            currentLauncher.launch(intentSenderRequest)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to launch consent UI", e)
                            println("[GoogleDriveAuthHelper] launch consent UI failed: ${e.javaClass.simpleName}: ${e.message}")
                            deferred.complete(NativeAuthResult.Failed("Failed to launch consent UI: ${e.message}"))
                        }
                    } else {
                        Log.e(TAG, "Resolution required but no PendingIntent")
                        deferred.complete(NativeAuthResult.Failed("Resolution required but no PendingIntent"))
                    }
                } else {
                    // Already authorized — extract server auth code
                    val code = result.serverAuthCode
                    if (code != null) {
                        Log.d(TAG, "Already authorized, code received (${code.take(10)}...)")
                        deferred.complete(NativeAuthResult.Success(code))
                    } else {
                        Log.e(TAG, "Authorized but no serverAuthCode returned")
                        println("[GoogleDriveAuthHelper] authorized but serverAuthCode=null — check that GOOGLE_CLIENT_ID is the Web client ID and offline access is enabled")
                        deferred.complete(NativeAuthResult.Failed("No serverAuthCode returned. Verify GOOGLE_CLIENT_ID is the Web OAuth client ID."))
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Authorization failed", e)
                val detail = describeAuthFailure(e)
                println("[GoogleDriveAuthHelper] authorize() failed: $detail")
                deferred.complete(NativeAuthResult.Failed(detail))
            }

        return deferred.await()
    }

    /**
     * Called from the ActivityResultLauncher callback in MainActivity.
     * Completes the pending deferred with the auth result.
     */
    fun handleAuthResult(resultCode: Int, data: Intent?) {
        Log.d(TAG, "handleAuthResult: resultCode=$resultCode")
        println("[GoogleDriveAuthHelper] handleAuthResult: resultCode=$resultCode")
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
                        Log.d(TAG, "Auth result OK, code received (${code.take(10)}...)")
                        deferred.complete(NativeAuthResult.Success(code))
                    } else {
                        Log.e(TAG, "Auth result OK but no serverAuthCode")
                        println("[GoogleDriveAuthHelper] consent OK but serverAuthCode=null — Web client ID may be misconfigured")
                        deferred.complete(NativeAuthResult.Failed("Consent succeeded but no serverAuthCode. Verify GOOGLE_CLIENT_ID is a Web OAuth client ID with offline access."))
                    }
                } catch (e: ApiException) {
                    Log.e(TAG, "Failed to parse auth result (ApiException)", e)
                    val detail = describeAuthFailure(e)
                    println("[GoogleDriveAuthHelper] parse auth result failed: $detail")
                    deferred.complete(NativeAuthResult.Failed(detail))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse auth result", e)
                    println("[GoogleDriveAuthHelper] parse auth result failed: ${e.javaClass.simpleName}: ${e.message}")
                    deferred.complete(NativeAuthResult.Failed("Failed to parse auth result: ${e.message}"))
                }
            } else {
                Log.e(TAG, "Auth result OK but activity=${currentActivity != null}, data=${data != null}")
                deferred.complete(NativeAuthResult.Failed("Auth result OK but missing activity/data"))
            }
        } else {
            // RESULT_CANCELED or RESULT_FIRST_USER — the user dismissed the consent sheet
            Log.d(TAG, "Auth cancelled, resultCode=$resultCode")
            deferred.complete(NativeAuthResult.Cancelled)
        }
    }

    private fun describeAuthFailure(e: Throwable): String {
        if (e is ApiException) {
            val code = e.statusCode
            val hint = when (code) {
                10 -> " — DEVELOPER_ERROR: the OAuth client is misconfigured. Make sure your Google Cloud project has an Android OAuth client registered with the app package name (com.romreviewertools.noteitup) and the SHA-1 of the signing certificate, AND that GOOGLE_CLIENT_ID in local.properties is the Web OAuth client ID."
                16 -> " — CANCELED by the system"
                7 -> " — NETWORK_ERROR"
                17 -> " — API_NOT_CONNECTED: Google Play Services unavailable"
                else -> ""
            }
            return "ApiException statusCode=$code${hint} (${e.message ?: ""})"
        }
        return "${e.javaClass.simpleName}: ${e.message ?: "unknown"}"
    }
}
