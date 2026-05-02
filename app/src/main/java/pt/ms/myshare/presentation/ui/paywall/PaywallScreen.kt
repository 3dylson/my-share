package pt.ms.myshare.presentation.ui.paywall

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumPaywallCard
import pt.ms.myshare.presentation.ui.components.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import pt.ms.myshare.presentation.ui.theme.*

@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    onPurchased: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val isPro by viewModel.isPro.collectAsState(initial = false)
    val availableProducts by viewModel.availableProducts.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val activity = androidx.activity.compose.LocalActivity.current
    val scrollState = rememberScrollState()

    var selectedProduct by remember { mutableStateOf<pt.ms.myshare.domain.model.StoreProduct?>(null) }

    LaunchedEffect(isPro) {
        if (isPro) {
            onPurchased()
        }
    }

    LaunchedEffect(availableProducts) {
        if (selectedProduct == null && availableProducts.isNotEmpty()) {
            selectedProduct = availableProducts.find { it.name.contains("Year", ignoreCase = true) } ?: availableProducts.first()
        }
    }

    Scaffold(
        containerColor = MyShareBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.paywall_close_content_description),
                        tint = MyShareSecondary
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            PremiumAppHeader(
                title = stringResource(R.string.paywall_title),
                subtitle = stringResource(R.string.paywall_subtitle)
            )
            
            Spacer(Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FeatureRow(stringResource(R.string.paywall_feature_goals_title), stringResource(R.string.paywall_feature_goals_desc))
                FeatureRow(stringResource(R.string.paywall_feature_automation_title), stringResource(R.string.paywall_feature_automation_desc))
                FeatureRow(stringResource(R.string.paywall_feature_reminders_title), stringResource(R.string.paywall_feature_reminders_desc))
                FeatureRow(stringResource(R.string.paywall_feature_tracking_title), stringResource(R.string.paywall_feature_tracking_desc))
            }

            Spacer(Modifier.height(48.dp))

            PremiumSectionHeader(title = "Choose Your Membership")

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MySharePrimary)
                }
            } else {
                availableProducts.forEach { product ->
                    val isAnnual = product.name.contains("Year", ignoreCase = true)
                    val period = if (isAnnual) "year" else "month"
                    PremiumPaywallCard(
                        title = product.name,
                        price = product.price,
                        period = if (isAnnual) stringResource(R.string.paywall_period_year) else stringResource(R.string.paywall_period_month),
                        description = if (isAnnual) stringResource(R.string.paywall_desc_annual) else stringResource(R.string.paywall_desc_monthly),
                        badge = if (isAnnual) stringResource(R.string.paywall_badge_best_value) else null,
                        isSelected = selectedProduct?.productId == product.productId,
                        onClick = { selectedProduct = product },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            selectedProduct?.let { product ->
                PremiumButton(
                    text = stringResource(R.string.paywall_upgrade_button),
                    onClick = { activity?.let { viewModel.purchasePlan(it, product) } }
                )
            }

            TextButton(
                onClick = { viewModel.restorePurchases() },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(
                    stringResource(R.string.paywall_restore_button),
                    style = MaterialTheme.typography.labelLarge,
                    color = MyShareSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                "Recurring billing. Cancel anytime in Store settings.",
                style = MaterialTheme.typography.labelSmall,
                color = MyShareSecondary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
            )
        }
    }
}

@Composable
private fun FeatureRow(title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MySharePrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MyShareOnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MyShareSecondary
            )
        }
    }
}
