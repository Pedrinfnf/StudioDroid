// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.runtime.android

import org.studiodroid.core.Availability

/** JNI ABI boundary only. M1 never loads a library, accepts commands, or starts native work. */
class NativeBridge {
    val availability: Availability = Availability.Unimplemented("Native runtime integration is not implemented")
    companion object { const val CONTRACT_VERSION = 1 }
}
