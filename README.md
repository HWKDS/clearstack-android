# ClearStack Android

ClearStack is an Android prototype that listens for notifications, groups them by sender, and uses a local on-device Gemma AI model to turn a conversation thread into a concise, structured summary.

The app is designed for mobile workflows where a user wants a quick read on what is happening across messages, reminders, payments, tasks, and meetings without needing to manually scroll through every notification.

## Features

- Notification listener that captures incoming app notifications
- Thread grouping by sender/conversation
- Local AI summarization with Google LiteRT LM / Gemma
- Structured insight extraction for:
  - summary text
  - priority level
  - tasks
  - payments
  - meetings
  - reminders
  - OTP detection
- Room database persistence for notification history and saved summaries
- Jetpack Compose-based UI
- Model download flow for first-run setup

## Tech stack

- Kotlin
- Jetpack Compose + Material 3
- Android Notification Listener Service
- Room database
- Google LiteRT LM (`com.google.ai.edge.litertlm`)
- Gradle + Android Gradle Plugin

## Project structure

```text
clearstack-android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/clearstackprototype1/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── NotificationListener.kt
│   │   │   │   ├── SummaryManager.kt
│   │   │   │   ├── GemmaService.kt
│   │   │   │   ├── ModelDownloadScreen.kt
│   │   │   │   ├── ClearStackDatabase.kt
│   │   │   │   └── ...
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/
│   │   └── test/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── .gitignore
├── README.md
└── local.properties
```

## How it works

1. The app registers a `NotificationListenerService`.
2. Incoming notifications are stored in a Room database and grouped into conversation threads.
3. When a thread has enough messages, the app sends the text to `GemmaService`.
4. The model returns JSON with a one-line summary and extracted information such as tasks, reminders, payment details, meetings, and OTP codes.
5. The result is saved in app state and DB for display in the Compose UI.

## Prerequisites

- Android Studio with Android SDK configured
- JDK 11+ (the app targets Java 11)
- A device or emulator running Android 9+ (min SDK 28)
- Internet access for downloading the initial model asset

## Getting started

### 1. Clone the repo

```bash
git clone https://github.com/HWKDS/clearstack-android.git
cd clearstack-android
```

### 2. Open in Android Studio

Open the project root in Android Studio and let Gradle sync complete.

### 3. Grant notification access

When the app launches, it checks whether notification access is enabled. If not, open the Android notification access settings and allow access for the app.

### 4. Download the local model

The app includes a model download flow and downloads the Gemma model bundle into the app's internal files directory:

```text
filesDir/gemma3-1b-it.litertlm
```

The current download logic points to a GitHub Release asset:

```text
https://github.com/HWKDS/clearstack-android/releases/download/model-v1/gemma3-1b-it-int4.litertlm
```

If the model is missing, the app will prompt the user to download it before using AI summarization.

### 5. Build and run

From Android Studio:

- choose a connected device or emulator
- select the app module
- click Run

Or from the terminal:

```bash
./gradlew assembleDebug
```

## CI/CD and downloads

GitHub Actions runs unit tests and a debug build for every push and pull request. To publish a downloadable release, create and push a tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The Android release workflow attaches an installable `ClearStack-<tag>.apk` and a source archive to the GitHub Release. The website in `website/` is deployed to GitHub Pages automatically when it changes. Its Android download button reads the latest release from the GitHub API, so it always points to the newest APK.

## Important notes

- This is a prototype app and not a production-ready privacy or security product.
- Notification access is required to read sender messaging content.
- The model runs locally in the app's files directory; network is only needed for the initial model download.
- Some notification payloads may be empty or inconsistent depending on the source app.

## License

No explicit license file was found in this repository, so usage rights are currently unspecified unless otherwise stated by the project owner.
