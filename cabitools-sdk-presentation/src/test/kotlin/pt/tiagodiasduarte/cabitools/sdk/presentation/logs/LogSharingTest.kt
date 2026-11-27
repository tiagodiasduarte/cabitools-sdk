package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import android.app.Application
import android.content.ClipboardManager
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpMessage
import java.util.TimeZone

@RunWith(AndroidJUnit4::class)
class LogSharingTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val format = LogFormat(TimeZone.getTimeZone("UTC"))

    @Test
    fun `given a body when it is copied then the clipboard holds it`() {
        // Given
        val body = "{\"id\":42}"

        // When
        context.copyToClipboard("Body", body)

        // Then
        val clip = context.getSystemService(ClipboardManager::class.java).primaryClip
        assertEquals(body, clip?.getItemAt(0)?.text)
    }

    @Test
    fun `given a log text when it is shared then the share sheet opens with it`() {
        // Given
        val text = "GET https://api.example.com/users/42"

        // When
        context.shareText(text)

        // Then
        val chooser = shadowOf(context).nextStartedActivity
        @Suppress("DEPRECATION")
        val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        assertEquals(Intent.ACTION_SEND, send?.action)
        assertEquals("text/plain", send?.type)
        assertEquals(text, send?.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun `given a log with a redacted header when its share text is built then it shows the stars only`() {
        // Given
        val log = log(request = HttpMessage(listOf(HttpHeader("Authorization", "****")), null, null, null, false))

        // When
        val text = LogShareText.build(log, format)

        // Then
        assertTrue("Authorization: ****" in text)
        assertFalse("X-Internal-Trace" in text)
        assertEquals(
            """
            GET https://api.example.com/users/42
            Status: 200 OK
            Started: 14:31:55.120
            Duration: 120 ms

            --- Request ---
            Authorization: ****

            (no body)

            --- Response ---
            Content-Type: application/json

            {"id":42}
            """.trimIndent(),
            text,
        )
    }

    @Test
    fun `given an HTTP 2 response without reason phrase when the share text is built then the status is the code`() {
        // Given
        val log = log().copy(statusMessage = "")

        // When
        val text = LogShareText.build(log, format)

        // Then
        assertTrue("Status: 200\n" in text)
    }

    @Test
    fun `given a body longer than the share limit when the share text is built then the body is cut and marked`() {
        // Given
        val log = log(response = HttpMessage(emptyList(), "text/plain", "x".repeat(150), 150L, false))

        // When
        val text = LogShareText.build(log, format, maxBodyChars = 100)

        // Then
        assertTrue(text.endsWith("x".repeat(100) + "\n[body truncated]"))
        assertFalse("x".repeat(101) in text)
    }

    @Test
    fun `given a failed call with a binary request when the share text is built then it has the error and no binary`() {
        // Given
        val log = log(
            request = HttpMessage(emptyList(), "image/png", null, 12_400L, false),
            response = HttpMessage(emptyList(), null, "partial", 9_000L, true),
        ).copy(statusCode = null, statusMessage = null, error = "SocketTimeoutException: timeout")

        // When
        val text = LogShareText.build(log, format)

        // Then
        assertTrue("Status: -" in text)
        assertTrue("Error: SocketTimeoutException: timeout" in text)
        assertTrue("(body of 12.4 kB not shown)" in text)
        assertTrue(text.endsWith("partial\n[body truncated]"))
    }

    private fun log(
        request: HttpMessage = HttpMessage(emptyList(), null, null, null, false),
        response: HttpMessage = HttpMessage(
            listOf(HttpHeader("Content-Type", "application/json")), "application/json", "{\"id\":42}", 9L, false,
        ),
    ) = HttpLogDetail(
        id = 1L,
        method = "GET",
        url = "https://api.example.com/users/42",
        startedAt = 52_315_120L,
        durationMs = 120L,
        protocol = "h2",
        statusCode = 200,
        statusMessage = "OK",
        error = null,
        request = request,
        response = response,
    )
}
