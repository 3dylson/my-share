package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.PlanPreview
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

import pt.ms.myshare.presentation.ui.components.PremiumMetricCard
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard

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
                .padding(24.dp)
        ) {
            Text(
                "Your Blueprint", 
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black
            )
            Text(
                "Created for your payday on ${preview.nextPayday.format(dateFormatter)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PremiumMetricCard(
                        title = "Initial Balance",
                        value = currency.format(preview.incomePerPayday),
                        supportingText = preview.summary,
                        isPrimary = true
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PremiumMetricCard(
                            title = "Fixed Costs", 
                            value = currency.format(preview.fixedCostsPerPayday), 
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                        )
                        PremiumMetricCard(
                            title = "Flexible", 
                            value = currency.format(preview.flexibleSpendPerPayday), 
                            modifier = Modifier.weight(1f),
                            supportingText = "${currency.format(preview.weeklyFlexibleSpend)} / week"
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Savings Goal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    Text(goalName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }
                                Text(currency.format(goalAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Text(
                                "Monthly contribution: ${currency.format(preview.savingsPerPayday)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            preview.goalTargetDate?.let { date ->
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Estimated: ${date.month.name.lowercase().replaceFirstChar(Char::titlecase)} ${date.year}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PremiumMetricCard(
                            title = "Investing", 
                            value = currency.format(preview.investingPerPayday), 
                            modifier = Modifier.weight(1f)
                        )
                        PremiumMetricCard(
                            title = "Crypto", 
                            value = currency.format(preview.cryptoPerPayday), 
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = onAutopilot, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Secure my plan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            TextButton(
                onClick = onNotNow, 
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Start with basic plan", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onClose) {
                    Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close")
                }
                TextButton(onClick = onRestore) { 
                    Text("Restore", style = MaterialTheme.typography.labelLarge) 
                }
            }
            
            Spacer(Modifier.height(16.dp))

            Text(
                pricingStrategy.paywallHeadline, 
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                pricingStrategy.paywallSubhead, 
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FeatureRow("Automation", "Recurring rules for every payday.")
                FeatureRow("Reminders", "Never forget to log your major spends.")
                FeatureRow("Goals", "Track unlimited goals simultaneously.")
                FeatureRow("Deals", "Early access to partner financial tools.")
            }

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PremiumChoiceCard(
                    text = "Monthly",
                    supportingText = pricingStrategy.monthlyLabel,
                    selected = selectedPlan == BillingPlan.MONTHLY,
                    modifier = Modifier.weight(1f),
                    onClick = { onPlanSelected(BillingPlan.MONTHLY) }
                )
                PremiumChoiceCard(
                    text = "Annual",
                    supportingText = pricingStrategy.annualLabel,
                    selected = selectedPlan == BillingPlan.ANNUAL,
                    modifier = Modifier.weight(1f),
                    onClick = { onPlanSelected(BillingPlan.ANNUAL) }
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Trial for ${pricingStrategy.trialDays} days. Cancel anytime.", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.weight(1f))
            
            Button(
                onClick = { activity?.let(onPurchaseSelected) }, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Upgrade Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FeatureRow(title: String, body: String) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Icon(
            androidx.compose.material.icons.Icons.Default.Check, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.primary,
            modifier = androidx.compose.ui.Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

