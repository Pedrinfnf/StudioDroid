// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

import org.junit.Assert.*
import org.junit.Test

class MemoryPolicyTest {
    @Test fun lowMemoryBudgetsMatchAcceptedDesign() {
        val budget = MemoryPolicy.budget(DeviceProfile.LOW_MEMORY)
        assertEquals(1, budget.downloads); assertEquals(1, budget.extractions)
        assertEquals(64 * 1024, budget.ioBufferBytes); assertEquals(128 * 1024, budget.uiLogTailBytes)
        assertEquals(16 * 1024 * 1024L, budget.retainedLogBytes)
    }
    @Test fun pressureOverridesPerformancePreferences() {
        for (pressure in listOf(MemoryPressure.LOW, MemoryPressure.CRITICAL, MemoryPressure.UNKNOWN)) {
            assertEquals(MemoryPolicy.budget(DeviceProfile.LOW_MEMORY), MemoryPolicy.budget(DeviceProfile.PERFORMANCE, pressure))
        }
    }
    @Test fun observedHeadroomControlsPressure() {
        assertEquals(MemoryPressure.CRITICAL, MemoryPolicy.pressure(memory().copy(availableBytes = 1)))
        assertEquals(MemoryPressure.LOW, MemoryPolicy.pressure(memory().copy(lowMemory = true)))
        assertEquals(MemoryPressure.UNKNOWN, MemoryPolicy.pressure(memory().copy(availableBytes = null)))
        assertEquals(MemoryPressure.NORMAL, MemoryPolicy.pressure(memory()))
    }
    @Test fun samplingIsBoundedAndOnlyAcceleratesUnderPressure() {
        assertEquals(5_000L, MemoryPolicy.sampleIntervalMillis(MemoryPressure.NORMAL))
        assertEquals(5_000L, MemoryPolicy.sampleIntervalMillis(MemoryPressure.UNKNOWN))
        assertEquals(1_000L, MemoryPolicy.sampleIntervalMillis(MemoryPressure.CRITICAL))
    }
}
