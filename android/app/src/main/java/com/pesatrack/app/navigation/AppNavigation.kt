package com.pesatrack.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pesatrack.app.presentation.dashboard.DashboardScreen
import com.pesatrack.app.presentation.splash.SplashScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH
    ) {

        composable(Destinations.SPLASH) {
            SplashScreen()
        }

        composable(Destinations.DASHBOARD) {
            DashboardScreen()
        }
    }
}