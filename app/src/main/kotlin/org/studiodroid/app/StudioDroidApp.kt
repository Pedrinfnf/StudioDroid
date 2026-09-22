// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app

import android.app.Application
import org.studiodroid.core.RuntimeController
import org.studiodroid.runtime.android.RuntimeOwner

class StudioDroidApp : Application(), RuntimeOwner {
    val graph: AppGraph by lazy { AppGraph(this) }
    override val runtimeController: RuntimeController get() = graph.controller
}
