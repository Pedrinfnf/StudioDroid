// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

enum class RendererProfile { DEFAULT, COMPATIBILITY, LOW_MEMORY, PERFORMANCE, CUSTOM }
enum class DriverProviderKind { SYSTEM, BUNDLED, CUSTOM }
data class GraphicsSelection(val provider: DriverProviderKind = DriverProviderKind.SYSTEM, val renderer: RendererProfile = RendererProfile.COMPATIBILITY)
interface GraphicsBackend { val availability: Availability; suspend fun validate(device: DeviceCapabilities): Availability }
interface VulkanDriverProvider { val kind: DriverProviderKind; suspend fun validate(device: DeviceCapabilities): Availability }
interface VulkanTransport { val availability: Availability }
/** Owned by StudioDroid. Optional internal Wine protocol transport is deliberately unselected. */
interface PresentationBackend { val availability: Availability; suspend fun detach() }
/** Surface/lifecycle/input ownership contract only; no Surface or X server implementation in M1. */
interface NativeSurfaceBackend : PresentationBackend
