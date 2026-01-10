package com.romreviewertools.noteitup.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class UrlOpener : KoinComponent {
    private val context: Context by inject()

    actual fun openUrl(url: String) {
        try {
            // Try to use Chrome Custom Tabs for better UX
            openWithCustomTabs(url)
        } catch (e: Exception) {
            // Fallback to regular browser intent if Custom Tabs fails
            openWithBrowser(url)
        }
    }

    private fun openWithCustomTabs(url: String) {
        val uri = Uri.parse(url)

        // Use app's primary purple color for toolbar
        val primaryColor = 0xFF6750A4.toInt()

        // Create custom color scheme
        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(primaryColor)
            .build()

        // Build Custom Tabs intent with customizations
        val customTabsIntent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorSchemeParams)
            .setShowTitle(true)
            .setUrlBarHidingEnabled(false)
            .setShareState(CustomTabsIntent.SHARE_STATE_ON)
            .build()

        customTabsIntent.launchUrl(context, uri)
    }

    private fun openWithBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
