package com.pesatrack.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.pesatrack.app.core.ProfileController
import com.pesatrack.app.core.LocalProfileController
import com.pesatrack.app.core.ProfilePreferences
import com.pesatrack.app.core.ThemePreferences
import com.pesatrack.app.navigation.AppNavigation
import com.pesatrack.app.ui.theme.DarkModeController
import com.pesatrack.app.ui.theme.LocalDarkModeController
import com.pesatrack.app.ui.theme.PesaTrackTheme

@Composable
fun PesaTrackApp() {
    val context = LocalContext.current
    var darkModeOverride by remember { mutableStateOf(ThemePreferences.getDarkModeOverride(context)) }
    val isDark = darkModeOverride ?: isSystemInDarkTheme()

    var profileName by remember { mutableStateOf(ProfilePreferences.getName(context)) }
    var profileEmail by remember { mutableStateOf(ProfilePreferences.getEmail(context)) }

    CompositionLocalProvider(
        LocalDarkModeController provides DarkModeController(
            isDarkMode = isDark,
            setDarkMode = { enabled ->
                darkModeOverride = enabled
                ThemePreferences.setDarkModeOverride(context, enabled)
            }
        ),
        LocalProfileController provides ProfileController(
            name = profileName,
            email = profileEmail,
            setProfile = { name, email ->
                profileName = name
                profileEmail = email
                ProfilePreferences.setProfile(context, name, email)
            }
        )
    ) {
        PesaTrackTheme(darkTheme = isDark) {
            AppNavigation()
        }
    }
}
