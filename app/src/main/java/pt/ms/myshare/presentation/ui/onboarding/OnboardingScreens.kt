package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Your personalized plan is ready", style = MaterialTheme.typography.headlineMedium)
            Text(preview.summary, color = MaterialTheme.colorScheme.primary)
            MetricCard(title = "Next payday ${preview.nextPayday.format(dateFormatter)}", value = currency.format(preview.incomePerPayday))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(title = "Fixed costs", value = currency.format(preview.fixedCostsPerPayday), modifier = Modifier.weight(1f))
                MetricCard(title = "Flexible spend", value = currency.format(preview.flexibleSpendPerPayday), modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(title = "Savings", value = currency.format(preview.savingsPerPayday), modifier = Modifier.weight(1f))
                MetricCard(title = "Investing + crypto", value = currency.format(preview.investingPerPayday.add(preview.cryptoPerPayday)), modifier = Modifier.weight(1f))
            }
            MetricCard(title = "Weekly spend guide", value = currency.format(preview.weeklyFlexibleSpend))
            MetricCard(
                title = goalName,
                value = currency.format(goalAmount),
                supporting = preview.goalTargetDate?.let { "At this pace you reach it by ${it.month.name.lowercase().replaceFirstChar(Char::titlecase)} ${it.year}" }
                    ?: "Add more goal contribution later to bring the date forward"
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onAutopilot, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("See premium automation", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            TextButton(
                onClick = onNotNow, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Use the free plan first", fontSize = 16.sp, fontWeight = FontWeight.Medium)
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onClose) { Text("Close") }
                TextButton(onClick = onRestore) { Text("Restore") }
            }
            Text(pricingStrategy.paywallHeadline, style = MaterialTheme.typography.headlineMedium)
            Text(pricingStrategy.paywallSubhead, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HelperCard(title = "Premium gives you", body = "Recurring payday rules, salary-day reminders, weekly reviews, and more than one goal.")
            HelperCard(title = "Trust first", body = "No ads in sensitive financial screens. Cancel in Google Play. Free stays useful with one plan and one goal.")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PaywallPlanCard(
                    title = "Monthly",
                    price = pricingStrategy.monthlyLabel,
                    selected = selectedPlan == BillingPlan.MONTHLY,
                    highlighted = pricingStrategy.heroPlan == BillingPlan.MONTHLY,
                    modifier = Modifier.weight(1f),
                    onClick = { onPlanSelected(BillingPlan.MONTHLY) }
                )
                PaywallPlanCard(
                    title = "Annual",
                    price = pricingStrategy.annualLabel,
                    selected = selectedPlan == BillingPlan.ANNUAL,
                    highlighted = pricingStrategy.heroPlan == BillingPlan.ANNUAL,
                    modifier = Modifier.weight(1f),
                    onClick = { onPlanSelected(BillingPlan.ANNUAL) }
                )
            }
            Text("Free trial: ${pricingStrategy.trialDays} days. Cancel during the trial and you won’t be charged.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { activity?.let(onPurchaseSelected) }, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Start ${pricingStrategy.trialDays}-day trial", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier, supporting: String? = null) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            supporting?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HelperCard(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(body)
        }
    }
}

@Composable
private fun PaywallPlanCard(
    title: String,
    price: String,
    selected: Boolean,
    highlighted: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFD5DDE2)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(price, style = MaterialTheme.typography.titleMedium)
            if (highlighted) {
                FilterChip(selected = true, onClick = {}, label = { Text("Best entry point") })
            }
        }
    }
}
