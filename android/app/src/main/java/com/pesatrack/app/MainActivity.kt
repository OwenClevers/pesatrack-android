package com.pesatrack.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity

// FragmentActivity (which itself extends ComponentActivity, so setContent
// below still works unchanged) rather than plain ComponentActivity --
// BiometricPrompt requires a FragmentActivity host for the security lock.
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Every screen but Onboarding has a dark green (PrimaryDark) app bar and
        // wants light status bar icons, so that's the sane platform-level
        // default -- set synchronously here rather than left to each screen's
        // first Composable frame, which raced the platform's own default style
        // on cold start (visible as a flash of dark icons on the Splash screen).
        // Onboarding still overrides this itself via StatusBarIcons since its
        // background is the plain adaptive Surface color, not PrimaryDark.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        setContent {
            PesaTrackApp()
        }
    }
}
