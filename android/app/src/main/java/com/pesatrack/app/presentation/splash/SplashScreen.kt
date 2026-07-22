package com.pesatrack.app.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pesatrack.app.core.Constants
import com.pesatrack.app.navigation.Screen
import com.pesatrack.app.ui.theme.PesaTrackColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController
) {

    LaunchedEffect(Unit) {

        delay(Constants.SPLASH_DELAY)

        navController.navigate(Screen.Dashboard.route) {

            popUpTo(Screen.Splash.route) {
                inclusive = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PesaTrackColors.Background),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            "💰",
            fontSize = 72.sp
        )

        Text(
            text = "PesaTrack",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Smart Personal Finance",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}