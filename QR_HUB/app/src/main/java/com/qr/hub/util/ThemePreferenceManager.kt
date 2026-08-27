package com.qr.hub.util

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT;

    val label: String
        get() = when (this) {
            SYSTEM -> "System Default"
            DARK -> "Dark Mode (Obsidian)"
            LIGHT -> "Light Mode (Ceramic)"
        }
}

object ThemePreferenceManager {
    private const val PREFS_NAME = "qr_hub_theme_prefs"
    private const val KEY_THEME_MODE = "key_theme_mode"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getThemeMode(context: Context): AppThemeMode {
        val raw = getPrefs(context).getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(raw ?: AppThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        getPrefs(context).edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
}
