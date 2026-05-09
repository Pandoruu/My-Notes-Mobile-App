package com.example.mynotes.di

import android.content.Context
import com.example.mynotes.data.local.DatabaseInit
import com.example.mynotes.data.local.SessionDataStore
import com.example.mynotes.data.local.SettingsDataStore
import com.example.mynotes.data.repository.CategoryRepository
import com.example.mynotes.data.repository.NotesRepository
import com.example.mynotes.data.repository.UserRepository
import com.example.mynotes.domain.usecase.auth.LoginUseCase
import com.example.mynotes.domain.usecase.auth.LogoutUseCase
import com.example.mynotes.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.example.mynotes.domain.usecase.auth.RegisterUseCase
import com.example.mynotes.domain.usecase.category.AddCategoryUseCase
import com.example.mynotes.domain.usecase.category.DeleteCategoryUseCase
import com.example.mynotes.domain.usecase.category.ObserveCategoriesUseCase
import com.example.mynotes.domain.usecase.category.UpdateCategoryUseCase
import com.example.mynotes.domain.usecase.notes.AddNoteUseCase
import com.example.mynotes.domain.usecase.notes.DeleteNoteUseCase
import com.example.mynotes.domain.usecase.notes.MoveNoteToTrashUseCase
import com.example.mynotes.domain.usecase.notes.ObserveAllNotesUseCase
import com.example.mynotes.domain.usecase.notes.ObserveFavoriteNotesUseCase
import com.example.mynotes.domain.usecase.notes.ObserveNoteByIdUseCase
import com.example.mynotes.domain.usecase.notes.ObserveNotesByCategoryUseCase
import com.example.mynotes.domain.usecase.notes.ObserveTrashedNotesUseCase
import com.example.mynotes.domain.usecase.notes.SearchNotesUseCase
import com.example.mynotes.domain.usecase.notes.ToggleFavoriteUseCase
import com.example.mynotes.domain.usecase.notes.TogglePinUseCase
import com.example.mynotes.domain.usecase.notes.UpdateNoteUseCase
import com.example.mynotes.domain.usecase.user.EnsureUserDataUseCase
import com.example.mynotes.domain.usecase.user.ObserveUserByIdUseCase
import com.example.mynotes.domain.usecase.notes.RestoreNoteUseCase
import com.example.mynotes.domain.usecase.settings.ObserveLanguageUseCase
import com.example.mynotes.domain.usecase.settings.ObserveThemeModeUseCase
import com.example.mynotes.domain.usecase.settings.SetLanguageUseCase
import com.example.mynotes.domain.usecase.settings.SetThemeModeUseCase

object AppContainer {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val database by lazy { DatabaseInit.getDatabase(appContext) }

    private val noteRepository by lazy { NotesRepository(database.noteDao()) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    private val userRepository by lazy { UserRepository(database.userDao()) }
    private val sessionRepository by lazy { SessionDataStore(appContext) }
    private val settingsRepository by lazy { SettingsDataStore(appContext) }

    private val ensureUserDataUseCase by lazy {
        EnsureUserDataUseCase(categoryRepository, noteRepository)
    }

    val observeCurrentUserIdUseCase by lazy { ObserveCurrentUserIdUseCase(sessionRepository) }
    val loginUseCase by lazy { LoginUseCase(userRepository, sessionRepository, ensureUserDataUseCase) }
    val registerUseCase by lazy { RegisterUseCase(userRepository) }
    val logoutUseCase by lazy { LogoutUseCase(sessionRepository) }

    val observeCategoriesUseCase by lazy { ObserveCategoriesUseCase(categoryRepository) }
    val addCategoryUseCase by lazy { AddCategoryUseCase(categoryRepository) }
    val updateCategoryUseCase by lazy { UpdateCategoryUseCase(categoryRepository) }
    val deleteCategoryUseCase by lazy { DeleteCategoryUseCase(categoryRepository) }

    val observeAllNotesUseCase by lazy { ObserveAllNotesUseCase(noteRepository) }
    val observeNotesByCategoryUseCase by lazy { ObserveNotesByCategoryUseCase(noteRepository) }
    val observeTrashedNotesUseCase by lazy { ObserveTrashedNotesUseCase(noteRepository) }
    val observeFavoriteNotesUseCase by lazy { ObserveFavoriteNotesUseCase(noteRepository) }
    val observeNoteByIdUseCase by lazy { ObserveNoteByIdUseCase(noteRepository) }
    val searchNotesUseCase by lazy { SearchNotesUseCase(noteRepository) }

    val addNoteUseCase by lazy { AddNoteUseCase(noteRepository) }
    val updateNoteUseCase by lazy { UpdateNoteUseCase(noteRepository) }
    val moveNoteToTrashUseCase by lazy { MoveNoteToTrashUseCase(noteRepository) }
    val restoreNoteUseCase by lazy { RestoreNoteUseCase(noteRepository) }
    val deleteNoteUseCase by lazy { DeleteNoteUseCase(noteRepository) }
    val togglePinUseCase by lazy { TogglePinUseCase(noteRepository) }
    val toggleFavoriteUseCase by lazy { ToggleFavoriteUseCase(noteRepository) }

    val observeUserByIdUseCase by lazy { ObserveUserByIdUseCase(userRepository) }

    val observeThemeModeUseCase by lazy { ObserveThemeModeUseCase(settingsRepository) }
    val setThemeModeUseCase by lazy { SetThemeModeUseCase(settingsRepository) }
    val observeLanguageUseCase by lazy { ObserveLanguageUseCase(settingsRepository) }
    val setLanguageUseCase by lazy { SetLanguageUseCase(settingsRepository) }
}
