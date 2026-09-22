// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

internal const val GIB = 1024L * 1024 * 1024
internal fun memory(gib: Long = 4, lowRam: Boolean? = false) = MemorySnapshot(gib * GIB, 2 * GIB, 256 * 1024 * 1024, false, lowRam)
internal fun device(ram: MemorySnapshot = memory()) = DeviceCapabilities(
    observedAtMillis = 1, androidApi = 36, androidVersion = "test", model = "fixture", supportedAbis = listOf("arm64-v8a"),
    process64Bit = true, pageSize = Observation.Known(4096, "fixture"), soc = Observation.Unknown("fixture"),
    gpuVendor = GpuVendor.UNKNOWN_OTHER, gpuModel = Observation.Unknown("fixture"), advertisedVulkanVersion = Observation.Unknown("fixture"),
    vulkanDriverVersion = Observation.Unknown("fixture"), vulkanExtensions = Observation.Unknown("fixture"), memory = ram,
    thermalStatus = Observation.Unknown("fixture"), storageAvailableBytes = Observation.Unknown("fixture"), storageTotalBytes = Observation.Unknown("fixture"),
)
