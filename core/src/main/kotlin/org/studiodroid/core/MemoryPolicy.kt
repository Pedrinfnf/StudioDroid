// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

enum class MemoryPressure { NORMAL, LOW, CRITICAL, UNKNOWN }
data class MemoryBudget(
    val downloads: Int, val extractions: Int, val ioBufferBytes: Int, val logQueueBytes: Int,
    val uiLogTailBytes: Int, val graphicsCacheBytes: Long, val translatorCacheBytes: Long,
    val retainedLogBytes: Long,
)
object MemoryPolicy {
    private const val KIB = 1024
    private const val MIB = 1024L * 1024
    fun pressure(memory: MemorySnapshot): MemoryPressure {
        val available = memory.availableBytes
        val threshold = memory.thresholdBytes
        if (available != null && threshold != null && threshold > 0 && available <= threshold) return MemoryPressure.CRITICAL
        if (memory.lowMemory == true) return MemoryPressure.LOW
        return if (available == null || threshold == null || memory.lowMemory == null) MemoryPressure.UNKNOWN else MemoryPressure.NORMAL
    }
    fun budget(profile: DeviceProfile, pressure: MemoryPressure = MemoryPressure.NORMAL): MemoryBudget {
        val effective = if (pressure != MemoryPressure.NORMAL) DeviceProfile.LOW_MEMORY else profile
        return when (effective) {
            DeviceProfile.LOW_MEMORY -> MemoryBudget(1, 1, 64 * KIB, 256 * KIB, 128 * KIB, 128 * MIB, 64 * MIB, 16 * MIB)
            DeviceProfile.BALANCED -> MemoryBudget(2, 1, 128 * KIB, 512 * KIB, 256 * KIB, 256 * MIB, 128 * MIB, 32 * MIB)
            DeviceProfile.PERFORMANCE -> MemoryBudget(2, 1, 128 * KIB, 1024 * KIB, 512 * KIB, 512 * MIB, 256 * MIB, 64 * MIB)
        }
    }
    fun sampleIntervalMillis(pressure: MemoryPressure): Long =
        if (pressure == MemoryPressure.LOW || pressure == MemoryPressure.CRITICAL) 1_000 else 5_000
}
