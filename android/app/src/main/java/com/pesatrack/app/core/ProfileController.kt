package com.pesatrack.app.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class ProfileController(
    val name: String?,
    val email: String?,
    val setProfile: (name: String, email: String) -> Unit
)

val LocalProfileController = staticCompositionLocalOf {
    ProfileController(name = null, email = null, setProfile = { _, _ -> })
}
