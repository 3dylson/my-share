package pt.ms.myshare.presentation

import android.app.Activity
import android.app.Application
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import pt.ms.myshare.R
import timber.log.Timber

class AppOpenAdManager(private val application: Application) {
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime: Long = 0

    companion object {
        private const val AD_EXPIRATION_HOURS = 4
    }

    fun showAdIfAvailable(activity: Activity) {
        if (isShowingAd) return

        if (isAdAvailable()) {
            appOpenAd?.show(activity)
            isShowingAd = true
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    isShowingAd = false
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }
        } else {
            loadAd(activity)
        }
    }

    fun preload(context: Context) {
        loadAd(context)
    }

    private fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable()) return

        val adUnitId = context.getString(R.string.admob_app_open_ad_unit_id)
        if (adUnitId.isBlank()) {
            Timber.tag("AdMob").d("App Open Ad disabled because no ad unit is configured")
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = System.currentTimeMillis()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    Timber.tag("AdMob").d("App Open Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && (System.currentTimeMillis() - loadTime) < (AD_EXPIRATION_HOURS * 3600000)
    }
}
