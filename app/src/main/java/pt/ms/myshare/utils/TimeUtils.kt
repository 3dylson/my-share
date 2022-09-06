package pt.ms.myshare.utils

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.*

object TimeUtils {

    /** Format a time to show month, day and year, e.g. "May 7, 1999" */
    fun monthDayAndYear(startTime: TemporalAccessor): String {
        //TODO
        return DateTimeFormatter.ofPattern("MMMM d, yyyy").format(startTime).replaceFirstChar { it.uppercase() }
    }
}