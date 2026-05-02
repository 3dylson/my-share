package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
@Composable
fun PlanPreviewScreen(
    preview: PlanPreview,
    goalName: String,
    goalAmount: BigDecimal,
    onAutopilot: () -> Unit,
    onNotNow: () -> Unit
) {
    val locale = Locale.getDefault()
    val currency = NumberFormat.getCurrencyInstance(locale)
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM", locale)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(40.dp))
            
            Text(
                stringResource(R.string.onboarding_plan_preview_title), 
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MyShareOnSurface
            )
            Text(
                stringResource(R.string.onboarding_plan_preview_subtitle, preview.nextPayday.format(dateFormatter)),
                style = MaterialTheme.typography.bodyLarge,
                color = MyShareSecondary,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Mission Step 1: Fixed Costs
                item {
                    MissionStepCard(
                        title = stringResource(R.string.onboarding_plan_preview_step_fixed_title),
                        body = stringResource(R.string.onboarding_plan_preview_step_fixed_body, currency.format(preview.fixedCostsPerPayday)),
                        icon = Icons.Default.Security,
                        iconColor = MySharePrimary,
                        amount = currency.format(preview.fixedCostsPerPayday)
                    )
                }

                // Mission Step 2: Goal Contribution
                item {
                    MissionStepCard(
                        title = stringResource(R.string.onboarding_plan_preview_step_goal_title, goalName),
                        body = stringResource(R.string.onboarding_plan_preview_step_goal_body, currency.format(preview.savingsPerPayday), goalName),
                        icon = Icons.Default.Flag,
                        iconColor = MySharePositive,
                        amount = currency.format(preview.savingsPerPayday)
                    )
                }

                // Mission Step 3: Flexible Spending
                item {
                    MissionStepCard(
                        title = stringResource(R.string.onboarding_plan_preview_step_flex_title),
                        body = stringResource(R.string.onboarding_plan_preview_step_flex_body, currency.format(preview.flexibleSpendPerPayday), currency.format(preview.weeklyFlexibleSpend)),
                        icon = Icons.Default.Celebration,
                        iconColor = MyShareWarning,
                        amount = currency.format(preview.flexibleSpendPerPayday)
                    )
                }

                // The Big Picture Impact
                item {
                    preview.goalTargetDate?.let { date ->
                        val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, locale)
                            .lowercase().replaceFirstChar(Char::titlecase)
                        
                        ImpactSummaryCard(
                            title = stringResource(R.string.onboarding_plan_preview_impact_title),
                            body = stringResource(R.string.onboarding_plan_preview_impact_body, monthName, date.year.toString()),
                            reminderText = stringResource(R.string.onboarding_plan_preview_reminder_callout, preview.nextPayday.format(dateFormatter))
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumButton(
                    text = stringResource(R.string.onboarding_plan_preview_button_secure),
                    onClick = onAutopilot
                )
                
                TextButton(
                    onClick = onNotNow, 
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        stringResource(R.string.onboarding_plan_preview_button_basic), 
                        style = MaterialTheme.typography.labelLarge, 
                        color = MyShareSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun PaywallScreen(
    pricingStrategy: pt.ms.myshare.domain.model.PricingStrategy,
    availableProducts: List<pt.ms.myshare.domain.model.StoreProduct> = emptyList(),
    selectedPlan: BillingPlan,
    onPlanSelected: (BillingPlan) -> Unit,
    onClose: () -> Unit,
    onRestore: () -> Unit,
    onPurchaseSelected: (android.app.Activity) -> Unit
) {
    val activity = androidx.activity.compose.LocalActivity.current
    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(80.dp) // Larger hit box
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close, 
                        contentDescription = stringResource(R.string.paywall_close_content_description), 
                        tint = MyShareSecondary,
                        modifier = Modifier.size(48.dp) // Larger icon
                    )
                }
                TextButton(
                    onClick = onRestore,
                    modifier = Modifier.padding(top = 8.dp)
                ) { 
                    Text(stringResource(R.string.paywall_restore_button), color = MyShareSecondary, style = MaterialTheme.typography.labelLarge) 
                }
            }
            
            Spacer(Modifier.height(8.dp))

            val context = LocalContext.current
            val headline = remember(pricingStrategy.paywallHeadline) {
                val resId = context.resources.getIdentifier(pricingStrategy.paywallHeadline, "string", context.packageName)
                if (resId != 0) context.getString(resId) else pricingStrategy.paywallHeadline
            }
            val subhead = remember(pricingStrategy.paywallSubhead) {
                val resId = context.resources.getIdentifier(pricingStrategy.paywallSubhead, "string", context.packageName)
                if (resId != 0) context.getString(resId) else pricingStrategy.paywallSubhead
            }

            Text(
                headline, 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                color = MyShareOnSurface,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                subhead, 
                style = MaterialTheme.typography.bodyLarge,
                color = MyShareSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            
            Spacer(Modifier.height(40.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FeatureRowNew(stringResource(R.string.paywall_feature_automation_title), stringResource(R.string.paywall_feature_automation_desc))
                FeatureRowNew(stringResource(R.string.paywall_feature_reminders_title), stringResource(R.string.paywall_feature_reminders_desc))
                FeatureRowNew(stringResource(R.string.paywall_feature_goals_title), stringResource(R.string.paywall_feature_goals_desc))
                FeatureRowNew(stringResource(R.string.paywall_feature_tracking_title), stringResource(R.string.paywall_feature_tracking_desc))
            }

            Spacer(Modifier.height(48.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val monthlyProduct = availableProducts.find { it.productId.contains("monthly", ignoreCase = true) }
                val annualProduct = availableProducts.find { it.productId.contains("annual", ignoreCase = true) }
                val hasLivePrices = monthlyProduct != null || annualProduct != null

                PremiumPaywallCard(
                    title = stringResource(R.string.paywall_plan_monthly),
                    price = monthlyProduct?.price ?: pricingStrategy.monthlyLabel,
                    period = stringResource(R.string.paywall_period_month),
                    description = stringResource(R.string.paywall_desc_monthly),
                    badge = if (!hasLivePrices) stringResource(R.string.paywall_badge_loading) else null,
                    isSelected = selectedPlan == BillingPlan.MONTHLY,
                    onClick = { onPlanSelected(BillingPlan.MONTHLY) }
                )
                PremiumPaywallCard(
                    title = stringResource(R.string.paywall_plan_annual),
                    price = annualProduct?.price ?: pricingStrategy.annualLabel,
                    period = stringResource(R.string.paywall_period_year),
                    description = stringResource(R.string.paywall_desc_annual),
                    badge = if (hasLivePrices) stringResource(R.string.paywall_badge_best_value) else stringResource(R.string.paywall_badge_loading),
                    isSelected = selectedPlan == BillingPlan.ANNUAL,
                    onClick = { onPlanSelected(BillingPlan.ANNUAL) }
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                stringResource(R.string.paywall_footer_trial, pricingStrategy.trialDays), 
                style = MaterialTheme.typography.bodySmall,
                color = MyShareSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))
            
            PremiumButton(
                text = stringResource(R.string.paywall_upgrade_button),
                onClick = { activity?.let(onPurchaseSelected) }
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureRowNew(title: String, body: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle, 
            contentDescription = null, 
            tint = MySharePrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MyShareOnSurface)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MyShareSecondary)
        }
    }
}

@Composable
fun MissionStepCard(
    title: String,
    body: String,
    icon: ImageVector,
    iconColor: Color,
    amount: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MyShareSecondary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MyShareOnSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MyShareSecondary,
                    lineHeight = 20.sp
                )
            }

            Text(
                amount,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MyShareOnSurface
            )
        }
    }
}

@Composable
fun ImpactSummaryCard(
    title: String,
    body: String,
    reminderText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MySharePrimaryContainer.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MySharePrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyLarge,
                color = MyShareOnSurface,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    reminderText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MySharePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
