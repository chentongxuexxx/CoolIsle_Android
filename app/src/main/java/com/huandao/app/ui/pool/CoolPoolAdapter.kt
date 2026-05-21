package com.huandao.app.ui.pool

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.huandao.app.databinding.ItemCoolPoolCardBinding
import com.huandao.app.domain.model.CoolPoolItem
import com.huandao.app.ui.components.CoolPoolCardView

/**
 * 冷静池 RecyclerView Adapter。
 * 使用 ListAdapter + DiffUtil 实现高效增量更新。
 *
 * @param onItemClick 卡片点击回调
 */
class CoolPoolAdapter(
    private val onItemClick: (CoolPoolItem) -> Unit,
) : ListAdapter<CoolPoolItem, CoolPoolAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCoolPoolCardBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        CoolPoolCardView.bind(holder.binding, item) {
            onItemClick(item)
        }
    }

    class ViewHolder(val binding: ItemCoolPoolCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    private class DiffCallback : DiffUtil.ItemCallback<CoolPoolItem>() {
        override fun areItemsTheSame(oldItem: CoolPoolItem, newItem: CoolPoolItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CoolPoolItem, newItem: CoolPoolItem): Boolean {
            return oldItem == newItem
        }
    }
}
