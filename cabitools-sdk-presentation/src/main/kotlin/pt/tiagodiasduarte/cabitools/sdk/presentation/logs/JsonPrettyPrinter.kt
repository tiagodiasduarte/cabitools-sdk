package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Indents JSON for reading. Anything that isn't one complete JSON value, truncated JSON
 * included, is returned as is.
 */
internal object JsonPrettyPrinter {

    private const val INDENT = 2

    fun format(body: String): String = try {
        val tokener = JSONTokener(body)
        val pretty = when (val value = tokener.nextValue()) {
            is JSONObject -> value.toString(INDENT)
            is JSONArray -> value.toString(INDENT)
            else -> null
        }
        // org.json escapes "/" as "\/"; JSON allows both, and the plain form is easier to read.
        if (pretty == null || tokener.more()) body else pretty.replace("\\/", "/")
    } catch (_: JSONException) {
        body
    }
}
