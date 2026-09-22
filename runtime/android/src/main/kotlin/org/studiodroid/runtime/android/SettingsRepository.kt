// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.studiodroid.core.DeviceProfile

data class LauncherSettings(val profileOverride: DeviceProfile? = null)
class SettingsRepository(context: Context) {
    private val app = context.applicationContext
    private val mutable = MutableStateFlow(LauncherSettings())
    val state = mutable.asStateFlow()
    // Called on the controller's I/O dispatcher, never during Activity inflation.
    @Synchronized fun load() {
        val value = app.getSharedPreferences("launcher-settings", Context.MODE_PRIVATE).getString("profile", null)
        mutable.value = LauncherSettings(DeviceProfile.entries.firstOrNull { it.name == value })
    }
    @Synchronized fun setProfile(profile: DeviceProfile?) {
        check(app.getSharedPreferences("launcher-settings", Context.MODE_PRIVATE).edit().putString("profile", profile?.name).commit()) { "Settings could not be saved" }
        mutable.value = LauncherSettings(profile)
    }
}
