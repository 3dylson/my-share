package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.CircularProgressIndicator
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
import pt.ms.myshare.presentation.ui.theme.MySharePositive
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

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
            Spacer(Modifier.height(32.dp))

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

            Spacer(Modifier.height(28.dp))

            if (preview != null) {
                val context = androidx.compose.ui.platform.LocalContext.current
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
                    contribution = currencyFormat.format(preview.savingsPerPayday)
                )

                Spacer(Modifier.height(18.dp))

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

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TrajectorySummaryCard(
    paydaySummary: String,
    goalDate: String?,
    goalName: String,
    contribution: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TrajectorySummaryRow(
                title = stringResource(R.string.onboarding_trajectory_label_payday),
                value = paydaySummary,
                body = stringResource(R.string.onboarding_trajectory_desc_payday),
                icon = Icons.Default.Savings,
                iconTint = MySharePrimary
            )
            TrajectorySummaryRow(
                title = stringResource(R.string.onboarding_trajectory_label_goal),
                value = goalDate ?: stringResource(R.string.onboarding_trajectory_goal_pending),
                body = stringResource(R.string.onboarding_trajectory_goal_subtitle, goalName),
                icon = Icons.Default.CalendarMonth,
                iconTint = MySharePositive
            )
            TrajectorySummaryRow(
                title = stringResource(R.string.onboarding_trajectory_intensity_title),
                value = contribution,
                body = stringResource(R.string.onboarding_trajectory_intensity_desc),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconTint = MySharePrimary
            )
        }
    }
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
                maxLines = 1,
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
