package pt.tiagodiasduarte.cabitools.sdk

/** Marks the HTTP logs configuration DSL, so nested blocks can't reach outer receivers. */
@DslMarker
public annotation class HttpLogsDsl

/** How long recorded HTTP calls are kept. The no-op records nothing, so this has no effect. */
public enum class Retention {
    ONE_HOUR,
    ONE_DAY,
    ONE_WEEK,
}

/**
 * Configuration of the HTTP logs interceptor. In `:sdk-no-op` the options are accepted, so host
 * code compiles unchanged, but they have no effect because nothing is recorded.
 */
@HttpLogsDsl
public class HttpLogsConfig internal constructor() {

    /** Accepted for API compatibility; has no effect. */
    public var retention: Retention = Retention.ONE_DAY

    /** Accepted for API compatibility; has no effect. Must be positive, like in `:sdk`. */
    public var maxContentLength: Long = DEFAULT_MAX_CONTENT_LENGTH
        set(value) {
            require(value > 0) { "maxContentLength must be positive, was $value" }
            field = value
        }

    /** Accepted for API compatibility; has no effect. */
    public var redactDefaultHeaders: Boolean = true

    /** Accepted for API compatibility; has no effect. */
    @Suppress("UNUSED_PARAMETER", "UnusedParameter")
    public fun redactHeaders(vararg names: String) {
        // Intentionally empty.
    }

    /** Accepted for API compatibility; has no effect. */
    @Suppress("UNUSED_PARAMETER", "UnusedParameter")
    public fun hideHeaders(vararg names: String) {
        // Intentionally empty.
    }
}

private const val DEFAULT_MAX_CONTENT_LENGTH = 250_000L
