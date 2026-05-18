package pt.ms.myshare.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ms.myshare.data.remote.FirestoreAppUpdatePolicyMapper
import pt.ms.myshare.domain.model.AppUpdatePolicy
import pt.ms.myshare.domain.model.AppUpdatePolicyLoadResult
import pt.ms.myshare.domain.model.AppUpdatePolicySource
import pt.ms.myshare.domain.repository.AppUpdatePolicyRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class FirestoreAppUpdatePolicyRepository @Inject constructor(
    context: Context,
    private val firestoreProvider: Provider<FirebaseFirestore>
) : AppUpdatePolicyRepository {

    private val appContext = context.applicationContext
    private val preferences by lazy {
        appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun loadPolicy(): AppUpdatePolicyLoadResult = withContext(Dispatchers.IO) {
        try {
            Timber.d("Fetching app update policy from Firestore")
            val snapshot = firestoreProvider.get().collection(COLLECTION_APP_CONFIG)
                .document(DOCUMENT_ANDROID_UPDATE_POLICY)
                .get()
                .await()

            check(snapshot.exists()) { "Android update policy document is missing" }
            val policy = FirestoreAppUpdatePolicyMapper.map(snapshot.data.orEmpty()).getOrThrow()
            cachePolicy(policy)
            Timber.d(
                "App update policy fetched. minimumSupportedVersionCode=%d immediateUpdateRequired=%s",
                policy.minimumSupportedVersionCode,
                policy.immediateUpdateRequired
            )
            AppUpdatePolicyLoadResult.Available(policy, AppUpdatePolicySource.Remote)
        } catch (error: Exception) {
            Timber.e(error, "Unable to fetch app update policy from Firestore")
            val cachedPolicy = loadCachedPolicy()
            if (cachedPolicy != null) {
                Timber.d(
                    "Using cached app update policy. minimumSupportedVersionCode=%d immediateUpdateRequired=%s",
                    cachedPolicy.minimumSupportedVersionCode,
                    cachedPolicy.immediateUpdateRequired
                )
                AppUpdatePolicyLoadResult.Available(cachedPolicy, AppUpdatePolicySource.Cache)
            } else {
                AppUpdatePolicyLoadResult.Unavailable(error)
            }
        }
    }

    private fun cachePolicy(policy: AppUpdatePolicy) {
        preferences.edit()
            .putInt(KEY_MINIMUM_SUPPORTED_VERSION_CODE, policy.minimumSupportedVersionCode)
            .putBoolean(KEY_IMMEDIATE_UPDATE_REQUIRED, policy.immediateUpdateRequired)
            .putString(KEY_PLAY_STORE_PACKAGE_NAME, policy.playStorePackageName)
            .apply()
    }

    private fun loadCachedPolicy(): AppUpdatePolicy? {
        if (!preferences.contains(KEY_MINIMUM_SUPPORTED_VERSION_CODE)) return null
        return AppUpdatePolicy(
            minimumSupportedVersionCode = preferences.getInt(KEY_MINIMUM_SUPPORTED_VERSION_CODE, 0),
            immediateUpdateRequired = preferences.getBoolean(KEY_IMMEDIATE_UPDATE_REQUIRED, false),
            playStorePackageName = preferences.getString(KEY_PLAY_STORE_PACKAGE_NAME, DEFAULT_PACKAGE_NAME)
                ?: DEFAULT_PACKAGE_NAME
        )
    }

    companion object {
        private const val COLLECTION_APP_CONFIG = "app_config"
        private const val DOCUMENT_ANDROID_UPDATE_POLICY = "android_update_policy"
        private const val PREFERENCES_NAME = "app_update_policy"
        private const val KEY_MINIMUM_SUPPORTED_VERSION_CODE = "minimum_supported_version_code"
        private const val KEY_IMMEDIATE_UPDATE_REQUIRED = "immediate_update_required"
        private const val KEY_PLAY_STORE_PACKAGE_NAME = "play_store_package_name"
        private const val DEFAULT_PACKAGE_NAME = "pt.ms.myshare"
    }
}
