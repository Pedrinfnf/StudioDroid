// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState

object Runtime {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        page.card("Execution", availability(state.runtime.availability))
        page.card("Backend and container", "No execution backend is registered. Container capability requires future app-UID validation.")
        page.card("Components", state.runtime.components.joinToString("\n") { "${it.kind.name.replace('_', ' ')}: ${it.state.name.replace('_', ' ')}" })
        page.card("Presentation", "StudioDroid owns NativeSurfaceBackend. Its internal transport remains open until M6.")
    }
}
