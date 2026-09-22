// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.studiodroid.core.*
import java.io.OutputStream

enum class Destination { HOME, RUNTIME, DIAGNOSTICS, STORAGE, LOGS, SETTINGS, ABOUT }
data class LauncherUiState(
    val runtime: RuntimeSnapshot = RuntimeSnapshot(), val destination: Destination = Destination.HOME,
    val logTail: String = "", val storage: StorageUsage? = null, val message: String? = null,
    val busy: Boolean = false, val profileOverride: DeviceProfile? = null,
)
class LauncherViewModel(private val graph: AppGraph, private val saved: SavedStateHandle) : ViewModel() {
    private val local = MutableStateFlow(LauncherUiState(destination = Destination.entries.firstOrNull { it.name == saved.get<String>("destination") } ?: Destination.HOME))
    val state: StateFlow<LauncherUiState> = combine(graph.controller.state, graph.settings.state, local) { runtime, settings, ui ->
        ui.copy(runtime = runtime, profileOverride = settings.profileOverride)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), local.value)
    fun select(destination: Destination) {
        saved["destination"] = destination.name
        local.update { it.copy(destination = destination, logTail = "", message = null) }
        if (destination == Destination.LOGS || destination == Destination.STORAGE) refreshPage()
    }
    fun refresh() = action {
        graph.controller.refreshCapabilities()
        loadPage()
    }
    fun refreshPage() = action { loadPage() }
    private suspend fun loadPage() = withContext(Dispatchers.IO) {
        val destination = local.value.destination
        val storage = if (destination == Destination.STORAGE) StorageUsage(graph.logs.diskBytes(), graph.sessions.diskBytes(), graph.controller.state.value.device?.storageAvailableBytes?.valueOrNull()) else null
        val tail = if (destination == Destination.LOGS) graph.logs.readTail(minOf(16 * 1024, MemoryPolicy.budget(graph.controller.state.value.profile).uiLogTailBytes)) else ""
        local.update { if (it.destination == destination) it.copy(logTail = tail, storage = storage) else it }
    }
    fun setProfile(profile: DeviceProfile?) = action {
        withContext(Dispatchers.IO) {
            graph.settings.setProfile(profile)
            graph.logs.append(DiagnosticEvent(System.currentTimeMillis(), LogCategory.ANDROID, LogCode.SETTINGS_CHANGED))
        }
        graph.controller.refreshCapabilities()
    }
    fun export(open: () -> OutputStream?) = action {
        withContext(Dispatchers.IO) {
            val output = checkNotNull(open()) { "Destination could not be opened" }
            output.use { graph.exporter.export(graph.controller.state.value, it) }
        }
        local.update { it.copy(message = "Diagnostic package exported") }
    }
    fun onHidden() { local.update { it.copy(logTail = "") } }
    private fun action(block: suspend () -> Unit) {
        if (local.value.busy) return
        local.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try { block() }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { local.update { it.copy(message = "Operation failed. Storage may be unavailable; no runtime was started.") } }
            finally { local.update { it.copy(busy = false) } }
        }
    }
}
