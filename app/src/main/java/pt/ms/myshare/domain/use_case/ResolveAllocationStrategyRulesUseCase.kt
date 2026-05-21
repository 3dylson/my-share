package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.AllocationStrategy
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.model.PlanningFocus
import java.math.BigDecimal
import javax.inject.Inject

class ResolveAllocationStrategyRulesUseCase @Inject constructor() {

    fun execute(
        focus: PlanningFocus,
        preset: AllocationPreset,
        strategy: AllocationStrategy
    ): List<PaydayRule> {
        val weights = adjustedWeights(focus, preset, strategy)
        return buildList {
            addRuleIfPositive("Savings", weights.savings, PaydayRuleType.SAVINGS)
            addRuleIfPositive("Investing", weights.investing, PaydayRuleType.INVESTING)
            addRuleIfPositive("Crypto", weights.crypto, PaydayRuleType.CRYPTO)
            addRuleIfPositive("Debt", weights.debt, PaydayRuleType.DEBT)
        }
    }

    private fun MutableList<PaydayRule>.addRuleIfPositive(
        name: String,
        weight: BigDecimal,
        type: PaydayRuleType
    ) {
        if (weight > BigDecimal.ZERO) {
            add(
                PaydayRule(
                    name = name,
                    amount = weight.multiply(BigDecimal("100")),
                    type = type,
                    isPercentage = true
                )
            )
        }
    }

    private fun adjustedWeights(
        focus: PlanningFocus,
        preset: AllocationPreset,
        strategy: AllocationStrategy
    ): AllocationStrategyWeights {
        val base = when (strategy) {
            AllocationStrategy.BALANCED_SAVINGS -> focus.defaultSavingsWeights()
            AllocationStrategy.NO_SAVINGS_NOW -> AllocationStrategyWeights(
                flexibleSpend = bd("0.70"),
                savings = bd("0.00"),
                investing = bd("0.20"),
                crypto = bd("0.00"),
                debt = bd("0.10")
            )
            AllocationStrategy.DEBT_FIRST -> AllocationStrategyWeights(
                flexibleSpend = bd("0.55"),
                savings = bd("0.00"),
                investing = bd("0.05"),
                crypto = bd("0.00"),
                debt = bd("0.40")
            )
            AllocationStrategy.INVESTING_FIRST -> AllocationStrategyWeights(
                flexibleSpend = bd("0.45"),
                savings = bd("0.00"),
                investing = bd("0.45"),
                crypto = bd("0.10"),
                debt = bd("0.00")
            )
            AllocationStrategy.FLEXIBLE_BUDGET_ONLY -> AllocationStrategyWeights(
                flexibleSpend = bd("1.00"),
                savings = bd("0.00"),
                investing = bd("0.00"),
                crypto = bd("0.00"),
                debt = bd("0.00")
            )
            AllocationStrategy.CUSTOM -> AllocationStrategyWeights(
                flexibleSpend = bd("1.00"),
                savings = bd("0.00"),
                investing = bd("0.00"),
                crypto = bd("0.00"),
                debt = bd("0.00")
            )
        }
        if (strategy == AllocationStrategy.FLEXIBLE_BUDGET_ONLY || strategy == AllocationStrategy.CUSTOM) return base

        return when (preset) {
            AllocationPreset.CONSERVATIVE -> base.copy(
                flexibleSpend = base.flexibleSpend.add(bd("0.05")).coerceAtMost(ONE),
                savings = base.savings.add(if (base.savings > BigDecimal.ZERO) bd("0.05") else BigDecimal.ZERO),
                investing = base.investing.subtract(bd("0.05")).max(BigDecimal.ZERO),
                crypto = base.crypto.subtract(bd("0.05")).max(BigDecimal.ZERO)
            ).normalized()
            AllocationPreset.BALANCED -> base
            AllocationPreset.GROWTH -> base.copy(
                flexibleSpend = base.flexibleSpend.subtract(bd("0.05")).max(BigDecimal.ZERO),
                savings = base.savings.subtract(bd("0.05")).max(BigDecimal.ZERO),
                investing = base.investing.add(bd("0.07")),
                crypto = base.crypto.add(if (base.crypto > BigDecimal.ZERO) bd("0.03") else BigDecimal.ZERO)
            ).normalized()
        }
    }

    private fun PlanningFocus.defaultSavingsWeights(): AllocationStrategyWeights = when (this) {
        PlanningFocus.SAVE_WITHOUT_STRESS -> AllocationStrategyWeights(
            flexibleSpend = bd("0.45"),
            savings = bd("0.35"),
            investing = bd("0.15"),
            crypto = bd("0.00"),
            debt = bd("0.00")
        )
        PlanningFocus.INVEST_WITH_DISCIPLINE -> AllocationStrategyWeights(
            flexibleSpend = bd("0.35"),
            savings = bd("0.20"),
            investing = bd("0.30"),
            crypto = bd("0.10"),
            debt = bd("0.00")
        )
        PlanningFocus.STOP_OVERSPENDING -> AllocationStrategyWeights(
            flexibleSpend = bd("0.40"),
            savings = bd("0.35"),
            investing = bd("0.10"),
            crypto = bd("0.00"),
            debt = bd("0.00")
        )
        PlanningFocus.PLAN_TOGETHER -> AllocationStrategyWeights(
            flexibleSpend = bd("0.50"),
            savings = bd("0.25"),
            investing = bd("0.10"),
            crypto = bd("0.00"),
            debt = bd("0.00")
        )
    }

    private fun AllocationStrategyWeights.normalized(): AllocationStrategyWeights {
        val total = flexibleSpend.add(savings).add(investing).add(crypto).add(debt)
        if (total <= ONE) return this
        val excess = total.subtract(ONE)
        return copy(flexibleSpend = flexibleSpend.subtract(excess).max(BigDecimal.ZERO))
    }

    private data class AllocationStrategyWeights(
        val flexibleSpend: BigDecimal,
        val savings: BigDecimal,
        val investing: BigDecimal,
        val crypto: BigDecimal,
        val debt: BigDecimal
    )

    private companion object {
        val ONE: BigDecimal = BigDecimal.ONE
        fun bd(value: String): BigDecimal = BigDecimal(value)
    }
}
