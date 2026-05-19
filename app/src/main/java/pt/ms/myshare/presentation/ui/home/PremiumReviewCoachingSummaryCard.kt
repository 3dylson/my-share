package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.PremiumReviewCoachingStatus
import pt.ms.myshare.presentation.ui.theme.MySharePositive
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import pt.ms.myshare.presentation.ui.theme.MyShareWarning

@Composable
fun PremiumReviewCoachingSummaryCard(
    summary: PremiumReviewCoachingSummaryState,
    modifier: Modifier = Modifier
) {
    val accentColor = summary.status.accentColor()
    val headline = stringResourceByKey(summary.headlineKey)
    val body = stringResourceByKey(summary.bodyKey)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.24f))
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
                    color = accentColor.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = summary.status.icon(),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .padding(9.dp)
                            .size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResourceByKey("home_review_coaching_summary_label").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
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

            PremiumReviewCoachingMetricGrid(
                metrics = summary.metrics,
                accentColor = accentColor
            )
        }
    }
}

@Composable
private fun PremiumReviewCoachingMetricGrid(
    metrics: List<PremiumReviewCoachingMetricState>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val shouldStack = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.2f
        if (shouldStack) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.forEach { metric ->
                    PremiumReviewCoachingMetricRow(
                        metric = metric,
                        accentColor = accentColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.forEach { metric ->
                    PremiumReviewCoachingMetricTile(
                        metric = metric,
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumReviewCoachingMetricTile(
    metric: PremiumReviewCoachingMetricState,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 76.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResourceByKey(metric.labelKey),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = metric.valueLabel,
                style = MaterialTheme.typography.titleSmall,
                color = if (metric.isPositive) accentColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PremiumReviewCoachingMetricRow(
    metric: PremiumReviewCoachingMetricState,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
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
                color = if (metric.isPositive) accentColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun PremiumReviewCoachingStatus.accentColor(): Color {
    return when (this) {
        PremiumReviewCoachingStatus.STRONG -> MySharePositive
        PremiumReviewCoachingStatus.STEADY -> MySharePrimary
        PremiumReviewCoachingStatus.NEEDS_ATTENTION -> MyShareWarning
    }
}

private fun PremiumReviewCoachingStatus.icon(): ImageVector {
    return when (this) {
        PremiumReviewCoachingStatus.STRONG -> Icons.Default.CheckCircle
        PremiumReviewCoachingStatus.STEADY -> Icons.Default.QueryStats
        PremiumReviewCoachingStatus.NEEDS_ATTENTION -> Icons.Default.Warning
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
