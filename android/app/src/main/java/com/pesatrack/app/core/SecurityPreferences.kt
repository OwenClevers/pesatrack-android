package com.pesatrack.app.core

import android.content.Context

object SecurityPreferences {

    private const val PREFS_NAME = "pesatrack_prefs"
    private const val KEY_LOCK_ENABLED = "app_lock_enabled"
    private const val KEY_LOCK_TIMEOUT_MS = "app_lock_timeout_ms"

    fun isLockEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_LOCK_ENABLED, false)

    fun setLockEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LOCK_ENABLED, enabled)
            .apply()
    }

    fun getLockTimeout(context: Context): LockTimeout =
        LockTimeout.fromMillis(
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(KEY_LOCK_TIMEOUT_MS, LockTimeout.IMMEDIATE.millis)
        )

    fun setLockTimeout(context: Context, timeout: LockTimeout) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LOCK_TIMEOUT_MS, timeout.millis)
            .apply()
    }
}
