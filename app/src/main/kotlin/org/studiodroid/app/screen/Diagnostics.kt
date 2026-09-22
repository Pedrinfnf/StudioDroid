// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import org.studiodroid.app.LauncherUiState

object Diagnostics {
    fun render(page: PageBuilder, state: LauncherUiState, actions: ScreenActions) {
        val device = state.runtime.device
        page.card("Device", device?.let { "${it.model}\nAndroid ${it.androidVersion} (API ${it.androidApi})\nABIs: ${it.supportedAbis.joinToString()}\n64-bit process: ${it.process64Bit}" } ?: "Device information has not been measured yet.")
        page.card("Hardware", "SoC: ${observation(device?.soc)}\nPage size: ${observation(device?.pageSize)}\nGPU: ${observation(device?.gpuModel)}")
        page.card("Graphics capabilities", "Advertised Vulkan: ${observation(device?.advertisedVulkanVersion)}\nDriver: ${observation(device?.vulkanDriverVersion)}\nExtensions: ${observation(device?.vulkanExtensions)}\nThese observations do not establish runtime compatibility.")
        page.card("Memory", "Physical: ${bytes(device?.memory?.physicalBytes)}\nAvailable: ${bytes(device?.memory?.availableBytes)}\nLauncher PSS: ${bytes(device?.memory?.processPssBytes)}\nSwap total: ${bytes(device?.memory?.swapTotalBytes)}\nPressure: ${state.runtime.pressure}")
        page.card("Thermal", observation(device?.thermalStatus))
        page.button("Refresh observations", !state.busy && !state.runtime.refreshing, actions.refresh)
        page.card("Diagnostic export", "Exports launcher observations and up to 128 KiB of structured logs. Device model is included. Projects, accounts and settings files are excluded.")
        page.button("Export diagnostic package", !state.busy, actions.export)
    }
}
