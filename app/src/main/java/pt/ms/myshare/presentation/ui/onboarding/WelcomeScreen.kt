package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(64.dp))
            
            Text(
                "Build your payday plan in exactly 60 seconds.",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 44.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "No bank sync. No heavy setup. Start with simple numbers and get a clear split for your next salary.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(48.dp))

            // Premium Visual Anchor
            androidx.compose.foundation.background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ).let { 
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(it)
                        .padding(20.dp)
                ) {
                    Text(
                        "Designed for Intention",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Give every dollar a job before you even spend it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.weight(1f))
            
            Button(
                onClick = onContinue, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onSkip, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Explore the app first", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}
