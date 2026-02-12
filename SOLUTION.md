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
   - Content area (WYSIWYG markdown editor)
   - Favorite toggle
   - Save functionality
   - **Back button with unsaved changes confirmation dialog** ⚠️ IMPORTANT
     - Prevents accidental data loss
     - Shows dialog: "Save changes?" with options: Save, Discard, Cancel
     - Debounce protection to prevent multiple rapid taps causing navigation issues

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

13. **AI Settings Screen** (`AISettingsScreen.kt`)
    - AI provider selection (6 providers)
    - API key input with secure storage
    - Model selection dropdown
    - Connection test button
    - Enable/disable AI features toggle
    - Provider information cards

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
- [x] Google Drive integration with OAuth2 (native Android auth via AuthorizationClient)
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
| `GoogleDriveAuthHelper` | Android native auth bridge (AuthorizationClient + CompletableDeferred) |
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
| Android | javax.crypto | Ktor + OkHttp | Native AuthorizationClient (Google Drive), System browser (Dropbox) |
| iOS | CommonCrypto | Ktor + Darwin | UIApplication |
| JVM | javax.crypto | Ktor + Java | Desktop browser |

**Google Drive OAuth (Android - Native Flow):**
```
User taps Connect → AuthorizationClient.authorize() → Native Google consent UI
                                                            ↓
                                                    serverAuthCode
                                                            ↓
                                            Token exchange (no redirect_uri)
                                                            ↓
                                                    Access + Refresh tokens saved
```
- Uses `play-services-auth` (`AuthorizationClient`) instead of browser-based OAuth
- `GoogleDriveAuthHelper` singleton bridges Activity callbacks with coroutines via `CompletableDeferred`
- Token exchange omits `redirect_uri` (not needed for native auth codes)
- iOS/JVM still use browser-based flow as fallback
- Requires Android OAuth client in Google Cloud Console (matched by package name + SHA-1)

### Phase 7 - Import from Popular Apps (Completed)

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
- [x] iOS TarExtractor implementation (Joplin import on iOS)
  - Implemented using pure Kotlin TAR parser (no external dependencies)
  - Supports POSIX ustar format with prefix field for long paths
  - Day One import also works on iOS (uses ZipExporter)
- [x] Joplin resource reference parsing (images in markdown content with `![](:/resource_id)` notation)
  - Parses `![alt](:/resourceId)` references from note body
  - Maps resource IDs to new image IDs and populates imageIds in entries
  - Removes inline resource references from content (images display in gallery)
  - Collapses resulting blank lines for clean content
- [ ] Testing with real Day One export files
- [ ] Testing with real Joplin JEX export files
- [ ] Integration testing across all platforms
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
| iOS | Pure Kotlin TAR parser | ✅ Implemented |

**Current Limitations:**
- Testing with real-world export files still pending
- Large import performance optimization (1000+ entries) not yet done

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

### Phase 8 - API-Based AI Integration (Completed ✅)

**AI-powered writing assistance using user's own API keys (BYOK - Bring Your Own Key).**

**Completed:**
- [x] Multi-provider AI support (OpenAI, Claude, Gemini, Groq, OpenRouter, Together AI)
- [x] AI Settings screen with provider selection and API key management
- [x] Secure API key storage (platform-specific encryption)
- [x] Model selection per provider
- [x] Connection test functionality
- [x] 8 text improvement types (Journal, Grammar, Clarity, Shorter, Expand, Professional, Casual, Summarize)
- [x] AI Toolbar in editor with horizontal scrollable chips
- [x] AI suggestion dialog with preview and accept/cancel
- [x] OpenAI-compatible API client
- [x] Gemini-specific API client (custom format)
- [x] Error handling with detailed logging
- [x] UI integration in EditorScreen
- [x] Streaming response infrastructure
- [x] Dependency injection setup

**Pending:**
- [x] Brainstorming chat interface (implemented with starter prompts)
- [x] Conversation history storage (persistent across sessions via SQLDelight)
- [ ] Token counting and cost estimation
- [ ] Diff view for suggestions
- [ ] Context awareness (reference previous entries)

