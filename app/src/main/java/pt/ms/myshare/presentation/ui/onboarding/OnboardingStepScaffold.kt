package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.PremiumButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingStepScaffold(
    title: String,
    subtitle: String,
    actionText: String,
    actionEnabled: Boolean = true,
    progressStep: Int? = null,
    progressTotal: Int? = null,
    onBack: () -> Unit,
    onAction: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val compactHeight = maxHeight < 700.dp
            val topSpacing = if (compactHeight) 28.dp else 36.dp
            val afterBackSpacing = if (compactHeight) 4.dp else 8.dp
            val afterProgressSpacing = if (compactHeight) 14.dp else 20.dp
            val beforeContentSpacing = if (compactHeight) 20.dp else 28.dp
            val bottomContentPadding = if (compactHeight) 164.dp else 132.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .imeNestedScroll()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(topSpacing))
                IconButton(
                    onClick = onBack,
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Spacer(Modifier.height(afterBackSpacing))

                if (progressStep != null && progressTotal != null) {
                    OnboardingProgressIndicator(
                        stepIndex = progressStep,
                        stepTotal = progressTotal
                    )
                    Spacer(Modifier.height(afterProgressSpacing))
                }

                Text(
                    text = title,
                    style = if (compactHeight) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.headlineMedium
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(beforeContentSpacing))
                content()
                Spacer(Modifier.height(bottomContentPadding))
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding(),
                color = MaterialTheme.colorScheme.background
            ) {
                PremiumButton(
                    text = actionText,
                    onClick = onAction,
                    enabled = actionEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 28.dp)
                )
            }
        }
    }
}
