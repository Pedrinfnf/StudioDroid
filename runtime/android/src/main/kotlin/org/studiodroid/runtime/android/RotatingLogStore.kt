// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import org.studiodroid.core.DiagnosticEvent
import java.io.File
import java.io.RandomAccessFile

/** Synchronous bounded I/O on the caller's I/O dispatcher; no in-memory logging queue. */
class RotatingLogStore(private val directory: File) {
    private var retainedBytes = 16L * 1024 * 1024
    private fun file(index: Int) = File(directory, "launcher-$index.jsonl")
    @Synchronized fun configure(bytes: Long) {
        require(bytes in 4096..64L * 1024 * 1024)
        retainedBytes = bytes
        for (index in 0..3) if (file(index).length() > retainedBytes / 4) check(file(index).delete()) { "Log budget cleanup failed" }
    }
    @Synchronized fun append(event: DiagnosticEvent) {
        check(directory.isDirectory || directory.mkdirs()) { "Log directory unavailable" }
        // All text originates in enums/UUID; no arbitrary message, project path or credential fields.
        val line = """{"timestampMillis":${event.timestampMillis},"category":"${event.category.name}","code":"${event.code.name}","sessionId":${event.sessionId?.let { "\"$it\"" } ?: "null"},"value":${event.value ?: "null"}}""" + "\n"
        val bytes = line.toByteArray(Charsets.UTF_8)
        check(bytes.size <= 512)
        if (file(0).length() + bytes.size > retainedBytes / 4) rotate()
        file(0).appendBytes(bytes)
    }
    private fun rotate() {
        check(!file(3).exists() || file(3).delete()) { "Log rotation failed" }
        for (index in 2 downTo 0) if (file(index).exists()) check(file(index).renameTo(file(index + 1))) { "Log rotation failed" }
    }
    @Synchronized fun diskBytes(): Long = (0..3).sumOf { file(it).length() }
    @Synchronized fun readTail(maxBytes: Int): String {
        require(maxBytes in 1..512 * 1024)
        var remaining = maxBytes
        val parts = mutableListOf<String>()
        for (index in 0..3) {
            if (remaining == 0) break
            val path = file(index); if (!path.isFile) continue
            RandomAccessFile(path, "r").use { input ->
                val count = minOf(input.length(), remaining.toLong()).toInt()
                val start = input.length() - count
                input.seek(start)
                val bytes = ByteArray(count); input.readFully(bytes)
                var text = bytes.toString(Charsets.UTF_8)
                if (start > 0) text = text.substringAfter('\n', "")
                parts.add(0, text)
                remaining -= count
            }
        }
        return parts.joinToString("")
    }
}
