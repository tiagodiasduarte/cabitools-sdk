package pt.tiagodiasduarte.cabitools.sdk.infrastructure.http

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import pt.tiagodiasduarte.cabitools.sdk.domain.model.ContentTypes
import java.nio.charset.Charset

/**
 * What was kept of one body: [text] holds at most `maxContentLength` bytes, decoded, and is null
 * for binary, uncaptured or absent bodies. [size] is the real size in bytes, null when unknown.
 */
internal data class CapturedBody(
    val text: String?,
    val size: Long?,
    val truncated: Boolean,
) {
    companion object {
        val NONE = CapturedBody(text = null, size = null, truncated = false)
    }
}

internal object BodyCapture {

    /** Text bodies are stored as text; anything else (images, protobuf...) only by size. */
    fun isText(contentType: MediaType?): Boolean = ContentTypes.isText(contentType?.toString())

    /** Copies up to [maxContentLength] bytes of a request body. One-shot and duplex bodies are never read. */
    fun request(body: RequestBody?, maxContentLength: Long): CapturedBody = when {
        body == null -> CapturedBody.NONE
        body.isOneShot() || body.isDuplex() ->
            CapturedBody(text = null, size = body.contentLength().takeIf { it >= 0 }, truncated = false)
        else -> {
            val buffer = Buffer().also { body.writeTo(it) }
            fromBuffer(buffer, totalSize = buffer.size, contentType = body.contentType(), maxContentLength)
        }
    }

    /**
     * Builds the captured body from [kept], which holds at most [maxContentLength] bytes of a body
     * whose real size is [totalSize].
     */
    fun fromBuffer(kept: Buffer, totalSize: Long, contentType: MediaType?, maxContentLength: Long): CapturedBody {
        val truncated = totalSize > maxContentLength
        val text = if (isText(contentType)) {
            kept.readString(minOf(kept.size, maxContentLength), contentType.charsetOrUtf8())
        } else {
            null
        }
        return CapturedBody(text = text, size = totalSize, truncated = truncated)
    }

    /**
     * A response body the app closed before reading it to the end: the size is only known from
     * Content-Length, and whatever was read is kept, marked as truncated.
     */
    fun partial(kept: Buffer, knownSize: Long?, contentType: MediaType?, maxContentLength: Long): CapturedBody {
        if (kept.size == 0L) return CapturedBody(text = null, size = knownSize, truncated = false)
        val read = fromBuffer(kept, totalSize = knownSize ?: kept.size, contentType, maxContentLength)
        return read.copy(size = knownSize, truncated = read.text != null)
    }

    private fun MediaType?.charsetOrUtf8(): Charset = this?.charset() ?: Charsets.UTF_8
}
