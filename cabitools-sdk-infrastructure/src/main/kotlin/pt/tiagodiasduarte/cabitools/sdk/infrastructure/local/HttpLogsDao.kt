package pt.tiagodiasduarte.cabitools.sdk.infrastructure.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/** The columns the logs list shows. Bodies and headers are left out on purpose. */
internal data class HttpLogSummary(
    val id: Long,
    val method: String,
    val url: String,
    val requestedAt: Long,
    val durationMs: Long,
    val responseCode: Int?,
    val error: String?,
    val responseBodySize: Long?,
)

/** A log with its bodies, for the detail screen. */
internal data class HttpLogWithBodies(
    @Embedded val log: HttpLogEntity,
    @Relation(parentColumn = "id", entityColumn = "logId")
    val bodies: HttpLogBodiesEntity?,
)

@Dao
internal abstract class HttpLogsDao {

    @Insert
    protected abstract suspend fun insertLog(log: HttpLogEntity): Long

    @Insert
    protected abstract suspend fun insertBodies(bodies: HttpLogBodiesEntity)

    /** Saves a log and its bodies together, so a log never exists without them. Returns its id. */
    @Transaction
    open suspend fun insert(log: HttpLogEntity, requestBody: String?, responseBody: String?): Long {
        val id = insertLog(log)
        insertBodies(HttpLogBodiesEntity(logId = id, requestBody = requestBody, responseBody = responseBody))
        return id
    }

    /** Logs started at or after [since], newest first. */
    @Query(
        "SELECT id, method, url, requestedAt, durationMs, responseCode, error, responseBodySize " +
            "FROM http_logs WHERE requestedAt >= :since ORDER BY requestedAt DESC, id DESC",
    )
    abstract fun observeSummaries(since: Long): Flow<List<HttpLogSummary>>

    @Transaction
    @Query("SELECT * FROM http_logs WHERE id = :id")
    abstract suspend fun getWithBodies(id: Long): HttpLogWithBodies?

    /** Deletes every log; bodies go with them. */
    @Query("DELETE FROM http_logs")
    abstract suspend fun clear()

    /** Deletes logs started before [before]. Returns how many were deleted. */
    @Query("DELETE FROM http_logs WHERE requestedAt < :before")
    abstract suspend fun deleteOlderThan(before: Long): Int

    /** Keeps only the [max] newest logs. Returns how many were deleted. */
    @Query(
        "DELETE FROM http_logs WHERE id NOT IN " +
            "(SELECT id FROM http_logs ORDER BY requestedAt DESC, id DESC LIMIT :max)",
    )
    abstract suspend fun keepNewest(max: Int): Int
}
