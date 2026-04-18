package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pt.ms.myshare.presentation.ui.theme.*

@Composable
fun BuildingPlanScreen(onBuilt: () -> Unit) {
    var step1Visible by remember { mutableStateOf(false) }
    var step2Visible by remember { mutableStateOf(false) }
    var step3Visible by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        delay(400)
        step1Visible = true
        progress = 0.33f
        delay(700)
        step2Visible = true
        progress = 0.66f
        delay(700)
        step3Visible = true
        progress = 1.0f
        delay(1200)
        onBuilt()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = MySharePrimary,
                trackColor = MySharePrimaryContainer,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(56.dp))
            
            Text(
                "Finalizing Blueprint",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MyShareOnSurface
            )
            
            Text(
                "Personalizing your logic and safety nets based on your journey.",
                style = MaterialTheme.typography.bodyLarge,
                color = MyShareSecondary,
                lineHeight = 24.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                AnimatedStep(visible = step1Visible, text = "Aligning with your goals")
                AnimatedStep(visible = step2Visible, text = "Structuring fixed costs")
                AnimatedStep(visible = step3Visible, text = "Calculating weekly safe-to-spend")
            }
        }
    }
}

@Composable
private fun AnimatedStep(visible: Boolean, text: String) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 20 }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(20.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MyShareOnSurface
            )
        }
    }
}
