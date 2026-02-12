package com.romreviewertools.noteitup

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.romreviewertools.noteitup.data.cloud.CloudProviderType
import com.romreviewertools.noteitup.data.cloud.GoogleDriveAuthHelper
import com.romreviewertools.noteitup.data.review.InAppReviewManager
import com.romreviewertools.noteitup.data.security.ActivityHolder
import com.romreviewertools.noteitup.presentation.screens.cloudsync.OAuthCallback
import com.romreviewertools.noteitup.presentation.screens.cloudsync.OAuthCallbackEmitter
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {
    private val inAppReviewManager: InAppReviewManager by inject()

    private val googleAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        GoogleDriveAuthHelper.handleAuthResult(result.resultCode, result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Register activity for biometric authentication
        ActivityHolder.setActivity(this)

        // Register activity for in-app review
        inAppReviewManager.setActivity(this)

        // Register activity for Google Drive native auth
        GoogleDriveAuthHelper.init(this, googleAuthLauncher)

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
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHolder.clearActivity()
        inAppReviewManager.clearActivity()
        GoogleDriveAuthHelper.clear()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        val scheme = uri.scheme ?: return

        // Check if this is Google Drive OAuth callback
        if (scheme == "com.romreviewertools.noteitup" && uri.host == "oauth2callback") {
            val code = uri.getQueryParameter("code")
            if (code != null) {
                OAuthCallbackHolder.pendingCode = code
                OAuthCallbackHolder.isDropbox = false
                // Emit to SharedFlow for reactive handling
                OAuthCallbackEmitter.emit(OAuthCallback(code, CloudProviderType.GOOGLE_DRIVE))
            }
        }
        // Check if this is Dropbox OAuth callback (db-APP_KEY format)
        else if (scheme.startsWith("db-")) {
            val code = uri.getQueryParameter("code")
            if (code != null) {
                OAuthCallbackHolder.pendingCode = code
                OAuthCallbackHolder.isDropbox = true
                // Emit to SharedFlow for reactive handling
                OAuthCallbackEmitter.emit(OAuthCallback(code, CloudProviderType.DROPBOX))
            }
        }
    }
}

/**
 * Simple holder for OAuth callback data.
 * The CloudSyncScreen will check this when it's displayed.
 */
object OAuthCallbackHolder {
    var pendingCode: String? = null
    var isDropbox: Boolean = false

    fun consumeCode(): Pair<String, Boolean>? {
        val code = pendingCode ?: return null
        val dropbox = isDropbox
        pendingCode = null
        return code to dropbox
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}