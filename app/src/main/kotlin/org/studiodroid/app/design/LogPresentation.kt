// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.design

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Display-only parsing of the existing bounded tail. No fabricated events or timestamps. */
data class LogEntry(val time: String, val category: String, val code: String, val session: String?, val value: String?, val malformed: Boolean = false)
object LogPresentation {
    fun parse(tail: String): List<LogEntry> {
        val format = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
        return tail.take(16 * 1024).lineSequence().filter { it.isNotBlank() }.map { line ->
            try {
                val json = JSONObject(line)
                val timestamp = (json.opt("timestampMillis") as? Number)?.takeIf { it is Long || it is Int }
                LogEntry(timestamp?.toLong()?.takeIf { it >= 0 }?.let { format.format(Date(it)) } ?: "Unknown time",
                    json.optString("category", "UNKNOWN").take(48), json.optString("code", "UNKNOWN").take(160),
                    if (json.isNull("sessionId")) null else json.optString("sessionId").take(80),
                    if (json.isNull("value")) null else json.opt("value")?.toString()?.take(80))
            } catch (_: Exception) { LogEntry("Unknown time", "UNPARSED", "Entry could not be decoded", null, null, true) }
        }.toList().asReversed()
    }
}
