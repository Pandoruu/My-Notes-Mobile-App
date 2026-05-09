package com.example.mynotes.view.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mynotes.di.AppContainer
import com.example.mynotes.domain.model.Note
import com.example.mynotes.presentation.search.SearchViewModel
import com.example.mynotes.databinding.FragmentSearchBinding
import com.example.mynotes.view.adapter.NoteAdapter

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SearchViewModel
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            SearchViewModel.Factory(
                observeCurrentUserIdUseCase = AppContainer.observeCurrentUserIdUseCase,
                searchNotesUseCase = AppContainer.searchNotesUseCase,
                togglePinUseCase = AppContainer.togglePinUseCase,
                toggleFavoriteUseCase = AppContainer.toggleFavoriteUseCase
            )
        )[SearchViewModel::class.java]

        setupRecyclerView()
        setupSearchBar()
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            onClick = { note ->
                // Navigate đến ViewNoteFragment với noteId
                val action = SearchFragmentDirections.actionSearchNavToViewNoteFragment(note.id)
                findNavController().navigate(action)
            },
            onLongClick = { note ->
                // Hiển thị dialog với options
                showNoteOptionsDialog(note)
            }
        )
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = noteAdapter
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

    private fun setupSearchBar() {
        // Xử lý nút search
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        // Xử lý khi nhấn enter trên bàn phím
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // Optional: Tìm kiếm theo thời gian thực khi gõ
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                val query = editable.toString().trim()
                if (query.isEmpty()) {
                    // Xóa kết quả khi không có text
                    noteAdapter.submitList(emptyList())
                } else {
                    // Tìm kiếm theo thời gian thực
                    searchNotes(query)
                }
            }
        })
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            searchNotes(query)
            // Ẩn bàn phím sau khi search
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        }
    }

    private fun searchNotes(query: String) {
        viewModel.search(query).observe(viewLifecycleOwner) { filteredNotes ->
            noteAdapter.submitList(filteredNotes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