**Components:**
| Component | Description |
|-----------|-------------|
| `AIService` | Multi-provider AI API client |
| `AISettingsRepository` | Secure settings and API key storage |
| `ImproveTextUseCase` | Text improvement business logic |
| `AISettingsScreen` | Provider selection and configuration UI |
| `AIToolbar` | Horizontal chip list for improvement types |
| `AISuggestionDialog` | AI suggestion preview dialog |

**Supported Providers:**
- **Groq** (default) - Fast, generous free tier
- **Google Gemini** - 15 req/min free tier
- **OpenAI** - Paid (GPT-4o, GPT-4o-mini)
- **Anthropic Claude** - Paid (Claude 3.5 Sonnet, Haiku)
- **OpenRouter** - Access to 100+ models
- **Together AI** - $25 free credit

---

## Detailed Phase 8 Implementation

#### Overview
Successfully integrated AI-powered text improvement features using multiple AI provider APIs. Users provide their own API keys, ensuring zero cost to developers while giving users full control over their AI provider and usage.

#### Implemented Features

**1. Multi-Provider AI Support**
- ✅ OpenAI (GPT-4o, GPT-4o-mini, GPT-3.5-turbo)
- ✅ Anthropic Claude (Claude 3.5 Sonnet, Haiku)
- ✅ Google Gemini (Gemini 2.0 Flash, Gemini 2.5 Flash)
- ✅ Groq (Llama 3.3, Mixtral) - **Default provider**
- ✅ OpenRouter (100+ models)
- ✅ Together AI (Llama, Mixtral, Qwen)

**2. AI Text Improvement Types**
Nine improvement options available via AI toolbar:
1. **Improve for Journal** - Makes text more personal, reflective, emotionally authentic
2. **Fix Grammar** - Corrects grammar, spelling, punctuation
3. **Improve Clarity** - Enhances readability and understanding
4. **Make Shorter** - Condenses while keeping key points
5. **Expand** - Adds detail, examples, depth
6. **Professional Tone** - Rewrites with formal, professional tone
7. **Casual Tone** - Rewrites with friendly, casual tone
8. **Summarize** - Creates concise summary of main points

**3. AI Settings Management**
- ✅ Provider selection with visual picker
- ✅ Secure API key storage (platform-specific encryption)
- ✅ Model selection per provider
- ✅ Connection test functionality
- ✅ Enable/disable AI features toggle
- ✅ Streaming response support

#### Supported AI Providers

Users can choose from multiple popular AI APIs (bring your own API key):

| Provider | Default Model | Free Tier | Pricing | Best For |
|----------|--------------|-----------|---------|----------|
| **Groq** ⭐ | llama-3.3-70b-versatile | ✅ Yes (generous) | Free + paid | **Fast, free, recommended** |
| **Google Gemini** | gemini-2.0-flash | ✅ Yes (15 req/min) | Free tier + paid | Cost-effective, good quality |
| **OpenAI** | gpt-4o-mini | No | $0.15-$5/1M tokens | High-quality text improvement |
| **Anthropic Claude** | claude-3-5-haiku-20241022 | No | $3-$15/1M tokens | Long-form writing assistance |
| **OpenRouter** | meta-llama/llama-3.2-3b-instruct:free | ✅ Some free | Pay-per-use | Access to 100+ models |
| **Together AI** | meta-llama/Llama-3-8b-chat-hf | ✅ Yes ($25 credit) | Pay-per-use | Open-source models |

⭐ **Groq is the default provider** - fastest inference with generous free tier

**Provider Selection Criteria:**
- User brings their own API key (zero cost to app/developer)
- Free tiers available for users to try
- Standard OpenAI-compatible API format
- Good performance for diary/text improvement tasks
- Privacy-conscious options available

#### Technical Implementation

