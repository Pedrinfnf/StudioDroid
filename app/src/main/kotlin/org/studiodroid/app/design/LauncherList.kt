// SPDX-License-Identifier: GPL-3.0-only
package org.studiodroid.app.design

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.studiodroid.app.design.StudioTheme.dp

/** Immutable display descriptions; only visible rows create Android views. */
data class DisplayRow(val key: String, val content: Any, val create: () -> View)
class LauncherList : ListAdapter<DisplayRow, LauncherList.Holder>(object : DiffUtil.ItemCallback<DisplayRow>() {
    override fun areItemsTheSame(old: DisplayRow, new: DisplayRow) = old.key == new.key
    override fun areContentsTheSame(old: DisplayRow, new: DisplayRow) = old.content == new.content
}) {
    class Holder(val host: FrameLayout) : RecyclerView.ViewHolder(host)
    override fun onCreateViewHolder(parent: ViewGroup, type: Int) = Holder(FrameLayout(parent.context).apply {
        layoutParams = RecyclerView.LayoutParams(-1, -2)
        setPadding(0, 0, 0, context.dp(10))
    })
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.host.removeAllViews(); holder.host.addView(getItem(position).create(), FrameLayout.LayoutParams(-1, -2))
    }
    override fun onViewRecycled(holder: Holder) { holder.host.removeAllViews() }
}
