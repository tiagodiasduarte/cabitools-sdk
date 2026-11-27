package pt.tiagodiasduarte.cabitools.sdk.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetAppInfoUseCase
import pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetDeviceInfoUseCase
import pt.tiagodiasduarte.cabitools.sdk.presentation.mapper.logsSection
import pt.tiagodiasduarte.cabitools.sdk.presentation.mapper.toSection
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.SettingsUiState

internal class SettingsViewModel(
    private val getAppInfo: GetAppInfoUseCase,
    private val getDeviceInfo: GetDeviceInfoUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val sections = withContext(dispatcher) {
                // Order is part of the contract: Application, Device, then Logs.
                listOf(
                    getAppInfo().toSection(),
                    getDeviceInfo().toSection(),
                    logsSection(),
                )
            }
            _uiState.value = SettingsUiState(sections = sections)
        }
    }

    companion object {
        fun factory(getAppInfo: GetAppInfoUseCase, getDeviceInfo: GetDeviceInfoUseCase): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { SettingsViewModel(getAppInfo, getDeviceInfo) }
            }
    }
}
