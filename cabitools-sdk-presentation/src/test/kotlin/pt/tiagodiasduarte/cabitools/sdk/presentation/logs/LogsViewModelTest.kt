package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogItem
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class LogsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val format = LogFormat(TimeZone.getTimeZone("UTC"))

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `given recorded calls when the list loads then items are formatted in the repository order`() =
        runTest(dispatcher) {
            // Given
            val repository = FakeHttpLogsRepository(listOf(success(id = 2), success(id = 1)))

            // When
            val state = LogsViewModel(repository, format).uiState.first { it.loaded }

            // Then
            assertEquals(listOf(2L, 1L), state.items.map { it.id })
            assertEquals(
                LogItemUi(2, "POST", "https://api.example.com/orders", "14:31:55", "340 ms", "356 B", "201"),
                state.items.first(),
            )
        }

    @Test
    fun `given a failed call when the list loads then its item has no status and no size`() = runTest(dispatcher) {
        // Given
        val failed = success(id = 3)
            .copy(statusCode = null, error = "SocketTimeoutException: timeout", responseSize = null)
        val repository = FakeHttpLogsRepository(listOf(failed))

        // When
        val item = LogsViewModel(repository, format).uiState.first { it.loaded }.items.single()

        // Then
        assertEquals(null, item.status)
        assertEquals(null, item.size)
    }

    @Test
    fun `given no recorded calls when the list loads then it is loaded and empty`() = runTest(dispatcher) {
        // Given
        val repository = FakeHttpLogsRepository()

        // When
        val state = LogsViewModel(repository, format).uiState.first { it.loaded }

        // Then
        assertEquals(LogsUiState(items = emptyList(), loaded = true), state)
    }

    @Test
    fun `given the list was not collected yet when the state is read then it is not loaded`() {
        // Given
        val repository = FakeHttpLogsRepository(listOf(success(id = 1)))

        // When
        val state = LogsViewModel(repository, format).uiState.value

        // Then
        assertFalse(state.loaded)
    }

    @Test
    fun `given an open list when a new call finishes then it appears at the top`() = runTest(dispatcher) {
        // Given
        val repository = FakeHttpLogsRepository(listOf(success(id = 1)))
        val viewModel = LogsViewModel(repository, format)
        backgroundScope.launch { viewModel.uiState.collect {} }

        // When
        repository.logs.value = listOf(success(id = 2)) + repository.logs.value

        // Then
        assertEquals(listOf(2L, 1L), viewModel.uiState.value.items.map { it.id })
    }

    @Test
    fun `given recorded calls when the user clears the logs then the list becomes empty`() = runTest(dispatcher) {
        // Given
        val repository = FakeHttpLogsRepository(listOf(success(id = 1)))
        val viewModel = LogsViewModel(repository, format)
        backgroundScope.launch { viewModel.uiState.collect {} }

        // When
        viewModel.clear()

        // Then
        assertEquals(1, repository.clears)
        assertEquals(emptyList<LogItemUi>(), viewModel.uiState.value.items)
    }

    private fun success(id: Long) = HttpLogItem(
        id = id,
        method = "POST",
        url = "https://api.example.com/orders",
        startedAt = 52_315_000L,
        durationMs = 340L,
        statusCode = 201,
        error = null,
        responseSize = 356L,
    )
}
