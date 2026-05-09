package com.example.mynotes.domain.usecase.user

import com.example.mynotes.domain.model.Category
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.repository.CategoryRepository
import com.example.mynotes.domain.repository.NoteRepository

class EnsureUserDataUseCase(
    private val categoryRepository: CategoryRepository,
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(userId: Int) {
        var category = categoryRepository.getCategoryByName(userId, "All")
        if (category == null) {
            val id = categoryRepository.addCategory(Category(userId = userId, name = "All")).toInt()
            category = Category(id = id, userId = userId, name = "All")
        }

        val notes = noteRepository.getAllNotesOnce(userId)
        if (notes.isNotEmpty()) return

        val demoNotes = listOf(
            Note(
                userId = userId,
                categoryId = category.id,
                title = "Chao mung den MyNotes!",
                detail = "Day la ghi chu mau dau tien cua ban."
            ),
            Note(
                userId = userId,
                categoryId = category.id,
                title = "Ghi chu so 2",
                detail = "Ban co the chinh sua noi dung nay."
            ),
            Note(
                userId = userId,
                categoryId = category.id,
                title = "Ghi chu so 3",
                detail = "Ban co the chinh sua noi dung nay."
            ),
            Note(
                userId = userId,
                categoryId = category.id,
                title = "Ghi chu so 4",
                detail = "Ban co the chinh sua noi dung nay."
            ),
            Note(
                userId = userId,
                categoryId = category.id,
                title = "Ghi chu so 5",
                detail = "Ban co the chinh sua noi dung nay."
            )
        )
        demoNotes.forEach { noteRepository.addNote(it) }
    }
}

