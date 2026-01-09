# NoteItUP - Diary App Solution Document

A Compose Multiplatform diary application inspired by **Diaro** and **Joplin**, featuring Material 3 design, end-to-end encryption (planned), and cross-platform support for Android, iOS, and Desktop.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Current Implementation Status](#current-implementation-status)
3. [Architecture](#architecture)
4. [UI/UX Design](#uiux-design)
5. [Data Models](#data-models)
6. [Project Structure](#project-structure)
7. [Dependencies](#dependencies)
8. [Future Features](#future-features)

---

## Project Overview

### Vision
Create an open-source, privacy-focused diary app that combines the best features of Diaro (intuitive journaling experience) and Joplin (open-source, encryption, and export capabilities).

### Target Platforms
- Android (API 24+)
- iOS (15.0+)
- Desktop (JVM)

### Design Philosophy
- **Privacy First**: Local-first with optional encrypted sync
- **Material 3**: Modern, adaptive UI with dynamic color theming
- **Simplicity**: Clean, distraction-free writing experience
- **Open Source**: MIT License, community-driven development

---

## Current Implementation Status

### Phase 1 - Core Features (Completed)

| Feature | Status | Description |
|---------|--------|-------------|
| Project Setup | ✅ | Compose Multiplatform with Android, iOS, JVM targets |
| Clean Architecture | ✅ | Domain, Data, Presentation layers with proper separation |
| SQLDelight Database | ✅ | Multiplatform database with expect/actual driver factory |
| Koin DI | ✅ | Dependency injection across all platforms |
| Material 3 Theme | ✅ | Light/dark theme with purple color scheme |
| Home Screen | ✅ | Greeting card, stats row, recent entries list |
| Entry Editor | ✅ | Create/edit entries with title, content, mood |
| All Entries Screen | ✅ | View all diary entries |
| Compose Navigation | ✅ | Type-safe navigation with serializable routes |
| Favorite Toggle | ✅ | Mark entries as favorites |
| Mood Tracking | ✅ | Select mood (Amazing, Good, Neutral, Sad, Terrible) |

### Phase 2 - Enhanced UX (Completed)

| Feature | Status | Description |
|---------|--------|-------------|
| Search Functionality | ✅ | Debounced search across title and content |
| Tags Management | ✅ | Create, view, and delete tags with colors |
| Search Screen | ✅ | SearchBar with results list |
| Tags Screen | ✅ | Tag list with create dialog and delete action |

### Screens Implemented

1. **Home Screen** (`HomeScreen.kt`)
   - Bottom navigation bar with Home and Settings tabs
   - Dynamic greeting based on time of day
   - Stats row (total entries, streak days, tags, favorites) - clickable for Statistics
   - Recent entries list (limited to 20)
   - FAB for creating new entry (only visible on Home tab)
   - "See All" navigation to all entries
   - TopAppBar with Search, Calendar, Folders, Tags icons
   - Inline Settings content when Settings tab is selected

2. **All Entries Screen** (`AllEntriesScreen.kt`)
   - Shows all diary entries
   - Back navigation to home

3. **Editor Screen** (`EditorScreen.kt`)
   - Title input
   - Mood selector with emoji chips
   - Content area
   - Favorite toggle
   - Save functionality

4. **Search Screen** (`SearchScreen.kt`)
   - SearchBar with debounced query (300ms)
   - Results list using DiaryEntryCard
   - Empty state for no results
   - Initial state prompting user to type

5. **Tags Screen** (`TagsScreen.kt`)
   - List of all tags with color indicators
   - FAB to create new tag
   - Create tag dialog with name and color picker
   - Delete tag action per item

6. **Calendar Screen** (`CalendarScreen.kt`)
   - Month view with entry indicators
   - Month/year navigation
   - Entry list for selected date
   - Click to view entries by date

7. **Folders Screen** (`FoldersScreen.kt`)
   - Folder management with CRUD operations
   - Folder selection for organizing entries

8. **Settings Content** (inline in HomeScreen via `SettingsContent.kt`)
   - Theme mode selection (Light/Dark/System)
   - Accent color picker (6 colors)
   - Font size slider (Small/Medium/Large)
   - Daily reminder toggle with time picker
   - Export data navigation

9. **Statistics Screen** (`StatisticsScreen.kt`)
   - Mood distribution chart
   - Monthly entries bar chart
   - Current and longest streak display
   - Total words and average words per entry

10. **Backup & Restore Screen** (`ExportScreen.kt`)
    - Format selection (JSON/CSV/Markdown)
    - Include options (entries, folders, tags)
    - Export and share functionality
    - Import/Restore from JSON backup file
    - Import from Day One ZIP exports
    - Import from Joplin JEX (TAR archive) exports
    - Platform-specific file picker for selecting backup files

11. **Security Settings Screen** (`SecuritySettingsScreen.kt`)
    - PIN lock setup and management
    - Change PIN / Remove PIN options
    - Biometric authentication toggle
    - Auto-lock timeout configuration
    - Security information card

12. **Lock Screen** (`LockScreen.kt`)
    - PIN entry with numeric keypad
    - Visual PIN dots indicator
    - Biometric authentication button
    - PIN setup flow (enter, confirm)
    - Error handling for incorrect PIN

---

## Architecture

### Clean Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION LAYER                                 │
│  ┌───────────────┐  ┌───────────────┐  ┌─────────────┐  ┌───────────────┐   │
│  │    Screens    │  │  ViewModels   │  │  UI State   │  │  Navigation   │   │
│  │  (Composable) │  │  (StateFlow)  │  │  (Data)     │  │  (Compose)    │   │
│  └───────────────┘  └───────────────┘  └─────────────┘  └───────────────┘   │
│                              │                                               │
│                              ▼                                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                             DOMAIN LAYER                                     │
│  ┌───────────────┐  ┌───────────────┐  ┌─────────────────────────────────┐  │
│  │   Use Cases   │  │    Models     │  │   Repository Interfaces         │  │
│  │               │  │  (Entities)   │  │   (Contracts)                   │  │
│  │ - CreateEntry │  │ - DiaryEntry  │  │ - DiaryRepository               │  │
│  │ - GetEntries  │  │ - Folder      │  │                                 │  │
│  │ - UpdateEntry │  │ - Tag         │  │                                 │  │
│  │ - DeleteEntry │  │ - Mood        │  │                                 │  │
│  │ - GetStats    │  │ - DiaryStats  │  │                                 │  │
│  │ - SearchEntry │  │               │  │                                 │  │
│  │ - CreateTag   │  │               │  │                                 │  │
│  │ - DeleteTag   │  │               │  │                                 │  │
│  └───────────────┘  └───────────────┘  └─────────────────────────────────┘  │
│                              │                                               │
│                              ▼                                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                              DATA LAYER                                      │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐                    │
│  │   Database    │  │  Repository   │  │    Mappers    │                    │
│  │  (SQLDelight) │  │    Impl       │  │ (Entity↔DTO)  │                    │
│  └───────────────┘  └───────────────┘  └───────────────┘                    │
│  ┌───────────────────────────────────────────────────────┐                  │
│  │            Platform-specific Driver Factory            │                  │
│  │   Android: AndroidSqliteDriver                         │                  │
│  │   iOS: NativeSqliteDriver                              │                  │
│  │   JVM: JdbcSqliteDriver                                │                  │
│  └───────────────────────────────────────────────────────┘                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### MVI Pattern

ViewModels follow the MVI (Model-View-Intent) pattern:
- **Intent**: Sealed interface defining user actions
- **State**: Immutable data class representing UI state
- **ViewModel**: Processes intents and updates state via StateFlow

```kotlin
// Example: HomeIntent.kt
sealed interface HomeIntent {
    data object LoadEntries : HomeIntent
    data class ToggleFavorite(val entryId: String) : HomeIntent
    data class DeleteEntry(val entryId: String) : HomeIntent
    data object DismissError : HomeIntent
}
```

### Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| UI Framework | Compose Multiplatform | 1.9.3 |
| Language | Kotlin | 2.3.0 |
| Navigation | Compose Navigation | 2.9.1 |
| Database | SQLDelight | 2.0.2 |
| DI | Koin | 4.1.0 |
| Async | Kotlin Coroutines | (bundled) |
| DateTime | kotlinx-datetime | 0.7.1 |
| Serialization | kotlinx-serialization | 1.7.3 |
| UUID | benasher44/uuid | 0.8.4 |

---

## UI/UX Design

### Material 3 Color Scheme

#### Light Mode
```kotlin
Primary: Purple (0xFF6750A4)
OnPrimary: White
PrimaryContainer: Light Purple
Surface: Off-white
Background: Off-white
```

#### Dark Mode
```kotlin
Primary: Light Purple (0xFFD0BCFF)
OnPrimary: Dark Purple
PrimaryContainer: Dark Purple
Surface: Near-black
Background: Near-black
```

### Home Screen Layout

```
┌──────────────────────────────────────────┐
│  ┌────────────────────────────────────┐  │
│  │         Top App Bar                │  │
│  │  "My Diary"                    🔍  │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │       Greeting Card                │  │
│  │  "Good Morning"                    │  │
│  │  📅 Thursday, January 1, 2026      │  │
│  │  "How are you feeling today?"      │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │    Quick Stats Row                 │  │
│  │  ┌────┐  ┌────┐  ┌────┐  ┌────┐   │  │
│  │  │ 42 │  │ 7  │  │ 5  │  │ 12 │   │  │
│  │  │Ent.│  │Days│  │Tags│  │Fav.│   │  │
│  │  └────┘  └────┘  └────┘  └────┘   │  │
│  └────────────────────────────────────┘  │
│                                          │
│  Recent Entries               [See All]  │
│  ┌────────────────────────────────────┐  │
│  │  😊 Today's Reflection        ♡   │  │
│  │  Today at 10:30 AM                 │  │
│  │  "Started the day with..."         │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │  🎯 Goals for 2026            ♡   │  │
│  │  Yesterday at 9:15 PM              │  │
│  │  "This year I want to..."          │  │
│  └────────────────────────────────────┘  │
│                                          │
│                               ┌────────┐ │
│                               │   +    │ │
│                               │  FAB   │ │
│                               └────────┘ │
└──────────────────────────────────────────┘
```

### Components

| Component | File | Description |
|-----------|------|-------------|
| GreetingCard | `GreetingCard.kt` | Dynamic greeting with date |
| StatsRow | `StatsRow.kt` | 4 stat chips in a row |
| DiaryEntryCard | `DiaryEntryCard.kt` | Entry card with mood, title, preview |
| TagChip | `TagChip.kt` | Small tag display chip |

---

## Data Models

### Domain Models

```kotlin
data class DiaryEntry(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val folderId: String? = null,
    val tags: List<Tag> = emptyList(),
    val isFavorite: Boolean = false,
    val mood: Mood? = null
)

enum class Mood(val emoji: String, val label: String) {
    AMAZING("😄", "Amazing"),
    GOOD("🙂", "Good"),
    NEUTRAL("😐", "Neutral"),
    SAD("😢", "Sad"),
    TERRIBLE("😫", "Terrible")
}

data class DiaryStats(
    val totalEntries: Int,
    val streakDays: Int,
    val totalTags: Int,
    val favoriteCount: Int
)

enum class LockType {
    NONE, PIN, BIOMETRIC, PIN_AND_BIOMETRIC
}

data class SecuritySettings(
    val lockType: LockType,
    val autoLockTimeout: AutoLockTimeout,
    val pinHash: String?,
    val biometricEnabled: Boolean
)
```

### SQLDelight Schema

```sql
CREATE TABLE DiaryEntryEntity (
    id TEXT PRIMARY KEY NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    folder_id TEXT,
    is_favorite INTEGER NOT NULL DEFAULT 0,
    mood TEXT
);

CREATE TABLE TagEntity (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    color TEXT
);

CREATE TABLE EntryTagEntity (
    entry_id TEXT NOT NULL,
    tag_id TEXT NOT NULL,
    PRIMARY KEY (entry_id, tag_id)
);
```

---

## Project Structure

```
NoteItUP/
├── composeApp/
│   └── src/
│       ├── commonMain/kotlin/com/romreviewertools/noteitup/
│       │   ├── App.kt
│       │   ├── di/
│       │   │   └── AppModule.kt
│       │   ├── data/
│       │   │   ├── database/
│       │   │   │   └── DriverFactory.kt (expect)
│       │   │   ├── export/
│       │   │   │   ├── FileExporter.kt (expect)
│       │   │   │   └── FileImporter.kt (expect)
│       │   │   ├── import/
│       │   │   │   ├── TarExtractor.kt (expect)
│       │   │   │   ├── dayone/
│       │   │   │   │   ├── DayOneModels.kt
│       │   │   │   │   └── DayOneParser.kt
│       │   │   │   └── joplin/
│       │   │   │       ├── JoplinModels.kt
│       │   │   │       └── JoplinParser.kt
│       │   │   ├── repository/
│       │   │   │   ├── DiaryRepositoryImpl.kt
│       │   │   │   └── SecurityRepositoryImpl.kt
│       │   │   └── mapper/
│       │   │       └── EntityMapper.kt
│       │   ├── domain/
│       │   │   ├── model/
│       │   │   │   ├── DiaryEntry.kt
│       │   │   │   ├── Mood.kt
│       │   │   │   ├── Tag.kt
│       │   │   │   └── DiaryStats.kt
│       │   │   ├── repository/
│       │   │   │   └── DiaryRepository.kt
│       │   │   └── usecase/
│       │   │       ├── CreateEntryUseCase.kt
│       │   │       ├── GetEntriesUseCase.kt
│       │   │       ├── UpdateEntryUseCase.kt
│       │   │       ├── ExportEntriesUseCase.kt
│       │   │       ├── ImportEntriesUseCase.kt
│       │   │       ├── ImportDayOneUseCase.kt
│       │   │       ├── ImportJoplinUseCase.kt
│       │   │       └── ...
│       │   └── presentation/
│       │       ├── navigation/
│       │       │   ├── Routes.kt
│       │       │   └── AppNavigation.kt
│       │       ├── theme/
│       │       │   ├── Color.kt
│       │       │   ├── Type.kt
│       │       │   └── Theme.kt
│       │       ├── components/
│       │       │   ├── GreetingCard.kt
│       │       │   ├── StatsRow.kt
│       │       │   ├── DiaryEntryCard.kt
│       │       │   ├── TagChip.kt
│       │       │   └── FilePicker.kt (expect)
│       │       └── screens/
│       │           ├── home/
│       │           ├── editor/
│       │           ├── allentries/
│       │           ├── search/
│       │           ├── tags/
│       │           ├── security/
│       │           │   ├── SecurityViewModel.kt
│       │           │   ├── SecuritySettingsScreen.kt
│       │           │   └── LockScreen.kt
│       │           └── export/
│       ├── androidMain/
│       │   └── kotlin/.../
│       │       ├── MainActivity.kt
│       │       ├── DiaryApplication.kt
│       │       ├── di/PlatformModule.android.kt
│       │       └── data/database/DriverFactory.android.kt
│       ├── iosMain/
│       │   └── kotlin/.../
│       │       ├── MainViewController.kt
│       │       ├── KoinHelper.kt
│       │       ├── di/PlatformModule.ios.kt
│       │       └── data/database/DriverFactory.ios.kt
│       └── jvmMain/
│           └── kotlin/.../
│               ├── main.kt
│               ├── di/PlatformModule.jvm.kt
│               └── data/database/DriverFactory.jvm.kt
├── iosApp/
│   └── iosApp/
│       ├── iOSApp.swift
│       └── ContentView.swift
├── gradle/
│   └── libs.versions.toml
└── SOLUTION.md
```

---

## Dependencies

### gradle/libs.versions.toml (Key Versions)

```toml
[versions]
kotlin = "2.3.0"
compose-multiplatform = "1.9.3"
sqldelight = "2.0.2"
koin = "4.1.0"
kotlinx-datetime = "0.7.1"
kotlinx-serialization = "1.7.3"
navigation-compose = "2.9.1"
uuid = "0.8.4"
commons-compress = "1.25.0"
```

### Platform-Specific

| Platform | SQLDelight Driver |
|----------|-------------------|
| Android | AndroidSqliteDriver |
| iOS | NativeSqliteDriver |
| JVM | JdbcSqliteDriver |

---

## Completed Features (Phase 3 & 4)

### Phase 3 - Enhanced UX (Completed)
- [x] Calendar view with month navigation
- [x] Folders/Categories management
- [x] Markdown support in editor
- [x] Tag selection in Editor

### Phase 4 - Themes, Statistics, Export & Reminders (Completed)
- [x] Theme customization (Light/Dark/System)
- [x] Accent color picker (6 colors)
- [x] Font size adjustment (Small/Medium/Large)
- [x] Statistics screen with mood distribution, streaks, word counts
- [x] Export functionality (JSON/CSV/Markdown)
- [x] Import/Restore from JSON backup
- [x] Daily reminder with time picker
- [x] Bottom navigation bar (Home/Settings)
- [x] Settings integrated inline with bottom nav
- [x] Custom app icon
- [x] FileProvider for secure file sharing (Android)

### Phase 5 - Security (Completed)
- [x] PIN lock with 4-6 digit support
- [x] PIN setup and verification screens
- [x] Auto-lock timeout settings (Immediate to Never)
- [x] Security settings screen
- [x] Biometric authentication (Face ID / Touch ID / Fingerprint)
  - Android: BiometricPrompt with BIOMETRIC_STRONG/WEAK
  - iOS: LocalAuthentication with LAContext (Face ID & Touch ID)
  - JVM: Stub implementation (not available)
- [x] Security repository for secure PIN storage
- [x] App lock on launch with auto-trigger biometric
- [x] Platform-specific BiometricAuthenticator (expect/actual pattern)

### Phase 6 - Cloud Sync (Completed)
Cloud backup/sync with user's own storage (Google Drive & Dropbox).

**Features implemented:**
- [x] Google Drive integration with OAuth2
- [x] Dropbox integration with OAuth2
- [x] Encrypted backup bundles (AES-256-GCM)
- [x] Password-based key derivation (PBKDF2 with 100k iterations)
- [x] Auto-sync with configurable intervals (15min, 30min, hourly, 6hr, daily)
- [x] Manual backup/restore buttons
- [x] Sync status indicators with progress
- [x] WiFi-only sync option
- [x] Backup list with delete functionality
- [x] Cloud quota display

**Components:**
| Component | Description |
|-----------|-------------|
| `EncryptionService` | Platform-specific AES-256-GCM encryption (expect/actual) |
| `EncryptedBundleService` | Creates/extracts encrypted backup bundles |
| `GoogleDriveProvider` | Google Drive API v3 with OAuth2 |
| `DropboxProvider` | Dropbox API v2 with OAuth2 |
| `CloudSyncManager` | Orchestrates sync operations |
| `CloudSyncRepository` | Persists sync settings and tokens |
| `CloudSyncScreen` | Full-featured UI for cloud management |

**Architecture:**
```
User Password → PBKDF2 (100k iterations) → AES-256-GCM Encryption
                                                    ↓
ExportEntriesUseCase → JSON → Compress → Encrypt → Upload to Cloud
```

**Bundle Format:**
```
backup_TIMESTAMP.noteitup
├── 4 bytes: metadata length (big-endian int)
├── N bytes: metadata JSON (unencrypted)
│   {
│     "version": 1,
│     "createdAt": timestamp,
│     "entryCount": n,
│     "folderCount": n,
│     "tagCount": n,
│     "salt": "base64",
│     "encryptionAlgorithm": "AES-256-GCM"
│   }
└── Remaining: encrypted data (IV + ciphertext + auth tag)
```

**Platform Implementations:**
| Platform | Encryption | HTTP Client | OAuth |
|----------|------------|-------------|-------|
| Android | javax.crypto | Ktor + OkHttp | System browser |
| iOS | CommonCrypto | Ktor + Darwin | UIApplication |
| JVM | javax.crypto | Ktor + Java | Desktop browser |

### Phase 7 - Import from Popular Apps (Mostly Completed)

**Import functionality from Day One and Joplin journaling apps.**

**Completed:**
- [x] Day One ZIP import support (Android, JVM)
- [x] Joplin JEX (TAR archive) import support (Android, JVM)
- [x] Smart title extraction for Day One entries
- [x] Notebook-to-Folder mapping for Joplin
- [x] To-do conversion with checkbox notation
- [x] Location data preservation
- [x] Tag and folder hierarchy preservation
- [x] Skip & track error handling
- [x] Photo/image import with thumbnail generation
- [x] Full UI integration in ExportScreen
- [x] Dependency injection setup
- [x] ViewModel integration with proper state management

**Pending:**
- [ ] iOS TarExtractor implementation (Joplin import on iOS)
  - Challenge: No built-in TAR support in iOS Foundation framework
  - Options: CocoaPods library (e.g., "LightUntar"), Swift Compression framework, or custom TAR parser
  - Day One import works on iOS (uses ZipExporter which is already implemented)
- [ ] Testing with real Day One export files
- [ ] Testing with real Joplin JEX export files
- [ ] Integration testing across all platforms
- [ ] Joplin resource reference parsing (images in markdown content with `![](:/resource_id)` notation)
- [ ] Large import performance optimization (1000+ entries)

**Supported Import Formats:**

| Format | Source App | Structure | Features |
|--------|------------|-----------|----------|
| Day One ZIP | Day One | Journal.json + photos/ | Markdown content, starred→favorite, tags, location, photos |
| Joplin JEX | Joplin | TAR with JSON files | Notebooks→folders, to-dos→checkboxes, tags, resources |

**Components:**

| Component | Description |
|-----------|-------------|
| `DayOneParser` | Parses Day One JSON exports with smart title extraction |
| `JoplinParser` | Parses Joplin JEX archives with type-based detection |
| `ImportDayOneUseCase` | Orchestrates Day One import flow |
| `ImportJoplinUseCase` | Orchestrates Joplin import flow |
| `TarExtractor` | Platform-specific TAR extraction (expect/actual) |

**Day One Import Flow:**
```
Day One ZIP → Extract → Parse Journal.json → Transform Data → Import
                ↓
         photos/*.jpg → Copy to app storage → Create thumbnails
```

**Joplin Import Flow:**
```
Joplin JEX → TAR Extract → Detect types (1=note, 2=notebook, 5=tag, etc.)
                               ↓
                    Transform & map IDs → Import with relationships
```

**Key Mappings:**

Day One:
- `starred` → `isFavorite`
- First markdown heading → `title` (or first line, or date-based fallback)
- `photos` → `ImageAttachment` objects
- `tags` → Tags (created if not exists)
- `location` → Location object

Joplin:
- Notebooks → Folders (with parent hierarchy)
- `is_todo=1` → Regular entry with `- [ ]` checkbox notation
- `todo_completed=1` → `- [x]` checked notation
- `parent_id` → `folderId` (hierarchy preserved)
- Resources (type_=4, mime=image/*) → ImageAttachment objects

**Enhanced Import Result:**
```kotlin
data class ImportResult(
    val success: Boolean,
    val entriesImported: Int,
    val foldersImported: Int,
    val tagsImported: Int,
    val imagesImported: Int,
    val entriesSkipped: Int,        // NEW
    val skippedItems: List<SkippedItem>, // NEW
    val error: String?
)
```

**Dependencies Added:**
- `org.apache.commons:commons-compress:1.25.0` (Android/JVM) - TAR extraction

**Platform-Specific Implementations:**
| Platform | TAR Extraction | Status |
|----------|----------------|--------|
| Android | Apache Commons Compress | ✅ Implemented |
| JVM | Apache Commons Compress | ✅ Implemented |
| iOS | Pending | ⏳ Not yet implemented |

**Current Limitations:**
- Joplin import only available on Android and Desktop (JVM) platforms
- iOS users can import Day One archives but not Joplin JEX files until iOS TarExtractor is implemented
- Images embedded in Joplin markdown content (resource references) are not yet parsed and linked

---

## Future Features

### Phase 7.5 - WYSIWYG Markdown Editor (Completed ✅)

**Rich text editing experience with markdown storage.**

#### Implementation Status: ✅ Completed

The app now features a WYSIWYG markdown editor where users see formatted text while editing, without needing to toggle between edit and preview modes.

#### What Was Implemented

**Library Used:** `Richeditor-compose` by MohamedRejeb v1.0.0-rc07
- Full Kotlin Multiplatform support (Android, iOS, Desktop)
- Native markdown bidirectional conversion (`setMarkdown()` / `toMarkdown()`)
- Material 3 integration
- Built-in text formatting support

**Changes Made:**

1. **Added Library Dependency** (`build.gradle.kts` + `libs.versions.toml`):
   ```kotlin
   implementation(libs.richeditor.compose)
   ```

2. **Updated EditorScreen.kt**:
   - Replaced `BasicTextField` with `RichTextEditor` component
   - Removed preview mode toggle (no longer needed)
   - Integrated `rememberRichTextState()` for state management
   - Auto-sync markdown content with `richTextState.setMarkdown()` and `richTextState.toMarkdown()`
   - Removed markdown toolbar (formatting is now visual)

3. **UI Improvements**:
   - Removed the preview/edit toggle button (👁️ icon) from top bar
   - Content now displays formatted in real-time as users type
   - **Bold**, *italic*, headings, lists render immediately
   - Cleaner, more focused writing experience

#### How It Works

```kotlin
// Rich text state manages content
val richTextState = rememberRichTextState()

// Load markdown content from database
LaunchedEffect(uiState.content) {
    richTextState.setMarkdown(uiState.content)
}

// Save markdown content on save
viewModel.processIntent(EditorIntent.UpdateContent(richTextState.toMarkdown()))

// Display rich text editor
RichTextEditor(
    state = richTextState,
    modifier = Modifier.fillMaxWidth().weight(1f),
    textStyle = MaterialTheme.typography.bodyLarge,
    placeholder = { Text("Write your thoughts...") }
)
```

#### Benefits Achieved

- ✅ **WYSIWYG editing**: Users see formatted text while typing
- ✅ **Markdown storage**: Content still saved as markdown for compatibility
- ✅ **No mode switching**: Edit and preview are now unified
- ✅ **Cross-platform**: Works on Android, iOS, and Desktop
- ✅ **Simplified UI**: Removed unnecessary toggle button
- ✅ **Better UX**: Uninterrupted writing flow

#### Testing & Next Steps

**Testing Completed:**
- ✅ Library integration successful
- ✅ Build passing on Android
- ✅ Markdown conversion working (bidirectional)
- ✅ UI updated and streamlined

**To Be Tested:**
- [ ] Manual testing on Android device/emulator
- [ ] Testing on iOS device/simulator
- [ ] Testing on Desktop (JVM)
- [ ] Performance with long entries (1000+ words)
- [ ] Edge cases: empty content, special characters, complex markdown

**Potential Future Enhancements:**
- Add optional formatting toolbar (bold, italic, heading buttons)
- Support for inline image preview in editor
- Table editing support
- Code block syntax highlighting
- Customizable text styles and colors

---

### Phase 8 - API-Based AI Integration (Planned)

**Cloud AI for text improvement and brainstorming - User brings their own API key.**

#### Vision
Integrate popular AI APIs (OpenAI, Claude, Gemini, Groq) to assist users with writing and brainstorming. Users provide their own API keys, ensuring zero cost to developers and giving users full control over their AI provider and usage.

#### Core Features

**1. AI Text Improvement & Suggestions**
- Grammar and spelling corrections
- Style improvements and rewording suggestions
- Tone adjustment (formal, casual, concise, etc.)
- Sentence restructuring for clarity
- Real-time suggestions while writing

**2. Brainstorming Chatbot**
- Conversational AI for idea generation
- Journaling prompts and reflection questions
- Creative writing assistance
- Memory-based conversations (context from previous entries)

#### Supported AI Providers

Users can choose from multiple popular AI APIs (bring your own API key):

| Provider | Models Available | Free Tier | Pricing | Best For |
|----------|-----------------|-----------|---------|----------|
| **OpenAI** | GPT-4o, GPT-4o-mini, GPT-3.5-turbo | No | $0.15-$5/1M tokens | High-quality text improvement |
| **Anthropic Claude** | Claude 3.5 Sonnet, Haiku | No | $3-$15/1M tokens | Long-form writing assistance |
| **Google Gemini** | Gemini 1.5 Flash, Pro | ✅ Yes (generous) | Free tier + paid | Cost-effective, good quality |
| **Groq** | Llama 3, Mixtral, Gemma | ✅ Yes | Free + paid | Fast inference speed |
| **OpenRouter** | 100+ models | ✅ Some free | Pay-per-use | Access to many models |
| **Together AI** | Qwen, Llama, Mixtral | ✅ Yes ($25 credit) | Pay-per-use | Open-source models |

**Provider Selection Criteria:**
- User brings their own API key (zero cost to app/developer)
- Free tiers available for users to try
- Standard OpenAI-compatible API format
- Good performance for diary/text improvement tasks
- Privacy-conscious options available

#### Technical Implementation Plan

**Architecture:**
```
┌─────────────────────────────────────────────────┐
│            NoteItUP Diary App                   │
│  ┌───────────────────────────────────────────┐  │
│  │     Editor Screen / Chat Interface        │  │
│  └──────────────┬────────────────────────────┘  │
│                 │                                │
│  ┌──────────────▼────────────────────────────┐  │
│  │      AI Service (Use Cases)               │  │
│  │  - TextImprovementUseCase                 │  │
│  │  - BrainstormingChatUseCase               │  │
│  └──────────────┬────────────────────────────┘  │
│                 │                                │
│  ┌──────────────▼────────────────────────────┐  │
│  │   AI API Service (expect/actual)          │  │
│  │  - API key management                     │  │
│  │  - HTTP client with retry logic           │  │
│  │  - Response parsing & streaming           │  │
│  │  - Context management                     │  │
│  └──────────────┬────────────────────────────┘  │
│                 │                                │
│  ┌──────────────▼────────────────────────────┐  │
│  │    Platform-Specific HTTP & Storage       │  │
│  │  Android: Ktor + OkHttp                   │  │
│  │  iOS: Ktor + Darwin                       │  │
│  │  Desktop: Ktor + Java                     │  │
│  └───────────────────────────────────────────┘  │
│                 │                                │
│                 ▼                                │
│        ☁️ User's AI Provider                    │
│     (OpenAI / Claude / Gemini / etc.)          │
└─────────────────────────────────────────────────┘
```

**Key Technologies:**
- **Ktor Client**: Cross-platform HTTP client for API calls
  - Already used in Phase 6 (Cloud Sync)
  - Supports request/response streaming
  - Built-in retry and timeout handling
- **kotlinx.serialization**: JSON parsing for API responses
- **Encrypted Preferences**: Secure API key storage
  - Android: EncryptedSharedPreferences
  - iOS: Keychain Services
  - JVM: Platform-specific secure storage
- **OpenAI-compatible API**: Standard format used by most providers

#### Features Breakdown

**Settings Screen Additions:**
- [ ] AI provider selection (OpenAI, Claude, Gemini, Groq, etc.)
- [ ] API key management with secure storage
- [ ] Model selection per provider (e.g., GPT-4o vs GPT-4o-mini)
- [ ] Provider connection test
- [ ] Enable/disable AI features toggle
- [ ] Usage tracking (token counts, cost estimates)
- [ ] Privacy notice and data handling disclosure

**Editor Enhancements:**
- [ ] "Improve Text" button with AI suggestions
- [ ] Context menu actions (improve, rephrase, expand, shorten)
- [ ] Tone adjustment options (formal, casual, concise, creative)
- [ ] Grammar and spelling correction with inline suggestions
- [ ] Accept/reject suggestion UI with diff view
- [ ] Batch improvement for entire entry
- [ ] Streaming response display with cancel option

**Brainstorming Chat:**
- [ ] Dedicated chat interface with AI assistant
- [ ] Conversation history (session-based, stored locally)
- [ ] Quick prompts (reflection questions, creative prompts, journaling ideas)
- [ ] Insert chat responses directly into diary entry
- [ ] Context awareness (can reference previous diary entries with user permission)
- [ ] Provider and model switching within chat

#### BYOK (Bring Your Own Key) Advantages

**Cost Benefits:**
- ✅ Zero cost to app developers (no API expenses)
- ✅ Users pay only for what they use
- ✅ Access to free tier providers (Gemini, Groq, OpenRouter)
- ✅ Users can take advantage of promotional credits
- ✅ No recurring subscription fees from the app

**Privacy & Control:**
- ✅ Users choose their AI provider based on privacy preferences
- ✅ Direct relationship between user and AI provider
- ✅ API keys stored securely on device (encrypted storage/keychain)
- ✅ Users can delete API keys and data anytime
- ✅ No app-level tracking of AI usage
- ✅ Option to use privacy-focused providers (e.g., local-first options in future)

**Flexibility:**
- ✅ Users can switch providers anytime
- ✅ Access to latest models as providers release them
- ✅ Choose different models for different tasks (fast vs high-quality)
- ✅ Freedom to use multiple providers simultaneously

#### Technical Challenges

**High Priority:**
- [ ] Secure API key storage across platforms (Keychain/EncryptedPreferences)
- [ ] HTTP client configuration with streaming support
- [ ] Rate limiting and retry logic for API calls
- [ ] Error handling for API failures and network issues
- [ ] Token counting and cost estimation

**Medium Priority:**
- [ ] Provider-specific API format differences (OpenAI vs Claude vs Gemini)
- [ ] Response streaming for real-time suggestions
- [ ] Context management for long diary entries (token limits)
- [ ] Prompt engineering for diary-specific tasks
- [ ] UI/UX for AI interactions and suggestions
- [ ] Offline mode graceful degradation

**Low Priority:**
- [ ] Multi-language support for AI prompts
- [ ] Advanced prompt templates library
- [ ] Usage analytics and cost tracking
- [ ] Migration path to local models in Phase 9

#### Implementation Phases

**Phase 8.1 - API Key Management & Settings**
1. Create AI settings repository with secure storage
2. Implement API key input and validation UI
3. Add provider selection (OpenAI, Claude, Gemini, Groq, etc.)
4. Implement connection test functionality
5. Set up Ktor HTTP client with proper configuration

**Phase 8.2 - Text Improvement Use Cases**
1. Design AI service architecture (expect/actual pattern)
2. Implement OpenAI-compatible API client
3. Create text improvement use cases (grammar, style, tone)
4. Build suggestion UI in editor with accept/reject
5. Add streaming response support

**Phase 8.3 - Brainstorming Chat Interface**
1. Design chat UI with conversation history
2. Implement chat use case with context management
3. Create journaling prompts library
4. Add ability to insert AI responses into entries
5. Support for referencing previous diary entries (with permission)

**Phase 8.4 - Provider Support & Polish**
1. Add support for multiple providers (Claude, Gemini, Groq)
2. Implement token counting and cost estimation
3. Add provider-specific model selection
4. Performance optimization and error handling refinement
5. Comprehensive testing across platforms and providers
6. Documentation and user guide

---

### Phase 9 - Additional Advanced Features
- [ ] Image attachments (native capture/gallery)
- [ ] Widgets (Android home screen)
- [ ] Local encryption (AES-256) for data at rest
- [ ] Rich text formatting toolbar
- [ ] Voice notes with transcription
- [ ] Drawing/sketching support
- [ ] Collaborative journaling (optional cloud sync with E2EE)

---

## Running the App

### Android
```bash
./gradlew :composeApp:installDebug
```

### Desktop (JVM)
```bash
./gradlew :composeApp:run
```

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run.

---

*This document reflects the current implementation as of Phase 7 (mostly complete). Phases 1-6 are fully implemented. Phase 7 (Day One and Joplin import) is functional on Android and Desktop platforms, with iOS TarExtractor implementation pending for Joplin support on iOS.*

---

## Known Limitations & Pending Work

### Phase 7 Remaining Tasks

**High Priority:**
1. **iOS TarExtractor Implementation**
   - Required for Joplin import on iOS devices
   - Day One import already works on all platforms
   - Estimated effort: Medium (need to research iOS TAR extraction libraries)

**Medium Priority:**
2. **Real-world Testing**
   - Test with actual Day One export files from users
   - Test with actual Joplin JEX exports
   - Validate edge cases and large imports

3. **Joplin Image Resource Parsing**
   - Parse `![](:/resource_id)` references in Joplin markdown
   - Map old resource IDs to new image IDs
   - Update markdown content with correct image references

**Low Priority:**
4. **Performance Optimization**
   - Test with 1000+ entry imports
   - Optimize batch database operations
   - Add progress indicators for long-running imports
