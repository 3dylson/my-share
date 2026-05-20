package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.KeyboardDismissEffect
import pt.ms.myshare.presentation.ui.components.rememberKeyboardDismissOnScrollConnection
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.MySharePositive
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import java.text.NumberFormat
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrajectoryScreen(
    preview: PlanPreview?,
    goalName: String,
    userPreferences: UserPreferences,
    onNext: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(userPreferences.locale).apply {
        currency = userPreferences.currency
    }
    val keyboardDismissOnScrollConnection = rememberKeyboardDismissOnScrollConnection()

    KeyboardDismissEffect(preview?.nextPayday)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                PremiumButton(
                    text = stringResource(R.string.onboarding_trajectory_button),
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .nestedScroll(keyboardDismissOnScrollConnection)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.onboarding_trajectory_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.onboarding_trajectory_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 21.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(20.dp))

            if (preview != null) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val hasPriorityContribution = preview.priorityContributionPerPayday > java.math.BigDecimal.ZERO
                val formattedWeeklyFlexibleSpend = LocalizedAmountFormatter.formatCurrency(
                    amount = preview.weeklyFlexibleSpend,
                    locale = userPreferences.locale,
                    currencyCode = userPreferences.currencyCode
                )
                val paydaySummary = remember(preview.summary) {
                    val resId = context.resources.getIdentifier(preview.summary, "string", context.packageName)
                    if (resId != 0) context.getString(resId) else preview.summary
                }
                TrajectorySummaryCard(
                    paydaySummary = paydaySummary,
                    goalDate = preview.goalTargetDate?.let { date ->
                        date.format(DateTimeFormatter.ofPattern("MMMM yyyy", userPreferences.locale))
                    },
                    contribution = currencyFormat.format(preview.priorityContributionPerPayday),
                    weeklyFlexibleSpend = formattedWeeklyFlexibleSpend,
                    hasPriorityContribution = hasPriorityContribution
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.onboarding_trajectory_footer),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun TrajectorySummaryCard(
    paydaySummary: String,
    goalDate: String?,
    contribution: String,
    weeklyFlexibleSpend: String,
    hasPriorityContribution: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrajectoryPathVisual(
                weeklyFlexibleSpend = weeklyFlexibleSpend,
                contribution = contribution,
                goalDate = goalDate,
                hasPriorityContribution = hasPriorityContribution
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
            TrajectorySummaryTakeaway(
                title = stringResource(R.string.onboarding_trajectory_label_payday),
                value = paydaySummary,
                body = stringResource(R.string.onboarding_trajectory_desc_payday)
            )
        }
    }
}

@Composable
private fun TrajectoryPathVisual(
    weeklyFlexibleSpend: String,
    contribution: String,
    goalDate: String?,
    hasPriorityContribution: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        TrajectoryPathNode(
            title = stringResource(R.string.onboarding_trajectory_label_payday),
            value = weeklyFlexibleSpend,
            icon = Icons.Default.Savings,
            tint = MySharePrimary,
            modifier = Modifier.weight(1f)
        )
        TrajectoryPathConnector()
        TrajectoryPathNode(
            title = if (hasPriorityContribution) {
                stringResource(R.string.onboarding_trajectory_flexible_title)
            } else {
                stringResource(R.string.onboarding_trajectory_label_strategy)
            },
            value = contribution,
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            tint = MySharePositive,
            modifier = Modifier.weight(1f)
        )
        TrajectoryPathConnector()
        TrajectoryPathNode(
            title = if (hasPriorityContribution) {
                stringResource(R.string.onboarding_trajectory_label_goal)
            } else {
                stringResource(R.string.onboarding_trajectory_goal_pending)
            },
            value = goalDate ?: stringResource(R.string.onboarding_trajectory_goal_pending),
            icon = Icons.Default.CalendarMonth,
            tint = MySharePrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TrajectoryPathNode(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = tint.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, tint.copy(alpha = 0.18f))
        ) {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun TrajectoryPathConnector() {
    Surface(
        modifier = Modifier
            .padding(top = 17.dp)
            .width(18.dp)
            .height(2.dp),
        color = MySharePrimary.copy(alpha = 0.24f),
        shape = RoundedCornerShape(999.dp),
        content = {}
    )
}

@Composable
private fun TrajectorySummaryTakeaway(
    title: String,
    value: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}
