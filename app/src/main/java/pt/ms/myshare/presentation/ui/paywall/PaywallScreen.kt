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
            selectedProduct = availableProducts.first()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(16.dp))
            
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Close, 
                    contentDescription = "Close",
                    tint = MyShareSecondary
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Unlock Unlimited Potential", 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 40.sp,
                color = MyShareOnSurface
            )
            
            Text(
                "Experience the full power of My Share and master your finances with precision.", 
                color = MyShareSecondary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(40.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FeatureRow("Unlimited Intentional Goals")
                FeatureRow("Advanced Rule Presets & Logic")
                FeatureRow("Full 12-Month Trajectory History")
                FeatureRow("Ad-Free, Focused Experience")
                FeatureRow("Priority Cloud Sync")
            }

            Spacer(Modifier.height(48.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MySharePrimary)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    availableProducts.forEach { product ->
                        PremiumPaywallCard(
                            title = product.name,
                            price = product.price,
                            description = if (product.name.contains("Year", ignoreCase = true)) "Best value for long-term growth" else "Flexible monthly commitment",
                            isSelected = selectedProduct?.id == product.id,
                            isTrialEligible = product.id.contains("trial", ignoreCase = true),
                            onClick = { selectedProduct = product }
                        )
                    }
                }

                if (availableProducts.isEmpty()) {
                    Text(
                        "Updating premium plans...",
                        color = MyShareSecondary,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(40.dp))

            selectedProduct?.let { product ->
                PremiumButton(
                    text = "Unlock Premium Now",
                    onClick = { activity?.let { viewModel.purchasePlan(it, product) } }
                )
            } ?: PremiumButton(
                text = "Loading Plans...",
                onClick = {},
                enabled = false
            )

            TextButton(
                onClick = { viewModel.restorePurchases() },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    "Restore Purchases", 
                    style = MaterialTheme.typography.labelLarge,
                    color = MyShareSecondary
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Recurring billing. Cancel anytime in Store settings.",
                style = MaterialTheme.typography.labelSmall,
                color = MyShareSecondary.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
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
        Text(
            text = text, 
            style = MaterialTheme.typography.bodyLarge,
            color = MyShareOnSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
