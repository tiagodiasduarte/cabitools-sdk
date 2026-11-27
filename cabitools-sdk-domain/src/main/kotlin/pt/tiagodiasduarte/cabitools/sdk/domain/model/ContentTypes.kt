package pt.tiagodiasduarte.cabitools.sdk.domain.model

/** Which bodies are text: stored as text when recorded, and shown as text in the detail screen. */
public object ContentTypes {

    private val TEXT_SUBTYPES = setOf("json", "xml", "x-www-form-urlencoded", "javascript", "graphql")

    /** [contentType] is a header value such as `application/json; charset=utf-8`. No content type counts as text. */
    public fun isText(contentType: String?): Boolean {
        val (type, subtype) = parse(contentType) ?: return true
        return type == "text" || subtype in TEXT_SUBTYPES || subtype.endsWith("+json") || subtype.endsWith("+xml")
    }

    public fun isJson(contentType: String?): Boolean {
        val (_, subtype) = parse(contentType) ?: return false
        return subtype == "json" || subtype.endsWith("+json")
    }

    private fun parse(contentType: String?): Pair<String, String>? {
        val mime = contentType?.substringBefore(';')?.trim()?.lowercase()
        return mime?.split('/')?.takeIf { it.size == 2 }?.let { it[0] to it[1] }
    }
}
