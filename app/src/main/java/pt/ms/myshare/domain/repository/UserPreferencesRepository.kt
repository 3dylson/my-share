package pt.ms.myshare.domain.repository

import kotlinx.coroutines.flow.Flow
import pt.ms.myshare.domain.model.UserPreferences

interface UserPreferencesRepository {
    fun observePreferences(): Flow<UserPreferences>
    fun loadPreferences(): UserPreferences
    suspend fun savePreferences(preferences: UserPreferences)
    suspend fun syncFromFirestore()
    suspend fun syncToFirestoreIfAuthenticated()
}
