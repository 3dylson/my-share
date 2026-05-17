package pt.ms.myshare.domain.model

enum class AllocationStrategy {
    BALANCED_SAVINGS,
    NO_SAVINGS_NOW,
    DEBT_FIRST,
    INVESTING_FIRST,
    FLEXIBLE_BUDGET_ONLY,
    CUSTOM
}
