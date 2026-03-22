package pt.ms.myshare.presentation.ui.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import pt.ms.myshare.R
import pt.ms.myshare.utils.ads.ConsentManager
import pt.ms.myshare.utils.logs.FirebaseUtils

@Composable
fun SafeAdBanner(
    modifier: Modifier = Modifier,
    isPremium: Boolean
) {
    // 1. Strict Guardrail: Premium users NEVER execute ad impressions
    if (isPremium) {
        FirebaseUtils.logEvent("ad_suppressed", android.os.Bundle().apply {
            putString("reason", "premium_user")
        })
        return
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            // 2. Strict Guardrail: Validate Consent before rendering
            val consentManager = ConsentManager(context)
            if (!consentManager.canRequestAds()) {
                 FirebaseUtils.logEvent("ad_suppressed", android.os.Bundle().apply {
                     putString("reason", "no_consent")
                 })
                 return@AndroidView android.view.View(context) // Return empty view
            }

            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = context.getString(R.string.admob_banner_ad_unit_id)
                adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdImpression() {
                        super.onAdImpression()
                        FirebaseUtils.logEvent("ad_impression", android.os.Bundle().apply {
                            putString("surface", "more_tab")
                        })
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
