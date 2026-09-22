// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState

object Logs {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        page.section("Event stream", "Newest first · device local time · maximum 16 KiB")
        page.logActions(state.logTail, !state.busy, actions.refresh)
        page.note("retention", "Disk-backed and rotated. This view clears when the launcher is hidden.")
        page.logs(state.logTail)
    }
}
