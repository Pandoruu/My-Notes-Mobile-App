package com.example.mynotes.database.viewmodel
import androidx.lifecycle.*
import com.example.mynotes.database.repo.NotesRepository
import com.example.mynotes.database.table.*
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NotesRepository) : ViewModel() {


    // USER
    fun observeUserById(userId: Int): LiveData<User?> =
        repository.observeUserById(userId)

    fun observeAllUsers(): LiveData<List<User>> =
        repository.observeAllUsers()

    fun addUser(username: String, password: String) {
        viewModelScope.launch {
            repository.addUser(User(username = username, password = password))
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch { repository.updateUser(user) }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch { repository.deleteUser(user) }
    }
    // CATEGORY
    fun observeCategories(userId: Int): LiveData<List<Category>> =
        repository.observeCategoriesByUser(userId)

    fun addCategory(userId: Int, name: String) {
        viewModelScope.launch {
            repository.addCategory(Category(userId = userId, name = name))
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch { repository.updateCategory(category) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }
    // NOTES
    fun observeActiveNotes(userId: Int): LiveData<List<Note>> =
        repository.observeActiveNotes(userId)

    fun observeTrashedNotes(userId: Int): LiveData<List<Note>> =
        repository.observeTrashedNotes(userId)

    fun searchNotes(userId: Int, query: String): LiveData<List<Note>> =
        repository.observeSearchNotes(userId, query)

    fun observeNoteById(noteId: Int): LiveData<Note?> =
        repository.observeNoteById(noteId)

    fun observeAllNotes(userId: Int): LiveData<List<Note>> =
        repository.observeAllNotes(userId)

    fun observeNotesByCategory(userId: Int, categoryName: String): LiveData<List<Note>> =
        repository.observeNotesByCategory(userId, categoryName)


    fun addNote(
        userId: Int,
        categoryId: Int?,
        title: String,
        detail: String?
    ) {
        viewModelScope.launch {
            repository.addNote(
                Note(
                    userId = userId,
                    categoryId = categoryId,
                    title = title,
                    detail = detail,
                    titlePlain = removeAccents(title),
                    detailPlain = detail?.let { removeAccents(it) }
                )
            )
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(
                titlePlain = removeAccents(note.title),
                detailPlain = note.detail?.let { removeAccents(it) },
                updatedAt = java.util.Date()
            ))
        }
    }

    fun moveNoteToTrash(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(
                isTrashed = true,
                trashedAt = java.util.Date()
            ))
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(
                isTrashed = false,
                trashedAt = null
            ))
        }
    }

    fun deleteNotePermanently(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    //bỏ dấu để search
    private fun removeAccents(input: String): String {
        val normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    //Factory
    class NotesViewModelFactory(private val repository: NotesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesViewModel(repository) as T
        }
    }
}
