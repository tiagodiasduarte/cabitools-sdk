package pt.tiagodiasduarte.cabitools.sdk.mapper

import pt.tiagodiasduarte.cabitools.sdk.HttpLogsConfig
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.http.HeaderRules

/** Maps the public header options onto the infrastructure's header rules. */
internal fun HttpLogsConfig.toHeaderRules(): HeaderRules = HeaderRules(
    redactDefaultHeaders = redactDefaultHeaders,
    redactedHeaders = redactedHeaders,
    hiddenHeaders = hiddenHeaders,
)
