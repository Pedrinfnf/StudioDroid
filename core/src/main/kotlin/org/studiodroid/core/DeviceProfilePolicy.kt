// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

enum class DeviceProfile { LOW_MEMORY, BALANCED, PERFORMANCE }
object DeviceProfilePolicy {
    private const val GIB = 1024L * 1024 * 1024
    fun choose(memory: MemorySnapshot, graphicsQualified: Boolean): DeviceProfile {
        val physical = memory.physicalBytes ?: return DeviceProfile.LOW_MEMORY
        if (physical < 5 * GIB || memory.lowRamDevice != false) return DeviceProfile.LOW_MEMORY
        return if (physical >= 9 * GIB && graphicsQualified) DeviceProfile.PERFORMANCE else DeviceProfile.BALANCED
    }
}
