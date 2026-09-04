package com.pesatrack.app.core

fun formatKsh(amount: Double): String =
    "KSh " + String.format("%,.0f", amount)