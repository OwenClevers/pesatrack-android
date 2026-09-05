package com.pesatrack.app.domain.model

// Fixed, product-defined thresholds -- not arbitrary user-configurable
// percentages. Each fires at most once per category per month; see
// BudgetAlertRepository.
enum class BudgetThreshold(val percent: Int, val label: String) {
    WARNING(80, "Warning at 80%"),
    EXCEEDED(100, "Exceeded at 100%")
}
