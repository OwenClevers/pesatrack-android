package com.pesatrack.app.core

import android.content.Context

object ThemePreferences {

    private const val PREFS_NAME = "pesatrack_prefs"
    private const val KEY_DARK_MODE = "dark_mode_enabled"

    // Null means no explicit choice has been made yet -- follow the system setting.
    fun getDarkModeOverride(context: Context): Boolean? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null
    }

    fun setDarkModeOverride(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()
    }
}
