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

## Future Features

### Phase 7 - Advanced
- [ ] Image attachments
- [ ] Location tagging
- [ ] Widgets (Android)
- [ ] Local encryption (AES-256) for data at rest

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

*This document reflects the current implementation as of Phase 6 completion. All phases 1-6 are fully implemented, including Cloud Sync with Google Drive and Dropbox support.*
