package pt.tiagodiasduarte.cabitools.sdk

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.HttpLogs

@RunWith(AndroidJUnit4::class)
class CabitoolsHttpInterceptorTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val server = MockWebServer()

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `given a configured interceptor when a call is made then it is recorded with the configured redaction`() =
        runTest {
            // Given
            server.enqueue(MockResponse().setBody("ok"))
            val interceptor = Cabitools.httpInterceptor(context) {
                retention = Retention.ONE_WEEK
                redactHeaders("X-Api-Key")
            }
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            // When
            client.newCall(Request.Builder().url(server.url("/orders")).header("X-Api-Key", "secret").build())
                .execute().use { it.body?.string() }

            // Then
            val repository = HttpLogs.repository(context)
            val item = repository.observeLogs().first { logs -> logs.isNotEmpty() }.single()
            assertEquals(server.url("/orders").toString(), item.url)
            val headers = repository.getLog(item.id)?.request?.headers
            assertEquals(listOf(HttpHeader("X-Api-Key", "****")), headers)
        }
}
