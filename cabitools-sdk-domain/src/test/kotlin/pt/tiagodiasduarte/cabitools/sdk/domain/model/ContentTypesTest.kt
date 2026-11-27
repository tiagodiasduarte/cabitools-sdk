package pt.tiagodiasduarte.cabitools.sdk.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentTypesTest {

    @Test
    fun `given content types when they are checked for json then only json types match`() {
        // Given
        val types = listOf("application/json; charset=utf-8", "application/problem+json", "text/plain", null, "json")

        // When
        val json = types.map(ContentTypes::isJson)

        // Then
        assertEquals(listOf(true, true, false, false, false), json)
    }

    @Test
    fun `given content types when they are checked for text then binary types do not match`() {
        // Given
        val types = listOf("TEXT/HTML", "application/xml", "image/png", "application/octet-stream", null)

        // When
        val text = types.map(ContentTypes::isText)

        // Then
        assertEquals(listOf(true, true, false, false, true), text)
    }
}
