// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app

import android.content.ClipboardManager
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.navigation.NavigationView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogViewerUiTest {
    @Test fun rawDialogDisplaysAndCopiesOnlyTheBoundedTail() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.findViewById<NavigationView>(R.id.navigation).menu.performIdentifierAction(R.id.nav_logs, 0) }
            var opened = false
            repeat(50) {
                if (!opened) {
                    instrumentation.waitForIdleSync()
                    scenario.onActivity { activity ->
                        val button = descendants(activity.findViewById(R.id.content)).filterIsInstance<TextView>()
                            .firstOrNull { it.text.toString() == "Raw JSON" && it.isEnabled }
                        opened = button?.performClick() == true
                    }
                    if (!opened) SystemClock.sleep(200)
                }
            }
            assertTrue("Raw log action available after tail read", opened)
            instrumentation.waitForIdleSync(); SystemClock.sleep(200)
            val root = checkNotNull(instrumentation.uiAutomation.rootInActiveWindow)
            assertTrue(root.findAccessibilityNodeInfosByText("timestampMillis").isNotEmpty())
            LauncherVisualTest().capture("phone-logs-raw")
            val copy = root.findAccessibilityNodeInfosByText("Copy").first { it.isClickable }
            assertTrue(copy.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            instrumentation.waitForIdleSync()
            scenario.onActivity { activity ->
                val clip = activity.getSystemService(ClipboardManager::class.java).primaryClip
                val raw = checkNotNull(clip).getItemAt(0).text.toString()
                assertTrue(raw.startsWith("{")); assertTrue(raw.toByteArray().size <= 16 * 1024)
                assertTrue(raw.contains("timestampMillis"))
            }
            SystemClock.sleep(2000) // Let Android's clipboard overlay finish before later screenshots.
        }
    }
    private fun descendants(view: View): Sequence<View> = sequence {
        yield(view)
        if (view is ViewGroup) for (i in 0 until view.childCount) yieldAll(descendants(view.getChildAt(i)))
    }
}
