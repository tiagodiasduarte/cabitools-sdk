package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import pt.tiagodiasduarte.cabitools.sdk.presentation.theme.CabitoolsTheme

@Config(qualifiers = "w411dp-h891dp")
@RunWith(AndroidJUnit4::class)
class LogsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val opened = mutableListOf<Long>()
    private var clears = 0
    private var backs = 0

    private fun setContent(state: () -> LogsUiState) = composeRule.setContent {
        CabitoolsTheme(dynamicColor = false) {
            LogsScreen(state(), onBack = { backs++ }, onClear = { clears++ }, onOpen = { opened += it })
        }
    }

    @Test
    fun `given a successful call when the list is shown then its item has method url time duration size and status`() {
        // Given
        val state = LogsUiState(loaded = true, items = listOf(success))

        // When
        setContent { state }

        // Then
        listOf("POST", "https://api.example.com/orders", "201", "14:31:55   340 ms   356 B")
            .forEach { composeRule.onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun `given a failed call when the list is shown then its item shows an error indicator instead of a status`() {
        // Given
        val failed = success.copy(id = 9, status = null, size = null, duration = "10000 ms")

        // When
        setContent { LogsUiState(loaded = true, items = listOf(failed)) }

        // Then
        composeRule.onNodeWithText("Error").assertIsDisplayed()
        composeRule.onNodeWithText("14:31:55   10000 ms").assertIsDisplayed()
    }

    @Test
    fun `given no recorded calls when the list is shown then it says nothing was recorded yet`() {
        // Given
        val state = LogsUiState(loaded = true)

        // When
        setContent { state }

        // Then
        composeRule.onNodeWithText("No HTTP calls recorded yet").assertIsDisplayed()
    }

    @Test
    fun `given the list is still loading when it is shown then the empty message is not shown`() {
        // Given
        val state = LogsUiState(loaded = false)

        // When
        setContent { state }

        // Then
        assertTrue(composeRule.onAllNodesWithText("No HTTP calls recorded yet").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `given an open list when a new call is added then it is shown above the older one`() {
        // Given
        var state by mutableStateOf(LogsUiState(loaded = true, items = listOf(success)))
        setContent { state }

        // When
        val newer = success.copy(id = 2, method = "GET", url = "https://api.example.com/new")
        state = state.copy(items = listOf(newer) + state.items)

        // Then
        val newTop = composeRule.onNodeWithText("https://api.example.com/new").fetchSemanticsNode().boundsInRoot.top
        val oldTop = composeRule.onNodeWithText("https://api.example.com/orders").fetchSemanticsNode().boundsInRoot.top
        assertTrue("new log must be on top", newTop < oldTop)
    }

    @Test
    fun `given an item when it is selected then its detail is requested`() {
        // Given
        setContent { LogsUiState(loaded = true, items = listOf(success)) }

        // When
        composeRule.onNodeWithText("https://api.example.com/orders").performClick()

        // Then
        assertEquals(listOf(1L), opened)
    }

    @Test
    fun `given the list when clear is selected then the logs are cleared`() {
        // Given
        setContent { LogsUiState(loaded = true, items = listOf(success)) }

        // When
        composeRule.onNodeWithContentDescription("Clear logs").performClick()

        // Then
        assertEquals(1, clears)
    }

    @Test
    fun `given the list when back is selected then the screen asks to go back`() {
        // Given
        setContent { LogsUiState(loaded = true) }

        // When
        composeRule.onNodeWithContentDescription("Back").performClick()

        // Then
        assertEquals(1, backs)
    }

    private companion object {
        val success = LogItemUi(1, "POST", "https://api.example.com/orders", "14:31:55", "340 ms", "356 B", "201")
    }
}
