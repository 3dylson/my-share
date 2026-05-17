package pt.ms.myshare.presentation.ui.formatting

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Responsibility: Converts allocation inputs between fixed currency amounts and percentage rates.
 */
object AllocationAmountConverter {

    fun fixedAmountToPercentage(amount: BigDecimal, availableAmount: BigDecimal): BigDecimal? {
        if (availableAmount <= BigDecimal.ZERO) return null
        return amount
            .divide(availableAmount, CONVERSION_SCALE, RoundingMode.HALF_UP)
            .multiply(ONE_HUNDRED)
            .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)
    }

    fun percentageToFixedAmount(percentage: BigDecimal, availableAmount: BigDecimal): BigDecimal? {
        if (availableAmount <= BigDecimal.ZERO) return null
        return availableAmount
            .multiply(percentage.divide(ONE_HUNDRED, CONVERSION_SCALE, RoundingMode.HALF_UP))
            .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)
    }

    private val ONE_HUNDRED = BigDecimal("100")
    private const val CONVERSION_SCALE = 6
    private const val DISPLAY_SCALE = 2
}
