package pt.tiagodiasduarte.cabitools.sdk.domain.model

/**
 * Identifying information about the host app that embeds the SDK.
 *
 * @property versionName `null` when the host app was built without a version name.
 */
public data class AppInfo(
    val applicationId: String,
    val versionName: String?,
    val versionCode: Long,
)
