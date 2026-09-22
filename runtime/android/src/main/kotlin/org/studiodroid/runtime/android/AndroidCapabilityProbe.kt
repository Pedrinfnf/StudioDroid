// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.os.PowerManager
import android.os.Process
import android.os.StatFs
import android.system.Os
import android.system.OsConstants
import org.studiodroid.core.*
import java.io.File

class AndroidCapabilityProbe(context: Context) {
    private val app = context.applicationContext
    private val activity = app.getSystemService(ActivityManager::class.java)
    fun memory(includePss: Boolean = false): MemorySnapshot {
        val info = ActivityManager.MemoryInfo().also(activity::getMemoryInfo)
        val swap = if (includePss) readSwap() else emptyMap()
        return MemorySnapshot(info.totalMem, info.availMem, info.threshold, info.lowMemory, activity.isLowRamDevice,
            if (includePss) runCatching { Debug.getPss() * 1024L }.getOrNull() else null,
            swap["SwapTotal"], swap["SwapFree"])
    }
    fun probe(): DeviceCapabilities {
        val vulkan = app.packageManager.systemAvailableFeatures
            .firstOrNull { it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION }?.version
        val version = if (vulkan != null && vulkan > 0) Observation.Known(
            "${vulkan ushr 22}.${(vulkan ushr 12) and 1023}.${vulkan and 4095}", "PackageManager feature declaration; not native-driver qualification")
        else Observation.Unknown("No Vulkan hardware version advertised by PackageManager")
        val soc = if (Build.VERSION.SDK_INT >= 31) listOf(Build.SOC_MANUFACTURER, Build.SOC_MODEL)
            .filter { it.isNotBlank() && it != Build.UNKNOWN }.joinToString(" ") else ""
        return DeviceCapabilities(
            System.currentTimeMillis(), Build.VERSION.SDK_INT, Build.VERSION.RELEASE, "${Build.MANUFACTURER} ${Build.MODEL}",
            Build.SUPPORTED_ABIS.toList(), Process.is64Bit(),
            observe("Os.sysconf") { Os.sysconf(OsConstants._SC_PAGESIZE).also { check(it > 0) } },
            if (soc.isNotEmpty()) Observation.Known(soc, "Build.SOC_MANUFACTURER/SOC_MODEL") else Observation.Unknown("Public SoC identity unavailable"),
            GpuVendor.UNKNOWN_OTHER, Observation.Unknown("GPU model requires a future isolated native probe"), version,
            Observation.Unknown("Native Vulkan driver probe not implemented"), Observation.Unknown("Native Vulkan extension probe not implemented"),
            memory(true), observe("PowerManager") { app.getSystemService(PowerManager::class.java).currentThermalStatus },
            observe("StatFs app-managed storage") { StatFs(app.filesDir.path).availableBytes },
            observe("StatFs app-managed storage") { StatFs(app.filesDir.path).totalBytes },
        )
    }
    private fun <T> observe(source: String, action: () -> T): Observation<T> = try {
        Observation.Known(action(), source)
    } catch (_: Exception) { Observation.Unknown("$source unavailable") }
    private fun readSwap(): Map<String, Long> = runCatching {
        val result = mutableMapOf<String, Long>()
        File("/proc/meminfo").bufferedReader().use { reader ->
            // Kernel virtual file, bounded characters, never a whole-file read.
            var remaining = 16 * 1024
            val line = StringBuilder(128)
            while (remaining-- > 0) {
                val char = reader.read(); if (char < 0) break
                if (char == 10) {
                    val parts = line.toString().trim().split(Regex("\\s+"))
                    if (parts.size == 3 && parts[0] in setOf("SwapTotal:", "SwapFree:") && parts[2] == "kB") {
                        parts[1].toLongOrNull()?.takeIf { it >= 0 && it <= Long.MAX_VALUE / 1024 }?.let { result[parts[0].dropLast(1)] = it * 1024 }
                    }
                    line.setLength(0)
                } else if (line.length < 256) line.append(char.toChar())
            }
        }
        result
    }.getOrDefault(emptyMap())
}
