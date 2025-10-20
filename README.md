# My Notes — Mobile App

My Notes là một ứng dụng ghi chú dành cho Android, viết hoàn toàn bằng Kotlin. Ứng dụng hướng tới mục tiêu nhẹ, dễ dùng và có cấu trúc rõ ràng.

## Tính năng (dự kiến)
- Tạo, chỉnh sửa và xóa ghi chú
- Danh sách ghi chú
- Tìm kiếm, sắp xếp hoặc lọc ghi chú
- Lưu trữ cục bộ (offline)
- Kiến trúc phân tầng (UI → ViewModel → Repository → DataSource)

## Công nghệ
- Ngôn ngữ: Kotlin
- Nền tảng: Android (AndroidX / Jetpack)
- Hệ thống build: Gradle (Kotlin DSL - build.gradle.kts)

## Yêu cầu
- JDK 11+
- Android Studio Narwhal 4 (hoặc cao hơn)
- Android SDK 33+
- Kết nối internet để tải dependency lần đầu

## Cài đặt & chạy
1. Clone repository:
   git clone https://github.com/Pandoruu/My-Notes-Mobile-App.git

2. Mở project bằng Android Studio:
   - File → Open → chọn thư mục chứa project
   - Hoặc từ terminal: ./gradlew clean

3. Đồng bộ Gradle (Sync)

4. Chọn device/emulator và chạy app:
   - Run → app
   - Hoặc từ terminal: ./gradlew assembleDebug

## Cấu trúc dự án (tổng quan)

com.example.mynotes/
│
├── database/
│   ├── dao/
│   │   ├── CategoryDao.kt
│   │   ├── NoteDao.kt
│   │   └── UserDao.kt
│   │
│   ├── repo/
│   │   └── NotesRepository.kt
│   │
│   ├── table/
│   │   ├── Category.kt
│   │   ├── Note.kt
│   │   └── User.kt
│   │
│   ├── viewmodel/
│   │   ├── NotesViewModel.kt
│   │   └── Converters.kt
│   │
│   ├── DatabaseInit.kt
│   └── NotesDatabase.kt
│
│── view/
   ├── adapter/
   │   ├── CategoryManagerAdapter.kt
   │   ├── NoteAdapter.kt
   │   └── TrashAdapter.kt
   │
   └── ui/
       ├── calendar/
       │   └── CalendarFragment.kt
       ├── category/
       │   └── CategoryFragment.kt
       ├── favorite/
       │   └── FavoriteFragment.kt
       ├── home/
       │   └── NotesFragment.kt
       ├── main/
       │   └── MainActivity.kt
       ├── search/
       │   └── SearchFragment.kt
       └── view_note/
           └── ViewNoteFragment.kt
        
