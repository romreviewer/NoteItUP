<p align="center">
  <img src="composeApp/src/androidMain/ic_launcher-playstore.png" width="120" alt="NoteItUP App Icon"/>
</p>

<h1 align="center">NoteItUP</h1>

<p align="center">
  <strong>AI-Powered Personal Diary & Journal</strong><br>
  Privacy-first journaling with on-device Gemma 4 AI -- your thoughts never leave your phone
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.romreviewertools.noteitup">
    <img src="https://img.shields.io/badge/Google%20Play-Download-green?style=for-the-badge&logo=google-play" alt="Get it on Google Play"/>
  </a>
  <a href="https://github.com/romreviewer/NoteItUP">
    <img src="https://img.shields.io/badge/GitHub-Open%20Source-black?style=for-the-badge&logo=github" alt="View on GitHub"/>
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Gemma_4-On--Device_AI-8E24AA?logo=google&logoColor=white" alt="Gemma 4 On-Device"/>
  <img src="https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin Multiplatform"/>
  <img src="https://img.shields.io/badge/Compose-Multiplatform-4285F4?logo=jetpack-compose&logoColor=white" alt="Compose Multiplatform"/>
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="MIT License"/>
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop-orange" alt="Platforms"/>
</p>

---

## Overview

NoteItUP is a modern, open-source journaling application that combines the simplicity of traditional diary writing with the power of AI assistance. Built with **Kotlin Multiplatform** and **Compose Multiplatform**, it runs natively on Android, iOS, and Desktop while keeping your data secure and private.

**Key Highlights:**
- 🧠 **On-Device AI** - Google Gemma 4 runs entirely on your phone via LiteRT-LM. No internet, no cloud, no data shared
- 🤖 **8 AI Writing Tools** - Grammar, clarity, tone, journaling, summarization + brainstorm chat
- 🔐 **Privacy-First** - Local-first storage, encrypted cloud backups, PIN & biometric lock
- 📱 **Cross-Platform** - One Kotlin codebase for Android, iOS & Desktop
- 🌟 **100% Open Source** - MIT licensed, community-driven

## Screenshots

<p align="center">
  <img src="screenshots/home_screen.png" width="200" alt="Home Screen"/>
  <img src="screenshots/editor_screen.png" width="200" alt="Editor Screen"/>
  <img src="screenshots/calendar_screen.png" width="200" alt="Calendar Screen"/>
  <img src="screenshots/settings_screen.png" width="200" alt="Settings Screen"/>
</p>

---

## On-Device AI with Gemma 4

NoteItUP integrates Google's **Gemma 4 E2B** (2 billion parameter) model for fully private, offline AI writing assistance. Your diary entries never leave your device for AI processing.

### Why On-Device?

A diary is deeply personal. Cloud-based AI means sending your private thoughts to external servers. With on-device inference, the AI model runs directly on your phone's hardware -- no network requests, no third-party access, no data collection. Your thoughts stay yours.

### How It Works

```
You write a diary entry
    |
    v
Tap an AI chip (e.g. "Fix Grammar")
    |
    v
Gemma 4 processes your text locally
  - Android: GPU acceleration via LiteRT-LM
  - Desktop: CPU inference via LiteRT-LM
  - No network call. No API key. No cloud.
    |
    v
Improved text appears in a suggestion dialog
  - Accept to replace, or dismiss to keep original
```

### Technical Details

