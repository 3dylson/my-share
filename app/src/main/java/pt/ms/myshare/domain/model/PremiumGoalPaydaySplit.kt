package pt.ms.myshare.domain.model

import java.math.BigDecimal

data class PremiumGoalPaydaySplit(
    val totalMove: BigDecimal,
    val items: List<PremiumGoalPaydaySplitItem>
)

data class PremiumGoalPaydaySplitItem(
    val goalId: String,
    val amount: BigDecimal,
    val sharePercent: BigDecimal
)
