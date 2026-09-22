// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState
import org.studiodroid.app.R
import org.studiodroid.app.design.*

object About {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        page.hero("StudioDroid v2", "2.0.0-m1 · Android ARM64", Badge("M1 / FOUNDATION", Tone.INFO), "A new home for a real desktop workspace.")
        page.section("Built for the long run")
        page.note("mission", "An independent launcher/runtime platform preparing to run the original Windows Studio on Android. M1 provides the launcher foundation; Studio execution is unavailable.")
        page.link("Project repository", "Source, architecture and development", "https://github.com/Pedrinfnf/StudioDroid")
        page.section("Open source")
        page.link("Open-source licenses", "StudioDroid · GPL-3.0-only", "https://github.com/Pedrinfnf/StudioDroid/blob/m1/ui-preview/LICENSE")
        page.link("Third-party notices", "Kotlin, AndroidX and Material · Apache-2.0", "https://github.com/Pedrinfnf/StudioDroid/blob/m1/ui-preview/NOTICE")
        page.section("Research references", "Architecture research; no bundled runtime from these projects")
        page.link("Vodka", "Container, translation and runtime research", "https://github.com/Shellworks-Development/vodka")
        page.link("ZalithLauncher2", "Launcher and modular graphics concepts", "https://github.com/ZalithLauncher/ZalithLauncher2")
        page.note("x11", "Termux:X11 is a research reference only. StudioDroid owns its presentation backend.")
        page.section("Independent project")
        page.status("affiliation", "Not affiliated with Roblox", "StudioDroid is not endorsed by Roblox Corporation. Roblox Studio and its trademarks belong to their respective owners.", R.drawable.ic_about, Badge("DISCLOSURE"))
    }
}
