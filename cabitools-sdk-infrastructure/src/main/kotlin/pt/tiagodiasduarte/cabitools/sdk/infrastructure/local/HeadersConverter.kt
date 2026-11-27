package pt.tiagodiasduarte.cabitools.sdk.infrastructure.local

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader

/** Stores headers as a JSON list of `{"name": ..., "value": ...}` objects, keeping order and repeats. */
internal class HeadersConverter {

    @TypeConverter
    fun fromHeaders(headers: List<HttpHeader>): String = JSONArray().apply {
        headers.forEach { put(JSONObject().put(NAME, it.name).put(VALUE, it.value)) }
    }.toString()

    @TypeConverter
    fun toHeaders(json: String): List<HttpHeader> {
        val array = JSONArray(json)
        return List(array.length()) { index ->
            val header = array.getJSONObject(index)
            HttpHeader(header.getString(NAME), header.getString(VALUE))
        }
    }

    private companion object {
        const val NAME = "name"
        const val VALUE = "value"
    }
}
