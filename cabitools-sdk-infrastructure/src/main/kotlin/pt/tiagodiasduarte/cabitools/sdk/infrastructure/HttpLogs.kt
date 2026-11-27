package pt.tiagodiasduarte.cabitools.sdk.infrastructure

import android.content.Context
import okhttp3.Interceptor
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.HttpLogsRepository
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.http.HeaderRules
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.http.HttpLogsInterceptor
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.local.HttpLogsStore

/**
 * Entry point to the HTTP logs infrastructure. The store, interceptor, DAO and Room entities stay
 * internal to this module; other modules only get an interceptor and a repository.
 */
public object HttpLogs {

    /**
     * An interceptor that records calls into the process-wide store. One database serves the
     * process, so the most recently set [retentionMillis] applies.
     */
    public fun interceptor(
        context: Context,
        retentionMillis: Long,
        headerRules: HeaderRules,
        maxContentLength: Long,
    ): Interceptor {
        val store = HttpLogsStore.get(context)
        store.retentionMillis = retentionMillis
        return HttpLogsInterceptor(store, headerRules, maxContentLength)
    }

    /** The recorded calls, for the logs screens. */
    public fun repository(context: Context): HttpLogsRepository = HttpLogsRepositoryImpl(HttpLogsStore.get(context))
}
