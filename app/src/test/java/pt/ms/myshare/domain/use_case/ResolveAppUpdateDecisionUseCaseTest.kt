package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ms.myshare.domain.model.AppUpdatePolicy
import pt.ms.myshare.domain.model.AppUpdatePolicyLoadResult
import pt.ms.myshare.domain.model.AppUpdatePolicySource
import pt.ms.myshare.domain.repository.AppUpdatePolicyRepository

class ResolveAppUpdateDecisionUseCaseTest {

    @Test
    fun `version below minimum requires immediate update and blocks app use`() = runTest {
        val useCase = ResolveAppUpdateDecisionUseCase(
            FakeAppUpdatePolicyRepository(
                AppUpdatePolicyLoadResult.Available(
                    policy = AppUpdatePolicy(
                        minimumSupportedVersionCode = 8,
                        immediateUpdateRequired = true,
                        playStorePackageName = "pt.ms.myshare"
                    ),
                    source = AppUpdatePolicySource.Remote
                )
            )
        )

        val decision = useCase(installedVersionCode = 7)

        assertTrue(decision.shouldRequestImmediateUpdate)
        assertTrue(decision.mustBlockAppUse)
        assertFalse(decision.canContinue)
    }

    @Test
    fun `version matching minimum can continue`() = runTest {
        val useCase = ResolveAppUpdateDecisionUseCase(
            FakeAppUpdatePolicyRepository(
                AppUpdatePolicyLoadResult.Available(
                    policy = AppUpdatePolicy(
                        minimumSupportedVersionCode = 8,
                        immediateUpdateRequired = true,
                        playStorePackageName = "pt.ms.myshare"
                    ),
                    source = AppUpdatePolicySource.Remote
                )
            )
        )

        val decision = useCase(installedVersionCode = 8)

        assertFalse(decision.shouldRequestImmediateUpdate)
        assertFalse(decision.mustBlockAppUse)
        assertTrue(decision.canContinue)
    }

    @Test
    fun `cached policy still blocks when installed version is below minimum`() = runTest {
        val useCase = ResolveAppUpdateDecisionUseCase(
            FakeAppUpdatePolicyRepository(
                AppUpdatePolicyLoadResult.Available(
                    policy = AppUpdatePolicy(
                        minimumSupportedVersionCode = 8,
                        immediateUpdateRequired = true,
                        playStorePackageName = "pt.ms.myshare"
                    ),
                    source = AppUpdatePolicySource.Cache
                )
            )
        )

        val decision = useCase(installedVersionCode = 7)

        assertTrue(decision.shouldRequestImmediateUpdate)
        assertTrue(decision.mustBlockAppUse)
    }

    @Test
    fun `unavailable policy allows app usage with recoverable error`() = runTest {
        val error = IllegalStateException("Config unavailable")
        val useCase = ResolveAppUpdateDecisionUseCase(
            FakeAppUpdatePolicyRepository(AppUpdatePolicyLoadResult.Unavailable(error))
        )

        val decision = useCase(installedVersionCode = 7)

        assertFalse(decision.shouldRequestImmediateUpdate)
        assertFalse(decision.mustBlockAppUse)
        assertTrue(decision.canContinue)
        assertSame(error, decision.policyError)
    }
}

private class FakeAppUpdatePolicyRepository(
    private val result: AppUpdatePolicyLoadResult
) : AppUpdatePolicyRepository {
    override suspend fun loadPolicy(): AppUpdatePolicyLoadResult = result
}
