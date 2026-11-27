package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonPrettyPrinterTest {

    @Test
    fun `given a valid JSON object when it is formatted then it is indented on separate lines`() {
        // Given
        val body = "{\"id\":42,\"status\":\"ok\"}"

        // When
        val pretty = JsonPrettyPrinter.format(body)

        // Then
        assertEquals("{\n  \"id\": 42,\n  \"status\": \"ok\"\n}", pretty)
    }

    @Test
    fun `given a JSON array with a url when it is formatted then slashes are not escaped`() {
        // Given
        val body = "[{\"url\":\"https://a.example/x\"}]"

        // When
        val pretty = JsonPrettyPrinter.format(body)

        // Then
        assertEquals("[\n  {\n    \"url\": \"https://a.example/x\"\n  }\n]", pretty)
    }

    @Test
    fun `given invalid or truncated JSON when it is formatted then it is returned as stored`() {
        // Given
        val bodies = listOf("not json", "{\"items\":[1,2", "42", "{\"a\":1} trailing")

        // When
        val formatted = bodies.map(JsonPrettyPrinter::format)

        // Then
        assertEquals(bodies, formatted)
    }
}
