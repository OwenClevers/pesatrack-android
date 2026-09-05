package com.pesatrack.app.core

object Constants {

    const val APP_NAME = "PesaTrack"

    const val SPLASH_DELAY = 2000L

    // Intent extra carrying where a notification tap should land the app,
    // read by SplashScreen once the normal onboarding/lock gating decides the
    // app is otherwise headed to Dashboard.
    const val EXTRA_NAVIGATE_TO = "navigate_to"
    const val NAVIGATE_TARGET_BUDGETS = "budgets"
}