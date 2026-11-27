package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Formatting shared by the logs list and detail screens. Sizes use decimal units (1 kB = 1000 B). */
internal class LogFormat(private val timeZone: TimeZone = TimeZone.getDefault()) {

    fun time(epochMillis: Long): String = pattern(TIME).format(Date(epochMillis))

    fun timeWithMillis(epochMillis: Long): String = pattern(TIME_MILLIS).format(Date(epochMillis))

    fun duration(millis: Long): String = "$millis ms"

    fun size(bytes: Long): String = when {
        bytes < KILO -> "$bytes B"
        bytes < KILO * KILO -> String.format(Locale.ROOT, "%.1f kB", bytes / KILO.toDouble())
        else -> String.format(Locale.ROOT, "%.1f MB", bytes / (KILO * KILO).toDouble())
    }

    private fun pattern(pattern: String) =
        SimpleDateFormat(pattern, Locale.ROOT).apply { timeZone = this@LogFormat.timeZone }

    private companion object {
        const val TIME = "HH:mm:ss"
        const val TIME_MILLIS = "HH:mm:ss.SSS"
        const val KILO = 1000L
    }
}
