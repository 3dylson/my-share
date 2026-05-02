package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.PricingStrategy
import java.util.Locale
import javax.inject.Inject

class ResolvePricingStrategyUseCase @Inject constructor() {

    fun execute(locale: Locale = Locale.getDefault()): PricingStrategy {
        return when (locale.country.uppercase(Locale.US)) {
            "NG", "PK", "IN" -> PricingStrategy(
                marketCluster = "emerging_monthly_first",
                monthlyLabel = localizedCurrency(locale, monthlyMinor = 499),
                annualLabel = localizedCurrency(locale, annualMinor = 3999),
                heroPlan = BillingPlan.MONTHLY,
                trialDays = 7,
                paywallHeadline = "paywall_headline_emerging",
                paywallSubhead = "paywall_subhead_emerging"
            )
            "BR" -> PricingStrategy(
                marketCluster = "brazil_monthly_first",
                monthlyLabel = localizedCurrency(locale, monthlyMinor = 1199),
                annualLabel = localizedCurrency(locale, annualMinor = 8999),
                heroPlan = BillingPlan.MONTHLY,
                trialDays = 7,
                paywallHeadline = "paywall_headline_brazil",
                paywallSubhead = "paywall_subhead_brazil"
            )
            else -> PricingStrategy(
                marketCluster = "rest_of_world_annual_first",
                monthlyLabel = localizedCurrency(locale, monthlyMinor = 599),
                annualLabel = localizedCurrency(locale, annualMinor = 4999),
                heroPlan = BillingPlan.ANNUAL,
                trialDays = 7,
                paywallHeadline = "paywall_headline_default",
                paywallSubhead = "paywall_subhead_default"
            )
        }
    }

    private fun localizedCurrency(locale: Locale, monthlyMinor: Int? = null, annualMinor: Int? = null): String {
        val format = java.text.NumberFormat.getCurrencyInstance(locale)
        return when {
            monthlyMinor != null -> format.format(monthlyMinor / 100.0)
            annualMinor != null -> format.format(annualMinor / 100.0)
            else -> error("Either monthlyMinor or annualMinor must be provided")
        }
    }
}
