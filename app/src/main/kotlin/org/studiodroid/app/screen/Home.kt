// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState

object Home {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        page.card("Your Studio workspace", "Studio is not installed. Installation and runtime execution are coming in later milestones.")
        page.button("Launch unavailable", false)
        page.card("Runtime", availability(state.runtime.availability))
        page.card("Device profile", "${state.runtime.profile.name.replace('_', ' ')}\nAvailable memory: ${bytes(state.runtime.device?.memory?.availableBytes)}")
        page.card("Graphics", "System Vulkan policy selected\nGraphics translation: not installed\nPresentation: not implemented")
        if (state.runtime.lastSession?.recoveryRequired == true) page.notice("The previous session outcome is unknown. Recovery is required; nothing will restart automatically.")
        page.button(if (state.runtime.refreshing) "Checking device…" else "Refresh device information", !state.busy && !state.runtime.refreshing, actions.refresh)
    }
}
