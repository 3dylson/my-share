package pt.ms.myshare.presentation.ui.appupdate

import pt.ms.myshare.domain.model.AppUpdatePolicy

sealed class AppUpdateGateState {
    data object Loading : AppUpdateGateState()
    data object Ready : AppUpdateGateState()
    data class RequiredUpdate(val policy: AppUpdatePolicy) : AppUpdateGateState()
}
