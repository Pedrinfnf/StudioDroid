// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

import java.util.UUID

enum class LogCategory { ANDROID, CONTAINER, FEX, WINE, DXVK, VULKAN, DRIVER, STUDIO, INPUT, AUDIO, NETWORK, STORAGE }
enum class LogCode { LAUNCHER_INITIALIZED, CAPABILITIES_REFRESHED, CAPABILITY_PROBE_FAILED, MEMORY_PRESSURE, LAUNCH_REFUSED, SETTINGS_CHANGED, SESSION_RECOVERY_REQUIRED, DIAGNOSTICS_EXPORTED }
/** M1 deliberately accepts no free-form messages, credentials, or filesystem paths. */
data class DiagnosticEvent(val timestampMillis: Long, val category: LogCategory, val code: LogCode, val sessionId: UUID? = null, val value: Long? = null)
data class StorageUsage(val logsBytes: Long, val sessionBytes: Long, val availableBytes: Long?)
