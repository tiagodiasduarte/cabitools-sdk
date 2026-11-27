package pt.tiagodiasduarte.cabitools.sdk.infrastructure.local

import android.content.Context
import android.database.SQLException
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.http.HttpLogSink

/**
 * Process-wide access to the HTTP logs database. Writes run on one coroutine at a time, so a
 * call never waits for the database and inserts never race with pruning. Database errors are
 * logged, never thrown into the host app's HTTP call.
 */
internal class HttpLogsStore(
    private val dao: HttpLogsDao,
    private val writeScope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis,
) : HttpLogSink {

    /** How long logs are kept, in milliseconds: the retention of the most recently created interceptor. */
    @Volatile
    var retentionMillis: Long = DEFAULT_RETENTION_MILLIS

    /**
     * Saves a finished call in the background, then deletes expired and excess logs. Callers
     * don't wait for it; the returned job lets tests know when the write finished.
     */
    override fun save(log: HttpLogEntity, requestBody: String?, responseBody: String?) {
        saveAsync(log, requestBody, responseBody)
    }

    /** Like [save], returning the write so tests can wait for it. */
    fun saveAsync(log: HttpLogEntity, requestBody: String?, responseBody: String?): Job = writeScope.launch {
        safely("save an HTTP log") {
            dao.insert(log, requestBody, responseBody)
            prune()
        }
    }

    /** Logs within the retention period, newest first. Collecting it first deletes expired logs. */
    fun observeSummaries(): Flow<List<HttpLogSummary>> = flow {
        safely("prune HTTP logs") { dao.deleteOlderThan(cutoff()) }
        emitAll(dao.observeSummaries(since = cutoff()))
    }

    suspend fun get(id: Long): HttpLogWithBodies? = dao.getWithBodies(id)

    suspend fun clear() = dao.clear()

    private suspend fun prune() {
        dao.deleteOlderThan(cutoff())
        dao.keepNewest(MAX_LOGS)
    }

    private fun cutoff(): Long = clock() - retentionMillis

    private suspend fun safely(action: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: SQLException) {
            Log.w(TAG, "Could not $action", e)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Could not $action", e)
        }
    }

    companion object {
        const val MAX_LOGS = 500
        private val DEFAULT_RETENTION_MILLIS = TimeUnit.DAYS.toMillis(1)
        private const val TAG = "CabitoolsHttpLogs"

        @Volatile
        private var instance: HttpLogsStore? = null

        /** The one store of this process, created on first use from the application context. */
        fun get(context: Context, dispatcher: CoroutineDispatcher = Dispatchers.IO): HttpLogsStore =
            instance ?: synchronized(this) {
                instance ?: HttpLogsStore(
                    dao = HttpLogsDatabase.create(context).httpLogsDao(),
                    writeScope = CoroutineScope(SupervisorJob() + dispatcher.limitedParallelism(1)),
                ).also { instance = it }
            }
    }
}
