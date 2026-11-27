package pt.tiagodiasduarte.cabitools.sdk.presentation.logs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpMessage

/** Copies [text] to the clipboard under [label]. */
internal fun Context.copyToClipboard(label: String, text: String) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}

/** Opens the Android share sheet with [text]. */
internal fun Context.shareText(text: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/**
 * A plain-text report of one call for bug reports. Header values are already redacted and hidden
 * headers were never stored. Each body is cut at [maxBodyChars] to stay below the binder limit.
 */
internal object LogShareText {

    const val MAX_BODY_CHARS = 100_000

    fun build(log: HttpLogDetail, format: LogFormat, maxBodyChars: Int = MAX_BODY_CHARS): String = buildString {
        appendLine("${log.method} ${log.url}")
        val message = log.statusMessage?.takeIf { it.isNotBlank() }
        appendLine("Status: ${listOfNotNull(log.statusCode, message).joinToString(" ").ifEmpty { "-" }}")
        appendLine("Started: ${format.timeWithMillis(log.startedAt)}")
        appendLine("Duration: ${format.duration(log.durationMs)}")
        log.error?.let { appendLine("Error: $it") }
        appendMessage("Request", log.request, format, maxBodyChars)
        appendMessage("Response", log.response, format, maxBodyChars)
    }.trimEnd()

    private fun StringBuilder.appendMessage(title: String, message: HttpMessage, format: LogFormat, maxBodyChars: Int) {
        appendLine()
        appendLine("--- $title ---")
        message.headers.forEach { appendLine("${it.name}: ${it.value}") }
        appendLine()
        val body = message.body
        val bodySize = message.bodySize
        when {
            body == null && bodySize != null && bodySize > 0 ->
                appendLine("(body of ${format.size(bodySize)} not shown)")
            body.isNullOrEmpty() -> appendLine("(no body)")
            body.length > maxBodyChars -> appendLine(body.take(maxBodyChars)).appendLine("[body truncated]")
            else -> {
                appendLine(body)
                if (message.bodyTruncated) appendLine("[body truncated]")
            }
        }
    }
}
