// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState

object Settings {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        page.card("Device profile", "Automatic defaults favor stability on 4 GB devices. Overrides change launcher budgets; they cannot enable an unavailable runtime. Memory pressure always restores conservative budgets.")
        page.profiles(state.profileOverride, !state.busy, actions.profile)
        page.card("Advanced runtime options", "Execution, driver and rendering settings become available when their components are implemented and validated.")
    }
}
