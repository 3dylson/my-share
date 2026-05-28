package pt.ms.myshare.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.presentation.ui.localization.UiText
import pt.ms.myshare.presentation.ui.localization.resolve

data class PremiumPaywallProofItem(
    val label: String,
    val title: String,
    val body: String,
    val icon: ImageVector = Icons.Default.CheckCircle
)

data class PremiumPaywallPlanOption(
    val plan: BillingPlan,
    val title: String,
    val price: String,
    val period: String,
    val badge: String? = null,
    val comparisonPrice: String? = null,
    val savingsLabel: String? = null,
    val isSelected: Boolean = false
)

data class PremiumPaywallRecommendationPreview(
    val label: String,
    val title: String,
    val body: String,
    val currentFlexibleLabel: String,
    val recommendedFlexibleLabel: String,
    val currentPriorityLabel: String,
    val recommendedPriorityLabel: String,
    val flexibleMetricLabel: String,
    val nextFlexibleMetricLabel: String,
    val priorityMetricLabel: String,
    val nextPriorityMetricLabel: String,
    val confidenceLabel: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumPaywallBottomSheet(
    onDismissRequest: () -> Unit,
    onUpgradeClick: () -> Unit,
    title: String? = null,
    body: String? = null,
    recommendationPreview: PremiumPaywallRecommendationPreview? = null,
    proofItems: List<PremiumPaywallProofItem> = emptyList(),
    planOptions: List<PremiumPaywallPlanOption> = emptyList(),
    checkoutTerms: String? = null,
    currencyNotice: String? = null,
    upgradeButtonText: String? = null,
    onPlanSelected: (BillingPlan) -> Unit = {},
    isBillingActionInProgress: Boolean = false,
    billingMessage: UiText? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val resolvedTitle = title ?: stringResource(R.string.premium_gate_general_title)
    val resolvedBody = body ?: stringResource(R.string.premium_gate_general_body)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = stringResource(R.string.content_description_premium_feature),
                modifier = Modifier
                    .size(52.dp)
                    .padding(bottom = 12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = resolvedTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = resolvedBody,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            recommendationPreview?.let { preview ->
                PremiumPaywallRecommendationCard(
                    preview = preview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )
            }

            proofItems.forEachIndexed { index, item ->
                PremiumPaywallProofRow(
                    item = item,
                    modifier = Modifier.fillMaxWidth()
                )
                if (index != proofItems.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            if (planOptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                planOptions.forEachIndexed { index, option ->
                    PremiumPaywallPlanRow(
                        option = option,
                        onClick = { onPlanSelected(option.plan) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (index != planOptions.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            if (billingMessage != null) {
                val resolvedBillingMessage = remember(billingMessage, context) {
                    billingMessage.resolve(context)
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = resolvedBillingMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            checkoutTerms?.let { terms ->
                Text(
                    text = terms,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
            }

            currencyNotice?.let { notice ->
                Text(
                    text = notice,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )
            }

            PremiumPrimaryButton(
                text = if (isBillingActionInProgress) {
                    stringResource(R.string.paywall_upgrade_loading)
                } else {
                    upgradeButtonText ?: stringResource(R.string.paywall_upgrade_button)
                },
                onClick = {
                    if (!isBillingActionInProgress) {
                        onUpgradeClick()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.dialog_not_now), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PremiumPaywallRecommendationCard(
    preview: PremiumPaywallRecommendationPreview,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = preview.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = preview.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = preview.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            PremiumPaywallMetricGrid(preview = preview)
            preview.confidenceLabel?.let { confidence ->
                Text(
                    text = confidence,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PremiumPaywallMetricGrid(
    preview: PremiumPaywallRecommendationPreview,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val metrics = listOf(
            preview.flexibleMetricLabel to preview.currentFlexibleLabel,
            preview.nextFlexibleMetricLabel to preview.recommendedFlexibleLabel,
            preview.priorityMetricLabel to preview.currentPriorityLabel,
            preview.nextPriorityMetricLabel to preview.recommendedPriorityLabel
        )
        val shouldStack = maxWidth < 320.dp || LocalDensity.current.fontScale >= 1.25f

        if (shouldStack) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.forEach { metric ->
                    PremiumPaywallMetricPill(
                        label = metric.first,
                        value = metric.second,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.chunked(2).forEach { rowMetrics ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowMetrics.forEach { metric ->
                            PremiumPaywallMetricPill(
                                label = metric.first,
                                value = metric.second,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumPaywallMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
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
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun PremiumPaywallProofRow(
    item: PremiumPaywallProofItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = item.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PremiumPaywallPlanRow(
    option: PremiumPaywallPlanOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (option.isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    }
    val backgroundColor = if (option.isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(if (option.isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (option.isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (option.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    option.badge?.let { badge ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                option.comparisonPrice?.let { comparison ->
                    Text(
                        text = comparison,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
                option.savingsLabel?.let { savings ->
                    Text(
                        text = savings,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = option.price,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = option.period,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
