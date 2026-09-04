package com.pesatrack.app.ui.theme

import androidx.compose.ui.graphics.Color

// Brand — identical in both themes so the semantics (expense red, income green,
// brand green) hold regardless of light/dark; only surfaces, text and containers
// below adapt per theme.
val Primary = Color(0xFF00A859)
val Secondary = Color(0xFF1EB980)
val Accent = Color(0xFFFFC107)

// Semantic
val Expense = Color(0xFFE53935)
val Income = Color(0xFF43A047)

// App bar — a darker shade of Primary; dark mode goes darker still so it doesn't
// glow against a dark background.
val AppBarLight = Color(0xFF00703C)
val AppBarDark = Color(0xFF00341C)

// Surfaces — light
val BackgroundLight = Color(0xFFF7F8FA)
val SurfaceLight = Color(0xFFFFFFFF)
val DividerLight = Color(0xFFE9EBEF)

// Surfaces — dark
val BackgroundDark = Color(0xFF121316)
val SurfaceDark = Color(0xFF1C1E22)
val DividerDark = Color(0xFF2E3138)

// Text — light
val TextPrimaryLight = Color(0xFF111827)
val TextSecondaryLight = Color(0xFF6B7280)

// Text — dark
val TextPrimaryDark = Color(0xFFF3F4F6)
val TextSecondaryDark = Color(0xFF9CA3AF)

// Onboarding
val OnboardingArtBackground = Color(0xFFEDF7F1)
val OnboardingDotInactive = Color(0xFFDDE1E7)
val OnboardingArtBackgroundDark = Color(0xFF1B2620)
val OnboardingDotInactiveDark = Color(0xFF3A3D42)

// Category tints — container / content pairs, light
val FoodContainer = Color(0xFFFFF8E1)
val FoodContent = Color(0xFFA67C00)
val FuelContainer = Color(0xFFE8F5E9)
val FuelContent = Color(0xFF43A047)
val ShoppingContainer = Color(0xFFFCE4EC)
val ShoppingContent = Color(0xFFC2185B)
val UtilitiesContainer = Color(0xFFE3F2FD)
val UtilitiesContent = Color(0xFF1565C0)
val EntertainmentContainer = Color(0xFFEDE7F6)
val EntertainmentContent = Color(0xFF5E35B1)
val TransportContainer = Color(0xFFE0F2F1)
val TransportContent = Color(0xFF00796B)
val MedicalContainer = Color(0xFFFDECEA)
val MedicalContent = Color(0xFFE53935)
val EducationContainer = Color(0xFFE8EAF6)
val EducationContent = Color(0xFF3949AB)
val OtherContainer = Color(0xFFF1F2F4)
val OtherContent = Color(0xFF5F6570)

// Category tints — container / content pairs, dark. A dark, low-chroma container
// with a bright content color keeps the icon legible instead of the light pastel
// containers above going muddy on a dark surface.
val FoodContainerDark = Color(0xFF4A3B00)
val FoodContentDark = Color(0xFFFFD54F)
val FuelContainerDark = Color(0xFF1B4620)
val FuelContentDark = Color(0xFF81C995)
val ShoppingContainerDark = Color(0xFF4A1330)
val ShoppingContentDark = Color(0xFFF48FB1)
val UtilitiesContainerDark = Color(0xFF0D3B66)
val UtilitiesContentDark = Color(0xFF90CAF9)
val EntertainmentContainerDark = Color(0xFF2E1F52)
val EntertainmentContentDark = Color(0xFFB39DDB)
val TransportContainerDark = Color(0xFF0F3D38)
val TransportContentDark = Color(0xFF80CBC4)
val MedicalContainerDark = Color(0xFF4E1512)
val MedicalContentDark = Color(0xFFEF9A9A)
val EducationContainerDark = Color(0xFF1F2452)
val EducationContentDark = Color(0xFF9FA8DA)
val OtherContainerDark = Color(0xFF33363B)
val OtherContentDark = Color(0xFFC7CAD1)
