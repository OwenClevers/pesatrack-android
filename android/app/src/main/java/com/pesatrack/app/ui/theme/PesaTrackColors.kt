package com.pesatrack.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PesaTrackColorScheme(
    val expense: Color,
    val income: Color,
    val accent: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val appBar: Color
)

val LocalPesaTrackColors = staticCompositionLocalOf {
    PesaTrackColorScheme(
        expense = Expense,
        income = Income,
        accent = Accent,
        divider = Divider,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        appBar = PrimaryDark
    )
}