package pt.tiagodiasduarte.cabitools.sdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpLogsConfigTest {

    @Test
    fun `given no options when the config is created then it uses the defaults`() {
        // Given
        val configure: HttpLogsConfig.() -> Unit = {}

        // When
        val config = HttpLogsConfig().apply(configure)

        // Then
        assertEquals(Retention.ONE_DAY, config.retention)
        assertEquals(250_000L, config.maxContentLength)
        assertTrue(config.redactDefaultHeaders)
        assertTrue(config.redactedHeaders.isEmpty())
        assertTrue(config.hiddenHeaders.isEmpty())
    }

    @Test
    fun `given custom options when the config is created then it keeps them`() {
        // Given
        val configure: HttpLogsConfig.() -> Unit = {
            retention = Retention.ONE_HOUR
            maxContentLength = 10_000L
            redactDefaultHeaders = false
        }

        // When
        val config = HttpLogsConfig().apply(configure)

        // Then
        assertEquals(Retention.ONE_HOUR, config.retention)
        assertEquals(10_000L, config.maxContentLength)
        assertEquals(false, config.redactDefaultHeaders)
    }

    @Test
    fun `given header names in any case when they are added then the config keeps them lowercase`() {
        // Given
        val configure: HttpLogsConfig.() -> Unit = {
            redactHeaders("X-Api-Key", "x-session")
            hideHeaders("X-Internal-Trace")
            hideHeaders("x-internal-trace")
        }

        // When
        val config = HttpLogsConfig().apply(configure)

        // Then
        assertEquals(setOf("x-api-key", "x-session"), config.redactedHeaders)
        assertEquals(setOf("x-internal-trace"), config.hiddenHeaders)
    }

    @Test
    fun `given a non-positive max content length when it is set then it throws`() {
        // Given
        val config = HttpLogsConfig()

        // When
        val zero = assertThrows(IllegalArgumentException::class.java) { config.maxContentLength = 0 }

        // Then
        assertEquals("maxContentLength must be positive, was 0", zero.message)
        assertEquals(250_000L, config.maxContentLength)
    }

    @Test
    fun `given each retention option when its duration is read then it matches the period`() {
        // Given
        val hour = 60 * 60 * 1000L

        // When
        val durations = Retention.entries.map { it.millis }

        // Then
        assertEquals(listOf(hour, 24 * hour, 7 * 24 * hour), durations)
    }
}
