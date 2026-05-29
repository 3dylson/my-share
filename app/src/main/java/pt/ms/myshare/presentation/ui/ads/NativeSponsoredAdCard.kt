package pt.ms.myshare.presentation.ui.ads

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.AdPlacement

@Composable
fun NativeSponsoredAdCard(
    isPremium: Boolean,
    hasFirstPlan: Boolean,
    modifier: Modifier = Modifier
) {
    val orchestrator = LocalAdsOrchestrator.current ?: return
    val placement = AdPlacement.MORE_NATIVE_CARD
    val adsStateVersion by orchestrator.adsStateVersion.collectAsState()
    val canShow = remember(placement, isPremium, hasFirstPlan, adsStateVersion) {
        orchestrator.canShowPlacement(
            placement = placement,
            isPremium = isPremium,
            hasFirstPlan = hasFirstPlan
        )
    }
    if (!canShow) {
        return
    }

    val context = LocalContext.current
    val sponsoredLabel = stringResource(R.string.advertisement_label)
    val adUnitId = orchestrator.adUnitIdFor(placement)
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f).toArgb()
    val headlineColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val bodyColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val labelColor = MaterialTheme.colorScheme.primary.toArgb()
    val buttonColor = MaterialTheme.colorScheme.primary.toArgb()
    val buttonTextColor = MaterialTheme.colorScheme.onPrimary.toArgb()
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    DisposableEffect(adUnitId) {
        if (!isLoading && nativeAd == null) {
            isLoading = true
            orchestrator.recordAdRequestStarted(placement, isPremium)
            AdLoader.Builder(context, adUnitId)
                .forNativeAd { loadedAd ->
                    nativeAd?.destroy()
                    nativeAd = loadedAd
                    isLoading = false
                    orchestrator.recordAdLoaded(placement, isPremium)
                }
                .withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isLoading = false
                            orchestrator.recordAdLoadFailed(placement, error.message, isPremium)
                        }

                        override fun onAdImpression() {
                            orchestrator.recordAdImpression(placement, isPremium)
                        }

                        override fun onAdClicked() {
                            orchestrator.recordAdClicked(placement, isPremium)
                        }
                    }
                )
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()
                .loadAd(orchestrator.createAdRequest())
        }
        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    val ad = nativeAd ?: return
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(NATIVE_AD_CARD_HEIGHT_DP.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        )
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            factory = {
                createNativeAdView(
                    context = context,
                    sponsoredLabel = sponsoredLabel,
                    surfaceColor = surfaceColor,
                    borderColor = borderColor,
                    headlineColor = headlineColor,
                    bodyColor = bodyColor,
                    labelColor = labelColor,
                    buttonColor = buttonColor,
                    buttonTextColor = buttonTextColor
                )
            },
            update = { nativeAdView ->
                bindNativeAd(nativeAdView, ad)
            }
        )
    }
}

private fun createNativeAdView(
    context: Context,
    sponsoredLabel: String,
    surfaceColor: Int,
    borderColor: Int,
    headlineColor: Int,
    bodyColor: Int,
    labelColor: Int,
    buttonColor: Int,
    buttonTextColor: Int
): NativeAdView {
    val view = NativeAdView(context).apply {
        setBackgroundColor(surfaceColor)
    }
    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, 0)
        dividerPadding = dp(context, 8)
    }
    val label = TextView(context).apply {
        text = sponsoredLabel.uppercase()
        setTextColor(labelColor)
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        letterSpacing = 0.08f
    }
    val headline = TextView(context).apply {
        id = View.generateViewId()
        setTextColor(headlineColor)
        textSize = 18f
        typeface = Typeface.DEFAULT_BOLD
        maxLines = 2
    }
    val body = TextView(context).apply {
        id = View.generateViewId()
        setTextColor(bodyColor)
        textSize = 14f
        maxLines = 3
    }
    val cta = Button(context).apply {
        id = View.generateViewId()
        setTextColor(buttonTextColor)
        setBackgroundColor(buttonColor)
        gravity = Gravity.CENTER
        minHeight = dp(context, 44)
    }

    container.addView(label)
    container.addView(headline, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).withTopMargin(context, 8))
    container.addView(body, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).withTopMargin(context, 6))
    container.addView(cta, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).withTopMargin(context, 12))
    view.addView(container)
    view.headlineView = headline
    view.bodyView = body
    view.callToActionView = cta
    view.setBackgroundColor(surfaceColor)
    view.foreground = android.graphics.drawable.ColorDrawable(borderColor).apply { alpha = 0 }
    return view
}

private fun bindNativeAd(view: NativeAdView, nativeAd: NativeAd) {
    (view.headlineView as? TextView)?.text = nativeAd.headline
    val bodyView = view.bodyView as? TextView
    val body = nativeAd.body
    bodyView?.text = body.orEmpty()
    bodyView?.visibility = if (body.isNullOrBlank()) View.GONE else View.VISIBLE
    val ctaView = view.callToActionView as? Button
    val callToAction = nativeAd.callToAction
    ctaView?.text = callToAction.orEmpty()
    ctaView?.visibility = if (callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
    view.setNativeAd(nativeAd)
}

private fun LinearLayout.LayoutParams.withTopMargin(context: Context, marginDp: Int): LinearLayout.LayoutParams {
    topMargin = dp(context, marginDp)
    return this
}

private fun dp(context: Context, value: Int): Int {
    return (value * context.resources.displayMetrics.density).toInt()
}

private const val NATIVE_AD_CARD_HEIGHT_DP = 176
