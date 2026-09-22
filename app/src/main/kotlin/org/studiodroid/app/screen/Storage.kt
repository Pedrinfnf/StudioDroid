// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState
import org.studiodroid.app.R
import org.studiodroid.app.design.*
import org.studiodroid.core.valueOrNull

object Storage {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        val free = state.runtime.device?.storageAvailableBytes?.valueOrNull()
        val total = state.runtime.device?.storageTotalBytes?.valueOrNull()
        val usage = state.storage
        page.section("Storage map", "App-managed internals. Android-managed exports.")
        page.metrics(Metric("AVAILABLE", bytes(free), R.drawable.ic_storage, "Filesystem containing the app"), Metric("FILESYSTEM SIZE", bytes(total), R.drawable.ic_storage, "Shared with other apps and Android"))
        page.progress("filesystem", "Filesystem space used", if (free != null && total != null && total > 0 && free in 0..total) 1f-free.toFloat()/total else null, "System-wide filesystem usage, not StudioDroid allocation")
        page.section("App-managed storage")
        page.metrics(Metric("ROTATED LOGS", bytes(usage?.logsBytes), R.drawable.ic_logs, "Four bounded files"), Metric("SESSION METADATA", bytes(usage?.sessionBytes), R.drawable.ic_runtime, "Atomic local journal"))
        page.note("launcher", "These totals cover logs and session metadata. App code, settings and caches are not included in this measurement.")
        page.section("Runtime files")
        page.empty("runtime", "No runtime payloads", "No Studio or runtime installation exists in M1. No payload usage is reported.", R.drawable.ic_runtime, Badge("NOT INSTALLED"))
        page.section("User files")
        page.status("files", "Your document provider", "Diagnostic exports use Android’s document picker. You choose the destination.", R.drawable.ic_folder, Badge("AVAILABLE", Tone.INFO))
        page.empty("projects", "Project storage", "Import and export will arrive with the storage integration milestone.", R.drawable.ic_studio)
        page.secondary("Refresh storage", !state.busy, action = actions.refresh)
    }
}
