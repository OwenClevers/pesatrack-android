package com.pesatrack.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pesatrack.app.presentation.budgets.BudgetsScreen
import com.pesatrack.app.presentation.categories.CategoriesScreen
import com.pesatrack.app.presentation.dashboard.DashboardScreen
import com.pesatrack.app.presentation.mpesa.MpesaImportScreen
import com.pesatrack.app.presentation.onboarding.OnboardingScreen
import com.pesatrack.app.presentation.reports.ReportsScreen
import com.pesatrack.app.presentation.settings.SettingsScreen
import com.pesatrack.app.presentation.splash.SplashScreen
import com.pesatrack.app.presentation.transactions.AddTransactionScreen
import com.pesatrack.app.presentation.transactions.TransactionDetailsScreen
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

        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController)
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(navController)
        }

        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: return@composable
            AddTransactionScreen(navController, transactionId)
        }

        composable(Screen.Transactions.route) {
            TransactionsScreen(navController)
        }

        composable(
            route = Screen.TransactionDetails.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: return@composable
            TransactionDetailsScreen(navController, transactionId)
        }

        composable(Screen.Budgets.route) {
            BudgetsScreen(navController)
        }

        composable(Screen.Reports.route) {
            ReportsScreen(navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(navController)
        }

        composable(Screen.MpesaImport.route) {
            MpesaImportScreen(navController)
        }
    }
}