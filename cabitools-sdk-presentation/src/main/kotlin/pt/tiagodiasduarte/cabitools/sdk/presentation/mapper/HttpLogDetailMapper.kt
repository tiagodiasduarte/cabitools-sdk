package pt.tiagodiasduarte.cabitools.sdk.presentation.mapper

import pt.tiagodiasduarte.cabitools.sdk.domain.model.ContentTypes
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpMessage
import pt.tiagodiasduarte.cabitools.sdk.presentation.R
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.BodyUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.JsonPrettyPrinter
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.LogDetailUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.LogFormat
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.LogShareText
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.MessageUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.OverviewRowUi

private const val NONE = "-"

/** Maps a recorded call to the log detail screen: overview rows, request, response and share text. */
internal fun HttpLogDetail.toUi(format: LogFormat) = LogDetailUi(
    title = "$method ${url.toPath()}",
    overview = listOfNotNull(
        OverviewRowUi(R.string.cabitools_detail_url, url),
        OverviewRowUi(R.string.cabitools_detail_method, method),
        OverviewRowUi(R.string.cabitools_detail_status, status()),
        OverviewRowUi(R.string.cabitools_detail_started, format.timeWithMillis(startedAt)),
        OverviewRowUi(R.string.cabitools_detail_duration, format.duration(durationMs)),
        OverviewRowUi(R.string.cabitools_detail_protocol, protocol ?: NONE),
        OverviewRowUi(R.string.cabitools_detail_request_size, request.bodySize?.let(format::size) ?: NONE),
        OverviewRowUi(R.string.cabitools_detail_response_size, response.bodySize?.let(format::size) ?: NONE),
        error?.let { OverviewRowUi(R.string.cabitools_detail_error, it) },
    ),
    request = request.toUi(format),
    response = response.toUi(format),
    shareText = LogShareText.build(this, format),
)

private fun HttpLogDetail.status(): String =
    // HTTP/2 responses have no reason phrase, so the message is often blank.
    listOfNotNull(statusCode, statusMessage?.takeIf { it.isNotBlank() }).joinToString(" ").ifEmpty { NONE }

private fun HttpMessage.toUi(format: LogFormat) = MessageUi(headers = headers, body = bodyUi(format))

private fun HttpMessage.bodyUi(format: LogFormat): BodyUi {
    // Local copies: HttpMessage comes from the domain module, so its properties can't be smart-cast.
    val body = body
    val size = bodySize?.takeIf { it > 0 }
    return when {
        body != null -> BodyUi.Text(
            text = if (ContentTypes.isJson(contentType)) JsonPrettyPrinter.format(body) else body,
            truncated = bodyTruncated,
        )
        bodySize == null && contentType != null -> BodyUi.NotCaptured(size = null)
        size == null -> BodyUi.Empty
        ContentTypes.isText(contentType) -> BodyUi.NotCaptured(format.size(size))
        else -> BodyUi.Binary(format.size(size))
    }
}

/** Path and query, so the title stays short; the full URL is on the Overview tab. */
private fun String.toPath(): String = substringAfter("://").let { rest ->
    rest.indexOf('/').takeIf { it >= 0 }?.let { rest.substring(it) } ?: "/"
}
