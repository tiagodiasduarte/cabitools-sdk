package pt.tiagodiasduarte.cabitools.sdk.infrastructure.http

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
import okio.GzipSink
import okio.buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.local.HttpLogEntity
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class HttpLogsInterceptorTest {

    private val server = MockWebServer()
    private val saved = mutableListOf<SavedLog>()
    private val sink = HttpLogSink { log, request, response -> saved += SavedLog(log, request, response) }
    private var nanos = 0L

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `given a successful call when its body is read then the log has method url status protocol and sizes`() {
        // Given
        server.enqueue(
            MockResponse().setResponseCode(201).setHeader("Content-Type", "application/json").setBody("{\"id\":1}"),
        )
        val client = client()

        // When
        client.newCall(post("/orders", "{\"item\":7}")).execute().use { it.body?.string() }

        // Then
        val log = saved.single().log
        assertEquals("POST", log.method)
        assertEquals(server.url("/orders").toString(), log.url)
        assertEquals(1_000L, log.requestedAt)
        assertEquals(40L, log.durationMs)
        assertEquals(201, log.responseCode)
        assertEquals("http/1.1", log.protocol)
        assertEquals(10L, log.requestBodySize)
        assertEquals(8L, log.responseBodySize)
        assertEquals("application/json", log.responseContentType)
        assertEquals("{\"item\":7}", saved.single().requestBody)
        assertEquals("{\"id\":1}", saved.single().responseBody)
    }

    @Test
    fun `given a response over the limit when the app reads it then the app gets it all and the copy is truncated`() {
        // Given
        val body = "x".repeat(1_000)
        server.enqueue(MockResponse().setHeader("Content-Type", "text/plain").setBody(body))
        val client = client(maxContentLength = 100L)

        // When
        val received = client.newCall(get("/big")).execute().use { it.body?.string() }

        // Then
        assertEquals(body, received)
        assertEquals("x".repeat(100), saved.single().responseBody)
        assertEquals(1_000L, saved.single().log.responseBodySize)
        assertTrue(saved.single().log.responseBodyTruncated)
    }

    @Test
    fun `given an authorization header when the call is made then the server gets the real value and the log stars`() {
        // Given
        server.enqueue(MockResponse().setBody("ok"))
        val client = client()

        // When
        client.newCall(get("/me", authorization = "Bearer abc123")).execute().use { it.body?.string() }

        // Then
        assertEquals("Bearer abc123", server.takeRequest().getHeader("Authorization"))
        assertTrue(HttpHeader("Authorization", "****") in saved.single().log.requestHeaders)
        assertTrue(saved.single().log.requestHeaders.none { "abc123" in it.value })
    }

    @Test
    fun `given a server that does not answer when the call times out then the error is logged and still thrown`() {
        // Given
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val client = client { readTimeout(200, TimeUnit.MILLISECONDS) }

        // When
        val error = assertThrows(SocketTimeoutException::class.java) { client.newCall(get("/slow")).execute() }

        // Then
        val log = saved.single().log
        assertNull(log.responseCode)
        assertEquals("SocketTimeoutException: ${error.message}", log.error)
        assertNull(saved.single().responseBody)
    }

    @Test
    fun `given the app closes the body without reading it when the call ends then the log is still saved`() {
        // Given
        server.enqueue(MockResponse().setHeader("Content-Type", "text/plain").setBody("unread"))
        val client = client()

        // When
        client.newCall(get("/skip")).execute().close()

        // Then
        assertEquals(6L, saved.single().log.responseBodySize)
        assertNull(saved.single().responseBody)
    }

    @Test
    fun `given a body of unknown length closed without reading when the call ends then its size is unknown`() {
        // Given
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setChunkedBody("{\"id\":1}", 4))
        val client = client()

        // When
        client.newCall(get("/unread")).execute().close()

        // Then
        assertNull(saved.single().log.responseBodySize)
        assertNull(saved.single().responseBody)
        assertEquals(false, saved.single().log.responseBodyTruncated)
    }

    @Test
    fun `given the app reads part of a body then closes it when the call ends then the read part is kept truncated`() {
        // Given
        server.enqueue(MockResponse().setHeader("Content-Type", "text/plain").setBody("abcdefghij"))
        val client = client()

        // When
        client.newCall(get("/partial")).execute().use { it.body?.source()?.readUtf8(4) }

        // Then
        assertEquals(10L, saved.single().log.responseBodySize)
        assertTrue(saved.single().responseBody.orEmpty().startsWith("abcd"))
        assertTrue(saved.single().log.responseBodyTruncated)
    }

    @Test
    fun `given a response without body when the call ends then it is logged right away`() {
        // Given
        server.enqueue(MockResponse().setResponseCode(204))
        val client = client()

        // When
        val response = client.newCall(get("/empty")).execute()

        // Then
        assertEquals(204, saved.single().log.responseCode)
        response.close()
        assertEquals(1, saved.size)
    }

    @Test
    fun `given a chunked response of unknown length when it is read then the size is the bytes received`() {
        // Given
        server.enqueue(MockResponse().setHeader("Content-Type", "text/plain").setChunkedBody("a".repeat(300), 64))
        val client = client()

        // When
        client.newCall(get("/chunked")).execute().use { it.body?.string() }

        // Then
        assertEquals(300L, saved.single().log.responseBodySize)
        assertEquals("a".repeat(300), saved.single().responseBody)
    }

    @Test
    fun `given the app asked for gzip itself when the response arrives then the body is not stored as text`() {
        // Given
        val gzipped = Buffer().also { buffer -> GzipSink(buffer).buffer().use { it.writeUtf8("{\"id\":1}") } }
        server.enqueue(MockResponse().setHeader("Content-Encoding", "gzip").setBody(gzipped))
        val client = client()

        // When
        client.newCall(Request.Builder().url(server.url("/gz")).header("Accept-Encoding", "gzip").build())
            .execute().use { it.body?.bytes() }

        // Then
        assertNull(saved.single().responseBody)
        assertEquals(200, saved.single().log.responseCode)
    }

    @Test
    fun `given a chunked response over the limit when it is read then only the first bytes are kept`() {
        // Given
        server.enqueue(MockResponse().setHeader("Content-Type", "text/plain").setChunkedBody("b".repeat(1_000), 64))
        val client = client(maxContentLength = 100L)

        // When
        val received = client.newCall(get("/chunked-big")).execute().use { it.body?.string() }

        // Then
        assertEquals(1_000, received?.length)
        assertEquals("b".repeat(100), saved.single().responseBody)
        assertEquals(1_000L, saved.single().log.responseBodySize)
    }

    @Test
    fun `given a request body without content type when the call is made then the request content type is empty`() {
        // Given
        server.enqueue(MockResponse().setBody("ok"))
        val client = client()

        // When
        client.newCall(Request.Builder().url(server.url("/raw")).post("raw".toRequestBody(null)).build())
            .execute().use { it.body?.string() }

        // Then
        assertNull(saved.single().log.requestContentType)
        assertEquals("raw", saved.single().requestBody)
    }

    @Test
    fun `given a second client without the interceptor when it makes a call then nothing is logged`() {
        // Given
        server.enqueue(MockResponse().setBody("ok"))
        val otherClient = OkHttpClient()

        // When
        otherClient.newCall(get("/other")).execute().use { it.body?.string() }

        // Then
        assertEquals(emptyList<SavedLog>(), saved)
    }

    private fun client(
        maxContentLength: Long = 250_000L,
        configure: OkHttpClient.Builder.() -> Unit = {},
    ): OkHttpClient {
        val clock = { 1_000L }
        val nanoTime = { nanos.also { nanos += TimeUnit.MILLISECONDS.toNanos(40) } }
        val rules = HeaderRules(redactDefaultHeaders = true, redactedHeaders = emptySet(), hiddenHeaders = emptySet())
        val interceptor = HttpLogsInterceptor(sink, rules, maxContentLength, clock, nanoTime)
        return OkHttpClient.Builder().addInterceptor(interceptor).apply(configure).build()
    }

    private fun get(path: String, authorization: String? = null) = Request.Builder().url(server.url(path))
        .apply { authorization?.let { header("Authorization", it) } }
        .build()

    private fun post(path: String, json: String) = Request.Builder().url(server.url(path))
        .post(json.toRequestBody("application/json".toMediaType()))
        .build()

    private data class SavedLog(val log: HttpLogEntity, val requestBody: String?, val responseBody: String?)
}
