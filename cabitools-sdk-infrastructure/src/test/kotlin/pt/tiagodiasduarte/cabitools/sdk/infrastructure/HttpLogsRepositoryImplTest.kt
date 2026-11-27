package pt.tiagodiasduarte.cabitools.sdk.infrastructure

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.local.HttpLogEntity
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.local.HttpLogsDatabase
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.local.HttpLogsStore
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogItem
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpMessage

@RunWith(AndroidJUnit4::class)
class HttpLogsRepositoryImplTest {

    private val testScope = TestScope()
    private val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext<Context>(),
        HttpLogsDatabase::class.java,
    ).allowMainThreadQueries().build()
    private val store = HttpLogsStore(db.httpLogsDao(), testScope.backgroundScope, clock = { NOW })
    private val repository = HttpLogsRepositoryImpl(store)

    @After
    fun tearDown() = db.close()

    @Test
    fun `given a stored call when the list is observed then it is mapped to a list item`() = testScope.runTest {
        // Given
        store.saveAsync(entity, "{\"q\":1}", "{\"ok\":true}").join()

        // When
        val items = repository.observeLogs().first()

        // Then
        assertEquals(
            listOf(HttpLogItem(items.single().id, "POST", URL, NOW, 340L, 201, null, 11L)),
            items,
        )
    }

    @Test
    fun `given a stored call when its detail is read then request and response carry headers and bodies`() =
        testScope.runTest {
            // Given
            store.saveAsync(entity, "{\"q\":1}", "{\"ok\":true}").join()
            val id = repository.observeLogs().first().single().id

            // When
            val detail = repository.getLog(id)

            // Then
            assertEquals(
                HttpLogDetail(
                    id = id, method = "POST", url = URL, startedAt = NOW, durationMs = 340L, protocol = "h2",
                    statusCode = 201, statusMessage = "Created", error = null,
                    request = HttpMessage(
                        listOf(HttpHeader("Authorization", "****")), "application/json", "{\"q\":1}", 7L, false,
                    ),
                    response = HttpMessage(emptyList(), "application/json", "{\"ok\":true}", 11L, false),
                ),
                detail,
            )
        }

    @Test
    fun `given stored calls when the logs are cleared then the list is empty and the detail is gone`() =
        testScope.runTest {
            // Given
            store.saveAsync(entity, null, null).join()
            val id = repository.observeLogs().first().single().id

            // When
            repository.clearLogs()

            // Then
            assertEquals(emptyList<HttpLogItem>(), repository.observeLogs().first())
            assertNull(repository.getLog(id))
        }

    private companion object {
        const val NOW = 1_000_000L
        const val URL = "https://api.example.com/orders"
        val entity = HttpLogEntity(
            method = "POST", url = URL, requestedAt = NOW, durationMs = 340L, protocol = "h2",
            responseCode = 201, responseMessage = "Created", error = null,
            requestHeaders = listOf(HttpHeader("Authorization", "****")), requestContentType = "application/json",
            requestBodySize = 7L, requestBodyTruncated = false, responseHeaders = emptyList(),
            responseContentType = "application/json", responseBodySize = 11L, responseBodyTruncated = false,
        )
    }
}
