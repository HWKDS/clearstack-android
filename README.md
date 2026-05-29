# ClearStack Android Prototype

ClearStack is an AI-powered notification management app that listens to incoming notifications and uses Google's Gemini AI to summarize conversation threads into concise, one-sentence summaries.

## 🚀 Features

- **Notification Listener**: Automatically captures incoming notifications from other apps.
- **Smart Threading**: Groups notifications by sender/conversation.
- **AI Summarization**: Uses Gemini 2.5 Flash to summarize multiple messages into a single, easy-to-read sentence.
- **Jetpack Compose UI**: A modern, responsive interface built with the latest Android tools.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Networking**: OkHttp
- **AI Integration**: Google Generative AI (Gemini API)
- **CI/CD**: GitHub Actions for automated APK builds and releases

## ⚙️ Setup & Installation

### 1. Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 28 (Android 9.0) or higher
- A Google Gemini API Key (Get one at [Google AI Studio](https://aistudio.google.com/))

### 2. Clone the Repository
```bash
git clone https://github.com/your-username/clearstack-android.git
cd clearstack-android
```

### 3. Configure API Key
Before running the app, you must add your Gemini API key:
1. Open the project in Android Studio.
2. Navigate to `app/src/main/java/com/example/clearstackprototype1/GeminiService.kt`.
3. Locate the `API_KEY` constant:
   ```kotlin
   private const val API_KEY = "your_api_key_here"
   ```
4. Replace `"your_api_key_here"` with your actual API key.

### 4. Build and Run
- Sync the project with Gradle files.
- Connect an Android device or start an emulator.
- Click **Run 'app'** in Android Studio.

## 📱 How to Use

1. **Enable Notification Access**: When you first open the app, click the **"Enable Notification Access"** button. This will take you to Android settings where you must toggle on access for **ClearStack Prototype**.
2. **Receive Messages**: The app will now listen for incoming notifications.
3. **View Summaries**: As you receive multiple messages from the same sender, ClearStack will automatically generate an AI summary and display it on the main screen.

## 🤖 CI/CD (GitHub Actions)

This project includes automated workflows:
- **Build**: Every push to `main` builds the APK and saves it as an Action Artifact.
- **Release**: Pushing a tag (e.g., `v1.0`, `2`) triggers a GitHub Release with the APK and source code attached.

---
*Note: This is a prototype version for demonstration purposes.*
