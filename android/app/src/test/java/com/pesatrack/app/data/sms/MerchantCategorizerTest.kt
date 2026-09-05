package com.pesatrack.app.data.sms

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.fake.FakeMerchantCategoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MerchantCategorizerTest {

    private val food = Category(id = 1, name = "Food", iconKey = "food", colorKey = "food")
    private val fuel = Category(id = 2, name = "Fuel", iconKey = "fuel", colorKey = "fuel")
    private val shopping = Category(id = 3, name = "Shopping", iconKey = "shopping", colorKey = "shopping")
    private val utilities = Category(id = 4, name = "Utilities", iconKey = "utilities", colorKey = "utilities")
    private val other = Category(id = 5, name = "Other", iconKey = "other", colorKey = "other")

    private val categories = listOf(food, fuel, shopping, utilities, other)

    @Test
    fun `learned mapping takes priority over keyword match`() = runTest {
        val repository = FakeMerchantCategoryRepository(mapOf("SHELL" to food.id))
        val categorizer = MerchantCategorizer(repository)

        // "SHELL" would keyword-match fuel, but the learned mapping wins.
        assertEquals(food.id, categorizer.classify("Shell", categories))
    }

    @Test
    fun `learned mapping is ignored if its category no longer exists`() = runTest {
        val repository = FakeMerchantCategoryRepository(mapOf("SHELL" to 999L))
        val categorizer = MerchantCategorizer(repository)

        assertEquals(fuel.id, categorizer.classify("Shell", categories))
    }

    @Test
    fun `learned mapping lookup is case and whitespace insensitive`() = runTest {
        val repository = FakeMerchantCategoryRepository()
        repository.learn("  Naivas Supermarket  ", food.id)
        val categorizer = MerchantCategorizer(repository)

        assertEquals(food.id, categorizer.classify("naivas supermarket", categories))
    }

    @Test
    fun `specificity scoring resolves NAIVAS FUEL STATION to shopping not fuel`() = runTest {
        val categorizer = MerchantCategorizer(FakeMerchantCategoryRepository())

        assertEquals(shopping.id, categorizer.classify("NAIVAS FUEL STATION", categories))
    }

    @Test
    fun `single keyword match resolves to its category`() = runTest {
        val categorizer = MerchantCategorizer(FakeMerchantCategoryRepository())

        assertEquals(fuel.id, categorizer.classify("Total Energies Karen", categories))
    }

    @Test
    fun `Airtime resolves to utilities`() = runTest {
        val categorizer = MerchantCategorizer(FakeMerchantCategoryRepository())

        assertEquals(utilities.id, categorizer.classify("Airtime", categories))
    }

    @Test
    fun `Fuliza M-PESA resolves to other`() = runTest {
        val categorizer = MerchantCategorizer(FakeMerchantCategoryRepository())

        assertEquals(other.id, categorizer.classify("Fuliza M-PESA", categories))
    }

    @Test
    fun `no keyword match falls back to other`() = runTest {
        val categorizer = MerchantCategorizer(FakeMerchantCategoryRepository())

        assertEquals(other.id, categorizer.classify("JOHN KAMAU", categories))
    }
}
