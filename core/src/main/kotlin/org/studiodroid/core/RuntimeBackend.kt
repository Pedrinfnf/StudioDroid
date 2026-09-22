// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

interface RuntimeBackend {
    val descriptor: BackendDescriptor
    suspend fun initialize(): Availability
    suspend fun validate(request: LaunchRequest): Set<LaunchIssue>
    suspend fun prepareEnvironment(request: LaunchRequest): PlanResult
    suspend fun prepareLaunch(request: LaunchRequest): PlanResult
    suspend fun launch(plan: LaunchPlan): LaunchResult
    suspend fun stop(): Availability
    fun getStatus(): Availability
    fun getCapabilities(): Set<String>
    fun getVersion(): String?
    suspend fun getDiagnostics(): List<DiagnosticEvent>
    suspend fun cleanup()
}
