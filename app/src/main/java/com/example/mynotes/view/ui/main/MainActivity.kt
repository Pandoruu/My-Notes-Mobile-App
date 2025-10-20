package com.example.mynotes.view.ui.main

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mynotes.R
import com.example.mynotes.database.DatabaseInit
import com.example.mynotes.database.table.*
import com.example.mynotes.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNav.setupWithNavController(navController)

        // Ẩn/hiện bottom navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.viewNoteFragment,
                R.id.favoriteFragment,
                R.id.trashFragment -> binding.bottomNav.visibility = View.GONE
                else -> binding.bottomNav.visibility = View.VISIBLE
            }
        }

        setupInitialData()
    }

    private fun setupInitialData() {
        val db = DatabaseInit.getDatabase(this)
        val userDao = db.userDao()
        val categoryDao = db.categoryDao()
        val noteDao = db.noteDao()

        lifecycleScope.launch(Dispatchers.IO) {
            // Tạo user mặc định
            var user = userDao.getUserByUsernameOnce("local_user")
            if (user == null) {
                val newUser = User(username = "local_user", password = "")
                val id = userDao.insert(newUser)
                user = newUser.copy(id = id.toInt())
            }

            // Tạo category mặc định “All”
            var category = categoryDao.getCategoryByNameOnce(user.id, "All")
            if (category == null) {
                val newCategory = Category(userId = user.id, name = "All")
                val id = categoryDao.insert(newCategory)
                category = newCategory.copy(id = id.toInt())
            }

            // Tạo 5 ghi chú mẫu nếu chưa có
            val notes = noteDao.getAllNotesOnce(user.id)
            if (notes.isEmpty()) {
                val demoNotes = listOf(
                    Note(userId = user.id, categoryId = category.id, title = "Chào mừng đến MyNotes!", detail = "Đây là ghi chú mẫu đầu tiên của bạn."),
                    Note(userId = user.id, categoryId = category.id, title = "Ghi chú số 2", detail = "Bạn có thể chỉnh sửa nội dung này."),
                    Note(userId = user.id, categoryId = category.id, title = "Ghi chú số 3", detail = "Thử tạo thêm category khác để phân loại."),
                    Note(userId = user.id, categoryId = category.id, title = "Ghi chú số 4", detail = "Bạn có thể ghim note quan trọng để nó hiện trên đầu."),
                    Note(userId = user.id, categoryId = category.id, title = "Ghi chú số 5", detail = "Kéo xuống Trash để xoá ghi chú khi không cần nữa.")
                )
                demoNotes.forEach { noteDao.insert(it) }
            }
        }
    }
}
