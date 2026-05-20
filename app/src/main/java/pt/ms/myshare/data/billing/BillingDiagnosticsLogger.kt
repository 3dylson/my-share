package pt.ms.myshare.data.billing

import android.os.Bundle
import com.android.billingclient.api.QueryProductDetailsResult
import pt.ms.myshare.domain.model.PremiumSubscriptionProducts
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber

internal object BillingDiagnosticsLogger {

    fun logProductRefresh(
        source: String,
        result: QueryProductDetailsResult,
        products: List<StoreProduct>
    ) {
        val annualProducts = products.filter { it.productId == PremiumSubscriptionProducts.ANNUAL_ID }
        val monthlyProducts = products.filter { it.productId == PremiumSubscriptionProducts.MONTHLY_ID }
        val annualOfferIds = annualProducts.mapNotNull { it.offerId }.distinct()
        val monthlyOfferIds = monthlyProducts.mapNotNull { it.offerId }.distinct()
        val founderReturned = products.any { it.isFounderOffer() }
        val annualTrialReturned = annualProducts.any { it.hasFreeTrial }

        FirebaseUtils.logEvent("billing_products_refreshed", Bundle().apply {
            putString("source", source)
            putInt("product_details_count", result.productDetailsList.size)
            putInt("mapped_count", products.size)
            putInt("unfetched_count", result.unfetchedProductList.size)
            putInt("annual_offer_count", annualProducts.size)
            putInt("monthly_offer_count", monthlyProducts.size)
            putString("founder_offer_returned", founderReturned.toString())
            putString("annual_trial_returned", annualTrialReturned.toString())
            putString("annual_offer_ids", annualOfferIds.toAnalyticsValue())
            putString("monthly_offer_ids", monthlyOfferIds.toAnalyticsValue())
        })
        FirebaseUtils.setCrashlyticsKey("billing_last_refresh_source", source)
        FirebaseUtils.setCrashlyticsKey("billing_products_mapped", products.size)
        FirebaseUtils.setCrashlyticsKey("billing_unfetched_count", result.unfetchedProductList.size)
        FirebaseUtils.setCrashlyticsKey("billing_founder_offer_returned", founderReturned)
        FirebaseUtils.setCrashlyticsKey("billing_annual_trial_returned", annualTrialReturned)
        FirebaseUtils.setCrashlyticsKey("billing_annual_offer_ids", annualOfferIds.toCrashlyticsValue())
        FirebaseUtils.logCrashlyticsBreadcrumb(
            "Billing products refreshed source=$source mapped=${products.size} " +
                "founder=$founderReturned annualTrial=$annualTrialReturned"
        )

        if (!founderReturned) {
            Timber.tag(TAG).w(
                "Founder offer not returned by Play Billing. source=%s annualOffers=%s annualTrialReturned=%s",
                source,
                annualOfferIds.toCrashlyticsValue(),
                annualTrialReturned
            )
        }
    }

    fun logProductRefreshFailure(source: String, responseCode: Int, debugMessage: String) {
        FirebaseUtils.logEvent("billing_products_refresh_failed", Bundle().apply {
            putString("source", source)
            putInt("response_code", responseCode)
            putString("debug_message", debugMessage.take(MAX_ANALYTICS_VALUE_LENGTH))
        })
        FirebaseUtils.setCrashlyticsKey("billing_last_refresh_source", source)
        FirebaseUtils.setCrashlyticsKey("billing_last_refresh_code", responseCode)
        FirebaseUtils.logCrashlyticsBreadcrumb(
            "Billing product refresh failed source=$source code=$responseCode message=${debugMessage.take(MAX_CRASHLYTICS_VALUE_LENGTH)}"
        )
    }

    fun logUnfetchedProducts(source: String, result: QueryProductDetailsResult) {
        result.unfetchedProductList.forEach { product ->
            FirebaseUtils.logEvent("billing_product_unfetched", Bundle().apply {
                putString("source", source)
                putString("product_id", product.productId)
                putString("product_type", product.productType)
                putInt("status_code", product.statusCode)
            })
            Timber.tag(TAG).e(
                "Billing product unfetched source=%s id=%s type=%s code=%d",
                source,
                product.productId,
                product.productType,
                product.statusCode
            )
        }
    }

    fun logFounderOfferUnavailable(products: List<StoreProduct>) {
        val annualProducts = products.filter { it.productId == PremiumSubscriptionProducts.ANNUAL_ID }
        val annualOfferIds = annualProducts.mapNotNull { it.offerId }.distinct()
        FirebaseUtils.logEvent("legacy_founder_offer_missing", Bundle().apply {
            putInt("available_product_count", products.size)
            putInt("annual_offer_count", annualProducts.size)
            putString("annual_trial_returned", annualProducts.any { it.hasFreeTrial }.toString())
            putString("annual_offer_ids", annualOfferIds.toAnalyticsValue())
        })
        FirebaseUtils.setCrashlyticsKey("legacy_founder_offer_missing", true)
        FirebaseUtils.setCrashlyticsKey("legacy_founder_available_count", products.size)
        FirebaseUtils.setCrashlyticsKey("legacy_founder_annual_offer_ids", annualOfferIds.toCrashlyticsValue())
        FirebaseUtils.logCrashlyticsBreadcrumb(
            "Founder offer unavailable available=${products.size} annualOffers=${annualOfferIds.toCrashlyticsValue()}"
        )
    }

    private fun StoreProduct.isFounderOffer(): Boolean {
        return offerId == PremiumSubscriptionProducts.ANNUAL_FOUNDER_OFFER_ID ||
            PremiumSubscriptionProducts.FOUNDER_OFFER_TAG in offerTags
    }

    private fun List<String>.toAnalyticsValue(): String {
        return if (isEmpty()) {
            VALUE_NONE
        } else {
            joinToString(",").take(MAX_ANALYTICS_VALUE_LENGTH)
        }
    }

    private fun List<String>.toCrashlyticsValue(): String {
        return if (isEmpty()) {
            VALUE_NONE
        } else {
            joinToString(",").take(MAX_CRASHLYTICS_VALUE_LENGTH)
        }
    }

    private const val TAG = "BillingDiagnostics"
    private const val VALUE_NONE = "none"
    private const val MAX_ANALYTICS_VALUE_LENGTH = 100
    private const val MAX_CRASHLYTICS_VALUE_LENGTH = 120
}
