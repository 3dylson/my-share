package pt.ms.myshare.presentation.ui.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import pt.ms.myshare.R
import timber.log.Timber

object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var hasShownThisSession = false

    fun loadAd(context: Context) {
        if (interstitialAd != null || isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        val adUnitId = context.getString(R.string.admob_interstitial_ad_unit_id)
        if (adUnitId.isBlank()) {
            Timber.tag("InterstitialAd").d("Interstitial Ad disabled because no ad unit is configured")
            isLoading = false
            return
        }

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Timber.tag("InterstitialAd").w("Failed to load ad: %s", adError.message)
                    interstitialAd = null
                    isLoading = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Timber.tag("InterstitialAd").d("Ad loaded")
                    interstitialAd = ad
                    isLoading = false
                }
            }
        )
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (hasShownThisSession) {
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            hasShownThisSession = true
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Timber.tag("InterstitialAd").d("Ad dismissed")
                    interstitialAd = null
                    onAdDismissed()
                    // Pre-load the next ad
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Timber.tag("InterstitialAd").w("Ad failed to show: %s", adError.message)
                    interstitialAd = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Timber.tag("InterstitialAd").d("Ad showed")
                }
            }
            ad.show(activity)
        } else {
            Timber.tag("InterstitialAd").w("Ad is not ready to be shown")
            onAdDismissed()
        }
    }
}
