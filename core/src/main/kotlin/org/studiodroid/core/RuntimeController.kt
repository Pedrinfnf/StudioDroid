// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

import kotlinx.coroutines.flow.StateFlow

interface RuntimeController {
    val state: StateFlow<RuntimeSnapshot>
    suspend fun initialize()
    suspend fun refreshCapabilities()
    suspend fun prepareLaunch(request: LaunchRequest): PlanResult
    suspend fun launch(plan: LaunchPlan): LaunchResult
    suspend fun stop(): Availability
}
