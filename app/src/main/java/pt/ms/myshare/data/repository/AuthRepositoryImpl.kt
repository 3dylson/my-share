package pt.ms.myshare.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pt.ms.myshare.domain.model.User
import pt.ms.myshare.domain.repository.AuthRepository
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val user = firebaseUser?.let {
                User(
                    email = it.email
                )
            }
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            Timber.d("Google sign-in completed")
            authResult.user?.toDomainResult()
                ?: Result.failure(Exception("Signup successful but user is null"))
        } catch (e: Exception) {
            Timber.e(e, "Google sign-in failed")
            Result.failure(e)
        }
    }

    override suspend fun connectGoogleAccount(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val currentUser = firebaseAuth.currentUser
            val authResult = if (currentUser == null) {
                Timber.d("No Firebase user exists. Signing in with Google instead of linking.")
                firebaseAuth.signInWithCredential(credential).await()
            } else {
                Timber.d("Linking Google account to current Firebase user. anonymous=%s", currentUser.isAnonymous)
                currentUser.linkWithCredential(credential).await()
            }

            Timber.d("Google account connected")
            authResult.user?.toDomainResult()
                ?: Result.failure(Exception("Google account connected but user is null"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Timber.e(e, "Google account is already linked to another Firebase user")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Google account connection failed")
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<User> {
        return try {
            val authResult = firebaseAuth.signInAnonymously().await()
            Timber.d("Anonymous sign-in completed")
            authResult.user?.toDomainResult()
                ?: Result.failure(Exception("Anonymous login failed: user is null"))
        } catch (e: Exception) {
            Timber.e(e, "Anonymous sign-in failed")
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        Timber.d("Signing out Firebase user")
        firebaseAuth.signOut()
    }

    private fun com.google.firebase.auth.FirebaseUser.toDomainResult(): Result<User> {
        return Result.success(User(email = email))
    }
}
