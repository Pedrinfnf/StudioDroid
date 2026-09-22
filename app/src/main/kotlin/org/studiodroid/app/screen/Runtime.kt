// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState
import org.studiodroid.app.R
import org.studiodroid.app.design.*
import org.studiodroid.core.*

object Runtime {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        page.section("Execution pipeline", "Independent components. One controlled launch.")
        page.status("execution", "Runtime unavailable", availability(state.runtime.availability), R.drawable.ic_lock, Badge("UNAVAILABLE"))
        val components = listOf(
            Triple(ComponentKind.HOST_ROOTFS, "Host rootfs", "ARM64 Linux environment"),
            Triple(ComponentKind.CPU_TRANSLATOR, "FEX", "Planned translation backend"),
            Triple(ComponentKind.GUEST_ROOTFS, "Guest rootfs", "x86_64 userspace"),
            Triple(ComponentKind.WINDOWS_COMPATIBILITY, "Wine", "Windows compatibility layer"),
            Triple(ComponentKind.GRAPHICS_TRANSLATION, "DXVK", "Versioned graphics translation"),
            Triple(ComponentKind.GRAPHICS_DRIVER, "Graphics driver", "Custom package · system policy remains default"),
            Triple(ComponentKind.STUDIO, "Studio", "Original, unmodified Windows application"),
        )
        components.forEachIndexed { index, (kind, title, detail) ->
            val record = state.runtime.components.firstOrNull { it.kind == kind }
            page.stage("component-$kind", title, detail, when(kind) { ComponentKind.STUDIO -> R.drawable.ic_studio; ComponentKind.GRAPHICS_DRIVER, ComponentKind.GRAPHICS_TRANSLATION -> R.drawable.ic_graphics; else -> R.drawable.ic_runtime }, componentBadge(record), index == components.lastIndex, record?.identity?.version)
        }
        page.note("container", "Backend and container selection require future validation under the Android app identity.")
        page.section("Presentation", "Owned by StudioDroid · transport decision stays open until M6")
        page.stage("wine", "Wine", "Protocol source", R.drawable.ic_runtime, Badge("FUTURE", Tone.PLANNED))
        page.stage("transport", "Internal transport", "Direct transport or an owned compatibility layer; unresolved", R.drawable.ic_link, Badge("UNRESOLVED", Tone.PLANNED))
        page.stage("surface-backend", "NativeSurfaceBackend", "Lifecycle, buffers, input and overlays", R.drawable.ic_graphics, Badge("FUTURE", Tone.PLANNED))
        page.stage("surface", "Android Surface", "Final low-copy presentation target", R.drawable.ic_graphics, Badge("FUTURE", Tone.PLANNED), true)
        page.note("ownership", "Conceptual path only. No external X server dependency; Termux:X11 is research-only.")
    }
}
