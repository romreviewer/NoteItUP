package com.romreviewertools.noteitup.data.security

import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

/**
 * Holds a weak reference to the current activity for biometric authentication.
 */
object ActivityHolder {
    private var activityRef: WeakReference<FragmentActivity>? = null

    fun setActivity(activity: FragmentActivity) {
        activityRef = WeakReference(activity)
    }

    fun getActivity(): FragmentActivity? {
        return activityRef?.get()
    }

    fun clearActivity() {
        activityRef = null
    }
}
