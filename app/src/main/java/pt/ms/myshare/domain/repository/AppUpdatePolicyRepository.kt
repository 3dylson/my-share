package pt.ms.myshare.domain.repository

import pt.ms.myshare.domain.model.AppUpdatePolicyLoadResult

interface AppUpdatePolicyRepository {
    suspend fun loadPolicy(): AppUpdatePolicyLoadResult
}
