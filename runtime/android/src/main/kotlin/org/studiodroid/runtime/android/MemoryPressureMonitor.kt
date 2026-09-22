// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import kotlinx.coroutines.*
import org.studiodroid.core.*

/** Active only while clients are visible. No foreground/background service or permanent polling. */
class MemoryPressureMonitor(
    context: Context, private val scope: CoroutineScope, private val probe: AndroidCapabilityProbe,
    private val controller: AndroidRuntimeController,
) : ComponentCallbacks2 {
    private val app = context.applicationContext
    private var clients = 0
    private var job: Job? = null
    @Volatile private var pressureHint = false
    @Synchronized fun acquire() {
        clients++
        if (clients != 1) return
        app.registerComponentCallbacks(this)
        job = scope.launch(Dispatchers.IO) {
            var sample = 0
            while (isActive) {
                val memory = try { probe.memory(includePss = sample++ % 12 == 0) } catch (_: Exception) {
                    MemorySnapshot(null, null, null, null, null)
                }
                val observed = if (pressureHint) memory.copy(lowMemory = true) else memory
                pressureHint = false
                controller.observeMemory(observed)
                delay(MemoryPolicy.sampleIntervalMillis(MemoryPolicy.pressure(observed)))
            }
        }
    }
    @Synchronized fun release() {
        if (clients == 0) return
        clients--
        if (clients == 0) { job?.cancel(); job = null; app.unregisterComponentCallbacks(this); pressureHint = false }
    }
    @Suppress("DEPRECATION") override fun onTrimMemory(level: Int) {
        if (level in ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW..ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) pressureHint = true
    }
    @Deprecated("Fallback for older Android callbacks") override fun onLowMemory() { pressureHint = true }
    override fun onConfigurationChanged(newConfig: Configuration) = Unit
}
