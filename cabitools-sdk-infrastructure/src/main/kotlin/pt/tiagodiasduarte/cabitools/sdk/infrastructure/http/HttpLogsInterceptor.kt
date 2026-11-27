package pt.tiagodiasduarte.cabitools.sdk.infrastructure.http

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.buffer
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.local.HttpLogEntity
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Records every call that goes through it, without changing what the app sends or receives.
 * A call is saved when its response body is fully read or closed, or right away when it fails.
 */
internal class HttpLogsInterceptor(
    private val sink: HttpLogSink,
    private val headerRules: HeaderRules,
    private val maxContentLength: Long,
    private val clock: () -> Long = System::currentTimeMillis,
    private val nanoTime: () -> Long = System::nanoTime,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startedAt = clock()
        val startNanos = nanoTime()
        val requestBody = BodyCapture.request(request.body, maxContentLength)
        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            sink.save(failedLog(request, startedAt, elapsedMs(startNanos), requestBody, e), requestBody.text, null)
            throw e
        }
        val durationMs = elapsedMs(startNanos)
        val body = response.body
        if (body == null || body.contentLength() == 0L || response.isEncoded()) {
            val responseBody = CapturedBody(text = null, size = body?.contentLength()?.takeIf { it >= 0 }, false)
            sink.save(log(request, response, startedAt, durationMs, requestBody, responseBody), requestBody.text, null)
            return response
        }
        val contentType = body.contentType()
        val knownSize = body.contentLength().takeIf { it >= 0 }
        val tee = TeeSource(body.source(), maxContentLength) { kept, bytesRead, complete ->
            val responseBody = if (complete) {
                BodyCapture.fromBuffer(kept, totalSize = bytesRead, contentType, maxContentLength)
            } else {
                BodyCapture.partial(kept, knownSize, contentType, maxContentLength)
            }
            sink.save(
                log(request, response, startedAt, durationMs, requestBody, responseBody),
                requestBody.text,
                responseBody.text,
            )
        }
        return response.newBuilder()
            .body(tee.buffer().asResponseBody(contentType, body.contentLength()))
            .build()
    }

    /** A body the app asked to receive compressed (its own Accept-Encoding) can't be shown as text. */
    private fun Response.isEncoded(): Boolean =
        header("Content-Encoding")?.let { !it.equals("identity", ignoreCase = true) } ?: false

    private fun elapsedMs(startNanos: Long) = TimeUnit.NANOSECONDS.toMillis(nanoTime() - startNanos)

    private fun log(
        request: Request,
        response: Response,
        startedAt: Long,
        durationMs: Long,
        requestBody: CapturedBody,
        responseBody: CapturedBody,
    ) = baseLog(request, startedAt, durationMs, requestBody).copy(
        protocol = response.protocol.toString(),
        responseCode = response.code,
        responseMessage = response.message,
        responseHeaders = headerRules.apply(response.headers),
        responseContentType = response.body?.contentType()?.toString(),
        responseBodySize = responseBody.size,
        responseBodyTruncated = responseBody.truncated,
    )

    private fun failedLog(
        request: Request,
        startedAt: Long,
        durationMs: Long,
        requestBody: CapturedBody,
        error: IOException,
    ) = baseLog(request, startedAt, durationMs, requestBody).copy(
        error = listOfNotNull(error::class.java.simpleName, error.message).joinToString(": "),
    )

    private fun baseLog(request: Request, startedAt: Long, durationMs: Long, requestBody: CapturedBody) =
        HttpLogEntity(
            method = request.method,
            url = request.url.toString(),
            requestedAt = startedAt,
            durationMs = durationMs,
            protocol = null,
            responseCode = null,
            responseMessage = null,
            error = null,
            requestHeaders = headerRules.apply(request.headers),
            requestContentType = request.body?.contentType()?.toString(),
            requestBodySize = requestBody.size,
            requestBodyTruncated = requestBody.truncated,
            responseHeaders = emptyList(),
            responseContentType = null,
            responseBodySize = null,
            responseBodyTruncated = false,
        )
}
