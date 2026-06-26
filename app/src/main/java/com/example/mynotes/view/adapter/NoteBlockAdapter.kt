package com.example.mynotes.view.adapter

import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mynotes.R
import com.example.mynotes.domain.model.NoteBlock
import com.example.mynotes.domain.model.TextAlignment
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Callback interface để ViewNoteFragment nhận các sự kiện từ adapter.
 */
interface BlockEditorCallback {
    /** Nội dung text của block tại [index] đã thay đổi */
    fun onTextChanged(index: Int, text: String)
    /** Người dùng nhấn Enter trong block tại [index] – tạo block mới ngay sau đó */
    fun onEnterPressed(index: Int)
    /**
     * Người dùng nhấn Backspace khi con trỏ ở đầu block tại [index].
     * [isEmpty]: true nếu EditText đang rỗng.
     */
    fun onBackspaceAtStart(index: Int, isEmpty: Boolean)
    /** Checkbox block tại [index] được toggle */
    fun onCheckboxToggled(index: Int, isChecked: Boolean)
    /** Yêu cầu xóa block tại [index] */
    fun onDeleteBlock(index: Int)
    /** TextBlock tại [index] nhận focus – Fragment hiện formatting toolbar */
    fun onTextBlockFocused(index: Int, editText: AppCompatEditText)
    /** Không còn TextBlock nào đang focus – Fragment ẩn formatting toolbar */
    fun onTextBlockUnfocused()
}

/**
 * Multi-type RecyclerView adapter cho rich content editor.
 *
 * Quản lý 4 view type: TEXT, CHECKBOX, IMAGE, AUDIO.
 * Không dùng ListAdapter/DiffUtil để tránh mất focus EditText khi list thay đổi nhỏ.
 * Thay vào đó, Fragment gọi notifyItemInserted / notifyItemRemoved trực tiếp.
 */
