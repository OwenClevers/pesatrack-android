package com.pesatrack.app.data.budget

import com.pesatrack.app.domain.model.BudgetThreshold

/**
 * Pure threshold-crossing decision: given a category's current spend percent
 * and the thresholds already fired for it this month, returns the thresholds
 * that should newly fire, ascending (WARNING before EXCEEDED) so a single
 * transaction that jumps straight past 100% still notifies both in order.
 * No Android/repository dependency, so it's testable as a plain JVM unit.
 */
object BudgetAlertEvaluator {

    fun evaluate(percent: Int, alreadyFired: Set<BudgetThreshold>): List<BudgetThreshold> =
        BudgetThreshold.entries
            .filter { percent >= it.percent && it !in alreadyFired }
            .sortedBy { it.percent }
}
