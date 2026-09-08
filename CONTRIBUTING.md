# Contributing to ClearStack Android

Thank you for your interest in contributing to ClearStack Android. Contributions are welcome through issues and pull requests.

## Before you start

Please check existing issues and pull requests before opening a new one. For larger changes, open an issue first to discuss the proposed approach.

When reporting a bug, include:

- a clear description of the problem
- steps to reproduce it
- expected and actual behavior
- Android version and device/emulator details
- relevant Logcat output, with personal notification content and secrets removed

When suggesting a feature, explain the use case and how it would improve the notification workflow.

## Development setup

1. Install Android Studio and configure an Android SDK with API level 37.
2. Ensure JDK 11 or newer is available.
3. Clone the repository:

   ```bash
   git clone https://github.com/HWKDS/clearstack-android.git
   cd clearstack-android
   ```

4. Open the project in Android Studio and allow Gradle to sync.
5. Run the app on an Android 9+ device or emulator.
6. Enable notification access when prompted.
7. Download the Gemma model from the in-app model download screen if it is not already installed.

## Making changes

- Keep changes focused and consistent with the existing Kotlin, Jetpack Compose, Room, and Gradle conventions.
- Do not commit API keys, credentials, private notification content, generated build outputs, or local machine configuration.
- Treat notification data as sensitive. Use redacted examples in issues, tests, screenshots, and logs.
- Update documentation when behavior, setup, or user-facing workflows change.
- Add or update tests for behavior that can be tested reliably.

## Validation

Run the checks relevant to your change before opening a pull request:

```bash
./gradlew test
./gradlew assembleDebug
```

On Windows, use the Gradle wrapper batch file:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

If a check cannot be run, explain why in the pull request.

## Pull requests

Pull requests should:

- have a concise title describing the change
- explain the problem and the solution
- identify user-facing or privacy-related effects
- include screenshots or a short recording for UI changes when useful
- mention the validation commands that were run
- keep unrelated formatting or refactoring out of the diff

Maintainers may request changes, additional tests, or clarification before merging.

## Privacy and security

Please do not disclose notification contents, model files, API credentials, or other sensitive information in a public issue. For suspected security vulnerabilities, contact the repository owner privately rather than opening a public issue.

## License

By contributing to this repository, you agree that your contributions may be distributed under the repository's license.
