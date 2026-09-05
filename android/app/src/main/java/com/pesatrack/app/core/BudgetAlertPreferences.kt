package com.pesatrack.app.core

import android.content.Context
import com.pesatrack.app.domain.model.BudgetThreshold

object BudgetAlertPreferences {

    private const val PREFS_NAME = "pesatrack_prefs"
    private const val KEY_ENABLED = "budget_alerts_enabled"
    private const val KEY_THRESHOLD_PREFIX = "budget_alert_threshold_"

    // Helpful by default -- unlike App lock, there's no invasive/opt-in
    // reason to start this off.
    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    fun isThresholdEnabled(context: Context, threshold: BudgetThreshold): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_THRESHOLD_PREFIX + threshold.name, true)

    fun setThresholdEnabled(context: Context, threshold: BudgetThreshold, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_THRESHOLD_PREFIX + threshold.name, enabled)
            .apply()
    }

    // What BudgetAlertChecker.check() should actually be allowed to fire --
    // empty (and thus a no-op check) when the master toggle is off.
    fun enabledThresholds(context: Context): Set<BudgetThreshold> {
        if (!isEnabled(context)) return emptySet()
        return BudgetThreshold.entries.filterTo(mutableSetOf()) { isThresholdEnabled(context, it) }
    }
}
