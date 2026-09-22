// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

import java.util.UUID

/** Pure preflight only. Backend/native containment and readiness checks remain mandatory. */
class LaunchPlanner {
    private val id = Regex("[a-z][a-z0-9._-]{0,127}")
    private val hash = Regex("[a-f0-9]{64}")
    private val envKey = Regex("[A-Z_][A-Z0-9_]{0,127}")
    // Host environment is not supplied by users; backends add their own validated variables.
    private val allowedEnvironment = setOf("LANG", "LC_ALL", "TZ")
    fun prepare(request: LaunchRequest, device: DeviceCapabilities?, backends: List<BackendDescriptor>,
                containers: List<BackendDescriptor>, installed: List<ComponentRecord>, profile: DeviceProfile): PlanResult {
        val issues = linkedSetOf<LaunchIssue>()
        if (device == null) issues += LaunchIssue.DEVICE_UNKNOWN
        else {
            if (!device.supportsArm64) issues += LaunchIssue.ARM64_REQUIRED
            if (MemoryPolicy.pressure(device.memory) in setOf(MemoryPressure.LOW, MemoryPressure.CRITICAL)) issues += LaunchIssue.MEMORY_PRESSURE
        }
        if (backends.none { it.id == request.backendId && it.availability == Availability.Available }) issues += LaunchIssue.BACKEND_UNAVAILABLE
        if (containers.none { it.id == request.containerId && it.availability == Availability.Available }) issues += LaunchIssue.CONTAINER_UNAVAILABLE
        val components = request.components
        if (components.isEmpty() || components.size > 64 || components.map { it.id }.distinct().size != components.size ||
            components.none { it.id == request.process.executable.componentId } || components.any { version ->
                !id.matches(version.id) || version.version.isBlank() || version.version.length > 256 || !hash.matches(version.sha256) ||
                    installed.none { it.identity == version && it.state == InstallState.VALIDATED && it.actualSha256 == version.sha256 }
            }) issues += LaunchIssue.COMPONENT_UNVERIFIED
        val process = request.process
        if (!validPath(process.executable.relativePath, false) || !validPath(process.workingDirectory, true) ||
            process.arguments.size > 256 || process.arguments.any { it.length > 8192 || '\u0000' in it } ||
            process.environment.size > 128 || process.environment.any { (key, value) ->
                !envKey.matches(key) || key !in allowedEnvironment || value.length > 8192 || '\u0000' in value
            }) issues += LaunchIssue.INVALID_ARGUMENTS
        return if (issues.isNotEmpty()) PlanResult.Blocked(issues)
        else PlanResult.Prepared(LaunchPlan(UUID.randomUUID(), request.copy(
            components = components.toList(), process = process.copy(arguments = process.arguments.toList(), environment = process.environment.toMap())
        ), profile))
    }
    private fun validPath(path: String, absolute: Boolean): Boolean {
        if (path.isEmpty() || path.length > 4096 || path.startsWith('/') != absolute ||
            path.any { it.code < 32 || it.code == 127 || it.code == 92 }) return false
        if (absolute && path == "/") return true
        val parts = (if (absolute) path.drop(1) else path).split('/')
        return parts.none { it.isEmpty() || it == "." || it == ".." }
    }
}
