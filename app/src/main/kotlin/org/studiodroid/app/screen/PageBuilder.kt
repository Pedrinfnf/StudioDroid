// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import android.content.Context
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textview.MaterialTextView
import org.studiodroid.core.*
import java.util.Locale

class PageBuilder(private val context: Context, host: ViewGroup) {
    private val column = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(12), dp(20), dp(24)) }
    init {
        host.removeAllViews()
        host.addView(NestedScrollView(context).apply { isFillViewport = true; addView(column) }, ViewGroup.LayoutParams(-1, -1))
    }
    fun card(title: String, body: String) {
        val content = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(16), dp(20), dp(16)) }
        content.addView(text(title, 20f).apply { setTypeface(typeface, Typeface.BOLD) })
        content.addView(text(body, 15f).apply { setPadding(0, dp(8), 0, 0) })
        column.addView(MaterialCardView(context).apply { addView(content); radius = dp(20).toFloat() }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })
    }
    fun notice(message: String) = card("Launcher notice", message)
    fun button(label: String, enabled: Boolean = true, action: () -> Unit = {}) {
        column.addView(MaterialButton(context).apply { text = label; isEnabled = enabled; minHeight = dp(48); setOnClickListener { action() } }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
    }
    fun logs(value: String) {
        column.addView(text(value.ifEmpty { "No launcher log entries yet." }, 12f).apply { typeface = Typeface.MONOSPACE; setTextIsSelectable(true) })
    }
    fun profiles(selected: DeviceProfile?, enabled: Boolean, action: (DeviceProfile?) -> Unit) {
        val values = listOf<DeviceProfile?>(null) + DeviceProfile.entries
        val group = RadioGroup(context)
        values.forEachIndexed { index, profile -> group.addView(MaterialRadioButton(context).apply {
            id = index + 100; text = profile?.name?.replace('_', ' ') ?: "Automatic (recommended)"; isEnabled = enabled; minHeight = dp(48)
        }) }
        group.check(values.indexOf(selected) + 100)
        group.setOnCheckedChangeListener { _, checked -> values.getOrNull(checked - 100).let(action) }
        column.addView(group)
    }
    private fun text(value: String, size: Float): TextView = MaterialTextView(context).apply { text = value; textSize = size }
    private fun dp(value: Int) = (value * context.resources.displayMetrics.density).toInt()
}
data class ScreenActions(val refresh: () -> Unit, val export: () -> Unit, val profile: (DeviceProfile?) -> Unit)
fun bytes(value: Long?): String = value?.let { String.format(Locale.ROOT, "%.1f MiB", it / (1024.0 * 1024)) } ?: "Unknown"
fun <T> observation(value: Observation<T>?): String = when (value) {
    is Observation.Known -> "${value.value} (${value.source})"
    is Observation.Unknown -> "Unknown — ${value.reason}"
    null -> "Not measured yet"
}
fun availability(value: Availability): String = when (value) {
    Availability.Available -> "Available"
    is Availability.Unimplemented -> value.reason
    is Availability.Unsupported -> value.reason
    is Availability.Unverified -> value.reason
}
