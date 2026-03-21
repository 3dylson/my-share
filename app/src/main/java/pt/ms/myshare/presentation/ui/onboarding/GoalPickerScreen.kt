package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.GoalType
import java.math.BigDecimal

@Composable
fun GoalPickerScreen(
    initialType: GoalType?,
    initialAmount: BigDecimal?,
    initialLabel: String?,
    onBack: () -> Unit,
    onNext: (GoalType, BigDecimal, String?) -> Unit
) {
    var selected by remember { mutableStateOf(initialType ?: GoalType.EMERGENCY_FUND) }
    var amountText by remember {
        mutableStateOf(
            initialAmount?.toPlainString()
                ?: when (selected) {
                    GoalType.EMERGENCY_FUND -> "3000"
                    GoalType.INVEST_TARGET -> "10000"
                    GoalType.CUSTOM -> "1000"
                }
        )
    }
    var labelText by remember { mutableStateOf(initialLabel ?: "") }

    fun parseAmount(): BigDecimal? = runCatching { BigDecimal(amountText.trim()) }.getOrNull()

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF6F8FA)) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("Back") }
                Spacer(Modifier.weight(1f))
            }

            Text("Choose your first goal", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("We’ll estimate a target date based on your plan.", color = Color(0xFF546E7A))
            Spacer(Modifier.height(20.dp))

            GoalCard(
                title = "Emergency fund",
                subtitle = "Feel safe with a cash buffer.",
                selected = selected == GoalType.EMERGENCY_FUND,
                onClick = {
                    selected = GoalType.EMERGENCY_FUND
                    if (amountText.isBlank()) amountText = "3000"
                }
            )
            Spacer(Modifier.height(12.dp))
            GoalCard(
                title = "Invest €10,000",
                subtitle = "Build wealth consistently.",
                selected = selected == GoalType.INVEST_TARGET,
                onClick = {
                    selected = GoalType.INVEST_TARGET
                    if (amountText.isBlank()) amountText = "10000"
                }
            )
            Spacer(Modifier.height(12.dp))
            GoalCard(
                title = "Save for something",
                subtitle = "Trip, car, deposit — your call.",
                selected = selected == GoalType.CUSTOM,
                onClick = {
                    selected = GoalType.CUSTOM
                    if (amountText.isBlank()) amountText = "1000"
                }
            )

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.replace(',', '.') },
                label = { Text("Goal amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            if (selected == GoalType.CUSTOM) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = { Text("Label (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.weight(1f))

            val amount = parseAmount()
            Button(
                onClick = {
                    onNext(selected, amount ?: BigDecimal.ZERO, labelText.takeIf { it.isNotBlank() })
                },
                enabled = amount != null && amount > BigDecimal.ZERO,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Next", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun GoalCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    else BorderStroke(1.dp, Color(0xFFE0E0E0))
    val bg = if (selected) Color(0xFFE3F2FD) else Color.White

    Card(
        shape = RoundedCornerShape(20.dp),
        border = border,
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Color(0xFF607D8B))
        }
    }
}
