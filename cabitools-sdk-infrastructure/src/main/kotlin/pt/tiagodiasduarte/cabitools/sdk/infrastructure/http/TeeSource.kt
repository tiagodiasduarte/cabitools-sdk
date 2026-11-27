package pt.tiagodiasduarte.cabitools.sdk.infrastructure.http

import okio.Buffer
import okio.ForwardingSource
import okio.Source

/**
 * Passes a response body through to the app unchanged, keeping a copy of its first
 * [maxContentLength] bytes and counting its full size. [onDone] runs once, when the body is
 * fully read or closed, with the kept bytes, the number of bytes read, and whether the app read
 * the body to its end.
 */
internal class TeeSource(
    delegate: Source,
    private val maxContentLength: Long,
    private val onDone: (kept: Buffer, bytesRead: Long, complete: Boolean) -> Unit,
) : ForwardingSource(delegate) {

    private val kept = Buffer()
    private var bytesRead = 0L
    private var done = false

    override fun read(sink: Buffer, byteCount: Long): Long {
        val read = super.read(sink, byteCount)
        if (read == -1L) {
            finish(complete = true)
        } else {
            val room = maxContentLength - kept.size
            if (room > 0) sink.copyTo(kept, sink.size - read, minOf(read, room))
            bytesRead += read
        }
        return read
    }

    override fun close() {
        finish(complete = false)
        super.close()
    }

    private fun finish(complete: Boolean) {
        if (done) return
        done = true
        onDone(kept, bytesRead, complete)
    }
}
