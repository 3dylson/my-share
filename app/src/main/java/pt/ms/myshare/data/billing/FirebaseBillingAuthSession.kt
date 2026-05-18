package pt.ms.myshare.data.billing

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class FirebaseBillingAuthSession @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : BillingAuthSession {

    override val userId: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }
        trySend(firebaseAuth.currentUser?.uid)
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override fun currentUserId(): String? = firebaseAuth.currentUser?.uid

    override suspend fun requireAuthenticatedUserId(): Result<String> =
        requireAuthenticatedSession().map { it.userId }

    override suspend fun requireAuthenticatedSession(): Result<BillingAuthenticatedSession> {
        val existingUid = firebaseAuth.currentUser?.uid
        if (!existingUid.isNullOrBlank()) {
            Timber.tag(TAG).d("Reusing existing Firebase billing session")
            return requireIdToken(existingUid)
        }

        return try {
            Timber.tag(TAG).d("Creating anonymous Firebase billing session")
            val authResult = firebaseAuth.signInAnonymously().await()
            val uid = authResult.user?.uid
            if (uid.isNullOrBlank()) {
                Result.failure(IllegalStateException("Anonymous billing session returned no user"))
            } else {
                Timber.tag(TAG).d("Anonymous Firebase billing session ready")
                requireIdToken(uid)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Anonymous Firebase billing session failed")
            Result.failure(e)
        }
    }

    private suspend fun requireIdToken(uid: String): Result<BillingAuthenticatedSession> {
        return try {
            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
            if (token.isNullOrBlank()) {
                Result.failure(IllegalStateException("Firebase billing session returned no ID token"))
            } else {
                Timber.tag(TAG).d("Firebase billing ID token ready")
                Result.success(BillingAuthenticatedSession(userId = uid, idToken = token))
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Firebase billing ID token failed")
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "BillingAuthSession"
    }
}
