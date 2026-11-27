package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class LogFormatTest {

    private val format = LogFormat(TimeZone.getTimeZone("UTC"))

    @Test
    fun `given an epoch time when it is formatted then it shows the time of day in the zone`() {
        // Given
        val epochMillis = 52_315_120L

        // When
        val times = listOf(format.time(epochMillis), format.timeWithMillis(epochMillis))

        // Then
        assertEquals(listOf("14:31:55", "14:31:55.120"), times)
    }

    @Test
    fun `given sizes in each range when they are formatted then they use decimal units`() {
        // Given
        val sizes = listOf(0L, 356L, 999L, 1_000L, 12_400L, 250_000L, 1_200_000L)

        // When
        val formatted = sizes.map(format::size)

        // Then
        assertEquals(listOf("0 B", "356 B", "999 B", "1.0 kB", "12.4 kB", "250.0 kB", "1.2 MB"), formatted)
    }

    @Test
    fun `given a duration when it is formatted then it is shown in milliseconds`() {
        // Given
        val millis = 340L

        // When
        val duration = format.duration(millis)

        // Then
        assertEquals("340 ms", duration)
    }
}
