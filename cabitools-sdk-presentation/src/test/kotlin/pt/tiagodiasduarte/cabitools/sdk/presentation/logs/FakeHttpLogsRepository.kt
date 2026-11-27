package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogItem
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.HttpLogsRepository

internal class FakeHttpLogsRepository(
    items: List<HttpLogItem> = emptyList(),
    private val details: Map<Long, HttpLogDetail> = emptyMap(),
) : HttpLogsRepository {

    val logs = MutableStateFlow(items)
    var clears = 0

    override fun observeLogs(): Flow<List<HttpLogItem>> = logs

    override suspend fun getLog(id: Long): HttpLogDetail? = details[id]

    override suspend fun clearLogs() {
        clears++
        logs.value = emptyList()
    }
}
