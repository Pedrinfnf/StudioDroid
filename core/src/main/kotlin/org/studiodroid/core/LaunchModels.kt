// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

import java.util.UUID

data class ComponentPath(val componentId: String, val relativePath: String)
data class ProcessArguments(val executable: ComponentPath, val arguments: List<String>, val environment: Map<String, String>, val workingDirectory: String)
data class LaunchRequest(val backendId: String, val containerId: String, val components: List<ComponentVersion>, val process: ProcessArguments)
data class LaunchPlan(val sessionId: UUID, val request: LaunchRequest, val profile: DeviceProfile)
enum class LaunchIssue { DEVICE_UNKNOWN, ARM64_REQUIRED, BACKEND_UNAVAILABLE, CONTAINER_UNAVAILABLE, COMPONENT_UNVERIFIED, INVALID_ARGUMENTS, MEMORY_PRESSURE }
sealed interface PlanResult {
    data class Prepared(val plan: LaunchPlan) : PlanResult
    data class Blocked(val issues: Set<LaunchIssue>) : PlanResult
}
sealed interface LaunchResult {
    data class Started(val sessionId: UUID) : LaunchResult
    data class Refused(val reason: String) : LaunchResult
}
