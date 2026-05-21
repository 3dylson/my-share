package pt.ms.myshare.data.billing

import com.android.billingclient.api.ProductDetails
import pt.ms.myshare.domain.model.PremiumSubscriptionProducts
import pt.ms.myshare.domain.model.StoreProduct
import timber.log.Timber

internal object BillingProductMapper {

    fun map(details: ProductDetails): List<StoreProduct> {
        val expectedBillingPeriod = PremiumSubscriptionProducts.expectedBillingPeriod(details.productId)
        return details.subscriptionOfferDetails
            .orEmpty()
            .sortedWith(
                compareByDescending<ProductDetails.SubscriptionOfferDetails> {
                    it.matchesBillingPeriod(expectedBillingPeriod)
                }.thenByDescending { it.hasFreeTrial() }
            )
            .mapNotNull { offer -> offer.toStoreProduct(details) }
    }

    private fun ProductDetails.SubscriptionOfferDetails.toStoreProduct(details: ProductDetails): StoreProduct? {
        val phases = pricingPhases.pricingPhaseList
        val trialPhase = phases.firstOrNull { it.isFreeTrial() }
        val recurringPhase = phases.lastOrNull {
            it.priceAmountMicros > 0L &&
                it.recurrenceMode == ProductDetails.RecurrenceMode.INFINITE_RECURRING
        } ?: phases.lastOrNull { it.priceAmountMicros > 0L }

        val offerToken = this.offerToken
        val price = recurringPhase?.formattedPrice
        Timber.tag(TAG).d(
            "Billing offer returned product=%s basePlan=%s offerId=%s tags=%s phases=%s hasToken=%s",
            details.productId,
            basePlanId,
            offerId,
            offerTags.orEmpty().joinToString(","),
            phases.joinToString("|") { phase ->
                "${phase.formattedPrice}:${phase.billingPeriod}:${phase.recurrenceMode}:${phase.billingCycleCount}"
            },
            offerToken.isNotBlank()
        )
        if (offerToken.isBlank() || price.isNullOrBlank()) {
            Timber.tag(TAG).e(
                "Billing offer skipped product=%s basePlan=%s offerId=%s missingToken=%s missingRecurringPrice=%s",
                details.productId,
                basePlanId,
                offerId,
                offerToken.isBlank(),
                price.isNullOrBlank()
            )
            return null
        }

        return StoreProduct(
            productId = details.productId,
            name = details.name,
            description = details.description,
            price = price,
            basePlanId = basePlanId,
            offerToken = offerToken,
            priceAmountMicros = recurringPhase.priceAmountMicros,
            priceCurrencyCode = recurringPhase.priceCurrencyCode,
            recurringBillingPeriod = recurringPhase.billingPeriod,
            offerId = offerId,
            offerTags = offerTags.orEmpty(),
            freeTrialPeriod = trialPhase?.billingPeriod,
            freeTrialDays = trialPhase?.let {
                BillingPeriodParser.totalDays(it.billingPeriod, it.billingCycleCount)
            }
        )
    }

    private fun ProductDetails.SubscriptionOfferDetails.hasFreeTrial(): Boolean {
        return pricingPhases.pricingPhaseList.any { it.isFreeTrial() }
    }

    private fun ProductDetails.SubscriptionOfferDetails.matchesBillingPeriod(expectedBillingPeriod: String?): Boolean {
        if (expectedBillingPeriod == null) return true
        return pricingPhases.pricingPhaseList.any {
            it.billingPeriod == expectedBillingPeriod &&
                it.priceAmountMicros > 0L &&
                it.recurrenceMode == ProductDetails.RecurrenceMode.INFINITE_RECURRING
        }
    }

    private fun ProductDetails.PricingPhase.isFreeTrial(): Boolean {
        return priceAmountMicros == 0L &&
            billingPeriod.isNotBlank() &&
            recurrenceMode != ProductDetails.RecurrenceMode.INFINITE_RECURRING
    }

    private const val TAG = "BillingProductMapper"
}
