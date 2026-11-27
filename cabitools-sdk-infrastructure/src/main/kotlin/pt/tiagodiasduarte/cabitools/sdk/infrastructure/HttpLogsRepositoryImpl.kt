package pt.tiagodiasduarte.cabitools.sdk.infrastructure

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.local.HttpLogsStore
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogItem
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.HttpLogsRepository
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.mapper.toDetail
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.mapper.toItem

internal class HttpLogsRepositoryImpl(private val store: HttpLogsStore) : HttpLogsRepository {

    override fun observeLogs(): Flow<List<HttpLogItem>> =
        store.observeSummaries().map { summaries -> summaries.map { it.toItem() } }

    override suspend fun getLog(id: Long): HttpLogDetail? = store.get(id)?.toDetail()

    override suspend fun clearLogs() = store.clear()
}
