package pt.ms.myshare.presentation.ui.edit_profile

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class CurrencyVisualTransformation(private val locale: Locale = Locale.getDefault()) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text

        if (originalText.isEmpty()) {
            val formattedText = NumberFormat.getCurrencyInstance(locale).format(BigDecimal.ZERO)
            val offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return formattedText.length
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return 0
                }
            }
            return TransformedText(AnnotatedString(formattedText), offsetMapping)
        }

        // The viewModel stores the raw digits, e.g., "12345"
        val amount = originalText.toBigDecimalOrNull() ?: BigDecimal.ZERO

        // Format the number as currency, e.g., "$123.45"
        val formattedText = NumberFormat.getCurrencyInstance(locale).format(amount.divide(BigDecimal(100)))

        // The offset mapping is crucial for cursor position. A simple but effective
        // implementation is to always place the cursor at the end of the formatted text.
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return formattedText.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return originalText.length
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
