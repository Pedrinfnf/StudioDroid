// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import android.util.AtomicFile
import org.json.JSONObject
import org.studiodroid.core.*
import java.io.File
import java.util.UUID

/** Last session metadata only. No session is created by installing/opening the launcher. */
class SessionRepository(private val directory: File) {
    private val journal = AtomicFile(File(directory, "last-session.json"))
    var issue: String? = null; private set
    @Synchronized fun load(): SessionRecord? {
        issue = null
        if (!journal.baseFile.exists() && !File(journal.baseFile.path + ".bak").exists()) return null
        return try {
            val text = journal.openRead().use { input ->
                val buffer = ByteArray(64 * 1024 + 1)
                var used = 0
                while (used < buffer.size) { val n = input.read(buffer, used, buffer.size - used); if (n < 0) break; used += n }
                require(used <= 64 * 1024)
                String(buffer, 0, used, Charsets.UTF_8)
            }
            val json = JSONObject(text); require(json.getInt("schemaVersion") == 1)
            val result = json.getJSONObject("result")
            val exit = if (result.isNull("exitCode")) null else result.getInt("exitCode")
            val signal = if (result.isNull("signal")) null else result.getInt("signal")
            val errno = if (result.isNull("errno")) null else result.getInt("errno")
            val kind = result.getString("kind")
            require(kind == "exited" || exit == null); require(kind == "signaled" || signal == null); require(kind == "start-failed" || errno == null)
            val decoded = when (kind) {
                "running" -> SessionResult.Running
                "exited" -> SessionResult.Exited(requireNotNull(exit))
                "signaled" -> SessionResult.Signaled(requireNotNull(signal))
                "start-failed" -> SessionResult.StartFailed(errno, if (result.isNull("message")) null else result.getString("message"))
                "cancelled" -> SessionResult.Cancelled
                "unknown" -> SessionResult.Unknown
                else -> error("Unknown session result")
            }
            val record = SessionRecord(UUID.fromString(json.getString("id")), SessionPhase.valueOf(json.getString("phase")), decoded, json.getLong("timestampMillis"), json.optBoolean("recoveryRequired"))
            if (decoded == SessionResult.Running) record.copy(result = SessionResult.Unknown, recoveryRequired = true).also(::save) else record
        } catch (_: Exception) {
            issue = "Saved session could not be verified; its journal has been retained"
            null
        }
    }
    @Synchronized fun save(record: SessionRecord) {
        check(directory.isDirectory || directory.mkdirs())
        val json = JSONObject().put("schemaVersion", 1).put("id", record.id.toString()).put("phase", record.phase.name)
            .put("timestampMillis", record.timestampMillis).put("recoveryRequired", record.recoveryRequired).put("result", resultJson(record.result))
        val bytes = json.toString().toByteArray(Charsets.UTF_8); require(bytes.size <= 64 * 1024)
        val stream = journal.startWrite()
        try { stream.write(bytes); journal.finishWrite(stream) } catch (error: Exception) { journal.failWrite(stream); throw error }
    }
    fun diskBytes(): Long = journal.baseFile.length()
    companion object {
        fun resultJson(result: SessionResult): JSONObject {
            var exit: Int? = null; var signal: Int? = null; var errno: Int? = null; var message: String? = null
            val kind = when (result) {
                SessionResult.Running -> "running"
                is SessionResult.Exited -> { exit = result.exitCode; "exited" }
                is SessionResult.Signaled -> { signal = result.signal; "signaled" }
                is SessionResult.StartFailed -> { errno = result.errno; message = result.message; "start-failed" }
                SessionResult.Cancelled -> "cancelled"
                SessionResult.Unknown -> "unknown"
            }
            return JSONObject().put("kind", kind).put("exitCode", exit ?: JSONObject.NULL).put("signal", signal ?: JSONObject.NULL)
                .put("errno", errno ?: JSONObject.NULL).put("message", message ?: JSONObject.NULL)
        }
    }
}
