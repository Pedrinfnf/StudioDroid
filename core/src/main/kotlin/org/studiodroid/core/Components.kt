// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

enum class ComponentKind { HOST_ROOTFS, GUEST_ROOTFS, CPU_TRANSLATOR, WINDOWS_COMPATIBILITY, GRAPHICS_TRANSLATION, GRAPHICS_DRIVER, STUDIO }
enum class InstallState { NOT_INSTALLED, UNKNOWN, INSTALLED, VALIDATED, QUARANTINED }
data class ComponentVersion(val id: String, val version: String, val sha256: String)
data class ComponentRecord(
    val kind: ComponentKind,
    val identity: ComponentVersion? = null,
    val state: InstallState = InstallState.NOT_INSTALLED,
    val actualSha256: String? = null,
    val diskBytes: Long? = null,
    val source: String? = null,
)
