// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.studiodroid.core.*

class AndroidRuntimeController(
    private val probe: AndroidCapabilityProbe, private val composition: RuntimeComposition,
    private val sessions: SessionRepository, private val settings: SettingsRepository,
    private val logs: RotatingLogStore,
) : RuntimeController {
    private val mutable = MutableStateFlow(RuntimeSnapshot())
    override val state = mutable.asStateFlow()
    private val initialization = Mutex()
    private val refresh = Mutex()
    private var initialized = false
    private val planner = LaunchPlanner()
    override suspend fun initialize() = withContext(Dispatchers.IO) {
        initialization.withLock {
            if (!initialized) {
                try {
                    settings.load()
                    val last = sessions.load()
                    mutable.update { it.copy(lastSession = last, probeError = sessions.issue) }
                    logs.append(event(LogCode.LAUNCHER_INITIALIZED))
                    if (last?.recoveryRequired == true) logs.append(event(LogCode.SESSION_RECOVERY_REQUIRED))
                    initialized = true
                } catch (_: Exception) { mutable.update { it.copy(probeError = "Launcher metadata could not be initialized") } }
            }
        }
        if (state.value.device == null) refreshCapabilities()
    }
    override suspend fun refreshCapabilities() = withContext(Dispatchers.IO) {
        if (!refresh.tryLock()) return@withContext
        try {
            mutable.update { it.copy(refreshing = true) }
            val device = probe.probe()
            val profile = settings.state.value.profileOverride ?: DeviceProfilePolicy.choose(device.memory, device.graphicsQualified)
            val pressure = MemoryPolicy.pressure(device.memory)
            val logIssue = runCatching {
                logs.configure(MemoryPolicy.budget(profile, pressure).retainedLogBytes)
                logs.append(event(LogCode.CAPABILITIES_REFRESHED))
            }.exceptionOrNull()?.let { "Launcher log storage is unavailable" }
            mutable.update { it.copy(device = device, profile = profile, pressure = pressure, refreshing = false,
                availability = if (device.supportsArm64) Availability.Unimplemented("Runtime execution is not implemented in M1") else Availability.Unsupported("An ARM64 device and 64-bit process are required"),
                probeError = logIssue ?: sessions.issue) }
        } catch (_: Exception) {
            mutable.update { it.copy(refreshing = false, probeError = "Capability refresh failed; last observations may be stale") }
        } finally { refresh.unlock() }
    }
    internal suspend fun observeMemory(memory: MemorySnapshot) = withContext(Dispatchers.IO) {
        val pressure = MemoryPolicy.pressure(memory)
        val current = state.value
        val profile = settings.state.value.profileOverride ?: DeviceProfilePolicy.choose(memory, current.device?.graphicsQualified == true)
        if (pressure != current.pressure) runCatching { logs.append(event(LogCode.MEMORY_PRESSURE)) }
        val logIssue = runCatching { logs.configure(MemoryPolicy.budget(profile, pressure).retainedLogBytes) }
            .exceptionOrNull()?.let { "Launcher log storage is unavailable" }
        mutable.update { old -> old.copy(pressure = pressure, profile = profile, probeError = logIssue ?: old.probeError, device = old.device?.let { device ->
            device.copy(memory = memory.copy(processPssBytes = memory.processPssBytes ?: device.memory.processPssBytes,
                swapTotalBytes = memory.swapTotalBytes ?: device.memory.swapTotalBytes, swapFreeBytes = memory.swapFreeBytes ?: device.memory.swapFreeBytes))
        }) }
    }
    override suspend fun prepareLaunch(request: LaunchRequest): PlanResult = planner.prepare(request, state.value.device,
        composition.backends.map { it.descriptor }, composition.containers.map { it.descriptor }, state.value.components, state.value.profile)
    override suspend fun launch(plan: LaunchPlan): LaunchResult = withContext(Dispatchers.IO) {
        // Recheck at the control-plane boundary; even a caller-created plan is never executable here.
        runCatching { logs.append(event(LogCode.LAUNCH_REFUSED)) }
        LaunchResult.Refused("No execution backend is implemented; no process or session was started")
    }
    override suspend fun stop(): Availability = Availability.Unimplemented("No executable runtime session exists in M1")
    private fun event(code: LogCode) = DiagnosticEvent(System.currentTimeMillis(), LogCategory.ANDROID, code)
}
