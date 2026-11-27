package pt.tiagodiasduarte.cabitools.sdk.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import pt.tiagodiasduarte.cabitools.sdk.presentation.R
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.LogDetailScreen
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.LogDetailViewModel
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.LogsScreen
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.LogsViewModel
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.copyToClipboard
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.shareText
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.CabitoolsRoute

/**
 * The screens of the Cabitools Activity: Settings, the logs list and a log's detail.
 * Back pops the stack. On Settings, the system's default back closes the Activity.
 */
@Composable
public fun CabitoolsNavHost(dependencies: PresentationDependencies) {
    val backStack = rememberSaveable(saver = backStackSaver) {
        mutableStateListOf<CabitoolsRoute>(CabitoolsRoute.Settings)
    }
    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<CabitoolsRoute.Settings> {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.factory(dependencies.getAppInfo, dependencies.getDeviceInfo),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                SettingsScreen(uiState, onNavigate = { backStack.add(it) })
            }
            entry<CabitoolsRoute.Logs> {
                val viewModel: LogsViewModel =
                    viewModel(factory = LogsViewModel.factory(dependencies.httpLogsRepository))
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LogsScreen(
                    uiState = uiState,
                    onBack = { backStack.removeAt(backStack.lastIndex) },
                    onClear = viewModel::clear,
                    onOpen = { id -> backStack.add(CabitoolsRoute.LogDetail(id)) },
                )
            }
            entry<CabitoolsRoute.LogDetail> { route ->
                val viewModel: LogDetailViewModel = viewModel(
                    factory = LogDetailViewModel.factory(route.id, dependencies.httpLogsRepository),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val copyLabel = stringResource(R.string.cabitools_detail_body)
                LogDetailScreen(
                    uiState = uiState,
                    onBack = { backStack.removeAt(backStack.lastIndex) },
                    onCopy = { body -> context.copyToClipboard(copyLabel, body) },
                    onShare = context::shareText,
                )
            }
        },
    )
}

private val backStackSaver: Saver<SnapshotStateList<CabitoolsRoute>, ArrayList<String>> = Saver(
    save = { stack -> ArrayList(stack.map(CabitoolsRoute::encode)) },
    restore = { saved -> mutableStateListOf<CabitoolsRoute>().apply { addAll(saved.map(CabitoolsRoute::decode)) } },
)
