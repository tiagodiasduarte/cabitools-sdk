package pt.tiagodiasduarte.cabitools.sdk.infrastructure.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader

@RunWith(AndroidJUnit4::class)
class HttpLogsDaoTest {

    private val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext<Context>(),
        HttpLogsDatabase::class.java,
    ).allowMainThreadQueries().build()
    private val dao = db.httpLogsDao()

    @After
    fun tearDown() = db.close()

    @Test
    fun `given a log with bodies when it is inserted then it is read back with its bodies and headers`() = runTest {
        // Given
        val log = log(requestedAt = 1_000L).copy(
            requestHeaders = listOf(HttpHeader("Set-Cookie", "a=1"), HttpHeader("Set-Cookie", "b=2")),
        )

        // When
        val id = dao.insert(log, requestBody = "{\"q\":1}", responseBody = "{\"ok\":true}")

        // Then
        val stored = dao.getWithBodies(id)
        assertEquals(log.copy(id = id), stored?.log)
        assertEquals(HttpLogBodiesEntity(id, "{\"q\":1}", "{\"ok\":true}"), stored?.bodies)
    }

    @Test
    fun `given logs at different times when summaries are observed then newer ones come first without old ones`() =
        runTest {
            // Given
            val old = dao.insert(log(requestedAt = 1_000L), null, null)
            val middle = dao.insert(log(requestedAt = 2_000L), null, null)
            val newest = dao.insert(log(requestedAt = 3_000L), null, null)

            // When
            val summaries = dao.observeSummaries(since = 2_000L).first()

            // Then
            assertEquals(listOf(newest, middle), summaries.map { it.id })
            assertFalse(old in summaries.map { it.id })
        }

    @Test
    fun `given a stored log when its summary is read then it has the list columns`() = runTest {
        // Given
        val id = dao.insert(log(requestedAt = 5_000L), null, null)

        // When
        val summary = dao.observeSummaries(since = 0L).first().single()

        // Then
        assertEquals(HttpLogSummary(id, "GET", "https://api.example.com/users", 5_000L, 120L, 200, null, 356L), summary)
    }

    @Test
    fun `given stored logs when they are cleared then logs and bodies are gone`() = runTest {
        // Given
        val id = dao.insert(log(requestedAt = 1_000L), "request", "response")

        // When
        dao.clear()

        // Then
        assertEquals(emptyList<HttpLogSummary>(), dao.observeSummaries(since = 0L).first())
        assertNull(dao.getWithBodies(id))
        assertEquals(0, bodiesCount())
    }

    @Test
    fun `given logs older than a cutoff when old logs are deleted then their bodies are deleted too`() = runTest {
        // Given
        dao.insert(log(requestedAt = 1_000L), "old request", "old response")
        val kept = dao.insert(log(requestedAt = 9_000L), "new request", "new response")

        // When
        val deleted = dao.deleteOlderThan(before = 5_000L)

        // Then
        assertEquals(1, deleted)
        assertEquals(listOf(kept), dao.observeSummaries(since = 0L).first().map { it.id })
        assertEquals(1, bodiesCount())
    }

    @Test
    fun `given more logs than the limit when keeping the newest then the oldest are deleted`() = runTest {
        // Given
        val ids = (1..5).map { dao.insert(log(requestedAt = it * 1_000L), "body $it", null) }

        // When
        val deleted = dao.keepNewest(max = 3)

        // Then
        assertEquals(2, deleted)
        assertEquals(ids.takeLast(3).reversed(), dao.observeSummaries(since = 0L).first().map { it.id })
        assertEquals(3, bodiesCount())
    }

    private fun bodiesCount(): Int = db.openHelper.readableDatabase
        .query("SELECT COUNT(*) FROM http_log_bodies").use { it.moveToFirst(); it.getInt(0) }

    private fun log(requestedAt: Long) = HttpLogEntity(
        method = "GET",
        url = "https://api.example.com/users",
        requestedAt = requestedAt,
        durationMs = 120L,
        protocol = "h2",
        responseCode = 200,
        responseMessage = "OK",
        error = null,
        requestHeaders = listOf(HttpHeader("Accept", "application/json")),
        requestContentType = null,
        requestBodySize = null,
        requestBodyTruncated = false,
        responseHeaders = listOf(HttpHeader("Content-Type", "application/json")),
        responseContentType = "application/json",
        responseBodySize = 356L,
        responseBodyTruncated = false,
    )
}
