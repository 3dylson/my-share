package pt.ms.myshare.data.billing

import com.android.billingclient.api.ProductDetails
import pt.ms.myshare.domain.model.StoreProduct

internal object BillingProductMapper {

    fun map(details: ProductDetails): StoreProduct? {
        val selectedOffer = details.subscriptionOfferDetails
            .orEmpty()
            .sortedWith(compareByDescending<ProductDetails.SubscriptionOfferDetails> { it.hasFreeTrial() })
            .firstOrNull { it.pricingPhases.pricingPhaseList.isNotEmpty() }
            ?: return null

        val phases = selectedOffer.pricingPhases.pricingPhaseList
        val trialPhase = phases.firstOrNull { it.isFreeTrial() }
        val recurringPhase = phases.lastOrNull {
            it.priceAmountMicros > 0L &&
                it.recurrenceMode == ProductDetails.RecurrenceMode.INFINITE_RECURRING
        } ?: phases.lastOrNull { it.priceAmountMicros > 0L }

        val offerToken = selectedOffer.offerToken
        val price = recurringPhase?.formattedPrice
        if (offerToken.isBlank() || price.isNullOrBlank()) return null

        return StoreProduct(
            productId = details.productId,
            name = details.name,
            description = details.description,
            price = price,
            basePlanId = selectedOffer.basePlanId,
            offerToken = offerToken,
            recurringBillingPeriod = recurringPhase.billingPeriod,
            offerId = selectedOffer.offerId,
            offerTags = selectedOffer.offerTags.orEmpty(),
            freeTrialPeriod = trialPhase?.billingPeriod,
            freeTrialDays = trialPhase?.let {
                BillingPeriodParser.totalDays(it.billingPeriod, it.billingCycleCount)
            }
        )
    }

    private fun ProductDetails.SubscriptionOfferDetails.hasFreeTrial(): Boolean {
        return pricingPhases.pricingPhaseList.any { it.isFreeTrial() }
    }

    private fun ProductDetails.PricingPhase.isFreeTrial(): Boolean {
        return priceAmountMicros == 0L &&
            billingPeriod.isNotBlank() &&
            recurrenceMode != ProductDetails.RecurrenceMode.INFINITE_RECURRING
    }
}