**Architecture (Implemented):**
```
┌─────────────────────────────────────────────────┐
│            NoteItUP Diary App                   │
│  ┌───────────────────────────────────────────┐  │
│  │     Editor Screen with AI Toolbar         │  │
│  │  - AIToolbar composable                   │  │
│  │  - AISuggestionDialog                     │  │
│  │  - AI Settings navigation                 │  │
│  └──────────────┬────────────────────────────┘  │
│                 │                                │
│  ┌──────────────▼────────────────────────────┐  │
│  │      EditorViewModel                      │  │
│  │  - ImproveTextUseCase                     │  │
│  │  - Handles improvement types              │  │
│  │  - State management (loading, success)    │  │
│  └──────────────┬────────────────────────────┘  │
│                 │                                │
│  ┌──────────────▼────────────────────────────┐  │
│  │   AIService (data/ai/AIService.kt)        │  │
│  │  - Multi-provider support                 │  │
│  │  - OpenAI-compatible API client           │  │
│  │  - Gemini API client (special format)     │  │
│  │  - HTTP client (Ktor)                     │  │
│  │  - Error handling & logging               │  │
│  └──────────────┬────────────────────────────┘  │
│                 │                                │
│  ┌──────────────▼────────────────────────────┐  │
│  │   AISettingsRepository                    │  │
│  │  - Secure API key storage                 │  │
│  │  - Provider & model selection             │  │
│  │  - Settings persistence                   │  │
│  └──────────────┬────────────────────────────┘  │
│                 │                                │
│                 ▼                                │
│        ☁️ User's AI Provider                    │
│     (Groq / OpenAI / Gemini / Claude)          │
└─────────────────────────────────────────────────┘
```

**Key Components Implemented:**

| Component | File | Description |
|-----------|------|-------------|
| `AIService` | `data/ai/AIService.kt` | Multi-provider AI API client with streaming support |
| `AIModels` | `data/ai/AIModels.kt` | Data models for AI requests/responses (OpenAI + Gemini formats) |
| `ImprovementType` | `data/ai/AIModels.kt` | Enum defining 8 improvement types with system prompts |
| `AIProvider` | `domain/model/AIProvider.kt` | Enum of supported providers with base URLs and models |
| `AISettings` | `domain/model/AIProvider.kt` | Settings data class with provider, key, model selection |
| `AISettingsRepository` | `data/repository/AISettingsRepositoryImpl.kt` | Persists AI settings with secure key storage |
| `ImproveTextUseCase` | `domain/usecase/ImproveTextUseCase.kt` | Business logic for text improvement |
| `AISettingsViewModel` | `presentation/screens/aisettings/` | ViewModel for AI settings screen |
| `AISettingsScreen` | `presentation/screens/aisettings/` | UI for provider selection, API key, connection test |
| `AIToolbar` | `presentation/components/AIToolbar.kt` | Horizontal chip list for improvement type selection |
| `AISuggestionDialog` | `presentation/components/AISuggestionDialog.kt` | Dialog showing AI improvement result |

**Key Technologies:**
- **Ktor Client**: Cross-platform HTTP client for API calls (already used in Phase 6)
- **kotlinx.serialization**: JSON parsing for OpenAI and Gemini API responses
- **Encrypted Preferences**: Secure API key storage (platform-specific via expect/actual)
- **Koin DI**: Dependency injection for AI services and use cases

#### Features Breakdown

**AI Settings Screen (Completed):**
- ✅ AI provider selection (OpenAI, Claude, Gemini, Groq, OpenRouter, Together AI)
- ✅ API key management with secure storage
- ✅ Model selection per provider (GPT-4o, GPT-4o-mini, Claude Haiku, etc.)
- ✅ Provider connection test with status feedback
- ✅ Enable/disable AI features toggle
- ✅ Auto-clear API key when switching providers
- ✅ Visual provider cards with descriptions

**Editor Integration (Completed):**
- ✅ AI Toolbar component with horizontal scrollable chips
- ✅ 8 improvement type options (journal, grammar, clarity, shorter, expand, professional, casual, summarize)
- ✅ AI suggestion dialog with improved text preview
- ✅ Accept suggestion replaces editor content
- ✅ Loading state with progress indicator
- ✅ Error handling with user-friendly messages
- ✅ Streaming response support (infrastructure ready)

**Pending Features:**
- [ ] Inline diff view for suggestions (side-by-side comparison)
- [ ] Undo/redo for AI suggestions
- [ ] Usage tracking (token counts, cost estimates)
- [ ] Context awareness (reference previous entries)

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

**Phase 8.1 - API Key Management & Settings (✅ Completed)**
- ✅ Created AISettingsRepository with secure storage
- ✅ Implemented API key input and validation UI
- ✅ Added provider selection (OpenAI, Claude, Gemini, Groq, OpenRouter, Together AI)
- ✅ Implemented connection test functionality
- ✅ Set up Ktor HTTP client with proper configuration

