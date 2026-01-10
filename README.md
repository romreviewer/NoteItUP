<p align="center">
  <img src="composeApp/src/androidMain/ic_launcher-playstore.png" width="120" alt="NoteItUP App Icon"/>
</p>

# NoteItUP - AI-Powered Personal Diary

✨ **Your Personal Journal with AI Writing Assistant**

A modern, intelligent diary application that helps you write better with **AI-powered text improvement**. Built with **Kotlin Multiplatform** and **Compose Multiplatform**, targeting Android, iOS, and Desktop (JVM).

**🤖 AI Features:**
- 8 AI improvement types for your writing
- Support for 6+ AI providers (OpenAI, Claude, Gemini, Groq, and more)
- Free AI options available (Groq & Gemini)
- Privacy-first: Your API keys stay on your device

## Screenshots

<p align="center">
  <img src="screenshots/home_screen.png" width="200" alt="Home Screen"/>
  <img src="screenshots/editor_screen.png" width="200" alt="Editor Screen"/>
  <img src="screenshots/calendar_screen.png" width="200" alt="Calendar Screen"/>
  <img src="screenshots/settings_screen.png" width="200" alt="Settings Screen"/>
</p>

## Features

### 🤖 AI-Powered Writing Assistant

Transform your journaling with intelligent AI assistance:

- **8 Improvement Types** - Enhance your writing with one tap:
  - 📝 **Improve for Journal** - Optimize entries for personal reflection
  - ✅ **Fix Grammar** - Correct spelling and grammar errors
  - 💡 **Improve Clarity** - Make your thoughts clearer and more organized
  - ✂️ **Make Shorter** - Condense while keeping key points
  - 📖 **Expand** - Add depth and detail to your entries
  - 👔 **Professional Tone** - Convert to formal writing style
  - 😊 **Casual Tone** - Make it conversational and relaxed
  - 📋 **Summarize** - Create concise summaries of long entries

- **Multiple AI Providers** - Choose what works best for you:
  - OpenAI (GPT-4o, GPT-4o-mini, GPT-3.5-turbo)
  - Anthropic Claude (3.5 Sonnet, Haiku)
  - Google Gemini (2.0 Flash) - **FREE tier available**
  - Groq (Llama 3.x) - **FREE & FAST - Recommended!**
  - OpenRouter (100+ models, some free)
  - Together AI ($25 free credit for new users)

- **Privacy & Security First**:
  - 🔐 **Your keys, your control** - API keys stored securely on your device
  - 🛡️ **Platform-specific encryption** - Protected using system keychains
  - 🚫 **No tracking** - Your journal content never leaves your device (except to your chosen AI provider)
  - ✅ **Easy testing** - Test connection before using any provider

- **Smart Integration**:
  - One-tap improvements directly in the editor
  - Select text to improve specific sections
  - AI toolbar for quick access to all improvement types
  - Real-time suggestions

### Core Journaling Features

- **Rich Text Editor** - Write diary entries with Markdown support
  - Bold, Italic, Headers (H1, H2)
  - Bullet lists, Numbered lists
  - Blockquotes, Code blocks, Links
  - Live preview mode

- **Mood Tracking** - Track your emotional state with mood indicators
  - Amazing, Good, Neutral, Sad, Terrible moods
  - Visual mood emojis on entries

- **Organization**
  - **Folders** - Organize entries into custom folders with colors
  - **Tags** - Add multiple tags to entries for easy filtering
  - **Favorites** - Mark important entries as favorites

### More Advanced Features

- **Image Attachments** - Attach photos to diary entries
  - Pick from gallery or capture with camera
  - Automatic image compression (max 1920px, JPEG 85%)
  - Thumbnail previews

- **Location Tagging** - Add GPS coordinates to entries
  - Automatic reverse geocoding for address display
  - Location permission handling

- **Calendar View** - Browse entries by date
  - Visual indicators for days with entries
  - Quick navigation to specific dates

- **Search** - Full-text search across all entries

- **Statistics Dashboard**
  - Total entries count
  - Writing streak tracking
  - Mood distribution
  - Tags and favorites count

### Security
- **PIN Protection** - Lock app with 4-digit PIN
- **Biometric Authentication** - Fingerprint/Face unlock support

### Data Management
- **Export/Import** - Backup and restore entries (JSON format)
- **Cloud Sync** - Backup to cloud providers
  - Dropbox integration (available)
  - Google Drive (coming soon)
  - Auto-sync with configurable intervals

## Tech Stack

- **Kotlin Multiplatform** - Shared business logic across platforms
- **Compose Multiplatform** - Shared UI across Android, iOS, Desktop
- **SQLDelight** - Type-safe SQL with multiplatform support
- **Koin** - Dependency injection
- **Kotlin Coroutines & Flow** - Asynchronous programming
- **Ktor Client** - Cross-platform HTTP client for AI APIs and cloud sync
- **kotlinx.serialization** - JSON parsing for API requests/responses
- **Material Design 3** - Modern UI components
- **Richeditor-compose** - WYSIWYG markdown editor

## Project Structure

