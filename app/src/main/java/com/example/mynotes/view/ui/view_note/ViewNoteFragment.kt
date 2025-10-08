package com.example.mynotes.view.ui.view_note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mynotes.R
import com.example.mynotes.database.DatabaseInit
import com.example.mynotes.database.repo.NotesRepository
import com.example.mynotes.database.table.Note
import com.example.mynotes.database.viewmodel.NotesViewModel
import com.example.mynotes.databinding.FragmentViewNoteBinding

class ViewNoteFragment : Fragment() {

    private var _binding: FragmentViewNoteBinding? = null
    private val binding get() = _binding!!

    private val args: ViewNoteFragmentArgs by navArgs()
    private lateinit var viewModel: NotesViewModel

    private var currentNote: Note? = null
    private var selectedCategoryId: Int? = null
    private val userId = 1 //để tạm

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

        // Khởi tạo ViewModel
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

        val noteId = args.noteId

        // Nếu là note cũ
        if (noteId != -1) {
            viewModel.observeNoteById(noteId).observe(viewLifecycleOwner) { note ->
                note?.let {
                    currentNote = it
                    binding.etTitle.setText(it.title)
                    binding.etDetail.setText(it.detail)
                }
            }
        }

        // Thiết lập Spinner category
        setupCategorySpinner()

        // Nút lưu
        binding.btnSave.setOnClickListener { saveNote() }

        // Nút xóa
        binding.topLayout.findViewById<ImageView>(R.id.btn_trash_bin)?.setOnClickListener {
            currentNote?.let {
                viewModel.moveNoteToTrash(it)
                Toast.makeText(requireContext(), "Moved to Trash", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCategorySpinner() {
        viewModel.observeCategories(userId).observe(viewLifecycleOwner) { categories ->
            val categoryNames = mutableListOf("All")
            categoryNames.addAll(categories.map { it.name })

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinner.adapter = adapter

            // chọn sẵn
            currentNote?.categoryId?.let { cid ->
                val pos = categories.indexOfFirst { it.id == cid } + 1
                if (pos >= 0) binding.spinner.setSelection(pos)
            }

            // Chọn category
            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedCategoryId = if (position == 0) null else categories[position - 1].id
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

        if (currentNote == null) {
            // Tạo mới
            viewModel.addNote(
                userId = userId,
                categoryId = selectedCategoryId,
                title = title,
                detail = detail
            )
            Toast.makeText(requireContext(), "Note created", Toast.LENGTH_SHORT).show()
        } else {
            // Cập nhật
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
