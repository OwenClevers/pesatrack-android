package com.pesatrack.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object AddTransaction : Screen("add_transaction")
    object Transactions : Screen("transactions")
    object TransactionDetails : Screen("transaction_details/{transactionId}") {
        fun route(transactionId: Long) = "transaction_details/$transactionId"
    }
    object Budgets : Screen("budgets")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object Categories : Screen("categories")
}