package pt.ms.myshare.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pt.ms.myshare.data.auth.CredentialStateClearer
import pt.ms.myshare.domain.model.GoogleAccountConnection
import pt.ms.myshare.domain.model.GoogleAccountConnectionMode
import pt.ms.myshare.domain.model.User
import pt.ms.myshare.domain.repository.AuthRepository
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val credentialStateClearer: CredentialStateClearer
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val user = firebaseUser?.let {
                User(
                    id = it.uid,
                    email = it.email,
                    isAnonymous = it.isAnonymous
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

    override suspend fun connectGoogleAccount(idToken: String): Result<GoogleAccountConnection> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val previousUser = firebaseAuth.currentUser
        return try {
            val mode: GoogleAccountConnectionMode
            val authResult = if (previousUser == null) {
                Timber.d("No Firebase user exists. Signing in with Google instead of linking.")
                mode = GoogleAccountConnectionMode.SignedIn
                firebaseAuth.signInWithCredential(credential).await()
            } else {
                Timber.d("Linking Google account to current Firebase user. anonymous=%s", previousUser.isAnonymous)
                mode = GoogleAccountConnectionMode.LinkedToCurrentUser
                previousUser.linkWithCredential(credential).await()
            }

            Timber.d("Google account connected mode=%s", mode)
            authResult.user?.toConnectionResult(mode = mode, previousUserId = previousUser?.uid)
                ?: Result.failure(Exception("Google account connected but user is null"))
        } catch (e: FirebaseAuthUserCollisionException) {
            try {
                Timber.e(e, "Google account is already linked to another Firebase user. Signing in to existing account.")
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                Timber.d(
                    "Signed in existing Google account after link collision. previousUid=%s currentUid=%s",
                    previousUser?.uid,
                    authResult.user?.uid
                )
                authResult.user?.toConnectionResult(
                    mode = GoogleAccountConnectionMode.SignedInToExistingAccount,
                    previousUserId = previousUser?.uid
                )
                    ?: Result.failure(Exception("Google sign-in after link collision returned null user"))
            } catch (signInError: Exception) {
                Timber.e(signInError, "Google sign-in after account collision failed")
                Result.failure(signInError)
            }
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
        try {
            credentialStateClearer.clearCredentialState()
            Timber.d("Credential Manager state cleared after Firebase sign-out")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear Credential Manager state after Firebase sign-out")
        }
    }

    private fun com.google.firebase.auth.FirebaseUser.toDomainResult(): Result<User> {
        return Result.success(User(id = uid, email = email, isAnonymous = isAnonymous))
    }

    private fun com.google.firebase.auth.FirebaseUser.toConnectionResult(
        mode: GoogleAccountConnectionMode,
        previousUserId: String?
    ): Result<GoogleAccountConnection> {
        return Result.success(
            GoogleAccountConnection(
                user = User(id = uid, email = email, isAnonymous = isAnonymous),
                mode = mode,
                previousUserId = previousUserId,
                currentUserId = uid
            )
        )
    }
}
