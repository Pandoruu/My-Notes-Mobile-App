package com.example.mynotes.view.ui.trash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mynotes.databinding.FragmentTrashBinding
import androidx.navigation.fragment.findNavController
import com.example.mynotes.di.AppContainer
import com.example.mynotes.presentation.trash.TrashViewModel
import com.example.mynotes.view.adapter.TrashAdapter

class TrashFragment : Fragment() {
    private var _binding : FragmentTrashBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TrashViewModel
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

        viewModel = ViewModelProvider(
            this,
            TrashViewModel.Factory(
                observeCurrentUserIdUseCase = AppContainer.observeCurrentUserIdUseCase,
                observeTrashedNotesUseCase = AppContainer.observeTrashedNotesUseCase,
                restoreNoteUseCase = AppContainer.restoreNoteUseCase,
                deleteNoteUseCase = AppContainer.deleteNoteUseCase
            )
        )[TrashViewModel::class.java]

        adapter = TrashAdapter(
            onRestore = { note -> viewModel.restoreNote(note) },
            onDeleteForever = { note -> showDeleteForeverConfirm(note) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.observeTrashedNotes().observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun showDeleteForeverConfirm(note: com.example.mynotes.domain.model.Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete forever")
            .setMessage("This note will be permanently deleted. Continue?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteNotePermanently(note)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
