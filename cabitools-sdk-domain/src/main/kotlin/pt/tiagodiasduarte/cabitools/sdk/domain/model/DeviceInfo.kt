package pt.tiagodiasduarte.cabitools.sdk.domain.model

/** Raw device and OS values as reported by the system. */
public data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidRelease: String,
    val apiLevel: Int,
)
