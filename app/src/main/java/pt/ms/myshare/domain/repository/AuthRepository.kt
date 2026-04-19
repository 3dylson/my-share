package pt.ms.myshare.domain.repository

import kotlinx.coroutines.flow.Flow
import pt.ms.myshare.domain.model.User

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInAnonymously(): Result<User>
    suspend fun signOut()
}
