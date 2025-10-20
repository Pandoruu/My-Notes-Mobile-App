package com.example.mynotes.view.ui.category

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.R
import com.example.mynotes.database.DatabaseInit
import com.example.mynotes.database.repo.NotesRepository
import com.example.mynotes.database.table.Category
import com.example.mynotes.database.viewmodel.NotesViewModel
import com.example.mynotes.databinding.FragmentCategoryBinding
import com.example.mynotes.view.adapter.CategoryManagerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotesViewModel
    private lateinit var adapter: CategoryManagerAdapter

    private val currentUserId = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = DatabaseInit.getDatabase(requireContext())
        val repo = NotesRepository(db.userDao(), db.categoryDao(), db.noteDao())
        viewModel = ViewModelProvider(this, NotesViewModel.NotesViewModelFactory(repo))[NotesViewModel::class.java]

        setupRecyclerView()
        setupAddButton()
        observeData()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = CategoryManagerAdapter(
            onEdit = { showEditDialog(it) },
            onDelete = { viewModel.deleteCategory(it) }
        )
        binding.recyclerViewCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewCategories.adapter = adapter

        val touchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {
                adapter.moveItem(vh.bindingAdapterPosition, target.bindingAdapterPosition)
                return true
            }
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {}
        })
        touchHelper.attachToRecyclerView(binding.recyclerViewCategories)
    }

    private fun setupAddButton() {
        binding.btnAddCategory.setOnClickListener { showEditDialog(null) }
    }

    private fun observeData() {
        viewModel.observeCategories(currentUserId).observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun showEditDialog(category: Category?) {
        val input = EditText(requireContext()).apply {
            setText(category?.name ?: "")
            hint = "Tên danh mục"
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (category == null) "Thêm danh mục" else "Đổi tên danh mục")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    if (category == null)
                        viewModel.addCategory(currentUserId, name)
                    else
                        viewModel.updateCategory(category.copy(name = name))
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
