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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumPaywallCard
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
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
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
            Text(
                "Master Your Share",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MyShareOnSurface,
                letterSpacing = (-1).sp
            )
            
            Text(
                "Unlock the full system and automate your path to financial freedom.",
                style = MaterialTheme.typography.bodyLarge,
                color = MyShareSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                lineHeight = 26.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureRow("Unlimited Intentional Goals", "Build savings for everything that matters.")
                FeatureRow("Live Rule Automation", "Get notified exactly what to do on payday.")
                FeatureRow("Advanced Analytics", "Track your wealth trajectory over 12 months.")
                FeatureRow("Priority Support", "Direct access to our financial design team.")
            }

            Spacer(Modifier.height(48.dp))

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
                        period = period,
                        description = if (isAnnual) "Focus on long-term wealth" else "Start small, expand later",
                        badge = if (isAnnual) "BEST VALUE" else null,
                        isSelected = selectedProduct?.productId == product.productId,
                        onClick = { selectedProduct = product },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            selectedProduct?.let { product ->
                PremiumButton(
                    text = "Upgrade Now",
                    onClick = { activity?.let { viewModel.purchasePlan(it, product) } }
                )
            }

            TextButton(
                onClick = { viewModel.restorePurchases() },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(
                    "Restore Purchases",
                    style = MaterialTheme.typography.labelLarge,
                    color = MyShareSecondary
                )
            }

            Text(
                "Recurring billing. Cancel anytime in Store settings.",
                style = MaterialTheme.typography.labelSmall,
                color = MyShareSecondary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
            )
        }
    }
}

@Composable
private fun FeatureRow(title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MySharePrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(16.dp)
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
