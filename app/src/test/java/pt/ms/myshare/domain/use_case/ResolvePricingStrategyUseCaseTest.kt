package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.ms.myshare.domain.model.BillingPlan
import java.util.Locale

class ResolvePricingStrategyUseCaseTest {

    private val useCase = ResolvePricingStrategyUseCase()

    @Test
    fun `emerging markets default to monthly hero`() {
        val strategy = useCase.execute(Locale("en", "NG"))

        assertEquals(BillingPlan.MONTHLY, strategy.heroPlan)
        assertEquals("emerging_monthly_first", strategy.marketCluster)
    }

    @Test
    fun `rest of world defaults to annual hero`() {
        val strategy = useCase.execute(Locale("en", "US"))

        assertEquals(BillingPlan.ANNUAL, strategy.heroPlan)
        assertEquals("rest_of_world_annual_first", strategy.marketCluster)
    }
}
