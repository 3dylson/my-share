package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Savings
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.theme.MySharePositive
import pt.ms.myshare.presentation.ui.theme.MySharePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaydayRecommendationConfirmationBottomSheet(
    recommendation: PaydayAdjustmentRecommendationState,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
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
            RecommendationSheetHeader(
                icon = Icons.Default.AutoAwesome,
                iconTint = MySharePrimary,
                label = stringResource(R.string.home_review_recommendation_premium_unlocked_label),
                title = stringResource(R.string.home_review_recommendation_confirm_title),
                body = stringResource(R.string.home_review_recommendation_confirm_desc)
            )

            RecommendationBeforeAfterSummary(recommendation = recommendation)

            RecommendationEvidenceRow(recommendation = recommendation)

            Button(
                onClick = onConfirm,
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
                    text = stringResource(R.string.home_review_recommendation_apply),
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.dialog_not_now),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaydayRecommendationAppliedBottomSheet(
    onDismissRequest: () -> Unit,
    onReviewRules: () -> Unit,
    onUndo: () -> Unit,
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
            RecommendationSheetHeader(
                icon = Icons.Default.CheckCircle,
                iconTint = MySharePositive,
                label = stringResource(R.string.home_review_recommendation_premium_unlocked_label),
                title = stringResource(R.string.home_review_recommendation_applied_title),
                body = stringResource(R.string.home_review_recommendation_applied_desc)
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.25f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RecommendationReviewRulesButton(onClick = onReviewRules, modifier = Modifier.fillMaxWidth())
                        RecommendationUndoButton(onClick = onUndo, modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RecommendationUndoButton(onClick = onUndo, modifier = Modifier.weight(1f))
                        RecommendationReviewRulesButton(onClick = onReviewRules, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationSheetHeader(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    label: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = iconTint.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(10.dp).size(22.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MySharePrimary,
                fontWeight = FontWeight.Black
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
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

@Composable
private fun RecommendationBeforeAfterSummary(
    recommendation: PaydayAdjustmentRecommendationState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val shouldStack = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.25f
        if (shouldStack) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RecommendationSummaryPanel(
                    title = stringResource(R.string.home_review_recommendation_confirm_current),
                    flexibleLabel = recommendation.currentFlexibleSpendLabel,
                    priorityLabel = recommendation.currentPriorityContributionLabel,
                    icon = Icons.Default.RadioButtonUnchecked,
                    modifier = Modifier.fillMaxWidth()
                )
                RecommendationSummaryPanel(
                    title = stringResource(R.string.home_review_recommendation_confirm_after),
                    flexibleLabel = recommendation.recommendedFlexibleSpendLabel,
                    priorityLabel = recommendation.recommendedPriorityContributionLabel,
                    icon = Icons.Default.CheckCircle,
                    isRecommended = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RecommendationSummaryPanel(
                    title = stringResource(R.string.home_review_recommendation_confirm_current),
                    flexibleLabel = recommendation.currentFlexibleSpendLabel,
                    priorityLabel = recommendation.currentPriorityContributionLabel,
                    icon = Icons.Default.RadioButtonUnchecked,
                    modifier = Modifier.weight(1f)
                )
                RecommendationSummaryPanel(
                    title = stringResource(R.string.home_review_recommendation_confirm_after),
                    flexibleLabel = recommendation.recommendedFlexibleSpendLabel,
                    priorityLabel = recommendation.recommendedPriorityContributionLabel,
                    icon = Icons.Default.CheckCircle,
                    isRecommended = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RecommendationSummaryPanel(
    title: String,
    flexibleLabel: String,
    priorityLabel: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isRecommended: Boolean = false
) {
    val accent = if (isRecommended) MySharePrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (isRecommended) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isRecommended) MySharePrimary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    fontWeight = FontWeight.Black
                )
            }
            RecommendationSummaryLine(
                label = stringResource(R.string.home_review_recommendation_confirm_flexible),
                value = flexibleLabel
            )
            RecommendationSummaryLine(
                label = stringResource(R.string.home_review_recommendation_confirm_priority),
                value = priorityLabel
            )
        }
    }
}

@Composable
private fun RecommendationSummaryLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun RecommendationEvidenceRow(
    recommendation: PaydayAdjustmentRecommendationState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = pluralStringResource(
                    R.plurals.home_review_recommendation_confirm_evidence_count,
                    recommendation.analyzedReviewCount,
                    recommendation.confidencePercent,
                    recommendation.analyzedReviewCount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RecommendationReviewRulesButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.home_review_recommendation_review_rules),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RecommendationUndoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Undo,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.home_review_recommendation_undo),
            fontWeight = FontWeight.Bold
        )
    }
}
