package pt.tiagodiasduarte.cabitools.sdk.presentation.model

import androidx.navigation3.runtime.NavKey

/** The screens of the Cabitools Activity, used as Navigation 3 back stack keys. */
internal sealed interface CabitoolsRoute : NavKey {
    data object Settings : CabitoolsRoute
    data object Logs : CabitoolsRoute
    data class LogDetail(val id: Long) : CabitoolsRoute

    companion object {
        private const val LOG_DETAIL_PREFIX = "log:"

        /** A stable string for saving the back stack across configuration changes and process death. */
        fun encode(route: CabitoolsRoute): String = when (route) {
            Settings -> "settings"
            Logs -> "logs"
            is LogDetail -> "$LOG_DETAIL_PREFIX${route.id}"
        }

        fun decode(value: String): CabitoolsRoute = when {
            value == "logs" -> Logs
            value.startsWith(LOG_DETAIL_PREFIX) -> LogDetail(value.removePrefix(LOG_DETAIL_PREFIX).toLong())
            else -> Settings
        }
    }
}
