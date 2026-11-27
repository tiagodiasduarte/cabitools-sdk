package pt.tiagodiasduarte.cabitools.sdk.infrastructure.mapper

import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogItem
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpMessage
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.local.HttpLogSummary
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.local.HttpLogWithBodies

/** Maps stored HTTP logs (Room rows) to the domain models. */
internal fun HttpLogSummary.toItem() = HttpLogItem(
    id = id,
    method = method,
    url = url,
    startedAt = requestedAt,
    durationMs = durationMs,
    statusCode = responseCode,
    error = error,
    responseSize = responseBodySize,
)

internal fun HttpLogWithBodies.toDetail() = HttpLogDetail(
    id = log.id,
    method = log.method,
    url = log.url,
    startedAt = log.requestedAt,
    durationMs = log.durationMs,
    protocol = log.protocol,
    statusCode = log.responseCode,
    statusMessage = log.responseMessage,
    error = log.error,
    request = HttpMessage(
        headers = log.requestHeaders,
        contentType = log.requestContentType,
        body = bodies?.requestBody,
        bodySize = log.requestBodySize,
        bodyTruncated = log.requestBodyTruncated,
    ),
    response = HttpMessage(
        headers = log.responseHeaders,
        contentType = log.responseContentType,
        body = bodies?.responseBody,
        bodySize = log.responseBodySize,
        bodyTruncated = log.responseBodyTruncated,
    ),
)
