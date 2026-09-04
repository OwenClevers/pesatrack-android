package com.pesatrack.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PesaTrackColorScheme(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val appBar: Color,
    val expense: Color,
    val income: Color,
    val accent: Color
)

val LightPesaTrackColors = PesaTrackColorScheme(
    isDark = false,
    background = BackgroundLight,
    surface = SurfaceLight,
    divider = DividerLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    appBar = AppBarLight,
    expense = Expense,
    income = Income,
    accent = Accent
)

val DarkPesaTrackColors = PesaTrackColorScheme(
    isDark = true,
    background = BackgroundDark,
    surface = SurfaceDark,
    divider = DividerDark,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    appBar = AppBarDark,
    expense = Expense,
    income = Income,
    accent = Accent
)

val LocalPesaTrackColors = staticCompositionLocalOf { LightPesaTrackColors }

// Theme-aware aliases. Every screen already imports these five names (plus
// PrimaryDark for the app bar) expecting a plain Color; keeping the names but
// sourcing them from LocalPesaTrackColors means no screen has to change to pick
// up dark mode -- PesaTrackTheme just has to provide the right scheme instance.
val Background: Color @Composable get() = LocalPesaTrackColors.current.background
val Surface: Color @Composable get() = LocalPesaTrackColors.current.surface
val Divider: Color @Composable get() = LocalPesaTrackColors.current.divider
val TextPrimary: Color @Composable get() = LocalPesaTrackColors.current.textPrimary
val TextSecondary: Color @Composable get() = LocalPesaTrackColors.current.textSecondary
val PrimaryDark: Color @Composable get() = LocalPesaTrackColors.current.appBar
