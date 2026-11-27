package pt.tiagodiasduarte.cabitools.sdk.presentation

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.tiagodiasduarte.cabitools.sdk.presentation.R
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.CabitoolsRoute
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.InfoRowUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.NavigationRowUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.SectionUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.SettingsUiState
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.UiText
import pt.tiagodiasduarte.cabitools.sdk.presentation.theme.CabitoolsTheme

internal const val SETTINGS_LIST_TAG = "cabitools_list"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    onNavigate: (CabitoolsRoute) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.cabitools_title)) }) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.testTag(SETTINGS_LIST_TAG),
            contentPadding = innerPadding,
        ) {
            uiState.sections.forEachIndexed { index, section ->
                if (index > 0) {
                    item(key = "divider_$index") { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
                }
                item(key = "title_$index") { SectionTitle(section) }
                itemsIndexed(section.rows, key = { rowIndex, _ -> "row_${index}_$rowIndex" }) { _, row ->
                    when (row) {
                        is InfoRowUi -> InfoRow(row)
                        is NavigationRowUi -> NavigationRow(row, onNavigate)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(section: SectionUi) {
    Text(
        text = stringResource(section.titleRes),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}

@Composable
private fun InfoRow(row: InfoRowUi) {
    ListItem(
        headlineContent = { Text(stringResource(row.labelRes)) },
        supportingContent = { Text(row.value.asString()) },
    )
}

@Composable
private fun NavigationRow(row: NavigationRowUi, onNavigate: (CabitoolsRoute) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(row.labelRes)) },
        trailingContent = { Icon(painterResource(R.drawable.cabitools_ic_chevron_right), contentDescription = null) },
        modifier = Modifier.clickable { onNavigate(row.destination) },
    )
}

@Composable
private fun UiText.asString(): String = when (this) {
    is UiText.Plain -> value
    is UiText.Resource -> stringResource(resId)
}

private val previewState = SettingsUiState(
    sections = listOf(
        SectionUi(
            R.string.cabitools_section_application,
            listOf(
                InfoRowUi(R.string.cabitools_label_version_name, UiText.Plain("2.3.0")),
                InfoRowUi(R.string.cabitools_label_version_code, UiText.Plain("230")),
                InfoRowUi(R.string.cabitools_label_app_id, UiText.Plain("com.example.host")),
            ),
        ),
        SectionUi(
            R.string.cabitools_section_device,
            listOf(
                InfoRowUi(R.string.cabitools_label_model, UiText.Plain("Google Pixel 8")),
                InfoRowUi(R.string.cabitools_label_android_version, UiText.Plain("15 (API 35)")),
            ),
        ),
        SectionUi(
            R.string.cabitools_section_logs,
            listOf(NavigationRowUi(R.string.cabitools_label_open_logs, CabitoolsRoute.Logs)),
        ),
    ),
)

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    CabitoolsTheme(dynamicColor = false) { SettingsScreen(previewState) }
}
