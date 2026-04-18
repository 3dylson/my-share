package pt.ms.myshare.presentation.ui.paywall

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.ms.myshare.domain.model.StoreProduct

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

    LaunchedEffect(isPro) {
        if (isPro) {
            onPurchased()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onBack) { Text("Close") }
            
            Text("Unlock Full Control", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Get the most out of My Share by unlocking premium features.", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureRow("Unlimited Goals & Tracking")
                FeatureRow("Multiple Rules & Presets")
                FeatureRow("Deeper Review History (Last 12mo)")
                FeatureRow("100% Ad-Free Experience")
            }

            Spacer(Modifier.weight(1f))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                availableProducts.firstOrNull()?.let { product ->
                    Button(
                        onClick = { activity?.let { viewModel.purchasePlan(it, product) } },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Subscribe for ${product.price} / mo",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } ?: run {
                    Text(
                        "No premium plans available right now.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                TextButton(
                    onClick = { viewModel.restorePurchases() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Restore Purchases")
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("• ", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
