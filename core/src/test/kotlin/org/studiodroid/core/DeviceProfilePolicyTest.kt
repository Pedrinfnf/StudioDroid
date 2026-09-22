// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceProfilePolicyTest {
    @Test fun fourGiBIsFirstClassLowMemory() { assertEquals(DeviceProfile.LOW_MEMORY, DeviceProfilePolicy.choose(memory(4), true)) }
    @Test fun lowRamSignalWinsEvenWithLargePhysicalRam() { assertEquals(DeviceProfile.LOW_MEMORY, DeviceProfilePolicy.choose(memory(16, true), true)) }
    @Test fun unknownRamAndLowRamSignalAreConservative() {
        assertEquals(DeviceProfile.LOW_MEMORY, DeviceProfilePolicy.choose(memory().copy(physicalBytes = null), true))
        assertEquals(DeviceProfile.LOW_MEMORY, DeviceProfilePolicy.choose(memory(8, null), true))
    }
    @Test fun performanceRequiresBothMemoryAndQualification() {
        assertEquals(DeviceProfile.BALANCED, DeviceProfilePolicy.choose(memory(8), true))
        assertEquals(DeviceProfile.BALANCED, DeviceProfilePolicy.choose(memory(12), false))
        assertEquals(DeviceProfile.PERFORMANCE, DeviceProfilePolicy.choose(memory(12), true))
    }
    @Test fun swapDoesNotCountAsPhysicalMemory() {
        assertEquals(DeviceProfile.LOW_MEMORY, DeviceProfilePolicy.choose(memory(4).copy(swapTotalBytes = 16 * GIB), true))
    }
    @Test fun fiveGiBBoundaryUsesOsReportedBytes() {
        assertEquals(DeviceProfile.LOW_MEMORY, DeviceProfilePolicy.choose(memory(5).copy(physicalBytes = 5 * GIB - 1), false))
        assertEquals(DeviceProfile.BALANCED, DeviceProfilePolicy.choose(memory(5), false))
    }
}
