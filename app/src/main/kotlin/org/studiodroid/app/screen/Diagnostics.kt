// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState
import org.studiodroid.app.R
import org.studiodroid.app.design.*
import org.studiodroid.core.*

object Diagnostics {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        val d = state.runtime.device
        val m = d?.memory
        page.section("Device intelligence", d?.model ?: "Waiting for device observations")
        page.secondary(if (state.runtime.refreshing) "Reading device…" else "Refresh observations", !state.busy && !state.runtime.refreshing, action = actions.refresh)
        page.metrics(
            Metric("CPU / SOC", observation(d?.soc), R.drawable.ic_cpu, badge = observedBadge(d?.soc)),
            Metric("ARCHITECTURE", d?.let { if (it.supportsArm64) "ARM64" else it.supportedAbis.firstOrNull() ?: "Unknown" } ?: "Unknown", R.drawable.ic_runtime, d?.let { "${if(it.process64Bit) "64" else "32"}-bit process" }),
            Metric("ANDROID", d?.androidVersion ?: "Unknown", R.drawable.ic_home, d?.let { "API ${it.androidApi}" }),
            Metric("PAGE SIZE", bytes(d?.pageSize?.valueOrNull()), R.drawable.ic_memory, badge = observedBadge(d?.pageSize)),
        )
        page.note("abis", "Reported ABIs: ${d?.supportedAbis?.joinToString() ?: "Unknown"}")
        page.section("Graphics", "Advertised capabilities do not prove runtime compatibility")
        page.metrics(
            Metric("VULKAN", observation(d?.advertisedVulkanVersion), R.drawable.ic_graphics, "PackageManager declaration", if (d?.advertisedVulkanVersion is Observation.Known) Badge("ADVERTISED", Tone.INFO) else Badge("UNKNOWN")),
            Metric("GPU", observation(d?.gpuModel), R.drawable.ic_cpu, badge = observedBadge(d?.gpuModel)),
        )
        page.status("driver", "Vulkan driver", observation(d?.vulkanDriverVersion), R.drawable.ic_graphics, observedBadge(d?.vulkanDriverVersion))
        page.status("extensions", "Vulkan extensions", d?.vulkanExtensions?.valueOrNull()?.joinToString() ?: "Native probe not implemented", R.drawable.ic_runtime, observedBadge(d?.vulkanExtensions))
        page.section("Memory", "Physical RAM and swap are reported separately")
        val physical = m?.physicalBytes
        val available = m?.availableBytes
        val fraction = if (physical != null && physical > 0 && available != null && available in 0..physical) (physical - available).toFloat() / physical else null
        page.progress("memory", "System RAM in use", fraction, "${bytes(available)} available of ${bytes(physical)} physical · includes the entire system")
        page.metrics(
            Metric("PHYSICAL RAM", bytes(physical), R.drawable.ic_memory),
            Metric("AVAILABLE", bytes(available), R.drawable.ic_memory),
            Metric("LAUNCHER PSS", bytes(m?.processPssBytes), R.drawable.ic_diagnostics, "Latest process sample"),
            Metric("SWAP TOTAL", bytes(m?.swapTotalBytes), R.drawable.ic_storage, "Free: ${bytes(m?.swapFreeBytes)}"),
        )
        page.status("pressure", "Memory pressure", "Android memory signals · budgets reduce under pressure", R.drawable.ic_memory,
            Badge(state.runtime.pressure.name, if (state.runtime.pressure in listOf(MemoryPressure.LOW, MemoryPressure.CRITICAL)) Tone.CAUTION else Tone.NEUTRAL))
        page.section("Thermal")
        val raw = d?.thermalStatus?.valueOrNull()
        val name = when(raw) { 0 -> "No thermal pressure"; 1 -> "Light thermal pressure"; 2 -> "Moderate throttling"; 3 -> "Severe throttling"; 4 -> "Critical temperature"; 5 -> "Emergency"; 6 -> "Shutdown threshold"; else -> "Unknown" }
        page.status("thermal", name, "PowerManager status: ${raw ?: "Unknown"}", R.drawable.ic_bolt, Badge(if (raw == null) "UNKNOWN" else "OBSERVED", if (raw != null && raw >= 2) Tone.CAUTION else Tone.NEUTRAL))
        page.section("Diagnostic package")
        page.note("export", "Device observations + up to 128 KiB of logs. Includes the device model; excludes projects and accounts.")
        page.secondary("Export diagnostics", !state.busy, R.drawable.ic_export, actions.export)
    }
}
