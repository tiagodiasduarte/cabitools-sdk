package pt.tiagodiasduarte.cabitools.sdk.infrastructure.http

import okhttp3.Headers
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader

/**
 * Applies the developer's header rules before a log is saved: hidden headers are dropped and
 * redacted ones keep their name with the value `****`. Names match case-insensitively.
 */
public data class HeaderRules(
    public val redactDefaultHeaders: Boolean,
    public val redactedHeaders: Set<String>,
    public val hiddenHeaders: Set<String>,
) {
    private val redacted: Set<String> =
        (if (redactDefaultHeaders) DEFAULT_REDACTED else emptySet()) + redactedHeaders.map { it.lowercase() }
    private val hidden: Set<String> = hiddenHeaders.map { it.lowercase() }.toSet()

    internal fun apply(headers: Headers): List<HttpHeader> = (0 until headers.size).mapNotNull { index ->
        val name = headers.name(index)
        when (name.lowercase()) {
            in hidden -> null
            in redacted -> HttpHeader(name, REDACTED)
            else -> HttpHeader(name, headers.value(index))
        }
    }

    internal companion object {
        const val REDACTED = "****"
        val DEFAULT_REDACTED = setOf("authorization", "proxy-authorization", "cookie", "set-cookie")
    }
}
