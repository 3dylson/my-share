package pt.ms.myshare.presentation.ui.formatting

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class LocalizedAmountFormatterTest {

    @Test
    fun `parseAmount accepts comma decimal locales`() {
        val result = LocalizedAmountFormatter.parseAmount("1234,56", Locale.forLanguageTag("pt-PT"))

        assertEquals(BigDecimal("1234.56"), result)
    }

    @Test
    fun `parseAmount accepts grouped comma decimal values`() {
        val result = LocalizedAmountFormatter.parseAmount("1 234,56", Locale.forLanguageTag("fr-FR"))

        assertEquals(BigDecimal("1234.56"), result)
    }

    @Test
    fun `parseAmount accepts Arabic Indic digits`() {
        val result = LocalizedAmountFormatter.parseAmount("١٢٣٤٫٥٦", Locale.forLanguageTag("ar"))

        assertEquals(BigDecimal("1234.56"), result)
    }

    @Test
    fun `formatEditableAmount uses locale decimal separator without grouping`() {
        val result = LocalizedAmountFormatter.formatEditableAmount(BigDecimal("1234.56"), Locale.forLanguageTag("pt-PT"))

        assertEquals("1234,56", result)
    }

    @Test
    fun `amountPlaceholder uses locale decimal separator`() {
        val result = LocalizedAmountFormatter.amountPlaceholder(Locale.forLanguageTag("pt-PT"))

        assertEquals("0,00", result)
    }

    @Test
    fun `formatPercentage uses locale percent pattern`() {
        val result = LocalizedAmountFormatter.formatPercentage(BigDecimal("12.5"), Locale.US)

        assertEquals("12.5%", result)
    }
}
