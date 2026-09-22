// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app

import android.graphics.Rect
import android.content.ContentValues
import android.provider.MediaStore
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.navigation.NavigationView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.studiodroid.app.design.LogPresentation
import org.studiodroid.core.DeviceProfile

/** Captures actual Android views, including compact, large-font and landscape CI runs. */
@RunWith(AndroidJUnit4::class)
class LauncherVisualTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val modes = listOf("home" to R.id.nav_home, "runtime" to R.id.nav_runtime,
        "diagnostics" to R.id.nav_diagnostics, "storage" to R.id.nav_storage,
        "logs" to R.id.nav_logs, "settings" to R.id.nav_settings, "about" to R.id.nav_about)

    @Test fun allDestinationsRenderAndCapture() {
        val variant = InstrumentationRegistry.getArguments().getString("visualVariant", "phone")
        require(variant.matches(Regex("[a-z0-9_-]+")))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            awaitIdle(scenario)
            modes.forEach { (name, id) ->
                scenario.onActivity { it.findViewById<NavigationView>(R.id.navigation).menu.performIdentifierAction(id, 0) }
                awaitIdle(scenario)
                scenario.onActivity {
                    assertEquals(id, it.findViewById<NavigationView>(R.id.navigation).checkedItem?.itemId)
                    val list = it.findViewById<RecyclerView>(R.id.launcher_list)
                    assertTrue("$name has rows", (list.adapter?.itemCount ?: 0) > 0)
                    list.scrollToPosition(0)
                }
                instrumentation.waitForIdleSync(); SystemClock.sleep(180)
                scenario.onActivity { assertReadable(it.findViewById(R.id.content)) }
                capture("$variant-$name")
                scenario.onActivity { val list = it.findViewById<RecyclerView>(R.id.launcher_list); list.scrollToPosition((list.adapter?.itemCount ?: 1) - 1) }
                instrumentation.waitForIdleSync(); SystemClock.sleep(180)
                scenario.onActivity { assertReadable(it.findViewById(R.id.content)) }
                capture("$variant-$name-bottom")
            }
            scenario.onActivity { it.findViewById<DrawerLayout>(R.id.drawer).openDrawer(GravityCompat.START, false) }
            instrumentation.waitForIdleSync(); SystemClock.sleep(180)
            scenario.onActivity { assertReadable(it.findViewById(R.id.navigation)) }
            capture("$variant-drawer")
        }
    }
    @Test fun profileCardsKeepExistingPersistence() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            awaitIdle(scenario)
            scenario.onActivity { it.findViewById<NavigationView>(R.id.navigation).menu.performIdentifierAction(R.id.nav_settings, 0) }
            awaitIdle(scenario)
            scenario.onActivity { activity ->
                val list = activity.findViewById<RecyclerView>(R.id.launcher_list)
                list.scrollToPosition(2)
            }
            instrumentation.waitForIdleSync(); SystemClock.sleep(180)
            scenario.onActivity { activity ->
                val card = descendants(activity.findViewById(R.id.content)).first { it.contentDescription?.startsWith("Low memory.") == true }
                assertTrue(card.performClick())
            }
            awaitIdle(scenario)
            scenario.onActivity { assertEquals(DeviceProfile.LOW_MEMORY, (it.application as StudioDroidApp).graph.settings.state.value.profileOverride) }
            scenario.recreate(); awaitIdle(scenario)
            scenario.onActivity { assertEquals(DeviceProfile.LOW_MEMORY, (it.application as StudioDroidApp).graph.settings.state.value.profileOverride) }
            // Return to automatic using the same UI action; no persistent test override.
            scenario.onActivity { it.findViewById<RecyclerView>(R.id.launcher_list).scrollToPosition(1) }
            instrumentation.waitForIdleSync(); SystemClock.sleep(180)
            scenario.onActivity { activity -> descendants(activity.findViewById(R.id.content)).first { it.contentDescription?.startsWith("Automatic.") == true }.performClick() }
            awaitIdle(scenario)
        }
    }
    @Test fun logViewerPreservesUnknownAndBounds() {
        val entries = LogPresentation.parse("{\"category\":\"ANDROID\",\"code\":\"MEMORY_PRESSURE\"}\nnot-json\n")
        assertEquals(2, entries.size)
        assertTrue(entries.first().malformed)
        assertEquals("Unknown time", entries.last().time)
        assertNull(entries.last().session)
        assertNull(entries.last().value)
        assertTrue(LogPresentation.parse(" ").isEmpty())
        assertTrue(LogPresentation.parse("{}\n".repeat(10000)).size <= 5462)
    }
    private fun awaitIdle(scenario: ActivityScenario<MainActivity>) {
        // Wait for actual controller/UI completion, bounded to 10 seconds.
        repeat(50) {
            instrumentation.waitForIdleSync()
            var ready = false
            scenario.onActivity { activity -> ready = !(activity.application as StudioDroidApp).runtimeController.state.value.refreshing && (activity.findViewById<RecyclerView>(R.id.launcher_list).adapter?.itemCount ?: 0) > 0 }
            if (ready) { SystemClock.sleep(200); return }
            SystemClock.sleep(200)
        }
        fail("Launcher did not settle within ten seconds")
    }
    private fun descendants(view: View): Sequence<View> = sequence {
        yield(view)
        if (view is ViewGroup) for (i in 0 until view.childCount) yieldAll(descendants(view.getChildAt(i)))
    }
    private fun assertReadable(root: View) {
        descendants(root).filterIsInstance<TextView>().filter { it.isShown && it.getGlobalVisibleRect(Rect()) }.forEach { view ->
            val layout = view.layout ?: return@forEach
            for (line in 0 until layout.lineCount) assertEquals("Clipped text: ${view.text}", 0, layout.getEllipsisCount(line))
            assertTrue("Zero-width label: ${view.text}", view.width > 0)
        }
    }
    private fun capture(name: String) {
        val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        val resolver = instrumentation.targetContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$name.png")
            put(MediaStore.Downloads.MIME_TYPE, "image/png")
            put(MediaStore.Downloads.RELATIVE_PATH, "Download/studiodroid-qa")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = checkNotNull(resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values))
        try {
            checkNotNull(resolver.openOutputStream(uri)).use {
                check(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it))
            }
            values.clear(); values.put(MediaStore.Downloads.IS_PENDING, 0)
            check(resolver.update(uri, values, null, null) == 1)
        } finally { bitmap.recycle() }
    }
}
