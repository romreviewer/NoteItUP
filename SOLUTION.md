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
| Splash Screen | androidx.core:core-splashscreen | 1.0.1 |

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
androidx-splashscreen = "1.0.1"
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
- [x] Android 12+ Splash Screen (eliminates white screen on cold start)

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

### Android 12+ Splash Screen (Completed)

**Eliminates white screen flash on cold start using the AndroidX SplashScreen compat library.**

**Problem:** On app launch, a white screen was visible before Compose content rendered, causing a poor first impression.

**Solution:** Implemented the `androidx.core:core-splashscreen` library which provides a consistent branded splash screen across all Android versions (API 24+).

**Implementation:**

| Component | File | Change |
|-----------|------|--------|
| Dependency | `libs.versions.toml` | Added `androidx-splashscreen = "1.0.1"` |
| Build config | `build.gradle.kts` | Added `implementation(libs.androidx.splashscreen)` |
| Theme | `res/values/themes.xml` | New splash theme with purple background (#582486) and app icon |
| Manifest | `AndroidManifest.xml` | Changed app theme to `@style/Theme.NoteItUP.Splash` |
| Activity | `MainActivity.kt` | Added `installSplashScreen()` before `super.onCreate()` |

**How it works:**
- **Android 12+ (API 31+):** Delegates to the native system splash screen API, showing the adaptive icon with purple background
- **Android 11 and below:** The compat library recreates the same splash screen behavior using a themed window background
- `installSplashScreen()` must be called before `super.onCreate()` to properly transition from the splash theme to app content
- `postSplashScreenTheme` restores the original `Theme.Material.Light.NoActionBar` after splash completes

**Splash Theme Configuration:**
```xml
<style name="Theme.NoteItUP.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">#582486</item>
    <item name="windowSplashScreenAnimatedIcon">@mipmap/ic_launcher_foreground</item>
    <item name="postSplashScreenTheme">@android:style/Theme.Material.Light.NoActionBar</item>
</style>
```

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

### Phase 8.5 - Local AI with Gemma 4 (On-Device Inference)

**Private, offline AI-powered writing assistance using Google's Gemma 4 model running entirely on-device via LiteRT-LM.**

**Status:** Planned

#### Overview

Adds a new `LOCAL_GEMMA` AI provider alongside existing cloud-based BYOK providers. Users can download and run Gemma 4 E2B (2 billion parameters) directly on their device -- no internet required, no API key needed, complete privacy for diary content.

**Key Value:** Personal diary entries never leave the device for AI processing.

#### Recommended Model
- **Model:** Gemma 4 E2B (2B parameters)
- **Format:** `.litertlm` (LiteRT-LM optimized)
- **Size:** ~1.6 GB download
- **RAM:** Requires 8GB+ system RAM
- **Why:** Optimized for mobile edge inference, good text-refinement quality at small size

#### Inference Library: LiteRT-LM

**Why LiteRT-LM over MediaPipe GenAI:**
- Newer, actively maintained by Google AI Edge team
- Stable Kotlin API with Gradle dependencies for both Android and JVM
- GPU and NPU hardware acceleration
- Streaming responses via Kotlin Flow
- Powers the official Google AI Edge Gallery app
- Supports Gemma 4 natively

**Dependencies:**
```gradle
// Android
implementation("com.google.ai.edge.litertlm:litertlm-android:0.10.0")

// JVM (Desktop)
implementation("com.google.ai.edge.litertlm:litertlm-jvm:0.10.0")
```

**Android Manifest (GPU support):**
```xml
<application>
    <uses-native-library android:name="libOpenCL.so" android:required="false"/>
    <uses-native-library android:name="libvndksupport.so" android:required="false"/>
</application>
```

#### Platform Support

| Platform | Status | Backend | Notes |
|----------|--------|---------|-------|
| Android | Planned | GPU (primary), CPU (fallback) | Full support, primary target |
| JVM (Desktop) | Planned | CPU | Full support |
| iOS | Stub | N/A | LiteRT-LM Swift API not yet stable ("Coming Soon") |

#### Architecture

```
User selects "Local (Gemma 4)" in AI Settings
    |
    v
AISettingsScreen shows Model Management UI (not API key)
    |
    v
Model Acquisition (two options):
  a) In-app download from HuggingFace --> app storage
  b) Select existing .litertlm file via file picker
    |
    v
AIService.makeRequest() detects LOCAL_GEMMA --> delegates to LocalInferenceEngine
    |
    v
LocalInferenceEngine (expect/actual):
  - Android: LiteRT-LM Engine with GPU backend
  - JVM: LiteRT-LM Engine with CPU backend
  - iOS: Stub (throws "not available")
    |
    v
Response --> same AIToolbar / AISuggestionDialog UX as cloud providers
```

#### Implementation Plan

**Step 1: Dependencies & Build Configuration**

Files to modify:
- `gradle/libs.versions.toml` -- Add `litertlm` version and library entries
- `composeApp/build.gradle.kts` -- Add platform-specific LiteRT-LM dependencies
- `composeApp/src/androidMain/AndroidManifest.xml` -- Add GPU native library declarations

```toml
# libs.versions.toml
[versions]
litertlm = "0.10.0"

[libraries]
litertlm-android = { module = "com.google.ai.edge.litertlm:litertlm-android", version.ref = "litertlm" }
litertlm-jvm = { module = "com.google.ai.edge.litertlm:litertlm-jvm", version.ref = "litertlm" }
```

**Step 2: Update AIProvider Enum**

File: `domain/model/AIProvider.kt`

Add `LOCAL_GEMMA` entry with helper properties:

```kotlin
LOCAL_GEMMA(
    displayName = "Local (Gemma 4)",
    baseUrl = "",  // Not used for local inference
    hasFreeTier = true,
    description = "Gemma 4 E2B - Private, on-device AI. No internet needed. Requires 8GB+ RAM.",
    apiKeyUrl = ""  // Not used
)

// Helper properties
val isLocal: Boolean get() = this == LOCAL_GEMMA
val requiresApiKey: Boolean get() = !isLocal
```

**Step 3: Add `hasLocalAISupport()` to PlatformCapabilities**

Files (expect/actual pattern):
- `commonMain/.../util/PlatformCapabilities.kt` -- Add `fun hasLocalAISupport(): Boolean`
- `androidMain` -- Returns `true`
- `jvmMain` -- Returns `true`
- `iosMain` -- Returns `false`

Used to hide the `LOCAL_GEMMA` provider on platforms that don't support it.

**Step 4: Create LocalInferenceEngine (expect/actual)**

New files:
| File | Description |
|------|-------------|
| `commonMain/.../data/ai/LocalInferenceEngine.kt` | Expect class definition |
| `androidMain/.../data/ai/LocalInferenceEngine.android.kt` | LiteRT-LM with GPU backend |
| `jvmMain/.../data/ai/LocalInferenceEngine.jvm.kt` | LiteRT-LM with CPU backend |
| `iosMain/.../data/ai/LocalInferenceEngine.ios.kt` | Stub (throws UnsupportedOperationException) |

Common interface:
```kotlin
expect class LocalInferenceEngine {
    suspend fun loadModel(modelPath: String)
    fun unloadModel()
    fun isModelLoaded(): Boolean
    suspend fun generateResponse(
        systemPrompt: String,
        userMessage: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 1024
    ): String
    suspend fun generateResponseStream(
        systemPrompt: String,
        userMessage: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 1024
    ): Flow<String>
}
```

Android actual (key details):
```kotlin
actual class LocalInferenceEngine(private val context: Context) {
    private var engine: Engine? = null

    actual suspend fun loadModel(modelPath: String) = withContext(Dispatchers.IO) {
        val config = EngineConfig(
            modelPath = modelPath,
            backend = Backend.GPU(),  // GPU acceleration
            cacheDir = context.cacheDir.path
        )
        engine = Engine(config).also { it.initialize() }
    }

    actual suspend fun generateResponse(...): String = withContext(Dispatchers.IO) {
        val conv = engine!!.createConversation(
            ConversationConfig(
                systemInstruction = Contents.of(systemPrompt),
                samplerConfig = SamplerConfig(temperature = temperature)
            )
        )
        conv.use { it.sendMessage(userMessage).text }
    }

    actual suspend fun generateResponseStream(...): Flow<String> = flow {
        val conv = engine!!.createConversation(...)
        conv.sendMessageAsync(userMessage).collect { emit(it.text) }
    }.flowOn(Dispatchers.IO)
}
```

JVM actual: Same structure but `Backend.CPU()` instead of GPU.

**Step 5: Create ModelDownloadManager (expect/actual)**

New files:
| File | Description |
|------|-------------|
| `commonMain/.../data/ai/ModelDownloadManager.kt` | Expect class + ModelInfo, ModelDownloadState |
| `androidMain/.../data/ai/ModelDownloadManager.android.kt` | HuggingFace download + file picker import |
| `jvmMain/.../data/ai/ModelDownloadManager.jvm.kt` | HuggingFace download + JFileChooser import |
| `iosMain/.../data/ai/ModelDownloadManager.ios.kt` | Stub |

Common types:
```kotlin
data class ModelInfo(
    val name: String,           // "Gemma 4 E2B"
    val fileName: String,       // "gemma-4-E2B-it.litertlm"
    val sizeBytes: Long,        // ~1.6GB
    val downloadUrl: String,    // HuggingFace URL
    val description: String
)

sealed class ModelDownloadState {
    data object NotDownloaded : ModelDownloadState()
    data class Downloading(val progress: Float) : ModelDownloadState()
    data object Downloaded : ModelDownloadState()
    data class Error(val message: String) : ModelDownloadState()
}

enum class ModelSource { NONE, DOWNLOADED, IMPORTED }

expect class ModelDownloadManager {
    fun getDownloadState(): StateFlow<ModelDownloadState>
    suspend fun downloadModel(modelInfo: ModelInfo)
    fun cancelDownload()
    suspend fun deleteModel()
    fun getModelPath(): String?
    fun getAvailableModels(): List<ModelInfo>
    suspend fun importModel(externalPath: String): Result<String>
    suspend fun validateModelFile(path: String): Boolean
}
```

Model acquisition paths:

| Path | Flow | When to Use |
|------|------|-------------|
| **In-app download** | Tap "Download Model" -> HuggingFace download with progress -> stored in app dir | First-time users, easiest UX |
| **Select from device** | Tap "Select File" -> file picker -> validate -> copy to app dir | User already has `.litertlm` from AI Edge Gallery, manual download, or previous install |

Both paths store the model in app-managed directory:
- Android: `context.filesDir/models/`
- JVM: `~/.noteitup/models/`

Hardcoded model entry:
```kotlin
val GEMMA_4_E2B = ModelInfo(
    name = "Gemma 4 E2B",
    fileName = "gemma-4-E2B-it.litertlm",
    sizeBytes = 1_600_000_000L,
    downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
    description = "2B params. Best for 8GB+ RAM devices. Fast, private text improvement."
)
```

**Step 6: Update AIService for Local Routing**

File: `data/ai/AIService.kt`

- Accept `LocalInferenceEngine` as new constructor dependency
- Add `LOCAL_GEMMA` branch in `makeRequest()` that bypasses HTTP and delegates to `LocalInferenceEngine`
- Skip `apiKey.isBlank()` validation when provider is local
- Update `testConnection()` to test local model inference
- Update `getDefaultModel()` and `buildApiUrl()` for new enum entry

```kotlin
class AIService(
    private val httpClient: HttpClient,
    private val aiSettingsRepository: AISettingsRepository,
    private val localInferenceEngine: LocalInferenceEngine
) {
    suspend fun improveText(text: String, improvementType: ImprovementType): Result<String> {
        val settings = aiSettingsRepository.aiSettings.firstOrNull() ?: ...
        if (settings.selectedProvider == AIProvider.LOCAL_GEMMA) {
            return makeLocalRequest(improvementType.systemPrompt, text)
        }
        // ... existing cloud path ...
    }

    private suspend fun makeLocalRequest(systemPrompt: String, userMessage: String): Result<String> {
        if (!localInferenceEngine.isModelLoaded()) {
            return Result.failure(Exception("Local model not loaded"))
        }
        val response = localInferenceEngine.generateResponse(systemPrompt, userMessage)
        return Result.success(response.trim())
    }
}
```

**Step 7: Update AISettingsRepository**

File: `data/repository/AISettingsRepository.kt`

Minor: skip API key requirement when provider is `LOCAL_GEMMA`.

**Step 8: Update AI Settings UI**

File: `presentation/screens/aisettings/AISettingsScreen.kt`

When `LOCAL_GEMMA` is selected, replace API Key card with Model Management card:

```
+--------------------------------------------+
|  AI Provider: [Local (Gemma 4)]  v         |
|                                            |
|  "Private, on-device AI. No internet       |
|   needed. Requires 8GB+ RAM."              |
|  (check) Free - no API key needed          |
+--------------------------------------------+
|  Model: Gemma 4 E2B (~1.6 GB)             |
|                                            |
|  Status: Not Downloaded                    |
|  [=========>          ] 45%                |
|                                            |
|  [Download Model]  [Select from Device]    |
|                                            |
|  (i) Supported format: .litertlm          |
|  (!) Requires 8GB+ RAM                    |
+--------------------------------------------+
|  [Test Model]                              |
+--------------------------------------------+
```

After model is available:
```
+--------------------------------------------+
|  Model: Gemma 4 E2B                        |
|  (check) Ready (1.6 GB)                   |
|                                            |
|  [Test Model] [Delete] [Change File]       |
|  (i) Model runs entirely on your device    |
+--------------------------------------------+
```

Logic:
- `provider.isLocal` --> show Model Management card, hide API Key card
- `provider.requiresApiKey` --> show API Key card (existing behavior)
- Filter `LOCAL_GEMMA` from dropdown when `PlatformCapabilities.hasLocalAISupport()` is false (iOS)

**Step 9: Update AISettings ViewModel & Intents**

New intents:
```kotlin
data object DownloadModel : AISettingsIntent
data object CancelDownload : AISettingsIntent
data object DeleteModel : AISettingsIntent
data object LoadModel : AISettingsIntent
data object SelectModelFile : AISettingsIntent
data class ModelFileSelected(val path: String) : AISettingsIntent
```

New state fields:
```kotlin
data class AISettingsUiState(
    // ... existing ...
    val modelDownloadState: ModelDownloadState = ModelDownloadState.NotDownloaded,
    val isModelLoaded: Boolean = false,
    val modelSource: ModelSource = ModelSource.NONE
)
```

**Step 10: Update DI Module**

File: `di/AppModule.kt`

```kotlin
val aiModule = module {
    singleOf(::AISettingsRepository)
    singleOf(::LocalInferenceEngine)      // NEW
    singleOf(::ModelDownloadManager)       // NEW
    singleOf(::AIService)                  // Now takes LocalInferenceEngine
    factoryOf(::ImproveTextUseCase)
    factoryOf(::ChatUseCase)
}
```

**Step 11: Update ChatUseCase**

File: `domain/usecase/ChatUseCase.kt`

Update `isConfigured()` to not require API key for `LOCAL_GEMMA`:
```kotlin
suspend fun isConfigured(): Boolean {
    val settings = aiSettingsRepository.aiSettings.firstOrNull() ?: return false
    if (!settings.enabled) return false
    if (settings.selectedProvider.isLocal) return true
    return settings.apiKey.isNotBlank()
}
```

#### Model Download: Android Foreground Service

The ~1.6GB model download uses an Android **ForegroundService** to ensure the download:
- Survives screen navigation and app backgrounding
- Shows a persistent notification with progress and cancel action
- Can be cancelled reliably from both the notification and the UI

**Why not Play Asset Delivery?**
- Model file would need to be in the AAB (2GB+ build artifacts, slow builds)
- Model updates would require a new app release
- Only works on Play Store (not F-Droid, sideloading, or Desktop)
- The "Select File" import feature wouldn't work with Play-managed assets

**Download Architecture:**
```
User taps "Download"
    |
    v
ModelDownloadManager.downloadModel()
    |
    v
context.startForegroundService(intent) --> ModelDownloadService starts
    |
    v
ModelDownloadService.onStartCommand():
  - Creates notification channel "model_download" (IMPORTANCE_LOW)
  - Shows foreground notification with progress bar + cancel action
  - Gets ModelDownloadManager from Koin (shared singleton)
  - Downloads via Ktor HttpClient from HuggingFace
  - Updates ModelDownloadManager._downloadState (shared StateFlow)
  - UI observes the same StateFlow --> progress bar updates
    |
    v
On complete: state = Downloaded, stopForeground(), stopSelf()
On cancel:   state = NotDownloaded, delete temp file, stopSelf()
On error:    state = Error, stopSelf()
```

**Notification UX:**
```
+------------------------------------------+
| NoteItUP                                 |
| Downloading Gemma 4 E2B... 45%           |
| [========>                    ]           |
|                              [Cancel]    |
+------------------------------------------+
```

**Components:**

| Component | File | Description |
|-----------|------|-------------|
| `ModelDownloadService` | `androidMain/.../data/ai/ModelDownloadService.kt` | Android ForegroundService for background model download |

**JVM (Desktop):** Uses its own `CoroutineScope(SupervisorJob())` for cancellable downloads (no service needed).

**Android Manifest additions:**
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

<service
    android:name=".data.ai.ModelDownloadService"
    android:exported="false"
    android:foregroundServiceType="dataSync" />
```

#### Components Summary

| Component | File | Description |
|-----------|------|-------------|
| `LocalInferenceEngine` | `data/ai/LocalInferenceEngine.kt` | expect/actual for on-device LLM inference via LiteRT-LM |
| `ModelDownloadManager` | `data/ai/ModelDownloadManager.kt` | expect/actual for model download, import, and lifecycle |
| `ModelDownloadService` | `androidMain data/ai/ModelDownloadService.kt` | Android ForegroundService for background model download |
| `ModelInfo` | `data/ai/ModelDownloadManager.kt` | Data class describing available models |
| `ModelDownloadState` | `data/ai/ModelDownloadManager.kt` | Sealed class for download progress tracking |
| `AIProvider.LOCAL_GEMMA` | `domain/model/AIProvider.kt` | New enum entry for local inference provider |

#### Files to Create

| File | Purpose |
|------|---------|
| `commonMain/.../data/ai/LocalInferenceEngine.kt` | Expect class for on-device inference |
| `androidMain/.../data/ai/LocalInferenceEngine.android.kt` | LiteRT-LM GPU implementation |
| `jvmMain/.../data/ai/LocalInferenceEngine.jvm.kt` | LiteRT-LM CPU implementation |
| `iosMain/.../data/ai/LocalInferenceEngine.ios.kt` | Stub implementation |
| `commonMain/.../data/ai/ModelDownloadManager.kt` | Expect class for model lifecycle |
| `androidMain/.../data/ai/ModelDownloadManager.android.kt` | Android service-based download + file picker import |
| `androidMain/.../data/ai/ModelDownloadService.kt` | Android ForegroundService for model download |
| `jvmMain/.../data/ai/ModelDownloadManager.jvm.kt` | JVM download with own CoroutineScope |
| `iosMain/.../data/ai/ModelDownloadManager.ios.kt` | Stub |

#### Files to Modify

| File | Changes |
|------|---------|
| `gradle/libs.versions.toml` | Add litertlm version + library entries |
| `composeApp/build.gradle.kts` | Add platform-specific LiteRT-LM dependencies |
| `AndroidManifest.xml` | Add GPU native libs + foreground service permission + service declaration |
| `domain/model/AIProvider.kt` | Add `LOCAL_GEMMA` entry + `isLocal`/`requiresApiKey` properties |
| `data/ai/AIService.kt` | Add local inference routing, accept `LocalInferenceEngine` |
| `data/repository/AISettingsRepository.kt` | Skip API key validation for local provider |
| `util/PlatformCapabilities.kt` (all platforms) | Add `hasLocalAISupport()` |
| `di/AppModule.kt` | Register `LocalInferenceEngine`, `ModelDownloadManager` |
| `presentation/screens/aisettings/AISettingsScreen.kt` | Conditional UI for local vs cloud provider |
| `presentation/screens/aisettings/AISettingsIntent.kt` | Add download/load/delete/select intents |
| `presentation/screens/aisettings/AISettingsUiState.kt` | Add model download state fields |
| `presentation/screens/aisettings/AISettingsViewModel.kt` | Handle new intents |
| `domain/usecase/ChatUseCase.kt` | Update `isConfigured()` for local provider |

#### LiteRT-LM API Usage (Reference)

```kotlin
import com.google.ai.edge.litertlm.*

// 1. Initialize engine (takes ~5-10s, run on background thread)
val engineConfig = EngineConfig(
    modelPath = "/path/to/gemma-4-E2B-it.litertlm",
    backend = Backend.GPU(),  // or Backend.CPU()
    cacheDir = context.cacheDir.path
)
val engine = Engine(engineConfig)
engine.initialize()

// 2. Create conversation with system instruction
val conversation = engine.createConversation(
    ConversationConfig(
        systemInstruction = Contents.of("You are a helpful writing assistant..."),
        samplerConfig = SamplerConfig(topK = 10, topP = 0.95, temperature = 0.7)
    )
)

// 3a. Synchronous response
val response = conversation.sendMessage("Improve this text: ...")
println(response.text)

// 3b. Streaming response via Flow
conversation.sendMessageAsync("Improve this text: ...")
    .collect { chunk -> print(chunk.text) }

// 4. Cleanup
conversation.close()
engine.close()
```

#### Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Model file ~1.6GB | Storage-heavy for users | Clear size warning in UI, easy delete option |
| LiteRT-LM init takes ~5-10s | Perceived latency | Load on first use, keep in memory, show loading indicator |
| 2B model quality vs cloud LLMs | Lower quality suggestions | Set appropriate expectations in UI ("fast, private, basic improvements") |
| GPU not available on all Android | Slower inference | Fallback to CPU backend with warning |
| HuggingFace download may fail | Poor download experience | Retry logic, resume support, progress indicator, cancel option |
| User has model from elsewhere | Need to import | File picker option to select existing `.litertlm` file |

#### Privacy Advantages

- All AI inference happens on-device -- zero network requests for text processing
- No API keys stored, no third-party account needed
- Diary content never leaves the device for AI operations
- Model runs completely offline after download
- Aligns with NoteItUP's "Privacy First" design philosophy

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
| AI Writing Assistant | ✅ | ✅ | ✅ | 8 improvement types, 6 cloud providers (Groq, OpenAI, etc.) |
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
| Local AI (Gemma 4) | ✅ LiteRT-LM GPU | ✅ LiteRT-LM CPU | ❌ Not yet available | LiteRT-LM Swift API coming soon |

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

*This document reflects the current implementation as of Phase 9 (User Engagement & Analytics). Phases 1-6 are fully implemented. Phase 6 Cloud Sync now uses native Google Identity Services (AuthorizationClient) on Android for Google Drive OAuth. Phase 7 (Day One and Joplin import) is functional on all platforms including iOS. Phase 7.5 (WYSIWYG Markdown Editor), Phase 8 (API-Based AI Integration), and Phase 9 (Analytics & In-App Review) are completed. Phase 8.5 (Local AI with Gemma 4 via LiteRT-LM) is planned for Android and JVM platforms. Brainstorm chat history is now persistent via SQLDelight. Desktop multi-window support has been implemented with menu bar integration.*

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
