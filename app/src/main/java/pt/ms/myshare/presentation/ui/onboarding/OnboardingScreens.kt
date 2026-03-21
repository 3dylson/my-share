package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PlanPreview
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PlanPreviewScreen(
    planPreview: PlanPreview?,
    goalAmount: BigDecimal?,
    currencySymbol: String = "€",
    onSliderChange: (Int) -> Unit,
    sliderValue: Int,
    monthsSooner: Int?,
    onAutopilot: () -> Unit,
    onNotNow: () -> Unit
) {
    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text("Here’s your plan", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(formattedDate, fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        // Card A: This payday
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("This payday", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                PaydayRow(iconRes = R.drawable.ic_baseline_show_chart, label = "Stocks", amount = planPreview?.perPaydayAmounts?.stocks, currencySymbol = currencySymbol)
                PaydayRow(iconRes = R.drawable.ic_baseline_currency_bitcoin, label = "Crypto", amount = planPreview?.perPaydayAmounts?.crypto, currencySymbol = currencySymbol)
                PaydayRow(iconRes = R.drawable.savings_48px, label = "Savings", amount = planPreview?.perPaydayAmounts?.savings, currencySymbol = currencySymbol)
            }
        }

        // Card B: Goal timeline
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Your goal timeline", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                if (goalAmount != null && planPreview?.goalTargetDate != null) {
                    val ym = planPreview.goalTargetDate
                    Text("Reach $currencySymbol${goalAmount.toPlainString()} by ${ym.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${ym.year}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("Set a goal to see your timeline", fontSize = 18.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(16.dp))
                TimelineProgressBar()
            }
        }

        // Interactive teaser: slider
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Invest a bit more", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("+$currencySymbol$sliderValue", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    Slider(
                        value = sliderValue.toFloat(),
                        onValueChange = { onSliderChange(it.toInt()) },
                        valueRange = 0f..200f,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (monthsSooner != null && monthsSooner > 0) {
                    Text("Reach your goal $monthsSooner months sooner", fontSize = 14.sp, color = Color(0xFF4CAF50))
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        // CTAs
        Button(
            onClick = onAutopilot,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Turn on Autopilot", fontSize = 20.sp)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = onNotNow,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Not now", fontSize = 18.sp)
        }
    }
}

@Composable
private fun PaydayRow(icon: String, label: String, amount: BigDecimal?, currencySymbol: String) {
    // Deprecated signature kept to avoid breaking previews; use the iconRes overload.
    PaydayRow(iconRes = null, label = label, amount = amount, currencySymbol = currencySymbol, fallbackText = icon)
}

@Composable
private fun PaydayRow(iconRes: Int?, label: String, amount: BigDecimal?, currencySymbol: String, fallbackText: String = "") {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = Color(0xFF37474F),
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(fallbackText.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 18.sp, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Crossfade(targetState = amount) { value ->
            Text(
                "$currencySymbol${value?.setScale(2, RoundingMode.HALF_UP)?.toPlainString() ?: "-"}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.animateContentSize()
            )
        }
    }
}

@Composable
private fun TimelineProgressBar() {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
        ) {
            // Visual only, no progress logic
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(0.08f).fillMaxHeight().background(Color(0xFF4CAF50), RoundedCornerShape(6.dp)))
                Spacer(Modifier.weight(0.92f))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("1m", fontSize = 12.sp, color = Color.Gray)
            Text("3m", fontSize = 12.sp, color = Color.Gray)
            Text("6m", fontSize = 12.sp, color = Color.Gray)
            Text("12m", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PaywallScreen(
    annualPrice: String,
    monthlyPrice: String,
    trialAvailable: Boolean,
    selectedPlan: PaywallPlan,
    onPlanSelected: (PaywallPlan) -> Unit,
    onClose: () -> Unit,
    onRestore: () -> Unit,
    onPurchaseSelected: (PaywallPlan) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF6F8FA)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(0.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Text("✕", fontSize = 22.sp, color = Color.Gray)
                }
                IconButton(onClick = onRestore) {
                    Text("Restore", fontSize = 16.sp, color = Color(0xFF1976D2))
                }
            }
            Spacer(Modifier.height(16.dp))
            // Headline
            Text(
                "Autopilot your plan",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Stay consistent. Hit your goal faster.",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(24.dp))
            // Bullets
            Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                PaywallBullet("Payday reminders with exact amounts", "💡")
                PaywallBullet("Goal timeline + progress history", "📈")
                PaywallBullet("Save strategies (custom categories & splits)", "🗂️")
            }
            Spacer(Modifier.height(32.dp))
            // Plans
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PaywallPlanCard(
                    plan = PaywallPlan.Annual,
                    price = annualPrice,
                    selected = selectedPlan == PaywallPlan.Annual,
                    recommended = true,
                    modifier = Modifier.weight(1f),
                    onClick = { onPlanSelected(PaywallPlan.Annual) }
                )
                PaywallPlanCard(
                    plan = PaywallPlan.Monthly,
                    price = monthlyPrice,
                    selected = selectedPlan == PaywallPlan.Monthly,
                    recommended = false,
                    modifier = Modifier.weight(1f),
                    onClick = { onPlanSelected(PaywallPlan.Monthly) }
                )
            }
            Spacer(Modifier.height(32.dp))
            // CTA
            Button(
                onClick = { onPurchaseSelected(selectedPlan) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp)
            ) {
                Text(if (trialAvailable) "Start free trial" else "Continue", fontSize = 20.sp)
            }
            Spacer(Modifier.height(16.dp))
            // Disclosure
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Text(
                    "Annual: $annualPrice billed yearly. Monthly: $monthlyPrice billed monthly. Auto-renewal applies. Cancel anytime in Google Play. Subscription is not required for basic calculator use.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

enum class PaywallPlan { Annual, Monthly }

@Composable
private fun PaywallBullet(text: String, icon: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(icon, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
        Text(text, fontSize = 16.sp)
    }
}

@Composable
private fun PaywallPlanCard(
    plan: PaywallPlan,
    price: String,
    selected: Boolean,
    recommended: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (selected) Color(0xFF1976D2) else Color(0xFFE0E0E0)
    val bgColor = if (selected) Color(0xFFE3F2FD) else Color.White
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(plan.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(price, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (recommended) {
                Spacer(Modifier.height(8.dp))
                Text("Recommended", fontSize = 12.sp, color = Color(0xFF1976D2), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun OnboardingFlow(
    viewModel: OnboardingViewModel,
    entitlementRepository: pt.ms.myshare.domain.repository.EntitlementRepository,
    showPaywall: Boolean,
    onShowPaywall: () -> Unit,
    onGoToReminderSetup: () -> Unit
) {
    // Deprecated: the real onboarding flow is implemented via NavGraph in OnboardingNavGraph.kt
}

@Composable
fun SettingsScreen(
    remindersEnabled: Boolean,
    onToggleReminders: (Boolean) -> Unit
) {
    // Deprecated: settings live in the existing preferences screen.
}
