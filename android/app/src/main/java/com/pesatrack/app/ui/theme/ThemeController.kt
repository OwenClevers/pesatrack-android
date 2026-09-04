package com.pesatrack.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class DarkModeController(
    val isDarkMode: Boolean,
    val setDarkMode: (Boolean) -> Unit
)

val LocalDarkModeController = staticCompositionLocalOf {
    DarkModeController(isDarkMode = false, setDarkMode = {})
}
