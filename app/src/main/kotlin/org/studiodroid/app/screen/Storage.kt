// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState
import org.studiodroid.core.valueOrNull

object Storage {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        page.card("App-managed storage", "Available filesystem space: ${bytes(state.runtime.device?.storageAvailableBytes?.valueOrNull())}\nLauncher logs: ${bytes(state.storage?.logsBytes)}\nSession metadata: ${bytes(state.storage?.sessionBytes)}")
        page.card("Studio and runtime files", "No runtime payload or Studio installer exists in M1. No payload storage is allocated.")
        page.card("Your files", "Diagnostic exports use Android’s document picker. Project import/export will be added with the storage integration milestone.")
        page.button("Refresh storage usage", !state.busy, actions.refresh)
    }
}
