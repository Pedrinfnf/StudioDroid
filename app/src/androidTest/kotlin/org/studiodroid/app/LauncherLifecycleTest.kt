// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.navigation.NavigationView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.studiodroid.core.Availability
import org.studiodroid.core.RuntimeController

@RunWith(AndroidJUnit4::class)
class LauncherLifecycleTest {
    @Test fun recreationKeepsNavigationAndDoesNotCreateAnotherController() {
        var controller: RuntimeController? = null
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                controller = (activity.application as StudioDroidApp).runtimeController
                activity.findViewById<NavigationView>(R.id.navigation).menu.performIdentifierAction(R.id.nav_runtime, 0)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.recreate()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                assertSame(controller, (activity.application as StudioDroidApp).runtimeController)
                assertEquals(R.id.nav_runtime, activity.findViewById<NavigationView>(R.id.navigation).checkedItem?.itemId)
                assertNotEquals(Availability.Available, controller!!.state.value.availability)
            }
        }
    }
}
