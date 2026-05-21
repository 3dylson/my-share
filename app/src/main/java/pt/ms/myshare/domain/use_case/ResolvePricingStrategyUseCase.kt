package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.PricingStrategy
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class ResolvePricingStrategyUseCase @Inject constructor() {

    fun execute(locale: Locale = Locale.getDefault()): PricingStrategy {
        val countryCode = locale.country.uppercase(Locale.US)
        val strategy = marketStrategies[countryCode] ?: defaultStrategy(locale)
        return PricingStrategy(
            marketCluster = strategy.marketCluster,
            monthlyLabel = localizedCurrency(locale, strategy.currencyCode, strategy.monthlyAmount),
            annualLabel = localizedCurrency(locale, strategy.currencyCode, strategy.annualAmount),
            heroPlan = strategy.heroPlan,
            trialDays = 7,
            paywallHeadline = strategy.paywallHeadline,
            paywallSubhead = strategy.paywallSubhead
        )
    }

    private fun localizedCurrency(locale: Locale, currencyCode: String, amount: BigDecimal): String {
        val fractionDigits = amount.scale().coerceAtLeast(0)
        return NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
            minimumFractionDigits = fractionDigits
            maximumFractionDigits = fractionDigits
        }.format(amount)
    }

    private fun defaultStrategy(locale: Locale): MarketPricingStrategy {
        return MarketPricingStrategy(
            currencyCode = runCatching { Currency.getInstance(locale).currencyCode }.getOrDefault("USD"),
            monthlyAmount = BigDecimal("4.99"),
            annualAmount = BigDecimal("29.99"),
            marketCluster = "rest_of_world_annual_first",
            heroPlan = BillingPlan.ANNUAL,
            paywallHeadline = "paywall_headline_default",
            paywallSubhead = "paywall_subhead_default"
        )
    }

    private data class MarketPricingStrategy(
        val currencyCode: String,
        val monthlyAmount: BigDecimal,
        val annualAmount: BigDecimal,
        val marketCluster: String,
        val heroPlan: BillingPlan,
        val paywallHeadline: String,
        val paywallSubhead: String
    )

    private companion object {
        private val emergingMonthlyFirst = "emerging_monthly_first"
        private val accessibleMonthlyFirst = "accessible_monthly_first"
        private val intermediateMonthlyFirst = "intermediate_monthly_first"
        private val annualFirstCluster = "rest_of_world_annual_first"
        private val emergingHeadline = "paywall_headline_emerging"
        private val emergingSubhead = "paywall_subhead_emerging"
        private val defaultHeadline = "paywall_headline_default"
        private val defaultSubhead = "paywall_subhead_default"

        private val marketStrategies = mapOf(
            "NG" to monthlyFirst("NGN", "1500", "12000", emergingMonthlyFirst),
            "PK" to monthlyFirst("PKR", "399", "2999", emergingMonthlyFirst),
            "IN" to monthlyFirst("INR", "149", "999", emergingMonthlyFirst),
            "BR" to MarketPricingStrategy(
                currencyCode = "BRL",
                monthlyAmount = BigDecimal("9.90"),
                annualAmount = BigDecimal("69.90"),
                marketCluster = "brazil_monthly_first",
                heroPlan = BillingPlan.MONTHLY,
                paywallHeadline = "paywall_headline_brazil",
                paywallSubhead = "paywall_subhead_brazil"
            ),
            "ID" to monthlyFirst("IDR", "19000", "149000", accessibleMonthlyFirst),
            "PH" to monthlyFirst("PHP", "99", "699", accessibleMonthlyFirst),
            "EG" to monthlyFirst("EGP", "49.99", "399.99", accessibleMonthlyFirst),
            "VN" to monthlyFirst("VND", "49000", "349000", accessibleMonthlyFirst),
            "TH" to monthlyFirst("THB", "79", "599", accessibleMonthlyFirst),
            "BD" to monthlyFirst("BDT", "279", "1680", accessibleMonthlyFirst),
            "MX" to monthlyFirst("MXN", "59", "399", intermediateMonthlyFirst),
            "TR" to monthlyFirst("TRY", "49.99", "349.99", intermediateMonthlyFirst),
            "ZA" to monthlyFirst("ZAR", "59.99", "399.99", intermediateMonthlyFirst),
            "CO" to monthlyFirst("COP", "9900", "69900", intermediateMonthlyFirst),
            "PT" to annualFirst("EUR", "3.49", "22.99"),
            "DE" to annualFirst("EUR", "4.99", "29.99"),
            "GB" to annualFirst("GBP", "4.49", "29.99"),
            "US" to annualFirst("USD", "4.99", "29.99")
        )

        private fun monthlyFirst(
            currencyCode: String,
            monthlyAmount: String,
            annualAmount: String,
            marketCluster: String
        ) = MarketPricingStrategy(
            currencyCode = currencyCode,
            monthlyAmount = BigDecimal(monthlyAmount),
            annualAmount = BigDecimal(annualAmount),
            marketCluster = marketCluster,
            heroPlan = BillingPlan.MONTHLY,
            paywallHeadline = emergingHeadline,
            paywallSubhead = emergingSubhead
        )

        private fun annualFirst(
            currencyCode: String,
            monthlyAmount: String,
            annualAmount: String
        ) = MarketPricingStrategy(
            currencyCode = currencyCode,
            monthlyAmount = BigDecimal(monthlyAmount),
            annualAmount = BigDecimal(annualAmount),
            marketCluster = annualFirstCluster,
            heroPlan = BillingPlan.ANNUAL,
            paywallHeadline = defaultHeadline,
            paywallSubhead = defaultSubhead
        )
    }
}
