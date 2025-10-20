package com.example.mynotes.view.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mynotes.R
import com.example.mynotes.database.DatabaseInit
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

        adapter = NoteAdapter { note ->
            val action = NotesFragmentDirections.actionNotesNavToViewNoteFragment(note.id)
            findNavController().navigate(action)
        }
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerView.adapter = adapter

        setupTabs()

        binding.btnAdd.setOnClickListener {
            // -1 để tạo note mới
            val action = NotesFragmentDirections.actionNotesNavToViewNoteFragment(-1)
            findNavController().navigate(action)
        }

        // Mặc định hiển thị tab "All"
        loadAllNotes()

        setUpHamburgerMenu()

    }

    private fun setupTabs() {
        viewModel.observeCategories(currentUserId).observe(viewLifecycleOwner) { categories ->
            binding.tabLayout.removeAllTabs()

            // Tab đầu tiên luôn là "All"
            val allTab = binding.tabLayout.newTab().setText("All")
            binding.tabLayout.addTab(allTab)

            // Thêm các tab khác
            for (cat in categories) {
                if (cat.name != "All") {
                    binding.tabLayout.addTab(binding.tabLayout.newTab().setText(cat.name))
                }
            }

            // Mặc định chọn All
            binding.tabLayout.selectTab(allTab)
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabName = tab?.text.toString()
                currentCategoryName = tabName
                if (tabName == "All") loadAllNotes()
                else loadNotesByCategory(tabName)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


    // Load tất cả notes
    private fun loadAllNotes() {
        viewModel.observeAllNotes(currentUserId).observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }
    }

    // Load note theo Category
    private fun loadNotesByCategory(categoryName: String) {
        viewModel.observeNotesByCategory(currentUserId, categoryName)
            .observe(viewLifecycleOwner) { notes ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
