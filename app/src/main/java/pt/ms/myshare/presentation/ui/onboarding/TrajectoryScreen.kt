package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.MySharePositive
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Calendar

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                PremiumButton(
                    text = stringResource(R.string.onboarding_trajectory_button),
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .padding(horizontal = 24.dp)
                .imeNestedScroll()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(24.dp))

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
                val formattedPriorityContribution = preview.priorityContributionPerPayday
                    .takeIf { it > BigDecimal.ZERO }
                    ?.let { amount ->
                        LocalizedAmountFormatter.formatCurrency(
                            amount = amount,
                            locale = userPreferences.locale,
                            currencyCode = userPreferences.currencyCode
                        )
                    }
                val exampleUnusedAmount = preview.weeklyFlexibleSpend
                    .multiply(BigDecimal("0.30"))
                    .setScale(2, RoundingMode.HALF_UP)
                val formattedExampleUnused = LocalizedAmountFormatter.formatCurrency(
                    amount = exampleUnusedAmount,
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
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, date.year)
                            set(Calendar.MONTH, date.monthValue - 1)
                        }
                        android.text.format.DateFormat.format("MMMM yyyy", calendar).toString()
                    },
                    goalName = goalName,
                    contribution = currencyFormat.format(preview.priorityContributionPerPayday),
                    weeklyFlexibleSpend = formattedWeeklyFlexibleSpend,
                    hasPriorityContribution = hasPriorityContribution
                )

                Spacer(Modifier.height(12.dp))

                TrajectoryPremiumBridgeCard(
                    weeklyFlexibleSpend = formattedWeeklyFlexibleSpend,
                    priorityContribution = formattedPriorityContribution,
                    goalName = goalName
                )

                Spacer(Modifier.height(12.dp))

                TrajectoryLockedAdjustmentCard(
                    exampleUnusedAmount = formattedExampleUnused,
                    weeklyFlexibleSpend = formattedWeeklyFlexibleSpend,
                    goalName = goalName,
                    hasPriorityContribution = hasPriorityContribution
                )

                Spacer(Modifier.height(10.dp))

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
private fun TrajectoryLockedAdjustmentCard(
    exampleUnusedAmount: String,
    weeklyFlexibleSpend: String,
    goalName: String,
    hasPriorityContribution: Boolean,
    modifier: Modifier = Modifier
) {
    val body = if (hasPriorityContribution && goalName.isNotBlank()) {
        stringResource(
            R.string.onboarding_trajectory_adjustment_body,
            exampleUnusedAmount,
            goalName,
            weeklyFlexibleSpend
        )
    } else {
        stringResource(
            R.string.onboarding_trajectory_adjustment_body_without_priority,
            exampleUnusedAmount,
            weeklyFlexibleSpend
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MySharePrimary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_trajectory_adjustment_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.onboarding_trajectory_adjustment_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 320.dp
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TrajectoryAdjustmentPill(
                            label = stringResource(R.string.onboarding_trajectory_adjustment_free_label),
                            body = stringResource(R.string.onboarding_trajectory_adjustment_free_body),
                            icon = Icons.Default.RadioButtonUnchecked,
                            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TrajectoryAdjustmentPill(
                            label = stringResource(R.string.onboarding_trajectory_adjustment_premium_label),
                            body = stringResource(R.string.onboarding_trajectory_adjustment_premium_body),
                            icon = Icons.Default.CheckCircle,
                            iconTint = MySharePrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TrajectoryAdjustmentPill(
                            label = stringResource(R.string.onboarding_trajectory_adjustment_free_label),
                            body = stringResource(R.string.onboarding_trajectory_adjustment_free_body),
                            icon = Icons.Default.RadioButtonUnchecked,
                            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        TrajectoryAdjustmentPill(
                            label = stringResource(R.string.onboarding_trajectory_adjustment_premium_label),
                            body = stringResource(R.string.onboarding_trajectory_adjustment_premium_body),
                            icon = Icons.Default.CheckCircle,
                            iconTint = MySharePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrajectoryAdjustmentPill(
    label: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconTint,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TrajectoryPremiumBridgeCard(
    weeklyFlexibleSpend: String,
    priorityContribution: String?,
    goalName: String,
    modifier: Modifier = Modifier
) {
    val body = if (priorityContribution != null && goalName.isNotBlank()) {
        stringResource(
            R.string.onboarding_trajectory_premium_bridge_body,
            weeklyFlexibleSpend,
            priorityContribution,
            goalName
        )
    } else {
        stringResource(
            R.string.onboarding_trajectory_premium_bridge_body_without_priority,
            weeklyFlexibleSpend
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = MySharePrimary.copy(alpha = 0.16f)
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_trajectory_premium_bridge_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MySharePrimary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(R.string.onboarding_trajectory_premium_bridge_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun TrajectorySummaryCard(
    paydaySummary: String,
    goalDate: String?,
    goalName: String,
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
            TrajectorySummaryRow(
                title = stringResource(R.string.onboarding_trajectory_label_payday),
                value = paydaySummary,
                body = stringResource(R.string.onboarding_trajectory_desc_payday),
                icon = Icons.Default.Savings,
                iconTint = MySharePrimary
            )
            TrajectorySummaryRow(
                title = if (hasPriorityContribution) {
                    stringResource(R.string.onboarding_trajectory_label_goal)
                } else {
                    stringResource(R.string.onboarding_trajectory_label_strategy)
                },
                value = goalDate ?: stringResource(R.string.onboarding_trajectory_goal_pending),
                body = if (hasPriorityContribution) {
                    stringResource(R.string.onboarding_trajectory_goal_subtitle, goalName)
                } else {
                    stringResource(R.string.onboarding_trajectory_strategy_pending)
                },
                icon = Icons.Default.CalendarMonth,
                iconTint = MySharePositive
            )
            TrajectorySummaryRow(
                title = if (hasPriorityContribution) {
                    stringResource(R.string.onboarding_trajectory_intensity_title)
                } else {
                    stringResource(R.string.onboarding_trajectory_flexible_title)
                },
                value = contribution,
                body = if (hasPriorityContribution) {
                    stringResource(R.string.onboarding_trajectory_intensity_desc)
                } else {
                    stringResource(R.string.onboarding_trajectory_flexible_desc)
                },
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconTint = MySharePrimary
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
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
private fun TrajectorySummaryRow(
    title: String,
    value: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
