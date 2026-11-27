package pt.tiagodiasduarte.cabitools.sdk.infrastructure

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.http.HeaderRules
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.http.HttpLogsInterceptor
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.local.HttpLogsStore

@RunWith(AndroidJUnit4::class)
class HttpLogsTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val rules = HeaderRules(
        redactDefaultHeaders = true,
        redactedHeaders = emptySet(),
        hiddenHeaders = emptySet(),
    )

    @Test
    fun `given a retention when an interceptor is created then it records into the shared store with it`() {
        // Given
        val retentionMillis = WEEK

        // When
        val interceptor = HttpLogs.interceptor(context, retentionMillis, rules, maxContentLength = 250_000L)

        // Then
        assertTrue(interceptor is HttpLogsInterceptor)
        assertEquals(WEEK, HttpLogsStore.get(context).retentionMillis)
    }

    @Test
    fun `given two interceptors with different retention when both are created then the latest applies`() {
        // Given
        HttpLogs.interceptor(context, WEEK, rules, maxContentLength = 250_000L)

        // When
        HttpLogs.interceptor(context, HOUR, rules, maxContentLength = 250_000L)

        // Then
        assertEquals(HOUR, HttpLogsStore.get(context).retentionMillis)
    }

    private companion object {
        const val HOUR = 60 * 60 * 1000L
        const val WEEK = 7 * 24 * HOUR
    }
}
