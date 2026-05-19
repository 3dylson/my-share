package pt.ms.myshare.presentation.ui.home

object StrategyCollectionLayoutPolicy {
    const val PREMIUM_INLINE_LIMIT = 3
    private const val FREE_INLINE_LIMIT = 1

    fun visibleCount(totalCount: Int, isPremium: Boolean): Int {
        val limit = if (isPremium) PREMIUM_INLINE_LIMIT else FREE_INLINE_LIMIT
        return totalCount.coerceAtLeast(0).coerceAtMost(limit)
    }

    fun hiddenCount(totalCount: Int, isPremium: Boolean): Int {
        return (totalCount.coerceAtLeast(0) - visibleCount(totalCount, isPremium)).coerceAtLeast(0)
    }

    fun shouldShowPremiumArchive(totalCount: Int, isPremium: Boolean): Boolean {
        return isPremium && hiddenCount(totalCount, isPremium) > 0
    }
}
