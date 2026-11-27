package pt.tiagodiasduarte.cabitools.sdk.infrastructure.http

import okhttp3.Headers
import org.junit.Assert.assertEquals
import org.junit.Test
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader

class HeaderRulesTest {

    @Test
    fun `given the default rules when sensitive headers are captured then their values become stars`() {
        // Given
        val rules = HeaderRules(redactDefaultHeaders = true, redactedHeaders = emptySet(), hiddenHeaders = emptySet())
        val headers = Headers.headersOf(
            "authorization", "Bearer abc123",
            "Proxy-Authorization", "Basic xyz",
            "Cookie", "session=1",
            "Set-Cookie", "a=1",
            "Accept", "application/json",
        )

        // When
        val captured = rules.apply(headers)

        // Then
        assertEquals(
            listOf(
                HttpHeader("authorization", "****"),
                HttpHeader("Proxy-Authorization", "****"),
                HttpHeader("Cookie", "****"),
                HttpHeader("Set-Cookie", "****"),
                HttpHeader("Accept", "application/json"),
            ),
            captured,
        )
    }

    @Test
    fun `given an extra redacted header in any case when it is captured then its value becomes stars`() {
        // Given
        val rules = HeaderRules(
            redactDefaultHeaders = true,
            redactedHeaders = setOf("x-api-key"),
            hiddenHeaders = emptySet(),
        )

        // When
        val captured = rules.apply(Headers.headersOf("X-Api-Key", "secret"))

        // Then
        assertEquals(listOf(HttpHeader("X-Api-Key", "****")), captured)
    }

    @Test
    fun `given a hidden header when headers are captured then it is not stored at all`() {
        // Given
        val rules = HeaderRules(
            redactDefaultHeaders = true,
            redactedHeaders = emptySet(),
            hiddenHeaders = setOf("X-Internal-Trace", "Authorization"),
        )

        // When
        val captured = rules.apply(
            Headers.headersOf("x-internal-trace", "t-1", "Authorization", "Bearer a", "Accept", "*/*"),
        )

        // Then
        assertEquals(listOf(HttpHeader("Accept", "*/*")), captured)
    }

    @Test
    fun `given default redaction turned off when sensitive headers are captured then their values are kept`() {
        // Given
        val rules = HeaderRules(redactDefaultHeaders = false, redactedHeaders = emptySet(), hiddenHeaders = emptySet())

        // When
        val captured = rules.apply(Headers.headersOf("Authorization", "Bearer abc"))

        // Then
        assertEquals(listOf(HttpHeader("Authorization", "Bearer abc")), captured)
    }

    @Test
    fun `given repeated headers when they are captured then each line is kept in order`() {
        // Given
        val rules = HeaderRules(redactDefaultHeaders = false, redactedHeaders = emptySet(), hiddenHeaders = emptySet())

        // When
        val captured = rules.apply(Headers.headersOf("Set-Cookie", "a=1", "Set-Cookie", "b=2"))

        // Then
        assertEquals(listOf(HttpHeader("Set-Cookie", "a=1"), HttpHeader("Set-Cookie", "b=2")), captured)
    }
}
