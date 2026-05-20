package pt.ms.myshare.data.repository

import android.content.SharedPreferences
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedFirstRunExperienceRepositoryTest {

    @Test
    fun `home coach marks pending persists in preferences`() = runTest {
        val storage = mutableMapOf<String, Boolean>()
        val editor = mockk<SharedPreferences.Editor>()
        val prefs = mockk<SharedPreferences>()
        every { prefs.getBoolean(any(), any()) } answers {
            storage[firstArg()] ?: secondArg()
        }
        every { prefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } answers {
            storage[firstArg()] = secondArg()
            editor
        }
        every { editor.apply() } just Runs
        val repository = SharedFirstRunExperienceRepository(prefs)

        assertFalse(repository.isHomeCoachMarksPending())

        repository.setHomeCoachMarksPending(true)
        assertTrue(repository.isHomeCoachMarksPending())

        repository.setHomeCoachMarksPending(false)
        assertFalse(repository.isHomeCoachMarksPending())
    }
}
