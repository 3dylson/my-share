package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import pt.ms.myshare.domain.model.PlanPreview
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TrajectoryScreen(
    preview: PlanPreview?,
    goalName: String,
    onNext: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Your projection", style = MaterialTheme.typography.headlineMedium)
            
            preview?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Next payday summary", style = MaterialTheme.typography.titleMedium)
                        Text(it.summary, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Goal: $goalName",
                            style = MaterialTheme.typography.titleSmall
                        )
                        it.goalTargetDate?.let { date ->
                            Text(
                                "On pace for ${date.month.name.lowercase().replaceFirstChar(Char::titlecase)} ${date.year}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Text(
                    "This plan allocates ${currencyFormat.format(it.savingsPerPayday)} towards your future every payday.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } ?: Text("Calculating your trajectory...")

            Text(
                "Free tier: Monitor one goal and payday split. Premium: recurring rules and deeper history.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.weight(1f))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text("Continue")
            }
        }
    }
}
