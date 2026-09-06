package com.qr.hub.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

object InAppReviewManager {
    private const val TAG = "InAppReviewManager"
    private const val PREF_NAME = "qr_hub_review_prefs"
    private const val KEY_ACTION_COUNT = "user_success_action_count"
    private const val KEY_LAST_REVIEW_TIME = "last_review_prompt_time"
    private const val REVIEW_THRESHOLD = 4 // Prompt after 4 successful actions
    private const val MIN_INTERVAL_DAYS_MS = 14L * 24 * 60 * 60 * 1000 // 14 days between prompts

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Increments the user success counter (scans, generation, exports)
     * and triggers in-app review if criteria are met.
     */
    fun recordSuccessfulAction(activity: Activity?) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) return
        val prefs = getPrefs(activity)
        val currentCount = prefs.getInt(KEY_ACTION_COUNT, 0) + 1
        val lastReviewTime = prefs.getLong(KEY_LAST_REVIEW_TIME, 0L)
        val now = System.currentTimeMillis()

        prefs.edit().putInt(KEY_ACTION_COUNT, currentCount).apply()

        if (currentCount >= REVIEW_THRESHOLD && (now - lastReviewTime > MIN_INTERVAL_DAYS_MS)) {
            triggerReviewFlow(activity)
        }
    }

    /**
     * Directly triggers Google Play In-App Review sheet.
     */
    fun triggerReviewFlow(activity: Activity) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener {
                        Log.d(TAG, "In-app review flow completed successfully.")
                        getPrefs(activity).edit()
                            .putLong(KEY_LAST_REVIEW_TIME, System.currentTimeMillis())
                            .putInt(KEY_ACTION_COUNT, 0)
                            .apply()
                    }
                } else {
                    Log.d(TAG, "In-app review request failed: ${task.exception?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating in-app review", e)
        }
    }
}
