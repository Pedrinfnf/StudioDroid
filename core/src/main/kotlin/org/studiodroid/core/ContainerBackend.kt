// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

interface ContainerBackend {
    val descriptor: BackendDescriptor
    suspend fun probe(): Availability
    suspend fun validate(plan: LaunchPlan): Set<LaunchIssue>
    suspend fun prepare(plan: LaunchPlan): Availability
    suspend fun start(plan: LaunchPlan): LaunchResult
    suspend fun stop(): Availability
    suspend fun cleanup()
}
