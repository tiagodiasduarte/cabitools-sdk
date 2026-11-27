package pt.tiagodiasduarte.cabitools.sdk.domain.model

/** One HTTP header line. Kept as a list, not a map, so repeated headers such as `Set-Cookie` survive. */
public data class HttpHeader(
    val name: String,
    val value: String,
)
