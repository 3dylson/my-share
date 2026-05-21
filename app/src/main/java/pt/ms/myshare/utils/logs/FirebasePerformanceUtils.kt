package pt.ms.myshare.utils.logs

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import timber.log.Timber

object FirebasePerformanceUtils {

    suspend fun <T> traceSuspend(
        name: String,
        attributes: Map<String, String> = emptyMap(),
        block: suspend (Trace?) -> T
    ): T {
        val trace = createTrace(name, attributes)
        trace?.start()
        return try {
            block(trace)
        } finally {
            trace?.stopSafely(name)
        }
    }

    fun Trace.putMetricSafely(name: String, value: Long) {
        runCatching {
            putMetric(name, value)
        }.onFailure {
            Timber.tag(TAG).e(it, "Failed to put performance metric traceMetric=%s", name)
        }
    }

    private fun createTrace(name: String, attributes: Map<String, String>): Trace? {
        return runCatching {
            FirebasePerformance.getInstance().newTrace(name).apply {
                attributes.forEach { (key, value) ->
                    putAttribute(key, value.take(MAX_ATTRIBUTE_LENGTH))
                }
            }
        }.onFailure {
            Timber.tag(TAG).e(it, "Failed to create performance trace trace=%s", name)
        }.getOrNull()
    }

    private fun Trace.stopSafely(name: String) {
        runCatching {
            stop()
        }.onFailure {
            Timber.tag(TAG).e(it, "Failed to stop performance trace trace=%s", name)
        }
    }

    private const val TAG = "FirebasePerformance"
    private const val MAX_ATTRIBUTE_LENGTH = 100
}
