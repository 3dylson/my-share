package pt.ms.myshare.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import java.math.BigDecimal

class UpdateGoalProgressUseCaseTest {

    private lateinit var plannerRepository: PlannerRepository
    private lateinit var entitlementRepository: EntitlementRepository
    private lateinit var updateGoalProgressUseCase: UpdateGoalProgressUseCase

    @Before
    fun setUp() {
        plannerRepository = mockk(relaxed = true)
        entitlementRepository = mockk(relaxed = true)
        updateGoalProgressUseCase = UpdateGoalProgressUseCase(plannerRepository, entitlementRepository)
    }

    @Test
    fun `execute adds contribution to single goal for free user`() = runTest {
        // Given
        val goal = Goal(id = "1", name = "Travel", targetAmount = BigDecimal("1000"), currentProgress = BigDecimal("100"))
        coEvery { entitlementRepository.isPro } returns flowOf(false)
        coEvery { plannerRepository.observeGoals() } returns flowOf(listOf(goal))

        // When
        updateGoalProgressUseCase.execute(BigDecimal("50"))

        // Then
        coVerify { 
            plannerRepository.saveGoal(match { 
                it.id == "1" && it.currentProgress == BigDecimal("150") && !it.isCompleted 
            }) 
        }
    }

    @Test
    fun `execute splits contribution among active goals for pro user`() = runTest {
        // Given
        val goal1 = Goal(id = "1", name = "Travel", targetAmount = BigDecimal("1000"), currentProgress = BigDecimal("0"))
        val goal2 = Goal(id = "2", name = "Emergency", targetAmount = BigDecimal("1000"), currentProgress = BigDecimal("0"))
        coEvery { entitlementRepository.isPro } returns flowOf(true)
        coEvery { plannerRepository.observeGoals() } returns flowOf(listOf(goal1, goal2))

        // When
        updateGoalProgressUseCase.execute(BigDecimal("100"))

        // Then
        coVerify { 
            plannerRepository.saveGoal(match { it.id == "1" && it.currentProgress == BigDecimal("50.00") })
            plannerRepository.saveGoal(match { it.id == "2" && it.currentProgress == BigDecimal("50.00") })
        }
    }

    @Test
    fun `execute marks goal as completed when target is reached`() = runTest {
        // Given
        val goal = Goal(id = "1", name = "Phone", targetAmount = BigDecimal("500"), currentProgress = BigDecimal("450"))
        coEvery { entitlementRepository.isPro } returns flowOf(false)
        coEvery { plannerRepository.observeGoals() } returns flowOf(listOf(goal))

        // When
        updateGoalProgressUseCase.execute(BigDecimal("50"))

        // Then
        coVerify { 
            plannerRepository.saveGoal(match { it.id == "1" && it.isCompleted }) 
        }
    }
}
