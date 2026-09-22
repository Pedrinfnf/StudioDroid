// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState
import org.studiodroid.app.R
import org.studiodroid.app.design.*

object Settings {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        page.section("Resource strategy", "Choose how the launcher uses memory and I/O")
        page.profiles(state.profileOverride, !state.busy, actions.profile)
        page.status("effective", "Current profile", state.runtime.profile.name.replace('_', ' '), R.drawable.ic_memory, Badge(if(state.profileOverride == null) "AUTOMATIC" else "OVERRIDE", Tone.INFO))
        page.note("scope", "Profiles adjust budgets. They cannot enable missing runtimes or capabilities. Memory pressure always restores conservative budgets.")
        page.section("Runtime tuning", "Controls will unlock as components are validated")
        page.empty("execution", "Execution options", "Backend and container configuration", R.drawable.ic_runtime)
        page.empty("graphics", "Graphics & presentation", "Driver selection, renderer profile and resolution", R.drawable.ic_graphics)
        page.note("motion", "Subtle transitions follow Android’s animation setting. Animations are also reduced while touch exploration is enabled.")
    }
}