**Phase 8.2 - Text Improvement Use Cases (✅ Completed)**
- ✅ Designed AI service architecture for multi-provider support
- ✅ Implemented OpenAI-compatible API client
- ✅ Implemented Gemini-specific API client (different format)
- ✅ Created 8 text improvement types (grammar, style, tone, journal-specific, etc.)
- ✅ Built AIToolbar and AISuggestionDialog in editor
- ✅ Added streaming response support infrastructure

**Phase 8.3 - Brainstorming Chat Interface (✅ Completed)**
- [x] Design chat UI with conversation history
- [x] Persistent chat history via SQLDelight (BrainstormMessageEntity table)
- [x] Create journaling starter prompts
- [x] Add ability to insert AI responses into entries
- [ ] Support for referencing previous diary entries (with permission)

**Phase 8.4 - Provider Support & Polish (✅ Mostly Completed)**
- ✅ Added support for 6 providers (OpenAI, Claude, Gemini, Groq, OpenRouter, Together AI)
- ✅ Implemented provider-specific model selection
- ✅ Error handling with detailed logging
- ✅ Fixed Gemini API format compatibility
- ✅ Set Groq as default provider
- ✅ UI improvements (LazyRow, unified chip styling, aligned toolbar buttons)
- [ ] Token counting and cost estimation (pending)
- [ ] Comprehensive cross-platform testing (pending)

#### How to Use AI Features

**Setup (One-time):**
1. Navigate to Settings → AI Settings (or tap AI icon in Editor when AI is disabled)
2. Select your preferred AI provider (Groq recommended for free tier)
3. Enter your API key from the provider's website
4. (Optional) Choose a specific model for the provider
5. Tap "Test Connection" to verify setup
6. Enable AI features toggle

**Using AI in Editor:**
1. Write or select text in the diary entry editor
2. Scroll through the AI toolbar below the editor
3. Tap any improvement type chip (e.g., "Improve for Journal", "Fix Grammar")
4. Wait for AI to process (loading indicator shown)
5. Review the suggested improvement in the dialog
6. Tap "Accept" to replace your text, or "Cancel" to dismiss

**Getting API Keys:**
- **Groq** (recommended): https://console.groq.com/keys - Generous free tier
- **Google Gemini**: https://aistudio.google.com/apikey - 15 requests/minute free
- **OpenAI**: https://platform.openai.com/api-keys - Paid only
- **Anthropic Claude**: https://console.anthropic.com/settings/keys - Paid only
- **OpenRouter**: https://openrouter.ai/keys - Some free models available
- **Together AI**: https://api.together.xyz/settings/api-keys - $25 free credit

---

### Phase 9 - User Engagement & Analytics ✅ COMPLETED

**In-App Rating & Firebase Analytics for user engagement and app insights.**

**Implemented:**

1. **Android In-App Rating** ✅ COMPLETED
   - **Trigger:** After user saves a diary entry (not on every save)
   - **Logic:**
     - Show rating prompt after user has saved 5 entries
     - Don't show if user already rated or dismissed permanently
     - Uses Google Play In-App Review API
     - Minimum 7 days between prompts, max 3 dismissals
   - **Implementation:**
     - Platform-specific (Android with `InAppReviewManager`, stub for iOS/JVM)
     - Uses `com.google.android.play:review-ktx:2.0.2` library
     - `ReviewStateRepository` for tracking state in preferences
     - Integrated into `EditorViewModel.save()` method

2. **Firebase Analytics Events** ✅ COMPLETED
   - **Purpose:** Track user behavior to improve app experience
   - **Events Implemented:**
     - `entry_created` - When user creates a new entry
     - `entry_saved` - When user saves an entry (new or edit)
     - `entry_deleted` - When user deletes an entry
     - `mood_selected` - Which mood users select (mood type as parameter)
     - `ai_feature_used` - AI improvement type used (type as parameter)
     - `brainstorm_session_started` - User started a brainstorm chat
     - `export_completed` - Export format and count
     - `import_completed` - Import source (Day One/Joplin) and count
     - `cloud_sync_completed` - Sync provider and status
     - `theme_changed` - Theme preference changes
     - `folder_created` / `tag_created` - Organization features used
     - `review_prompt_shown` / `review_completed` - Review flow tracking
   - **Implementation:**
     - `AnalyticsService` with expect/actual pattern
     - Android: Firebase Analytics with Bundle params
     - iOS/JVM: Stub implementations (no-op)
     - Events tracked in EditorViewModel for entry operations
   - **Privacy:**
     - No PII (personally identifiable information)
     - No diary content sent to analytics
     - Only aggregate usage patterns

