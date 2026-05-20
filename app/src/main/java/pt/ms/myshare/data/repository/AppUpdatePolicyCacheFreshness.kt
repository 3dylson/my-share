package pt.ms.myshare.data.repository

object AppUpdatePolicyCacheFreshness {
    const val MAX_CACHE_AGE_MILLIS: Long = 7 * 24 * 60 * 60 * 1000L

    fun isFresh(
        cachedAtMillis: Long,
        nowMillis: Long,
        maxAgeMillis: Long = MAX_CACHE_AGE_MILLIS
    ): Boolean {
        if (cachedAtMillis <= 0L) return false
        if (nowMillis < cachedAtMillis) return false
        return nowMillis - cachedAtMillis <= maxAgeMillis
    }
}
