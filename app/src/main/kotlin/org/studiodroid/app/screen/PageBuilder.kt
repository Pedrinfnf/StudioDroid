// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.studiodroid.app.R
import org.studiodroid.app.design.*
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
import org.studiodroid.core.*
import java.util.Locale

/** A small virtualized design-system DSL, shared by every destination. */
class PageBuilder(private val context: Context, host: ViewGroup) {
    private val adapter = LauncherList()
    private val rows = mutableListOf<DisplayRow>()
    private var destination = ""
    private var changed = false
    private var cachedTail = ""
    private var cachedEntries = emptyList<LogEntry>()
    val list = RecyclerView(context).apply {
        id = R.id.launcher_list; layoutManager = LinearLayoutManager(context)
        adapter = this@PageBuilder.adapter; itemAnimator = null
        clipToPadding = false; setItemViewCacheSize(2)
        val available = context.resources.configuration.screenWidthDp
        val margin = maxOf(16, (available - 840) / 2)
        setPadding(context.dp(margin), context.dp(4), context.dp(margin), context.dp(24))
    }
    init { host.addView(list, ViewGroup.LayoutParams(-1, -1)) }
    fun begin(key: String) {
        rows.clear(); changed = destination != key; destination = key
        if (key != "LOGS") { cachedTail = ""; cachedEntries = emptyList() }
    }
    fun commit() {
        val switch = changed
        adapter.submitList(rows.toList()) {
            if (switch) {
                list.scrollToPosition(0)
                if (context.motionAllowed()) { list.alpha = 0.75f; list.animate().alpha(1f).setDuration(150).start() }
            }
        }
    }
    fun clear() { rows.clear(); cachedTail = ""; cachedEntries = emptyList(); adapter.submitList(emptyList()); list.recycledViewPool.clear() }
    private fun row(key: String, content: Any, gap: Int = 10, create: () -> View) { rows += DisplayRow("$destination/$key", content, gap, create) }
    fun section(title: String, detail: String? = null) = row("section-$title", listOf(title, detail)) { SectionHeader(context, title, detail) }
    fun note(key: String, message: String) = row("note-$key", message) { context.label(message, 12f, muted).apply { setPadding(context.dp(2), 0, context.dp(2), context.dp(4)) } }
    fun notice(message: String) = status("notice-$message", "Launcher notice", message, R.drawable.ic_about, Badge("ATTENTION", Tone.CAUTION))
    fun status(key: String, title: String, detail: String, icon: Int, badge: Badge, version: String? = null) = row(key, listOf(title, detail, badge, version)) { StatusRow(context, title, detail, icon, badge, version) }
    fun stage(key: String, title: String, detail: String, icon: Int, badge: Badge, last: Boolean = false, version: String? = null) = row(key, listOf(title, detail, badge, version, last), if (last) 10 else 0) { RuntimeStage(context, title, detail, icon, badge, last, version) }
    fun empty(key: String, title: String, detail: String, icon: Int, badge: Badge = Badge("FUTURE", Tone.PLANNED)) = row(key, listOf(title, detail, badge)) { EmptyState(context, title, detail, icon, badge) }
    fun primary(label: String, enabled: Boolean, action: () -> Unit = {}) = row("primary-$label", listOf(label, enabled)) { PrimaryAction(context, label, enabled, action = action) }
    fun secondary(label: String, enabled: Boolean = true, icon: Int = R.drawable.ic_refresh, action: () -> Unit) = row("secondary-$label", listOf(label, enabled)) { SecondaryAction(context, label, enabled, icon, action) }
    fun progress(key: String, label: String, fraction: Float?, detail: String, tone: Tone = Tone.INFO) = row(key, listOf(label, fraction, detail, tone)) { ProgressIndicator(context, label, fraction, detail, tone) }
    fun metrics(vararg metrics: Metric) {
        val cfg = context.resources.configuration
        val columns = if (minOf(cfg.screenWidthDp, 840) >= 360 && cfg.fontScale <= 1.3f) 2 else 1
        metrics.toList().chunked(columns).forEach { group ->
            row("metric-${group.first().label}", group) {
                LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.TOP
                    group.forEachIndexed { index, metric -> addView(MetricCard(context, metric), LinearLayout.LayoutParams(0, -1, 1f).apply { if (index > 0) marginStart = context.dp(10) }) }
                }
            }
        }
    }
    fun hero(title: String, subtitle: String, badge: Badge, detail: String) = row("hero", listOf(title, subtitle, badge, detail)) {
        context.column(16).apply {
            background = context.shape(raised, 0xFF315078.toInt(), 16)
            val wide = context.resources.configuration.screenWidthDp >= 600 && context.resources.configuration.fontScale <= 1.3f
            val identity = LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(icon(context, R.drawable.ic_launcher, cyan, 40))
                addView(context.column().apply {
                    line(context.label(title, 22f, StudioTheme.text, true))
                    line(context.label(subtitle, 11f, muted), 5)
                }, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = context.dp(12) })
            }
            val status = context.column().apply {
                addView(StatusChip(context, badge), LinearLayout.LayoutParams(-2, -2))
                line(context.label(detail, 12f, muted), 8)
            }
            if (wide) line(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(identity, LinearLayout.LayoutParams(0, -2, 1.2f))
                addView(status, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = context.dp(20) })
            }) else { line(identity); line(status, 14) }
        }
    }
    fun profiles(selected: DeviceProfile?, enabled: Boolean, action: (DeviceProfile?) -> Unit) {
        val choices = listOf(
            Triple<DeviceProfile?, String, String>(null, "Automatic", "Recommended · safe defaults for this device"),
            Triple(DeviceProfile.LOW_MEMORY, "Low memory", "Conservative resource budgets · designed for 4 GB"),
            Triple(DeviceProfile.BALANCED, "Balanced", "Balanced budgets for most devices"),
            Triple(DeviceProfile.PERFORMANCE, "Performance", "Higher budgets on qualified devices"),
        )
        val wide = context.resources.configuration.screenWidthDp >= 600 && context.resources.configuration.fontScale <= 1.3f
        choices.chunked(if (wide) 2 else 1).forEach { group ->
            row("profiles-${group.first().second}", listOf(group.map { selected == it.first }, enabled)) {
                LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    group.forEachIndexed { index, (profile, title, detail) ->
                        addView(ProfileOption(context, title, detail,
                            if (profile == DeviceProfile.PERFORMANCE) R.drawable.ic_bolt else R.drawable.ic_memory,
                            selected == profile, enabled) { action(profile) },
                            LinearLayout.LayoutParams(0, -1, 1f).apply { if(index > 0) marginStart = context.dp(10) })
                    }
                }
            }
        }
    }

    fun logs(tail: String) {
        if (cachedTail != tail) { cachedTail = tail; cachedEntries = LogPresentation.parse(tail) }
        val entries = cachedEntries
        if (entries.isEmpty()) empty("empty-logs", "No entries yet", "Refresh to read the bounded log tail.", R.drawable.ic_logs, Badge("EMPTY"))
        entries.forEachIndexed { index, entry -> row("log-$index", entry) {
            context.column(14).apply {
                background = context.shape(StudioTheme.surface, border, 12)
                val tone = when (entry.category) { "ANDROID" -> Tone.INFO; "STORAGE", "NETWORK" -> Tone.PLANNED; else -> Tone.NEUTRAL }
                line(LinearLayout(context).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    addView(TechnicalValue(context, entry.time, 12f).apply { setTextColor(muted) }, LinearLayout.LayoutParams(0, -2, 1f))
                    addView(StatusChip(context, Badge(entry.category, tone)), LinearLayout.LayoutParams(-2, -2).apply { marginStart = context.dp(12) })
                })
                line(TechnicalValue(context, entry.code.replace('_', ' '), 13f), 10)
                entry.session?.let { line(context.label("Session $it", 11f, muted), 8) }
                entry.value?.let { line(context.label("Value: $it", 12f, muted), 6) }
            }
        } }
    }
    fun logActions(tail: String, enabled: Boolean, refresh: () -> Unit) {
        row("log-actions", listOf(tail, enabled)) {
            LinearLayout(context).apply {
                val compact = context.resources.configuration.screenWidthDp >= 360 && context.resources.configuration.fontScale <= 1.3f
                orientation = if (compact) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
                addView(SecondaryAction(context, "Refresh", enabled, R.drawable.ic_refresh, refresh),
                    LinearLayout.LayoutParams(if (compact) 0 else -1, -2, if (compact) 1f else 0f))
                addView(SecondaryAction(context, "Raw JSON", tail.isNotEmpty(), R.drawable.ic_logs) { showRawLogs(tail) },
                    LinearLayout.LayoutParams(if (compact) 0 else -1, -2, if (compact) 1f else 0f).apply {
                        if (compact) marginStart = context.dp(10) else topMargin = context.dp(10)
                    })
            }
        }
    }
    private fun showRawLogs(tail: String) {
            val raw = tail.take(16 * 1024)
            val rawList = RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                val rawAdapter = LauncherList(); adapter = rawAdapter
                rawAdapter.submitList(raw.lineSequence().filter { it.isNotBlank() }.mapIndexed { i, line ->
                    DisplayRow("raw-$i", line) {
                        TechnicalValue(context, line, 12f).apply {
                            setPadding(context.dp(16), context.dp(6), context.dp(16), context.dp(6))
                            setTextIsSelectable(true)
                        }
                    }
                }.toList())
            }
            MaterialAlertDialogBuilder(context).setTitle("Raw log tail · 16 KiB max").setView(rawList)
                .setPositiveButton("Copy") { _, _ -> context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("StudioDroid log tail", raw)) }
                .setNegativeButton("Close", null).show()
    }
    fun link(title: String, detail: String, url: String) {
        row("link-$title", listOf(title, detail, url)) {
            StatusRow(context, title, detail, R.drawable.ic_link, Badge("OPEN LINK", Tone.INFO)).apply {
                clickSurface { try { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) } catch (_: android.content.ActivityNotFoundException) { Toast.makeText(context, "No browser available", Toast.LENGTH_SHORT).show() } }
                contentDescription = "$title. $detail. Open link"
            }
        }
    }
}
data class ScreenActions(val refresh: () -> Unit, val export: () -> Unit, val profile: (DeviceProfile?) -> Unit)
fun bytes(value: Long?): String = value?.let { when { it >= 1024L*1024*1024 -> String.format(Locale.ROOT, "%.1f GiB", it/(1024.0*1024*1024)); it >= 1024*1024 -> String.format(Locale.ROOT, "%.1f MiB", it/(1024.0*1024)); it >= 1024 -> String.format(Locale.ROOT, "%.1f KiB", it/1024.0); else -> "$it B" } } ?: "Unknown"
fun <T> observation(value: Observation<T>?): String = value?.valueOrNull()?.toString() ?: "Unknown"
fun observedBadge(value: Observation<*>?) = if (value is Observation.Known) Badge("OBSERVED", Tone.INFO) else Badge("UNKNOWN")
fun availability(value: Availability): String = when (value) { Availability.Available -> "Available"; is Availability.Unimplemented -> value.reason; is Availability.Unsupported -> value.reason; is Availability.Unverified -> value.reason }
fun componentBadge(record: ComponentRecord?) = when(record?.state) {
    InstallState.VALIDATED -> Badge("VALIDATED", Tone.GOOD)
    InstallState.INSTALLED -> Badge("INSTALLED", Tone.INFO)
    InstallState.NOT_INSTALLED -> Badge("NOT INSTALLED")
    InstallState.QUARANTINED -> Badge("QUARANTINED", Tone.CAUTION)
    else -> Badge("UNKNOWN")
}
