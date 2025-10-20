package com.example.mynotes.view.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mynotes.database.DatabaseInit
import com.example.mynotes.database.repo.NotesRepository
import com.example.mynotes.database.viewmodel.NotesViewModel
import com.example.mynotes.databinding.FragmentFavoriteBinding
import com.example.mynotes.view.adapter.NoteAdapter

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotesViewModel
    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val db = DatabaseInit.getDatabase(requireContext())
//        val repo = NotesRepository(
//            userDao = db.userDao(),
//            categoryDao = db.categoryDao(),
//            noteDao = db.noteDao()
//        )
//        viewModel = ViewModelProvider(
//            this,
//            NotesViewModel.NotesViewModelFactory(repo)
//        )[NotesViewModel::class.java]
//
//        adapter = NoteAdapter { note ->
//        }
//
//        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
//        binding.recyclerView.adapter = adapter
//
//        viewModel.observeFavoriteNotes(1).observe(viewLifecycleOwner) { notes ->
//            adapter.submitList(notes)
//        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
