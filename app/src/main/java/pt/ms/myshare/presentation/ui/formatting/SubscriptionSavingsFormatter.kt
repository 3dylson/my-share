package pt.ms.myshare.presentation.ui.formatting

import pt.ms.myshare.domain.model.StoreProduct
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object SubscriptionSavingsFormatter {

    fun formatAnnualComparison(
        monthlyProduct: StoreProduct?,
        annualProduct: StoreProduct?,
        locale: Locale
    ): AnnualSubscriptionComparison? {
        val monthlyMicros = monthlyProduct?.priceAmountMicros?.takeIf { it > 0L } ?: return null
        val currencyCode = monthlyProduct.priceCurrencyCode?.takeIf { it.isNotBlank() } ?: return null
        val monthlyEquivalentMicros = monthlyMicros * MONTHS_PER_YEAR
        val annualMicros = annualProduct?.priceAmountMicros?.takeIf { it > 0L }
        val annualCurrencyCode = annualProduct?.priceCurrencyCode

        return AnnualSubscriptionComparison(
            monthlyEquivalentPrice = formatMicros(monthlyEquivalentMicros, currencyCode, locale),
            savingsPrice = if (annualMicros != null && annualCurrencyCode == currencyCode) {
                (monthlyEquivalentMicros - annualMicros)
                    .takeIf { it > 0L }
                    ?.let { formatMicros(it, currencyCode, locale) }
            } else {
                null
            }
        )
    }

    private fun formatMicros(amountMicros: Long, currencyCode: String, locale: Locale): String {
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
        }
        val amount = BigDecimal.valueOf(amountMicros).divide(MICROS_PER_UNIT)
        return formatter.format(amount)
    }

    private val MICROS_PER_UNIT = BigDecimal("1000000")
    private const val MONTHS_PER_YEAR = 12
}

data class AnnualSubscriptionComparison(
    val monthlyEquivalentPrice: String,
    val savingsPrice: String?
)
