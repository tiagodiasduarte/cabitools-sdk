package pt.tiagodiasduarte.cabitools.sdk

import android.content.ContextWrapper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NoOpHttpInterceptorTest {

    private val server = MockWebServer()
    private val context = ContextWrapper(null)

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `given the no-op interceptor when a call is made then the response passes through unchanged`() {
        // Given
        server.enqueue(MockResponse().setResponseCode(201).setHeader("X-Test", "yes").setBody("hello"))
        val interceptor = Cabitools.httpInterceptor(context) {
            retention = Retention.ONE_HOUR
            redactHeaders("Authorization")
            hideHeaders("X-Trace")
        }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        // When
        val response = client.newCall(
            Request.Builder().url(server.url("/orders")).header("Authorization", "Bearer abc").build(),
        ).execute()

        // Then
        assertEquals(201, response.code)
        assertEquals("yes", response.header("X-Test"))
        assertEquals("hello", response.body?.string())
        assertEquals("Bearer abc", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `given a non-positive max content length when the no-op interceptor is created then it throws`() {
        // Given
        val configure: HttpLogsConfig.() -> Unit = { maxContentLength = -1 }

        // When
        val error = assertThrows(IllegalArgumentException::class.java) {
            Cabitools.httpInterceptor(context, configure)
        }

        // Then
        assertEquals("maxContentLength must be positive, was -1", error.message)
    }

    @Test
    fun `given the no-op entry point when open is called then nothing happens`() {
        // Given
        val caller = context

        // When
        Cabitools.open(caller)

        // Then
        assertEquals(null, caller.baseContext)
    }
}
