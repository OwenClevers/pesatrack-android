package com.pesatrack.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pesatrack.app.presentation.dashboard.DashboardScreen
import com.pesatrack.app.presentation.splash.SplashScreen
import com.pesatrack.app.presentation.transactions.AddTransactionScreen
import com.pesatrack.app.presentation.transactions.TransactionsScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(navController)
        }

        composable(Screen.Transactions.route) {
            TransactionsScreen(navController)
        }
    }
}