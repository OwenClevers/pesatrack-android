package com.pesatrack.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pesatrack.app.core.AppLockState
import com.pesatrack.app.core.SecurityPreferences
import com.pesatrack.app.presentation.backup.BackupScreen
import com.pesatrack.app.presentation.budgets.BudgetsScreen
import com.pesatrack.app.presentation.categories.CategoriesScreen
import com.pesatrack.app.presentation.dashboard.DashboardScreen
import com.pesatrack.app.presentation.lock.LockScreen
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
    val context = LocalContext.current

    // Process-level (not Activity-level) lifecycle: ON_STOP/ON_START fire once
    // per real background/foreground cycle, unlike Activity callbacks which
    // also fire across configuration changes and internal navigation.
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (SecurityPreferences.isLockEnabled(context)) {
                        AppLockState.markBackgrounded()
                    }
                }
                Lifecycle.Event.ON_START -> {
                    val currentRoute = navController.currentDestination?.route
                    val alreadyGated = currentRoute == null ||
                        currentRoute == Screen.Splash.route ||
                        currentRoute == Screen.Lock.route
                    if (!alreadyGated &&
                        SecurityPreferences.isLockEnabled(context) &&
                        AppLockState.shouldRelock(SecurityPreferences.getLockTimeout(context))
                    ) {
                        navController.navigate(Screen.Lock.route)
                    }
                }
                else -> {}
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
        onDispose { ProcessLifecycleOwner.get().lifecycle.removeObserver(observer) }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }

        composable(Screen.Lock.route) {
            LockScreen(navController)
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

        composable(Screen.Backup.route) {
            BackupScreen(navController)
        }
    }
}