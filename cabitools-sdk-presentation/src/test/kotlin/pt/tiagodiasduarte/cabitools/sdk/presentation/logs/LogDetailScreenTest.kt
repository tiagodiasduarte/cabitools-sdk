package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import pt.tiagodiasduarte.cabitools.sdk.presentation.R
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader
import pt.tiagodiasduarte.cabitools.sdk.presentation.theme.CabitoolsTheme

@Config(qualifiers = "w411dp-h891dp")
@RunWith(AndroidJUnit4::class)
class LogDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var backs = 0
    private val copied = mutableListOf<String>()
    private val shared = mutableListOf<String>()

    private fun setContent(state: LogDetailUiState) = composeRule.setContent {
        CabitoolsTheme(dynamicColor = false) {
            LogDetailScreen(state, onBack = { backs++ }, onCopy = { copied += it }, onShare = { shared += it })
        }
    }

    @Test
    fun `given a log when the detail opens then it shows the Overview tab with the call fields`() {
        // Given
        val state = LogDetailUiState(loaded = true, detail = detail())

        // When
        setContent(state)

        // Then
        composeRule.onNodeWithText("GET /users/42").assertIsDisplayed()
        composeRule.onNodeWithText("Overview").assertIsSelected()
        listOf("URL", "https://api.example.com/users/42", "Status", "200 OK", "Duration", "120 ms")
            .forEach { composeRule.onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun `given a log when the Request tab is selected then it shows the request headers and body`() {
        // Given
        setContent(LogDetailUiState(loaded = true, detail = detail()))

        // When
        composeRule.onNodeWithText("Request").performClick()

        // Then
        composeRule.onNodeWithText("Authorization: ****").assertIsDisplayed()
        composeRule.onNodeWithText("No body").assertIsDisplayed()
    }

    @Test
    fun `given a truncated response when the Response tab is selected then it shows the body and the marker`() {
        // Given
        val response = MessageUi(
            listOf(HttpHeader("Content-Type", "application/json")),
            BodyUi.Text("{\n  \"id\": 42,", truncated = true),
        )
        setContent(LogDetailUiState(loaded = true, detail = detail(response = response)))

        // When
        composeRule.onNodeWithText("Response").performClick()

        // Then
        composeRule.onNodeWithText("Content-Type: application/json").assertIsDisplayed()
        composeRule.onNodeWithText("Body truncated: only the first part was stored.").assertIsDisplayed()
        composeRule.onNodeWithText("  \"id\": 42,").assertIsDisplayed()
    }

    @Test
    fun `given a binary response when the Response tab is selected then it says the body is not shown`() {
        // Given
        val response = MessageUi(emptyList(), BodyUi.Binary("12.4 kB"))
        setContent(LogDetailUiState(loaded = true, detail = detail(response = response)))

        // When
        composeRule.onNodeWithText("Response").performClick()

        // Then
        composeRule.onNodeWithText("No headers").assertIsDisplayed()
        composeRule.onNodeWithText("Binary body (12.4 kB) not shown").assertIsDisplayed()
    }

    @Test
    fun `given a request body that was not read when the Request tab is selected then it says so`() {
        // Given
        val request = MessageUi(emptyList(), BodyUi.NotCaptured("5 B"))
        setContent(LogDetailUiState(loaded = true, detail = detail(request = request)))

        // When
        composeRule.onNodeWithText("Request").performClick()

        // Then
        composeRule.onNodeWithText("Body (5 B) was not captured").assertIsDisplayed()
    }

    @Test
    fun `given a response closed unread when the Response tab is selected then it says it was not read`() {
        // Given
        val response = MessageUi(emptyList(), BodyUi.NotCaptured(size = null))
        setContent(LogDetailUiState(loaded = true, detail = detail(response = response)))

        // When
        composeRule.onNodeWithText("Response").performClick()

        // Then
        composeRule.onNodeWithText("Body was not captured: the app closed it before reading it").assertIsDisplayed()
    }

    @Test
    fun `given a log that no longer exists when the detail opens then it says it is not available`() {
        // Given
        val state = LogDetailUiState(loaded = true, detail = null)

        // When
        setContent(state)

        // Then
        composeRule.onNodeWithText("This log is no longer available.").assertIsDisplayed()
    }

    @Test
    fun `given the detail when back is selected then the screen asks to go back`() {
        // Given
        setContent(LogDetailUiState(loaded = true, detail = detail()))

        // When
        composeRule.onNodeWithContentDescription("Back").performClick()

        // Then
        assertEquals(1, backs)
    }

    @Test
    fun `given the Response tab with a text body when copy is selected then the body is copied`() {
        // Given
        val response = MessageUi(emptyList(), BodyUi.Text("{\n  \"id\": 42\n}", truncated = false))
        setContent(LogDetailUiState(loaded = true, detail = detail(response = response)))
        composeRule.onNodeWithText("Response").performClick()

        // When
        composeRule.onNodeWithContentDescription("Copy body").performClick()

        // Then
        assertEquals(listOf("{\n  \"id\": 42\n}"), copied)
    }

    @Test
    fun `given the Overview tab when the actions are shown then there is no copy action`() {
        // Given
        val state = LogDetailUiState(loaded = true, detail = detail())

        // When
        setContent(state)

        // Then
        assertTrue(composeRule.onAllNodesWithContentDescription("Copy body").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `given a log when share is selected then its share text is shared`() {
        // Given
        setContent(LogDetailUiState(loaded = true, detail = detail()))

        // When
        composeRule.onNodeWithContentDescription("Share log").performClick()

        // Then
        assertEquals(listOf("GET https://api.example.com/users/42\nAuthorization: ****"), shared)
    }

    private fun detail(
        request: MessageUi = MessageUi(listOf(HttpHeader("Authorization", "****")), BodyUi.Empty),
        response: MessageUi = MessageUi(emptyList(), BodyUi.Text("{}", truncated = false)),
    ) = LogDetailUi(
        title = "GET /users/42",
        overview = listOf(
            OverviewRowUi(R.string.cabitools_detail_url, "https://api.example.com/users/42"),
            OverviewRowUi(R.string.cabitools_detail_status, "200 OK"),
            OverviewRowUi(R.string.cabitools_detail_duration, "120 ms"),
        ),
        request = request,
        response = response,
        shareText = "GET https://api.example.com/users/42\nAuthorization: ****",
    )
}
