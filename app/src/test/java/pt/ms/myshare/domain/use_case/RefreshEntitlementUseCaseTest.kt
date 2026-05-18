package pt.ms.myshare.domain.use_case

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import pt.ms.myshare.domain.repository.EntitlementRepository

class RefreshEntitlementUseCaseTest {

    @Test
    fun `invoke refreshes active entitlement from repository`() = runTest {
        val entitlementRepository = mockk<EntitlementRepository>(relaxed = true)
        val useCase = RefreshEntitlementUseCase(entitlementRepository)

        useCase()

        coVerify { entitlementRepository.checkActiveEntitlement() }
    }
}
