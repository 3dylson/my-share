package pt.ms.myshare.presentation.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.SupportedLanguage
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import java.util.Currency
import java.util.Locale

@Composable
fun LanguagePickerDialog(
    selectedLanguageTag: String,
    onDismissRequest: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.preferences_language_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                UserPreferences.supportedLanguages.forEach { language ->
                    LanguageRow(
                        language = language,
                        selected = language.languageTag == selectedLanguageTag,
                        onClick = { onLanguageSelected(language.languageTag) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_close))
            }
        }
    )
}

@Composable
fun CurrencyPickerDialog(
    selectedCurrencyCode: String,
    locale: Locale,
    onDismissRequest: () -> Unit,
    onCurrencySelected: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val currencies = remember(locale) {
        Currency.getAvailableCurrencies()
            .map { currency ->
                CurrencyOption(
                    code = currency.currencyCode,
                    symbol = currency.getSymbol(locale),
                    displayName = currency.getDisplayName(locale)
                )
            }
            .sortedWith(compareBy<CurrencyOption> { it.displayName }.thenBy { it.code })
    }
    val filtered = remember(query, currencies) {
        val normalized = query.trim()
        if (normalized.isBlank()) {
            currencies
        } else {
            currencies.filter {
                it.code.contains(normalized, ignoreCase = true) ||
                    it.displayName.contains(normalized, ignoreCase = true) ||
                    it.symbol.contains(normalized, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.preferences_currency_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(R.string.preferences_currency_search_hint)) }
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(filtered, key = { it.code }) { currency ->
                        CurrencyRow(
                            option = currency,
                            selected = currency.code == selectedCurrencyCode,
                            onClick = { onCurrencySelected(currency.code) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_close))
            }
        }
    )
}

@Composable
private fun LanguageRow(
    language: SupportedLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    PreferenceSelectionRow(
        title = language.displayName,
        subtitle = language.languageTag,
        selected = selected,
        onClick = onClick
    )
}

@Composable
private fun CurrencyRow(
    option: CurrencyOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    PreferenceSelectionRow(
        title = "${option.code} · ${option.displayName}",
        subtitle = option.symbol,
        selected = selected,
        onClick = onClick
    )
}

@Composable
private fun PreferenceSelectionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.content_description_selected),
                    tint = MySharePrimary
                )
            }
        }
    }
}

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val displayName: String
)

fun languageLabel(languageTag: String): String {
    return UserPreferences.supportedLanguages.firstOrNull { it.languageTag == languageTag }?.displayName
        ?: Locale.forLanguageTag(languageTag).displayName
}

fun currencyLabel(currencyCode: String, locale: Locale): String {
    val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull() ?: return currencyCode
    return "${currency.currencyCode} · ${currency.getDisplayName(locale)} (${LocalizedAmountFormatter.currencySymbol(locale, currency.currencyCode)})"
}
