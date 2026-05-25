package com.romreviewertools.noteitup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.romreviewertools.noteitup.data.cloud.CloudProviderType
import com.romreviewertools.noteitup.data.cloud.DropboxAuthHelper
import com.romreviewertools.noteitup.data.cloud.GoogleDriveAuthHelper
import com.romreviewertools.noteitup.data.review.InAppReviewManager
import com.romreviewertools.noteitup.data.security.ActivityHolder
import com.romreviewertools.noteitup.presentation.screens.cloudsync.OAuthCallback
import com.romreviewertools.noteitup.presentation.screens.cloudsync.OAuthCallbackEmitter
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    private val inAppReviewManager: InAppReviewManager by inject()

    private val googleAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        GoogleDriveAuthHelper.handleAuthResult(result.resultCode, result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Register activity for biometric authentication
        ActivityHolder.setActivity(this)

        // Register activity for in-app review
        inAppReviewManager.setActivity(this)

        // Register activity for Google Drive native auth
        GoogleDriveAuthHelper.init(this, googleAuthLauncher)

        // Register activity for Dropbox SDK native auth
        DropboxAuthHelper.init(this)

        // Handle OAuth callback from initial launch
        handleOAuthIntent(intent)

        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        ActivityHolder.setActivity(this)
        inAppReviewManager.setActivity(this)
        GoogleDriveAuthHelper.init(this, googleAuthLauncher)
        DropboxAuthHelper.init(this)
        // The Dropbox SDK auth flow returns via SharedPreferences, so we poll on resume.
        DropboxAuthHelper.checkAuthResult()
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHolder.clearActivity()
        inAppReviewManager.clearActivity()
        GoogleDriveAuthHelper.clear()
        DropboxAuthHelper.clear()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val uri = intent?.data
        Log.d(TAG, "handleOAuthIntent: uri=$uri")
        if (uri == null) return
        val scheme = uri.scheme ?: return
        Log.d(TAG, "OAuth intent: scheme=$scheme, host=${uri.host}")

        // Google Drive OAuth callback. Dropbox is now handled by the SDK's AuthActivity.
        if (scheme == "com.romreviewertools.noteitup" && uri.host == "oauth2callback") {
            val code = uri.getQueryParameter("code")
            Log.d(TAG, "Google Drive callback: code=${if (code != null) "found (${code.take(10)}...)" else "null"}")
            if (code != null) {
                OAuthCallbackHolder.pendingCode = code
                // Emit to SharedFlow for reactive handling
                OAuthCallbackEmitter.emit(OAuthCallback(code, CloudProviderType.GOOGLE_DRIVE))
            }
        } else {
            Log.d(TAG, "OAuth intent: scheme not recognized, ignoring")
        }
    }
}

/**
 * Simple holder for the Google Drive OAuth callback code.
 * The CloudSyncScreen will check this when it's displayed.
 */
object OAuthCallbackHolder {
    var pendingCode: String? = null

    fun consumeCode(): String? {
        val code = pendingCode ?: return null
        pendingCode = null
        return code
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}