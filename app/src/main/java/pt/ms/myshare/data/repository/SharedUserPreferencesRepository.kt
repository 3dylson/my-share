package pt.ms.myshare.data.repository

import android.content.Context
import android.telephony.TelephonyManager
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.domain.repository.UserPreferencesRepository
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedUserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserPreferencesRepository {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val preferencesState = MutableStateFlow(readPreferences())

    override fun observePreferences(): Flow<UserPreferences> = preferencesState.asStateFlow()

    override fun loadPreferences(): UserPreferences = preferencesState.value

    override suspend fun savePreferences(preferences: UserPreferences) = withContext(Dispatchers.IO) {
        val sanitized = UserPreferences.sanitize(
            languageTag = preferences.languageTag,
            currencyCode = preferences.currencyCode,
            countryIso = resolveCountryIso()
        )
        if (sanitized != preferences) {
            Timber.tag(TAG).d(
                "Sanitized user preferences language=%s currency=%s to language=%s currency=%s",
                preferences.languageTag,
                preferences.currencyCode,
                sanitized.languageTag,
                sanitized.currencyCode
            )
        }
        writeLocal(sanitized)
        preferencesState.value = sanitized
        syncToFirestoreIfAuthenticated()
    }

    override suspend fun syncFromFirestore() = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser ?: return@withContext
        try {
            Timber.tag(TAG).d("Syncing user preferences from Firestore")
            val snapshot = firestore.collection("users").document(user.uid).get().await()
            val remoteLanguage = snapshot.getString(FIELD_LANGUAGE_TAG)
            val remoteCurrency = snapshot.getString(FIELD_CURRENCY_CODE)
            if (remoteLanguage.isNullOrBlank() || remoteCurrency.isNullOrBlank()) {
                Timber.tag(TAG).d("Remote preferences missing. Writing local preferences to Firestore")
                syncToFirestoreIfAuthenticated()
                return@withContext
            }
            val remote = UserPreferences.sanitize(remoteLanguage, remoteCurrency, countryIso = resolveCountryIso())
            writeLocal(remote)
            preferencesState.value = remote
            Timber.tag(TAG).d("User preferences synced language=%s currency=%s", remote.languageTag, remote.currencyCode)
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Failed to sync user preferences from Firestore")
        }
    }

    override suspend fun syncToFirestoreIfAuthenticated() = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser ?: return@withContext
        val preferences = preferencesState.value
        try {
            Timber.tag(TAG).d("Saving user preferences to Firestore language=%s currency=%s", preferences.languageTag, preferences.currencyCode)
            firestore.collection("users").document(user.uid)
                .set(
                    mapOf(
                        FIELD_LANGUAGE_TAG to preferences.languageTag,
                        FIELD_CURRENCY_CODE to preferences.currencyCode,
                        FIELD_PROFILE_UPDATED_AT to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                .await()
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Failed to save user preferences to Firestore")
        }
    }

    private fun readPreferences(): UserPreferences {
        val preferences = UserPreferences.sanitize(
            languageTag = prefs.getString(KEY_LANGUAGE_TAG, null),
            currencyCode = prefs.getString(KEY_CURRENCY_CODE, null),
            countryIso = resolveCountryIso()
        )
        if (prefs.getString(KEY_LANGUAGE_TAG, null).isNullOrBlank() || prefs.getString(KEY_CURRENCY_CODE, null).isNullOrBlank()) {
            writeLocal(preferences)
        }
        return preferences
    }

    private fun writeLocal(preferences: UserPreferences) {
        prefs.edit()
            .putString(KEY_LANGUAGE_TAG, preferences.languageTag)
            .putString(KEY_CURRENCY_CODE, preferences.currencyCode)
            .apply()
    }

    private fun resolveCountryIso(): String? {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val networkCountry = telephonyManager?.networkCountryIso
            ?.takeIf { it.isNotBlank() }
            ?.uppercase(Locale.US)
        val simCountry = telephonyManager?.simCountryIso
            ?.takeIf { it.isNotBlank() }
            ?.uppercase(Locale.US)
        val localeCountry = Locale.getDefault().country
            .takeIf { it.isNotBlank() }
            ?.uppercase(Locale.US)
        val country = networkCountry ?: simCountry ?: localeCountry
        Timber.tag(TAG).d("Resolved default currency countryIso=%s", country)
        return country
    }

    private companion object {
        const val TAG = "UserPreferences"
        const val KEY_LANGUAGE_TAG = "user_preferences_language_tag"
        const val KEY_CURRENCY_CODE = "user_preferences_currency_code"
        const val FIELD_LANGUAGE_TAG = "languageTag"
        const val FIELD_CURRENCY_CODE = "currencyCode"
        const val FIELD_PROFILE_UPDATED_AT = "profileUpdatedAt"
    }
}
