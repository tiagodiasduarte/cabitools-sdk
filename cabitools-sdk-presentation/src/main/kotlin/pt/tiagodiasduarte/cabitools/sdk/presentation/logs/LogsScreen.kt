package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.tiagodiasduarte.cabitools.sdk.presentation.R
import pt.tiagodiasduarte.cabitools.sdk.presentation.theme.CabitoolsTheme

internal const val LOGS_LIST_TAG = "cabitools_logs_list"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogsScreen(
    uiState: LogsUiState,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onOpen: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cabitools_logs_title)) },
                navigationIcon = {
                    ToolbarIcon(R.drawable.cabitools_ic_arrow_back, R.string.cabitools_back, onBack)
                },
                actions = { ToolbarIcon(R.drawable.cabitools_ic_delete, R.string.cabitools_logs_clear, onClear) },
            )
        },
    ) { innerPadding ->
        if (uiState.loaded && uiState.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.cabitools_logs_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.testTag(LOGS_LIST_TAG), contentPadding = innerPadding) {
                items(uiState.items, key = { it.id }) { item ->
                    LogItem(item, onOpen)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
internal fun ToolbarIcon(iconRes: Int, descriptionRes: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(painterResource(iconRes), contentDescription = stringResource(descriptionRes))
    }
}

@Composable
private fun LogItem(item: LogItemUi, onOpen: (Long) -> Unit) {
    ListItem(
        overlineContent = {
            Row {
                Text(item.method, fontWeight = FontWeight.Bold)
                Text(
                    text = item.status ?: stringResource(R.string.cabitools_logs_error),
                    color = if (item.isFailure) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        },
        headlineContent = { Text(item.url, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text(listOfNotNull(item.time, item.duration, item.size).joinToString("   ")) },
        modifier = Modifier.clickable { onOpen(item.id) },
    )
}

/** Failed calls and 4xx/5xx responses are shown in the error color. */
private val LogItemUi.isFailure: Boolean
    get() = (status?.toIntOrNull() ?: Int.MAX_VALUE) >= FIRST_ERROR_STATUS

private const val FIRST_ERROR_STATUS = 400

private val previewState = LogsUiState(
    loaded = true,
    items = listOf(
        LogItemUi(2, "GET", "https://api.example.com/users/42", "14:32:07", "120 ms", "1.2 kB", "200"),
        LogItemUi(1, "POST", "https://api.example.com/orders", "14:31:55", "340 ms", "356 B", "201"),
        LogItemUi(0, "DELETE", "https://api.example.com/orders/9", "14:30:12", "10000 ms", null, null),
    ),
)

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LogsScreenPreview() {
    CabitoolsTheme(dynamicColor = false) { LogsScreen(previewState, onBack = {}, onClear = {}, onOpen = {}) }
}

@Preview(name = "Empty")
@Composable
private fun LogsScreenEmptyPreview() {
    CabitoolsTheme(dynamicColor = false) {
        LogsScreen(LogsUiState(loaded = true), onBack = {}, onClear = {}, onOpen = {})
    }
}
