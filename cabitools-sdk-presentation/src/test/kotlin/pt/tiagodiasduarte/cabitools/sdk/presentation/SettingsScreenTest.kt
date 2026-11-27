package pt.tiagodiasduarte.cabitools.sdk.presentation

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import pt.tiagodiasduarte.cabitools.sdk.presentation.R
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.CabitoolsRoute
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.InfoRowUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.NavigationRowUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.SectionUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.SettingsUiState
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.UiText
import pt.tiagodiasduarte.cabitools.sdk.presentation.theme.CabitoolsTheme

// Phone-sized screen, so every row of the default state fits without scrolling.
@Config(qualifiers = "w411dp-h891dp")
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val state = SettingsUiState(
        sections = listOf(
            SectionUi(
                R.string.cabitools_section_application,
                listOf(
                    InfoRowUi(
                        R.string.cabitools_label_version_name,
                        UiText.Resource(R.string.cabitools_value_unknown),
                    ),
                    InfoRowUi(R.string.cabitools_label_version_code, UiText.Plain("230")),
                    InfoRowUi(R.string.cabitools_label_app_id, UiText.Plain("com.example.host")),
                ),
            ),
            SectionUi(
                R.string.cabitools_section_device,
                listOf(
                    InfoRowUi(R.string.cabitools_label_model, UiText.Plain("Samsung SM-S918B")),
                    InfoRowUi(R.string.cabitools_label_android_version, UiText.Plain("15 (API 35)")),
                ),
            ),
            SectionUi(
                R.string.cabitools_section_logs,
                listOf(NavigationRowUi(R.string.cabitools_label_open_logs, CabitoolsRoute.Logs)),
            ),
        ),
    )

    private val navigations = mutableListOf<CabitoolsRoute>()

    private fun setContent(uiState: SettingsUiState) = composeRule.setContent {
        CabitoolsTheme(dynamicColor = false) { SettingsScreen(uiState, onNavigate = { navigations += it }) }
    }

    @Test
    fun `given application and device sections when the screen is shown then Application is above Device`() {
        // Given
        val uiState = state

        // When
        setContent(uiState)

        // Then
        val appTop = composeRule.onNodeWithText("Application").assertIsDisplayed()
            .fetchSemanticsNode().boundsInRoot.top
        val deviceTop = composeRule.onNodeWithText("Device").assertIsDisplayed()
            .fetchSemanticsNode().boundsInRoot.top
        assertTrue("Application must be above Device", appTop < deviceTop)
    }

    @Test
    fun `given rows with values when the screen is shown then every label and value is displayed`() {
        // Given
        val uiState = state

        // When
        setContent(uiState)

        // Then
        listOf(
            "Version name", "Unknown",
            "Version code", "230",
            "App id", "com.example.host",
            "Model", "Samsung SM-S918B",
            "Android version", "15 (API 35)",
        ).forEach { composeRule.onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun `given more rows than fit on screen when the list is scrolled then the last row is displayed`() {
        // Given
        val manyRows = List(40) { InfoRowUi(R.string.cabitools_label_model, UiText.Plain("value $it")) }
        setContent(SettingsUiState(sections = listOf(SectionUi(R.string.cabitools_section_device, manyRows))))

        // When
        composeRule.onNodeWithTag(SETTINGS_LIST_TAG).performScrollToNode(hasText("value 39"))

        // Then
        composeRule.onNodeWithText("value 39").assertIsDisplayed()
    }

    @Test
    fun `given the three sections when the screen is shown then Logs is below Device with an Open Logs row`() {
        // Given
        val uiState = state

        // When
        setContent(uiState)

        // Then
        val deviceTop = composeRule.onNodeWithText("Device").fetchSemanticsNode().boundsInRoot.top
        val logsTop = composeRule.onNodeWithText("Logs").assertIsDisplayed().fetchSemanticsNode().boundsInRoot.top
        assertTrue("Logs must be below Device", deviceTop < logsTop)
        composeRule.onNodeWithText("Open Logs").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun `given the Open Logs row when it is selected then the logs screen is requested`() {
        // Given
        setContent(state)

        // When
        composeRule.onNodeWithText("Open Logs").performClick()

        // Then
        assertEquals(listOf<CabitoolsRoute>(CabitoolsRoute.Logs), navigations)
    }
}
