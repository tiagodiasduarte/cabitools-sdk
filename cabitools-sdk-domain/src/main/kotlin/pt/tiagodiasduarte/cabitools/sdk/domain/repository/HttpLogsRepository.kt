package pt.tiagodiasduarte.cabitools.sdk.domain.repository

import kotlinx.coroutines.flow.Flow
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogItem

public interface HttpLogsRepository {
    /** Logs within the retention period, newest first, updated as calls finish. */
    public fun observeLogs(): Flow<List<HttpLogItem>>

    public suspend fun getLog(id: Long): HttpLogDetail?

    public suspend fun clearLogs()
}
