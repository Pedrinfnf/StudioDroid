// SPDX-License-Identifier: GPL-3.0-only
// These views require display data and are constructed by PageBuilder, never XML inflation.
@file:android.annotation.SuppressLint("ViewConstructor")
package org.studiodroid.app.design

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.*
import androidx.core.view.ViewCompat
import com.google.android.material.button.MaterialButton
import org.studiodroid.app.R
import org.studiodroid.app.design.StudioTheme.green
import org.studiodroid.app.design.StudioTheme.purple
import org.studiodroid.app.design.StudioTheme.amber
import org.studiodroid.app.design.StudioTheme.error
import org.studiodroid.app.design.StudioTheme.muted
import org.studiodroid.app.design.StudioTheme.cyan
import org.studiodroid.app.design.StudioTheme.blue
import org.studiodroid.app.design.StudioTheme.raised
import org.studiodroid.app.design.StudioTheme.surface
import org.studiodroid.app.design.StudioTheme.border
import org.studiodroid.app.design.StudioTheme.dp
import org.studiodroid.app.design.StudioTheme.shape
import org.studiodroid.app.design.StudioTheme.label
import org.studiodroid.app.design.StudioTheme.column
import org.studiodroid.app.design.StudioTheme.motionAllowed
import org.studiodroid.app.design.StudioTheme.clickSurface

/** Visual states describe evidence. Unknown is neutral, never an error. */
enum class Tone(val color: Int) { INFO(cyan), NEUTRAL(muted), GOOD(green), PLANNED(purple), CAUTION(amber), ERROR(error) }
data class Badge(val text: String, val tone: Tone = Tone.NEUTRAL)
data class Metric(val label: String, val value: String, val icon: Int, val detail: String? = null, val badge: Badge? = null)

class StatusChip(context: Context, badge: Badge) : androidx.appcompat.widget.AppCompatTextView(context) {
    init {
        text = badge.text; textSize = 10f; setTextColor(badge.tone.color)
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL); letterSpacing = 0.05f
        setPadding(context.dp(8), context.dp(5), context.dp(8), context.dp(5))
        background = context.shape(StudioTheme.background, badge.tone.color and 0x66FFFFFF, 6)
        contentDescription = "Status: ${badge.text}"
    }
}
class TechnicalValue(context: Context, value: String, size: Float = 21f) : androidx.appcompat.widget.AppCompatTextView(context) {
    init { text = value; textSize = size; setTextColor(StudioTheme.text); typeface = Typeface.MONOSPACE; includeFontPadding = false }
}
fun icon(context: Context, resource: Int, color: Int = StudioTheme.cyan, size: Int = 24) = ImageView(context).apply {
    setImageResource(resource); imageTintList = ColorStateList.valueOf(color)
    layoutParams = LinearLayout.LayoutParams(context.dp(size), context.dp(size))
    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
}
fun LinearLayout.gap(height: Int) { addView(View(context), LinearLayout.LayoutParams(1, context.dp(height))) }
fun LinearLayout.line(view: View, top: Int = 0) { addView(view, LinearLayout.LayoutParams(-1, -2).apply { topMargin = context.dp(top) }) }

