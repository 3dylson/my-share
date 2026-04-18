package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun BuildingPlanScreen(onBuilt: () -> Unit) {
    var step1Visible by remember { mutableStateOf(false) }
    var step2Visible by remember { mutableStateOf(false) }
    var step3Visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400)
        step1Visible = true
        delay(700)
        step2Visible = true
        delay(700)
        step3Visible = true
        delay(1200)
        onBuilt()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Building your personalized salary plan...",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            AnimatedVisibility(
                visible = step1Visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 40 }
            ) {
                LoadingStep(text = "Aligning with your goals")
            }
            
            AnimatedVisibility(
                visible = step2Visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 40 }
            ) {
                LoadingStep(text = "Structuring fixed costs")
            }
            
            AnimatedVisibility(
                visible = step3Visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 40 }
            ) {
                LoadingStep(text = "Calculating weekly safe-to-spend")
            }
        }
    }
}

@Composable
private fun LoadingStep(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
