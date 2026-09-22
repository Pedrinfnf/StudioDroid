// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

sealed interface Observation<out T> {
    data class Known<T>(val value: T, val source: String) : Observation<T>
    data class Unknown(val reason: String) : Observation<Nothing>
}
fun <T> Observation<T>.valueOrNull(): T? = (this as? Observation.Known<T>)?.value
sealed interface Availability {
    data object Available : Availability
    data class Unimplemented(val reason: String) : Availability
    data class Unsupported(val reason: String) : Availability
    data class Unverified(val reason: String) : Availability
}
enum class GpuVendor { ADRENO, MALI, POWERVR, UNKNOWN_OTHER }
data class MemorySnapshot(
    val physicalBytes: Long?, val availableBytes: Long?, val thresholdBytes: Long?,
    val lowMemory: Boolean?, val lowRamDevice: Boolean?,
    val processPssBytes: Long? = null, val swapTotalBytes: Long? = null, val swapFreeBytes: Long? = null,
)
data class DeviceCapabilities(
    val observedAtMillis: Long,
    val androidApi: Int,
    val androidVersion: String,
    val model: String,
    val supportedAbis: List<String>,
    val process64Bit: Boolean,
    val pageSize: Observation<Long>,
    val soc: Observation<String>,
    val gpuVendor: GpuVendor,
    val gpuModel: Observation<String>,
    val advertisedVulkanVersion: Observation<String>,
    val vulkanDriverVersion: Observation<String>,
    val vulkanExtensions: Observation<List<String>>,
    val memory: MemorySnapshot,
    val thermalStatus: Observation<Int>,
    val storageAvailableBytes: Observation<Long>,
    val storageTotalBytes: Observation<Long>,
    val graphicsQualified: Boolean = false,
) {
    val supportsArm64: Boolean get() = "arm64-v8a" in supportedAbis && process64Bit
}
data class BackendDescriptor(val id: String, val displayName: String, val availability: Availability, val version: String? = null)
