package com.example.mynotes.view.ui.view_note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mynotes.di.AppContainer
import com.example.mynotes.domain.model.Note
import com.example.mynotes.presentation.viewnote.ViewNoteViewModel
import com.example.mynotes.databinding.FragmentViewNoteBinding

class ViewNoteFragment : Fragment() {

    private var _binding: FragmentViewNoteBinding? = null
    private val binding get() = _binding!!

    private val args: ViewNoteFragmentArgs by navArgs()
    private lateinit var viewModel: ViewNoteViewModel

    private var currentNote: Note? = null
    private var selectedCategoryId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewNoteViewModel.Factory(
                observeCurrentUserIdUseCase = AppContainer.observeCurrentUserIdUseCase,
                observeNoteByIdUseCase = AppContainer.observeNoteByIdUseCase,
                observeCategoriesUseCase = AppContainer.observeCategoriesUseCase,
                addNoteUseCase = AppContainer.addNoteUseCase,
                updateNoteUseCase = AppContainer.updateNoteUseCase,
                moveNoteToTrashUseCase = AppContainer.moveNoteToTrashUseCase
            )
        )[ViewNoteViewModel::class.java]

        val noteId = args.noteId

        binding.btnTrashBin.isVisible = false

        // Nếu là note cũ
        if (noteId != -1) {
            viewModel.observeNoteById(noteId).observe(viewLifecycleOwner) { note ->
                note?.let {
                    currentNote = it
                    binding.etTitle.setText(it.title)
                    binding.etDetail.setText(it.detail)
                    binding.btnTrashBin.isVisible = true
                }
            }
        }

        setupCategorySpinner()

        binding.btnSave.setOnClickListener {
            saveNote()
        }

        binding.btnTrashBin.setOnClickListener {
            currentNote?.let { note ->
                showMoveToTrashConfirm(note)
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCategorySpinner() {
        viewModel.observeCategories().observe(viewLifecycleOwner) { categories ->
            val categoryNames = categories.map { it.name }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinner.adapter = adapter

            currentNote?.categoryId?.let { cid ->
                val idx = categories.indexOfFirst { it.id == cid }
                if (idx != -1) binding.spinner.setSelection(idx)
            }

            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // position trùng với index trong categories
                    selectedCategoryId = categories.getOrNull(position)?.id
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val detail = binding.etDetail.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentNote == null) { // Tạo mới
            viewModel.addNote(
                categoryId = selectedCategoryId,
                title = title,
                detail = detail
            )
            Toast.makeText(requireContext(), "Note created", Toast.LENGTH_SHORT).show()
        } else { // Cập nhật
            val updated = currentNote!!.copy(
                title = title,
                detail = detail,
                categoryId = selectedCategoryId
            )
            viewModel.updateNote(updated)
            Toast.makeText(requireContext(), "Note updated", Toast.LENGTH_SHORT).show()
        }

        findNavController().popBackStack()
    }

    private fun showMoveToTrashConfirm(note: Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete note")
            .setMessage("Move this note to Trash?")
            .setPositiveButton("Move") { dialog, _ ->
                viewModel.moveNoteToTrash(note)
                Toast.makeText(requireContext(), "Moved to Trash", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
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
