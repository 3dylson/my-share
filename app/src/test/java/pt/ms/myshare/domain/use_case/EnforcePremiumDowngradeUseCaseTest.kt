package pt.ms.myshare.domain.use_case

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.ms.myshare.domain.model.EntitlementState
import pt.ms.myshare.domain.repository.PlannerRepository

class EnforcePremiumDowngradeUseCaseTest {

    private lateinit var plannerRepository: PlannerRepository
    private lateinit var useCase: EnforcePremiumDowngradeUseCase

    @Before
    fun setUp() {
        plannerRepository = mockk(relaxed = true)
        useCase = EnforcePremiumDowngradeUseCase(plannerRepository)
    }

    @Test
    fun `execute disables automation when entitlement becomes free`() = runTest {
        every { plannerRepository.observeAutomationEnabled() } returns flowOf(true)

        val changed = useCase.execute(EntitlementState.FREE)

        assertTrue(changed)
        coVerify { plannerRepository.saveAutomationEnabled(false) }
    }

    @Test
    fun `execute keeps automation during grace period`() = runTest {
        every { plannerRepository.observeAutomationEnabled() } returns flowOf(true)

        val changed = useCase.execute(EntitlementState.GRACE_PERIOD)

        assertFalse(changed)
        coVerify(exactly = 0) { plannerRepository.saveAutomationEnabled(any()) }
    }

    @Test
    fun `execute ignores unknown entitlement while billing is still loading`() = runTest {
        every { plannerRepository.observeAutomationEnabled() } returns flowOf(true)

        val changed = useCase.execute(EntitlementState.UNKNOWN)

        assertFalse(changed)
        coVerify(exactly = 0) { plannerRepository.saveAutomationEnabled(any()) }
    }
}
