package pt.tiagodiasduarte.cabitools.sdk.infrastructure.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader

/** One finished HTTP call, without bodies. Bodies live in [HttpLogBodiesEntity] so the list stays light. */
@Entity(tableName = "http_logs", indices = [Index("requestedAt")])
internal data class HttpLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val method: String,
    val url: String,
    /** Epoch millis when the call started. */
    val requestedAt: Long,
    val durationMs: Long,
    val protocol: String?,
    /** Null when no response arrived; [error] is set instead. */
    val responseCode: Int?,
    val responseMessage: String?,
    val error: String?,
    /** Already redacted, with hidden headers removed. */
    val requestHeaders: List<HttpHeader>,
    val requestContentType: String?,
    /** Real size in bytes, even when the stored body is truncated. Null when unknown or absent. */
    val requestBodySize: Long?,
    val requestBodyTruncated: Boolean,
    val responseHeaders: List<HttpHeader>,
    val responseContentType: String?,
    val responseBodySize: Long?,
    val responseBodyTruncated: Boolean,
)

/** The stored request and response bodies of one log. Deleted with its log. */
@Entity(
    tableName = "http_log_bodies",
    foreignKeys = [
        ForeignKey(
            entity = HttpLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["logId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class HttpLogBodiesEntity(
    @PrimaryKey val logId: Long,
    /** At most `maxContentLength` bytes; null for binary, uncaptured or absent bodies. */
    val requestBody: String?,
    val responseBody: String?,
)
