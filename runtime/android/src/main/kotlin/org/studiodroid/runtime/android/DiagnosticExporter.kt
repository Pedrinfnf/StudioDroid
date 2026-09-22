// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import org.json.JSONArray
import org.json.JSONObject
import org.studiodroid.core.*
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DiagnosticExporter(private val logs: RotatingLogStore) {
    /** Caller supplies an SAF stream. Only bounded launcher metadata/logs are exported. */
    fun export(snapshot: RuntimeSnapshot, destination: OutputStream) {
        val json = JSONObject().put("format", "studiodroid-launcher-diagnostics").put("schemaVersion", 1)
            .put("milestone", "M1").put("collectedAtMillis", System.currentTimeMillis()).put("profile", snapshot.profile.name)
            .put("pressure", snapshot.pressure.name).put("runtimeImplemented", false).put("redacted", true)
            .put("components", JSONArray(snapshot.components.map { JSONObject().put("kind", it.kind.name).put("state", it.state.name) }))
        snapshot.device?.let { d ->
            json.put("device", JSONObject().put("observedAtMillis", d.observedAtMillis).put("androidApi", d.androidApi)
                .put("androidVersion", d.androidVersion).put("model", d.model).put("abis", JSONArray(d.supportedAbis))
                .put("pageSize", d.pageSize.valueOrNull() ?: JSONObject.NULL).put("soc", d.soc.valueOrNull() ?: JSONObject.NULL)
                .put("gpuVendor", d.gpuVendor.name).put("gpuModel", d.gpuModel.valueOrNull() ?: JSONObject.NULL)
                .put("advertisedVulkanVersion", d.advertisedVulkanVersion.valueOrNull() ?: JSONObject.NULL)
                .put("vulkanDriverVersion", JSONObject.NULL).put("vulkanExtensions", JSONObject.NULL)
                .put("physicalBytes", d.memory.physicalBytes ?: JSONObject.NULL).put("availableBytes", d.memory.availableBytes ?: JSONObject.NULL)
                .put("processPssBytes", d.memory.processPssBytes ?: JSONObject.NULL).put("swapTotalBytes", d.memory.swapTotalBytes ?: JSONObject.NULL)
                .put("swapFreeBytes", d.memory.swapFreeBytes ?: JSONObject.NULL).put("thermalStatus", d.thermalStatus.valueOrNull() ?: JSONObject.NULL))
        }
        snapshot.lastSession?.let { session ->
            json.put("lastSession", JSONObject().put("id", session.id.toString()).put("phase", session.phase.name)
                .put("result", SessionRepository.resultJson(session.result).put("message",
                    if (session.result is SessionResult.StartFailed) "Startup failure; private details omitted" else JSONObject.NULL))
                .put("recoveryRequired", session.recoveryRequired))
        }
        val metadata = json.toString(2).toByteArray(Charsets.UTF_8); require(metadata.size <= 64 * 1024)
        // The tail is capped to LOW_MEMORY for every export, independently of user overrides.
        val tail = logs.readTail(128 * 1024).toByteArray(Charsets.UTF_8)
        ZipOutputStream(destination).use { zip ->
            zip.putNextEntry(ZipEntry("launcher.json")); zip.write(metadata); zip.closeEntry()
            zip.putNextEntry(ZipEntry("logs/launcher.jsonl")); zip.write(tail); zip.closeEntry()
        }
    }
}
