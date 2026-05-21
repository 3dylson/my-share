package pt.ms.myshare.presentation.ui.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class CurrencyPickerCatalogTest {

    @Test
    fun `options keep high confidence currencies and remove obsolete accounting units`() {
        val codes = CurrencyPickerCatalog.options(Locale.US).map { it.code }

        assertTrue(codes.containsAll(listOf("USD", "EUR", "GBP", "BRL", "INR", "AED")))
        assertFalse(codes.contains("AFA"))
        assertFalse(codes.contains("ALK"))
        assertFalse(codes.contains("XUA"))
    }

    @Test
    fun `options are returned in product priority order`() {
        val codes = CurrencyPickerCatalog.options(Locale.US).map { it.code }

        assertEquals(listOf("USD", "EUR", "GBP"), codes.take(3))
    }
}
