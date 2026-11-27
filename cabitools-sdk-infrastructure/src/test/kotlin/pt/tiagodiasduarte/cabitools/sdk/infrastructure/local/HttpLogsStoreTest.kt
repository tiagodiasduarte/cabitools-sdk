package pt.tiagodiasduarte.cabitools.sdk.infrastructure.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HttpLogsStoreTest {

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val db = Room.inMemoryDatabaseBuilder(context, HttpLogsDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private var now = 0L
    private val store = HttpLogsStore(db.httpLogsDao(), testScope.backgroundScope, clock = { now })

    @After
    fun tearDown() = db.close()

    @Test
    fun `given a finished call when it is saved then it is listed`() = testScope.runTest {
        // Given
        now = HOUR

        // When
        store.save(log(requestedAt = now), "request", "response")

        // Then
        val summaries = store.observeSummaries().first { it.isNotEmpty() }
        assertEquals(listOf("https://api.example.com/1"), summaries.map { it.url })
        assertEquals("response", store.get(summaries.single().id)?.bodies?.responseBody)
    }

    @Test
    fun `given each retention period when time passes beyond it then older logs are deleted and hidden`() =
        testScope.runTest {
            listOf(HOUR, DAY, WEEK).forEach { retentionMillis ->
                // Given
                store.clear()
                store.retentionMillis = retentionMillis
                now = WEEK * 2
                store.saveAsync(log(requestedAt = now - retentionMillis - 1, id = 1), null, null).join()
                store.saveAsync(log(requestedAt = now - retentionMillis + 1, id = 2), null, null).join()

                // When
                val urls = store.observeSummaries().first().map { it.url }

                // Then
                assertEquals("$retentionMillis ms", listOf("https://api.example.com/2"), urls)
                assertEquals("$retentionMillis ms", 1, rows("http_logs"))
            }
        }

    @Test
    fun `given expired logs and no new calls when the list is opened then the expired logs are deleted`() =
        testScope.runTest {
            // Given
            store.retentionMillis = HOUR
            now = HOUR
            store.saveAsync(log(requestedAt = now), "old", null).join()
            now += 2 * HOUR

            // When
            val summaries = store.observeSummaries().first()

            // Then
            assertEquals(emptyList<HttpLogSummary>(), summaries)
            assertEquals(0, rows("http_logs"))
            assertEquals(0, rows("http_log_bodies"))
        }

    @Test
    fun `given 500 stored logs when one more call is saved then the oldest is dropped`() = testScope.runTest {
        // Given
        now = HOUR
        (1..HttpLogsStore.MAX_LOGS)
            .map { store.saveAsync(log(requestedAt = it.toLong(), id = it), null, null) }
            .joinAll()

        // When
        store.saveAsync(log(requestedAt = now, id = HttpLogsStore.MAX_LOGS + 1), null, null).join()

        // Then
        val urls = store.observeSummaries().first().map { it.url }
        assertEquals(HttpLogsStore.MAX_LOGS, urls.size)
        assertEquals("https://api.example.com/${HttpLogsStore.MAX_LOGS + 1}", urls.first())
        assertEquals(false, "https://api.example.com/1" in urls)
    }

    @Test
    fun `given the database fails when a call is saved then no error reaches the caller`() = testScope.runTest {
        // Given
        db.close()

        // When
        val write = store.saveAsync(log(requestedAt = 1L), "request", "response")
        write.join()

        // Then
        assertFalse(write.isCancelled)
    }

    @Test
    fun `given the store was created when it is requested again then the same instance is returned`() {
        // Given
        val first = HttpLogsStore.get(context, dispatcher)

        // When
        val second = HttpLogsStore.get(context, dispatcher)

        // Then
        assertSame(first, second)
    }

    private fun rows(table: String): Int = db.openHelper.readableDatabase
        .query("SELECT COUNT(*) FROM $table").use { it.moveToFirst(); it.getInt(0) }

    private fun log(requestedAt: Long, id: Int = 1) = HttpLogEntity(
        method = "GET",
        url = "https://api.example.com/$id",
        requestedAt = requestedAt,
        durationMs = 10L,
        protocol = "h2",
        responseCode = 200,
        responseMessage = "OK",
        error = null,
        requestHeaders = listOf(HttpHeader("Accept", "*/*")),
        requestContentType = null,
        requestBodySize = null,
        requestBodyTruncated = false,
        responseHeaders = emptyList(),
        responseContentType = null,
        responseBodySize = null,
        responseBodyTruncated = false,
    )

    private companion object {
        const val HOUR = 60 * 60 * 1000L
        const val DAY = 24 * HOUR
        const val WEEK = 7 * DAY
    }
}
