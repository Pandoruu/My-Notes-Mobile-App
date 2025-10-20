package com.example.mynotes.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.R
import com.example.mynotes.database.table.Note
import com.example.mynotes.databinding.ItemNoteBinding

class TrashAdapter(
    private val onRestore: (Note) -> Unit,
    private val onDeleteForever: (Note) -> Unit
) : ListAdapter<Note, TrashAdapter.NoteViewHolder>(DiffCallback()) {

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.title.text = note.title
            binding.detail.text = note.detail ?: ""

            binding.root.setOnClickListener { view ->
                showPopupMenu(view, note)
            }
        }

        private fun showPopupMenu(view: View, note: Note) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.trash_menu, popup.menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_restore -> onRestore(note)
                    R.id.action_delete -> onDeleteForever(note)
                }
                true
            }
            popup.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
    }
}
