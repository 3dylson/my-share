package pt.ms.myshare.domain.model

import java.math.BigDecimal

data class PremiumRulePaydayMix(
    val totalMove: BigDecimal,
    val items: List<PremiumRulePaydayMixItem>
)

data class PremiumRulePaydayMixItem(
    val ruleId: String,
    val amount: BigDecimal,
    val sharePercent: BigDecimal
)
