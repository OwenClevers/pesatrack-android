package com.pesatrack.app.data.budget

import com.pesatrack.app.domain.model.BudgetThreshold
import com.pesatrack.app.domain.model.Category

// Posts (or, in tests, records) a budget-threshold notification. Kept as an
// interface so BudgetAlertChecker's orchestration is testable without a real
// NotificationManager.
interface BudgetAlertNotifier {
    // Returns whether the alert was actually shown -- BudgetAlertChecker only
    // marks a threshold fired when this is true, so e.g. a denied
    // POST_NOTIFICATIONS permission doesn't permanently burn the one-time
    // firing opportunity for a threshold the user never actually saw; if they
    // grant the permission later, a currently-crossed threshold still fires.
    fun notify(category: Category, threshold: BudgetThreshold, percent: Int): Boolean
}
