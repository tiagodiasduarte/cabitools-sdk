package pt.tiagodiasduarte.cabitools.sdk.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.tiagodiasduarte.cabitools.sdk.HttpLogsConfig
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.http.HeaderRules

class HttpLogsConfigMapperTest {

    @Test
    fun `given header options when they are mapped then the rules carry the same options`() {
        // Given
        val config = HttpLogsConfig().apply {
            redactHeaders("X-Api-Key")
            hideHeaders("X-Internal-Trace")
        }

        // When
        val rules = config.toHeaderRules()

        // Then
        assertEquals(
            HeaderRules(
                redactDefaultHeaders = true,
                redactedHeaders = setOf("x-api-key"),
                hiddenHeaders = setOf("x-internal-trace"),
            ),
            rules,
        )
    }

    @Test
    fun `given default redaction turned off when it is mapped then the rules turn it off too`() {
        // Given
        val config = HttpLogsConfig().apply { redactDefaultHeaders = false }

        // When
        val rules = config.toHeaderRules()

        // Then
        assertEquals(false, rules.redactDefaultHeaders)
    }
}
