// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import org.studiodroid.core.RuntimeController

interface RuntimeOwner { val runtimeController: RuntimeController }
/** Reserved bound-service access to the same controller. Not started/bound by M1 UI. */
class RuntimeService : Service() {
    inner class LocalBinder : Binder() {
        fun controller(): RuntimeController = (application as RuntimeOwner).runtimeController
    }
    override fun onBind(intent: Intent): IBinder = LocalBinder()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { stopSelf(startId); return START_NOT_STICKY }
}
