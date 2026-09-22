// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.studiodroid.core.*

class RotatingLogStoreTest {
    @get:Rule val temporary = TemporaryFolder()
    @Test fun rotationAndTailStayWithinBudgets() {
        val directory = temporary.newFolder()
        val store = RotatingLogStore(directory); store.configure(4096)
        repeat(200) { store.append(DiagnosticEvent(it.toLong(), LogCategory.ANDROID, LogCode.CAPABILITIES_REFRESHED)) }
        assertTrue(store.diskBytes() <= 4096)
        assertTrue(directory.listFiles()!!.size <= 4)
        val tail = store.readTail(512)
        assertTrue(tail.toByteArray().size <= 512)
        assertTrue(tail.contains("199"))
        assertTrue(tail.lines().filter { it.isNotBlank() }.all { it.startsWith("{") && it.endsWith("}") })
    }
    @Test fun reducingBudgetDoesNotLeaveOversizedLogs() {
        val store = RotatingLogStore(temporary.newFolder()); store.configure(8192)
        repeat(200) { store.append(DiagnosticEvent(it.toLong(), LogCategory.ANDROID, LogCode.CAPABILITIES_REFRESHED)) }
        store.configure(4096)
        assertTrue(store.diskBytes() <= 4096)
    }
}
