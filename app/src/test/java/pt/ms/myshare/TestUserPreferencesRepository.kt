package pt.ms.myshare

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.domain.repository.UserPreferencesRepository

class TestUserPreferencesRepository(
    initialPreferences: UserPreferences = UserPreferences(languageTag = "en", currencyCode = "USD")
) : UserPreferencesRepository {
    private val preferences = MutableStateFlow(initialPreferences)

    override fun observePreferences(): Flow<UserPreferences> = preferences.asStateFlow()

    override fun loadPreferences(): UserPreferences = preferences.value

    override suspend fun savePreferences(preferences: UserPreferences) {
        this.preferences.value = preferences
    }

    override suspend fun syncFromFirestore() = Unit

    override suspend fun syncToFirestoreIfAuthenticated() = Unit
}
