package com.qr.hub.util

import android.content.Context
import android.content.SharedPreferences

object UpiPreferenceManager {
    private const val PREF_NAME = "qr_hub_upi_prefs"
    private const val KEY_DEFAULT_PACKAGE = "default_upi_package"
    private const val KEY_DEFAULT_NAME = "default_upi_name"
    private const val KEY_QUICK_PAY_ENABLED = "quick_pay_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getDefaultPackage(context: Context): String? {
        return getPrefs(context).getString(KEY_DEFAULT_PACKAGE, null)
    }

    fun getDefaultName(context: Context): String? {
        return getPrefs(context).getString(KEY_DEFAULT_NAME, null)
    }

    fun setDefaultApp(context: Context, packageName: String?, appName: String?) {
        getPrefs(context).edit().apply {
            if (packageName != null && appName != null) {
                putString(KEY_DEFAULT_PACKAGE, packageName)
                putString(KEY_DEFAULT_NAME, appName)
            } else {
                remove(KEY_DEFAULT_PACKAGE)
                remove(KEY_DEFAULT_NAME)
            }
            apply()
        }
    }

    fun isQuickPayEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_QUICK_PAY_ENABLED, false)
    }

    fun setQuickPayEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_QUICK_PAY_ENABLED, enabled).apply()
    }
}
