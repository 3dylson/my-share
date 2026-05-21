package pt.ms.myshare.presentation.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrategyCollectionLayoutPolicyTest {

    @Test
    fun `free users see one inline item`() {
        assertEquals(1, StrategyCollectionLayoutPolicy.visibleCount(totalCount = 5, isPremium = false))
        assertEquals(4, StrategyCollectionLayoutPolicy.hiddenCount(totalCount = 5, isPremium = false))
        assertFalse(StrategyCollectionLayoutPolicy.shouldShowPremiumArchive(totalCount = 5, isPremium = false))
    }

    @Test
    fun `premium users see first three items inline and archive the rest`() {
        assertEquals(3, StrategyCollectionLayoutPolicy.visibleCount(totalCount = 6, isPremium = true))
        assertEquals(3, StrategyCollectionLayoutPolicy.hiddenCount(totalCount = 6, isPremium = true))
        assertTrue(StrategyCollectionLayoutPolicy.shouldShowPremiumArchive(totalCount = 6, isPremium = true))
    }

    @Test
    fun `premium users with small collections do not see archive prompt`() {
        assertEquals(2, StrategyCollectionLayoutPolicy.visibleCount(totalCount = 2, isPremium = true))
        assertEquals(0, StrategyCollectionLayoutPolicy.hiddenCount(totalCount = 2, isPremium = true))
        assertFalse(StrategyCollectionLayoutPolicy.shouldShowPremiumArchive(totalCount = 2, isPremium = true))
    }
}
