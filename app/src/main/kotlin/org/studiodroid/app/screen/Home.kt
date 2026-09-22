// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState
import org.studiodroid.app.R
import org.studiodroid.app.design.*
import org.studiodroid.core.*

object Home {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        val device = state.runtime.device
        page.hero("Your next workspace.", "StudioDroid v2 / 2.0.0-m1", Badge("LAUNCH UNAVAILABLE"), "Studio is not installed. Your launcher foundation is here.")
        page.primary("Launch Studio", false)
        page.note("launch", availability(state.runtime.availability))
        page.section("Device overview", "Live observations · conservative defaults")
        page.metrics(
            Metric("DEVICE PROFILE", state.runtime.profile.name.replace('_', ' '), R.drawable.ic_memory, if (state.profileOverride == null) "Automatically selected" else "Manual override"),
            Metric("AVAILABLE RAM", bytes(device?.memory?.availableBytes), R.drawable.ic_diagnostics, "Physical RAM · swap excluded"),
        )
        page.section("Runtime readiness", "Planned execution path · no execution backend registered")
        val host = when { device == null -> Badge("UNVERIFIED"); device.supportsArm64 -> Badge("OBSERVED", Tone.INFO); else -> Badge("UNSUPPORTED", Tone.CAUTION) }
        page.stage("host", "Android host", device?.let { "Android ${it.androidVersion} · ${if (it.supportsArm64) "ARM64" else it.supportedAbis.firstOrNull() ?: "Unknown ABI"}" } ?: "Waiting for device observations", R.drawable.ic_cpu, host)
        val stages = listOf(
            Triple(ComponentKind.CPU_TRANSLATOR, "FEX", "Planned CPU translator"),
            Triple(ComponentKind.GUEST_ROOTFS, "x86_64 guest", "Guest environment"),
            Triple(ComponentKind.WINDOWS_COMPATIBILITY, "Wine", "Windows compatibility"),
            Triple(ComponentKind.GRAPHICS_TRANSLATION, "DXVK", "Graphics translation"),
            Triple(ComponentKind.STUDIO, "Studio", "Original Windows application"),
        )
        stages.forEachIndexed { index, (kind, title, description) ->
            val record = state.runtime.components.firstOrNull { it.kind == kind }
            page.stage("ready-$kind", title, description, if (kind == ComponentKind.STUDIO) R.drawable.ic_studio else R.drawable.ic_runtime, componentBadge(record), index == stages.lastIndex, record?.identity?.version)
        }
        page.section("Graphics path")
        page.status("graphics", "System Vulkan", "Default driver policy · compatibility has not been qualified", R.drawable.ic_graphics, Badge("UNVERIFIED"))
        if (state.runtime.lastSession?.recoveryRequired == true) page.notice("Previous session outcome unknown. Recovery required; nothing restarts automatically.")
        page.secondary(if (state.runtime.refreshing) "Checking device…" else "Refresh device", !state.busy && !state.runtime.refreshing, action = actions.refresh)
    }
}
