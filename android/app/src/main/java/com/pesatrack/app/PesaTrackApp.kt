package com.pesatrack.app

import androidx.compose.runtime.Composable
import com.pesatrack.app.navigation.AppNavigation
import com.pesatrack.app.ui.theme.PesaTrackTheme

@Composable
fun PesaTrackApp() {
    PesaTrackTheme {
        AppNavigation()
    }
}