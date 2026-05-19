package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PaydayAdjustmentRecommendationDirection
import pt.ms.myshare.presentation.ui.theme.MySharePositive
import pt.ms.myshare.presentation.ui.theme.MySharePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumReviewResultBottomSheet(
    result: PremiumReviewResultState,
    onDismissRequest: () -> Unit,
    onReviewAdjustment: (PaydayAdjustmentRecommendationState) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PremiumReviewResultHeader(result = result)

            result.coachingSummary?.let { summary ->
                PremiumReviewResultPatternCard(summary = summary)
            }

            result.recommendation?.let { recommendation ->
                PremiumReviewResultRecommendationCard(
                    recommendation = recommendation,
                    onReviewAdjustment = { onReviewAdjustment(recommendation) }
                )
            }

            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_review_result_done),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PremiumReviewResultHeader(
    result: PremiumReviewResultState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MySharePositive.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = Icons.Default.EventAvailable,
                contentDescription = null,
                tint = MySharePositive,
                modifier = Modifier.padding(10.dp).size(22.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.home_review_result_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MySharePrimary,
                fontWeight = FontWeight.Black
            )
            Text(
                text = stringResource(R.string.home_review_result_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
            )
            Text(
                text = stringResource(
                    R.string.home_review_result_desc,
                    result.savedReviewDateLabel,
                    result.totalReviews
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PremiumReviewResultPatternCard(
    summary: PremiumReviewCoachingSummaryState,
    modifier: Modifier = Modifier
) {
    PremiumReviewResultCard(
        modifier = modifier,
        icon = Icons.Default.QueryStats,
        iconTint = MySharePositive,
        title = stringResource(R.string.home_review_result_pattern_title),
        body = stringResourceByKey(summary.headlineKey)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            summary.metrics.take(3).forEach { metric ->
                PremiumReviewResultMetricRow(metric = metric)
            }
        }
    }
}

@Composable
private fun PremiumReviewResultRecommendationCard(
    recommendation: PaydayAdjustmentRecommendationState,
    onReviewAdjustment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isKeepPlan = recommendation.direction == PaydayAdjustmentRecommendationDirection.KEEP_PLAN
    val title = if (isKeepPlan) {
        stringResource(R.string.home_review_result_keep_title)
    } else {
        stringResource(R.string.home_review_result_adjustment_title)
    }
    val body = if (isKeepPlan) {
        stringResource(R.string.home_review_result_keep_desc)
    } else {
        stringResource(
            R.string.home_review_result_adjustment_desc,
            recommendation.adjustmentAmountLabel
        )
    }

    PremiumReviewResultCard(
        modifier = modifier,
        icon = if (isKeepPlan) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
        iconTint = if (isKeepPlan) MySharePositive else MySharePrimary,
        title = title,
        body = body
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PremiumReviewResultMoveRow(
                label = stringResource(R.string.home_review_result_current_guide),
                flexibleLabel = recommendation.currentFlexibleSpendLabel,
                priorityLabel = recommendation.currentPriorityContributionLabel,
                isRecommended = false
            )
            PremiumReviewResultMoveRow(
                label = stringResource(R.string.home_review_result_next_guide),
                flexibleLabel = recommendation.recommendedFlexibleSpendLabel,
                priorityLabel = recommendation.recommendedPriorityContributionLabel,
                isRecommended = recommendation.isApplyable
            )

            if (recommendation.isApplyable) {
                Button(
                    onClick = onReviewAdjustment,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home_review_result_review_adjustment),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumReviewResultCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, iconTint.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconTint.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.padding(9.dp).size(20.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun PremiumReviewResultMetricRow(
    metric: PremiumReviewCoachingMetricState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResourceByKey(metric.labelKey),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = metric.valueLabel,
                style = MaterialTheme.typography.titleSmall,
                color = if (metric.isPositive) MySharePositive else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PremiumReviewResultMoveRow(
    label: String,
    flexibleLabel: String,
    priorityLabel: String,
    isRecommended: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = if (isRecommended) MySharePrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.Black
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                PremiumReviewResultMoveValue(
                    label = stringResource(R.string.home_review_result_flexible),
                    value = flexibleLabel,
                    modifier = Modifier.weight(1f)
                )
                PremiumReviewResultMoveValue(
                    label = stringResource(R.string.home_review_result_priority),
                    value = priorityLabel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PremiumReviewResultMoveValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun stringResourceByKey(key: String): String {
    val context = LocalContext.current
    return remember(key) {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        if (resId != 0) context.getString(resId) else key
    }
}
