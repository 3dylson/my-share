package pt.ms.myshare.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AuthRepositoryImplTest {

    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val repository = AuthRepositoryImpl(firebaseAuth)

    @Test
    fun `connectGoogleAccount signs into existing account after link collision`() = runTest {
        val anonymousUser = mockk<FirebaseUser>(relaxed = true)
        val existingUser = mockk<FirebaseUser>(relaxed = true)
        val authResult = mockk<AuthResult>()
        val collision = FirebaseAuthUserCollisionException(
            "ERROR_CREDENTIAL_ALREADY_IN_USE",
            "Credential already in use"
        )

        every { anonymousUser.uid } returns "anonymous-uid"
        every { anonymousUser.isAnonymous } returns true
        every { anonymousUser.linkWithCredential(any()) } returns Tasks.forException(collision)
        every { existingUser.uid } returns "existing-uid"
        every { existingUser.email } returns "user@example.com"
        every { existingUser.isAnonymous } returns false
        every { authResult.user } returns existingUser
        every { firebaseAuth.currentUser } returns anonymousUser
        every { firebaseAuth.signInWithCredential(any()) } returns Tasks.forResult(authResult)

        val result = repository.connectGoogleAccount("google-id-token")

        assertEquals("user@example.com", result.getOrThrow().email)
        assertFalse(result.getOrThrow().isAnonymous)
        verify(exactly = 1) { firebaseAuth.signInWithCredential(any()) }
    }
}
