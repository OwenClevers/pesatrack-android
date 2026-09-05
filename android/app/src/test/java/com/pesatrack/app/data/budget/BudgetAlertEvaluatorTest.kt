package com.pesatrack.app.data.budget

import com.pesatrack.app.domain.model.BudgetThreshold
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetAlertEvaluatorTest {

    @Test
    fun `below warning threshold fires nothing`() {
        assertEquals(emptyList<BudgetThreshold>(), BudgetAlertEvaluator.evaluate(percent = 50, alreadyFired = emptySet()))
        assertEquals(emptyList<BudgetThreshold>(), BudgetAlertEvaluator.evaluate(percent = 79, alreadyFired = emptySet()))
    }

    @Test
    fun `exactly at warning threshold fires warning only`() {
        assertEquals(listOf(BudgetThreshold.WARNING), BudgetAlertEvaluator.evaluate(percent = 80, alreadyFired = emptySet()))
    }

    @Test
    fun `between warning and exceeded fires warning only`() {
        assertEquals(listOf(BudgetThreshold.WARNING), BudgetAlertEvaluator.evaluate(percent = 95, alreadyFired = emptySet()))
    }

    @Test
    fun `at or past exceeded fires both, warning before exceeded`() {
        assertEquals(
            listOf(BudgetThreshold.WARNING, BudgetThreshold.EXCEEDED),
            BudgetAlertEvaluator.evaluate(percent = 100, alreadyFired = emptySet())
        )
        assertEquals(
            listOf(BudgetThreshold.WARNING, BudgetThreshold.EXCEEDED),
            BudgetAlertEvaluator.evaluate(percent = 150, alreadyFired = emptySet())
        )
    }

    @Test
    fun `already-fired warning is excluded even though still crossed`() {
        assertEquals(
            emptyList<BudgetThreshold>(),
            BudgetAlertEvaluator.evaluate(percent = 85, alreadyFired = setOf(BudgetThreshold.WARNING))
        )
    }

    @Test
    fun `warning already fired but exceeded newly crossed fires only exceeded`() {
        assertEquals(
            listOf(BudgetThreshold.EXCEEDED),
            BudgetAlertEvaluator.evaluate(percent = 120, alreadyFired = setOf(BudgetThreshold.WARNING))
        )
    }

    @Test
    fun `both already fired fires nothing regardless of percent`() {
        val bothFired = setOf(BudgetThreshold.WARNING, BudgetThreshold.EXCEEDED)
        assertEquals(emptyList<BudgetThreshold>(), BudgetAlertEvaluator.evaluate(percent = 200, alreadyFired = bothFired))
    }
}
