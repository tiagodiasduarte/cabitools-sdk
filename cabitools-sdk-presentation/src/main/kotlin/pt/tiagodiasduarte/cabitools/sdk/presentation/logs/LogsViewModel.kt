package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.HttpLogsRepository
import pt.tiagodiasduarte.cabitools.sdk.presentation.mapper.toUi

/** One row of the logs list, already formatted. [status] is null when the call failed. */
internal data class LogItemUi(
    val id: Long,
    val method: String,
    val url: String,
    val time: String,
    val duration: String,
    val size: String?,
    val status: String?,
)

internal data class LogsUiState(
    val items: List<LogItemUi> = emptyList(),
    /** False until the first list arrives, so the empty message doesn't flash. */
    val loaded: Boolean = false,
)

internal class LogsViewModel(
    private val repository: HttpLogsRepository,
    private val format: LogFormat = LogFormat(),
) : ViewModel() {

    val uiState: StateFlow<LogsUiState> = repository.observeLogs()
        .map { logs -> LogsUiState(items = logs.map { it.toUi(format) }, loaded = true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LogsUiState())

    fun clear() {
        viewModelScope.launch { repository.clearLogs() }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(repository: HttpLogsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { LogsViewModel(repository) }
        }
    }
}