| Component | Detail |
|-----------|--------|
| **Model** | Gemma 4 E2B -- 2 billion parameters, instruction-tuned |
| **Format** | `.litertlm` (optimized for edge inference) |
| **Runtime** | [LiteRT-LM](https://github.com/google-ai-edge/LiteRT-LM) by Google AI Edge |
| **Android Backend** | GPU (primary) with automatic CPU fallback |
| **Desktop Backend** | CPU |
| **Model Size** | ~1.6 GB download (one-time) |
| **RAM Required** | 8 GB+ system RAM |
| **Load Time** | ~5-10 seconds on first use per session |
| **Inference Time** | ~1-2 seconds per request (after loaded) |
| **Download** | Android system DownloadManager (background, resumable) |

### Lazy Loading -- No Wasted RAM

The model is **not** loaded at app startup. It loads into memory only when you first use an AI feature in a session:

1. **First AI use**: Model loads from disk (~5-10s). Inline status shows "Loading AI model for first use..."
2. **Subsequent uses**: Model is already in memory. Inference is near-instant (~1-2s).
3. **On app exit**: Exit dialog offers to unload the model and free RAM.

This means users who don't use AI features in a session pay zero RAM cost.

### Model Acquisition -- Two Options

| Method | Flow |
|--------|------|
| **Download in-app** | Tap "Download" in AI Settings. Android DownloadManager handles it in the background with a progress notification. Resumable, cancellable. |
| **Import existing file** | Tap "Select File" in AI Settings. Pick any `.litertlm` file from your device (e.g. from Google AI Edge Gallery or a previous install). |

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  AISettingsScreen                                           │
│  - Provider selector (Local Gemma / Cloud providers)        │
│  - Model download/import with progress                      │
│  - Load/unload/test/delete model                            │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  AIService                                                  │
│  - Routes to local or cloud based on provider               │
│  - Lazy auto-loads model on first use if downloaded          │
│  - onModelLoading callback for UI status messages            │
└──────────────────────────┬──────────────────────────────────┘
                           │
           ┌───────────────┴───────────────┐
           │                               │
┌──────────▼──────────┐     ┌──────────────▼──────────────────┐
│  LocalInferenceEngine│     │  Cloud HTTP (Ktor)              │
│  (expect/actual)     │     │  OpenAI / Claude / Gemini / etc │
│                      │     └─────────────────────────────────┘
│  Android: LiteRT-LM  │
│    GPU → CPU fallback │
│  JVM: LiteRT-LM CPU  │
│  iOS: Stub            │
└───────────┬──────────┘
            │
┌───────────▼──────────┐
│  ModelDownloadManager │
│  (expect/actual)      │
│                       │
│  Android: System      │
│    DownloadManager     │
│  JVM: Ktor + Job      │
│  iOS: Stub            │
└───────────────────────┘
```

---

## Features

### 🤖 AI Writing Tools

8 improvement types available in the editor toolbar, powered by local Gemma 4 or cloud providers:

| Type | Description |
|------|-------------|
| 📝 **Improve for Journal** | Optimize entries for personal reflection |
| ✅ **Fix Grammar** | Correct spelling and grammar errors |
| 💡 **Improve Clarity** | Make thoughts clearer and organized |
| ✂️ **Make Shorter** | Condense while keeping key points |
| 📖 **Expand** | Add depth and detail to entries |
| 👔 **Professional Tone** | Convert to formal writing style |
| 😊 **Casual Tone** | Make it conversational and relaxed |
| 📋 **Summarize** | Create concise summaries |

### 💬 Brainstorm Mode
- Interactive AI chat for creative writing
- Journaling prompts and idea generation
- Overcome writer's block
- Persistent conversation history across sessions

### Cloud AI Providers (BYOK)

Prefer cloud models? Bring your own API key. 6 providers supported:

| Provider | Free Tier | Models |
|----------|-----------|--------|
| **Groq** | ✅ Free & Fast | Llama 3.x |
| **Google Gemini** | ✅ 1,500 req/day | Gemini 2.0 Flash |
| **OpenRouter** | ✅ Some models | 100+ models |
| **Together AI** | $25 credit | Various |
| **OpenAI** | ❌ Paid | GPT-4o, GPT-4o-mini |
| **Anthropic** | ❌ Paid | Claude 3.5 Sonnet |

> Cloud providers are fully optional. The default is local Gemma 4 -- no account or API key required.

---

### ✍️ Rich Text Editor

Full **WYSIWYG editor** with formatting toolbar:

- **Text Formatting**: Bold, Italic, Underline, Strikethrough
- **Headings**: H1, H2, H3 levels
- **Lists**: Bullet lists, Numbered lists
- **Blocks**: Blockquotes, Code blocks
- **Links**: Inline hyperlinks
- **Live Preview**: Toggle between edit and preview modes

---

### 😊 Mood Tracking

Track your emotional journey:

- **5 Mood Levels**: Amazing, Good, Neutral, Sad, Terrible
- **Visual Indicators**: Emoji icons on entries
- **Statistics**: Mood distribution charts
- **Trends**: Track mood patterns over time

---

### 📁 Organization

Keep your journal organized:

- **Folders** - Custom folders with color coding
- **Tags** - Multiple tags per entry for flexible categorization
- **Favorites** - Star important entries for quick access
- **Calendar View** - Browse entries by date with visual indicators
- **Search** - Full-text search across all entries

---

### 📸 Media & Location

Enrich your entries:

- **Image Attachments**
  - Pick from gallery or capture with camera
  - Automatic compression (max 1920px, JPEG 85%)
  - Thumbnail previews in entries

- **Location Tagging**
  - GPS coordinates with your entries
  - Automatic address lookup (reverse geocoding)
  - Map integration

---

### 🔐 Security

Your privacy matters:

| Feature | Description |
|---------|-------------|
| **PIN Lock** | 4-6 digit PIN protection |
| **Biometric** | Fingerprint / Face ID unlock |
| **Auto-Lock** | Configurable timeout (immediate to 30 min) |
| **Encrypted Backups** | Password-protected cloud backups |
| **Local Storage** | All data stays on your device |

---

### ☁️ Cloud Sync

Secure backup to cloud providers:

- **Google Drive** - Full backup/restore support
- **Dropbox** - Full backup/restore support
- **End-to-End Encryption** - Backups encrypted with your password
- **Auto-Sync** - Configurable intervals (hourly, daily, weekly)
- **Wi-Fi Only** - Option to sync only on Wi-Fi

---

### 📤 Import & Export

Migrate from other apps or backup locally:

**Export Formats:**
- JSON (full backup with metadata)
- CSV (spreadsheet compatible)
- Markdown (plain text)
- PDF (formatted document)

**Import From:**
- NoteItUP backup (JSON)
- **Day One** - Full journal import with photos
- **Joplin** - Notes import (.jex files)

---

### 📊 Statistics Dashboard

Insights into your journaling:

- Total entries count
- Writing streak tracking
- Mood distribution pie chart
- Most used tags
- Entries per month/year
- Favorites count

---

### 🎨 Customization

Make it yours:

- **Themes**: Light, Dark, System default
- **Accent Colors**: Multiple color options
- **Font Sizes**: Small, Medium, Large, Extra Large
- **Daily Reminders**: Configurable notification time
- **Branded Splash Screen**: Purple branded launch screen (Android 12+ native API with backward compat)

---

## Platform Support

| Feature | Android | iOS | Desktop |
|---------|:-------:|:---:|:-------:|
| Rich Text Editor | ✅ | ✅ | ✅ |
| On-Device AI (Gemma 4) | ✅ GPU | ❌ | ✅ CPU |
| Cloud AI (BYOK) | ✅ | ✅ | ✅ |
| Brainstorm Chat | ✅ | ✅ | ✅ |
| Cloud Sync | ✅ | ✅ | ✅ |
| Import/Export | ✅ | ✅ | ✅ |
| Image Attachments | ✅ | ✅ | ✅ |
| Biometric Unlock | ✅ | ✅ | ❌ |
| Camera Capture | ✅ | ✅ | ❌ |
| GPS Location | ✅ | ✅ | ❌ |
| Daily Reminders | ✅ | ✅ | ❌ |
| In-App Review | ✅ | ❌ | ❌ |
| Multi-Window | ❌ | ❌ | ✅ |

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| **Kotlin Multiplatform** | Shared business logic |
| **Compose Multiplatform** | Shared UI across platforms |
| **SQLDelight** | Type-safe SQL database |
| **Koin** | Dependency injection |
| **Ktor Client** | HTTP client for APIs |
| **LiteRT-LM** | On-device LLM inference (Gemma 4) |
| **kotlinx.serialization** | JSON parsing |
| **Material Design 3** | Modern UI components |
| **Richeditor-compose** | WYSIWYG editor |
| **Firebase Analytics** | Usage analytics (Android) |
| **Play In-App Review** | Rating prompts (Android) |
| **AndroidX SplashScreen** | Branded launch screen (Android) |

---

## Getting Started

### Download

<a href="https://play.google.com/store/apps/details?id=com.romreviewertools.noteitup">
  <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80" alt="Get it on Google Play"/>
</a>

### Build from Source

**Prerequisites:**
- Android Studio Hedgehog or later
- JDK 17+
- Xcode 15+ (for iOS)

```bash
# Clone the repository
git clone https://github.com/romreviewer/NoteItUP.git
cd NoteItUP

# Android
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug

# Desktop
./gradlew :composeApp:run

# iOS - Open in Xcode
open iosApp/iosApp.xcodeproj
```

---

## AI Setup Guide

### Option 1: Local AI (Gemma 4) -- Default, No API Key

The default and most private option. Zero setup if you just want it to work.

**Automatic flow:**
1. Open any diary entry
2. Tap an AI chip in the toolbar (e.g. "Fix Grammar")
3. First time: model downloads (~1.6 GB) via system download notification
4. Model loads into memory (~5-10 seconds, shows "Loading AI model...")
5. Improved text appears. Done. Works offline from now on.

**Manual setup (optional):**
1. Go to **Settings -> AI Settings**
2. Provider is already set to "Local (Gemma 4)"
3. Tap **Download Model** or **Select File** (if you already have `gemma-4-E2B-it.litertlm`)
4. Once downloaded, tap **Load Model** -> **Test Model** to verify

**Import from AI Edge Gallery:** If you use Google's [AI Edge Gallery](https://github.com/google-ai-edge/gallery) app and already have the Gemma 4 E2B model downloaded, tap "Select File" and pick the `.litertlm` file. No re-download needed.

**Requirements:** Android or Desktop, 8GB+ RAM, ~1.6 GB free storage

### Option 2: Cloud AI Providers (BYOK)

If you prefer cloud-based models or need higher quality output:

1. Go to **Settings -> AI Settings**
2. Select a cloud provider from the dropdown
3. Enter your API key
4. Tap **Test Connection** to verify

| Provider | Get API Key | Notes |
|----------|-------------|-------|
| Groq | [console.groq.com](https://console.groq.com/keys) | Free & fast |
| Google Gemini | [aistudio.google.com](https://aistudio.google.com/apikey) | Free: 1,500 req/day |
| OpenAI | [platform.openai.com](https://platform.openai.com/api-keys) | Paid: ~$0.15-5/M tokens |
| Anthropic | [console.anthropic.com](https://console.anthropic.com/settings/keys) | Paid: ~$3-15/M tokens |
| OpenRouter | [openrouter.ai](https://openrouter.ai/keys) | Some free models |
| Together AI | [api.together.xyz](https://api.together.xyz/settings/api-keys) | $25 free credit |

---

## Architecture

The app follows **Clean Architecture** with **MVI** pattern:

```
composeApp/src/
├── commonMain/              # Shared code (95%+)
│   ├── data/
│   │   ├── ai/             # AIService, LocalInferenceEngine (expect), ModelDownloadManager (expect)
│   │   ├── database/       # SQLDelight
│   │   ├── repository/     # DiaryRepo, AISettingsRepo, SecurityRepo
│   │   └── cloud/          # Google Drive, Dropbox sync
│   ├── domain/
│   │   ├── model/          # DiaryEntry, AIProvider, Mood, Tag
│   │   └── usecase/        # ImproveTextUseCase, ChatUseCase, CRUD use cases
│   └── presentation/
│       ├── screens/        # Home, Editor, AISettings, Brainstorm, etc.
│       └── components/     # AIToolbar, DiaryEntryCard, FilePicker (expect)
├── androidMain/             # LiteRT-LM GPU, DownloadManager, BiometricPrompt
├── iosMain/                 # LocalAuthentication, stubs for local AI
└── jvmMain/                 # LiteRT-LM CPU, JFileChooser, JVM database driver
```

**Key Patterns:**
- `expect/actual` for platform-specific code (AI engine, download manager, biometrics, file picker)
- Unidirectional data flow with MVI + StateFlow
- Repository pattern for data access
- Dependency injection with Koin
- Lazy model loading with `onModelLoading` callbacks for UI feedback

---

## Contributing

Contributions are welcome! Here's how you can help:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Ideas for Contributions
- 🌐 Translations to other languages
- 🎨 New themes and color schemes
- 🐛 Bug fixes and performance improvements
- 📝 Documentation improvements
- ✨ New features

---

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## Support

- ⭐ **Star** this repo if you find it useful
- 🐛 **Report bugs** via [GitHub Issues](https://github.com/romreviewer/NoteItUP/issues)
- 💡 **Request features** via [GitHub Discussions](https://github.com/romreviewer/NoteItUP/discussions)
- 📧 **Contact**: Open an issue for questions

---

<p align="center">
  Built with ❤️ using Kotlin Multiplatform & Compose Multiplatform
</p>

<p align="center">
  <a href="https://github.com/romreviewer/NoteItUP">
    <img src="https://img.shields.io/github/stars/romreviewer/NoteItUP?style=social" alt="GitHub Stars"/>
  </a>
</p>