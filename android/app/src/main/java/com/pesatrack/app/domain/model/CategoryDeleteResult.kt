package com.pesatrack.app.domain.model

sealed interface CategoryDeleteResult {
    data object Deleted : CategoryDeleteResult
    data class Blocked(val transactionCount: Int) : CategoryDeleteResult
}
