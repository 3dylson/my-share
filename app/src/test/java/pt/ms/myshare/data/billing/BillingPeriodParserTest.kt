package pt.ms.myshare.data.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BillingPeriodParserTest {

    @Test
    fun `totalDays converts play billing period strings`() {
        assertEquals(7, BillingPeriodParser.totalDays("P1W", 1))
        assertEquals(14, BillingPeriodParser.totalDays("P7D", 2))
        assertEquals(30, BillingPeriodParser.totalDays("P1M", 1))
        assertEquals(365, BillingPeriodParser.totalDays("P1Y", 1))
    }

    @Test
    fun `totalDays rejects malformed or empty periods`() {
        assertNull(BillingPeriodParser.totalDays("monthly", 1))
        assertNull(BillingPeriodParser.totalDays("P", 1))
    }
}
