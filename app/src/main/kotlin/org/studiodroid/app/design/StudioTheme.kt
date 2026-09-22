// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.design

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.LinearLayout
import com.google.android.material.textview.MaterialTextView

object StudioTheme {
    const val background = 0xFF080F1D.toInt()
    const val surface = 0xFF101B2C.toInt()
    const val raised = 0xFF14243A.toInt()
    const val border = 0xFF253850.toInt()
    const val text = 0xFFECF4FF.toInt()
    const val muted = 0xFFA0B2CD.toInt()
    const val cyan = 0xFF4DE0F0.toInt()
    const val blue = 0xFF72B3FF.toInt()
    const val purple = 0xFFB5A3F5.toInt()
    const val green = 0xFF7CDDB3.toInt()
    const val amber = 0xFFECC080.toInt()
    const val error = 0xFFFFA4AE.toInt()
    fun Context.dp(n: Int) = (resources.displayMetrics.density * n).toInt()
    fun Context.shape(fill: Int = surface, stroke: Int = border, radius: Int = 16) = GradientDrawable().apply {
        setColor(fill); cornerRadius = dp(radius).toFloat(); setStroke(dp(1).coerceAtLeast(1), stroke)
    }
    fun Context.label(value: String, size: Float = 14f, color: Int = text, bold: Boolean = false) = MaterialTextView(this).apply {
        text = value; textSize = size; setTextColor(color); includeFontPadding = false
        if (bold) typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        breakStrategy = android.graphics.text.LineBreaker.BREAK_STRATEGY_HIGH_QUALITY
    }
    fun Context.column(padding: Int = 0) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
    }
    fun Context.motionAllowed(): Boolean = ValueAnimator.areAnimatorsEnabled() &&
        !getSystemService(AccessibilityManager::class.java).isTouchExplorationEnabled
    fun View.clickSurface(fill: Int = surface, selected: Boolean = false, action: () -> Unit) {
        background = RippleDrawable(ColorStateList.valueOf(0x224DE0F0), context.shape(fill, if (selected) cyan else border), null)
        isFocusable = true; minimumHeight = context.dp(52); setOnClickListener { action() }
    }
}
