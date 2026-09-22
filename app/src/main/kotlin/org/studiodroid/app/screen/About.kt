// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState

object About {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        page.card("StudioDroid v2", "2.0.0-m1 • Android launcher foundation\nAn independent project preparing to run the genuine Windows Studio application on Android ARM64.")
        page.card("Current milestone", "M1 includes the launcher, capability reporting, contracts and diagnostics. It does not run Roblox Studio.")
        page.card("Licenses and research", "StudioDroid: GPL-3.0-only\nAndroidX, Kotlin, Material Components: Apache-2.0\nVodka and ZalithLauncher2 informed architecture; their runtime code is not incorporated. Termux:X11 remains research-only.\nNot affiliated with Roblox Corporation.")
    }
}