**Components to Create:**

| Component | Description |
|-----------|-------------|
| `InAppReviewManager` | Android-specific in-app review prompt (expect/actual) |
| `AnalyticsService` | Platform-specific analytics tracking (expect/actual) |
| `AnalyticsEvent` | Sealed class defining all trackable events |
| `ReviewState` | Data class for tracking rating prompt state |

**Dependencies to Add:**

| Platform | Library | Purpose |
|----------|---------|---------|
| Android | `com.google.android.play:review-ktx:2.0.1` | In-App Review API |
| Android | `com.google.firebase:firebase-analytics-ktx` | Firebase Analytics |
| iOS | Firebase iOS SDK | Firebase Analytics |
| JVM | (none) | Stub implementations |

### Phase 10 - Additional Advanced Features (Future)
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

## Platform Feature Parity

### Feature Comparison: Android vs Desktop (JVM) vs iOS

#### ✅ **Fully Implemented on All Platforms**

| Feature | Android | Desktop (JVM) | iOS | Notes |
|---------|---------|---------------|-----|-------|
| Core Diary Features | ✅ | ✅ | ✅ | Entries, folders, tags, favorites, moods |
| Rich Text Editor (WYSIWYG) | ✅ | ✅ | ✅ | Markdown with live preview |
| Search | ✅ | ✅ | ✅ | Full-text search across entries |
| Calendar View | ✅ | ✅ | ✅ | Monthly calendar with entry indicators |
| Statistics | ✅ | ✅ | ✅ | Mood distribution, streaks, word counts |
| Export/Import | ✅ | ✅ | ✅ | JSON, CSV, Markdown formats |
| Day One Import | ✅ | ✅ | ✅ | ZIP with JSON + photos |
| Image Attachments | ✅ | ✅ | ✅ | Gallery picker with thumbnails |
| PIN Security | ✅ | ✅ | ✅ | 4-6 digit PIN lock |
| Cloud Sync | ✅ | ✅ | ✅ | Dropbox & Google Drive with AES-256-GCM encryption |
| AI Writing Assistant | ✅ | ✅ | ✅ | 8 improvement types, 6 providers (Groq, OpenAI, etc.) |
| Themes | ✅ | ✅ | ✅ | Light/Dark/System with 6 accent colors |
| Database | ✅ | ✅ | ✅ | SQLDelight with platform-specific drivers |

#### ⚠️ **Platform-Specific Implementations**

| Feature | Android | Desktop (JVM) | iOS | Implementation Differences |
|---------|---------|---------------|-----|---------------------------|
| Multi-Window Support | ❌ Single activity | ✅ Full support | ❌ Single window | Desktop can open multiple windows with menu bar |
| Joplin Import | ✅ Apache Commons | ✅ Apache Commons | ✅ Pure Kotlin TAR | iOS uses custom TAR parser |
| URL Opening | Chrome Custom Tabs | Desktop.browse() | UIApplication | Android has in-app browser |
| Image Picker | Photo Picker API | Swing JFileChooser | PHPicker | Platform-native pickers |
| File Picker | System Picker | Swing JFileChooser | UIDocument | Platform-native pickers |
| OAuth (Google Drive) | Native AuthorizationClient | localhost redirect | UIApplication | Android uses Google Identity Services |
| OAuth (Dropbox) | System Browser | localhost redirect | UIApplication | Desktop uses localhost:8080 |

#### ❌ **Hardware/Platform-Dependent Features**

| Feature | Android | Desktop (JVM) | iOS | Availability Reason |
|---------|---------|---------------|-----|---------------------|
| Camera Capture | ✅ | ❌ | ✅ | Desktops lack accessible camera APIs |
| Biometric Auth | ✅ Fingerprint/Face | ❌ Not available | ✅ Face ID/Touch ID | Desktop hardware limitation |
| Location Tagging | ✅ GPS + Geocoding | ❌ Not available | ✅ GPS + Geocoding | Desktops don't have GPS |
| Daily Reminders | ✅ System notifications | ❌ Not implemented | ✅ System notifications | Could add system tray on Desktop |
| Firebase Analytics | ✅ Full tracking | ❌ Stub only | ✅ Full tracking | Analytics primarily for mobile |