class NoteBlockAdapter(
    private val blocks: MutableList<NoteBlock>,
    private val callback: BlockEditorCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_CHECKBOX = 1
        private const val TYPE_IMAGE = 2
        private const val TYPE_AUDIO = 3
    }

    // Map adapterPosition → index of EditText cursor request after structural change
    var pendingFocusIndex: Int = -1

    override fun getItemViewType(position: Int): Int = when (blocks[position]) {
        is NoteBlock.TextBlock     -> TYPE_TEXT
        is NoteBlock.CheckboxBlock -> TYPE_CHECKBOX
        is NoteBlock.ImageBlock    -> TYPE_IMAGE
        is NoteBlock.AudioBlock    -> TYPE_AUDIO
    }

    override fun getItemCount(): Int = blocks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXT     -> TextBlockVH(inflater.inflate(R.layout.item_block_text, parent, false))
            TYPE_CHECKBOX -> CheckboxBlockVH(inflater.inflate(R.layout.item_block_checkbox, parent, false))
            TYPE_IMAGE    -> ImageBlockVH(inflater.inflate(R.layout.item_block_image, parent, false))
            TYPE_AUDIO    -> AudioBlockVH(inflater.inflate(R.layout.item_block_audio, parent, false))
            else          -> TextBlockVH(inflater.inflate(R.layout.item_block_text, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextBlockVH     -> holder.bind(blocks[position] as NoteBlock.TextBlock, position)
            is CheckboxBlockVH -> holder.bind(blocks[position] as NoteBlock.CheckboxBlock, position)
            is ImageBlockVH    -> holder.bind(blocks[position] as NoteBlock.ImageBlock, position)
            is AudioBlockVH    -> holder.bind(blocks[position] as NoteBlock.AudioBlock, position)
        }
        // Request focus nếu cần
        if (pendingFocusIndex == position) {
            pendingFocusIndex = -1
            when (holder) {
                is TextBlockVH     -> holder.requestFocus()
                is CheckboxBlockVH -> holder.requestFocus()
                else               -> {}
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // TEXT BLOCK
    // ─────────────────────────────────────────────────────────
    inner class TextBlockVH(view: View) : RecyclerView.ViewHolder(view) {
        val et: AppCompatEditText = view.findViewById(R.id.et_text_block)
        private var textWatcher: TextWatcher? = null
        private var currentPos: Int = -1

        init {
            setupKeyListener()
            setupFocusListener()
        }

        @Suppress("DEPRECATION")
        fun bind(block: NoteBlock.TextBlock, position: Int) {
            currentPos = position
            textWatcher?.let { et.removeTextChangedListener(it) }

            // Load HTML nếu có, ngược lại load plain text (backward compat)
            if (block.htmlText.isNotEmpty()) {
                val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    Html.fromHtml(block.htmlText, Html.FROM_HTML_MODE_COMPACT)
                else
                    Html.fromHtml(block.htmlText)
                et.setText(spanned)
            } else {
                et.setText(block.text)
            }
            et.setSelection(et.text?.length ?: 0)

            // Apply alignment
            et.gravity = when (block.alignment) {
                TextAlignment.CENTER -> Gravity.CENTER_HORIZONTAL or Gravity.TOP
                TextAlignment.RIGHT  -> Gravity.END or Gravity.TOP
                else                 -> Gravity.START or Gravity.TOP
            }

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                @Suppress("DEPRECATION")
                override fun afterTextChanged(s: Editable?) {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_ID.toInt() && pos != -1) {
                        val htmlStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            Html.toHtml(s, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                        else
                            Html.toHtml(s)
                        val plainText = s.toString()
                        blocks[pos] = (blocks[pos] as NoteBlock.TextBlock).copy(
                            text = plainText,
                            htmlText = htmlStr
                        )
                        callback.onTextChanged(pos, plainText)
                    }
                }
            }
            et.addTextChangedListener(textWatcher)
        }

        fun requestFocus() {
            et.post {
                et.requestFocus()
                et.setSelection(et.text?.length ?: 0)
            }
        }

        private fun setupFocusListener() {
            et.setOnFocusChangeListener { _, hasFocus ->
                val pos = bindingAdapterPosition
                if (pos == -1) return@setOnFocusChangeListener
                if (hasFocus) {
                    callback.onTextBlockFocused(pos, et)
                } else {
                    callback.onTextBlockUnfocused()
                }
            }
        }

        private fun setupKeyListener() {
            et.setOnKeyListener { _, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_ID.toInt() || pos == -1) return@setOnKeyListener false

                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        callback.onEnterPressed(pos)
                        true
                    }
                    KeyEvent.KEYCODE_DEL -> {
                        if (et.selectionStart == 0) {
                            callback.onBackspaceAtStart(pos, et.text.isNullOrEmpty())
                            true
                        } else false
                    }
                    else -> false
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // CHECKBOX BLOCK
    // ─────────────────────────────────────────────────────────
    inner class CheckboxBlockVH(view: View) : RecyclerView.ViewHolder(view) {
        private val cb: CheckBox = view.findViewById(R.id.cb_checkbox_block)
        private val et: AppCompatEditText = view.findViewById(R.id.et_checkbox_text)
        private var textWatcher: TextWatcher? = null

        init {
            setupKeyListener()
        }

        fun bind(block: NoteBlock.CheckboxBlock, position: Int) {
            textWatcher?.let { et.removeTextChangedListener(it) }

            cb.setOnCheckedChangeListener(null)
            cb.isChecked = block.isChecked
            et.setText(block.text)
            et.setSelection(block.text.length)

            // Gạch chân text nếu đã checked
            applyCheckedStyle(block.isChecked)

            cb.setOnCheckedChangeListener { _, isChecked ->
                val pos = bindingAdapterPosition
                if (pos != -1) {
                    blocks[pos] = (blocks[pos] as NoteBlock.CheckboxBlock).copy(isChecked = isChecked)
                    applyCheckedStyle(isChecked)
                    callback.onCheckboxToggled(pos, isChecked)
                }
            }

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val pos = bindingAdapterPosition
                    if (pos != -1) {
                        blocks[pos] = (blocks[pos] as NoteBlock.CheckboxBlock).copy(text = s.toString())
                        callback.onTextChanged(pos, s.toString())
                    }
                }
            }
            et.addTextChangedListener(textWatcher)
        }

        fun requestFocus() {
            et.post {
                et.requestFocus()
                et.setSelection(et.text?.length ?: 0)
            }
        }

        private fun applyCheckedStyle(checked: Boolean) {
            et.paintFlags = if (checked)
                et.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            else
                et.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            et.alpha = if (checked) 0.5f else 1.0f
        }

        private fun setupKeyListener() {
            et.setOnKeyListener { _, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                val pos = bindingAdapterPosition
                if (pos == -1) return@setOnKeyListener false

                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        callback.onEnterPressed(pos)
                        true
                    }
                    KeyEvent.KEYCODE_DEL -> {
                        if (et.selectionStart == 0) {
                            callback.onBackspaceAtStart(pos, et.text.isNullOrEmpty())
                            true
                        } else false
                    }
                    else -> false
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // IMAGE BLOCK
    // ─────────────────────────────────────────────────────────
    inner class ImageBlockVH(view: View) : RecyclerView.ViewHolder(view) {
        private val iv: ImageView = view.findViewById(R.id.iv_image_block)
        private val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_image)

        fun bind(block: NoteBlock.ImageBlock, position: Int) {
            Glide.with(iv.context)
                .load(File(block.filePath))
                .centerCrop()
                .into(iv)

            btnDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != -1) callback.onDeleteBlock(pos)
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // AUDIO BLOCK
    // ─────────────────────────────────────────────────────────
    inner class AudioBlockVH(view: View) : RecyclerView.ViewHolder(view) {
        private val btnPlay: FrameLayout = view.findViewById(R.id.btn_play_audio)
        private val ivPlayIcon: ImageView = view.findViewById(R.id.iv_play_icon)
        private val tvDuration: TextView = view.findViewById(R.id.tv_audio_duration)
        private val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_audio)

        private var mediaPlayer: MediaPlayer? = null
        private val handler = Handler(Looper.getMainLooper())
        private val updateRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        tvDuration.text = formatMs(mp.currentPosition.toLong())
                        handler.postDelayed(this, 200)
                    }
                }
            }
        }

        fun bind(block: NoteBlock.AudioBlock, position: Int) {
            tvDuration.text = formatMs(block.durationMs)

            btnPlay.setOnClickListener {
                if (mediaPlayer?.isPlaying == true) {
                    stopPlayback(block.durationMs)
                } else {
                    startPlayback(block.filePath, block.durationMs)
                }
            }

            btnDelete.setOnClickListener {
                stopPlayback(block.durationMs)
                val pos = bindingAdapterPosition
                if (pos != -1) callback.onDeleteBlock(pos)
            }
        }

        private fun startPlayback(filePath: String, totalDurationMs: Long) {
            try {
                stopPlayback(totalDurationMs)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(filePath)
                    prepare()
                    start()
                    setOnCompletionListener { stopPlayback(totalDurationMs) }
                }
                ivPlayIcon.setImageResource(R.drawable.ic_stop)
                ivPlayIcon.setColorFilter(android.graphics.Color.parseColor("#1A1A1A"))
                handler.post(updateRunnable)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun stopPlayback(totalDurationMs: Long) {
            handler.removeCallbacks(updateRunnable)
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
            ivPlayIcon.setImageResource(R.drawable.ic_play)
            ivPlayIcon.setColorFilter(android.graphics.Color.parseColor("#1A1A1A"))
            tvDuration.text = formatMs(totalDurationMs)
        }
    }

    // ─────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────
    private fun formatMs(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
