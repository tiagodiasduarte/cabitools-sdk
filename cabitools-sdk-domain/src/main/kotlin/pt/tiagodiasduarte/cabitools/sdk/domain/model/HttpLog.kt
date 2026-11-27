package pt.tiagodiasduarte.cabitools.sdk.domain.model

/** One recorded call as the logs list shows it. [statusCode] is null when the call failed; see [error]. */
public data class HttpLogItem(
    val id: Long,
    val method: String,
    val url: String,
    /** Epoch millis when the call started. */
    val startedAt: Long,
    val durationMs: Long,
    val statusCode: Int?,
    val error: String?,
    /** Response body size in bytes, null when unknown or when there was no response. */
    val responseSize: Long?,
)

/** One recorded call with everything the detail screen shows. */
public data class HttpLogDetail(
    val id: Long,
    val method: String,
    val url: String,
    val startedAt: Long,
    val durationMs: Long,
    val protocol: String?,
    val statusCode: Int?,
    val statusMessage: String?,
    val error: String?,
    val request: HttpMessage,
    val response: HttpMessage,
)

/** The headers and body of a request or response, as stored (redacted, truncated). */
public data class HttpMessage(
    val headers: List<HttpHeader>,
    val contentType: String?,
    /** Null for binary, uncaptured or absent bodies. */
    val body: String?,
    /** Real size in bytes, null when unknown or absent. */
    val bodySize: Long?,
    val bodyTruncated: Boolean,
)
