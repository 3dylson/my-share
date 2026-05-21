package pt.ms.myshare.presentation.ui.formatting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class AllocationAmountConverterTest {

    @Test
    fun `fixedAmountToPercentage converts fixed amount against available income`() {
        val result = AllocationAmountConverter.fixedAmountToPercentage(
            amount = BigDecimal("200"),
            availableAmount = BigDecimal("600")
        )

        assertEquals(BigDecimal("33.33"), result)
    }

    @Test
    fun `percentageToFixedAmount converts percentage against available income`() {
        val result = AllocationAmountConverter.percentageToFixedAmount(
            percentage = BigDecimal("25"),
            availableAmount = BigDecimal("600")
        )

        assertEquals(BigDecimal("150.00"), result)
    }

    @Test
    fun `conversions return null when available amount is zero`() {
        assertNull(
            AllocationAmountConverter.fixedAmountToPercentage(
                amount = BigDecimal("200"),
                availableAmount = BigDecimal.ZERO
            )
        )
        assertNull(
            AllocationAmountConverter.percentageToFixedAmount(
                percentage = BigDecimal("25"),
                availableAmount = BigDecimal.ZERO
            )
        )
    }
}
