package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pt.tiagodiasduarte.cabitools.sdk.presentation.R
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpMessage
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LogDetailViewModelTest {

    private val format = LogFormat(TimeZone.getTimeZone("UTC"))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun detailOf(log: HttpLogDetail): LogDetailUi? =
        LogDetailViewModel(log.id, FakeHttpLogsRepository(details = mapOf(log.id to log)), format).uiState.value.detail

    @Test
    fun `given a successful call when the detail loads then the overview has its fields and the title its path`() {
        // Given
        val log = detail()

        // When
        val ui = detailOf(log)

        // Then
        assertEquals("GET /users/42?full=1", ui?.title)
        assertEquals(
            listOf(
                OverviewRowUi(R.string.cabitools_detail_url, "https://api.example.com/users/42?full=1"),
                OverviewRowUi(R.string.cabitools_detail_method, "GET"),
                OverviewRowUi(R.string.cabitools_detail_status, "200 OK"),
                OverviewRowUi(R.string.cabitools_detail_started, "14:31:55.120"),
                OverviewRowUi(R.string.cabitools_detail_duration, "120 ms"),
                OverviewRowUi(R.string.cabitools_detail_protocol, "h2"),
                OverviewRowUi(R.string.cabitools_detail_request_size, "-"),
                OverviewRowUi(R.string.cabitools_detail_response_size, "23 B"),
            ),
            ui?.overview,
        )
    }

    @Test
    fun `given an HTTP 2 response without reason phrase when the detail loads then the status is just the code`() {
        // Given
        val log = detail().copy(statusMessage = "")

        // When
        val overview = detailOf(log)?.overview.orEmpty()

        // Then
        assertTrue(OverviewRowUi(R.string.cabitools_detail_status, "200") in overview)
    }

    @Test
    fun `given a failed call when the detail loads then the overview shows the error and no status`() {
        // Given
        val log = detail()
            .copy(statusCode = null, statusMessage = null, protocol = null, error = "SocketTimeoutException: timeout")

        // When
        val overview = detailOf(log)?.overview.orEmpty()

        // Then
        assertTrue(OverviewRowUi(R.string.cabitools_detail_status, "-") in overview)
        val expectedError = OverviewRowUi(R.string.cabitools_detail_error, "SocketTimeoutException: timeout")
        assertEquals(expectedError, overview.last())
    }

    @Test
    fun `given a valid JSON response when the detail loads then the body is pretty-printed`() {
        // Given
        val log = detail()

        // When
        val body = detailOf(log)?.response?.body

        // Then
        assertEquals(BodyUi.Text("{\n  \"id\": 42,\n  \"status\": \"ok\"\n}", truncated = false), body)
    }

    @Test
    fun `given an invalid JSON response when the detail loads then the body is shown as stored`() {
        // Given
        val log = detail(response = message(body = "{oops", contentType = "application/json"))

        // When
        val body = detailOf(log)?.response?.body

        // Then
        assertEquals(BodyUi.Text("{oops", truncated = false), body)
    }

    @Test
    fun `given a truncated response when the detail loads then the body is marked truncated`() {
        // Given
        val log = detail(response = message(body = "{\"items\":[1,2", truncated = true, size = 400_000L))

        // When
        val body = detailOf(log)?.response?.body

        // Then
        assertEquals(BodyUi.Text("{\"items\":[1,2", truncated = true), body)
    }

    @Test
    fun `given a png response when the detail loads then it says the binary body is not shown with its size`() {
        // Given
        val log = detail(response = message(body = null, contentType = "image/png", size = 12_400L))

        // When
        val body = detailOf(log)?.response?.body

        // Then
        assertEquals(BodyUi.Binary("12.4 kB"), body)
    }

    @Test
    fun `given a text body that was not read when the detail loads then it says it was not captured`() {
        // Given
        val log = detail(request = message(body = null, contentType = "text/plain", size = 5L))

        // When
        val body = detailOf(log)?.request?.body

        // Then
        assertEquals(BodyUi.NotCaptured("5 B"), body)
    }

    @Test
    fun `given a response closed unread with unknown size when the detail loads then it was not captured`() {
        // Given
        val log = detail(response = message(body = null, contentType = "application/json", size = null))

        // When
        val body = detailOf(log)?.response?.body

        // Then
        assertEquals(BodyUi.NotCaptured(size = null), body)
    }

    @Test
    fun `given a call without request body when the detail loads then the request body is empty`() {
        // Given
        val log = detail()

        // When
        val request = detailOf(log)?.request

        // Then
        assertEquals(MessageUi(listOf(HttpHeader("Authorization", "****")), BodyUi.Empty), request)
    }

    @Test
    fun `given a request body and no response size when the detail loads then the sizes show both cases`() {
        // Given
        val log = detail(
            request = message(body = "name=Ana", contentType = "application/x-www-form-urlencoded"),
            response = message(body = null, contentType = null, size = null),
        )

        // When
        val overview = detailOf(log)?.overview.orEmpty()

        // Then
        assertTrue(OverviewRowUi(R.string.cabitools_detail_request_size, "8 B") in overview)
        assertTrue(OverviewRowUi(R.string.cabitools_detail_response_size, "-") in overview)
    }

    @Test
    fun `given a plain text body when the detail loads then it is shown as stored without pretty printing`() {
        // Given
        val log = detail(response = message(body = "{\"looks\":\"json\"}", contentType = "text/plain"))

        // When
        val body = detailOf(log)?.response?.body

        // Then
        assertEquals(BodyUi.Text("{\"looks\":\"json\"}", truncated = false), body)
    }

    @Test
    fun `given an empty body of size zero when the detail loads then it is shown as no body`() {
        // Given
        val log = detail(response = message(body = null, contentType = "image/png", size = 0L))

        // When
        val body = detailOf(log)?.response?.body

        // Then
        assertEquals(BodyUi.Empty, body)
    }

    @Test
    fun `given a url without path when the detail loads then the title shows the root path`() {
        // Given
        val log = detail().copy(url = "https://api.example.com")

        // When
        val title = detailOf(log)?.title

        // Then
        assertEquals("GET /", title)
    }

    @Test
    fun `given a log that was cleared when the detail loads then it is loaded without detail`() {
        // Given
        val repository = FakeHttpLogsRepository()

        // When
        val state = LogDetailViewModel(7L, repository, format).uiState.value

        // Then
        assertTrue(state.loaded)
        assertNull(state.detail)
    }

    private fun message(
        body: String?,
        contentType: String? = "application/json",
        size: Long? = null,
        truncated: Boolean = false,
    ) =
        HttpMessage(emptyList(), contentType, body, size ?: body?.length?.toLong(), truncated)

    private fun detail(
        request: HttpMessage = HttpMessage(listOf(HttpHeader("Authorization", "****")), null, null, null, false),
        response: HttpMessage = message(body = "{\"id\":42,\"status\":\"ok\"}"),
    ) = HttpLogDetail(
        id = 1L,
        method = "GET",
        url = "https://api.example.com/users/42?full=1",
        startedAt = 52_315_120L,
        durationMs = 120L,
        protocol = "h2",
        statusCode = 200,
        statusMessage = "OK",
        error = null,
        request = request,
        response = response,
    )
}
