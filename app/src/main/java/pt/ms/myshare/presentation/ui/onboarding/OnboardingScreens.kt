package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import pt.ms.myshare.R

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
                style = MaterialTheme.typography.headlineLarge,
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
                item {
                    PremiumMetricCard(
                        label = stringResource(R.string.onboarding_plan_preview_label_initial),
                        value = currency.format(preview.incomePerPayday),
                        subtitle = preview.summary
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PremiumMetricCard(
                            label = stringResource(R.string.onboarding_plan_preview_label_fixed), 
                            value = currency.format(preview.fixedCostsPerPayday), 
                            modifier = Modifier.weight(1f)
                        )
                        PremiumMetricCard(
                            label = stringResource(R.string.onboarding_plan_preview_label_flexible), 
                            value = currency.format(preview.flexibleSpendPerPayday), 
                            modifier = Modifier.weight(1f),
                            subtitle = stringResource(R.string.onboarding_plan_preview_weekly_subtitle, currency.format(preview.weeklyFlexibleSpend))
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MySharePrimaryContainer.copy(alpha = 0.5f)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Text(
                                stringResource(R.string.onboarding_plan_preview_label_goal), 
                                style = MaterialTheme.typography.labelMedium, 
                                color = MySharePrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                goalName, 
                                style = MaterialTheme.typography.headlineSmall, 
                                fontWeight = FontWeight.ExtraBold,
                                color = MyShareOnSurface
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    currency.format(goalAmount), 
                                    style = MaterialTheme.typography.titleLarge, 
                                    fontWeight = FontWeight.Bold,
                                    color = MyShareOnSurface
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.onboarding_plan_preview_target_label), 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MyShareSecondary
                                )
                            }
                            
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.onboarding_plan_preview_allocation_body, currency.format(preview.savingsPerPayday)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MyShareSecondary
                            )
                            
                            preview.goalTargetDate?.let { date ->
                                Spacer(Modifier.height(8.dp))
                                val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, locale)
                                    .lowercase().replaceFirstChar(Char::titlecase)
                                Text(
                                    stringResource(R.string.onboarding_plan_preview_estimated_date, monthName, date.year.toString()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MySharePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PremiumMetricCard(
                            label = stringResource(R.string.onboarding_plan_preview_label_investing), 
                            value = currency.format(preview.investingPerPayday), 
                            modifier = Modifier.weight(1f)
                        )
                        PremiumMetricCard(
                            label = stringResource(R.string.onboarding_plan_preview_label_crypto), 
                            value = currency.format(preview.cryptoPerPayday), 
                            modifier = Modifier.weight(1f)
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
