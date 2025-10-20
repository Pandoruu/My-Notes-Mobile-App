package com.example.mynotes.view.adapter

import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.R
import com.example.mynotes.database.table.Category
import com.example.mynotes.databinding.ItemCategoryBinding

class CategoryManagerAdapter(
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : ListAdapter<Category, CategoryManagerAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.txtName.text = category.name

            binding.btnMore.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                MenuInflater(view.context).inflate(R.menu.category_menu, popup.menu)
                popup.setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.action_rename -> onEdit(category)
                        R.id.action_delete -> onDelete(category)
                    }
                    true
                }
                popup.show()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    fun moveItem(from: Int, to: Int) {
        val currentList = currentList.toMutableList()
        val item = currentList.removeAt(from)
        currentList.add(to, item)
        submitList(currentList)
    }
    class DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem == newItem
    }
}
