package com.example.mynotes.view.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mynotes.database.repo.NotesRepository
import com.example.mynotes.database.table.Note
import com.example.mynotes.database.viewmodel.NotesViewModel
import com.example.mynotes.databinding.FragmentSearchBinding
import com.example.mynotes.view.adapter.NoteAdapter
import com.example.mynotes.database.*
import java.text.Normalizer

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotesViewModel
    private lateinit var noteAdapter: NoteAdapter
    private var allNotes: List<Note> = emptyList()
    private var userId: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = DatabaseInit.getDatabase(requireContext())
        val repo = NotesRepository(db.userDao(), db.categoryDao(), db.noteDao())
        viewModel = ViewModelProvider(this, NotesViewModel.NotesViewModelFactory(repo))[NotesViewModel::class.java]


//        setupRecyclerView()
//        setupSearchBar()
    }

//    private fun setupRecyclerView() {
//        noteAdapter = NoteAdapter { note ->
//        }
//        binding.recyclerView.apply {
//            layoutManager = GridLayoutManager(requireContext(), 2)
//            adapter = noteAdapter
//        }
//    }
//
//    private fun setupSearchBar() {
//        binding.btnBack.setOnClickListener {
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        }
//
//        binding.etSearch.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//            override fun afterTextChanged(editable: Editable?) {
//                val query = editable.toString().trim()
//                if (query.isEmpty()) {
//                    noteAdapter.submitList(allNotes)
//                } else {
//                    searchNotes(query)
//                }
//            }
//        })
//    }
//
//    private fun searchNotes(query: String) {
//        viewModel.searchNotes(userId, removeAccents(query)).observe(viewLifecycleOwner) { filteredNotes ->
//            noteAdapter.submitList(filteredNotes)
//        }
//    }
//
//    private fun removeAccents(input: String): String {
//        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
//        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
