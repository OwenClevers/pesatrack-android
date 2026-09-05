package com.pesatrack.app.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pesatrack.app.R
import com.pesatrack.app.core.Constants
import com.pesatrack.app.core.OnboardingPreferences
import com.pesatrack.app.navigation.Screen
import com.pesatrack.app.ui.theme.Accent
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.StatusBarIcons
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController
) {
    val context = LocalContext.current

    StatusBarIcons(darkIcons = false)

    LaunchedEffect(Unit) {
        delay(Constants.SPLASH_DELAY)
        val destination = if (OnboardingPreferences.hasSeenOnboarding(context)) {
            Screen.Dashboard.route
        } else {
            Screen.Onboarding.route
        }
        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) {
                inclusive = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDark),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_splash_mark),
            contentDescription = null,
            modifier = Modifier.size(width = 96.dp, height = 84.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "PesaTrack",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Track every shilling.",
            style = MaterialTheme.typography.bodyMedium,
            color = Accent
        )

        Spacer(Modifier.height(34.dp))

        CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = Color.White,
            strokeWidth = 2.dp
        )
    }
}