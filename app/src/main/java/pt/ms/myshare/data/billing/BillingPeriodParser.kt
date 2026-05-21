package pt.ms.myshare.data.billing

internal object BillingPeriodParser {
    private val periodPattern = Regex("""^P(?:(\d+)Y)?(?:(\d+)M)?(?:(\d+)W)?(?:(\d+)D)?$""")

    fun totalDays(period: String, billingCycleCount: Int): Int? {
        val match = periodPattern.matchEntire(period) ?: return null
        val years = match.groupValues[1].toIntOrNull() ?: 0
        val months = match.groupValues[2].toIntOrNull() ?: 0
        val weeks = match.groupValues[3].toIntOrNull() ?: 0
        val days = match.groupValues[4].toIntOrNull() ?: 0
        val totalDays = years * 365 + months * 30 + weeks * 7 + days
        if (totalDays <= 0) return null
        return totalDays * billingCycleCount.coerceAtLeast(1)
    }
}
