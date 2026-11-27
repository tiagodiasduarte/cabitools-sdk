package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.tiagodiasduarte.cabitools.sdk.presentation.R
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader
import pt.tiagodiasduarte.cabitools.sdk.presentation.theme.CabitoolsTheme

internal const val LOG_DETAIL_CONTENT_TAG = "cabitools_log_detail_content"

private val TABS = listOf(
    R.string.cabitools_detail_tab_overview,
    R.string.cabitools_detail_tab_request,
    R.string.cabitools_detail_tab_response,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogDetailScreen(
    uiState: LogDetailUiState,
    onBack: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val detail = uiState.detail
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detail?.title ?: stringResource(R.string.cabitools_logs_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    ToolbarIcon(R.drawable.cabitools_ic_arrow_back, R.string.cabitools_back, onBack)
                },
                actions = { if (detail != null) DetailActions(detail, selectedTab, onCopy, onShare) },
            )
        },
    ) { innerPadding ->
        when {
            detail != null -> Column(Modifier.padding(innerPadding)) {
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    TABS.forEachIndexed { index, titleRes ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(stringResource(titleRes)) },
                        )
                    }
                }
                LazyColumn(Modifier.testTag(LOG_DETAIL_CONTENT_TAG)) {
                    when (selectedTab) {
                        0 -> overview(detail.overview)
                        1 -> message(detail.request)
                        else -> message(detail.response)
                    }
                }
            }
            uiState.loaded -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.cabitools_detail_not_found),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Copy (on a tab with a text body) and share. */
@Composable
private fun DetailActions(detail: LogDetailUi, tab: Int, onCopy: (String) -> Unit, onShare: (String) -> Unit) {
    detail.bodyOf(tab)?.let { body ->
        ToolbarIcon(R.drawable.cabitools_ic_copy, R.string.cabitools_detail_copy_body) { onCopy(body) }
    }
    ToolbarIcon(R.drawable.cabitools_ic_share, R.string.cabitools_detail_share) { onShare(detail.shareText) }
}

/** The text body shown on the Request (1) or Response (2) tab, if any: what the copy action copies. */
private fun LogDetailUi.bodyOf(tab: Int): String? = when (tab) {
    1 -> request.body
    2 -> response.body
    else -> null
}.let { (it as? BodyUi.Text)?.text }

private fun LazyListScope.overview(rows: List<OverviewRowUi>) {
    items(rows, key = { it.labelRes }) { row ->
        ListItem(
            overlineContent = { Text(stringResource(row.labelRes)) },
            headlineContent = { Text(row.value) },
        )
    }
}

private fun LazyListScope.message(message: MessageUi) {
    item(key = "headers_title") { SectionTitle(R.string.cabitools_detail_headers) }
    if (message.headers.isEmpty()) {
        item(key = "no_headers") { Note(stringResource(R.string.cabitools_detail_no_headers)) }
    } else {
        itemsIndexed(message.headers, key = { index, _ -> "header_$index" }) { _, header -> HeaderLine(header) }
    }
    item(key = "body_title") { SectionTitle(R.string.cabitools_detail_body) }
    when (val body = message.body) {
        is BodyUi.Text -> {
            if (body.truncated) {
                item(key = "truncated") { Note(stringResource(R.string.cabitools_detail_body_truncated)) }
            }
            // One item per line keeps a 250 kB body scrollable.
            itemsIndexed(body.text.lines(), key = { index, _ -> "body_$index" }) { _, line -> CodeLine(line) }
        }
        is BodyUi.Binary -> item(key = "binary") {
            Note(stringResource(R.string.cabitools_detail_body_binary, body.size))
        }
        is BodyUi.NotCaptured -> item(key = "not_captured") {
            Note(
                body.size?.let { stringResource(R.string.cabitools_detail_body_not_captured, it) }
                    ?: stringResource(R.string.cabitools_detail_body_not_read),
            )
        }
        BodyUi.Empty -> item(key = "empty") { Note(stringResource(R.string.cabitools_detail_body_empty)) }
    }
}

@Composable
private fun SectionTitle(titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}

@Composable
private fun HeaderLine(header: HttpHeader) {
    CodeLine("${header.name}: ${header.value}")
}

@Composable
private fun CodeLine(text: String) {
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun Note(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

private val previewDetail = LogDetailUi(
    title = "GET /users/42",
    overview = listOf(
        OverviewRowUi(R.string.cabitools_detail_url, "https://api.example.com/users/42"),
        OverviewRowUi(R.string.cabitools_detail_method, "GET"),
        OverviewRowUi(R.string.cabitools_detail_status, "200 OK"),
        OverviewRowUi(R.string.cabitools_detail_duration, "120 ms"),
    ),
    request = MessageUi(listOf(HttpHeader("Authorization", "****")), BodyUi.Empty),
    response = MessageUi(
        listOf(HttpHeader("Content-Type", "application/json")),
        BodyUi.Text("{\n  \"id\": 42,\n  \"status\": \"ok\"\n}", truncated = false),
    ),
    shareText = "GET https://api.example.com/users/42",
)

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LogDetailScreenPreview() {
    CabitoolsTheme(dynamicColor = false) {
        LogDetailScreen(LogDetailUiState(loaded = true, detail = previewDetail), onBack = {}, onCopy = {}, onShare = {})
    }
}
