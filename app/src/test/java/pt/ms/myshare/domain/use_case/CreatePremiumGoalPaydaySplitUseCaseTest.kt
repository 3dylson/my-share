package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pt.ms.myshare.domain.model.Goal
import java.math.BigDecimal

class CreatePremiumGoalPaydaySplitUseCaseTest {

    private val useCase = CreatePremiumGoalPaydaySplitUseCase()

    @Test
    fun `splits next payday move across two active goals`() {
        val split = useCase.execute(
            goals = listOf(
                goal(id = "emergency"),
                goal(id = "investing")
            ),
            priorityMove = BigDecimal("300")
        )

        assertEquals(BigDecimal("300.00"), split?.totalMove)
        assertEquals(BigDecimal("195.00"), split?.items?.get(0)?.amount)
        assertEquals(BigDecimal("65"), split?.items?.get(0)?.sharePercent)
        assertEquals(BigDecimal("105.00"), split?.items?.get(1)?.amount)
        assertEquals(BigDecimal("35"), split?.items?.get(1)?.sharePercent)
    }

    @Test
    fun `keeps rounded item amounts equal to total move`() {
        val split = useCase.execute(
            goals = listOf(
                goal(id = "first"),
                goal(id = "second"),
                goal(id = "third")
            ),
            priorityMove = BigDecimal("100.01")
        )

        val itemTotal = split?.items.orEmpty().fold(BigDecimal.ZERO) { total, item ->
            total.add(item.amount)
        }

        assertEquals(BigDecimal("100.01"), split?.totalMove)
        assertEquals(split?.totalMove, itemTotal)
    }

    @Test
    fun `shares long goal list without hiding goals from the model`() {
        val split = useCase.execute(
            goals = listOf(
                goal(id = "first"),
                goal(id = "second"),
                goal(id = "third"),
                goal(id = "fourth")
            ),
            priorityMove = BigDecimal("200")
        )

        assertEquals(4, split?.items?.size)
        assertEquals(BigDecimal("100.00"), split?.items?.get(0)?.amount)
        assertEquals(BigDecimal("50.00"), split?.items?.get(1)?.amount)
        assertEquals(BigDecimal("25.00"), split?.items?.get(2)?.amount)
        assertEquals(BigDecimal("25.00"), split?.items?.get(3)?.amount)
    }

    @Test
    fun `does not create split without multiple active goals and a positive move`() {
        assertNull(
            useCase.execute(
                goals = listOf(goal(id = "only")),
                priorityMove = BigDecimal("100")
            )
        )
        assertNull(
            useCase.execute(
                goals = listOf(goal(id = "first"), goal(id = "second")),
                priorityMove = BigDecimal.ZERO
            )
        )
        assertNull(
            useCase.execute(
                goals = listOf(
                    goal(id = "done", currentProgress = BigDecimal("500"), isCompleted = true),
                    goal(id = "full", currentProgress = BigDecimal("500"))
                ),
                priorityMove = BigDecimal("100")
            )
        )
    }

    private fun goal(
        id: String,
        currentProgress: BigDecimal = BigDecimal.ZERO,
        isCompleted: Boolean = false
    ): Goal {
        return Goal(
            id = id,
            name = id,
            targetAmount = BigDecimal("500"),
            currentProgress = currentProgress,
            isCompleted = isCompleted
        )
    }
}
