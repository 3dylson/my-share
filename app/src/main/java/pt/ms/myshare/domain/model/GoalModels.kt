package pt.ms.myshare.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

enum class GoalType {
    EMERGENCY_FUND,
    INVEST_TARGET,
    CUSTOM
}

data class Goal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: BigDecimal,
    val currentProgress: BigDecimal = BigDecimal.ZERO,
    val type: GoalType = GoalType.CUSTOM,
    val createdAt: LocalDate = LocalDate.now(),
    val isCompleted: Boolean = false
)

sealed class PaySchedule {
    data class Monthly(val dayOfMonth: Int) : PaySchedule()
    data class BiWeekly(val nextPayday: LocalDate) : PaySchedule()
}

enum class AllocationPreset {
    CONSERVATIVE,
    BALANCED,
    GROWTH
}

data class PlanInput(
    val netSalary: BigDecimal,
    val schedule: PaySchedule,
    val preset: AllocationPreset,
    val goal: Goal
)

