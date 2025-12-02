package com.example.mynotes.view.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mynotes.R
import com.example.mynotes.database.DatabaseInit
import com.example.mynotes.database.table.Note
import com.example.mynotes.database.repo.NotesRepository
import com.example.mynotes.database.viewmodel.NotesViewModel
import com.example.mynotes.databinding.FragmentNotesBinding
import com.example.mynotes.view.adapter.NoteAdapter
import com.google.android.material.tabs.TabLayout

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotesViewModel
    private lateinit var adapter: NoteAdapter

    private var currentUserId = 1 // User mặc định
    private var currentCategoryName = "All"

    private var currentNotesLiveData: LiveData<List<Note>>? = null

    private val categoriesList = mutableListOf<Pair<Int, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = DatabaseInit.getDatabase(requireContext())
        val repo = NotesRepository(
            userDao = db.userDao(),
            categoryDao = db.categoryDao(),
            noteDao = db.noteDao()
        )

        viewModel = ViewModelProvider(
            this,
            NotesViewModel.NotesViewModelFactory(repo)
        )[NotesViewModel::class.java]

        adapter = NoteAdapter(
            onClick = { note ->
                val action = NotesFragmentDirections.actionNotesNavToViewNoteFragment(note.id)
                findNavController().navigate(action)
            },
            onLongClick = { note ->
                showNoteOptionsDialog(note)
            }
        )
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerView.adapter = adapter

        setupTabs()

        binding.btnAdd.setOnClickListener {
            // -1 để tạo note mới
            val action = NotesFragmentDirections.actionNotesNavToViewNoteFragment(-1)
            findNavController().navigate(action)
        }

        setUpHamburgerMenu()
    }

    private fun setupTabs() {
        viewModel.observeCategories(currentUserId).observe(viewLifecycleOwner) { categories ->
            binding.tabLayout.removeAllTabs()
            categoriesList.clear()

            // Tab đầu tiên luôn là "All" với id = 0
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All"))
            categoriesList.add(0 to "All")

            for (cat in categories) {
                if (cat.name != "All") {
                    binding.tabLayout.addTab(binding.tabLayout.newTab().setText(cat.name))
                    categoriesList.add(cat.id to cat.name)
                }
            }

            // Chọn tab All mặc định (position 0) và load notes tương ứng
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
            currentCategoryName = "All"
            loadAllNotes()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                val (catId, catName) = categoriesList.getOrNull(position) ?: (0 to "All")
                currentCategoryName = catName
                if (catId == 0) {
                    loadAllNotes()
                } else {
                    loadNotesByCategory(catId)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // Load tất cả notes (loại bỏ observer cũ trước khi observe LiveData mới)
    private fun loadAllNotes() {
        currentNotesLiveData?.removeObservers(viewLifecycleOwner)
        currentNotesLiveData = viewModel.observeAllNotes(currentUserId)
        currentNotesLiveData?.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }
    }

    // Load note theo categoryId (loại bỏ observer cũ trước khi observe LiveData mới)
    private fun loadNotesByCategory(categoryId: Int) {
        currentNotesLiveData?.removeObservers(viewLifecycleOwner)
        currentNotesLiveData = viewModel.observeNotesByCategory(currentUserId, categoryId)
        currentNotesLiveData?.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }
    }

    private fun setUpHamburgerMenu() {
        binding.btnHamburger.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.hamburger_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_favorite -> {
                        findNavController().navigate(R.id.action_notesNav_to_favoriteFragment)
                        true
                    }
                    R.id.menu_trash -> {
                        findNavController().navigate(R.id.action_notesNav_to_trashFragment)
                        true
                    }
                    R.id.menu_category -> {
                        findNavController().navigate(R.id.action_notesNav_to_categoryFragment)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun showNoteOptionsDialog(note: Note) {
        val pinText = if (note.isPinned) "Unpin" else "Pin"
        val favoriteText = if (note.isFavorite) "Remove from Favorite" else "Add to Favorite"

        val options = arrayOf(pinText, favoriteText)

        AlertDialog.Builder(requireContext())
            .setTitle("Note Options")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Toggle Pin
                        viewModel.togglePin(note)
                    }
                    1 -> {
                        // Toggle Favorite
                        viewModel.toggleFavorite(note)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Gỡ observer nếu còn
        currentNotesLiveData?.removeObservers(viewLifecycleOwner)
        _binding = null
    }
}