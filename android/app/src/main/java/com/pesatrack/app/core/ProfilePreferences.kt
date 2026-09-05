package com.pesatrack.app.core

import android.content.Context

object ProfilePreferences {

    private const val PREFS_NAME = "pesatrack_prefs"
    private const val KEY_NAME = "profile_name"
    private const val KEY_EMAIL = "profile_email"

    // Null means unset -- callers fall back to a generic greeting/prompt.
    fun getName(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_NAME, null)

    fun getEmail(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_EMAIL, null)

    fun setProfile(context: Context, name: String?, email: String?) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        if (name.isNullOrBlank()) editor.remove(KEY_NAME) else editor.putString(KEY_NAME, name)
        if (email.isNullOrBlank()) editor.remove(KEY_EMAIL) else editor.putString(KEY_EMAIL, email)
        editor.apply()
    }
}
