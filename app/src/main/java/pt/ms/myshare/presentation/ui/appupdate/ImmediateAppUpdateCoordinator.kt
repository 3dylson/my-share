package pt.ms.myshare.presentation.ui.appupdate

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import timber.log.Timber

class ImmediateAppUpdateCoordinator(
    activity: Activity,
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
) {

    fun requestImmediateUpdate(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        onFlowStarted: () -> Unit,
        onNoImmediateUpdateAvailable: () -> Unit
    ) {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                logAppUpdateInfo(appUpdateInfo)
                if (appUpdateInfo.canStartImmediateUpdate()) {
                    startImmediateUpdate(appUpdateInfo, launcher, onFlowStarted, onNoImmediateUpdateAvailable)
                } else {
                    Timber.d("Immediate app update is not available")
                    onNoImmediateUpdateAvailable()
                }
            }
            .addOnFailureListener { error ->
                Timber.e(error, "Failed to read Play app update info")
                onNoImmediateUpdateAvailable()
            }
    }

    fun resumeImmediateUpdateIfInProgress(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        onFlowStarted: () -> Unit,
        onNoUpdateInProgress: () -> Unit
    ) {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                logAppUpdateInfo(appUpdateInfo)
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    startImmediateUpdate(appUpdateInfo, launcher, onFlowStarted, onNoUpdateInProgress)
                } else {
                    Timber.d("No immediate app update is in progress")
                    onNoUpdateInProgress()
                }
            }
            .addOnFailureListener { error ->
                Timber.e(error, "Failed to resume immediate app update")
                onNoUpdateInProgress()
            }
    }

    private fun startImmediateUpdate(
        appUpdateInfo: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        onFlowStarted: () -> Unit,
        onFlowUnavailable: () -> Unit
    ) {
        try {
            val started = appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                launcher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
            Timber.d("Immediate app update flow start requested. started=%s", started)
            if (started) {
                onFlowStarted()
            } else {
                onFlowUnavailable()
            }
        } catch (error: Exception) {
            Timber.e(error, "Immediate app update flow failed to start")
            onFlowUnavailable()
        }
    }

    private fun AppUpdateInfo.canStartImmediateUpdate(): Boolean {
        val availability = updateAvailability()
        return (availability == UpdateAvailability.UPDATE_AVAILABLE ||
            availability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) &&
            isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
    }

    private fun logAppUpdateInfo(appUpdateInfo: AppUpdateInfo) {
        Timber.d(
            "Play app update info. availability=%d priority=%d immediateAllowed=%s",
            appUpdateInfo.updateAvailability(),
            appUpdateInfo.updatePriority(),
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
        )
    }
}
