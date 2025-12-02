# My Notes - Mobile Application

My Notes is a note-taking application for Android, developed with Kotlin using MVVM architecture.

## Current Features

### Note Management
- Create new notes with title and content
- Edit notes
- Delete notes (move to trash)
- Restore notes from trash
- Permanently delete notes
- 
### Other Features
- Pin notes to the top of the list (displays pin icon)
- Mark notes as favorites
- Search by title and content (supports Vietnamese accent removal)
- Create, edit, delete categories
- Filter notes by category


## Features In Development

### Account System
- User login and registration
- Personal information management
- Data synchronization between devices

### Calendar
- View notes by time
- Schedule reminders
- Integration with calendar system

## Technologies Used

### Language & Framework
- Kotlin
- Android SDK
- Jetpack Components

### Architecture & Design Pattern
- MVVM (Model-View-ViewModel)
- Repository Pattern
- LiveData
- ViewBinding

### Database
- Room Database
- DAO Pattern

### Navigation & UI
- Navigation Component
- Safe Args
- RecyclerView
- Material Components

### Build & Tools
- Gradle Kotlin DSL
- Kotlin Coroutines
- Version Catalog

## System Requirements

### Development Environment
- JDK 11+
- Android Studio Hedgehog or higher
- Gradle 8.0+
- Android SDK API Level 24+
- Target SDK API Level 34

### Device Requirements
- Minimum SDK: Android 7.0 (API 24)

## Installation & Running

### Clone Repository
```bash
git clone https://github.com/Pandoruu/My-Notes-Mobile-App.git
cd My-Notes-Mobile-App
```

### Open Project in Android Studio
1. Open Android Studio
2. Select **File → Open**
3. Navigate to the project folder and select **Open**
4. Wait for Gradle sync to complete

### Build & Run
**Using Android Studio:**
1. Select device/emulator from dropdown
2. Press **Run** (Shift + F10) or click `run` icon

## Project Structure

```
My-Notes-Mobile-App/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/mynotes/
│   │   │   │   │
│   │   │   │   ├── database/              # Data Layer
│   │   │   │   │   ├── dao/               # Data Access Objects
│   │   │   │   │   │   ├── UserDao.kt
│   │   │   │   │   │   ├── CategoryDao.kt
│   │   │   │   │   │   └── NoteDao.kt
│   │   │   │   │   │
│   │   │   │   │   ├── table/             # Entity Models
│   │   │   │   │   │   ├── User.kt
│   │   │   │   │   │   ├── Category.kt
│   │   │   │   │   │   └── Note.kt
│   │   │   │   │   │
│   │   │   │   │   ├── repo/              # Repositories
│   │   │   │   │   │   └── NotesRepository.kt
│   │   │   │   │   │
│   │   │   │   │   ├── viewmodel/         # ViewModels
│   │   │   │   │   │   └── NotesViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   ├── Converters.kt      # Type Converters
│   │   │   │   │   ├── DatabaseInit.kt    # Database Singleton
│   │   │   │   │   └── NotesDatabase.kt   # Room Database
│   │   │   │   │
│   │   │   │   └── view/                  # Presentation Layer
│   │   │   │       ├── adapter/           # RecyclerView Adapters
│   │   │   │       │   ├── NoteAdapter.kt
│   │   │   │       │   ├── CategoryManagerAdapter.kt
│   │   │   │       │   └── TrashAdapter.kt
│   │   │   │       │
│   │   │   │       └── ui/                # UI Components
│   │   │   │           ├── main/
│   │   │   │           │   └── MainActivity.kt
│   │   │   │           ├── home/
│   │   │   │           │   └── NotesFragment.kt
│   │   │   │           ├── search/
│   │   │   │           │   └── SearchFragment.kt
│   │   │   │           ├── favorite/
│   │   │   │           │   └── FavoriteFragment.kt
│   │   │   │           ├── category/
│   │   │   │           │   └── CategoryFragment.kt
│   │   │   │           ├── view_note/
│   │   │   │           │   └── ViewNoteFragment.kt
│   │   │   │           ├── trash/
│   │   │   │           │   └── TrashFragment.kt
│   │   │   │           └── calendar/
│   │   │   │               └── CalendarFragment.kt
│   │   │   │
│   │   │   └── res/                       # Resources
│   │   │       ├── layout/                # XML Layouts
│   │   │       ├── drawable/              # Icons & Graphics
│   │   │       ├── navigation/            # Navigation Graph
│   │   │       ├── menu/                  # Menu Resources
│   │   │       └── values/                # Colors, Strings, Themes
│   │   │
│   │   └── androidTest/                   # Instrumented Tests
│   │
│   └── build.gradle.kts                   # App-level Build Config
│
├── gradle/                                # Gradle Configuration
│   └── libs.versions.toml                # Version Catalog
│
├── build.gradle.kts                       # Project-level Build Config
├── settings.gradle.kts                    # Settings
└── README.md                              # Documentation
```

## MVVM Architecture

### Data Flow
```
View (Fragment/Activity)
    ↕
ViewModel (LiveData)
    ↕
Repository
    ↕
DAO (Room)
    ↕
Database (SQLite)
```

### Layers Description

**View Layer (UI)**
- Fragments and Activities
- ViewBinding for view interaction
- Observe LiveData from ViewModel
- Handle user input and display data

**ViewModel Layer**
- Manage UI state and business logic
- Expose LiveData to View
- Call Repository to fetch/update data
- Lifecycle-aware

**Repository Layer**
- Single source of truth for data
- Mediator between ViewModel and DAO
- Handle data transformation

**DAO Layer**
- Interface defines database operations
- Room auto-generates implementation
- Support suspend functions for Coroutines

**Database Layer**
- Room Database (SQLite wrapper)
- Entity classes define tables
- Type Converters for complex types

## User Guide

### Home Screen
1. View notes list in grid layout
2. Press (+) button to create new note
3. Select category tab to filter notes
4. Long-press on note to Pin or Favorite
5. Press hamburger icon to open menu

### Search Notes
1. Open SearchFragment from bottom navigation
2. Enter keywords in search box
3. Results display in real-time
4. Press Enter or Search button to confirm
5. Click on note to view details

### Category Management
1. Go to Hamburger menu → Categories
2. Press (+) button to create new category
3. Long-press category to edit or delete

### Trash
1. Go to Hamburger menu → Trash
2. View list of deleted notes
3. Long-press to Restore or Delete permanently

### Favorite Notes
1. Go to Hamburger menu → Favorite
2. View all marked favorite notes
3. Click to view details or long-press to edit

## Main Dependencies

```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    
    // Room Database
    val room_version = "2.7.2"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    
    // Navigation Component
    val nav_version = "2.9.3"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
```

## Contributing

We welcome all contributions to the project!


## License

This project is licensed under nothing

## Authors

- **[Pandoruu](https://github.com/Pandoruu)** - Initial work

## Contact

Project Link: [https://github.com/Pandoruu/My-Notes-Mobile-App](https://github.com/Pandoruu/My-Notes-Mobile-App)

For questions or suggestions, please open an issue on GitHub.

**Last Updated**: December 2024
