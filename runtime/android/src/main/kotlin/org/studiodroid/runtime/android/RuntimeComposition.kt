// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import org.studiodroid.core.*

/** Registration boundary. No pretend translator/container is installed or available in M1. */
class RuntimeComposition {
    val backends: List<RuntimeBackend> = emptyList()
    val containers: List<ContainerBackend> = emptyList()
    val presentation: Availability = Availability.Unimplemented("NativeSurfaceBackend is reserved for M6; transport remains open")
    val graphics = GraphicsSelection()
}
