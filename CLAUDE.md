# QR HUB Project Guidelines

## Project Structure & Paths
- Git Repository Root: `QR_HUB/`
- Android App Source: `QR_HUB/app/`
- Java SDK Path (JDK 17 LTS): `C:\Users\Admin\.jdks\jdk-17.0.12+7`
- Fast Build Commands (from `QR_HUB/`):
  - Debug APK: `JAVA_HOME="C:\Users\Admin\.jdks\jdk-17.0.12+7" ./gradlew assembleDebug`
  - Release AAB: `JAVA_HOME="C:\Users\Admin\.jdks\jdk-17.0.12+7" ./gradlew bundleRelease`
  - Install on device: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
  - Clean build: `JAVA_HOME="C:\Users\Admin\.jdks\jdk-17.0.12+7" ./gradlew clean`
  - Run lint: `JAVA_HOME="C:\Users\Admin\.jdks\jdk-17.0.12+7" ./gradlew lint`

## App Architecture
- **UI Framework:** Pure Jetpack Compose (no XML layouts)
- **Language:** 100% Kotlin
- **Key Packages** (`app/src/main/java/com/qr/hub/`):
  - `scanner/` — CameraX + ML Kit barcode scanning, result display
  - `generate/` — QR/barcode creation with custom styling (colors, logos, shapes)
  - `history/` — Scan/generate history list + detail (Room DB)
  - `data/` — Room database, DAO, repository, models
  - `viewmodel/` — HistoryViewModel (MVVM pattern)
  - `util/` — ThemeManager, ADB WiFi connector, in-app review/update
  - `util/ads/` — AdMob banner (adaptive) + interstitial ad management
  - `util/security/` — URL safety inspector for scanned links
  - `privacy/` — About, privacy policy, terms screens
  - `ui/theme/` — Material 3 theme, colors, typography
- **Entry Point:** `MainActivity.kt` — single-activity Compose Navigation host

## Wireless ADB Connection
- ADB Path: `C:\Users\Admin\AppData\Local\Android\Sdk\platform-tools\adb.exe`
- Device IP & Port: `10.228.192.18:5555`
- Quick Connect: `adb connect 10.228.192.18:5555` or run `connect_device.bat`

## App Version & Production AdMob
- Current Version: v1.7 (versionCode 9) [Target SDK: Android 16 (API 36)]
- Production AdMob App ID: `ca-app-pub-5378252094188023~4426119685`
- Banner Ad ID: `ca-app-pub-5378252094188023/8845088294` (Inline Adaptive width: `(screenWidthDp - 36).coerceAtLeast(300)`)
- Interstitial Ad ID: `ca-app-pub-5378252094188023/2694235612` (Frequency: 1 ad per 2 actions)

## Play Store & Production Details
- App Name: QR HUB - Scan, Create & Custom
- Support Email: `support.qrhub@gmail.com`
- Play Store Assets: `app_icon_512x512.png` (512x512), `feature_graphic_1024x500.png` (1024x500)
- Release Notes: `../PlayStore_Assets/RELEASE_NOTES/`
- Release Keystore: `app/qrhub_release.jks`

## Critical Gotchas & Release Rules
- **JDK Version:** Always compile with JDK 17. System default Java 25 crashes Gradle daemon.
- **Ad Layout:** Never hardcode banner ad dimensions — preserve dynamic adaptive sizing in `BannerAdView.kt`.
- **Native Debug Symbols:** Keep `ndk { debugSymbolLevel = "FULL" }` in `build.gradle.kts` release build type. Play Console warning about missing symbols is cosmetic — all .so files are pre-stripped third-party libs (AndroidX, CameraX). Cannot generate debug symbols for these; safe to ignore the warning.
- **Compose-Only UI:** No XML layouts exist. All UI is Jetpack Compose — don't create layout XML files.
- **Signing Config:** Release keystore passwords are in `build.gradle.kts` (plaintext). Don't expose them in logs or commits outside this repo.
