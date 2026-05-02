package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.theme.*

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    // Dev-only: use a lambda parameter so release builds pass a no-op
    onSkipDev: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // Premium Visual Anchor - Glow effect
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(32.dp),
                color = MySharePrimaryContainer,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(48.dp))

            Text(
                stringResource(R.string.onboarding_welcome_title), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 42.sp,
                color = MyShareOnSurface
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                stringResource(R.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MyShareSecondary,
                modifier = Modifier.padding(horizontal = 8.dp),
                lineHeight = 26.sp
            )

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                PremiumButton(
                    text = stringResource(R.string.onboarding_welcome_button_get_started),
                    onClick = onContinue
                )

                // Only shown in debug builds; compiled away in release
                if (onSkipDev != null) {
                    TextButton(onClick = onSkipDev) {
                        Text(
                            stringResource(R.string.onboarding_welcome_button_skip_dev), 
                            color = MyShareSecondary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

