package pt.tiagodiasduarte.cabitools.sdk

import java.util.concurrent.TimeUnit

/** Marks the HTTP logs configuration DSL, so nested blocks can't reach outer receivers. */
@DslMarker
public annotation class HttpLogsDsl

/** How long recorded HTTP calls are kept. At most 500 logs are kept, whichever limit is reached first. */
public enum class Retention(internal val millis: Long) {
    ONE_HOUR(TimeUnit.HOURS.toMillis(1)),
    ONE_DAY(TimeUnit.DAYS.toMillis(1)),
    ONE_WEEK(TimeUnit.DAYS.toMillis(DAYS_PER_WEEK)),
}

private const val DAYS_PER_WEEK = 7L

/**
 * Configuration of the HTTP logs interceptor, set in code by the integrating developer:
 *
 * ```
 * Cabitools.httpInterceptor(context) {
 *     retention = Retention.ONE_DAY
 *     maxContentLength = 250_000L
 *     redactHeaders("X-Api-Key")
 *     hideHeaders("X-Internal-Trace")
 * }
 * ```
 */
@HttpLogsDsl
public class HttpLogsConfig internal constructor() {

    /** How long logs are kept. Defaults to [Retention.ONE_DAY]. */
    public var retention: Retention = Retention.ONE_DAY

    /** Maximum stored length of each request and response body, in bytes. Defaults to 250,000. */
    public var maxContentLength: Long = DEFAULT_MAX_CONTENT_LENGTH
        set(value) {
            require(value > 0) { "maxContentLength must be positive, was $value" }
            field = value
        }

    /**
     * Whether `Authorization`, `Proxy-Authorization`, `Cookie` and `Set-Cookie` values are
     * replaced by `****` before saving. Defaults to `true`.
     */
    public var redactDefaultHeaders: Boolean = true

    internal val redactedHeaders: MutableSet<String> = mutableSetOf()
    internal val hiddenHeaders: MutableSet<String> = mutableSetOf()

    /** Replaces the values of these headers by `****` before saving. Names are case-insensitive. */
    public fun redactHeaders(vararg names: String) {
        names.mapTo(redactedHeaders) { it.lowercase() }
    }

    /** Doesn't store these headers at all. Names are case-insensitive. */
    public fun hideHeaders(vararg names: String) {
        names.mapTo(hiddenHeaders) { it.lowercase() }
    }
}

private const val DEFAULT_MAX_CONTENT_LENGTH = 250_000L
