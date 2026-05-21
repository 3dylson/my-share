package pt.ms.myshare.presentation.ui.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles the Google User Messaging Platform (UMP) consent flow for AdMob and privacy compliance.
 */
class AdsConsentManager(private val activity: Activity) {

    private val consentInformation: ConsentInformation by lazy {
        Timber.tag(TAG).d("Loading ads consent information")
        UserMessagingPlatform.getConsentInformation(activity)
    }
    private val isMobileAdsSdkInitialized = AtomicBoolean(false)

    /**
     * Interface to listen for consent gathered events.
     */
    interface OnConsentGatheringFinishedListener {
        fun onConsentGatheringFinished(error: String?)
    }

    /**
     * Helper variable for checking if the user has consented to ads.
     */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /**
     * Resets the consent state (useful for testing).
     */
    fun reset() {
        consentInformation.reset()
    }

    /**
     * Starts the consent gathering process. 
     * This should be called early in the app lifecycle (e.g., in onCreate of MainActivity).
     */
    fun gatherConsent(
        onConsentGatheringFinishedListener: OnConsentGatheringFinishedListener
    ) {
        // For testing purposes, you can enable debug settings:
        /*
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("YOUR_TEST_DEVICE_ID")
            .build()
        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .build()
        */
        
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    // Consent gathering process is complete.
                    onConsentGatheringFinishedListener.onConsentGatheringFinished(formError?.message)
                }
            },
            { requestConsentError ->
                onConsentGatheringFinishedListener.onConsentGatheringFinished(requestConsentError.message)
            }
        )
    }

    /**
     * Shows the privacy options form (usually required for GDPR compliance in settings).
     */
    fun showPrivacyOptionsForm(onConsentFormDismissedListener: ConsentForm.OnConsentFormDismissedListener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
    }

    /**
     * Helper to check if privacy options are required/available.
     */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    private companion object {
        const val TAG = "AdsConsent"
    }
}
