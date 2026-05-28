package pt.ms.myshare.domain.use_case

enum class ReminderResponseAction(
    val labelKey: String,
    val analyticsValue: String
) {
    FINE(
        labelKey = "notification_action_fine",
        analyticsValue = "fine"
    ),
    TIGHT(
        labelKey = "notification_action_tight",
        analyticsValue = "tight"
    ),
    ALMOST_GONE(
        labelKey = "notification_action_almost_gone",
        analyticsValue = "almost_gone"
    ),
    MOVED_GOAL_MONEY(
        labelKey = "notification_action_moved_goal_money",
        analyticsValue = "moved_goal_money"
    )
}
