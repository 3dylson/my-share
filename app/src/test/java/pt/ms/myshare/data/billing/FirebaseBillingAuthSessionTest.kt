package pt.ms.myshare.data.billing

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseBillingAuthSessionTest {

    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val session = FirebaseBillingAuthSession(firebaseAuth)

    @Test
    fun `requireAuthenticatedSession reuses existing Firebase user`() = runTest {
        val user = firebaseUser(uid = "existing-uid", token = "existing-token")
        every { firebaseAuth.currentUser } returns user

        val result = session.requireAuthenticatedSession()

        assertEquals(
            BillingAuthenticatedSession(userId = "existing-uid", idToken = "existing-token"),
            result.getOrThrow()
        )
        verify(exactly = 0) { firebaseAuth.signInAnonymously() }
    }

    @Test
    fun `requireAuthenticatedSession creates anonymous user only when missing Firebase user`() = runTest {
        val user = firebaseUser(uid = "anonymous-uid", token = "anonymous-token")
        val authResult = mockk<AuthResult>()
        every { authResult.user } returns user
        every { firebaseAuth.currentUser } returnsMany listOf(null, user)
        every { firebaseAuth.signInAnonymously() } returns Tasks.forResult(authResult)

        val result = session.requireAuthenticatedSession()

        assertEquals(
            BillingAuthenticatedSession(userId = "anonymous-uid", idToken = "anonymous-token"),
            result.getOrThrow()
        )
        verify(exactly = 1) { firebaseAuth.signInAnonymously() }
    }

    @Test
    fun `requireAuthenticatedSession returns failure when anonymous sign-in fails`() = runTest {
        val error = IllegalStateException("auth failed")
        every { firebaseAuth.currentUser } returns null
        every { firebaseAuth.signInAnonymously() } returns Tasks.forException(error)

        val result = session.requireAuthenticatedSession()

        assertTrue(result.isFailure)
        verify(exactly = 1) { firebaseAuth.signInAnonymously() }
    }

    private fun firebaseUser(uid: String, token: String): FirebaseUser {
        val user = mockk<FirebaseUser>(relaxed = true)
        val tokenResult = mockk<GetTokenResult>()
        every { user.uid } returns uid
        every { user.getIdToken(false) } returns Tasks.forResult(tokenResult)
        every { tokenResult.token } returns token
        return user
    }
}
