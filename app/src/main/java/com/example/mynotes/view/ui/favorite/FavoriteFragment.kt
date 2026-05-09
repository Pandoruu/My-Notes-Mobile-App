package com.example.mynotes.view.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mynotes.di.AppContainer
import com.example.mynotes.domain.model.Note
import com.example.mynotes.presentation.favorite.FavoriteViewModel
import com.example.mynotes.databinding.FragmentFavoriteBinding
import com.example.mynotes.view.adapter.NoteAdapter

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FavoriteViewModel
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

        viewModel = ViewModelProvider(
            this,
            FavoriteViewModel.Factory(
                observeCurrentUserIdUseCase = AppContainer.observeCurrentUserIdUseCase,
                observeFavoriteNotesUseCase = AppContainer.observeFavoriteNotesUseCase,
                togglePinUseCase = AppContainer.togglePinUseCase,
                toggleFavoriteUseCase = AppContainer.toggleFavoriteUseCase
            )
        )[FavoriteViewModel::class.java]

        adapter = NoteAdapter(
            onClick = { note ->
                // Navigate to ViewNoteFragment
                val action = FavoriteFragmentDirections.actionFavoriteFragmentToViewNoteFragment(note.id)
                findNavController().navigate(action)
            },
            onLongClick = { note ->
                // Hiển thị dialog options
                showNoteOptionsDialog(note)
            }
        )

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        viewModel.observeFavoriteNotes().observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showNoteOptionsDialog(note: Note) {
        val pinText = if (note.isPinned) "Unpin" else "Pin"
        val favoriteText = if (note.isFavorite) "Remove from Favorite" else "Add to Favorite"

        val options = arrayOf(pinText, favoriteText)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
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
        _binding = null
    }
}
