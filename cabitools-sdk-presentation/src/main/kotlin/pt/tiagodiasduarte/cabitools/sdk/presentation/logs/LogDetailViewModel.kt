package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.HttpLogsRepository
import pt.tiagodiasduarte.cabitools.sdk.presentation.mapper.toUi

/** A label and its value on the Overview tab. */
internal data class OverviewRowUi(@StringRes val labelRes: Int, val value: String)

/** How a stored body is shown. */
internal sealed interface BodyUi {
    /** [text] is pretty-printed when it is valid JSON. */
    data class Text(val text: String, val truncated: Boolean) : BodyUi
    data class Binary(val size: String) : BodyUi
    /** [size] is null when the app closed the body before reading it and its length was unknown. */
    data class NotCaptured(val size: String?) : BodyUi
    data object Empty : BodyUi
}

internal data class MessageUi(val headers: List<HttpHeader>, val body: BodyUi)

internal data class LogDetailUi(
    /** For example `GET /users/42`. */
    val title: String,
    val overview: List<OverviewRowUi>,
    val request: MessageUi,
    val response: MessageUi,
    /** The whole call as text, for the share action. */
    val shareText: String,
)

internal data class LogDetailUiState(
    val loaded: Boolean = false,
    /** Null once loaded means the log no longer exists (cleared or expired). */
    val detail: LogDetailUi? = null,
)

internal class LogDetailViewModel(
    private val id: Long,
    private val repository: HttpLogsRepository,
    private val format: LogFormat = LogFormat(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogDetailUiState())
    val uiState: StateFlow<LogDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = LogDetailUiState(loaded = true, detail = repository.getLog(id)?.toUi(format))
        }
    }

    companion object {
        fun factory(id: Long, repository: HttpLogsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { LogDetailViewModel(id, repository) }
        }
    }
}