---

### Desktop (JVM) Specific Details

#### **What Works on Desktop** ✅

**Full Feature Parity:**
- ✅ Complete diary editing and organization
- ✅ All import/export capabilities (JSON, CSV, Markdown, Day One)
- ✅ Joplin import (TAR extraction via Apache Commons Compress)
- ✅ AI writing assistant with all providers
- ✅ Cloud sync with Dropbox and Google Drive
- ✅ PIN security with auto-lock
- ✅ Image attachments via file picker
- ✅ Complete data management and backup

**Desktop Advantages:**
- 🖥️ Larger screen for comfortable writing
- ⌨️ Full keyboard for faster typing
- 💾 Direct file system access for exports
- 📂 Multi-window support (open multiple entries, calendar, settings in separate windows)
- 🍔 Menu bar integration for quick access to all features
- 🔗 Standard browser opening for AI API key URLs

#### **Desktop-Specific Implementations**

**File Management:**
```kotlin
// Desktop uses Swing JFileChooser for native file dialogs
val fileChooser = JFileChooser().apply {
    dialogTitle = "Select Image"
    fileSelectionMode = JFileChooser.FILES_ONLY
}
```

**Storage Location:**
- Database: `~/.noteitup/noteitup.db`
- Images: `~/.noteitup/images/`
- Thumbnails: `~/.noteitup/thumbnails/`
- Preferences: Java Preferences API

**OAuth Flow:**
```kotlin
// Desktop uses localhost redirect for OAuth
redirectUri = "http://localhost:8080/oauth2callback"
// Opens system browser, waits for callback on local server
```

**TAR Extraction:**
```kotlin
// Desktop uses Apache Commons Compress (same as Android)
TarArchiveInputStream(FileInputStream(tarPath)).use { tar ->
    // Extract Joplin JEX files
}
```

#### **What's Different on Desktop** ⚠️

