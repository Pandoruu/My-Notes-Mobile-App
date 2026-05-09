package com.example.mynotes.view.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mynotes.R
import com.example.mynotes.di.AppContainer
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.model.ThemeMode
import com.example.mynotes.presentation.home.NotesListViewModel
import com.example.mynotes.presentation.menu.MenuViewModel
import com.example.mynotes.presentation.settings.SettingsViewModel
import com.example.mynotes.databinding.FragmentNotesBinding
import com.example.mynotes.view.adapter.NoteAdapter
import com.google.android.material.tabs.TabLayout
import androidx.navigation.NavOptions
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotesListViewModel
    private lateinit var adapter: NoteAdapter
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var menuViewModel: MenuViewModel

    private var currentThemeMode: ThemeMode = ThemeMode.LIGHT
    private var currentLanguage: String = "system"

    private var currentCategoryName = "All"

    private var currentNotesLiveData: LiveData<List<Note>>? = null

    private val categoriesList = mutableListOf<Pair<Int, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            NotesListViewModel.Factory(
                observeCurrentUserIdUseCase = AppContainer.observeCurrentUserIdUseCase,
                observeCategoriesUseCase = AppContainer.observeCategoriesUseCase,
                observeAllNotesUseCase = AppContainer.observeAllNotesUseCase,
                observeNotesByCategoryUseCase = AppContainer.observeNotesByCategoryUseCase,
                togglePinUseCase = AppContainer.togglePinUseCase,
                toggleFavoriteUseCase = AppContainer.toggleFavoriteUseCase
            )
        )[NotesListViewModel::class.java]

        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModel.Factory(
                observeThemeModeUseCase = AppContainer.observeThemeModeUseCase,
                observeLanguageUseCase = AppContainer.observeLanguageUseCase,
                setThemeModeUseCase = AppContainer.setThemeModeUseCase,
                setLanguageUseCase = AppContainer.setLanguageUseCase
            )
        )[SettingsViewModel::class.java]

        menuViewModel = ViewModelProvider(
            this,
            MenuViewModel.Factory(AppContainer.logoutUseCase)
        )[MenuViewModel::class.java]

        settingsViewModel.themeMode.observe(viewLifecycleOwner) { mode ->
            currentThemeMode = mode
        }
        settingsViewModel.language.observe(viewLifecycleOwner) { language ->
            currentLanguage = language
        }

        adapter = NoteAdapter(
            onClick = { note ->
                val action = NotesFragmentDirections.actionNotesNavToViewNoteFragment(note.id)
                findNavController().navigate(action)
            },
            onLongClick = { note ->
                showNoteOptionsDialog(note)
            }
        )
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerView.adapter = adapter

        setupTabs()

        binding.btnAdd.setOnClickListener {
            // -1 để tạo note mới
            val action = NotesFragmentDirections.actionNotesNavToViewNoteFragment(-1)
            findNavController().navigate(action)
        }

        setUpHamburgerMenu()
    }

    private fun setupTabs() {
        viewModel.observeCategories().observe(viewLifecycleOwner) { categories ->
            binding.tabLayout.removeAllTabs()
            categoriesList.clear()

            // Tab đầu tiên luôn là "All" với id = 0
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All"))
            categoriesList.add(0 to "All")

            for (cat in categories) {
                if (cat.name != "All") {
                    binding.tabLayout.addTab(binding.tabLayout.newTab().setText(cat.name))
                    categoriesList.add(cat.id to cat.name)
                }
            }

            // Chọn tab All mặc định (position 0) và load notes tương ứng
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
            currentCategoryName = "All"
            loadAllNotes()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                val (catId, catName) = categoriesList.getOrNull(position) ?: (0 to "All")
                currentCategoryName = catName
                if (catId == 0) {
                    loadAllNotes()
                } else {
                    loadNotesByCategory(catId)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // Load tất cả notes (loại bỏ observer cũ trước khi observe LiveData mới)
    private fun loadAllNotes() {
        currentNotesLiveData?.removeObservers(viewLifecycleOwner)
        currentNotesLiveData = viewModel.observeAllNotes()
        currentNotesLiveData?.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }
    }

    // Load note theo categoryId (loại bỏ observer cũ trước khi observe LiveData mới)
    private fun loadNotesByCategory(categoryId: Int) {
        currentNotesLiveData?.removeObservers(viewLifecycleOwner)
        currentNotesLiveData = viewModel.observeNotesByCategory(categoryId)
        currentNotesLiveData?.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }
    }

    private fun setUpHamburgerMenu() {
        binding.btnHamburger.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.hamburger_menu, popup.menu)
            popup.setForceShowIcon(true)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_account -> {
                        findNavController().navigate(R.id.action_notesNav_to_accountFragment)
                        true
                    }
                    R.id.menu_theme -> {
                        showThemeDialog()
                        true
                    }
                    R.id.menu_language -> {
                        showLanguageDialog()
                        true
                    }
                    R.id.menu_favorite -> {
                        findNavController().navigate(R.id.action_notesNav_to_favoriteFragment)
                        true
                    }
                    R.id.menu_trash -> {
                        findNavController().navigate(R.id.action_notesNav_to_trashFragment)
                        true
                    }
                    R.id.menu_category -> {
                        findNavController().navigate(R.id.action_notesNav_to_categoryFragment)
                        true
                    }
                    R.id.menu_logout -> {
                        showLogoutConfirm()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun showThemeDialog() {
        val options = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        )
        val selected = if (currentThemeMode == ThemeMode.DARK) 1 else 0

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.theme))
            .setSingleChoiceItems(options, selected) { dialog, which ->
                val mode = if (which == 1) ThemeMode.DARK else ThemeMode.LIGHT
                settingsViewModel.setThemeMode(mode)
                val nightMode = if (mode == ThemeMode.DARK) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                AppCompatDelegate.setDefaultNightMode(nightMode)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLanguageDialog() {
        val options = arrayOf(
            getString(R.string.lang_english),
            getString(R.string.lang_vietnamese)
        )
        val selected = if (currentLanguage == "vi") 1 else 0

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.language))
            .setSingleChoiceItems(options, selected) { dialog, which ->
                val tag = if (which == 1) "vi" else "en"
                settingsViewModel.setLanguage(tag)
                val locales = LocaleListCompat.forLanguageTags(tag)
                AppCompatDelegate.setApplicationLocales(locales)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLogoutConfirm() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirm))
            .setPositiveButton(getString(R.string.logout)) { dialog, _ ->
                menuViewModel.logout()
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.notesNav, true)
                    .build()
                findNavController().navigate(R.id.loginFragment, null, navOptions)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showNoteOptionsDialog(note: Note) {
        val pinText = if (note.isPinned) {
            getString(R.string.unpin)
        } else {
            getString(R.string.pin)
        }
        val favoriteText = if (note.isFavorite) {
            getString(R.string.remove_favorite)
        } else {
            getString(R.string.add_favorite)
        }

        val options = arrayOf(pinText, favoriteText)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.note_options))
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        viewModel.togglePin(note)
                    }
                    1 -> {
                        viewModel.toggleFavorite(note)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Gỡ observer nếu còn
        currentNotesLiveData?.removeObservers(viewLifecycleOwner)
        _binding = null
    }
}