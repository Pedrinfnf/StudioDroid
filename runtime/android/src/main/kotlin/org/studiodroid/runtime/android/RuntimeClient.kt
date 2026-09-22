// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.studiodroid.core.RuntimeController

/** An Activity owns a client, never the runtime controller or a runtime process. */
class RuntimeClient(private val controller: RuntimeController, private val monitor: MemoryPressureMonitor, private val scope: CoroutineScope) {
    private var connected = false
    fun connect() {
        if (connected) return
        connected = true
        scope.launch { controller.initialize() }
        monitor.acquire()
    }
    fun disconnect() {
        if (!connected) return
        connected = false
        monitor.release()
    }
}
