package com.pesatrack.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object AddTransaction : Screen("add_transaction")
}