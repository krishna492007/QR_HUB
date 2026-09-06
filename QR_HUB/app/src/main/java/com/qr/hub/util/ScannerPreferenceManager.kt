package com.qr.hub.util

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.ToneGenerator

object ScannerPreferenceManager {
    private const val PREF_NAME = "qr_hub_scanner_prefs"
    private const val KEY_BEEP_ENABLED = "scan_beep_enabled"
    private const val KEY_VIBRATE_ENABLED = "scan_vibrate_enabled"

    private var toneGenerator: ToneGenerator? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isBeepEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BEEP_ENABLED, true)
    }

    fun setBeepEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BEEP_ENABLED, enabled).apply()
    }

    fun isVibrateEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VIBRATE_ENABLED, true)
    }

    fun setVibrateEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_VIBRATE_ENABLED, enabled).apply()
    }

    /**
     * Plays a crisp high-pitch scanner beep sound (100ms) with zero latency.
     */
    fun playScanBeep(context: Context) {
        if (!isBeepEnabled(context)) return
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        } catch (_: Exception) {
            try {
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85).startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            } catch (_: Exception) {}
        }
    }
}
