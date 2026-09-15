# Reminder

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-informational)](https://developer.android.com/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-success)](https://developer.android.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![CI/CD](https://img.shields.io/github/actions/workflow/status/aryaxzell/reminder/build-app.yml?branch=main&label=Build%20CI&logo=githubactions&logoColor=white)](https://github.com/aryaxzell/reminder/actions)
[![Open Issues](https://img.shields.io/github/issues/aryaxzell/reminder?color=brightgreen)](https://github.com/aryaxzell/reminder/issues)
[![Pull Requests](https://img.shields.io/github/issues-pr/aryaxzell/reminder?color=blue)](https://github.com/aryaxzell/reminder/pulls)

A native Android reminder and task management application built with Kotlin and Jetpack Compose. The application delivers an authentic iOS Human Interface Guidelines (HIG) visual experience—featuring Cupertino-style iconography, dynamic typography hierarchies, segmented cards, interactive inspectors, and an exact local alarm notification system.

Repository: [github.com/aryaxzell/reminder](https://github.com/aryaxzell/reminder)  
Package Name: `com.aryaxzell.reminder`

---

## Architecture and Core Principles

The project follows modern Android architecture best practices and Clean Architecture principles:

- **Model-View-ViewModel (MVVM)**: Strict separation of UI presentation, domain business logic, and local data access layers.
- **Unidirectional Data Flow (UDF)**: ViewModels expose immutable `StateFlow` states collected reactively by Composable screens.
- **Offline-First Persistence**: High-performance Room ORM SQLite database providing instantaneous read/write operations without network dependency.
- **Hardware & System Services**: Tight integration with Android `AlarmManager` for exact time-sensitive alert delivery and persistent broadcast receivers for device reboot recovery.
- **Cupertino Design System**: Custom-engineered Compose components reflecting iOS design metrics, including SF-style symbols, color palettes, grouped table views, and modal sheets.

---

## Features

### 1. Smart Lists & Overview Dashboard
- **Dynamic "Today" Smart Card**: Displays the current calendar day badge embedded within the icon, filtering all tasks scheduled for the active date.
- **Scheduled Smart Card**: Chronological overview of upcoming tasks with date and time tags.
- **All Reminders Smart Card**: Aggregated view across all custom lists and categories with total count indicators.
- **Flagged Smart Card**: Dedicated view for starred/flagged high-importance items.
- **Completed Archive**: Collapsible completed list with one-tap clear history action.
- **Dashboard Customization**: Edit mode allowing users to reorder or toggle the visibility of individual smart lists.

### 2. Comprehensive Reminder Inspector
- **Metadata Management**: Title, multi-line notes, and URL attachment fields.
- **Date & Time Engine**: 
  - Date toggle with an integrated calendar picker.
  - Time toggle with instant time-of-day presets:
    - Morning (09:00)
    - Afternoon (13:00)
    - Tonight (20:00)
    - Custom Time Picker.
- **Priority Stratification**: Standard four-tier priority indexing (None, Low `!`, Medium `!!`, High `!!!`).
- **Flagging**: Quick toggle for priority tracking.
- **List Migration**: Instant re-assignment to any custom user-defined list.
- **Deletion Controls**: Direct destructive action with confirmation.

### 3. Custom List Creator & Customization
- **12 Cupertino Color Palettes**: Red, Orange, Yellow, Green, Mint, Teal, Cyan, Blue, Indigo, Purple, Pink, and Brown.
- **30+ Vector Glyphs**: Categorized icon set (List, Bookmark, Pin, Gift, Birthday, Work, School, Shopping, Fitness, Health, Finance, Music, Travel, and more).
- **Interactive Header Preview**: Real-time icon and color rendering while customizing.
- **List Classification**: Support for Standard lists and Grocery lists.

### 4. Background Alarm & Notification Engine
- **Exact Alarms**: Utilizes `AlarmManager.setExactAndAllowWhileIdle()` on supported API levels to ensure timely triggers even during Doze mode.
- **Actionable Notifications**: System notifications with direct "Mark as Completed" actions and intent routing to the specific reminder item.
- **Reboot Resilience**: `BootReceiver` restores and re-registers all pending alarms upon device startup (`ACTION_BOOT_COMPLETED`).
- **Runtime Permissions**: Automated permission handling for Android 13+ (`POST_NOTIFICATIONS`) and exact alarm scheduling permissions.

### 5. Search & Organization
- **Real-Time Global Search**: Instant substring query execution matching against reminder titles and notes.
- **Smart Grouping & Sorting**: Group by due date, creation date, or priority.
- **Completion Management**: Seamless checkmark animations with automatic state sync to SQLite storage.

---

## Technical Specifications & Libraries

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.0+ | Modern expressive language with Coroutines and Flow |
| **UI Framework** | Jetpack Compose | Declarative UI toolkit with Material 3 base customized to iOS HIG |
| **Architecture** | MVVM + Repository | Clean separation of UI, domain logic, and data layer |
| **Database** | Room ORM | SQLite abstraction with KSP compiler code generation |
| **Concurrency** | Kotlin Coroutines & Flow | Asynchronous task execution and reactive stream handling |
| **Serialization** | Kotlinx Serialization | Type-safe JSON handling and data modeling |
| **Image Loading** | Coil Compose | Asynchronous raster and vector image rendering |
| **Build System** | Gradle (Kotlin DSL) | Modular build configuration with Version Catalogs |

---

## Project Structure

```
reminder/
├── .github/
│   └── workflows/
│       └── build-app.yml         # CI/CD pipeline for automated APK build and release
├── app/
│   ├── build.gradle.kts          # Module-level Gradle configuration
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/example/
│           │   ├── data/         # Room Entities, DAOs, Database, and Repository
│           │   │   ├── ReminderDao.kt
│           │   │   ├── ReminderDatabase.kt
│           │   │   ├── ReminderEntity.kt
│           │   │   ├── ReminderListEntity.kt
│           │   │   └── ReminderRepository.kt
│           │   ├── receiver/     # Alarm & Notification Broadcast Receivers
│           │   │   ├── AlarmReceiver.kt
│           │   │   ├── BootReceiver.kt
│           │   │   └── NotificationActionReceiver.kt
│           │   ├── ui/           # Jetpack Compose Screens, Components & ViewModel
│           │   │   ├── DashboardScreen.kt
│           │   │   ├── ListDetailScreen.kt
│           │   │   ├── NewListScreen.kt
│           │   │   ├── OnboardingScreen.kt
│           │   │   ├── ReminderDetailSheet.kt
│           │   │   ├── ReminderViewModel.kt
│           │   │   └── theme/    # Theme, Color Schemes, Typography
│           │   ├── MainActivity.kt
│           │   └── ReminderApplication.kt
│           └── res/              # Android Resources (strings, icons, drawables)
├── gradle/
│   └── libs.versions.toml        # Centralized Version Catalog
├── build.gradle.kts              # Root-level build configuration
├── settings.gradle.kts           # Repository and module declarations
└── README.md
```

---

## Automated CI/CD & Deployment

This repository includes an automated GitHub Actions pipeline (`.github/workflows/build-app.yml`) designed for seamless delivery:

- **Monotonic Versioning**: Automatically calculates incremental `versionCode` values from the pipeline run number (`BASE_VERSION_CODE + github.run_number`) to guarantee that newly built APKs can immediately overwrite and update existing installations on Android devices without requiring an uninstallation step.
- **Consistent Keystore Signing**: Ensures signing keys remain identical across debug and release builds, preventing `INSTALL_FAILED_UPDATE_INCOMPATIBLE` signature mismatch errors.
- **Automated Artifacts**: Every commit to `main` generates ready-to-install debug and release APKs uploaded to GitHub Actions Artifacts.
- **Automated Releases**: Pushing a version tag (e.g., `v1.0.0`) automatically packages signed APKs and publishes a formal GitHub Release.

---

## Building from Source

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- Java Development Kit (JDK) 17
- Android SDK with API Level 36 (Android 15) support

### Build Commands

1. **Clone the repository**:
   ```bash
   git clone https://github.com/aryaxzell/reminder.git
   cd reminder
   ```

2. **Assemble Debug APK**:
   ```bash
   gradle :app:assembleDebug
   ```
   The generated APK will be available at:
   `app/build/outputs/apk/debug/app-debug.apk`

3. **Assemble Release APK**:
   ```bash
   gradle :app:assembleRelease
   ```
   The generated APK will be available at:
   `app/build/outputs/apk/release/app-release.apk`

4. **Install via ADB**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## Contributing

Contributions, bug reports, and feature requests are welcome:

1. Check existing issues or open a new one via the [Issue Tracker](https://github.com/aryaxzell/reminder/issues).
2. Use the provided **Bug Report** or **Feature Request** templates.
3. Fork the repository, create a feature branch, and submit a Pull Request following the PR template.

---

## License

This project is open source and available under the [MIT License](LICENSE).
