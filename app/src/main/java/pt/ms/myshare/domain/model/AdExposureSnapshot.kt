package pt.ms.myshare.domain.model

data class AdExposureSnapshot(
    val currentTimeMillis: Long,
    val lastAppOpenShownAtMillis: Long? = null,
    val lastNonBannerShownAtMillis: Long? = null,
    val interstitialImpressionsToday: Int = 0,
    val rewardedImpressionsToday: Int = 0,
    val nativeImpressionsToday: Int = 0,
    val nonBannerImpressionsToday: Int = 0
)
