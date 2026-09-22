// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.core

import org.junit.Assert.*
import org.junit.Test

class LaunchPlannerTest {
    private val planner = LaunchPlanner()
    private val version = ComponentVersion("fixture", "1", "a".repeat(64))
    private val installed = listOf(ComponentRecord(ComponentKind.CPU_TRANSLATOR, version, InstallState.VALIDATED, version.sha256))
    private val backend = listOf(BackendDescriptor("fixture", "Test only", Availability.Available))
    private val request = LaunchRequest("fixture", "fixture", listOf(version), ProcessArguments(ComponentPath("fixture", "bin/test"), listOf("one argument with spaces"), mapOf("LANG" to "C"), "/tmp"))
    private fun prepare(value: LaunchRequest = request, capabilities: DeviceCapabilities? = device(), records: List<ComponentRecord> = installed) =
        planner.prepare(value, capabilities, backend, backend, records, DeviceProfile.LOW_MEMORY)
    @Test fun noRegisteredBackendCannotProduceLaunchPlan() {
        val result = planner.prepare(request, device(), emptyList(), emptyList(), emptyList(), DeviceProfile.LOW_MEMORY) as PlanResult.Blocked
        assertTrue(LaunchIssue.BACKEND_UNAVAILABLE in result.issues)
        assertTrue(LaunchIssue.CONTAINER_UNAVAILABLE in result.issues)
        assertTrue(LaunchIssue.COMPONENT_UNVERIFIED in result.issues)
    }
    @Test fun unknownDeviceAndWrongAbiCannotBypassValidation() {
        assertTrue(LaunchIssue.DEVICE_UNKNOWN in (prepare(capabilities = null) as PlanResult.Blocked).issues)
        assertTrue(LaunchIssue.ARM64_REQUIRED in (prepare(capabilities = device().copy(process64Bit = false)) as PlanResult.Blocked).issues)
    }
    @Test fun onlyObservedVerifiedComponentsAreAccepted() {
        assertTrue(prepare(records = installed.map { it.copy(actualSha256 = null) }) is PlanResult.Blocked)
        assertTrue(prepare(records = installed.map { it.copy(state = InstallState.INSTALLED) }) is PlanResult.Blocked)
    }
    @Test fun pathTraversalControlsAndAmbiguousPathsAreRejected() {
        for (path in listOf("../bin/test", "/bin/test", "bin/../test", "bin//test", "bin/test/", "bin/test\n")) {
            assertTrue(path, prepare(request.copy(process = request.process.copy(executable = ComponentPath("fixture", path)))) is PlanResult.Blocked)
        }
        for (path in listOf("relative", "/tmp/../other", "/tmp//other", "/tmp/", "/tmp/other\n")) {
            assertTrue(path, prepare(request.copy(process = request.process.copy(workingDirectory = path))) is PlanResult.Blocked)
        }
    }
    @Test fun arbitraryLoaderEnvironmentAndNulArgumentsAreRejected() {
        assertTrue(prepare(request.copy(process = request.process.copy(environment = mapOf("LD_PRELOAD" to "/evil")))) is PlanResult.Blocked)
        assertTrue(prepare(request.copy(process = request.process.copy(arguments = listOf("a\u0000b")))) is PlanResult.Blocked)
    }
    @Test fun criticalMemoryPreventsPreparingEvenAnOtherwiseVerifiedFixture() {
        assertTrue(LaunchIssue.MEMORY_PRESSURE in (prepare(capabilities = device(memory().copy(availableBytes = 0))) as PlanResult.Blocked).issues)
    }
    @Test fun structuredArgumentsRemainSeparateAndSessionsAreUnique() {
        val first = (prepare() as PlanResult.Prepared).plan
        val second = (prepare() as PlanResult.Prepared).plan
        assertEquals(listOf("one argument with spaces"), first.request.process.arguments)
        assertNotEquals(first.sessionId, second.sessionId)
    }
    @Test fun duplicateReferencesAreRejected() { assertTrue(prepare(request.copy(components = listOf(version, version))) is PlanResult.Blocked) }
}
