package pt.tiagodiasduarte.cabitools.sdk.infrastructure.http

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.BufferedSink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BodyCaptureTest {

    @Test
    fun `given text based content types when they are checked then they are stored as text`() {
        // Given
        val types = listOf(
            "text/plain", "application/json", "application/problem+json", "application/xml",
            "application/atom+xml", "application/x-www-form-urlencoded",
        ).map { it.toMediaType() }

        // When
        val results = types.map { BodyCapture.isText(it) } + BodyCapture.isText(null)

        // Then
        assertTrue(results.all { it })
    }

    @Test
    fun `given binary content types when they are checked then they are not stored as text`() {
        // Given
        val types = listOf("image/png", "application/octet-stream", "application/x-protobuf").map { it.toMediaType() }

        // When
        val results = types.map { BodyCapture.isText(it) }

        // Then
        assertFalse(results.any { it })
    }

    @Test
    fun `given a body over the limit when it is captured then the first bytes are kept and the real size recorded`() {
        // Given
        val body = "a".repeat(400_000).toRequestBody("text/plain".toMediaType())

        // When
        val captured = BodyCapture.request(body, maxContentLength = 250_000L)

        // Then
        assertEquals(250_000, captured.text?.length)
        assertEquals(400_000L, captured.size)
        assertTrue(captured.truncated)
    }

    @Test
    fun `given a body within the limit when it is captured then it is kept whole`() {
        // Given
        val body = "{\"id\":42}".toRequestBody("application/json".toMediaType())

        // When
        val captured = BodyCapture.request(body, maxContentLength = 250_000L)

        // Then
        assertEquals(CapturedBody("{\"id\":42}", 9L, truncated = false), captured)
    }

    @Test
    fun `given a png body when it is captured then no text is stored but its size is`() {
        // Given
        val body = ByteArray(12_400).toRequestBody("image/png".toMediaType())

        // When
        val captured = BodyCapture.request(body, maxContentLength = 250_000L)

        // Then
        assertEquals(CapturedBody(text = null, size = 12_400L, truncated = false), captured)
    }

    @Test
    fun `given a latin-1 charset when a body is captured then it is decoded with that charset`() {
        // Given
        val contentType = "text/plain; charset=ISO-8859-1".toMediaType()
        val body = "café".toByteArray(Charsets.ISO_8859_1).toRequestBody(contentType)

        // When
        val captured = BodyCapture.request(body, maxContentLength = 250_000L)

        // Then
        assertEquals("café", captured.text)
    }

    @Test
    fun `given a one-shot request body when it is captured then it is not read`() {
        // Given
        val body = OneShotBody()

        // When
        val captured = BodyCapture.request(body, maxContentLength = 250_000L)

        // Then
        assertEquals(CapturedBody(text = null, size = 5L, truncated = false), captured)
        assertFalse(body.wasWritten)
    }

    @Test
    fun `given a duplex request body when it is captured then it is not read`() {
        // Given
        val body = OneShotBody(duplex = true, length = -1L)

        // When
        val captured = BodyCapture.request(body, maxContentLength = 250_000L)

        // Then
        assertFalse(body.wasWritten)
        assertEquals(CapturedBody(text = null, size = null, truncated = false), captured)
    }

    @Test
    fun `given a body without content type when it is captured then it is decoded as utf-8 text`() {
        // Given
        val body = "olá".toRequestBody(contentType = null)

        // When
        val captured = BodyCapture.request(body, maxContentLength = 250_000L)

        // Then
        assertEquals("olá", captured.text)
    }

    @Test
    fun `given no request body when it is captured then nothing is stored`() {
        // Given
        val body: RequestBody? = null

        // When
        val captured = BodyCapture.request(body, maxContentLength = 250_000L)

        // Then
        assertEquals(CapturedBody.NONE, captured)
    }

    @Test
    fun `given kept bytes of a longer response when the captured body is built then it is marked truncated`() {
        // Given
        val kept = Buffer().writeUtf8("{\"items\":[1,2")

        // When
        val captured = BodyCapture.fromBuffer(kept, totalSize = 50L, "application/json".toMediaType(), 13L)

        // Then
        assertEquals(CapturedBody("{\"items\":[1,2", 50L, truncated = true), captured)
    }

    private class OneShotBody(private val duplex: Boolean = false, private val length: Long = 5L) : RequestBody() {
        var wasWritten = false
        override fun isDuplex() = duplex
        override fun contentType() = "text/plain".toMediaType()
        override fun contentLength() = length
        override fun isOneShot() = !duplex
        override fun writeTo(sink: BufferedSink) {
            wasWritten = true
            sink.writeUtf8("hello")
        }
    }
}
