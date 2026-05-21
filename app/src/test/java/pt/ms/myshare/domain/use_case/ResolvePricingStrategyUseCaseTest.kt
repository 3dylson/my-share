package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ms.myshare.domain.model.BillingPlan
import java.util.Locale

class ResolvePricingStrategyUseCaseTest {

    private val useCase = ResolvePricingStrategyUseCase()

    @Test
    fun `emerging markets default to monthly hero`() {
        val strategy = useCase.execute(Locale.forLanguageTag("en-NG"))

        assertEquals(BillingPlan.MONTHLY, strategy.heroPlan)
        assertEquals("emerging_monthly_first", strategy.marketCluster)
        assertTrue(strategy.monthlyLabel.contains("1,500") || strategy.monthlyLabel.contains("1500"))
        assertTrue(strategy.annualLabel.contains("12,000") || strategy.annualLabel.contains("12000"))
    }

    @Test
    fun `brazil uses corrected local entry price`() {
        val strategy = useCase.execute(Locale.forLanguageTag("pt-BR"))

        assertEquals(BillingPlan.MONTHLY, strategy.heroPlan)
        assertEquals("brazil_monthly_first", strategy.marketCluster)
        assertTrue(strategy.monthlyLabel.contains("9,90"))
        assertTrue(strategy.annualLabel.contains("69,90"))
    }

    @Test
    fun `portugal uses medium europe fallback price`() {
        val strategy = useCase.execute(Locale.forLanguageTag("pt-PT"))

        assertEquals(BillingPlan.ANNUAL, strategy.heroPlan)
        assertEquals("rest_of_world_annual_first", strategy.marketCluster)
        assertTrue(strategy.monthlyLabel.contains("3,49"))
        assertTrue(strategy.annualLabel.contains("22,99"))
    }

    @Test
    fun `bangladesh uses play console local currency price`() {
        val strategy = useCase.execute(Locale.forLanguageTag("en-BD"))

        assertEquals(BillingPlan.MONTHLY, strategy.heroPlan)
        assertEquals("accessible_monthly_first", strategy.marketCluster)
        assertTrue(strategy.monthlyLabel.contains("279"))
        assertTrue(strategy.annualLabel.contains("1,680") || strategy.annualLabel.contains("1680"))
    }

    @Test
    fun `rest of world defaults to annual hero`() {
        val strategy = useCase.execute(Locale.forLanguageTag("en-US"))

        assertEquals(BillingPlan.ANNUAL, strategy.heroPlan)
        assertEquals("rest_of_world_annual_first", strategy.marketCluster)
    }
}
