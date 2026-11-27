package pt.tiagodiasduarte.cabitools.sdk.presentation.mapper

import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogItem
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.LogFormat
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.LogItemUi

/** Maps a recorded call to its row in the logs list. */
internal fun HttpLogItem.toUi(format: LogFormat) = LogItemUi(
    id = id,
    method = method,
    url = url,
    time = format.time(startedAt),
    duration = format.duration(durationMs),
    size = responseSize?.let(format::size),
    status = statusCode?.toString(),
)
