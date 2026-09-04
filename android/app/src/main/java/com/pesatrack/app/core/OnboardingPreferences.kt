package com.pesatrack.app.core

import android.content.Context

object OnboardingPreferences {

    private const val PREFS_NAME = "pesatrack_prefs"
    private const val KEY_SEEN_ONBOARDING = "seen_onboarding"

    fun hasSeenOnboarding(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SEEN_ONBOARDING, false)

    fun setSeenOnboarding(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SEEN_ONBOARDING, true)
            .apply()
    }
}