```
composeApp/src/
├── commonMain/          # Shared code for all platforms
│   ├── kotlin/
│   │   ├── data/        # Repository implementations, database
│   │   ├── domain/      # Models, use cases, repository interfaces
│   │   └── presentation/# Screens, ViewModels, UI components
│   └── sqldelight/      # Database schema and queries
├── androidMain/         # Android-specific implementations
├── iosMain/             # iOS-specific implementations
└── jvmMain/             # Desktop-specific implementations
```

## Platform Support

NoteItUP is built with **Kotlin Multiplatform** and runs on Android, iOS, and Desktop (Windows/Mac/Linux).

### ✅ Available on All Platforms

All core features work across all platforms:
- ✅ Complete diary editing (entries, folders, tags, moods)
- ✅ WYSIWYG rich text editor with Markdown
- ✅ Search, calendar, and statistics
- ✅ Import/Export (JSON, CSV, Markdown, Day One, Joplin)
- ✅ Image attachments with thumbnails
- ✅ AI Writing Assistant (8 improvement types, 6 providers)
- ✅ Cloud sync (Dropbox, Google Drive) with encryption
- ✅ PIN security with auto-lock
- ✅ Light/Dark themes with accent colors

### Platform-Specific Features

| Feature | Android | Desktop (JVM) | iOS |
|---------|---------|---------------|-----|
| Multi-Window Support | ❌ | ✅ Full support with menu bar | ❌ |
| Camera Photo Capture | ✅ | ❌ (use file picker) | ✅ |
| Biometric Unlock | ✅ Fingerprint/Face | ❌ (PIN only) | ✅ Face ID/Touch ID |
| GPS Location Tagging | ✅ | ❌ | ✅ |
| Daily Reminders | ✅ | ❌ (planned) | ✅ |
| Chrome Custom Tabs | ✅ | Standard browser | Safari |

**Desktop users:** All core journaling features work perfectly on Desktop. The Desktop version includes unique features like multi-window support and menu bar integration. The missing features are hardware-dependent (camera, GPS, biometrics) and don't affect the journaling experience.

## Building the Project

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17+
- Xcode 15+ (for iOS)

### Android
```shell
# Debug build
./gradlew :composeApp:assembleDebug

# Install on connected device
./gradlew :composeApp:installDebug
```

### Desktop (JVM)
```shell
./gradlew :composeApp:run
```

### iOS
Open `/iosApp` in Xcode and run, or use:
```shell
./gradlew :composeApp:iosSimulatorArm64Main
```

## Architecture

The app follows **Clean Architecture** with **MVI (Model-View-Intent)** pattern:

- **Domain Layer** - Business logic, models, use cases
- **Data Layer** - Repository implementations, local database, platform services
- **Presentation Layer** - Compose UI, ViewModels, UI state management

### Key Patterns
- `expect/actual` for platform-specific implementations
- Unidirectional data flow with StateFlow
- Repository pattern for data access

## AI Assistant Setup

The app supports AI-powered text improvement using your own API key (BYOK - Bring Your Own Key). This gives you full control over your AI usage and costs.

### Recommended: Groq (Free & Fast)
1. Visit [Groq Console](https://console.groq.com/keys)
2. Sign up for a free account
3. Create an API key
4. In NoteItUP: Settings → AI Settings → Select "Groq" → Paste API key
5. Tap "Test Connection" to verify

### Alternative Providers

**Google Gemini (Free Tier)**
- Get key: https://aistudio.google.com/apikey
- Free tier: 15 requests/minute, 1,500 requests/day
- Model: gemini-2.0-flash

**OpenAI (Paid)**
- Get key: https://platform.openai.com/api-keys
- Pricing: $0.15-$5 per million tokens
- Models: GPT-4o, GPT-4o-mini, GPT-3.5-turbo

**Anthropic Claude (Paid)**
- Get key: https://console.anthropic.com/settings/keys
- Pricing: $3-$15 per million tokens
- Models: Claude 3.5 Sonnet, Haiku

**OpenRouter (Some Free Models)**
- Get key: https://openrouter.ai/keys
- Access to 100+ models, some free

**Together AI ($25 Free Credit)**
- Get key: https://api.together.xyz/settings/api-keys
- $25 credit for new users

## Cloud Sync Setup

### Dropbox Setup
1. Create an app at [Dropbox App Console](https://www.dropbox.com/developers/apps)
2. Choose "Scoped access" and "App folder"
3. In Permissions tab, enable `files.content.write` and `files.content.read`
4. In Settings tab, add OAuth2 Redirect URI: `com.romreviewertools.noteitup://oauth2callback`
5. Copy `ApiKeys.kt.template` to `ApiKeys.kt` and add your App Key and Secret

### Google Drive Setup (Coming Soon)
Instructions will be added when Google Drive integration is enabled.

## Firebase Setup (Analytics & Crashlytics)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing one
3. Add an Android app with package name: `com.romreviewertools.noteitup`
4. Download `google-services.json` and place it in `composeApp/` directory
5. Enable **Analytics** and **Crashlytics** in Firebase Console

The app uses Firebase for:
- **Analytics**: Track user events and screen views
- **Crashlytics**: Crash reporting and error tracking

Note: `google-services.json` is gitignored for security. See `google-services.json.template` for the expected structure.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

Built with Kotlin Multiplatform & Compose Multiplatform
