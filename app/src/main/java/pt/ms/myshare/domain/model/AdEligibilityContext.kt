package pt.ms.myshare.domain.model

data class AdEligibilityContext(
    val placement: AdPlacement,
    val isPremium: Boolean,
    val hasFirstPlan: Boolean,
    val canRequestAds: Boolean,
    val sessionCount: Int,
    val rolloutBucket: Int,
    val isKeyboardVisible: Boolean = false,
    val isNotificationLaunch: Boolean = false,
    val isBlockedFlowActive: Boolean = false,
    val isCompletedAction: Boolean = false,
    val hasExplicitRewardOptIn: Boolean = false
)
