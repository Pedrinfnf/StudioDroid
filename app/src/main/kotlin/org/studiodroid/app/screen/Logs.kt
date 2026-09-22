// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState

object Logs {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        page.card("Launcher logs", "Showing up to 16 KiB. Logs are stored on disk and rotated; this view clears when the app is hidden.")
        page.button("Refresh log tail", !state.busy, actions.refresh)
        page.logs(state.logTail)
    }
}
