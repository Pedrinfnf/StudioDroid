// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

import java.util.UUID

enum class SessionPhase { PREPARE, START, RUNNING, STOP, CLEANUP, UNKNOWN }
sealed interface SessionResult {
    data object Running : SessionResult
    data class Exited(val exitCode: Int) : SessionResult { init { require(exitCode in 0..255) } }
    data class Signaled(val signal: Int) : SessionResult { init { require(signal in 1..255) } }
    data class StartFailed(val errno: Int?, val message: String?) : SessionResult {
        init { require(errno == null || errno in 1..65535); require(errno != null || !message.isNullOrBlank()) }
    }
    data object Cancelled : SessionResult
    data object Unknown : SessionResult
}
data class SessionRecord(val id: UUID, val phase: SessionPhase, val result: SessionResult, val timestampMillis: Long, val recoveryRequired: Boolean = false)
data class RuntimeSnapshot(
    val availability: Availability = Availability.Unimplemented("Execution is not implemented in M1"),
    val device: DeviceCapabilities? = null,
    val profile: DeviceProfile = DeviceProfile.LOW_MEMORY,
    val pressure: MemoryPressure = MemoryPressure.UNKNOWN,
    val components: List<ComponentRecord> = ComponentKind.entries.map { ComponentRecord(it) },
    val lastSession: SessionRecord? = null,
    val refreshing: Boolean = false,
    val probeError: String? = null,
)
