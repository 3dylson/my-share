package pt.ms.myshare.domain.model

enum class AdFormat(val analyticsName: String) {
    BANNER("banner"),
    NATIVE("native"),
    APP_OPEN("app_open"),
    INTERSTITIAL("interstitial"),
    REWARDED("rewarded")
}

enum class AdPlacement(
    val analyticsName: String,
    val format: AdFormat
) {
    HOME_ANCHORED_BANNER("home_anchored_banner", AdFormat.BANNER),
    MORE_ANCHORED_BANNER("more_anchored_banner", AdFormat.BANNER),
    MORE_NATIVE_CARD("more_native_card", AdFormat.NATIVE),
    APP_OPEN("app_open", AdFormat.APP_OPEN),
    POST_COMPLETED_ACTION_INTERSTITIAL("post_completed_action_interstitial", AdFormat.INTERSTITIAL),
    REWARDED_EXTRA_GOAL("rewarded_extra_goal", AdFormat.REWARDED)
}
