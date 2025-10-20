package com.example.mynotes.view.ui.trash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mynotes.databinding.FragmentTrashBinding
import androidx.navigation.fragment.findNavController
import com.example.mynotes.database.DatabaseInit
import com.example.mynotes.database.repo.NotesRepository
import com.example.mynotes.database.viewmodel.NotesViewModel
import com.example.mynotes.view.adapter.TrashAdapter

class TrashFragment : Fragment() {
    private var _binding : FragmentTrashBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: NotesViewModel
    private lateinit var adapter: TrashAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = DatabaseInit.getDatabase(requireContext())
        val repo = NotesRepository(db.userDao(), db.categoryDao(), db.noteDao())
        viewModel = ViewModelProvider(this, NotesViewModel.NotesViewModelFactory(repo))[NotesViewModel::class.java]

        adapter = TrashAdapter(
            onRestore = { note -> viewModel.restoreNote(note) },
            onDeleteForever = { note -> viewModel.deleteNotePermanently(note) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        val userId = 1
        viewModel.observeTrashedNotes(userId).observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
