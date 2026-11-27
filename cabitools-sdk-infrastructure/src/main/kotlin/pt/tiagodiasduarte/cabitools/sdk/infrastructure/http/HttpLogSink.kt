package pt.tiagodiasduarte.cabitools.sdk.infrastructure.http

import pt.tiagodiasduarte.cabitools.sdk.infrastructure.local.HttpLogEntity

/** Receives finished calls from the interceptor. Must not block or throw. */
internal fun interface HttpLogSink {
    fun save(log: HttpLogEntity, requestBody: String?, responseBody: String?)
}
