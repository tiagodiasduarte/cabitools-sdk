package pt.tiagodiasduarte.cabitools.sdk.domain.mapper

import java.util.Locale
import pt.tiagodiasduarte.cabitools.sdk.domain.model.DeviceDetails
import pt.tiagodiasduarte.cabitools.sdk.domain.model.DeviceInfo

/** Turns the raw device values into what the Device section shows. */
internal fun DeviceInfo.toDetails(): DeviceDetails = DeviceDetails(
    model = displayModel(manufacturer, model),
    androidVersion = "$androidRelease (API $apiLevel)",
)

private fun displayModel(manufacturer: String, model: String): String {
    val maker = manufacturer.trim()
    val name = model.trim()
    return when {
        maker.isEmpty() -> name
        name.startsWith(maker, ignoreCase = true) -> name
        else -> "${maker.replaceFirstChar { it.titlecase(Locale.ROOT) }} $name"
    }
}
