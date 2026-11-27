package pt.tiagodiasduarte.cabitools.sdk.domain.model

/** Device information formatted for display. */
public data class DeviceDetails(
    /** Manufacturer and model, e.g. `Samsung SM-S918B`; the manufacturer is never repeated. */
    val model: String,
    /** Android release and API level, e.g. `15 (API 35)`. */
    val androidVersion: String,
)
