package com.pesatrack.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Lock : Screen("lock?next={next}") {
        // "next" is a full route to land on once authenticated, from a cold
        // start where there's no back stack entry to simply pop back to
        // (see LockScreen.unlock()) -- e.g. a notification tap wants Budgets
        // instead of the usual Dashboard.
        fun route(next: String = Dashboard.route) = "lock?next=$next"
    }
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun route(transactionId: Long) = "edit_transaction/$transactionId"
    }
    object Transactions : Screen("transactions")
    object TransactionDetails : Screen("transaction_details/{transactionId}") {
        fun route(transactionId: Long) = "transaction_details/$transactionId"
    }
    object Budgets : Screen("budgets")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object Categories : Screen("categories")
    object MpesaImport : Screen("mpesa_import")
    object Backup : Screen("backup")
}