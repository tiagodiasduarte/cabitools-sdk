package pt.tiagodiasduarte.cabitools.sdk

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CabitoolsTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private fun waitForSettingsScreen() {
        composeRule.waitUntil(TIMEOUT_MS) {
            composeRule.onAllNodesWithText("Cabitools").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Application").assertIsDisplayed()
        composeRule.onNodeWithText("Device").assertIsDisplayed()
    }

    @Test
    fun givenSettingsOpenedFromAnActivity_whenBackIsPressed_thenTheHostActivityIsResumed() {
        // Given
        ActivityScenario.launch(HostActivity::class.java).use { scenario ->
            scenario.onActivity { Cabitools.open(it) }
            waitForSettingsScreen()

            // When
            Espresso.pressBack()

            // Then
            composeRule.waitUntil(TIMEOUT_MS) { scenario.state == Lifecycle.State.RESUMED }
        }
    }

    @Test
    fun givenTheApplicationContext_whenOpenIsCalled_thenTheSettingsScreenIsShown() {
        // Given
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        // When
        Cabitools.open(appContext)

        // Then
        waitForSettingsScreen()
        Espresso.pressBackUnconditionally()
    }

    @Test
    fun givenTheLogsScreenOpenedFromSettings_whenBackIsPressed_thenTheSettingsScreenIsShownAgain() {
        // Given
        ActivityScenario.launch(HostActivity::class.java).use { scenario ->
            scenario.onActivity { Cabitools.open(it) }
            waitForSettingsScreen()
            composeRule.onNodeWithText("Open Logs").performClick()
            composeRule.onNodeWithText("HTTP logs").assertIsDisplayed()

            // When
            Espresso.pressBack()

            // Then
            composeRule.onNodeWithText("Open Logs").assertIsDisplayed()
            Espresso.pressBack()
        }
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