**Image Capture:**
- ❌ No camera support (expected - desktops don't have accessible cameras)
- ✅ Alternative: Use file picker to select existing images
- ✅ Full thumbnail generation support

**Security:**
- ❌ No biometric authentication (hardware limitation)
- ✅ PIN lock works perfectly as alternative
- ✅ Auto-lock timeout settings available

**Location:**
- ❌ No GPS location tagging (desktops don't have GPS)
- ✅ Users can manually add location text if needed

**Notifications:**
- ❌ Daily reminders not implemented
- 💡 Future: Could add system tray notifications on Windows/Mac/Linux

#### **Desktop Multi-Window Support** ✅ (Implemented)

The Desktop version now supports multiple windows for a true desktop experience:

**Features:**
- ✅ Menu bar with File, Window, and Help menus
- ✅ Open any screen in a separate window (Calendar, Statistics, Search, Tags, Folders, AI Settings, Cloud Sync)
- ✅ Create multiple editor windows for side-by-side viewing
- ✅ Single-instance enforcement for utility windows (only one Calendar, Settings, etc.)
- ✅ Independent window sizing per window type
- ✅ Proper window lifecycle management

**Implementation:**
- `WindowManager.kt`: Manages window state with StateFlow
- `WindowContent.kt`: Renders content for each window type
- `main.kt`: Menu bar integration and dynamic window creation
- `DesktopWindow` sealed class: Type-safe window definitions

**Usage:**
- Use the Window menu to open different screens
- File → New Entry in Window: Opens a new editor window
- Each window has its own ViewModels via Koin injection
- Windows can be closed independently without affecting others

**Architecture:**
```kotlin
sealed class DesktopWindow(
    val id: String = UUID.randomUUID().toString(),
    val singleInstance: Boolean = false
) {
    data class Editor(entryId: String?, isNewEntry: Boolean)
    data object Calendar : DesktopWindow(singleInstance = true)
    data object Statistics : DesktopWindow(singleInstance = true)
    // ... other window types
}
```

#### **Desktop-Only Potential Enhancements** 💡

**Planned Future Features:**
1. **System Tray Integration**
   - Quick capture from system tray
   - Notification support for reminders
   - Global keyboard shortcuts

2. **Enhanced Keyboard Shortcuts**
   - Ctrl/Cmd+N: New entry in new window
   - Ctrl/Cmd+S: Save entry
   - Ctrl/Cmd+F: Search
   - Ctrl/Cmd+E: Export
   - Ctrl/Cmd+W: Close current window
   - Ctrl/Cmd+Shift+W: Close all windows

3. **Advanced Multi-Window Features**
   - Drag-and-drop between windows
   - Window state persistence (remember positions/sizes)
   - Tabbed windows option

4. **Desktop-Specific Features**
   - Markdown file import from file system
   - Drag-and-drop image attachment
   - Integration with desktop search (Spotlight/Windows Search)

---

### Platform Recommendation

**For Desktop Users:**

Desktop version is **fully functional** for journaling with the following notes:

✅ **Use Desktop If:**
- You prefer typing on a full keyboard
- You want larger screen for writing
- You need direct file system access
- You work primarily from a computer

⚠️ **Desktop Limitations (Expected):**
- No camera for photo capture (use file picker instead)
- No GPS location tagging (manual entry if needed)
- No biometric unlock (PIN works great)
- No daily notification reminders (set system reminder separately)

**Bottom Line:** Desktop version has complete feature parity for core diary functionality. The missing features are hardware-dependent and not critical for a great journaling experience.

---

*This document reflects the current implementation as of Phase 9 (User Engagement & Analytics). Phases 1-6 are fully implemented. Phase 6 Cloud Sync now uses native Google Identity Services (AuthorizationClient) on Android for Google Drive OAuth. Phase 7 (Day One and Joplin import) is functional on all platforms including iOS. Phase 7.5 (WYSIWYG Markdown Editor), Phase 8 (API-Based AI Integration), and Phase 9 (Analytics & In-App Review) are completed. Brainstorm chat history is now persistent via SQLDelight. Desktop multi-window support has been implemented with menu bar integration.*

---

## Known Limitations & Pending Work

### Critical UX Improvements

**High Priority:**
1. ~~**Unsaved Changes Dialog on Editor Back Button**~~ ✅ **COMPLETED**
   - BackHandler + top bar back button detect unsaved changes
   - AlertDialog with Save/Discard/Cancel options
   - Change detection via `checkForChanges()` comparing current state with original entry

### Phase 7 Remaining Tasks

**Completed:**
1. ~~**iOS TarExtractor Implementation**~~ ✅ **COMPLETED**
   - Implemented using pure Kotlin TAR parser (no external dependencies)
   - Supports POSIX ustar format with prefix field for long paths
   - Joplin import now works on all platforms including iOS

2. ~~**Joplin Image Resource Parsing**~~ ✅ **COMPLETED**
   - Parses `![](:/resource_id)` references in Joplin markdown note bodies
   - Maps old resource IDs to new image IDs via `resourceIdMap`
   - Removes inline resource references from content (images display in gallery)
   - Improved image file matching for Joplin resources (no-extension files)

**Remaining:**
3. **Real-world Testing**
   - Test with actual Day One export files from users
   - Test with actual Joplin JEX exports
   - Validate edge cases and large imports

4. **Performance Optimization**
   - Test with 1000+ entry imports
   - Optimize batch database operations
   - Add progress indicators for long-running imports

### Phase 8 Remaining Tasks

**Completed:**
1. ~~**Brainstorming Chat Interface**~~ ✅ **COMPLETED**
   - Full chat UI with message bubbles and starter prompts
   - AI integration with copy and insert-to-entry functionality

2. ~~**Persistent Conversation History**~~ ✅ **COMPLETED**
   - `BrainstormMessageEntity` table in SQLDelight (id, content, is_user, timestamp)
   - Schema migration (v2→v3) for existing users
   - Repository methods: `getBrainstormMessages()` (reactive Flow), `insertBrainstormMessage()`, `deleteAllBrainstormMessages()`
   - ViewModel driven by Flow — messages persist across screen navigation and app restarts
   - "Clear Chat" deletes all persisted messages

**Remaining:**
3. **Advanced Features**
   - [ ] Token counting and cost estimation
   - [ ] Diff view for AI suggestions (side-by-side comparison)
   - [ ] Undo/redo for AI suggestions

4. **Enhancements**
   - [ ] Context awareness (reference previous diary entries)
   - [ ] Multi-language support for AI prompts
   - [ ] Advanced prompt templates library
