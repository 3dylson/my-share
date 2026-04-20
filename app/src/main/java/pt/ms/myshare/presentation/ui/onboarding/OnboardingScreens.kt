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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

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
                "Final Blueprint", 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MyShareOnSurface
            )
            Text(
                "Created for your payday on ${preview.nextPayday.format(dateFormatter)}",
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
                        label = "Initial Balance",
                        value = currency.format(preview.incomePerPayday),
                        subtitle = preview.summary
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PremiumMetricCard(
                            label = "Fixed Costs", 
                            value = currency.format(preview.fixedCostsPerPayday), 
                            modifier = Modifier.weight(1f)
                        )
                        PremiumMetricCard(
                            label = "Flexible", 
                            value = currency.format(preview.flexibleSpendPerPayday), 
                            modifier = Modifier.weight(1f),
                            subtitle = "${currency.format(preview.weeklyFlexibleSpend)} / week"
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
                                "Savings Goal", 
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
                                    "target", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MyShareSecondary
                                )
                            }
                            
                            preview.goalTargetDate?.let { date ->
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Estimated: ${date.month.name.lowercase().replaceFirstChar(Char::titlecase)} ${date.year}",
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
                            label = "Investing", 
                            value = currency.format(preview.investingPerPayday), 
                            modifier = Modifier.weight(1f)
                        )
                        PremiumMetricCard(
                            label = "Crypto", 
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
                    text = "Secure My Plan",
                    onClick = onAutopilot
                )
                
                TextButton(
                    onClick = onNotNow, 
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        "Start with basic plan", 
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
                        contentDescription = "Close", 
                        tint = MyShareSecondary,
                        modifier = Modifier.size(48.dp) // Larger icon
                    )
                }
                TextButton(
                    onClick = onRestore,
                    modifier = Modifier.padding(top = 8.dp)
                ) { 
                    Text("Restore", color = MyShareSecondary, style = MaterialTheme.typography.labelLarge) 
                }
            }
            
            Spacer(Modifier.height(8.dp))

            Text(
                pricingStrategy.paywallHeadline, 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                color = MyShareOnSurface,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                pricingStrategy.paywallSubhead, 
                style = MaterialTheme.typography.bodyLarge,
                color = MyShareSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            
            Spacer(Modifier.height(40.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FeatureRowNew("Automation", "Recurring rules for every payday.")
                FeatureRowNew("Reminders", "Never forget to log your major spends.")
                FeatureRowNew("Unlimited Goals", "Track all your dreams simultaneously.")
                FeatureRowNew("Exclusive Deals", "Early access to partner financial tools.")
            }

            Spacer(Modifier.height(48.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumPaywallCard(
                    title = "Monthly",
                    price = pricingStrategy.monthlyLabel,
                    period = "month",
                    description = "Flexible commitment",
                    isSelected = selectedPlan == BillingPlan.MONTHLY,
                    onClick = { onPlanSelected(BillingPlan.MONTHLY) }
                )
                PremiumPaywallCard(
                    title = "Annual",
                    price = pricingStrategy.annualLabel,
                    period = "year",
                    description = "Best value for long-term growth",
                    isSelected = selectedPlan == BillingPlan.ANNUAL,
                    onClick = { onPlanSelected(BillingPlan.ANNUAL) }
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Trial for ${pricingStrategy.trialDays} days. Cancel anytime.", 
                style = MaterialTheme.typography.bodySmall,
                color = MyShareSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))
            
            PremiumButton(
                text = "Upgrade Now",
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
