package com.example.mynotes.domain.model

import java.util.UUID

/**
 * Caen chỉnh đoạn van bản trong TextBlock.
 */
enum class TextAlignment { LEFT, CENTER, RIGHT }

/**
 * Sealed class đại diện cho từng loại block trong nội dung của một note.
 * Mỗi block có ID duy nhất để DiffUtil và focus management hoạt động đúng.
 */
sealed class NoteBlock {
    abstract val id: String

    data class TextBlock(
        override val id: String = UUID.randomUUID().toString(),
        val text: String = "",
        /** Nội dung rich text dạng HTML. Nếu rỗng thì dùng [text] (backward compat). */
        val htmlText: String = "",
        val alignment: TextAlignment = TextAlignment.LEFT
    ) : NoteBlock()

    data class CheckboxBlock(
        override val id: String = UUID.randomUUID().toString(),
        val text: String = "",
        val isChecked: Boolean = false
    ) : NoteBlock()

    data class ImageBlock(
        override val id: String = UUID.randomUUID().toString(),
        val filePath: String
    ) : NoteBlock()

    data class AudioBlock(
        override val id: String = UUID.randomUUID().toString(),
        val filePath: String,
        val durationMs: Long = 0L
    ) : NoteBlock()
}