class SectionHeader(context: Context, title: String, caption: String? = null) : LinearLayout(context) {
    init {
        orientation = VERTICAL; setPadding(0, context.dp(14), 0, context.dp(4))
        line(context.label(title, 17f, StudioTheme.text, true).apply { ViewCompat.setAccessibilityHeading(this, true) })
        caption?.let { line(context.label(it, 12f, muted), 5) }
    }
}
open class StatusRow(context: Context, title: String, description: String, resource: Int, badge: Badge, version: String? = null) : LinearLayout(context) {
    init {
        orientation = HORIZONTAL; gravity = Gravity.TOP; background = context.shape()
        setPadding(context.dp(14), context.dp(14), context.dp(14), context.dp(14))
        addView(icon(context, resource, badge.tone.color))
        val body = context.column().apply {
            line(context.label(title, 15f, StudioTheme.text, true))
            if (description.isNotBlank()) line(context.label(description, 12f, muted), 4)
            val badges = LinearLayout(context).apply { orientation = HORIZONTAL; addView(StatusChip(context, badge)) }
            line(badges, 8)
            version?.let { line(TechnicalValue(context, it, 12f), 7) }
        }
        addView(body, LayoutParams(0, -2, 1f).apply { marginStart = context.dp(12) })
    }
}
class RuntimeStage(context: Context, title: String, description: String, resource: Int, badge: Badge, last: Boolean, version: String? = null) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        line(StatusRow(context, title, description, resource, badge, version))
        if (!last) addView(View(context).apply { setBackgroundColor(border); importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }, LayoutParams(context.dp(2), context.dp(12)).apply { marginStart = context.dp(26) })
    }
}
class MetricCard(context: Context, metric: Metric) : LinearLayout(context) {
    init {
        orientation = VERTICAL; background = context.shape(); setPadding(context.dp(14), context.dp(14), context.dp(14), context.dp(14))
        addView(icon(context, metric.icon, blue, 20)); gap(12)
        line(context.label(metric.label, 11f, muted)); line(TechnicalValue(context, metric.value), 5)
        metric.detail?.let { line(context.label(it, 11f, muted), 6) }
        metric.badge?.let { addView(StatusChip(context, it), LayoutParams(-2, -2).apply { topMargin = context.dp(8) }) }
    }
}
open class PrimaryAction(context: Context, label: String, enabled: Boolean, resource: Int = R.drawable.ic_launch, action: () -> Unit) : MaterialButton(context) {
    init {
        text = label; isEnabled = enabled; isAllCaps = false; textSize = 15f
        minHeight = context.dp(54); cornerRadius = context.dp(12); insetTop = 0; insetBottom = 0
        setTextColor(ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()), intArrayOf(0xFF9BB7DC.toInt(), StudioTheme.text)))
        backgroundTintList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()), intArrayOf(0xFF172D49.toInt(), 0xFF235CE8.toInt()))
        icon = androidx.appcompat.content.res.AppCompatResources.getDrawable(context, resource); iconTint = textColors; iconGravity = ICON_GRAVITY_TEXT_START
        setOnClickListener { action() }
    }
}
class SecondaryAction(context: Context, label: String, enabled: Boolean, resource: Int = R.drawable.ic_refresh, action: () -> Unit) : PrimaryAction(context, label, enabled, resource, action) {
    init { backgroundTintList = ColorStateList.valueOf(raised); setTextColor(ColorStateList.valueOf(blue)); iconTint = textColors; strokeWidth = context.dp(1); strokeColor = ColorStateList.valueOf(border) }
}
class EmptyState(context: Context, title: String, message: String, resource: Int, badge: Badge = Badge("FUTURE", Tone.PLANNED)) : StatusRow(context, title, message, resource, badge)
class ProgressIndicator(context: Context, label: String, fraction: Float?, detail: String, tone: Tone = Tone.INFO) : LinearLayout(context) {
    init {
        orientation = VERTICAL; background = context.shape(); setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
        line(context.label(label, 14f, StudioTheme.text, true)); gap(10)
        if (fraction != null) {
            addView(ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 1000; progress = (fraction.coerceIn(0f, 1f) * 1000).toInt(); isIndeterminate = false
                progressTintList = ColorStateList.valueOf(tone.color); progressBackgroundTintList = ColorStateList.valueOf(border)
                contentDescription = "$label: ${(fraction * 100).toInt()} percent"
            }, LayoutParams(-1, context.dp(6)))
        } else line(context.label("Unknown · no proportion available", 12f, muted))
        line(context.label(detail, 12f, muted), 10)
    }
}
class ProfileOption(context: Context, title: String, detail: String, resource: Int, selected: Boolean, enabled: Boolean, action: () -> Unit) : LinearLayout(context) {
    init {
        orientation = HORIZONTAL; gravity = Gravity.TOP; setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
        clickSurface(if (selected) raised else surface, selected, action); isEnabled = enabled
        addView(icon(context, if (selected) R.drawable.ic_check else resource, if (selected) cyan else muted))
        addView(context.column().apply {
            line(context.label(title, 15f, if (selected) cyan else StudioTheme.text, true))
            line(context.label(detail, 12f, muted), 5)
            if (selected) line(context.label("SELECTED", 10f, cyan, true), 8)
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }, LayoutParams(0, -2, 1f).apply { marginStart = context.dp(12) })
        contentDescription = "$title. $detail"
        accessibilityDelegate = object : AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info); info.className = "android.widget.RadioButton"; info.isCheckable = true; info.isChecked = selected
            }
        }
    }
}
