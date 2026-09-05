package com.pesatrack.app.presentation.lock

import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pesatrack.app.core.SecurityPreferences
import com.pesatrack.app.data.security.AuthResult
import com.pesatrack.app.data.security.BiometricAuthenticator
import com.pesatrack.app.data.security.findFragmentActivity
import com.pesatrack.app.navigation.Screen
import com.pesatrack.app.ui.theme.Accent
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.StatusBarIcons
import kotlinx.coroutines.launch

@Composable
fun LockScreen(navController: NavController, next: String = Screen.Dashboard.route) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val authenticator = remember(activity) { activity?.let(::BiometricAuthenticator) }
    val scope = rememberCoroutineScope()

    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Block system Back -- there is nothing safe to reveal underneath an
    // unauthenticated lock screen. Home still backgrounds the app normally.
    BackHandler(enabled = true) {}

    StatusBarIcons(darkIcons = false)

    fun unlock() {
        val destination = navController.previousBackStackEntry
        if (destination != null) {
            navController.popBackStack()
        } else {
            navController.navigate(next) {
                popUpTo(Screen.Lock.route) { inclusive = true }
            }
        }
    }

    fun attemptAuth() {
        val bio = authenticator
        if (bio == null) {
            // No FragmentActivity host somehow -- fail safe by not locking the
            // user out of their own data.
            SecurityPreferences.setLockEnabled(context, false)
            unlock()
            return
        }
        scope.launch {
            when (bio.authenticate(title = "Unlock PesaTrack")) {
                AuthResult.SUCCESS -> unlock()
                AuthResult.CANCELLED -> statusMessage = null
                AuthResult.FAILED -> statusMessage = "Authentication failed. Try again."
                AuthResult.UNAVAILABLE -> {
                    // The device's screen lock was removed after enabling this --
                    // there's no way to satisfy the gate any more, so turn it off
                    // rather than lock the user out permanently.
                    SecurityPreferences.setLockEnabled(context, false)
                    unlock()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        attemptAuth()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(56.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "PesaTrack is locked",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Authenticate to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = Accent
        )

        AnimatedVisibility(visible = statusMessage != null) {
            Column {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = statusMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { attemptAuth() },
            shape = RoundedCornerShape(11.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Unlock")
        }
    }
}
