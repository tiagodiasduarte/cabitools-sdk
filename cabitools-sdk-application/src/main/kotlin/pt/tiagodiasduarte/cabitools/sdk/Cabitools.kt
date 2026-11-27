package pt.tiagodiasduarte.cabitools.sdk

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import okhttp3.Interceptor
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.HttpLogs
import pt.tiagodiasduarte.cabitools.sdk.mapper.toHeaderRules

/**
 * Entry point of the Cabitools SDK.
 *
 * ```
 * Cabitools.open(context)
 * ```
 */
public object Cabitools {

    /**
     * Opens the Cabitools screen on top of the current screen.
     *
     * Any [Context] works. When it is not (or does not wrap) an [Activity], for example the
     * Application context, the screen is started with [Intent.FLAG_ACTIVITY_NEW_TASK].
     * Pressing back closes the screen and returns to the caller.
     */
    @JvmStatic
    public fun open(context: Context) {
        val intent = Intent(context, CabitoolsActivity::class.java)
        if (context.findActivity() == null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Creates the HTTP logs interceptor. Add it to the OkHttp client your Retrofit instance uses:
     *
     * ```
     * OkHttpClient.Builder()
     *     .addInterceptor(Cabitools.httpInterceptor(context) { retention = Retention.ONE_DAY })
     * ```
     *
     * Every call through that client is recorded and shown in the Cabitools screen.
     * Use `:sdk-no-op` in production builds: its interceptor records nothing.
     */
    @JvmStatic
    public fun httpInterceptor(context: Context, configure: HttpLogsConfig.() -> Unit = {}): Interceptor {
        val config = HttpLogsConfig().apply(configure)
        // One database per process: the most recently created interceptor's retention applies.
        return HttpLogs.interceptor(context, config.retention.millis, config.toHeaderRules(), config.maxContentLength)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
