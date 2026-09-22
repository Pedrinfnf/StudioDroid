// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app

import android.content.Context
import kotlinx.coroutines.*
import org.studiodroid.runtime.android.*
import java.io.File

class AppGraph(context: Context) {
    private val app = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val settings = SettingsRepository(app)
    val logs = RotatingLogStore(File(app.filesDir, "logs"))
    val sessions = SessionRepository(File(app.filesDir, "sessions"))
    private val probe = AndroidCapabilityProbe(app)
    val controller = AndroidRuntimeController(probe, RuntimeComposition(), sessions, settings, logs)
    private val monitor = MemoryPressureMonitor(app, scope, probe, controller)
    val exporter = DiagnosticExporter(logs)
    fun client() = RuntimeClient(controller, monitor, scope)
}
