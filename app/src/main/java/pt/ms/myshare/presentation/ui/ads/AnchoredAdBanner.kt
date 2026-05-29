package pt.ms.myshare.presentation.ui.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import pt.ms.myshare.domain.model.AdPlacement

@Composable
fun AnchoredAdBanner(
    placement: AdPlacement,
    isPremium: Boolean,
    hasFirstPlan: Boolean,
    isKeyboardVisible: Boolean,
    isBlockedFlowActive: Boolean,
    modifier: Modifier = Modifier
) {
    val orchestrator = LocalAdsOrchestrator.current ?: return
    val adsStateVersion by orchestrator.adsStateVersion.collectAsState()
    val canShow = remember(
        placement,
        isPremium,
        hasFirstPlan,
        isKeyboardVisible,
        isBlockedFlowActive,
        adsStateVersion
    ) {
        orchestrator.canShowPlacement(
            placement = placement,
            isPremium = isPremium,
            hasFirstPlan = hasFirstPlan,
            isKeyboardVisible = isKeyboardVisible,
            isBlockedFlowActive = isBlockedFlowActive
        )
    }
    if (!canShow) {
        return
    }

    val context = LocalContext.current
    val adUnitId = orchestrator.adUnitIdFor(placement)
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.coerceAtLeast(MIN_AD_WIDTH_DP)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
            .padding(top = 8.dp, bottom = 6.dp)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MIN_BANNER_HEIGHT_DP.dp),
            factory = {
                AdView(context).apply {
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            context,
                            screenWidthDp
                        )
                    )
                    setAdUnitId(adUnitId)
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            orchestrator.recordAdLoaded(placement, isPremium)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            orchestrator.recordAdLoadFailed(placement, error.message, isPremium)
                        }

                        override fun onAdImpression() {
                            orchestrator.recordAdImpression(placement, isPremium)
                        }

                        override fun onAdClicked() {
                            orchestrator.recordAdClicked(placement, isPremium)
                        }
                    }
                    orchestrator.recordAdRequestStarted(placement, isPremium)
                    loadAd(orchestrator.createAdRequest())
                }
            }
        )
    }
}

private const val MIN_AD_WIDTH_DP = 320
private const val MIN_BANNER_HEIGHT_DP = 50
