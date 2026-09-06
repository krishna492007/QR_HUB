package com.qr.hub.util

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

object InAppUpdateManager {
    private const val TAG = "InAppUpdateManager"
    const val UPDATE_REQUEST_CODE = 9001

    private var appUpdateManager: AppUpdateManager? = null
    private var installListener: InstallStateUpdatedListener? = null

    /**
     * Initializes and checks for available updates on Google Play.
     * @param manualCheck If true, shows user feedback when already on latest version or when not installed via Play Store.
     */
    fun checkForAppUpdate(
        activity: Activity,
        manualCheck: Boolean = false,
        onFlexibleUpdateDownloaded: (() -> Unit)? = null
    ) {
        try {
            val manager = AppUpdateManagerFactory.create(activity)
            appUpdateManager = manager

            installListener = InstallStateUpdatedListener { state ->
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    Log.d(TAG, "In-app update downloaded and ready for install.")
                    onFlexibleUpdateDownloaded?.invoke()
                }
            }
            manager.registerListener(installListener!!)

            val appUpdateInfoTask = manager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    val isFlexibleAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    val isImmediateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

                    if (isFlexibleAllowed) {
                        startUpdateFlow(activity, manager, appUpdateInfo, AppUpdateType.FLEXIBLE)
                    } else if (isImmediateAllowed) {
                        startUpdateFlow(activity, manager, appUpdateInfo, AppUpdateType.IMMEDIATE)
                    }
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                    if (manualCheck) {
                        Toast.makeText(
                            activity,
                            "You are using the latest version of QR HUB!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.addOnFailureListener { e ->
                Log.d(TAG, "Update check failed or no update available: ${e.message}")
                if (manualCheck) {
                    // Fallback to opening Play Store if in debug build or not from Play Store
                    Toast.makeText(
                        activity,
                        "Opening Google Play Store to check for updates...",
                        Toast.LENGTH_SHORT
                    ).show()
                    try {
                        activity.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${activity.packageName}")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                    } catch (_: Exception) {
                        try {
                            activity.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${activity.packageName}")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            )
                        } catch (_: Exception) {}
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for in-app updates", e)
        }
    }

    private fun startUpdateFlow(
        activity: Activity,
        manager: AppUpdateManager,
        info: AppUpdateInfo,
        type: Int
    ) {
        try {
            manager.startUpdateFlowForResult(
                info,
                type,
                activity,
                UPDATE_REQUEST_CODE
            )
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "Failed to start update flow", e)
        }
    }

    /**
     * Called in Activity onResume to handle completed downloads or ongoing updates.
     */
    fun onResume(activity: Activity, onFlexibleUpdateDownloaded: (() -> Unit)? = null) {
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                onFlexibleUpdateDownloaded?.invoke()
            }
        }
    }

    /**
     * Triggers immediate installation of a downloaded update (restarts app).
     */
    fun completeUpdate() {
        appUpdateManager?.completeUpdate()
    }

    /**
     * Clean up listeners when activity is destroyed.
     */
    fun onDestroy() {
        installListener?.let {
            appUpdateManager?.unregisterListener(it)
        }
    }
}
