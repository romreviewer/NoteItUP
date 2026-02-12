# Changelog

## v1.5.0 (Build 11)

### New Features
- **Persistent Brainstorm Chat History** — Brainstorm conversations now survive screen navigation and app restarts, stored via SQLDelight with schema migration for existing users
- **Native Google Drive OAuth (Android)** — Google Drive sign-in uses native AuthorizationClient instead of browser-based flow for a smoother experience
- **Joplin Image Import** — Joplin resource references (`![](:/resourceId)`) are now parsed, mapped to image attachments, and inline references cleaned from content

### Improvements
- Cloud sync screen and ViewModel refinements
- OAuthHandler updates across all platforms (Android, iOS, JVM)
- GoogleDriveAuthHelper singleton bridges Activity callbacks with coroutines

---

## v1.4.0 (Build 10)

### New Features
- **AI Brainstorm Chat** — Full chat interface with AI-powered brainstorming, starter prompts, copy and insert-to-entry
- **Firebase Analytics** — Event tracking for entries, moods, AI usage, imports, exports, and sync
- **Android In-App Review** — Smart review prompts after 5 saved entries using Google Play In-App Review API

### Improvements
- Default AI provider changed from Gemini to Groq
- Connection test button loader sizing fix
- AI Settings navigation and text formatting toggle fixes

---

## v1.3.0

### New Features
- **WYSIWYG Markdown Editor** — Rich text editing with live preview using Richeditor-compose
- Formatting toolbar with bold, italic, heading support
- Markdown cursor position and selection fixes

### Improvements
- Removed advertising ID permissions from Firebase
- Removed unnecessary READ_MEDIA_IMAGES permission (uses Photo Picker)

---

## v1.2.0

### New Features
- **API-Based AI Integration** — 6 providers (Groq, OpenAI, Claude, Gemini, OpenRouter, Together AI)
- 8 text improvement types (Journal, Grammar, Clarity, Shorter, Expand, Professional, Casual, Summarize)
- AI Settings screen with secure API key storage and connection test
- AI Toolbar in editor with suggestion dialog

---

## v1.1.0

### New Features
- **Cloud Sync** — Google Drive and Dropbox with AES-256-GCM encrypted backups
- **Day One & Joplin Import** — Import from popular journaling apps
- **Security** — PIN lock, biometric authentication (Face ID / Touch ID / Fingerprint)
- **Statistics** — Mood distribution, streaks, word counts, monthly charts

---

## v1.0.0

### Initial Release
- Diary entries with title, content, mood tracking
- Folders, tags, favorites, search
- Calendar view with month navigation
- Export (JSON, CSV, Markdown) and import
- Material 3 theming with light/dark mode and accent colors
- Cross-platform: Android, iOS, Desktop (JVM)
