package com.pesatrack.app.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Sets the status bar icon/text appearance for the screen currently composed.
// Call once near the top of each screen composable rather than once at the app
// root: Compose Navigation only composes the active destination's content, and
// re-composes it on every visit, so a per-screen call reliably reasserts the
// right appearance regardless of navigation order. A single app-root call would
// only run once for the app's lifetime and wouldn't revert when navigating away
// from a screen that overrode it (e.g. Onboarding).
@Composable
fun StatusBarIcons(darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        SideEffect {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
        }
    }
}
