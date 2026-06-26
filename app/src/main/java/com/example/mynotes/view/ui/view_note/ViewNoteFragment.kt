package com.example.mynotes.view.ui.view_note

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentViewNoteBinding
import com.example.mynotes.di.AppContainer
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.model.NoteBlock
import com.example.mynotes.domain.model.TextAlignment
import com.example.mynotes.presentation.viewnote.ViewNoteViewModel
import com.example.mynotes.view.adapter.BlockEditorCallback
import com.example.mynotes.view.adapter.NoteBlockAdapter
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

class ViewNoteFragment : Fragment(), BlockEditorCallback {

    private var _binding: FragmentViewNoteBinding? = null
    private val binding get() = _binding!!

    private val args: ViewNoteFragmentArgs by navArgs()
    private lateinit var viewModel: ViewNoteViewModel

    private var currentNote: Note? = null
    private var selectedCategoryId: Int? = null

    // Block list duoc chia se giua Fragment va Adapter
    private val blocks: MutableList<NoteBlock> = mutableListOf()
    private lateinit var adapter: NoteBlockAdapter

    // Formatting: theo doi EditText dang focus
    private var focusedEditText: AppCompatEditText? = null
    private var focusedBlockIndex: Int = -1

    // Audio recording
    private var mediaRecorder: MediaRecorder? = null
    private var audioOutputFile: String? = null
    private var recordingStartTime: Long = 0L
    private val recordingHandler = Handler(Looper.getMainLooper())
    private val recordingTimerRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - recordingStartTime
            val mins = TimeUnit.MILLISECONDS.toMinutes(elapsed)
            val secs = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60
            binding.tvRecordingTime.text = "Dang ghi am... %02d:%02d".format(mins, secs)
            recordingHandler.postDelayed(this, 500)
        }
    }

    // Permission launchers
    private val requestAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startAudioRecording()
        else Toast.makeText(requireContext(), "Can quyen ghi am de su dung tinh nang nay", Toast.LENGTH_SHORT).show()
    }

    private val requestImagePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pickImageLauncher.launch("image/*")
        else Toast.makeText(requireContext(), "Can quyen truy cap anh", Toast.LENGTH_SHORT).show()
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImageSelected(it) }
    }

    // ─────────────────────────────────────────────────────────

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        setupRecyclerView()
        setupToolbar()
        setupFormattingToolbar()
        setupTopBar()
        setupCategorySpinner()
        setupKeyboardInsets()

        val noteId = args.noteId
        binding.btnTrashBin.isVisible = false

        if (noteId != -1) {
            viewModel.observeNoteById(noteId).observe(viewLifecycleOwner) { note ->
                note?.let { loadNote(it) }
            }
        } else {
            if (blocks.isEmpty()) {
                blocks.add(NoteBlock.TextBlock())
                adapter.notifyItemInserted(0)
            }
        }
    }

    // Keyboard insets
    private fun setupKeyboardInsets() {
        // Bước 1: Căn chỉnh ban đầu theo navigation bar (edge-to-edge)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val ime   = insets.getInsets(WindowInsetsCompat.Type.ime())
            // Khi bàn phím đang mở thì dùng ime, ngược lại dùng navBar
            val bottomOffset = if (ime.bottom > 0) ime.bottom else navBar.bottom
            val translation = -bottomOffset.toFloat()
            binding.bottomToolbar.translationY = translation
            binding.formattingToolbar.translationY = translation
            insets
        }
        ViewCompat.setWindowInsetsAnimationCallback(
            binding.root,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val imeBottom  = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    val navBottom  = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                    val bottomOffset = if (imeBottom > 0) imeBottom else navBottom
                    binding.bottomToolbar.translationY = -bottomOffset.toFloat()
                    return insets
                }
            }
        )
    }

    // RecyclerView
    private fun setupRecyclerView() {
        adapter = NoteBlockAdapter(blocks, this)
        binding.rvBlocks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBlocks.adapter = adapter
        binding.rvBlocks.itemAnimator = null

        binding.rvBlocks.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val lastChild = binding.rvBlocks.getChildAt(binding.rvBlocks.childCount - 1)
                if (lastChild != null && event.y > lastChild.bottom) {
                    val lastBlock = blocks.lastOrNull()
                    if (lastBlock !is NoteBlock.TextBlock || lastBlock.text.isNotEmpty()) {
                        val insertAt = blocks.size
                        blocks.add(NoteBlock.TextBlock())
                        adapter.notifyItemInserted(insertAt)
                        adapter.pendingFocusIndex = insertAt
                        binding.rvBlocks.scrollToPosition(insertAt)
                        binding.rvBlocks.post { adapter.notifyItemChanged(insertAt) }
                    } else {
                        adapter.pendingFocusIndex = blocks.size - 1
                        adapter.notifyItemChanged(blocks.size - 1)
                    }
                    v.performClick()
                }
            }
            false
        }
    }

    // Toolbar actions (mic, image, checkbox, toggle formatting)
    private fun setupToolbar() {
        binding.btnAddAudio.setOnClickListener { requestAudioOrRecord() }
        binding.btnStopRecording.setOnClickListener { stopAudioRecording() }
        binding.btnAddImage.setOnClickListener { requestImagePermissionAndPick() }
        binding.btnAddCheckbox.setOnClickListener {
            val insertAt = blocks.size
            blocks.add(NoteBlock.CheckboxBlock())
            adapter.notifyItemInserted(insertAt)
            adapter.pendingFocusIndex = insertAt
            binding.rvBlocks.scrollToPosition(insertAt)
            binding.rvBlocks.post { adapter.notifyItemChanged(insertAt) }
        }
        // Chuyển sang formatting toolbar
        binding.btnShowFormatting.setOnClickListener {
            binding.bottomToolbar.visibility = View.GONE
            binding.formattingToolbar.visibility = View.VISIBLE
        }
    }

    // Formatting toolbar
    private fun setupFormattingToolbar() {
        // Quay lại media toolbar
        binding.btnFmtBack.setOnClickListener {
            binding.formattingToolbar.visibility = View.GONE
            binding.bottomToolbar.visibility = View.VISIBLE
        }
        binding.btnFmtBold.setOnClickListener { applySpan(StyleSpan(Typeface.BOLD)) }
        binding.btnFmtItalic.setOnClickListener { applySpan(StyleSpan(Typeface.ITALIC)) }
        binding.btnFmtUnderline.setOnClickListener { applySpan(UnderlineSpan()) }
        binding.btnFmtBullet.setOnClickListener { applyBulletList() }
        binding.btnFmtNumbered.setOnClickListener { applyNumberedList() }
        binding.btnFmtAlignLeft.setOnClickListener { applyAlignment(TextAlignment.LEFT) }
        binding.btnFmtAlignCenter.setOnClickListener { applyAlignment(TextAlignment.CENTER) }
        binding.btnFmtAlignRight.setOnClickListener { applyAlignment(TextAlignment.RIGHT) }
    }

    /** Apply a CharacterStyle span to the selected text in the focused EditText */
    private fun applySpan(span: Any) {
        val et = focusedEditText ?: return
        val start = et.selectionStart.coerceAtLeast(0)
        val end   = et.selectionEnd.coerceAtLeast(0)
        if (start == end) return  // Khong co text duoc chon
        val ssb = SpannableStringBuilder(et.text ?: "")
        // Toggle: neu da co span nay thi xoa, chua co thi them
        val existing = ssb.getSpans(start, end, span.javaClass)
        if (existing.isNotEmpty()) {
            existing.forEach { ssb.removeSpan(it) }
        } else {
            ssb.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        et.setText(ssb)
        et.setSelection(start, end)
    }

    /** Chuyen doan van ban thanh bullet list */
    private fun applyBulletList() {
        val et = focusedEditText ?: return
        val ssb = SpannableStringBuilder(et.text ?: "")
        val lines = ssb.toString().split("\n")
        val newSsb = SpannableStringBuilder()
        lines.forEachIndexed { i, line ->
            val start = newSsb.length
            newSsb.append(line)
            val end = newSsb.length
            newSsb.setSpan(
                BulletSpan(16),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (i < lines.size - 1) newSsb.append("\n")
        }
        et.setText(newSsb)
        et.setSelection(newSsb.length)
    }

    /** Chuyen doan van ban thanh numbered list */
    private fun applyNumberedList() {
        val et = focusedEditText ?: return
        val text = et.text.toString()
        val lines = text.split("\n")
        val newSsb = SpannableStringBuilder()
        lines.forEachIndexed { i, line ->
            val start = newSsb.length
            newSsb.append("${i + 1}. $line")
            val end = newSsb.length
            newSsb.setSpan(
                LeadingMarginSpan.Standard(0, 0),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (i < lines.size - 1) newSsb.append("\n")
        }
        et.setText(newSsb)
        et.setSelection(newSsb.length)
    }

    /** Ap dung can chinh cho TextBlock dang focus */
    private fun applyAlignment(alignment: TextAlignment) {
        val et = focusedEditText ?: return
        val idx = focusedBlockIndex
        if (idx < 0 || idx >= blocks.size) return

        val gravity = when (alignment) {
            TextAlignment.CENTER -> Gravity.CENTER_HORIZONTAL or Gravity.TOP
            TextAlignment.RIGHT  -> Gravity.END or Gravity.TOP
            else                 -> Gravity.START or Gravity.TOP
        }
        et.gravity = gravity

        val block = blocks[idx]
        if (block is NoteBlock.TextBlock) {
            blocks[idx] = block.copy(alignment = alignment)
        }

        // Highlight nut can chinh dang active
        updateAlignmentButtonState(alignment)
    }

    /** Cap nhat visual state cua 3 nut can chinh */
    private fun updateAlignmentButtonState(active: TextAlignment) {
        val alpha = 0.4f
        binding.btnFmtAlignLeft.alpha   = if (active == TextAlignment.LEFT)   1f else alpha
        binding.btnFmtAlignCenter.alpha = if (active == TextAlignment.CENTER) 1f else alpha
        binding.btnFmtAlignRight.alpha  = if (active == TextAlignment.RIGHT)  1f else alpha
    }

    // BlockEditorCallback

    override fun onTextChanged(index: Int, text: String) {}

    override fun onEnterPressed(index: Int) {
        val insertAt = index + 1
        blocks.add(insertAt, NoteBlock.TextBlock())
        adapter.notifyItemInserted(insertAt)
        adapter.pendingFocusIndex = insertAt
        binding.rvBlocks.scrollToPosition(insertAt)
        binding.rvBlocks.post { adapter.notifyItemChanged(insertAt) }
    }

    override fun onBackspaceAtStart(index: Int, isEmpty: Boolean) {
        if (index == 0) return
        val previousBlock = blocks[index - 1]
        val currentBlock  = blocks[index]
        when {
            isEmpty -> {
                blocks.removeAt(index)
                adapter.notifyItemRemoved(index)
                adapter.pendingFocusIndex = index - 1
                binding.rvBlocks.post { adapter.notifyItemChanged(index - 1) }
            }
            previousBlock is NoteBlock.ImageBlock
                || previousBlock is NoteBlock.AudioBlock
                || previousBlock is NoteBlock.CheckboxBlock -> {
                blocks.removeAt(index - 1)
                adapter.notifyItemRemoved(index - 1)
                adapter.pendingFocusIndex = index - 1
                binding.rvBlocks.post { adapter.notifyItemChanged(index - 1) }
            }
            currentBlock is NoteBlock.TextBlock && previousBlock is NoteBlock.TextBlock -> {
                val mergedText = previousBlock.text + currentBlock.text
                blocks[index - 1] = previousBlock.copy(text = mergedText)
                blocks.removeAt(index)
                adapter.notifyItemRemoved(index)
                adapter.pendingFocusIndex = index - 1
                binding.rvBlocks.post { adapter.notifyItemChanged(index - 1) }
            }
        }
    }

    override fun onCheckboxToggled(index: Int, isChecked: Boolean) {}

    override fun onDeleteBlock(index: Int) {
        if (index < 0 || index >= blocks.size) return
        blocks.removeAt(index)
        adapter.notifyItemRemoved(index)
        if (blocks.isEmpty()) {
            blocks.add(NoteBlock.TextBlock())
            adapter.notifyItemInserted(0)
        }
    }

    /** TextBlock nhan focus: ghi nho EditText dang active */
    override fun onTextBlockFocused(index: Int, editText: AppCompatEditText) {
        focusedEditText = editText
        focusedBlockIndex = index
        // Cap nhat trang thai nut can chinh theo block hien tai
        val block = blocks.getOrNull(index)
        if (block is NoteBlock.TextBlock) {
            updateAlignmentButtonState(block.alignment)
        }
    }

    /** Khong con TextBlock nao focus */
    override fun onTextBlockUnfocused() {
        // Sync text ve block truoc khi clear focus
        syncFocusedTextToBlock()
        focusedEditText = null
        focusedBlockIndex = -1
    }

    // Top bar
    private fun setupTopBar() {
        binding.btnSave.setOnClickListener { saveNote() }
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnTrashBin.setOnClickListener {
            currentNote?.let { showMoveToTrashConfirm(it) }
        }
    }

    // Load existing note
    private fun loadNote(note: Note) {
        currentNote = note
        binding.etTitle.setText(note.title)
        binding.btnTrashBin.isVisible = true

        blocks.clear()
        if (note.contentBlocks.isNotEmpty()) {
            blocks.addAll(note.contentBlocks)
        } else if (!note.detail.isNullOrEmpty()) {
            blocks.add(NoteBlock.TextBlock(text = note.detail))
        } else {
            blocks.add(NoteBlock.TextBlock())
        }
        adapter.notifyDataSetChanged()
    }

    // Image handling
    private fun requestImagePermissionAndPick() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                pickImageLauncher.launch("image/*")
            }
            else -> requestImagePermission.launch(permission)
        }
    }

    private fun handleImageSelected(uri: Uri) {
        try {
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val destFile = File(requireContext().filesDir, fileName)
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
            val insertAt = blocks.size
            blocks.add(NoteBlock.ImageBlock(filePath = destFile.absolutePath))
            adapter.notifyItemInserted(insertAt)
            val textAt = insertAt + 1
            blocks.add(NoteBlock.TextBlock())
            adapter.notifyItemInserted(textAt)
            adapter.pendingFocusIndex = textAt
            binding.rvBlocks.scrollToPosition(textAt)
            binding.rvBlocks.post { adapter.notifyItemChanged(textAt) }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Khong the chen anh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio recording
    private fun requestAudioOrRecord() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED -> startAudioRecording()
            else -> requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startAudioRecording() {
        val fileName = "audio_${UUID.randomUUID()}.3gp"
        val outputFile = File(requireContext().filesDir, fileName)
        audioOutputFile = outputFile.absolutePath
        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(requireContext())
            } else {
                @Suppress("DEPRECATION") MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(outputFile.absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
            recordingStartTime = System.currentTimeMillis()
            binding.recordingOverlay.visibility = View.VISIBLE
            recordingHandler.post(recordingTimerRunnable)
            startRecordingDotAnimation()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Loi khi bat dau ghi am: ${e.message}", Toast.LENGTH_SHORT).show()
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    private fun stopAudioRecording() {
        recordingHandler.removeCallbacks(recordingTimerRunnable)
        binding.ivRecordingDot.clearAnimation()
        binding.recordingOverlay.visibility = View.GONE
        val durationMs = System.currentTimeMillis() - recordingStartTime
        try {
            mediaRecorder?.apply { stop(); release() }
            mediaRecorder = null
            val filePath = audioOutputFile ?: return
            val insertAt = blocks.size
            blocks.add(NoteBlock.AudioBlock(filePath = filePath, durationMs = durationMs))
            adapter.notifyItemInserted(insertAt)
            val textAt = insertAt + 1
            blocks.add(NoteBlock.TextBlock())
            adapter.notifyItemInserted(textAt)
            adapter.pendingFocusIndex = textAt
            binding.rvBlocks.scrollToPosition(textAt)
            binding.rvBlocks.post { adapter.notifyItemChanged(textAt) }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Loi khi dung ghi am: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRecordingDotAnimation() {
        val animator = ObjectAnimator.ofFloat(binding.ivRecordingDot, "alpha", 1f, 0f).apply {
            duration = 600
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        animator.start()
        binding.ivRecordingDot.tag = animator
    }

    /**
     * Dong bo text hien tai tu EditText dang focus ve blocks list.
     * Goi truoc khi luu de dam bao du lieu moi nhat duoc ghi vao block.
     */
    private fun syncFocusedTextToBlock() {
        val et = focusedEditText ?: return
        val idx = focusedBlockIndex
        if (idx < 0 || idx >= blocks.size) return
        val block = blocks[idx]
        if (block is NoteBlock.TextBlock) {
            // Lay plain text (Spannable chua full formatted text o UI)
            blocks[idx] = block.copy(text = et.text.toString())
        }
    }

    // Save note
    private fun saveNote() {
        // Sync text cua EditText dang focus truoc khi luu tranh mat du lieu
        syncFocusedTextToBlock()

        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Tieu de khong duoc de trong", Toast.LENGTH_SHORT).show()
            return
        }
        val cleanedBlocks = blocks.dropLastWhile {
            it is NoteBlock.TextBlock && it.text.isBlank()
        }.ifEmpty { listOf(NoteBlock.TextBlock()) }

        val plainText = cleanedBlocks.joinToString("\n") { block ->
            when (block) {
                is NoteBlock.TextBlock     -> block.text
                is NoteBlock.CheckboxBlock -> "[${if (block.isChecked) "x" else " "}] ${block.text}"
                is NoteBlock.ImageBlock    -> "[Anh]"
                is NoteBlock.AudioBlock    -> "[Audio]"
            }
        }.trim()

        if (currentNote == null) {
            viewModel.addNote(
                categoryId = selectedCategoryId,
                title = title,
                detail = plainText,
                contentBlocks = cleanedBlocks
            )
            Toast.makeText(requireContext(), "Da tao ghi chu", Toast.LENGTH_SHORT).show()
        } else {
            val updated = currentNote!!.copy(
                title = title,
                detail = plainText,
                categoryId = selectedCategoryId,
                contentBlocks = cleanedBlocks,
                updatedAt = Date()
            )
            viewModel.updateNote(updated)
            Toast.makeText(requireContext(), "Da cap nhat ghi chu", Toast.LENGTH_SHORT).show()
        }
        findNavController().popBackStack()
    }

    // Category spinner
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
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedCategoryId = categories.getOrNull(position)?.id
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    // Trash dialog
    private fun showMoveToTrashConfirm(note: Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xoa ghi chu")
            .setMessage("Chuyen ghi chu nay vao Thung rac?")
            .setPositiveButton("Chuyen") { dialog, _ ->
                viewModel.moveNoteToTrash(note)
                Toast.makeText(requireContext(), "Da chuyen vao Thung rac", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                dialog.dismiss()
            }
            .setNegativeButton("Huy") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Lifecycle
    override fun onDestroyView() {
        super.onDestroyView()
        if (mediaRecorder != null) {
            try { mediaRecorder?.stop(); mediaRecorder?.release() } catch (_: Exception) {}
            mediaRecorder = null
        }
        recordingHandler.removeCallbacksAndMessages(null)
        _binding = null
    }
}
